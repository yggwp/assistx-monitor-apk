package com.assistx.monitor.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.assistx.monitor.network.ApiClient
import com.assistx.monitor.network.ApiService
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HistoryViewModel : ViewModel() {

    private val _summary = MutableStateFlow<AnalyticsSummaryUi?>(null)
    val summary: StateFlow<AnalyticsSummaryUi?> = _summary

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableSharedFlow<String?>()
    val error: SharedFlow<String?> = _error

    fun loadSummary(range: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = ApiClient.getApiService().getAnalyticsSummary(range)
                _summary.value = AnalyticsSummaryUi(
                    labels = result.labels,
                    onlineCounts = result.online_counts,
                    offlineCounts = result.offline_counts,
                    avgCpus = result.avg_cpu,
                    avgMems = result.avg_memory
                )
            } catch (e: Exception) {
                _error.emit("Gagal memuat history: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }
}

data class AnalyticsSummaryUi(
    val labels: List<String>,
    val onlineCounts: List<Int>,
    val offlineCounts: List<Int>,
    val avgCpus: List<Double>,
    val avgMems: List<Double>
)
