package com.example.baseproject.base.utils.util

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

object FileUriHelper {
    const val TAG = "FileUriHelper"

    // Method 1: Get file path from content URI
    fun getFilePathFromContentUri(context: Context, uri: Uri): String? {
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = context.contentResolver.query(uri, projection, null, null, null)

        return cursor?.use {
            val columnIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            it.moveToFirst()
            it.getString(columnIndex)
        }
    }

    // Method 2: Copy URI content to a local file
    fun copyUriToFile(context: Context, uri: Uri, destinationFile: File): Boolean {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val outputStream = FileOutputStream(destinationFile)

            inputStream?.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
            true
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }

    // Method 3: Get file name from URI
    fun getFileNameFromUri(context: Context, uri: Uri): String? {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (index > 0) {
                        result = it.getString(index)
                    }
                }
            }
        }

        if (result == null) {
            result = uri.path
            val cut = result?.lastIndexOf('/')
            if (cut != null && cut != -1) {
                result = result?.substring(cut + 1)
            }
        }

        return result
    }

    // Method 4: Get file size from URI
    fun getFileSizeFromUri(context: Context, uri: Uri): Long {
        var fileSize: Long = -1
        val cursor = context.contentResolver.query(uri, null, null, null, null)

        cursor?.use {
            val sizeIndex = it.getColumnIndex(OpenableColumns.SIZE)
            if (it.moveToFirst() && !it.isNull(sizeIndex)) {
                fileSize = it.getLong(sizeIndex)
            }
        }

        return fileSize
    }

    // Example usage in a function
    fun processFileFromUri(context: Context, uri: Uri, callback: (File?) -> Unit) {
        // Get file name
        val fileName = getFileNameFromUri(context, uri)
        Log.d(TAG, "File Name: $fileName")
//        println("File Name: $fileName")

        // Get file size
//        val fileSize = getFileSizeFromUri(context, uri)
//        Log.d(TAG, "File Size: $fileSize bytes")
        // Copy to local file
        val localFile = File(context.cacheDir, fileName ?: "temp_file")
        if (copyUriToFile(context, uri, localFile)) {
            Log.d(TAG, "File copied successfully to: ${localFile.absolutePath}")
            callback(localFile)
        }
    }
}