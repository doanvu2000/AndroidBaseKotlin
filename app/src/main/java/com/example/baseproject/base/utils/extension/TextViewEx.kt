package com.example.baseproject.base.utils.extension

import android.graphics.Paint
import android.text.Html
import android.widget.EditText
import android.widget.TextView

fun TextView.clear() {
    this.text = ""
}

fun TextView.setTextHtml(content: String) {
    this.text = if (isSdkN()) {
        Html.fromHtml(content, Html.FROM_HTML_MODE_COMPACT)
    } else {
        @Suppress("DEPRECATION") Html.fromHtml(content)
    }
}

fun TextView.setStrikeThrough(show: Boolean = true) {
    paintFlags = if (show) paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
    else paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
}

fun TextView.setUnderLine() {
    paintFlags = paintFlags or Paint.UNDERLINE_TEXT_FLAG
}

fun TextView.setDrawableEnd(drawableId: Int) {
    val drawable = if (drawableId == 0) {
        null
    } else {
        context.getDrawableById(drawableId)
    }
    this.setCompoundDrawablesWithIntrinsicBounds(
        null, null, drawable, null
    )
}

fun EditText.setDrawableEnd(drawableId: Int) {
    val drawable = if (drawableId == 0) {
        null
    } else {
        context.getDrawableById(drawableId)
    }
    this.setCompoundDrawablesWithIntrinsicBounds(
        null, null, drawable, null
    )
}

fun EditText.clear() {
    this.setText("")
}

val EditText.value
    get() = text?.toString() ?: ""