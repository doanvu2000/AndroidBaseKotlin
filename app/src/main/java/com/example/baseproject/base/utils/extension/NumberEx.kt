package com.example.baseproject.base.utils.extension

import android.content.Context
import android.content.res.Resources
import java.text.DecimalFormat

/**VN: 1.000.000
 * US: 1,000,000
 * */
const val DECIMAL_PATTERN1 = "#,###"
fun Int.toStringFormat(pattern: String? = DECIMAL_PATTERN1): String {
    val decimalFormat = DecimalFormat(pattern)
    return decimalFormat.format(this)
}

fun Int.toStringFormat(): String {
    if (this < 10) {
        return "0$this"
    }
    return this.toString()
}

fun Int.toDp(context: Context): Int {
    return context.resources.displayMetrics.density.toInt() * this
}

// Convert px to dp
val Int.dp: Int
    get() = (this / Resources.getSystem().displayMetrics.density).toInt()

//Convert dp to px
val Int.px: Int
    get() = (this * Resources.getSystem().displayMetrics.density).toInt()

fun Int.isEven(): Boolean {
    return this % 2 == 0
}

fun Int.isOdd() = !isEven()

fun Long.toTimeFormat(): String {//1000 -> 01
    val minutes = this / 1000 / 60
    val second = (this - minutes * 60 * 1000) / 1000
    var rs: String = if (minutes < 10) {
        "0$minutes"
    } else {
        "$minutes"
    }
    rs += ":"
    rs += if (second < 10) {
        "0$second"
    } else {
        "$second"
    }
    return rs
}