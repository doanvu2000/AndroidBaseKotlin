package com.example.baseproject.base.utils.extension

import android.app.Activity
import android.app.ActivityManager
import android.app.Service
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.baseproject.R
import com.example.baseproject.base.utils.ImageUtil
import java.io.IOException
import java.io.InputStream
import java.util.*


fun Context.checkPermission(permission: String): Boolean {
    return ContextCompat.checkSelfPermission(this, permission) == PERMISSION_GRANTED
}

fun Context.hasWritePermission() = this.checkPermission(WRITE_EXTERNAL_STORAGE)

fun Context.hasReadStoragePermission() = if (isSdk33()) {
    checkPermission(READ_MEDIA_IMAGE)
} else {
    checkPermission(READ_EXTERNAL_STORAGE)
}

fun Context.hasLocationPermission(): Boolean {
    return checkPermission(ACCESS_COARSE_LOCATION) && checkPermission(ACCESS_FINE_LOCATION)
}

fun Context.hasNotificationPermission(): Boolean {
    return if (!isSdk33()) {
        true
    } else {
        checkPermission(POST_NOTIFICATION)
    }
}

/** Demo: if api 33, permission READ_MEDIA_IMAGE*/
fun Context.requestPermissionReadStorage(permissionLauncher: ActivityResultLauncher<Array<String>>) {
    val isReadPermissionGranted = hasReadStoragePermission()

    val permissionRequest = mutableListOf<String>()
    val permission = if (isSdk33()) {
        READ_MEDIA_IMAGE
    } else {
        READ_EXTERNAL_STORAGE
    }
    if (!isReadPermissionGranted) {
        permissionRequest.add(permission)
    }
    if (permissionRequest.isNotEmpty()) {
        permissionLauncher.launch(permissionRequest.toTypedArray())
    }
}

fun Context.openActivity(pClass: Class<out Activity>, bundle: Bundle?) {
    val intent = Intent(this, pClass)
    if (bundle != null) {
        intent.putExtras(bundle)
    }
    startActivity(intent)
}

fun Context.openActivity(pClass: Class<out Activity>, isFinish: Boolean = false) {
    openActivity(pClass, null)
    if (isFinish) {
        (this as Activity).finish()
    }
}

fun Context.openActivity(pClass: Class<out Activity>, isFinish: Boolean = false, bundle: Bundle?) {
    openActivity(pClass, bundle)
    if (isFinish) {
        (this as Activity).finish()
    }
}

fun Context.loadImage(
    imageView: ImageView, url: String,
    error: Int = R.drawable.ic_launcher_background
) {
    Glide.with(this).load(url)
        .fitCenter()
        .placeholder(error)
        .into(imageView)

}

fun Context.loadImage(
    imageView: ImageView, url: Int,
    error: Int = R.drawable.ic_launcher_background
) {
    Glide.with(this).load(url)
        .fitCenter()
        .placeholder(error)
        .into(imageView)

}

fun Context.getDrawableIdByName(name: String): Int {
    return resources.getIdentifier(name.split(".").last(), "drawable", packageName)
}

fun Context.inflateLayout(layoutResource: Int, parent: ViewGroup): View {
    return LayoutInflater.from(this).inflate(layoutResource, parent, false)
}

fun Context.shareApp() {
    val subject = "Let go to record your emoji today!!"
    val sharingIntent = Intent(Intent.ACTION_SEND)
    sharingIntent.type = "text/plain"
    val shareBody = "https://play.google.com/store/apps/details?id=$packageName"
    sharingIntent.putExtra(Intent.EXTRA_SUBJECT, subject)
    sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody)
    this.startActivity(Intent.createChooser(sharingIntent, "Share to"))
}

fun Context.navigateToMarket(publishNameStore: String) {
    val market = "market://details?id="
    val webPlayStore = "https://play.google.com/store/apps/details?id="
    try {
        startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse(market + publishNameStore)
                //market://details?id=<package_name>
            )
        )
    } catch (e: ActivityNotFoundException) {
        startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse(webPlayStore + publishNameStore)
                //https://play.google.com/store/apps/details?id=<package_name>
            )
        )
    }
}

fun Context.sendEmail(toEmail: String, feedBackString: String) {
    val intent = Intent(Intent.ACTION_VIEW)
    val data = Uri.parse(
        "mailto:"
                + toEmail
                + "?subject=" + feedBackString + "&body=" + ""
    )
    intent.data = data
    try {
        startActivity(intent)
    } catch (ex: Exception) {
        Toast.makeText(
            this,
            "Not have email app to send email!",
            Toast.LENGTH_SHORT
        ).show()
        ex.printStackTrace()
    }
}

