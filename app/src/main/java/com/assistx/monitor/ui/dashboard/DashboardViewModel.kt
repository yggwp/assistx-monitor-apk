package com.assistx.monitor.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.assistx.monitor.data.model.PcDevice
import com.assistx.monitor.data.repository.DeviceRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class DashboardViewModel : ViewModel() {

    private val repository = DeviceRepository

    private val _filteredDevices = MutableStateFlow<List<PcDevice>>(emptyList())
    val filteredDevices: StateFlow<List<PcDevice>> = _filteredDevices

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableSharedFlow<String>()
    val error: SharedFlow<String> = _error

    private val _activeFilter = MutableStateFlow(DeviceFilter.ALL)
    val activeFilter: StateFlow<DeviceFilter> = _activeFilter

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _counts = MutableStateFlow(DeviceCounts())
    val counts: StateFlow<DeviceCounts> = _counts

    data class DeviceCounts(
        val total: Int = 0,
        val online: Int = 0,
        val offline: Int = 0,
        val alert: Int = 0
    )

    init {
        // Observe repository dengan efisien tanpa fetch ulang
        viewModelScope.launch {
            repository.devices.collectLatest { devices ->
                applyFilters(devices)
            }
        }
        viewModelScope.launch {
            repository.isLoading.collectLatest { loading ->
                _isLoading.value = loading
            }
        }
        viewModelScope.launch {
            repository.error.collectLatest { error ->
                if (error != null) _error.emit(error)
            }
        }
    }

    fun loadDevices() {
        // Pakai data cache dulu, fetch hanya jika kosong
        val cached = repository.getCachedDevices()
        if (cached.isNotEmpty()) {
            applyFilters(cached)
        } else {
            repository.fetchFromServer()
        }
    }

    fun setFilter(filter: DeviceFilter) {
        _activeFilter.value = filter
        applyFilters(repository.getCachedDevices())
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
        applyFilters(repository.getCachedDevices())
    }

    fun refresh() {
        repository.fetchFromServer()
    }

    private fun applyFilters(allDevices: List<PcDevice>) {
        val query = _searchQuery.value.lowercase()
        val filter = _activeFilter.value

        // Update counts
        _counts.value = DeviceCounts(
            total = allDevices.size,
            online = allDevices.count { it.isOnline },
            offline = allDevices.count { !it.isOnline },
            alert = allDevices.count { it.isQuotaCritical || it.isQuotaWarning }
        )

        // Filter
        val filtered = allDevices.filter { device ->
            val matchesFilter = when (filter) {
                DeviceFilter.ALL -> true
                DeviceFilter.ONLINE -> device.isOnline
                DeviceFilter.OFFLINE -> !device.isOnline
                DeviceFilter.ALERTS -> device.isQuotaCritical || device.isQuotaWarning
            }
            val matchesSearch = if (query.isBlank()) true else {
                device.name.lowercase().contains(query) ||
                        device.ipAddress.contains(query) ||
                        device.location.lowercase().contains(query) ||
                        (device.simcardNumber?.contains(query) ?: false)
            }
            matchesFilter && matchesSearch
        }
        _filteredDevices.value = filtered
    }
}

enum class DeviceFilter(val label: String) {
    ALL("Semua"),
    ONLINE("Online"),
    OFFLINE("Offline"),
    ALERTS("Peringatan")
}
