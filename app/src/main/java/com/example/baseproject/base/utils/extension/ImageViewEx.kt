package com.example.baseproject.base.utils.extension

import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.widget.ImageView
import com.bumptech.glide.Glide

fun ImageView.setTint() {
    val matrix = ColorMatrix()
    matrix.setSaturation(0f)

    val filter = ColorMatrixColorFilter(matrix)
    this.colorFilter = filter
}

fun ImageView.clearTint() {
    this.colorFilter = null
}

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