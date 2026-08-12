package com.dnsir.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val GameDnsColorScheme = darkColorScheme(
    primary = NeonPurple,
    secondary = NeonCyan,
    tertiary = AccentGreen,
    background = DarkBg,
    surface = DarkSurface,
    onPrimary = TextPrimary,
    onBackground = TextPrimary,
    onSurface = TextPrimary
)

@Composable
fun DnsIrTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = GameDnsColorScheme,
        typography = MaterialTheme.typography,
        content = content
    )
}
