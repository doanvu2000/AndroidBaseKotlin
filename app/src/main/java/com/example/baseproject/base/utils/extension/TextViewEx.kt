package com.example.baseproject.base.utils.extension

import android.graphics.Color
import android.graphics.Paint
import android.text.Html
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.FontRes
import androidx.core.content.res.ResourcesCompat

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

fun TextView.setFont(@FontRes fontId: Int) {
    val font = ResourcesCompat.getFont(context, fontId)
    this.typeface = font
}

enum class TextShadowEnum {
    None,
    Center,
    TopRight,
    BottomRight
}

fun TextView.setShadow(textShadowEnum: TextShadowEnum) {
    when (textShadowEnum) {
        TextShadowEnum.None -> {
            setShadowLayer(0f, 0f, 0f, Color.parseColor("#00000000"))
        }

        TextShadowEnum.Center -> {
            setShadowLayer(3f, 4f, 10f, Color.parseColor("#4D000000"))
        }

        TextShadowEnum.TopRight -> {
            setShadowLayer(3f, 8f, -8f, Color.parseColor("#4D000000"))
        }

        TextShadowEnum.BottomRight -> {
            setShadowLayer(3f, 8f, 8f, Color.parseColor("#4D000000"))
        }
    }
}