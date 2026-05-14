package com.assistx.monitor.ui.settings

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.assistx.monitor.R
import com.assistx.monitor.databinding.FragmentSettingsBinding
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SettingsFragment : Fragment(R.layout.fragment_settings) {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SettingsViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentSettingsBinding.bind(view)

        lifecycleScope.launch {
            val serverUrl = viewModel.serverUrl.first()
            binding.etServerUrl.setText(serverUrl)
        }

        lifecycleScope.launch {
            val darkMode = viewModel.darkMode.first()
            binding.switchDarkMode.isChecked = darkMode
        }

        lifecycleScope.launch {
            val alertsEnabled = viewModel.alertsEnabled.first()
            binding.switchAlerts.isChecked = alertsEnabled
        }

        lifecycleScope.launch {
            val pollInterval = viewModel.pollInterval.first()
            binding.etPollInterval.setText(pollInterval.toString())
        }

        binding.switchDarkMode.setOnCheckedChangeListener { _, checked ->
            viewModel.setDarkMode(checked)
            requireActivity().recreate()
        }

        binding.switchAlerts.setOnCheckedChangeListener { _, checked ->
            viewModel.setAlertsEnabled(checked)
        }

        binding.btnSaveServer.setOnClickListener {
            val url = binding.etServerUrl.text.toString().trim()
            viewModel.setServerUrl(url)
            com.assistx.monitor.network.ApiClient.onServerUrlChanged(url)
            binding.tvSaveStatus.text = "✅ URL server disimpan! Silakan restart aplikasi."
            binding.tvSaveStatus.visibility = View.VISIBLE
        }

        binding.btnSavePoll.setOnClickListener {
            val seconds = binding.etPollInterval.text.toString().toIntOrNull() ?: 5
            viewModel.setPollInterval(seconds)
            binding.tvPollStatus.text = "✅ Interval disimpan!"
            binding.tvPollStatus.visibility = View.VISIBLE
        }

        binding.btnOverlayPermission.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.canDrawOverlays(requireContext())) {
                    val intent = Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        android.net.Uri.parse("package:${requireContext().packageName}")
                    )
                    startActivity(intent)
                } else {
                    binding.tvOverlayStatus.text = "✅ Izin overlay sudah diberikan"
                    binding.tvOverlayStatus.visibility = View.VISIBLE
                }
            }
        }

        // Check overlay permission status
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Settings.canDrawOverlays(requireContext())) {
                binding.btnOverlayPermission.text = "✅ Izin Popup Sudah Aktif"
                binding.btnOverlayPermission.isEnabled = false
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
