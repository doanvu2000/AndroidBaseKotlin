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
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.ImageView
import androidx.core.graphics.drawable.toBitmap
import coil.Coil
import coil.request.ImageRequest
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
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

fun ImageView.setTint() {
    val matrix = ColorMatrix()
    matrix.setSaturation(0f)

    val filter = ColorMatrixColorFilter(matrix)
    this.colorFilter = filter
}

fun ImageView.clearTint() {
    this.colorFilter = null
}

/**
 * Glide lib
 */

fun ImageView.loadSrc(src: Any) {
    Glide.with(this.context)
        .load(src)
        .into(this)
}

fun ImageView.loadSrc(src: Any, error: Int) {
    Glide.with(this.context)
        .load(src)
        .error(error)
        .into(this)
}

fun ImageView.loadSrc(src: Any, placeHolder: Int, error: Int) {
    Glide.with(this.context)
        .load(src)
        .placeholder(placeHolder)
        .error(error)
        .into(this)
}

fun ImageView.loadGif(src: Any) {
    Glide.with(this.context)
        .asGif()
        .load(src)
        .into(this)
}

/*
 update: 04/11/2024
 by: doan-vu.dev
 */
fun ImageView.loadSrcNoCacheRam(src: Any) {
    Glide.with(this.context)
        .load(src)
        .skipMemoryCache(true)
        .into(this)
}

fun ImageView.loadSrcNoCacheDisk(src: Any) {
    Glide.with(this.context)
        .load(src)
        .diskCacheStrategy(DiskCacheStrategy.NONE)
        .into(this)
}

fun ImageView.loadSrcNoCache(src: Any) {
    Glide.with(this.context)
        .load(src)
        .skipMemoryCache(true)
        .diskCacheStrategy(DiskCacheStrategy.NONE)
        .into(this)
}

fun ImageView.loadSrcCacheData(src: Any) {
    Glide.with(this.context)
        .load(src)
        .skipMemoryCache(true)
        .diskCacheStrategy(DiskCacheStrategy.DATA)
        .into(this)
}

fun ImageView.loadSrcCacheResource(src: Any) {
    Glide.with(this.context)
        .load(src)
        .skipMemoryCache(true)
        .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
        .into(this)
}

fun ImageView.loadSrcCacheAllToDisk(src: Any) {
    Glide.with(this.context)
        .load(src)
        .skipMemoryCache(true)
        .diskCacheStrategy(DiskCacheStrategy.ALL)
        .into(this)
}

/**
 * Coil Lib
 * */

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

suspend fun Context.downloadImageWithCoil(url: String): Bitmap? {
    return withContext(Dispatchers.IO) {
        try {
            Coil.imageLoader(this@downloadImageWithCoil).execute(
                ImageRequest.Builder(this@downloadImageWithCoil)
                    .data(url)
                    .build()
            ).drawable?.toBitmap()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

fun Context.getBitmapUrl(url: String, onDone: (Bitmap?) -> Unit) {
    CoroutineScope(Dispatchers.IO).launch {
        val bitmap = async { downloadImageWithCoil(url) }
        onDone(bitmap.await())
    }
}

fun Context.glideLoadBitmap(url: Any, onDone: (Bitmap?) -> Unit) {
    val target = object : CustomTarget<Bitmap>() {
        override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
            onDone(resource)
        }

        override fun onLoadFailed(errorDrawable: Drawable?) {
            super.onLoadFailed(errorDrawable)
            onDone(null)
        }

        override fun onLoadCleared(placeholder: Drawable?) {

        }
    }
    Glide.with(this).asBitmap().load(url).skipMemoryCache(false)
        .diskCacheStrategy(DiskCacheStrategy.NONE).timeout(15000).into(target)
}

enum class TypeImage {
    JPEG, PNG
}

var typeImage = TypeImage.JPEG

fun saveBitmapToPicture(bitmap: Bitmap, onDone: (File?) -> Unit) =
    CoroutineScope(Dispatchers.IO).launch {
        val outputFile: File
        // Create a new file in the Pictures directory
        val calendar = Calendar.getInstance()

        val fileName = when (typeImage) {
            TypeImage.JPEG -> "image${calendar.timeInMillis}.png"
            TypeImage.PNG -> "image${calendar.timeInMillis}.jpeg"
        }
        outputFile = if (isSdk30()) {
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

fun Context.shareFileImage(file: File, title: String? = "") {
    val uri = getUriByFileProvider(file)
    val typeImage = when (typeImage) {
        TypeImage.JPEG -> {
            "image/jpeg"
        }

        TypeImage.PNG -> {
            "image/png"
        }
    }
    val shareIntent = Intent(Intent.ACTION_SEND)
    shareIntent.apply {
        type = typeImage // Set the MIME type for images
        putExtra(Intent.EXTRA_STREAM, uri)
        // Optionally, add a subject or text to the shared content
        putExtra(Intent.EXTRA_SUBJECT, title)
        putExtra(Intent.EXTRA_TEXT, title)
    }
    val chooser = Intent.createChooser(shareIntent, "Share $title")
    val resInfoList: List<ResolveInfo> = packageManager.queryIntentActivities(
        chooser,
        PackageManager.MATCH_DEFAULT_ONLY
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

fun Activity.saveImageToPictureDevice(url: String, onDone: (Uri?) -> Unit) {
    glideLoadBitmap(url) { bitmap ->
        val name = "${now()}"
        bitmap?.let {
            CoroutineScope(Dispatchers.IO).launch {
                val rs = async { insertImage(contentResolver, bitmap, name) }
                onDone.invoke(rs.await())
            }
        } ?: kotlin.run {
            onDone(null)
        }
    }
}

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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) { // this one
                put(
                    MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES
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
        return@withContext uri
    }