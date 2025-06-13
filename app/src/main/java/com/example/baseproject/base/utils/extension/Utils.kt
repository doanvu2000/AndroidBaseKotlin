package com.example.baseproject.base.utils.extension

import android.app.PendingIntent
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.Rect
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Parcelable
import android.provider.OpenableColumns
import android.util.TypedValue
import android.view.PixelCopy
import android.view.View
import android.view.Window
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.FileProvider
import androidx.core.graphics.createBitmap
import com.example.baseproject.BuildConfig
import com.example.baseproject.base.entity.deviceInfo
import com.example.baseproject.base.utils.util.AppLogger
import com.example.baseproject.base.utils.util.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.io.Serializable
import java.text.DecimalFormat
import java.util.Calendar
import kotlin.math.ceil
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.roundToInt

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

/**
 * retrofit enqueue
 */
fun <T> Call<T>.enqueueShort(
    success: ((Response<T>) -> Unit)? = null,
    failed: ((Throwable) -> Unit)? = null
) {
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

fun Int.getFlagPendingIntent(): Int {
    return PendingIntent.FLAG_IMMUTABLE or this
}

fun setDarkMode(enable: Boolean = false) {
    val mode = if (enable) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
    AppCompatDelegate.setDefaultNightMode(mode)
}

fun saveStringToFile(context: Context, content: String, fileName: String): File? {
    if (context.hasWritePermission()) {
        // Handle the case when the permission is not granted
        return null
    }

    val fileDir = File(context.getExternalFilesDir(null), "YourDirectory")
    fileDir.mkdirs()
    val file = File(fileDir, fileName)
    try {
        val fileOutputStream = FileOutputStream(file)
        fileOutputStream.write(content.toByteArray())
        fileOutputStream.close()
        return file
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return null
}

fun shareFile(context: Context, file: File) {
    val fileUri =
        FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", file)

    val shareIntent = Intent(Intent.ACTION_SEND)
    //set type for file
    shareIntent.type = "application/json"
    shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri)
    context.startActivity(Intent.createChooser(shareIntent, "Share JSON File"))
}

fun <T> getValueByCondition(condition: Boolean, trueValue: T, falseValue: T): T {
    return if (condition) {
        trueValue
    } else {
        falseValue
    }
}

suspend fun runOnDispatcherIO(action: () -> Unit) {
    withContext(Dispatchers.IO) {
        action.invoke()
    }
}

suspend fun runOnDispatcherDefault(action: () -> Unit) {
    withContext(Dispatchers.Default) {
        action.invoke()
    }
}

suspend fun runOnDispatcherMain(action: () -> Unit) {
    withContext(Dispatchers.Main) {
        action.invoke()
    }
}

private fun getByteLengthInDouble(): Double {
    return if (isSdkO()) 1000.0
    else 1024.0
}

fun getReadableSize(size: Long): String {
    if (size <= 0) return "0"
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    val digitGroups = (log10(size.toDouble()) / log10(getByteLengthInDouble())).toInt()
    return DecimalFormat("#,##0.#").format(size / getByteLengthInDouble().pow(digitGroups.toDouble()))
        .toString() + units[digitGroups]
}

fun Uri.getFileSize(context: Context?): Long {
    if (context == null) return -1L
    val contentResolver = context.contentResolver
    val length = contentResolver.openFileDescriptor(this, "r")?.use { it.statSize } ?: -1L

    if (length != -1L) {
        return length
    } else {
        // https://stackoverflow.com/questions/48302972/content-resolver-returns-wrong-size
        if (scheme.equals(ContentResolver.SCHEME_CONTENT)) {
            return contentResolver.query(this, arrayOf(OpenableColumns.SIZE), null, null, null)
                ?.use { cursor ->
                    val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                    if (sizeIndex == -1) {
                        return@use -1L
                    }
                    cursor.moveToFirst()
                    return try {
                        cursor.getLong(sizeIndex)
                    } catch (throwable: Throwable) {
                        -1L
                    }
                } ?: -1L
        } else {
            return -1L
        }
    }
}

fun Any?.ifNull(block: () -> Unit) = run {
    if (this == null) {
        block()
    }
}

val Int.dpf: Float
    get() {
        return dp.toFloat()
    }


val Float.dpf: Float
    get() {
        return dp.toFloat()
    }

val Float.dp: Int
    get() {
        return if (this == 0f) {
            0
        } else ceil((Resources.getSystem().displayMetrics.density * this).toDouble()).toInt()
    }

fun Cursor.getSafeColumn(column: String): Int? = try {
    getColumnIndexOrThrow(column)
} catch (e: Exception) {
    null
}

/**
 * update by doan-vu.dev 04/11/2024
 * */
fun isDebugMode() = BuildConfig.DEBUG
fun isEmulator(): Boolean = Build.FINGERPRINT.startsWith("generic") ||
        Build.FINGERPRINT.startsWith("unknown") ||
        Build.MODEL.contains("google_sdk") ||
        Build.MODEL.contains("sdk_gphone64") ||
        Build.MODEL.contains("Emulator") ||
        Build.MODEL.contains("Android SDK built for x86") ||
        Build.MANUFACTURER.contains("Genymotion") ||
        (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic")) ||
        Build.PRODUCT == "google_sdk" ||
        Build.PRODUCT.contains("sdk_gphone64_x86")

fun deviceInfoPrint() {
    val deviceInfo = deviceInfo {
        model = Build.MODEL
        manufacturer = Build.MANUFACTURER
        brand = Build.BRAND
        deviceString = Build.DEVICE
        product = Build.PRODUCT
        fingerPrint = Build.FINGERPRINT
        display = Build.DISPLAY
        hardware = Build.HARDWARE
        host = Build.HOST
        id = Build.ID
        tag = Build.TAGS
        time = Build.TIME.toTimeFormat()
        type = Build.TYPE
        user = Build.USER
    }

    AppLogger.d(Constants.TAG, deviceInfo.toPrettyDebugString())
}

/**
 * Preventing click multiple view
 * update by doan-vu.dev 04/11/2024
 * */
var readyClickStatic = true
fun delayClickStatic(timeDelay: Long = 200L) {
    readyClickStatic = false
    CoroutineScope(Dispatchers.IO).launch {
        delay(timeDelay)
        readyClickStatic = true
    }
}

fun clickStatic(action: () -> Unit) {
    if (readyClickStatic) {
        delayClickStatic()
        action()
    }
}

fun captureView(view: View, window: Window, bitmapCallback: (Bitmap) -> Unit) {
    // Above Android O, use PixelCopy
    val bitmap = createBitmap(view.width, view.height)
    val location = IntArray(2)
    view.getLocationInWindow(location)
    PixelCopy.request(
        window,
        Rect(location[0], location[1], location[0] + view.width, location[1] + view.height),
        bitmap,
        {
            if (it == PixelCopy.SUCCESS) {
                bitmapCallback.invoke(bitmap)
            }
        },
        Handler(Looper.getMainLooper())
    )
}

fun tryCatch(action: () -> Unit) {
    try {
        action()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun tryCatch(tryBlock: () -> Unit, catchBlock: ((e: Exception) -> Unit)? = null) {
    try {
        tryBlock()
    } catch (e: Exception) {
        catchBlock?.invoke(e)
    }
}

fun Int.dpToPx(): Int {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, this.toFloat(), Resources.getSystem().displayMetrics
    ).toInt()
}

fun Float.dpToPx(): Int = TypedValue.applyDimension(
    TypedValue.COMPLEX_UNIT_DIP,
    this,
    Resources.getSystem().displayMetrics
).toInt()

fun now() = System.currentTimeMillis()