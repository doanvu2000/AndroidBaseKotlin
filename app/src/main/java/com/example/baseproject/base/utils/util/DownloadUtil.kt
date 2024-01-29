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
            val outputStream: FileOutputStream
            val outputFile: File
            try {
                outputFile = File(cacheDir, fileName)
                outputStream = FileOutputStream(outputFile)
                val url = URL(src)
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                val inputStream: InputStream = connection.inputStream
                val fileLength = connection.contentLength
                val buffer = ByteArray(1024)
                var total: Int = 0
                var bytesRead: Int
                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    outputStream.write(buffer, 0, bytesRead)
                    total += bytesRead
                    //updateUIDownload(total * 100 / fileLength)
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
}