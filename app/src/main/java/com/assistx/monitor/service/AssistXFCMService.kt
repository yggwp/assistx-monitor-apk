package com.assistx.monitor.service

import android.content.Intent
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * Menerima push notification dari Firebase Cloud Messaging
 * untuk alert real-time dari server dashboard.
 */
class AssistXFCMService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "AssistXFCM"
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d(TAG, "FCM message received: ${message.from}")

        val title = message.notification?.title ?: message.data["title"]
        val body = message.notification?.body ?: message.data["body"]

        if (title != null || body != null) {
            showForegroundNotification(title ?: "AssistX Alert", body ?: "")
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.i(TAG, "FCM Token refreshed: $token")
        // NOTE: Kirim token ini ke server dashboard untuk topic subscription
    }

    private fun showForegroundNotification(title: String, body: String) {
        val alertManager = AlertManager.getInstance(this)
        // Direct call (method is internal now, no reflection needed)
        alertManager.showAlertNotification(title, body)
    }
}
