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

//region Calendar & Time Extensions

/**
 * Convert Long timestamp to Calendar
 */
fun Long.toCalendar(): Calendar {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = this
    return calendar
}

/**
 * Get year from timestamp
 */
fun Long.getYear(): Int = toCalendar().get(Calendar.YEAR)

/**
 * Get month from timestamp (1-12)
 */
fun Long.getMonth(): Int = toCalendar().get(Calendar.MONTH) + 1

/**
 * Get day from timestamp
 */
fun Long.getDay(): Int = toCalendar().get(Calendar.DAY_OF_MONTH)

/**
 * Get hour from timestamp
 */
fun Long.getHour(): Int = toCalendar().get(Calendar.HOUR_OF_DAY)

/**
 * Get minutes from timestamp
 */
fun Long.getMinutes(): Int = toCalendar().get(Calendar.MINUTE)

/**
 * Get seconds from timestamp
 */
fun Long.getSecond(): Int = toCalendar().get(Calendar.SECOND)

/**
 * Get current timestamp
 */
fun now(): Long = System.currentTimeMillis()

//endregion

//region Number & Math Extensions

/**
 * Round Double to specific decimal places
 */
fun Double.roundTo(numFractionDigits: Int): Double {
    val factor = 10.0.pow(numFractionDigits.toDouble())
    return (this * factor).roundToInt() / factor
}

/**
 * Invert Boolean value
 */
fun Boolean.invert(): Boolean = !this

/**
 * Get readable file size string
 */
fun getReadableSize(size: Long): String {
    if (size <= 0) return "0"
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    val byteLengthInDouble = if (isSdkO()) 1000.0 else 1024.0
    val digitGroups = (log10(size.toDouble()) / log10(byteLengthInDouble)).toInt()
    return DecimalFormat("#,##0.#").format(size / byteLengthInDouble.pow(digitGroups.toDouble()))
        .toString() + units[digitGroups]
}

//endregion

//region Unit Conversion

/**
 * Convert dp to px for Int
 */
fun Int.dpToPx(): Int {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, this.toFloat(), Resources.getSystem().displayMetrics
    ).toInt()
}

/**
 * Convert dp to px for Float
 */
fun Float.dpToPx(): Int = TypedValue.applyDimension(
    TypedValue.COMPLEX_UNIT_DIP, this, Resources.getSystem().displayMetrics
).toInt()

/**
 * Convert dp to Float for Int
 */
val Int.dpf: Float
    get() = dp.toFloat()

/**
 * Convert Float dp to px
 */
val Float.dp: Int
    get() = if (this == 0f) 0 else ceil((Resources.getSystem().displayMetrics.density * this).toDouble()).toInt()

/**
 * Convert Float dp to Float
 */
val Float.dpf: Float
    get() = dp.toFloat()

//endregion

//region Retrofit Extensions

/**
 * Simplified retrofit enqueue
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

//endregion

//region Bundle Utilities

/**
 * Create Bundle for Serializable object
 */
fun <K : Serializable> pushParcelableBundle(k: K): Bundle {
    val bundle = Bundle()
    bundle.putSerializable(k.javaClass.name, k)
    return bundle
}

/**
 * Create Bundle for Parcelable object
 */
fun <K : Parcelable> pushParcelableBundle(k: K): Bundle {
    val bundle = Bundle()
    bundle.putParcelable(k.javaClass.name, k)
    return bundle
}

//endregion

//region System & App Utilities

/**
 * Get PendingIntent flag with immutable
 */
fun Int.getFlagPendingIntent(): Int = PendingIntent.FLAG_IMMUTABLE or this

/**
 * Set dark mode for app
 */
fun setDarkMode(enable: Boolean = false) {
    val mode = if (enable) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
    AppCompatDelegate.setDefaultNightMode(mode)
}

/**
 * Check if app is in debug mode
 */
fun isDebugMode(): Boolean = BuildConfig.DEBUG

