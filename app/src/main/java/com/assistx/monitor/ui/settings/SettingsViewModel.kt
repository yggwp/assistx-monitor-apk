package com.assistx.monitor.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.assistx.monitor.AssistXApplication
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class SettingsViewModel : ViewModel() {

    private val prefs = AssistXApplication.instance.preferencesManager

    val serverUrl: Flow<String> = prefs.serverUrl
    val darkMode: Flow<Boolean> = prefs.darkMode
    val alertsEnabled: Flow<Boolean> = prefs.alertsEnabled
    val pollInterval: Flow<Int> = prefs.pollInterval

    fun setServerUrl(url: String) {
        viewModelScope.launch { prefs.setServerUrl(url) }
    }

    fun setDarkMode(enabled: Boolean) {
        viewModelScope.launch { prefs.setDarkMode(enabled) }
    }

    fun setAlertsEnabled(enabled: Boolean) {
        viewModelScope.launch { prefs.setAlertsEnabled(enabled) }
    }

    fun setPollInterval(seconds: Int) {
        viewModelScope.launch { prefs.setPollInterval(seconds) }
    }
}
