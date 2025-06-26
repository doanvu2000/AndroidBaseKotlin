package com.example.baseproject.base.utils.util

import androidx.lifecycle.LifecycleCoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

object DownloadUtil {

    //region Audio Download

    /**
     * Download audio file from URL
     * @param lifecycle LifecycleCoroutineScope for coroutine management
     * @param cacheDir cache directory to save file
     * @param fileName name of the output file
     * @param src source URL to download from
     * @param timeDelay delay before calling onDone callback
     * @param onDone callback when download is successful
     * @param onFail callback when download fails
     */
    fun downloadAudio(
        lifecycle: LifecycleCoroutineScope,
        cacheDir: File,
        fileName: String,
        src: String,
        timeDelay: Long = 300,
        onDone: (File) -> Unit,
        onFail: () -> Unit
    ) {
        lifecycle.launch(Dispatchers.IO) {
            try {
                val outputFile = File(cacheDir, fileName)
                val outputStream = FileOutputStream(outputFile)
                val url = URL(src)
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"

                val inputStream: InputStream = connection.inputStream
                val fileLength = connection.contentLength
                val buffer = ByteArray(1024)
                var total = 0
                var bytesRead: Int

                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    outputStream.write(buffer, 0, bytesRead)
                    total += bytesRead
                    // Optional: Update UI with download progress
                    // updateUIDownload(total * 100 / fileLength)
                }

                outputStream.close()
                inputStream.close()
                connection.disconnect()

                delay(timeDelay)
                onDone.invoke(outputFile)
            } catch (e: IOException) {
                e.printStackTrace()
                onFail.invoke()
            }
        }
    }

    //endregion
}