/**
 * Check if running on emulator
 */
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

//endregion

//region File Management

/**
 * Save string content to file
 */
fun saveStringToFile(context: Context, content: String, fileName: String): File? {
    if (!context.hasWritePermission()) {
        return null
    }

    val fileDir = File(context.getExternalFilesDir(null), "YourDirectory")
    fileDir.mkdirs()
    val file = File(fileDir, fileName)
    return try {
        val fileOutputStream = FileOutputStream(file)
        fileOutputStream.write(content.toByteArray())
        fileOutputStream.close()
        file
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

/**
 * Share file with other apps
 */
fun shareFile(context: Context, file: File) {
    val fileUri =
        FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", file)
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "application/json"
        putExtra(Intent.EXTRA_STREAM, fileUri)
    }
    context.startActivity(Intent.createChooser(shareIntent, "Share JSON File"))
}

/**
 * Get file size from Uri
 */
fun Uri.getFileSize(context: Context?): Long {
    if (context == null) return -1L
    val contentResolver = context.contentResolver
    val length = contentResolver.openFileDescriptor(this, "r")?.use { it.statSize } ?: -1L

    if (length != -1L) {
        return length
    } else {
        if (scheme.equals(ContentResolver.SCHEME_CONTENT)) {
            return contentResolver.query(this, arrayOf(OpenableColumns.SIZE), null, null, null)
                ?.use { cursor ->
                    val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                    if (sizeIndex == -1) return@use -1L
                    cursor.moveToFirst()
                    try {
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

//endregion

//region Utility Functions

/**
 * Get value by condition
 */
fun <T> getValueByCondition(condition: Boolean, trueValue: T, falseValue: T): T {
    return if (condition) trueValue else falseValue
}

/**
 * Execute block if object is null
 */
fun Any?.ifNull(block: () -> Unit) {
    if (this == null) block()
}

/**
 * Safe column access for Cursor
 */
fun Cursor.getSafeColumn(column: String): Int? = try {
    getColumnIndexOrThrow(column)
} catch (e: Exception) {
    null
}

//endregion

//region Coroutine Utilities

/**
 * Run action on IO dispatcher
 */
suspend fun runOnDispatcherIO(action: () -> Unit) {
    withContext(Dispatchers.IO) {
        action.invoke()
    }
}

/**
 * Run action on Default dispatcher
 */
suspend fun runOnDispatcherDefault(action: () -> Unit) {
    withContext(Dispatchers.Default) {
        action.invoke()
    }
}

/**
 * Run action on Main dispatcher
 */
suspend fun runOnDispatcherMain(action: () -> Unit) {
    withContext(Dispatchers.Main) {
        action.invoke()
    }
}

//endregion

//region Debug & Device Info

/**
 * Print device information for debugging
 */
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

//endregion

//region Click Prevention

/**
 * Static click prevention variable
 */
var readyClickStatic = true

/**
 * Delay click to prevent multiple clicks
 */
fun delayClickStatic(timeDelay: Long = 200L) {
    readyClickStatic = false
    CoroutineScope(Dispatchers.IO).launch {
        delay(timeDelay)
        readyClickStatic = true
    }
}

/**
 * Execute action with click prevention
 */
fun clickStatic(action: () -> Unit) {
    if (readyClickStatic) {
        delayClickStatic()
        action()
    }
}

//endregion

//region View Capture & Error Handling

/**
 * Capture view as bitmap
 */
fun captureView(view: View, window: Window, bitmapCallback: (Bitmap) -> Unit) {
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

/**
 * Try-catch wrapper
 */
fun tryCatch(action: () -> Unit) {
    try {
        action()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

/**
 * Try-catch wrapper with error handling
 */
fun tryCatch(tryBlock: () -> Unit, catchBlock: ((e: Exception) -> Unit)? = null) {
    try {
        tryBlock()
    } catch (e: Exception) {
        catchBlock?.invoke(e)
    }
}

//endregion
