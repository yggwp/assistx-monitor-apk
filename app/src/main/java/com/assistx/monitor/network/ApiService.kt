package com.assistx.monitor.network

import com.assistx.monitor.data.model.PcDevice
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @GET("api/clients")
    suspend fun getClients(): List<PcDevice>

    @POST("api/clients")
    suspend fun addClient(@Body client: Map<String, String>): ClientResponse

    @PUT("api/clients/{id}")
    suspend fun updateClient(@Path("id") id: String, @Body client: Map<String, String>): ClientResponse

    @DELETE("api/clients/{id}")
    suspend fun deleteClient(@Path("id") id: String): StatusResponse

    @GET("api/clients/{id}/history")
    suspend fun getClientHistory(@Path("id") id: String): List<HistoryEntry>

    @GET("api/analytics/summary")
    suspend fun getAnalyticsSummary(@Query("range") range: String): AnalyticsSummary

    @GET("api/pcs")
    suspend fun getPcs(): List<PcDevice>

    data class ClientResponse(
        val status: String,
        val client: PcDevice? = null,
        val error: String? = null
    )

    data class StatusResponse(
        val status: String? = null,
        val error: String? = null
    )

    data class HistoryEntry(
        val timestamp: String,
        val status: String,
        val cpu_usage: Double,
        val memory_usage: Double,
        val anydesk_status: Int = 0
    )

    data class AnalyticsSummary(
        val labels: List<String> = emptyList(),
        val online_counts: List<Int> = emptyList(),
        val offline_counts: List<Int> = emptyList(),
        val avg_cpu: List<Double> = emptyList(),
        val avg_memory: List<Double> = emptyList(),
        val range: String = "daily"
    )
}
