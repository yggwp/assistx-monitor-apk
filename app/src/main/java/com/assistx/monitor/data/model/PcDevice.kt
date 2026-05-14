package com.assistx.monitor.data.model

import com.google.gson.annotations.SerializedName

/**
 * Model data untuk perangkat POC yang dimonitor.
 */
data class PcDevice(
    @SerializedName("id")
    val id: String = "",

    @SerializedName("name")
    val name: String = "Unknown Device",

    @SerializedName("endpoint")
    val endpoint: String = "",

    @SerializedName("ip_address")
    val ipAddress: String = "N/A",

    @SerializedName("location")
    val location: String = "N/A",

    @SerializedName("anydesk")
    val anydeskId: String = "N/A",

    @SerializedName("anydesk_id")
    val anydeskIdAlt: String? = null,

    @SerializedName("simcard_number")
    val simcardNumber: String? = null,

    @SerializedName("quota")
    val quotaText: String = "N/A",

    @SerializedName("quota_link")
    val quotaLink: String? = null,

    @SerializedName("status")
    val status: String = "offline",

    @SerializedName("anydesk_status")
    val anydeskStatus: Int = 0,

    @SerializedName("cpu_usage")
    val cpuUsage: Double = 0.0,

    @SerializedName("memory_usage")
    val memoryUsage: Double = 0.0,

    @SerializedName("error")
    val error: String? = null
) {
    val isOnline: Boolean get() = status == "online"
    val isAnyDeskRunning: Boolean get() = anydeskStatus == 1
    val displayAnyDeskId: String get() = anydeskIdAlt ?: anydeskId
    val displayQuota: String get() = quotaText
    
    /**
     * Ekstrak nilai kuota dalam GB (jika tersedia).
     */
    val quotaGb: Float?
        get() {
            val match = Regex("([0-9.]+)\\s*GB", RegexOption.IGNORE_CASE).find(quotaText)
            return match?.groupValues?.get(1)?.toFloatOrNull()
        }

    val isQuotaCritical: Boolean
        get() = (quotaGb ?: Float.MAX_VALUE) < 1.0f

    val isQuotaWarning: Boolean
        get() {
            val gb = quotaGb ?: return false
            return gb in 1.0f..4.99f
        }
    
    /**
     * Status prioritas untuk filter: online > warning > offline
     */
    val alertState: AlertState
        get() = when {
            isQuotaCritical && isOnline -> AlertState.QUOTA_CRITICAL
            isQuotaWarning && isOnline -> AlertState.QUOTA_WARNING
            !isOnline -> AlertState.OFFLINE
            else -> AlertState.ONLINE
        }
}

enum class AlertState {
    ONLINE,
    OFFLINE,
    QUOTA_WARNING,
    QUOTA_CRITICAL
}
