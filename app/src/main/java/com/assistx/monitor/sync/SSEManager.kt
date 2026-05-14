package com.assistx.monitor.sync

import android.util.Log
import com.assistx.monitor.data.model.PcDevice
import com.assistx.monitor.network.ApiClient
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import okhttp3.Request
import okhttp3.Response
import okhttp3.sse.EventSource
import okhttp3.sse.EventSourceListener
import okhttp3.sse.EventSources
import java.io.IOException

class SSEManager {

    companion object {
        private const val TAG = "SSEManager"
        private const val MAX_RECONNECT_DELAY = 30_000L
    }

    private var eventSource: EventSource? = null
    private val gson = Gson()
    private var reconnectAttempts = 0
    private var isDestroyed = false

    private val _deviceUpdates = MutableSharedFlow<List<PcDevice>>(extraBufferCapacity = 1)
    val deviceUpdates: SharedFlow<List<PcDevice>> = _deviceUpdates

    private val _connectionState = MutableSharedFlow<SseState>(extraBufferCapacity = 1)
    val connectionState: SharedFlow<SseState> = _connectionState

    private val scope = CoroutineScope(Dispatchers.IO)

    fun connect() {
        if (isDestroyed) return
        disconnect()
        
        val serverUrl = ApiClient.getBaseUrl()
        val request = Request.Builder()
            .url("$serverUrl/api/stream")
            .header("Accept", "text/event-stream")
            .header("Cache-Control", "no-cache")
            .build()

        val factory = EventSources.createFactory(ApiClient.getOkHttpClient())
        
        eventSource = factory.newEventSource(request, object : EventSourceListener() {
            override fun onOpen(eventSource: EventSource, response: Response) {
                Log.i(TAG, "SSE Connected")
                reconnectAttempts = 0
                scope.launch { _connectionState.emit(SseState.CONNECTED) }
            }

            override fun onEvent(eventSource: EventSource, id: String?, type: String?, data: String) {
                try {
                    val listType = object : TypeToken<List<PcDevice>>() {}.type
                    val devices: List<PcDevice> = gson.fromJson(data, listType)
                    scope.launch { _deviceUpdates.emit(devices) }
                } catch (e: Exception) {
                    Log.w(TAG, "SSE parse error: ${e.message}")
                }
            }

            override fun onClosed(eventSource: EventSource) {
                Log.w(TAG, "SSE Closed by server")
                scope.launch { _connectionState.emit(SseState.DISCONNECTED) }
                scheduleReconnect()
            }

            override fun onFailure(eventSource: EventSource, t: Throwable?, response: Response?) {
                Log.e(TAG, "SSE Failure: ${t?.message}")
                scope.launch { _connectionState.emit(SseState.DISCONNECTED) }
                scheduleReconnect()
            }
        })
    }

    private fun scheduleReconnect() {
        if (isDestroyed) return
        val delay = (1000L * (++reconnectAttempts)).coerceAtMost(MAX_RECONNECT_DELAY)
        Log.i(TAG, "Reconnecting in ${delay}ms...")
        
        scope.launch {
            kotlinx.coroutines.delay(delay)
            connect()
        }
    }

    fun disconnect() {
        eventSource?.cancel()
        eventSource = null
    }

    fun destroy() {
        isDestroyed = true
        disconnect()
    }
}

enum class SseState {
    CONNECTED, DISCONNECTED, CONNECTING
}
