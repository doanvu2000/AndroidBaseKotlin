package com.example.baseproject.base.network

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job

class NetWorkViewModel : ViewModel() {
    private var currentFetchJob: Job? = null
    private lateinit var stringFetcher: UrlFetcher<String>


    fun initialize(context: Context) {
        stringFetcher = UrlFetcher(context) { it }
        stringFetcher.setCoroutineScope(viewModelScope)
    }

    // Fetch plain text
    fun fetchString(
        url: String,
        timeoutMs: Int = 10000,
        enableCache: Boolean = true,
        maxCacheSize: Int = 4 * 1024 * 1024
    ) = stringFetcher.fetch(
        url, config = UrlFetcherConfig(timeoutMs, enableCache, maxCacheSize)
    )

    fun cancelFetch() {
        currentFetchJob?.cancel()
        currentFetchJob = null
    }

    fun clearCache() {
        stringFetcher.clearCache()
    }
}