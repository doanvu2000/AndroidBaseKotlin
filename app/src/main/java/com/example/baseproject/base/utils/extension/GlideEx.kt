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

/**
 * Glide lib
 */

fun ImageView.loadSrc(src: Any) {
    tryCatch(tryBlock = {
        Glide.with(this.context).load(src).into(this)
    }, catchBlock = { e ->
        AppLogger.e("ImageViewEx", "loadSrc_error: ${e.message}")
    })
}

fun ImageView.loadSrc(src: Any, error: Int) {
    tryCatch(tryBlock = {
        Glide.with(this.context).load(src).error(error).into(this)
    }, catchBlock = { e ->
        AppLogger.e("ImageViewEx", "loadSrc_error: ${e.message}")
    })
}

fun ImageView.loadSrc(src: Any, placeHolder: Int, error: Int) {
    tryCatch(tryBlock = {
        Glide.with(this.context).load(src).placeholder(placeHolder).error(error).into(this)
    }, catchBlock = { e ->
        AppLogger.e("ImageViewEx", "loadSrc_error: ${e.message}")
    })
}

fun ImageView.loadGif(src: Any) {
    tryCatch(tryBlock = {
        Glide.with(this.context).asGif().load(src).into(this)
    }, catchBlock = { e ->
        AppLogger.e("ImageViewEx", "loadGif_error: ${e.message}")
    })
}

/*
 update: 04/11/2024
 by: doan-vu.dev
 */
fun ImageView.loadSrcNoCacheRam(src: Any) {
    tryCatch(tryBlock = {
        Glide.with(this.context).load(src).skipMemoryCache(true).into(this)
    }, catchBlock = { e ->
        AppLogger.e("ImageViewEx", "loadSrcNoCacheRam_error: ${e.message}")
    })
}

fun ImageView.loadSrcNoCacheDisk(src: Any) {
    tryCatch(tryBlock = {
        Glide.with(this.context).load(src).diskCacheStrategy(DiskCacheStrategy.NONE).into(this)
    }, catchBlock = { e ->
        AppLogger.e("ImageViewEx", "loadSrcNoCacheDisk_error: ${e.message}")
    })
}

fun ImageView.loadSrcNoCache(src: Any) {
    tryCatch(tryBlock = {
        Glide.with(this.context).load(src).skipMemoryCache(true)
            .diskCacheStrategy(DiskCacheStrategy.NONE).into(this)
    }, catchBlock = { e ->
        AppLogger.e("ImageViewEx", "loadSrcNoCache_error: ${e.message}")
    })
}

fun ImageView.loadSrcCacheData(src: Any) {
    tryCatch(tryBlock = {
        Glide.with(this.context).load(src).skipMemoryCache(true)
            .diskCacheStrategy(DiskCacheStrategy.DATA).into(this)
    }, catchBlock = { e ->
        AppLogger.e("ImageViewEx", "loadSrcCacheData_error: ${e.message}")
    })
}

fun ImageView.loadSrcCacheResource(src: Any) {
    tryCatch(tryBlock = {
        Glide.with(this.context).load(src).skipMemoryCache(true)
            .diskCacheStrategy(DiskCacheStrategy.RESOURCE).into(this)
    }, catchBlock = { e ->
        AppLogger.e("ImageViewEx", "loadSrcCacheResource_error: ${e.message}")
    })
}

fun ImageView.loadSrcCacheAllToDisk(src: Any) {
    tryCatch(tryBlock = {
        Glide.with(this.context).load(src).skipMemoryCache(true)
            .diskCacheStrategy(DiskCacheStrategy.ALL).into(this)
    }, catchBlock = { e ->
        AppLogger.e("ImageViewEx", "loadSrcCacheAllToDisk_error: ${e.message}")
    })
}

fun ImageView.loadSrcRoundedCacheAll(url: Any, radius: Int) {
    tryCatch(tryBlock = {
        Glide.with(context).load(url)
            .apply(RequestOptions.bitmapTransform(RoundedCorners(radius.dpToPx())))
            .skipMemoryCache(true).diskCacheStrategy(DiskCacheStrategy.ALL).into(this)
    }, catchBlock = { e ->
        AppLogger.e("ImageViewEx", "loadSrcRoundedCacheAll_error: ${e.message}")
    })
}

fun ImageView.loadSrcRoundedNoCache(url: Any, radius: Int) {
    tryCatch(tryBlock = {
        context ?: return@tryCatch
        Glide.with(context).load(url)
            .apply(RequestOptions.bitmapTransform(RoundedCorners(radius.dpToPx())))
            .skipMemoryCache(true).diskCacheStrategy(DiskCacheStrategy.NONE).into(this)
    }, catchBlock = { e ->
        AppLogger.e("ImageViewEx", "loadSrcRoundedNoCache_error: ${e.message}")
    })
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