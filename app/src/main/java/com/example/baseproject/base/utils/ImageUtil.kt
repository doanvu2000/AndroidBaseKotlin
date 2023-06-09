package com.example.baseproject.base.utils

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.ContentUris
import android.content.ContentValues
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.OutputStream

object ImageUtil {
    const val TYPE_JPEG = "image/jpeg"
    private fun isSdkAfter29(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return true
        }
        return false
    }

    //save to local, required permission: WRITE_EXTERNAL_STORAGE ( if sdk < 29)
    @SuppressLint("InlinedApi")
    fun savePhotoToExternalStorage(contentResolver: ContentResolver, name: String, bmp: Bitmap?): Boolean {
        val imageCollection: Uri = if (isSdkAfter29()) {
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
            contentResolver.insert(imageCollection, contentValues)?.also {
                contentResolver.openOutputStream(it).use { outputStream ->
                    if (bmp != null) {
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

    fun insertSharingImage(contentResolver: ContentResolver, bitmap: Bitmap): Uri? {
        val name = "${System.currentTimeMillis()}"
        return insertImage(contentResolver, bitmap, name)
    }

    private fun insertImage(resolver: ContentResolver, bitmap: Bitmap, fileName: String): Uri? {
        val fos: OutputStream?
//        val APP_MEDIA_FOLDER = "doan"
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, TYPE_JPEG)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) { // this one
                put(
                    MediaStore.MediaColumns.RELATIVE_PATH,
                    Environment.DIRECTORY_PICTURES
                )
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }
        }
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        uri?.let { resolver.openOutputStream(it) }.also { fos = it }
        fos?.use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        fos?.flush()
        fos?.close()
        contentValues.clear()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
            uri?.let {
                resolver.update(it, contentValues, null, null)
            }
        }
        return uri
    }

    //get all uri of gallery
    suspend fun retrieveImagesFromGallery(contentResolver: ContentResolver): List<Uri> = withContext(Dispatchers.IO) {
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
                val contentUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
                imageList.add(contentUri)
            }
        }
        return@withContext imageList
    }

}