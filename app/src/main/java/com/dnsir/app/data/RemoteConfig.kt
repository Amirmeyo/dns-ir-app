package com.dnsir.app.data

object RemoteConfig {
    const val REMOTE_DNS_URL =
        "https://raw.githubusercontent.com/Amirmeyo/dns-ir-app/main/dns-list.json"
}

val fallbackDnsList = listOf(
    DnsEntry(
        id = "1",
        title = "⚡️ DNS #1",
        ipv4 = listOf("114.114.114.114", "78.160.38.248"),
        ipv6 = listOf("2a02:ff01:3344::2903:91b0:c", "2a02:ff01:3344::2903:de59:b")
    ),
    DnsEntry(
        id = "2",
        title = "⚡️ DNS #2",
        ipv4 = listOf("84.200.69.80", "84.208.90.42"),
        ipv6 = listOf("2a00:801::856b:0fb8:c", "2a00:801::856b:5b73:b")
    ),
    DnsEntry(
        id = "3",
        title = "⚡️ DNS #3",
        ipv4 = listOf("114.114.114.114", "37.236.231.20"),
        ipv6 = listOf("2a02:ff01:3344::2903:de59:b", "2a02:ff01:3344::2903:91b0:c")
    ),
    DnsEntry(
        id = "4",
        title = "⚡️ DNS #4",
        ipv4 = listOf("114.114.114.114", "80.128.189.208"),
        ipv6 = listOf("2001:608:1a2b::fa00:29e2:c", "2001:608:1a2b::fa00:252f:b")
    )
)
