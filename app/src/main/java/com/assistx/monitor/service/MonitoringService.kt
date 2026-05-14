package com.assistx.monitor.service

import android.app.PendingIntent
import android.content.Intent
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.assistx.monitor.AssistXApplication
import com.assistx.monitor.R
import com.assistx.monitor.data.model.PcDevice
import com.assistx.monitor.data.repository.DeviceRepository
import com.assistx.monitor.network.ApiClient
import com.assistx.monitor.sync.SSEManager
import com.assistx.monitor.sync.SseState
import com.assistx.monitor.ui.MainActivity
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

class MonitoringService : LifecycleService() {

    companion object {
        const val NOTIFICATION_ID = 1001
        const val ACTION_STOP = "com.assistx.monitor.STOP_SERVICE"
    }

    private lateinit var sseManager: SSEManager
    private var pollingJob: Job? = null
    private var wakeLock: PowerManager.WakeLock? = null

    // Track previous device states for alert detection
    private val previousStates = ConcurrentHashMap<String, PcDevice>()

    override fun onCreate() {
        super.onCreate()
        sseManager = SSEManager()
        startForeground()
        startMonitoring()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopSelf()
            return START_NOT_STICKY
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent): IBinder? = null

    private fun startForeground() {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, AssistXApplication.CHANNEL_SERVICE)
            .setContentTitle("AssistX Monitoring")
            .setContentText("Memantau semua perangkat POC 24/7")
            .setSmallIcon(R.drawable.ic_monitor)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setSilent(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        startForeground(NOTIFICATION_ID, notification)
    }

    private fun startMonitoring() {
        // Initial fetch dari server
        lifecycleScope.launch {
            try {
                val devices = ApiClient.getApiService().getClients()
                DeviceRepository.updateDevices(devices)
                processDeviceUpdate(devices)
            } catch (_: Exception) { }
        }

        lifecycleScope.launch {
            // Collect SSE events
            launch {
                sseManager.deviceUpdates.collect { devices ->
                    DeviceRepository.updateDevices(devices)
                    processDeviceUpdate(devices)
                }
            }

            // Monitor SSE connection state
            launch {
                sseManager.connectionState.collect { state ->
                    when (state) {
                        SseState.DISCONNECTED -> startPollingFallback()
                        SseState.CONNECTED -> stopPollingFallback()
                        SseState.CONNECTING -> { /* nothing */ }
                    }
                }
            }

            // Start SSE connection
            sseManager.connect()
        }
    }

    private suspend fun startPollingFallback() {
        if (pollingJob?.isActive == true) return
        val pollSeconds = AssistXApplication.instance.preferencesManager.pollInterval.first()
        pollingJob = lifecycleScope.launch {
            while (isActive) {
                try {
                    val devices = ApiClient.getApiService().getClients()
                    DeviceRepository.updateDevices(devices)
                    processDeviceUpdate(devices)
                } catch (_: Exception) { }
                delay(pollSeconds * 1000L)
            }
        }
    }

    private fun stopPollingFallback() {
        pollingJob?.cancel()
        pollingJob = null
    }

    private fun processDeviceUpdate(devices: List<PcDevice>) {
        val alertManager = AlertManager.getInstance(this)
        for (device in devices) {
            val prev = previousStates[device.id]
            if (prev != null) {
                alertManager.checkAndAlert(prev, device)
            }
            previousStates[device.id] = device
        }
    }

    override fun onDestroy() {
        sseManager.destroy()
        pollingJob?.cancel()
        wakeLock?.release()
        super.onDestroy()
    }
}
