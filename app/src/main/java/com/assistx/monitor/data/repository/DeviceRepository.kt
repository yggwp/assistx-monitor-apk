package com.assistx.monitor.data.repository

import com.assistx.monitor.data.model.PcDevice
import com.assistx.monitor.network.ApiClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Singleton repository untuk sharing data antara MonitoringService dan UI fragments.
 * Service menulis data dari SSE/polling, Fragment membaca via StateFlow.
 */
object DeviceRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _devices = MutableStateFlow<List<PcDevice>>(emptyList())
    val devices: StateFlow<List<PcDevice>> = _devices

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _lastUpdate = MutableStateFlow(0L)
    val lastUpdate: StateFlow<Long> = _lastUpdate

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    /**
     * Dipanggil oleh SSE atau polling untuk update data.
     */
    fun updateDevices(devices: List<PcDevice>) {
        _devices.value = devices
        _lastUpdate.value = System.currentTimeMillis()
        _isLoading.value = false
        _error.value = null
    }

    /**
     * Dipanggil oleh fragment untuk fetch manual (pull-to-refresh).
     */
    fun fetchFromServer() {
        scope.launch {
            _isLoading.value = true
            try {
                val result = ApiClient.getApiService().getClients()
                updateDevices(result)
            } catch (e: Exception) {
                _error.value = "Gagal terhubung: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    fun getCachedDevices(): List<PcDevice> = _devices.value
}
