package com.assistx.monitor.ui.alerts

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.assistx.monitor.R
import com.assistx.monitor.data.model.PcDevice
import com.assistx.monitor.databinding.CardAlertItemBinding

class AlertsAdapter : ListAdapter<PcDevice, AlertsAdapter.ViewHolder>(DiffCallback()) {

    class ViewHolder(val binding: CardAlertItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = CardAlertItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val device = getItem(position) ?: return
        val ctx = holder.binding.root.context

        with(holder.binding) {
            tvDeviceName.text = device.name
            tvLocation.text = device.location

            when {
                !device.isOnline -> {
                    alertType.text = "OFFLINE"
                    alertType.setTextColor(ContextCompat.getColor(ctx, R.color.danger_red))
                    alertDetail.text = device.error ?: "Perangkat tidak merespon"
                    alertIcon.text = "🔴"
                }
                device.isQuotaCritical -> {
                    alertType.text = "KUOTA KRITIS"
                    alertType.setTextColor(ContextCompat.getColor(ctx, R.color.danger_red))
                    alertDetail.text = "Kuota tinggal ${device.displayQuota}"
                    alertIcon.text = "📉"
                }
                device.isQuotaWarning -> {
                    alertType.text = "KUOTA RENDAH"
                    alertType.setTextColor(ContextCompat.getColor(ctx, R.color.warning_orange))
                    alertDetail.text = "Kuota tinggal ${device.displayQuota}"
                    alertIcon.text = "⚠️"
                }
                else -> {
                    alertType.text = "ONLINE"
                    alertType.setTextColor(ContextCompat.getColor(ctx, R.color.success_green))
                    alertDetail.text = "Berfungsi normal"
                    alertIcon.text = "✅"
                }
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<PcDevice>() {
        override fun areItemsTheSame(oldItem: PcDevice, newItem: PcDevice): Boolean =
            oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: PcDevice, newItem: PcDevice): Boolean =
            oldItem == newItem
    }
}
