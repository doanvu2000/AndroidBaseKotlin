import android.content.Context
import androidx.collection.LruCache
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

sealed class NetworkResult<out T> {
    data class Success<out T>(val data: T) : NetworkResult<T>()
    data class Error(val exception: Exception) : NetworkResult<Nothing>()
    data class Progress(val bytesRead: Long, val contentLength: Long, val progress: Int) :
        NetworkResult<Nothing>()
}

data class UrlFetcherConfig(
    val timeoutMs: Int = 10000,
    val enableCache: Boolean = true,
    val maxCacheSize: Int = 4 * 1024 * 1024  // 4MB
)

class UrlFetcher<T>(
    private val context: Context,
    private val converter: (String) -> T
) {
    private var coroutineScope: CoroutineScope? = null
    private val cache = LruCache<String, CacheEntry<T>>(DEFAULT_CACHE_SIZE)

    companion object {
        private const val DEFAULT_CACHE_SIZE = 4 * 1024 * 1024  // 4MB
        private const val CACHE_DURATION_MS = 5 * 60 * 1000     // 5 minutes
        const val BUFFER_SIZE = 8192
    }

    private data class CacheEntry<T>(
        val data: T,
        val timestamp: Long
    )

    fun setCoroutineScope(scope: CoroutineScope) {
        this.coroutineScope = scope
    }

    fun fetch(
        urlString: String,
        config: UrlFetcherConfig = UrlFetcherConfig()
    ): Flow<NetworkResult<T>> = flow {
        coroutineScope?.ensureActive()

        // Check cache first if enabled
        if (config.enableCache) {
            cache[urlString]?.let { cacheEntry ->
                if (timeNow() - cacheEntry.timestamp < CACHE_DURATION_MS) {
                    emit(NetworkResult.Success(cacheEntry.data))
                    return@flow
                }
            }
        }

        try {
            val url = URL(urlString)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = config.timeoutMs
                readTimeout = config.timeoutMs
                requestMethod = "GET"
            }

            try {
                when (connection.responseCode) {
                    HttpURLConnection.HTTP_OK -> {
                        val contentLength = connection.contentLength.toLong()
                        val result = StringBuilder()
                        var totalBytesRead = 0L

                        connection.inputStream.use { input ->
                            val buffer = ByteArray(BUFFER_SIZE)
                            var bytesRead: Int

                            while (input.read(buffer).also { bytesRead = it } != -1) {
                                coroutineScope?.ensureActive()

                                totalBytesRead += bytesRead
                                result.append(String(buffer, 0, bytesRead))

                                val progress = if (contentLength > 0) {
                                    (totalBytesRead * 100 / contentLength).toInt()
                                } else {
                                    -1
                                }

                                emit(
                                    NetworkResult.Progress(
                                        totalBytesRead,
                                        contentLength,
                                        progress
                                    )
                                )
                            }
                        }

                        val stringResult = result.toString()
                        val convertedResult = converter(stringResult)

                        // Cache the result if enabled
                        if (config.enableCache) {
                            cache.put(urlString, CacheEntry(convertedResult, timeNow()))
                        }

                        emit(NetworkResult.Success(convertedResult))
                    }

                    else -> {
                        val errorMessage = connection.errorStream?.bufferedReader()?.readText()
                            ?: "Error: ${connection.responseCode} ${connection.responseMessage}"
                        emit(NetworkResult.Error(IOException(errorMessage)))
                    }
                }
            } finally {
                connection.disconnect()
            }
        } catch (e: Exception) {
            val error = when (e) {
                is IOException -> e
                is SecurityException -> IOException("Security error: ${e.message}", e)
                else -> IOException("Unknown error: ${e.message}", e)
            }
            emit(NetworkResult.Error(error))
        }
    }.flowOn(Dispatchers.IO)

    fun clearCache() {
        cache.evictAll()
    }

    private fun timeNow() = System.currentTimeMillis()
}