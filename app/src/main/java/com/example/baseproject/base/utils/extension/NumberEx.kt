package com.example.baseproject.base.utils.extension

import android.content.Context
import android.content.res.Resources
import java.text.DecimalFormat

//region Number Formatting

/**
 * Decimal pattern cho formatting
 * VN: 1.000.000
 * US: 1,000,000
 */
const val DECIMAL_PATTERN1 = "#,###"

/**
 * Format số Int với pattern tùy chỉnh
 */
fun Int.toStringFormat(pattern: String? = DECIMAL_PATTERN1): String {
    val decimalFormat = DecimalFormat(pattern)
    return decimalFormat.format(this)
}

/**
 * Format số Int thành string với leading zero
 * @sample 5 -> "05", 10 -> "10"
 */
fun Int.toStringFormat(): String {
    return if (this < 10) {
        "0$this"
    } else {
        this.toString()
    }
}

//endregion

//region Unit Conversion

/**
 * Convert Int sang dp theo context
 */
fun Int.toDp(context: Context): Int {
    return context.resources.displayMetrics.density.toInt() * this
}

/**
 * Convert px sang dp
 */
val Int.dp: Int
    get() = (this / Resources.getSystem().displayMetrics.density).toInt()

/**
 * Convert dp sang px
 */
val Int.px: Int
    get() = (this * Resources.getSystem().displayMetrics.density).toInt()

//endregion

//region Number Properties

/**
 * Kiểm tra số chẵn
 */
fun Int.isEven(): Boolean = this % 2 == 0

/**
 * Kiểm tra số lẻ
 */
fun Int.isOdd(): Boolean = !isEven()

//endregion

//region Time Formatting

/**
 * Convert milliseconds sang time format MM:SS
 * @sample 1000 -> "00:01", 61000 -> "01:01"
 */
fun Long.toTimeFormat(): String {
    val minutes = this / 1000 / 60
    val seconds = (this - minutes * 60 * 1000) / 1000

    val minutesStr = if (minutes < 10) "0$minutes" else "$minutes"
    val secondsStr = if (seconds < 10) "0$seconds" else "$seconds"

    return "$minutesStr:$secondsStr"
}

//endregion
