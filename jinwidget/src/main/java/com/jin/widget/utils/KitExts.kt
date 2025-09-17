package com.jin.widget.utils

import android.content.Context
import android.content.res.Resources
import android.util.DisplayMetrics
import android.util.TypedValue
import kotlin.math.roundToInt

/**
 * dp to px
 */
val Float.dpToPxF: Float
    get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, this, Resources.getSystem().displayMetrics
    )

val Int.dpToPxF: Float
    get() = this.toFloat().dpToPxF

val Float.dpToPx: Int
    get() = this.dpToPxF.toInt()

val Int.dpToPx: Int
    get() = this.dpToPxF.toInt()

fun Context.dpToPx(dp: Int): Int {
    val displayMetrics: DisplayMetrics = resources.displayMetrics
    return (Resources.getSystem().displayMetrics.density * dp).roundToInt().toInt()
}

/**
 * sp to px
 */
val Float.spToPxF: Float
    get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_SP, this, Resources.getSystem().displayMetrics
    )

val Int.spToPxF: Float
    get() = this.toFloat().spToPxF

val Float.spToPx: Int
    get() = this.spToPxF.toInt()

val Int.spToPx: Int
    get() = this.spToPxF.toInt()

/**
 * px to dp
 */
val Float.pxToDpF: Float
    get() = this.let {
        val scale = Resources.getSystem().displayMetrics.density
        (it / scale + 0.5f)
    }

val Int.pxToDpF: Float
    get() = this.toFloat().pxToDpF

val Float.pxToDp: Int
    get() = this.pxToDpF.toInt()

val Int.pxToDp: Int
    get() = this.pxToDpF.toInt()


/**
 * px to sp
 */
@Suppress("DEPRECATION")
val Float.pxToSpF: Float
    get() = this.let {
        val scale = Resources.getSystem().displayMetrics.scaledDensity
        (it / scale + 0.5f)
    }

val Int.pxToSpF: Float
    get() = this.toFloat().pxToSpF

val Float.pxToSp: Int
    get() = this.pxToSpF.toInt()

val Int.pxToSp: Int
    get() = this.pxToSpF.toInt()
