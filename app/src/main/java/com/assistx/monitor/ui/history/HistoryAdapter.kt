package com.assistx.monitor.ui.history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.assistx.monitor.R
import com.assistx.monitor.databinding.CardHistoryItemBinding

class HistoryAdapter : ListAdapter<TimelineItem, HistoryAdapter.ViewHolder>(DiffCallback()) {

    class ViewHolder(val binding: CardHistoryItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = CardHistoryItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position) ?: return
        val ctx = holder.binding.root.context

        with(holder.binding) {
            tvTimestamp.text = item.timestamp
            tvOnline.text = "${item.online} online"
            tvOffline.text = "${item.offline} offline"
            tvCpu.text = "CPU ${item.avgCpu.toInt()}%"
            tvMem.text = "RAM ${item.avgMem.toInt()}%"

            tvOnline.setTextColor(ContextCompat.getColor(ctx, R.color.success_green))
            tvOffline.setTextColor(ContextCompat.getColor(ctx, R.color.danger_red))
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<TimelineItem>() {
        override fun areItemsTheSame(oldItem: TimelineItem, newItem: TimelineItem): Boolean =
            oldItem.timestamp == newItem.timestamp

        override fun areContentsTheSame(oldItem: TimelineItem, newItem: TimelineItem): Boolean =
            oldItem == newItem
    }
}
