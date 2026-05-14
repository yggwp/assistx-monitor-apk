package com.assistx.monitor.service

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationCompat
import com.assistx.monitor.AssistXApplication
import com.assistx.monitor.R
import com.assistx.monitor.data.model.PcDevice
import com.assistx.monitor.ui.MainActivity
import com.assistx.monitor.ui.popup.AlertPopupActivity
import java.util.concurrent.atomic.AtomicInteger

class AlertManager private constructor(private val context: Context) {

    companion object {
        @Volatile
        private var instance: AlertManager? = null
        fun getInstance(context: Context): AlertManager {
            return instance ?: synchronized(this) {
                instance ?: AlertManager(context.applicationContext).also { instance = it }
            }
        }
    }

    private val prefs = AssistXApplication.instance.preferencesManager
    private var lastAlertTime = java.util.concurrent.ConcurrentHashMap<String, Long>()
    private val notifIdCounter = AtomicInteger((System.currentTimeMillis() % Int.MAX_VALUE).toInt())

    fun checkAndAlert(previous: PcDevice, current: PcDevice) {
        val now = System.currentTimeMillis()

        // Device went offline (was online, now offline)
        if (previous.isOnline && !current.isOnline) {
            val key = "offline_${current.id}"
            val lastTime = lastAlertTime[key] ?: 0L
            if (now - lastTime > 5 * 60 * 1000L) {
                lastAlertTime[key] = now
                showAlertNotification(
                    title = "\u26A0\uFE0F ${current.name} OFFLINE!",
                    body = "${current.location} - Perangkat tidak merespon. Segera periksa!"
                )
                showPopupAlert(current, "OFFLINE")
            }
        }

        // Device recovered (was offline, now online)
        if (!previous.isOnline && current.isOnline) {
            val key = "recovery_${current.id}"
            val lastTime = lastAlertTime[key] ?: 0L
            if (now - lastTime > 10 * 60 * 1000L) {
                lastAlertTime[key] = now
                showAlertNotification(
                    title = "\u2705 ${current.name} Sudah ONLINE",
                    body = "${current.location} - Perangkat telah kembali terhubung."
                )
                showPopupAlert(current, "RECOVERED")
            }
        }

        // Quota critical
        if (!previous.isQuotaCritical && current.isQuotaCritical) {
            val key = "quota_${current.id}"
            val lastTime = lastAlertTime[key] ?: 0L
            if (now - lastTime > 30 * 60 * 1000L) {
                lastAlertTime[key] = now
                showAlertNotification(
                    title = "\uD83D\uDCC9 Kuota ${current.name} KRITIS!",
                    body = "${current.location} - Kuota tinggal ${current.displayQuota}. Segera isi ulang!"
                )
            }
        }
    }

    /**
     * Show notification — internal visibility so AssistXFCMService can call directly.
     */
    internal fun showAlertNotification(title: String, body: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context, notifIdCounter.incrementAndGet(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, AssistXApplication.CHANNEL_ALERTS)
            .setSmallIcon(R.drawable.ic_alert)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .build()

        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.notify(notifIdCounter.incrementAndGet(), notification)
    }

    private fun showPopupAlert(device: PcDevice, alertType: String) {
        // Hanya tampilkan popup jika izin overlay sudah diberikan
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(context)) {
            return // fallback hanya notifikasi
        }
        try {
            val intent = Intent(context, AlertPopupActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                putExtra("device_name", device.name)
                putExtra("device_location", device.location)
                putExtra("device_ip", device.ipAddress)
                putExtra("alert_type", alertType)
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            // Kalau gagal (misal di background restricted), notifikasi saja sudah cukup
        }
    }
}
