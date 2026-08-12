package com.dnsir.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dnsir.app.data.DnsEntry
import com.dnsir.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DnsListScreen(
    dnsList: List<DnsEntry>,
    selectedDns: DnsEntry?,
    isLoading: Boolean,
    fetchError: Boolean,
    onSelect: (DnsEntry) -> Unit,
    onRefresh: () -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        containerColor = DarkBg,
        topBar = {
            TopAppBar(
                title = { Text("لیست DNS", color = TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null, tint = TextPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = onRefresh) {
                        Icon(Icons.Default.Refresh, contentDescription = null, tint = NeonCyan)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBg)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            if (isLoading) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = NeonCyan,
                    trackColor = DarkSurface
                )
            }
            if (fetchError) {
                Text(
                    text = "خطا در دریافت لیست، لیست پیش‌فرض نمایش داده شد",
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(12.dp)
                )
            }
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(DarkBg),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(dnsList) { entry ->
                    DnsCard(entry = entry, isSelected = entry.id == selectedDns?.id) {
                        onSelect(entry)
                    }
                }
            }
        }
    }
}

@Composable
private fun DnsCard(entry: DnsEntry, isSelected: Boolean, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) NeonPurple.copy(alpha = 0.25f) else DarkSurface
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(entry.title, color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                if (isSelected) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = AccentGreen)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("IPv4", color = TextSecondary, fontSize = 11.sp)
            entry.ipv4.forEach {
                Text(it, color = NeonCyan, fontSize = 13.sp)
            }
            if (entry.ipv6.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text("IPv6", color = TextSecondary, fontSize = 11.sp)
                entry.ipv6.forEach {
                    Text(it, color = NeonCyan, fontSize = 12.sp)
                }
            }
            entry.note?.let {
                Spacer(modifier = Modifier.height(6.dp))
                Text(it, color = TextSecondary, fontSize = 11.sp)
            }
        }
    }
}
