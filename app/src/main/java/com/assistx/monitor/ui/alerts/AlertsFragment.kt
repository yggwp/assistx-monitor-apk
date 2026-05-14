package com.assistx.monitor.ui.alerts

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.assistx.monitor.R
import com.assistx.monitor.databinding.FragmentAlertsBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class AlertsFragment : Fragment(R.layout.fragment_alerts) {

    private var _binding: FragmentAlertsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AlertsViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAlertsBinding.bind(view)

        val alertsAdapter = AlertsAdapter()
        binding.rvAlerts.layoutManager = LinearLayoutManager(requireContext())
        binding.rvAlerts.adapter = alertsAdapter

        lifecycleScope.launch {
            viewModel.alertDevices.collectLatest { devices ->
                alertsAdapter.submitList(devices)
                binding.tvEmpty.visibility = if (devices.isEmpty()) View.VISIBLE else View.GONE
                binding.rvAlerts.visibility = if (devices.isEmpty()) View.GONE else View.VISIBLE
            }
        }

        lifecycleScope.launch {
            viewModel.alertCount.collectLatest { count ->
                binding.tvAlertCount.text = "$count perangkat perlu perhatian"
            }
        }

        viewModel.loadAlertDevices()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
