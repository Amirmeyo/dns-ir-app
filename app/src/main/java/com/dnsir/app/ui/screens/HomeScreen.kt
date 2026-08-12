package com.dnsir.app.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dnsir.app.data.DnsEntry
import com.dnsir.app.ui.theme.*
import com.dnsir.app.viewmodel.ConnectionState

@Composable
fun HomeScreen(
    connectionState: ConnectionState,
    selectedDns: DnsEntry?,
    onConnectClick: () -> Unit,
    onGoToDnsList: () -> Unit,
    onGoToAbout: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAnim"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("DNS IR", color = AccentGreen, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Row {
                TextButton(onClick = onGoToDnsList) { Text("لیست DNS", color = NeonCyan) }
                TextButton(onClick = onGoToAbout) { Text("درباره ما", color = NeonCyan) }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            val statusColor = when (connectionState) {
                ConnectionState.CONNECTED -> AccentGreen
                ConnectionState.CONNECTING -> NeonCyan
                ConnectionState.DISCONNECTED -> TextSecondary
            }
            val statusText = when (connectionState) {
                ConnectionState.CONNECTED -> "متصل شد"
                ConnectionState.CONNECTING -> "در حال اتصال…"
                ConnectionState.DISCONNECTED -> "قطع شده"
            }

            Box(
                modifier = Modifier
                    .size(200.dp)
                    .graphicsLayer {
                        if (connectionState == ConnectionState.CONNECTED) {
                            scaleX = pulse; scaleY = pulse
                        }
                    }
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(statusColor.copy(alpha = 0.35f), DarkSurface)
                        )
                    )
                    .clickable { onConnectClick() },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(NeonPurple, NeonCyan))),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (connectionState == ConnectionState.CONNECTED) "قطع اتصال" else "اتصال",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text(statusText, color = statusColor, fontSize = 16.sp, fontWeight = FontWeight.Medium)

            Spacer(modifier = Modifier.height(32.dp))
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onGoToDnsList() }
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("DNS انتخاب‌شده", color = TextSecondary, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = selectedDns?.title ?: "هیچ DNS انتخاب نشده",
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (selectedDns != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = selectedDns.ipv4.joinToString("  •  "),
                            color = NeonCyan,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        ) {
            AdBannerPlaceholder()
        }
    }
}

@Composable
fun AdBannerPlaceholder() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .height(50.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(DarkSurface),
        contentAlignment = Alignment.Center
    ) {
        Text("جای بنر تبلیغاتی (AdMob)", color = TextSecondary, fontSize = 11.sp)
    }
}
