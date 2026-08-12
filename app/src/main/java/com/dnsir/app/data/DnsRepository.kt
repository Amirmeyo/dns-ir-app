package com.dnsir.app.data

class DnsRepository(
    private val api: DnsApiService = DnsApiService.create()
) {
    suspend fun fetchDnsList(): Result<List<DnsEntry>> {
        return try {
            val response = api.getDnsList(RemoteConfig.REMOTE_DNS_URL)
            if (response.servers.isNotEmpty()) {
                Result.success(response.servers)
            } else {
                Result.success(fallbackDnsList)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
