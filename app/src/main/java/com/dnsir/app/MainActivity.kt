package com.dnsir.app

import android.content.Intent
import android.net.Uri
import android.net.VpnService
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.dnsir.app.ui.screens.AboutScreen
import com.dnsir.app.ui.screens.DnsListScreen
import com.dnsir.app.ui.screens.HomeScreen
import com.dnsir.app.ui.screens.SplashScreen
import com.dnsir.app.ui.theme.DnsIrTheme
import com.dnsir.app.viewmodel.ConnectionState
import com.dnsir.app.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    private lateinit var vpnPermissionLauncher: androidx.activity.result.ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            vpnPermissionLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.StartActivityForResult()
            ) { result ->
                if (result.resultCode == RESULT_OK) {
                    startVpnService()
                } else {
                    viewModel.setConnectionState(ConnectionState.DISCONNECTED)
                }
            }

            DnsIrTheme {
                var showSplash by remember { mutableStateOf(true) }

                if (showSplash) {
                    SplashScreen(onFinished = { showSplash = false })
                } else {
                    AppNavHost()
                }
            }
        }
    }

    @Composable
    private fun AppNavHost() {
        val navController = rememberNavController()
        val dnsList by viewModel.dnsList.collectAsState()
        val isLoading by viewModel.isLoading.collectAsState()
        val fetchError by viewModel.fetchError.collectAsState()
        val connectionState by viewModel.connectionState.collectAsState()
        val selectedDns by viewModel.selectedDns

        NavHost(navController = navController, startDestination = "home") {
            composable("home") {
                HomeScreen(
                    connectionState = connectionState,
                    selectedDns = selectedDns,
                    onConnectClick = { toggleConnection() },
                    onGoToDnsList = { navController.navigate("dnsList") },
                    onGoToAbout = { navController.navigate("about") }
                )
            }
            composable("dnsList") {
                DnsListScreen(
                    dnsList = dnsList,
                    selectedDns = selectedDns,
                    isLoading = isLoading,
                    fetchError = fetchError,
                    onSelect = { viewModel.selectDns(it) },
                    onRefresh = { viewModel.loadDnsList() },
                    onBack = { navController.popBackStack() }
                )
            }
            composable("about") {
                AboutScreen(
                    onOpenInstagram = { openInstagram() },
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }

    private fun toggleConnection() {
        if (viewModel.connectionState.value == ConnectionState.CONNECTED) {
            stopVpnService()
        } else {
            requestVpnPermissionAndConnect()
        }
    }

    private fun requestVpnPermissionAndConnect() {
        viewModel.setConnectionState(ConnectionState.CONNECTING)
        val intent = VpnService.prepare(this)
        if (intent != null) {
            vpnPermissionLauncher.launch(intent)
        } else {
            startVpnService()
        }
    }

    private fun startVpnService() {
        val dns = viewModel.selectedDns.value
        val primary = dns?.ipv4?.getOrNull(0) ?: "1.1.1.1"
        val secondary = dns?.ipv4?.getOrNull(1)

        val intent = Intent(this, DnsVpnService::class.java).apply {
            action = DnsVpnService.ACTION_CONNECT
            putExtra(DnsVpnService.EXTRA_DNS_PRIMARY, primary)
            secondary?.let { putExtra(DnsVpnService.EXTRA_DNS_SECONDARY, it) }
        }
        startForegroundService(intent)
        viewModel.setConnectionState(ConnectionState.CONNECTED)
    }

    private fun stopVpnService() {
        val intent = Intent(this, DnsVpnService::class.java).apply {
            action = DnsVpnService.ACTION_DISCONNECT
        }
        startService(intent)
        viewModel.setConnectionState(ConnectionState.DISCONNECTED)
    }

    private fun openInstagram() {
        val uri = Uri.parse("https://www.instagram.com/abol_m_officiall")
        startActivity(Intent(Intent.ACTION_VIEW, uri))
    }
}
