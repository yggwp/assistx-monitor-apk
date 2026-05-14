package com.assistx.monitor.ui.dashboard

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.assistx.monitor.R
import com.assistx.monitor.databinding.FragmentDashboardBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class DashboardFragment : Fragment(R.layout.fragment_dashboard) {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private val viewModel: DashboardViewModel by viewModels()
    private lateinit var deviceAdapter: DeviceAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentDashboardBinding.bind(view)

        setupRecyclerView()
        setupFilterChips()
        setupSwipeRefresh()
        setupSearch()
        observeData()

        viewModel.loadDevices()
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                viewModel.setSearchQuery(s?.toString() ?: "")
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun setupRecyclerView() {
        deviceAdapter = DeviceAdapter()
        binding.rvDevices.layoutManager = LinearLayoutManager(requireContext())
        binding.rvDevices.adapter = deviceAdapter
    }

    private fun setupFilterChips() {
        binding.chipAll.setOnClickListener { viewModel.setFilter(DeviceFilter.ALL) }
        binding.chipOnline.setOnClickListener { viewModel.setFilter(DeviceFilter.ONLINE) }
        binding.chipOffline.setOnClickListener { viewModel.setFilter(DeviceFilter.OFFLINE) }
        binding.chipAlerts.setOnClickListener { viewModel.setFilter(DeviceFilter.ALERTS) }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.refresh()
        }
    }

    private fun observeData() {
        lifecycleScope.launch {
            viewModel.filteredDevices.collectLatest { devices ->
                deviceAdapter.submitList(devices)
            }
        }

        lifecycleScope.launch {
            viewModel.isLoading.collectLatest { loading ->
                binding.swipeRefresh.isRefreshing = loading
            }
        }

        lifecycleScope.launch {
            viewModel.counts.collectLatest { counts ->
                binding.chipAll.text = "Semua (${counts.total})"
                binding.chipOnline.text = "Online (${counts.online})"
                binding.chipOffline.text = "Offline (${counts.offline})"
                binding.chipAlerts.text = "Peringatan (${counts.alert})"
            }
        }

        lifecycleScope.launch {
            viewModel.activeFilter.collectLatest { filter ->
                binding.chipAll.isChecked = filter == DeviceFilter.ALL
                binding.chipOnline.isChecked = filter == DeviceFilter.ONLINE
                binding.chipOffline.isChecked = filter == DeviceFilter.OFFLINE
                binding.chipAlerts.isChecked = filter == DeviceFilter.ALERTS
            }
        }

        lifecycleScope.launch {
            viewModel.error.collectLatest { msg ->
                binding.tvError.text = msg
                binding.tvError.visibility = View.VISIBLE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
