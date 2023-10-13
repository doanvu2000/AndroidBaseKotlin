package com.example.baseproject.base.utils.extension

import android.content.Context
import java.text.DecimalFormat

/**VN: 1.000.000
 * US: 1,000,000
 * */
const val DECIMAL_PATTERN1 = "#,###"
fun Int.toStringFormat(pattern: String? = DECIMAL_PATTERN1): String {
    val decimalFormat = DecimalFormat(pattern)
    return decimalFormat.format(this)
}

fun Int.toDp(context: Context): Int {
    return context.resources.displayMetrics.density.toInt() * this
}