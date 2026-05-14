package com.assistx.monitor.ui.history

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.assistx.monitor.R
import com.assistx.monitor.databinding.FragmentHistoryBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class HistoryFragment : Fragment(R.layout.fragment_history) {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HistoryViewModel by viewModels()
    private lateinit var historyAdapter: HistoryAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentHistoryBinding.bind(view)

        historyAdapter = HistoryAdapter()
        binding.rvHistory.layoutManager = LinearLayoutManager(requireContext())
        binding.rvHistory.adapter = historyAdapter

        binding.toggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener
            val range = when (checkedId) {
                R.id.btnDaily -> "daily"
                R.id.btnWeekly -> "weekly"
                R.id.btnMonthly -> "monthly"
                else -> "daily"
            }
            viewModel.loadSummary(range)
        }

        observeData()
        viewModel.loadSummary("daily")
    }

    private fun observeData() {
        lifecycleScope.launch {
            viewModel.summary.collectLatest { summary ->
                if (summary != null) {
                    historyAdapter.submitList(summary.labels.zip(summary.onlineCounts)
                        .map { (label, online) ->
                            TimelineItem(
                                timestamp = label,
                                online = online,
                                offline = summary.offlineCounts.getOrElse(summary.labels.indexOf(label)) { 0 },
                                avgCpu = summary.avgCpus.getOrElse(summary.labels.indexOf(label)) { 0.0 },
                                avgMem = summary.avgMems.getOrElse(summary.labels.indexOf(label)) { 0.0 }
                            )
                        }
                    )
                }
            }
        }

        lifecycleScope.launch {
            viewModel.isLoading.collectLatest { loading ->
                binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
            }
        }

        lifecycleScope.launch {
            viewModel.error.collectLatest { msg ->
                binding.tvError.text = msg
                binding.tvError.visibility = if (msg != null) View.VISIBLE else View.GONE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

data class TimelineItem(
    val timestamp: String,
    val online: Int,
    val offline: Int,
    val avgCpu: Double,
    val avgMem: Double
)
