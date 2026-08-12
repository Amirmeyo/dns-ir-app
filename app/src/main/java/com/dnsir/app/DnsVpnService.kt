package com.dnsir.app

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.ParcelFileDescriptor
import androidx.core.app.NotificationCompat
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.util.concurrent.atomic.AtomicBoolean

class DnsVpnService : VpnService() {

    private var vpnInterface: ParcelFileDescriptor? = null
    private val isRunning = AtomicBoolean(false)
    private var workerThread: Thread? = null

    companion object {
        const val ACTION_CONNECT = "com.dnsir.app.CONNECT"
        const val ACTION_DISCONNECT = "com.dnsir.app.DISCONNECT"
        const val EXTRA_DNS_PRIMARY = "extra_dns_primary"
        const val EXTRA_DNS_SECONDARY = "extra_dns_secondary"

        private const val CHANNEL_ID = "dns_ir_vpn_channel"
        private const val NOTIF_ID = 1001
        private const val VPN_ADDRESS = "10.111.222.1"
        private const val VPN_ADDRESS_PREFIX = 32
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_DISCONNECT -> {
                stopVpn()
                return START_NOT_STICKY
            }
            ACTION_CONNECT -> {
                val primary = intent.getStringExtra(EXTRA_DNS_PRIMARY) ?: "1.1.1.1"
                val secondary = intent.getStringExtra(EXTRA_DNS_SECONDARY)
                startVpn(primary, secondary)
            }
        }
        return START_STICKY
    }

    private fun startVpn(primaryDns: String, secondaryDns: String?) {
        if (isRunning.get()) return

        startForeground(NOTIF_ID, buildNotification())

        val builder = Builder()
            .setSession(getString(R.string.app_name))
            .addAddress(VPN_ADDRESS, VPN_ADDRESS_PREFIX)
            .addDnsServer(primaryDns)
            .apply { secondaryDns?.let { addDnsServer(it) } }
            .addRoute(primaryDns, 32)

        secondaryDns?.let { builder.addRoute(it, 32) }

        vpnInterface = builder.establish() ?: run {
            stopSelf()
            return
        }

        isRunning.set(true)

        workerThread = Thread {
            runRelayLoop(primaryDns, secondaryDns)
        }.apply {
            name = "DnsIrVpnRelay"
            start()
        }
    }

    private fun runRelayLoop(primaryDns: String, secondaryDns: String?) {
        val fd = vpnInterface ?: return
        val input = FileInputStream(fd.fileDescriptor)
        val output = FileOutputStream(fd.fileDescriptor)
        val packet = ByteBuffer.allocate(32767)
        val upstreamSocket = DatagramSocket().also { protect(it) }

        try {
            while (isRunning.get()) {
                packet.clear()
                val length = input.read(packet.array())
                if (length <= 0) continue
                packet.limit(length)

                val dnsQuery = extractUdpPayloadIfDns(packet.array(), length) ?: continue

                try {
                    val outPacket = DatagramPacket(
                        dnsQuery.payload,
                        dnsQuery.payload.size,
                        InetSocketAddress(primaryDns, 53)
                    )
                    upstreamSocket.send(outPacket)

                    val respBuf = ByteArray(4096)
                    val respPacket = DatagramPacket(respBuf, respBuf.size)
                    upstreamSocket.soTimeout = 4000
                    upstreamSocket.receive(respPacket)

                    val ipResponse = buildIpv4UdpResponse(
                        originalPacket = packet.array(),
                        dnsPayload = respBuf,
                        dnsPayloadLength = respPacket.length
                    )
                    output.write(ipResponse)
                } catch (e: Exception) {
                }
            }
        } catch (e: Exception) {
        } finally {
            upstreamSocket.close()
        }
    }

    private data class DnsPayload(val payload: ByteArray, val ipHeaderLen: Int, val srcPort: Int, val dstPort: Int)

    private fun extractUdpPayloadIfDns(buf: ByteArray, length: Int): DnsPayload? {
        if (length < 20) return null
        val versionAndIhl = buf[0].toInt() and 0xFF
        val version = versionAndIhl shr 4
        if (version != 4) return null
        val ihl = (versionAndIhl and 0x0F) * 4
        val protocol = buf[9].toInt() and 0xFF
        if (protocol != 17) return null
        if (length < ihl + 8) return null

        val srcPort = ((buf[ihl].toInt() and 0xFF) shl 8) or (buf[ihl + 1].toInt() and 0xFF)
        val dstPort = ((buf[ihl + 2].toInt() and 0xFF) shl 8) or (buf[ihl + 3].toInt() and 0xFF)
        if (dstPort != 53) return null

        val udpLen = ((buf[ihl + 4].toInt() and 0xFF) shl 8) or (buf[ihl + 5].toInt() and 0xFF)
        val payloadLen = udpLen - 8
        if (payloadLen <= 0 || ihl + 8 + payloadLen > length) return null

        val payload = buf.copyOfRange(ihl + 8, ihl + 8 + payloadLen)
        return DnsPayload(payload, ihl, srcPort, dstPort)
    }

    private fun buildIpv4UdpResponse(originalPacket: ByteArray, dnsPayload: ByteArray, dnsPayloadLength: Int): ByteArray {
        val ihl = (originalPacket[0].toInt() and 0x0F) * 4
        val totalLen = ihl + 8 + dnsPayloadLength
        val out = ByteArray(totalLen)

        System.arraycopy(originalPacket, 0, out, 0, ihl)
        out[0] = originalPacket[0]
        System.arraycopy(originalPacket, 16, out, 12, 4)
        System.arraycopy(originalPacket, 12, out, 16, 4)
        out[2] = ((totalLen shr 8) and 0xFF).toByte()
        out[3] = (totalLen and 0xFF).toByte()
        out[10] = 0; out[11] = 0

        val srcPort = ((originalPacket[ihl + 2].toInt() and 0xFF) shl 8) or (originalPacket[ihl + 3].toInt() and 0xFF)
        val dstPort = ((originalPacket[ihl].toInt() and 0xFF) shl 8) or (originalPacket[ihl + 1].toInt() and 0xFF)
        val udpLen = 8 + dnsPayloadLength

        out[ihl] = ((srcPort shr 8) and 0xFF).toByte()
        out[ihl + 1] = (srcPort and 0xFF).toByte()
        out[ihl + 2] = ((dstPort shr 8) and 0xFF).toByte()
        out[ihl + 3] = (dstPort and 0xFF).toByte()
        out[ihl + 4] = ((udpLen shr 8) and 0xFF).toByte()
        out[ihl + 5] = (udpLen and 0xFF).toByte()
        out[ihl + 6] = 0; out[ihl + 7] = 0

        System.arraycopy(dnsPayload, 0, out, ihl + 8, dnsPayloadLength)
        return out
    }

    private fun stopVpn() {
        isRunning.set(false)
        workerThread?.interrupt()
        workerThread = null
        try {
            vpnInterface?.close()
        } catch (_: Exception) {
        }
        vpnInterface = null
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        stopVpn()
        super.onDestroy()
    }

    private fun buildNotification(): android.app.Notification {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java)
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.app_name),
                NotificationManager.IMPORTANCE_LOW
            )
            manager.createNotificationChannel(channel)
        }

        val disconnectIntent = Intent(this, DnsVpnService::class.java).apply { action = ACTION_DISCONNECT }
        val disconnectPending = PendingIntent.getService(
            this, 0, disconnectIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.app_name))
            .setContentText(getString(R.string.connected))
            .setSmallIcon(android.R.drawable.ic_lock_lock)
            .setOngoing(true)
            .addAction(0, getString(R.string.disconnect), disconnectPending)
            .build()
    }
}
