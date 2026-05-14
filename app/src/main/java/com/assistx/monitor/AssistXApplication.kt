package com.assistx.monitor

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.assistx.monitor.data.local.PreferencesManager
import com.assistx.monitor.network.ApiClient
import com.assistx.monitor.service.MonitoringService

class AssistXApplication : Application() {

    companion object {
        const val CHANNEL_SERVICE = "assistx_monitoring_service"
        const val CHANNEL_ALERTS = "assistx_alerts"
        lateinit var instance: AssistXApplication
            private set
    }

    lateinit var preferencesManager: PreferencesManager
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        preferencesManager = PreferencesManager(this)
        ApiClient.init(preferencesManager)
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        val notificationManager = getSystemService(NotificationManager::class.java)
        
        // Service channel (silent, runs 24/7)
        val serviceChannel = NotificationChannel(
            CHANNEL_SERVICE,
            "Monitoring Service",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Menampilkan status monitoring yang berjalan 24/7"
            setShowBadge(false)
        }
        
        // Alert channel (popup + sound)
        val alertChannel = NotificationChannel(
            CHANNEL_ALERTS,
            "Alerts & Notifications",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Notifikasi penting saat ada perangkat offline atau kuota kritis"
            enableVibration(true)
            enableLights(true)
        }
        
        notificationManager.createNotificationChannel(serviceChannel)
        notificationManager.createNotificationChannel(alertChannel)
    }
}
