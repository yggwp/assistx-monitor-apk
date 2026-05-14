package com.assistx.monitor.ui

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.assistx.monitor.AssistXApplication
import com.assistx.monitor.R
import com.assistx.monitor.databinding.ActivityMainBinding
import com.assistx.monitor.service.MonitoringService
import com.google.android.material.navigation.NavigationBarView
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var settingsLauncher: androidx.activity.result.ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupNavigation()
        applyTheme()
        startMonitoringService()
    }

    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        binding.bottomNav.setupWithNavController(navController)
    }

    private fun applyTheme() {
        lifecycleScope.launch {
            AssistXApplication.instance.preferencesManager.darkMode.collect { isDark ->
                if (isDark) {
                    setTheme(R.style.Theme_AssistXMonitor_Dark)
                } else {
                    setTheme(R.style.Theme_AssistXMonitor)
                }
            }
        }
    }

    private fun startMonitoringService() {
        val intent = Intent(this, MonitoringService::class.java)
        ContextCompat.startForegroundService(this, intent)
    }
}
