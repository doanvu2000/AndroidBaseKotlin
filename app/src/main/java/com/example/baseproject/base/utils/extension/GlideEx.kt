package com.example.baseproject.base.utils.extension

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.baseproject.base.utils.util.AppLogger

//region Basic Image Loading
private const val TAG = "GlideEx"

/**
 * Load image từ source (URL, resource, file, etc.)
 */
fun ImageView.loadSrc(src: Any) {
    tryCatch(tryBlock = {
        Glide.with(this.context).load(src).into(this)
    }, catchBlock = { e ->
        AppLogger.e(TAG, "loadSrc_error: ${e.message}")
    })
}

/**
 * Load image với error fallback
 */
fun ImageView.loadSrc(src: Any, error: Int) {
    tryCatch(tryBlock = {
        Glide.with(this.context).load(src).error(error).into(this)
    }, catchBlock = { e ->
        AppLogger.e(TAG, "loadSrc_error: ${e.message}")
    })
}

/**
 * Load image với placeholder và error fallback
 */
fun ImageView.loadSrc(src: Any, placeHolder: Int, error: Int) {
    tryCatch(tryBlock = {
        Glide.with(this.context).load(src).placeholder(placeHolder).error(error).into(this)
    }, catchBlock = { e ->
        AppLogger.e(TAG, "loadSrc_error: ${e.message}")
    })
}

/**
 * Load GIF animation
 */
fun ImageView.loadGif(src: Any) {
    tryCatch(tryBlock = {
        Glide.with(this.context).asGif().load(src).into(this)
    }, catchBlock = { e ->
        AppLogger.e(TAG, "loadGif_error: ${e.message}")
    })
}

//endregion

//region Cache Management

/**
 * Load image và skip RAM cache
 * Update: 04/11/2024 by: doan-vu.dev
 */
fun ImageView.loadSrcNoCacheRam(src: Any) {
    tryCatch(tryBlock = {
        Glide.with(this.context).load(src).skipMemoryCache(true).into(this)
    }, catchBlock = { e ->
        AppLogger.e(TAG, "loadSrcNoCacheRam_error: ${e.message}")
    })
}

/**
 * Load image và skip disk cache
 */
fun ImageView.loadSrcNoCacheDisk(src: Any) {
    tryCatch(tryBlock = {
        Glide.with(this.context).load(src).diskCacheStrategy(DiskCacheStrategy.NONE).into(this)
    }, catchBlock = { e ->
        AppLogger.e(TAG, "loadSrcNoCacheDisk_error: ${e.message}")
    })
}

/**
 * Load image không cache (cả RAM và Disk)
 */
fun ImageView.loadSrcNoCache(src: Any) {
    tryCatch(tryBlock = {
        Glide.with(this.context).load(src)
            .skipMemoryCache(true)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .into(this)
    }, catchBlock = { e ->
        AppLogger.e(TAG, "loadSrcNoCache_error: ${e.message}")
    })
}

/**
 * Load image với cache strategy DATA
 */
fun ImageView.loadSrcCacheData(src: Any) {
    tryCatch(tryBlock = {
        Glide.with(this.context).load(src)
            .skipMemoryCache(true)
            .diskCacheStrategy(DiskCacheStrategy.DATA)
            .into(this)
    }, catchBlock = { e ->
        AppLogger.e(TAG, "loadSrcCacheData_error: ${e.message}")
    })
}

/**
 * Load image với cache strategy RESOURCE
 */
fun ImageView.loadSrcCacheResource(src: Any) {
    tryCatch(tryBlock = {
        Glide.with(this.context).load(src)
            .skipMemoryCache(true)
            .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
            .into(this)
    }, catchBlock = { e ->
        AppLogger.e(TAG, "loadSrcCacheResource_error: ${e.message}")
    })
}

/**
 * Load image với cache strategy ALL
 */
fun ImageView.loadSrcCacheAllToDisk(src: Any) {
    tryCatch(tryBlock = {
        Glide.with(this.context).load(src)
            .skipMemoryCache(true)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(this)
    }, catchBlock = { e ->
        AppLogger.e(TAG, "loadSrcCacheAllToDisk_error: ${e.message}")
    })
}

//endregion

//region Rounded Corners

/**
 * Load image với rounded corners và cache ALL
 */
fun ImageView.loadSrcRoundedCacheAll(url: Any, radius: Int) {
    tryCatch(tryBlock = {
        Glide.with(context).load(url)
            .apply(RequestOptions.bitmapTransform(RoundedCorners(radius.dpToPx())))
            .skipMemoryCache(true)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(this)
    }, catchBlock = { e ->
        AppLogger.e(TAG, "loadSrcRoundedCacheAll_error: ${e.message}")
    })
}

/**
 * Load image với rounded corners và no cache
 */
fun ImageView.loadSrcRoundedNoCache(url: Any, radius: Int) {
    tryCatch(tryBlock = {
        context ?: return@tryCatch
        Glide.with(context).load(url)
            .apply(RequestOptions.bitmapTransform(RoundedCorners(radius.dpToPx())))
            .skipMemoryCache(true)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .into(this)
    }, catchBlock = { e ->
        AppLogger.e(TAG, "loadSrcRoundedNoCache_error: ${e.message}")
    })
}

//endregion

//region Bitmap Loading

/**
 * Load bitmap từ URL với callback
 */
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
            // No implementation needed
        }
    }

    Glide.with(this)
        .asBitmap()
        .load(url)
        .skipMemoryCache(false)
        .diskCacheStrategy(DiskCacheStrategy.NONE)
        .timeout(15000)
        .into(target)
}

//endregion
