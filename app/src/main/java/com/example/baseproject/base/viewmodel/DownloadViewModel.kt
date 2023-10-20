package com.example.baseproject.base.viewmodel

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Environment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.baseproject.base.utils.extension.hasWritePermission
import com.example.baseproject.base.utils.extension.isSdkR
import com.example.baseproject.base.utils.extension.isSdkTIRAMISU
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * @author Huannd
 * @contributor Doanvv
 * @since 20/10/2023
 * The class is not test
 * */
class DownloadViewModel(val context: Context) : BaseViewModel() {
    private val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

    private val _permissionGranted = MutableLiveData<Boolean>()

    private val pollingIntervalMillis = 1_000L

    val downloadProgressLiveData by lazy { MutableLiveData<Int>(0) }

    /**
     * Check if the app has the write permissions.
     */
    private fun checkWritePermissions(): Boolean {
        if (isSdkR()) {
            _permissionGranted.value = true
        } else {
            _permissionGranted.value = context.hasWritePermission()
        }
        return _permissionGranted.value ?: false
    }

    /**
     * Broadcast receiver to detect when the download is complete.
     */
    private val onComplete = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (DownloadManager.ACTION_DOWNLOAD_COMPLETE == intent.action) {
                val downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                if (downloadId != -1L) {
                    val query = DownloadManager.Query()
                    query.setFilterById(downloadId)
                    val cursor = downloadManager.query(query)
                    if (cursor.moveToFirst()) {
                        val columnIndexStatus = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                        if (DownloadManager.STATUS_SUCCESSFUL == cursor.getInt(columnIndexStatus)) {
                            // TODO: Success download => Do something
                        } else {
                            // TODO: Error download => Do something
                        }
                    }
                    cursor.close()
                }
            }
        }
    }

    /**
     * Register the broadcast receiver when the ViewModel is initialized.
     */
    init {
        @SuppressLint("UnspecifiedRegisterReceiverFlag")
        if (isSdkTIRAMISU()) {
            context.registerReceiver(
                onComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE), Context.RECEIVER_NOT_EXPORTED
            )
        } else {
            context.registerReceiver(
                onComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
            )
        }

    }

    /**
     * Check the download progress and update the LiveData.
     * @param downloadId: Long -> The ID of the download.
     */
    private fun checkDownloadProgress(downloadId: Long) {
        val query = DownloadManager.Query().setFilterById(downloadId)
        val cursor = downloadManager.query(query)
        if (cursor.moveToFirst()) {
            val columnIndexBytesDownloaded = cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
            val columnIndexBytesTotal = cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)

            if (columnIndexBytesDownloaded != -1 && columnIndexBytesTotal != -1) {
                val bytesDownloaded = cursor.getLong(columnIndexBytesDownloaded)
                val bytesTotal = cursor.getLong(columnIndexBytesTotal)

                val percent = (bytesDownloaded / bytesTotal.toFloat()) * 100
                downloadProgressLiveData.postValue(percent.toInt())
            }
        }
        cursor.close()
    }

    /**
     * Download a video from the given URL.
     *
     * @param videoUrl: String -> The URL of the video to download.
     * @param title: String -> The title of the video.
     * @param description: String -> The description of the video.
     */
    fun downloadVideo(videoUrl: String, title: String, description: String) {
        if (_permissionGranted.value != true && checkWritePermissions()) {
            val request = DownloadManager.Request(Uri.parse(videoUrl))
                .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
                .setAllowedOverRoaming(false).setTitle(title).setMimeType("video/mp4").setDescription(description)
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)

            // Set the local destination for the downloaded file
            request.setDestinationInExternalPublicDir(
                Environment.DIRECTORY_DOWNLOADS, "$title.mp4"
            )

            // Start download
            val downloadId = downloadManager.enqueue(request)

            // Delay the initial progress check
            checkDownloadProgressAndPoll(downloadId)
        }
    }

    /**
     * Check the download progress and poll again after [pollingIntervalMillis] milliseconds.
     *
     * @param downloadId: Long -> The ID of the download.
     */
    private fun checkDownloadProgressAndPoll(downloadId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            while (true) {
                // Checking and update the LiveData
                checkDownloadProgress(downloadId)

                // If the download is complete or failed, break the loop
                if (downloadProgressLiveData.value!! >= 100) {
                    break
                }

                // Delay the next check
                delay(pollingIntervalMillis)
            }
        }
    }
}