package com.assistx.monitor.ui.alerts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.assistx.monitor.data.model.PcDevice
import com.assistx.monitor.data.repository.DeviceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class AlertsViewModel : ViewModel() {

    private val _alertDevices = MutableStateFlow<List<PcDevice>>(emptyList())
    val alertDevices: StateFlow<List<PcDevice>> = _alertDevices

    private val _alertCount = MutableStateFlow(0)
    val alertCount: StateFlow<Int> = _alertCount

    init {
        // Observe repository, no extra fetch needed
        viewModelScope.launch {
            DeviceRepository.devices.collectLatest { devices ->
                val alerts = devices.filter { !it.isOnline || it.isQuotaCritical || it.isQuotaWarning }
                    .sortedByDescending { if (!it.isOnline) 2 else if (it.isQuotaCritical) 1 else 0 }
                _alertDevices.value = alerts
                _alertCount.value = alerts.size
            }
        }
    }

    fun loadAlertDevices() {
        val cached = DeviceRepository.getCachedDevices()
        val alerts = cached.filter { !it.isOnline || it.isQuotaCritical || it.isQuotaWarning }
            .sortedByDescending { if (!it.isOnline) 2 else if (it.isQuotaCritical) 1 else 0 }
        _alertDevices.value = alerts
        _alertCount.value = alerts.size
    }

    fun refresh() {
        DeviceRepository.fetchFromServer()
    }
}
