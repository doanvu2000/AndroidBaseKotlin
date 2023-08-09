package com.example.baseproject.base.utils

import android.app.PendingIntent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import androidx.appcompat.app.AppCompatDelegate
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.Serializable
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.*
import kotlin.math.pow
import kotlin.math.roundToInt

val isMinSdk23 get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
val isMinSdk26 get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
val isMinSdk29 get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
val isMinSdk30 get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R


fun Long.toCalendar(): Calendar {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = this
    return calendar
}

fun Long.getYear() = toCalendar().get(Calendar.YEAR)
fun Long.getMonth() = toCalendar().get(Calendar.MONTH) + 1
fun Long.getDay() = toCalendar().get(Calendar.DAY_OF_MONTH)
fun Long.getHour() = toCalendar().get(Calendar.HOUR_OF_DAY)
fun Long.getMinutes() = toCalendar().get(Calendar.MINUTE)
fun Long.getSecond() = toCalendar().get(Calendar.SECOND)

fun Double.roundTo(numFractionDigits: Int): Double {
    val factor = 10.0.pow(numFractionDigits.toDouble())
    return (this * factor).roundToInt() / factor
}

fun Boolean.invert(): Boolean {
    return !this
}

fun String.upperFirstCase(): String {
    val firstCase = this.first().toString().uppercase()
    return firstCase + this.substring(1)
}

fun String?.containsIgnoreCase(regex: String): Boolean {
    return this?.contains(regex, true) == true
}

fun String.encryptStringToLong(): Long {
    val digest = MessageDigest.getInstance("SHA-256")
    val hashBytes = digest.digest(this.toByteArray(StandardCharsets.UTF_8))

    // Convert the hash bytes to a long value
    var result: Long = 0
    for (i in 0 until 8) {
        result = (result shl 8) or (hashBytes[i].toLong() and 0xff)
    }

    return result
}

/**
 * append bitmap2 to bottom bitmap_root
 * */
fun Bitmap.appendVertical(bitmap2: Bitmap): Bitmap {
    return mergeBitmapsVertical(this, bitmap2)
}

/**
 * overlay bitmap2 into bitmap_root
 * */
fun Bitmap.overlay(bitmap2: Bitmap): Bitmap {
    return overlayBitMap(this, bitmap2)
}

/**
 * flip bitmap_root to horizontal xAxis or vertical yAxis
 * */
const val FLIP_ORIENTATION = -1.0f
const val DEFAULT_ORIENTATION = 1.0f
fun Bitmap.flip(flipXAxis: Boolean = false): Bitmap {
    return if (flipXAxis) flipHorizontalX() else flipVerticalY()
}

fun Bitmap.flipHorizontalX(): Bitmap {
    return createWithMatrix(Matrix().apply {
        postScale(FLIP_ORIENTATION, DEFAULT_ORIENTATION)
    })
}

fun Bitmap.flipVerticalY(): Bitmap {
    return createWithMatrix(Matrix().apply {
        postScale(DEFAULT_ORIENTATION, FLIP_ORIENTATION)
    })
}

fun Bitmap.rotate(angle: Float): Bitmap {
    return createWithMatrix(Matrix().apply {
        postRotate(angle)
    })
}

fun Bitmap.createWithMatrix(matrix: Matrix): Bitmap = Bitmap.createBitmap(
    this, 0, 0, this.width, this.height,
    matrix, true
)

fun mergeBitmapsVertical(bitmap1: Bitmap, bitmap2: Bitmap): Bitmap {
    val width = bitmap1.width.coerceAtLeast(bitmap2.width)
    val height = bitmap1.height + bitmap2.height

    val mergedBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(mergedBitmap)

    val zeroFloat = 0f
    canvas.drawBitmap(bitmap1, zeroFloat, zeroFloat, null)
    canvas.drawBitmap(bitmap2, zeroFloat, bitmap1.height.toFloat(), null)

    return mergedBitmap
}

fun overlayBitMap(bmp1: Bitmap, bmp2: Bitmap): Bitmap {
    val bmOverlay = Bitmap.createBitmap(bmp1.width, bmp1.height, bmp1.config)
    val bitmap2 = Bitmap.createScaledBitmap(bmp2, bmOverlay.width, bmOverlay.height, false)
    val canvas = Canvas(bmOverlay)
    canvas.drawBitmap(bmp1, Matrix(), null)
    canvas.drawBitmap(bitmap2, 0f, 0f, null)
    bmp1.recycle()
    bmp2.recycle()
    bitmap2.recycle()
    return bmOverlay
}

/**
 * retrofit enqueue
 */
fun <T> Call<T>.enqueueShort(success: ((Response<T>) -> Unit)? = null, failed: ((Throwable) -> Unit)? = null) {
    this.enqueue(object : Callback<T> {
        override fun onResponse(call: Call<T>, response: Response<T>) {
            success?.invoke(response)
        }

        override fun onFailure(call: Call<T>, t: Throwable) {
            failed?.invoke(t)
        }
    })
}

fun <K : Serializable> pushParcelableBundle(k: K): Bundle {
    val bundle = Bundle()
    bundle.putSerializable(k.javaClass.name, k)
    return bundle
}

fun <K : Parcelable> pushParcelableBundle(k: K): Bundle {
    val bundle = Bundle()
    bundle.putParcelable(k.javaClass.name, k)
    return bundle
}

fun <T> pushBundle(key: String, data: T): Bundle {
    val bundle = Bundle()
    when (data) {
        is Long -> bundle.putLong(key, data)
        is Int -> bundle.putInt(key, data)
        is Float -> bundle.putFloat(key, data)
        is Boolean -> bundle.putBoolean(key, data)
        is String -> bundle.putString(key, data.toString())
        is Parcelable -> bundle.putParcelable(key, data)
        is Serializable -> bundle.putSerializable(key, data)
        else -> bundle.putString(key, data.toString())
    }
    return bundle
}

fun Int.getFlagPendingIntent(): Int {
    return if (isMinSdk23) {
        PendingIntent.FLAG_IMMUTABLE or this
    } else {
        this
    }
}

//"JIN VU di hOC" => "Jin Vu Di Hoc"
fun String.capitalizeWord(): String {
    return this.lowercase().split(" ")
        .joinToString(separator = " ", transform = String::capitalize)
}

fun setDarkMode(enable: Boolean = false) {
    val mode = if (enable) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
    AppCompatDelegate.setDefaultNightMode(mode)
}