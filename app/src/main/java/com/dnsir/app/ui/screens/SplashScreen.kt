package com.dnsir.app.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dnsir.app.ui.theme.AccentGreen
import com.dnsir.app.ui.theme.DarkBg
import com.dnsir.app.ui.theme.NeonCyan
import com.dnsir.app.ui.theme.NeonPurple
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    val scale = remember { Animatable(0.6f) }
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        scale.animateTo(1f, animationSpec = tween(700, easing = LinearOutSlowInEasing))
    }
    LaunchedEffect(Unit) {
        alpha.animateTo(1f, animationSpec = tween(900))
        delay(900)
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(NeonPurple.copy(alpha = 0.35f), DarkBg)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .graphicsLayer {
                        scaleX = scale.value
                        scaleY = scale.value
                        this.alpha = alpha.value
                    }
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(NeonPurple, NeonCyan))),
                contentAlignment = Alignment.Center
            ) {
                Text("👾", fontSize = 48.sp)
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "DNS IR",
                color = AccentGreen,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.graphicsLayer { this.alpha = alpha.value }
            )
            Text(
                text = "کاهش پینگ برای گیمرهای ایرانی",
                color = NeonCyan,
                fontSize = 14.sp,
                modifier = Modifier
                    .padding(top = 8.dp)
                    .graphicsLayer { this.alpha = alpha.value }
            )
        }
    }
}
