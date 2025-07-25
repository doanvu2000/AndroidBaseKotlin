package com.example.baseproject.base.utils.util

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.ContentUris
import android.content.ContentValues
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import com.example.baseproject.base.utils.extension.isSdk30
import com.example.baseproject.base.utils.extension.isSdkQ
import com.example.baseproject.base.utils.extension.now
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

object ImageUtil {

    //region Constants

    const val TYPE_JPEG = "image/jpeg"

    //endregion

    //region Image Saving

    /**
     * Save photo to external storage
     * Required permission: WRITE_EXTERNAL_STORAGE (if SDK < 29)
     * @param contentResolver content resolver
     * @param name image name
     * @param bmp bitmap to save
     * @return true if successful, false otherwise
     */
    @SuppressLint("InlinedApi")
    fun savePhotoToExternalStorage(contentResolver: ContentResolver, name: String, bmp: Bitmap?): Boolean {
        val imageCollection: Uri = if (isSdkQ()) {
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } else {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }

        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "$name.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, TYPE_JPEG)
            if (bmp != null) {
                put(MediaStore.Images.Media.WIDTH, bmp.width)
                put(MediaStore.Images.Media.HEIGHT, bmp.height)
            }
        }

        return try {
            contentResolver.insert(imageCollection, contentValues)?.also { uri ->
                contentResolver.openOutputStream(uri).use { outputStream ->
                    if (bmp != null && outputStream != null) {
                        if (!bmp.compress(Bitmap.CompressFormat.JPEG, 95, outputStream)) {
                            throw IOException("Failed to save Bitmap")
                        }
                    }
                }
            } ?: throw IOException("Failed to create Media Store entry")
            true
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Insert sharing image và return Uri
     * @param contentResolver content resolver
     * @param bitmap bitmap to insert
     * @return Uri of inserted image
     */
    fun insertSharingImage(contentResolver: ContentResolver, bitmap: Bitmap): Uri? {
        val name = "${now()}"
        return insertImage(contentResolver, bitmap, name)
    }

    /**
     * Insert image vào MediaStore
     * @param resolver content resolver
     * @param bitmap bitmap to insert
     * @param fileName file name
     * @return Uri of inserted image
     */
    private fun insertImage(resolver: ContentResolver, bitmap: Bitmap, fileName: String): Uri? {
        val fos: java.io.OutputStream?
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, TYPE_JPEG)
            if (isSdkQ()) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }
        }

        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        uri?.let { resolver.openOutputStream(it) }.also { fos = it }
        fos?.use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        fos?.flush()
        fos?.close()

        contentValues.clear()
        if (isSdkQ()) {
            contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
            uri?.let {
                resolver.update(it, contentValues, null, null)
            }
        }
        return uri
    }

    //endregion

    //region Gallery Operations

    /**
     * Retrieve all images from gallery
     * @param contentResolver content resolver
     * @return List of image URIs
     */
    suspend fun retrieveImagesFromGallery(contentResolver: ContentResolver): List<Uri> =
        withContext(Dispatchers.IO) {
            val imageList = mutableListOf<Uri>()
            val projection = arrayOf(MediaStore.Images.Media._ID)
            val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"

            val query = contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                sortOrder
            )

            query?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val contentUri = ContentUris.withAppendedId(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        id
                    )
                    imageList.add(contentUri)
                }
            }
            return@withContext imageList
        }

    //endregion

    //region GIF Operations

    /**
     * Save GIF from URL to Pictures directory
     * Required WRITE_EXTERNAL_STORAGE permission with API below 30
     * @param gifUrl URL of GIF
     * @param fileName file name without extension
     * @param onDone callback with result file or null if failed
     */
    suspend fun saveGifFromUrlToPicture(
        gifUrl: String,
        fileName: String,
        onDone: (File?) -> Unit
    ) = withContext(Dispatchers.IO) {
        try {
            val outputFile: File = if (isSdk30()) {
                File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                        .toString() + "/$fileName.gif"
                )
            } else {
                File(Environment.getExternalStorageDirectory().toString() + "/$fileName.gif")
            }

            val outputStream = FileOutputStream(outputFile)
            val url = URL(gifUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"

            val inputStream: InputStream = connection.inputStream
            val buffer = ByteArray(1024)
            var bytesRead: Int

            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
            }

            outputStream.close()
            inputStream.close()
            connection.disconnect()

            onDone.invoke(outputFile)
        } catch (e: IOException) {
            e.printStackTrace()
            onDone.invoke(null)
        }
    }

    //endregion
}