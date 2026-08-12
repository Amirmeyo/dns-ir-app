package com.dnsir.app.data

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Url

interface DnsApiService {

    @GET
    suspend fun getDnsList(@Url url: String): DnsListResponse

    companion object {
        fun create(): DnsApiService {
            val retrofit = Retrofit.Builder()
                .baseUrl("https://raw.githubusercontent.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            return retrofit.create(DnsApiService::class.java)
        }
    }
}
