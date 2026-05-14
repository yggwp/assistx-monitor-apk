package com.assistx.monitor.ui.dashboard

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.assistx.monitor.R
import com.assistx.monitor.data.model.PcDevice
import com.assistx.monitor.databinding.CardDeviceBinding

class DeviceAdapter : ListAdapter<PcDevice, DeviceAdapter.ViewHolder>(DiffCallback()) {

    class ViewHolder(val binding: CardDeviceBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = CardDeviceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val device = getItem(position) ?: return
        val ctx = holder.binding.root.context

        with(holder.binding) {
            tvDeviceName.text = device.name
            tvLocation.text = device.location
            tvIpAddress.text = device.ipAddress
            tvAnyDeskId.text = device.displayAnyDeskId
            tvSimcard.text = device.simcardNumber ?: "N/A"

            // Online/Offline indicator
            if (device.isOnline) {
                statusIndicator.setBackgroundResource(R.drawable.circle_online)
                statusBadge.text = "ONLINE"
                statusBadge.setTextColor(ContextCompat.getColor(ctx, R.color.success_green))
            } else {
                statusIndicator.setBackgroundResource(R.drawable.circle_offline)
                statusBadge.text = "OFFLINE"
                statusBadge.setTextColor(ContextCompat.getColor(ctx, R.color.danger_red))
            }

            // CPU Progress
            cpuProgress.progress = device.cpuUsage.toInt()
            cpuText.text = "CPU ${device.cpuUsage.toInt()}%"
            cpuProgress.setProgressTintList(
                ContextCompat.getColorStateList(
                    ctx,
                    when {
                        device.cpuUsage > 80 -> R.color.danger_red
                        device.cpuUsage > 50 -> R.color.warning_orange
                        else -> R.color.success_green
                    }
                )
            )

            // Memory Progress
            memProgress.progress = device.memoryUsage.toInt()
            memText.text = "RAM ${device.memoryUsage.toInt()}%"
            memProgress.setProgressTintList(
                ContextCompat.getColorStateList(
                    ctx,
                    when {
                        device.memoryUsage > 80 -> R.color.danger_red
                        device.memoryUsage > 50 -> R.color.warning_orange
                        else -> R.color.success_green
                    }
                )
            )

            // Quota
            quotaText.text = "Kuota: ${device.displayQuota}"
            if (device.isQuotaCritical) {
                quotaText.setTextColor(ContextCompat.getColor(ctx, R.color.danger_red))
            } else if (device.isQuotaWarning) {
                quotaText.setTextColor(ContextCompat.getColor(ctx, R.color.warning_orange))
            } else {
                quotaText.setTextColor(ContextCompat.getColor(ctx, R.color.text_secondary))
            }

            // Copy AnyDesk button
            btnCopyAnyDesk.setOnClickListener {
                val clipboard = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(ClipData.newPlainText("AnyDesk ID", device.displayAnyDeskId))
                Toast.makeText(ctx, "AnyDesk ID disalin!", Toast.LENGTH_SHORT).show()
            }

            // WhatsApp button
            if (!device.simcardNumber.isNullOrBlank()) {
                btnWhatsApp.visibility = android.view.View.VISIBLE
                btnWhatsApp.setOnClickListener {
                    val waIntent = Intent(Intent.ACTION_VIEW).apply {
                        data = Uri.parse("https://wa.me/62${device.simcardNumber?.trimStart('0')}")
                    }
                    ctx.startActivity(waIntent)
                }
            } else {
                btnWhatsApp.visibility = android.view.View.GONE
            }

            // Error message
            if (device.error != null && !device.isOnline) {
                tvError.text = device.error
                tvError.visibility = android.view.View.VISIBLE
            } else {
                tvError.visibility = android.view.View.GONE
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
