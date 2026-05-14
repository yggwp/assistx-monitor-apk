package com.assistx.monitor.ui.popup

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.assistx.monitor.R
import com.assistx.monitor.ui.MainActivity

/**
 * Popup alert yang muncul di atas semua aplikasi, bahkan saat
 * AssistX Monitor berjalan di background / layar terkunci.
 * Menggunakan SYSTEM_ALERT_WINDOW untuk tampil di atas lock screen.
 */
class AlertPopupActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alert_popup)

        val deviceName = intent.getStringExtra("device_name") ?: "Unknown Device"
        val location = intent.getStringExtra("device_location") ?: "Unknown Location"
        val ip = intent.getStringExtra("device_ip") ?: "N/A"
        val alertType = intent.getStringExtra("alert_type") ?: "OFFLINE"

        setupWindow()

        val tvTitle = findViewById<TextView>(R.id.tvPopupTitle)
        val tvMessage = findViewById<TextView>(R.id.tvPopupMessage)
        val btnClose = findViewById<Button>(R.id.btnPopupClose)
        val btnOpenApp = findViewById<Button>(R.id.btnPopupOpenApp)

        when (alertType) {
            "OFFLINE" -> {
                tvTitle.text = "⚠️ PERANGKAT OFFLINE!"
                tvMessage.text = "$deviceName\n$location\nIP: $ip\n\nPerangkat tidak merespon. Mohon segera diperiksa!"
            }
            "RECOVERED" -> {
                tvTitle.text = "✅ PERANGKAT PULIH"
                tvMessage.text = "$deviceName\n$location\n\nPerangkat sudah kembali ONLINE."
            }
            else -> {
                tvTitle.text = "AssistX Alert"
                tvMessage.text = "$deviceName\n$location"
            }
        }

        btnClose.setOnClickListener { finish() }
        btnOpenApp.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            }
            startActivity(intent)
            finish()
        }
    }

    private fun setupWindow() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            window?.setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY)
        } else {
            @Suppress("DEPRECATION")
            window?.setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT)
        }

        window?.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        )

        window?.setGravity(Gravity.CENTER)
        window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.88).toInt(),
            WindowManager.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}
