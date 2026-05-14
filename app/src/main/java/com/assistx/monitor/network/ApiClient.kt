package com.assistx.monitor.network

import com.assistx.monitor.data.local.PreferencesManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {

    private var retrofit: Retrofit? = null
    private var currentBaseUrl: String = ""
    lateinit var preferencesManager: PreferencesManager
        private set

    fun init(prefs: PreferencesManager) {
        preferencesManager = prefs
    }

    /**
     * Get the current server URL (used by SSEManager for SSE connection).
     * Memoized to avoid repeated DataStore reads.
     */
    fun getBaseUrl(): String {
        return currentBaseUrl.ifEmpty {
            runBlocking { preferencesManager.serverUrl.first() }.also { currentBaseUrl = it }
        }
    }

    /**
     * Called when user changes server URL in Settings.
     */
    fun onServerUrlChanged(newUrl: String) {
        currentBaseUrl = newUrl.trimEnd('/')
        retrofit = null // force rebuild
    }

    private val okHttpClient: OkHttpClient by lazy {
        // Use NONE logging in release for battery & CPU savings
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.NONE
        }
        OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .addInterceptor(logging)
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("Cache-Control", "no-cache")
                    .addHeader("Pragma", "no-cache")
                    .build()
                chain.proceed(request)
            }
            .build()
    }

    fun getApiService(): ApiService {
        val baseUrl = getBaseUrl()
        if (retrofit == null || currentBaseUrl != baseUrl) {
            currentBaseUrl = baseUrl
            retrofit = Retrofit.Builder()
                .baseUrl(baseUrl.plus("/"))
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
        return retrofit!!.create(ApiService::class.java)
    }

    fun getOkHttpClient(): OkHttpClient = okHttpClient
}
