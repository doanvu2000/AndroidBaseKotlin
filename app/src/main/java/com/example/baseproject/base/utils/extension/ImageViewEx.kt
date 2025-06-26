package com.example.baseproject.base.utils.extension

import android.app.Activity
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.Bitmap
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.ImageView
import androidx.core.graphics.drawable.toBitmap
import coil.Coil
import coil.request.ImageRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.Calendar

//region Image Tinting

/**
 * Áp dụng grayscale effect cho ImageView
 */
fun ImageView.setTint() {
    val matrix = ColorMatrix()
    matrix.setSaturation(0f)
    val filter = ColorMatrixColorFilter(matrix)
    this.colorFilter = filter
}

/**
 * Xóa tint effect khỏi ImageView
 */
fun ImageView.clearTint() {
    this.colorFilter = null
}

//endregion

//region Coil Image Loading

/**
 * Load image với Coil library và callback success
 */
suspend fun ImageView.loadSrc(src: Any, onSuccess: () -> Unit): Bitmap? {
    return withContext(Dispatchers.IO) {
        try {
            Coil.imageLoader(context).execute(
                ImageRequest.Builder(context).data(src).build()
            ).drawable?.toBitmap()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

/**
 * Download image từ URL bằng Coil
 */
suspend fun Context.downloadImageWithCoil(url: String): Bitmap? {
    return withContext(Dispatchers.IO) {
        try {
            Coil.imageLoader(this@downloadImageWithCoil).execute(
                ImageRequest.Builder(this@downloadImageWithCoil).data(url).build()
            ).drawable?.toBitmap()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

/**
 * Get bitmap từ URL với callback
 */
fun Context.getBitmapUrl(url: String, onDone: (Bitmap?) -> Unit) {
    CoroutineScope(Dispatchers.IO).launch {
        val bitmap = async { downloadImageWithCoil(url) }
        onDone(bitmap.await())
    }
}

//endregion

//region Image Type Management

/**
 * Enum cho loại image
 */
enum class TypeImage {
    JPEG, PNG
}

/**
 * Biến global cho loại image hiện tại
 */
var typeImage = TypeImage.JPEG

//endregion

//region Image Saving

/**
 * Save bitmap vào thư viện Pictures
 */
fun saveBitmapToPicture(bitmap: Bitmap, onDone: (File?) -> Unit) =
    CoroutineScope(Dispatchers.IO).launch {
        val calendar = Calendar.getInstance()

        val fileName = when (typeImage) {
            TypeImage.JPEG -> "image${calendar.timeInMillis}.jpeg"
            TypeImage.PNG -> "image${calendar.timeInMillis}.png"
        }

        val outputFile: File = if (isSdk30()) {
            File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                    .toString() + "/" + fileName
            )
        } else {
            File(Environment.getExternalStorageDirectory().toString() + "/" + fileName)
        }

        try {
            val outputStream: OutputStream = FileOutputStream(outputFile)
            when (typeImage) {
                TypeImage.JPEG -> {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
                }
                TypeImage.PNG -> {
                    bitmap.compress(Bitmap.CompressFormat.PNG, 90, outputStream)
                }
            }
            outputStream.close()
            withContext(Dispatchers.Main) {
                onDone.invoke(outputFile)
            }
        } catch (e: IOException) {
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                onDone.invoke(null)
            }
        }
    }

/**
 * Save image từ URL vào device gallery
 */
fun Activity.saveImageToPictureDevice(url: String, onDone: (Uri?) -> Unit) {
    glideLoadBitmap(url) { bitmap ->
        val name = "${now()}"
        bitmap?.let {
            CoroutineScope(Dispatchers.IO).launch {
                val rs = async { insertImage(contentResolver, bitmap, name) }
                onDone.invoke(rs.await())
            }
        } ?: run {
            onDone(null)
        }
    }
}

/**
 * Insert image vào MediaStore
 */
private suspend fun insertImage(resolver: ContentResolver, bitmap: Bitmap, fileName: String): Uri? =
    withContext(Dispatchers.IO) {
        val fos: OutputStream?
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            val type = if (fileName.endsWith("png")) {
                "image/png"
            } else {
                "image/jpeg"
            }
            put(MediaStore.MediaColumns.MIME_TYPE, type)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
            uri?.let {
                resolver.update(it, contentValues, null, null)
            }
        }
        return@withContext uri
    }

//endregion

//region Image Sharing

/**
 * Share file image với các apps khác
 */
fun Context.shareFileImage(file: File, title: String? = "") {
    val uri = getUriByFileProvider(file)
    val mimeType = when (typeImage) {
        TypeImage.JPEG -> "image/jpeg"
        TypeImage.PNG -> "image/png"
    }

    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = mimeType
        putExtra(Intent.EXTRA_STREAM, uri)
        putExtra(Intent.EXTRA_SUBJECT, title)
        putExtra(Intent.EXTRA_TEXT, title)
    }

    val chooser = Intent.createChooser(shareIntent, "Share $title")
    val resInfoList: List<ResolveInfo> = packageManager.queryIntentActivities(
        chooser, PackageManager.MATCH_DEFAULT_ONLY
    )

    for (resolveInfo in resInfoList) {
        val packageName = resolveInfo.activityInfo.packageName
        grantUriPermission(
            packageName,
            uri,
            Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION
        )
    }
    startActivity(chooser)
}

//endregion
