package com.example.baseproject.base.utils.extension

import android.graphics.Paint
import android.text.Html
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.FontRes
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.toColorInt

//region Text Content Management

/**
 * Clear text content
 */
fun TextView.clear() {
    this.text = ""
}

/**
 * Clear EditText content
 */
fun EditText.clear() {
    this.setText("")
}

/**
 * Set HTML content cho TextView
 */
fun TextView.setTextHtml(content: String) {
    this.text = Html.fromHtml(content, Html.FROM_HTML_MODE_COMPACT)
}

/**
 * Get value từ EditText
 */
val EditText.value: String
    get() = text?.toString() ?: ""

//endregion

//region Text Styling

/**
 * Set strikethrough cho text
 */
fun TextView.setStrikeThrough(show: Boolean = true) {
    paintFlags = if (show) {
        paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
    } else {
        paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
    }
}

/**
 * Set underline cho text
 */
fun TextView.setUnderLine() {
    paintFlags = paintFlags or Paint.UNDERLINE_TEXT_FLAG
}

/**
 * Set font cho TextView
 */
fun TextView.setFont(@FontRes fontId: Int) {
    val font = ResourcesCompat.getFont(context, fontId)
    this.typeface = font
}

//endregion

//region Drawable Management

/**
 * Set drawable end cho TextView
 */
fun TextView.setDrawableEnd(drawableId: Int) {
    val drawable = if (drawableId == 0) {
        null
    } else {
        context.getDrawableById(drawableId)
    }
    this.setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null)
}

/**
 * Set drawable end cho EditText
 */
fun EditText.setDrawableEnd(drawableId: Int) {
    val drawable = if (drawableId == 0) {
        null
    } else {
        context.getDrawableById(drawableId)
    }
    this.setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null)
}

//endregion

//region Shadow Effects

/**
 * Enum cho các kiểu shadow
 */
enum class TextShadowEnum {
    None,
    Center,
    TopRight,
    BottomRight
}

/**
 * Set shadow cho TextView
 */
fun TextView.setShadow(textShadowEnum: TextShadowEnum) {
    when (textShadowEnum) {
        TextShadowEnum.None -> {
            setShadowLayer(0f, 0f, 0f, "#00000000".toColorInt())
        }

        TextShadowEnum.Center -> {
            setShadowLayer(3f, 4f, 10f, "#4D000000".toColorInt())
        }

        TextShadowEnum.TopRight -> {
            setShadowLayer(3f, 8f, -8f, "#4D000000".toColorInt())
        }

        TextShadowEnum.BottomRight -> {
            setShadowLayer(3f, 8f, 8f, "#4D000000".toColorInt())
        }
    }
}

/**
 * Show blur layer cho TextView
 */
fun TextView.showLayerBlur(blurColor: Int) {
    setShadowLayer(24f, 0f, 0f, blurColor)
}

/**
 * Clear blur layer
 */
fun TextView.clearLayerBlur() {
    setShadowLayer(0f, 0f, 0f, "#00000000".toColorInt())
}

//endregion
