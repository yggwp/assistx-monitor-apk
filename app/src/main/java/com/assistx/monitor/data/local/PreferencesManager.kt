package com.assistx.monitor.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "assistx_settings")

class PreferencesManager(private val context: Context) {

    companion object {
        private val KEY_SERVER_URL = stringPreferencesKey("server_url")
        private val KEY_AUTH_USERNAME = stringPreferencesKey("auth_username")
        private val KEY_AUTH_PASSWORD = stringPreferencesKey("auth_password")
        private val KEY_DARK_MODE = booleanPreferencesKey("dark_mode")
        private val KEY_POLL_INTERVAL = intPreferencesKey("poll_interval")
        private val KEY_ALERTS_ENABLED = booleanPreferencesKey("alerts_enabled")
        private val KEY_FIRST_RUN = booleanPreferencesKey("first_run")

        const val DEFAULT_SERVER_URL = "http://192.168.1.100:5060"
        const val DEFAULT_POLL_INTERVAL = 5
    }

    val serverUrl: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_SERVER_URL] ?: DEFAULT_SERVER_URL
    }

    val authUsername: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_AUTH_USERNAME] ?: "presales"
    }

    val authPassword: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[KEY_AUTH_PASSWORD] ?: "presales"
    }

    val darkMode: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_DARK_MODE] ?: true
    }

    val pollInterval: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[KEY_POLL_INTERVAL] ?: DEFAULT_POLL_INTERVAL
    }

    val alertsEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_ALERTS_ENABLED] ?: true
    }

    val firstRun: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_FIRST_RUN] ?: true
    }

    suspend fun setServerUrl(url: String) {
        context.dataStore.edit { it[KEY_SERVER_URL] = url.trimEnd('/') }
    }

    suspend fun setCredentials(username: String, password: String) {
        context.dataStore.edit {
            it[KEY_AUTH_USERNAME] = username
            it[KEY_AUTH_PASSWORD] = password
        }
    }

    suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { it[KEY_DARK_MODE] = enabled }
    }

    suspend fun setPollInterval(seconds: Int) {
        context.dataStore.edit { it[KEY_POLL_INTERVAL] = seconds.coerceIn(3, 30) }
    }

    suspend fun setAlertsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[KEY_ALERTS_ENABLED] = enabled }
    }

    suspend fun setFirstRunComplete() {
        context.dataStore.edit { it[KEY_FIRST_RUN] = false }
    }
}