fun Context.getRingTone(): Ringtone {
    val defaultRingtoneUri: Uri =
        RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
    return RingtoneManager.getRingtone(this, defaultRingtoneUri)
}

/**
 * require declare permission ACCESS_NETWORK_STATE in Manifest
 * */
fun Context.isInternetAvailable(): Boolean {
    var result = false
    val connectivityManager =
        getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    if (isSdkM()) {
        val networkCapabilities = connectivityManager.activeNetwork ?: return false
        val actNw =
            connectivityManager.getNetworkCapabilities(networkCapabilities) ?: return false
        result = when {
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            else -> false
        }
    } else {
        @Suppress("DEPRECATION")
        connectivityManager.run {
            connectivityManager.activeNetworkInfo?.run {
                result = when (type) {
                    ConnectivityManager.TYPE_WIFI -> true
                    ConnectivityManager.TYPE_MOBILE -> true
                    ConnectivityManager.TYPE_ETHERNET -> true
                    else -> false
                }

            }
        }
    }

    return result
}

fun Context.showToast(msg: String, isShowDurationLong: Boolean = false) {
    val duration = if (isShowDurationLong) Toast.LENGTH_LONG else Toast.LENGTH_SHORT
    Toast.makeText(this, msg, duration).show()
}

fun Context.connectService(pClass: Class<out Service>) {
    startService(Intent(this, pClass))
}

fun Context.endService(pClass: Class<out Service>) {
    stopService(Intent(this, pClass))
}

//required permission WRITE_EXTERNAL_STORAGE ( if sdk <= 29)
fun Context.shareImage(bitmap: Bitmap) {
    if (!hasWritePermission()) {
        showToast("No have permission to write file")
        return
    }
    ImageUtil.insertSharingImage(contentResolver, bitmap)?.also { uri ->
        val intent = Intent(Intent.ACTION_SEND)
        intent.putExtra(Intent.EXTRA_STREAM, uri)
        intent.type = ImageUtil.TYPE_JPEG
        startActivity(Intent.createChooser(intent, getString(R.string.app_name)))
    }
}

//required permission WRITE_EXTERNAL_STORAGE ( if sdk <= 29)
fun Context.saveImageToLocal(bitmap: Bitmap, result: (Boolean) -> Unit) {
    if (!hasWritePermission()) {
        showToast("No have permission to write file")
        return
    }
    val resultSaveImage = ImageUtil.savePhotoToExternalStorage(contentResolver, UUID.randomUUID().toString(), bitmap)
    result.invoke(resultSaveImage)
}

fun getPromptInfo(title: String, subtitle: String, negativeButtonText: String) = BiometricPrompt.PromptInfo.Builder()
    .setTitle(title)
    .setSubtitle(subtitle)
    .setNegativeButtonText(negativeButtonText)
    .build()

val biometricCall = object : BiometricPrompt.AuthenticationCallback() {

}

/**
 * ex:
 *  - assets/data.json => path = data.json
 *  - assets/db/data.json => path = db/data.json
 * */
fun Context.loadJsonFromAsset(path: String): String? {
    var json: String? = null
    try {
        val ios: InputStream = assets.open(path)
        val size = ios.available()
        val buffer = ByteArray(size)
        ios.read(buffer)
        ios.close()
        json = String(buffer, Charsets.UTF_8)
    } catch (e: IOException) {
        e.printStackTrace()
    }
    return json
}

fun Context.getColorById(colorSource: Int): Int {
    return ContextCompat.getColor(this, colorSource)
}

fun Context.getColorByIdWithTheme(colorAttr: Int): Int {
    val typedValue = TypedValue()
    theme.resolveAttribute(colorAttr, typedValue, true)
    return typedValue.data
}

fun Context.serviceIsRunning(serviceClass: Class<*>): Boolean {
    val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    @Suppress("DEPRECATION")
    for (service in manager.getRunningServices(Int.MAX_VALUE))
        if (serviceClass.name == service.service.className) return true
    return false
}

fun Context.getVersionName(): String {
    return try {
        val packageInfo: PackageInfo = packageManager.getPackageInfo(packageName, 0)
        packageInfo.versionName
    } catch (e: Exception) {
        Build.VERSION.RELEASE
    }
}

fun Context.getDrawableById(drawableId: Int): Drawable? {
    return ContextCompat.getDrawable(this, drawableId)
}

fun Context.shareText(value: String) {
    val sendIntent: Intent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, value)
        type = "text/plain"
    }
    val shareIntent = Intent.createChooser(sendIntent, null)
    startActivity(shareIntent)
}

fun Context.getLinkApp() = "https://play.google.com/store/apps/details?id=$packageName"

fun Context.hideKeyboard(view: View) {
    val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
}