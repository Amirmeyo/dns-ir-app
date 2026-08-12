package com.dnsir.app.data

data class DnsEntry(
    val id: String,
    val title: String,
    val ipv4: List<String> = emptyList(),
    val ipv6: List<String> = emptyList(),
    val note: String? = null
)

data class DnsListResponse(
    val updatedAt: String? = null,
    val servers: List<DnsEntry> = emptyList()
)
