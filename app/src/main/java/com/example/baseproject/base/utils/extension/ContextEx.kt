package com.example.baseproject.base.utils.extension

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityManager
import android.app.Service
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.icu.text.MeasureFormat
import android.icu.util.Measure
import android.icu.util.MeasureUnit
import android.location.Location
import android.location.LocationManager
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.BatteryManager
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.exifinterface.media.ExifInterface
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.baseproject.BuildConfig
import com.example.baseproject.R
import com.example.baseproject.base.utils.util.Constants
import com.example.baseproject.base.utils.util.ImageUtil
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.play.core.review.ReviewException
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.android.play.core.review.model.ReviewErrorCode
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID


fun Context.checkPermission(permission: String): Boolean {
    return ContextCompat.checkSelfPermission(this, permission) == PERMISSION_GRANTED
}

fun Context.hasWritePermission() = if (isSdkR()) {
    true
} else {
    this.checkPermission(WRITE_EXTERNAL_STORAGE)
}

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

fun Context.openActivity(
    pClass: Class<out Activity>,
    isFinish: Boolean = false,
    isAnimation: Boolean = true,
    bundle: Bundle?
) {
    openActivity(pClass, bundle)
    if (isAnimation) {
        (this as Activity).openWithSlide()
    }
    if (isFinish) {
        (this as Activity).finish()
    }
}

fun Context.loadImage(
    imageView: ImageView, url: String, error: Int = R.drawable.ic_launcher_background
) {
    Glide.with(this).load(url).fitCenter().placeholder(error).into(imageView)

}

fun Context.loadImage(
    imageView: ImageView, url: Int, error: Int = R.drawable.ic_launcher_background
) {
    Glide.with(this).load(url).fitCenter().placeholder(error).into(imageView)

}

@SuppressLint("DiscouragedApi")
fun Context.getDrawableIdByName(name: String): Int {
    return resources.getIdentifier(name, "drawable", packageName)
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
                Intent.ACTION_VIEW, Uri.parse(market + publishNameStore)
                //market://details?id=<package_name>
            )
        )
    } catch (e: ActivityNotFoundException) {
        startActivity(
            Intent(
                Intent.ACTION_VIEW, Uri.parse(webPlayStore + publishNameStore)
                //https://play.google.com/store/apps/details?id=<package_name>
            )
        )
    }
}

fun Context.rateApp() {
    val uri = Uri.parse("market://details?id=$packageName")
    val goToMarket = Intent(Intent.ACTION_VIEW, uri)
    goToMarket.flags = Intent.FLAG_ACTIVITY_NEW_TASK
    goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_ACTIVITY_NEW_DOCUMENT or Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
    try {
        startActivity(goToMarket)
    } catch (e: ActivityNotFoundException) {
        startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("http://play.google.com/store/apps/details?id=$packageName")
            )
        )
    }
}

fun Context.sendEmail(toEmail: String, feedBackString: String) {
    val intent = Intent(Intent.ACTION_VIEW)
    val data = Uri.parse(
        "mailto:$toEmail?subject=$feedBackString&body="
    )
    intent.data = data
    try {
        startActivity(intent)
    } catch (ex: Exception) {
        showToast("Not have email app to send email!")
        ex.printStackTrace()
    }
}

fun Context.getRingTone(): Ringtone {
    val defaultRingtoneUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
    return RingtoneManager.getRingtone(this, defaultRingtoneUri)
}

fun Context.isNetworkAvailable(): Boolean {
    val manager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val capabilities = manager.getNetworkCapabilities(manager.activeNetwork)
    return if (capabilities != null) {
        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) || capabilities.hasTransport(
            NetworkCapabilities.TRANSPORT_WIFI
        ) || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
    } else false
}

/**
 * require declare permission ACCESS_NETWORK_STATE in Manifest
 * */
fun Context.isInternetAvailable(): Boolean {
    val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val networkCapabilities = connectivityManager.activeNetwork ?: return false
    val actNw = connectivityManager.getNetworkCapabilities(networkCapabilities) ?: return false
    return when {
        actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
        actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
        actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
        else -> false
    }
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

/**
 * required permission WRITE_EXTERNAL_STORAGE ( if sdk <= 29)
 * */
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

/**
 * required permission WRITE_EXTERNAL_STORAGE ( if sdk <= 29)
 * */
fun Context.saveImageToLocal(bitmap: Bitmap, result: (Boolean) -> Unit) {
    if (!hasWritePermission()) {
        showToast("No have permission to write file")
        return
    }
    val resultSaveImage =
        ImageUtil.savePhotoToExternalStorage(contentResolver, UUID.randomUUID().toString(), bitmap)
    result.invoke(resultSaveImage)
}

fun getPromptInfo(title: String, subtitle: String, negativeButtonText: String) =
    BiometricPrompt.PromptInfo.Builder().setTitle(title).setSubtitle(subtitle)
        .setNegativeButtonText(negativeButtonText).build()

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
    @Suppress("DEPRECATION") return (getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager).getRunningServices(
        Int.MAX_VALUE
    ).any { serviceClass.name == it.service.className }
}

fun Context.getVersionName(): String {
    return try {
        val packageInfo: PackageInfo = packageManager.getPackageInfo(packageName, 0)
        packageInfo.versionName ?: BuildConfig.VERSION_NAME
    } catch (e: Exception) {
        BuildConfig.VERSION_NAME
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

@SuppressLint("HardwareIds")
fun Context.getDeviceId(): String {
    return Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
}

/**
 * @author doanvv
 * @contributor HuanND
 * */
fun Activity.showInAppReview() {
    val reviewManager = ReviewManagerFactory.create(this)
    reviewManager.requestReviewFlow().addOnCompleteListener { task ->
        if (task.isSuccessful) {
            val reviewInfo = task.result
            val flow = reviewManager.launchReviewFlow(this, reviewInfo)
            flow.addOnCompleteListener {
                // The flow has finished. The API does not indicate whether the user
                // reviewed or not, or even whether the review dialog was shown. Thus, no
                // matter the result, we continue our app flow.
            }

        } else {
            @ReviewErrorCode val reviewErrorCode = (task.exception as ReviewException).errorCode
        }
    }
}

fun Context.getUriByFileProvider(file: File): Uri {
    return FileProvider.getUriForFile(this, "$packageName.fileprovider", file)
}

fun Context.shareFileGif(file: File) {
    val fileUri = getUriByFileProvider(file)
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "image/gif"
        putExtra(Intent.EXTRA_STREAM, fileUri)
    }
    startActivity(Intent.createChooser(shareIntent, "Share File"))
}

/**
 * get bitmap online by glide
 * update: 04/11/2024
 * by: doan-vu.dev
 * */
fun Context.downloadBitmapByUrl(
    url: String,
    onProgressLoading: () -> Unit,
    onLoadedBitmap: (Bitmap) -> Unit,
    onLoadFailed: () -> Unit
) {
    val target = object : CustomTarget<Bitmap>() {
        override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
            onLoadedBitmap.invoke(resource)
        }

        override fun onLoadCleared(placeholder: Drawable?) {

        }

        override fun onLoadFailed(errorDrawable: Drawable?) {
            super.onLoadFailed(errorDrawable)
            onLoadFailed.invoke()
        }

        override fun onLoadStarted(placeholder: Drawable?) {
            super.onLoadStarted(placeholder)
            onProgressLoading.invoke()
        }
    }
    Glide.with(this).asBitmap().load(url).into(target)
}

fun Context.isGpsEnable(): Boolean {
    //todo: flag to open setting location source
//    Settings.ACTION_LOCATION_SOURCE_SETTINGS
    val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
    return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
//            && locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
}

@SuppressLint("MissingPermission")
fun Context.getLocationUser(
    onGetLocationComplete: ((location: Location) -> Unit)? = null,
    onMissingPermission: () -> Unit,
    onFail: (() -> Unit)? = null
) {
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    if (!checkPermission(Manifest.permission.ACCESS_FINE_LOCATION) && !checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION)) {
        showToast("Missing location permission")
        onMissingPermission.invoke()
        return
    }
    //check permission before get
    fusedLocationClient.getCurrentLocation(
        Priority.PRIORITY_HIGH_ACCURACY, CancellationTokenSource().token
    ).addOnSuccessListener { location: Location? ->
        location?.let {
            onGetLocationComplete?.invoke(it)
        } ?: kotlin.run {
            onFail?.invoke()
        }
    }
}

fun Context.hasWriteSettingPermission(): Boolean {
    return Settings.System.canWrite(this)
}

fun Context.hasOverlaySettingPermission(): Boolean {
    return Settings.canDrawOverlays(this)
}

fun Context.hasAnswerCallComing(): Boolean {
    return if (isSdk26()) {
        checkPermission(Manifest.permission.ANSWER_PHONE_CALLS)
    } else {
        false
    }
}

fun Context.hasReadContact(): Boolean {
    return checkPermission(Manifest.permission.READ_CONTACTS)
}

fun Context.isNotificationListenerServiceEnabled(): Boolean {
    val flat = Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
    return flat?.contains(packageName) ?: false
}

fun Context.getBatteryLevel(): Int {
    val batteryManager = getSystemService(Context.BATTERY_SERVICE) as BatteryManager
    return batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
}

fun Context.getAnimation(animationId: Int): Animation? {
    return AnimationUtils.loadAnimation(this, animationId)
}

/**
 * determine the conversion convention for kilobytes (1000 or 1024) by locale
 * */
fun Context.isKilobyteBasedOn1000(): Boolean {
    val formatter = MeasureFormat.getInstance(
        resources.configuration.locales[0], MeasureFormat.FormatWidth.SHORT
    )
    val oneKilobyte = Measure(1, MeasureUnit.KILOBYTE)
    return formatter.format(oneKilobyte)
        .contains("1000") // Check if the formatted output contains "1000"
}

fun Context.getOrientationImage(uri: Uri): Int? {
    try {
        val inputStream: InputStream?
        try {
            inputStream = contentResolver.openInputStream(Uri.parse(uri.toString()))
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            return null
        }

        inputStream?.let {
            val ei = ExifInterface(inputStream)
            return ei.getAttributeInt(
                ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL
            )
        }
    } catch (e: Exception) {
        Log.e(Constants.TAG, "modifyOrientation: ${e.message}")
        e.printStackTrace()
        return null
    }
    return null
}

fun Int?.isNeedRotateOrFlipImage(): Boolean {
    val exifList = listOf(
        ExifInterface.ORIENTATION_ROTATE_90,
        ExifInterface.ORIENTATION_ROTATE_180,
        ExifInterface.ORIENTATION_ROTATE_270,
        ExifInterface.ORIENTATION_FLIP_HORIZONTAL,
        ExifInterface.ORIENTATION_FLIP_VERTICAL
    )

    return exifList.contains(this)
}

fun modifyOrientation(bitmap: Bitmap, inputStream: InputStream): Bitmap {
    try {
        val ei = ExifInterface(inputStream)
        val orientation: Int =
            ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        return bitmap.modifyOrientation(orientation)
    } catch (e: Exception) {
        Log.e(Constants.TAG, "modifyOrientation: ${e.message}")
        e.printStackTrace()
        return bitmap
    }
}

fun Bitmap.modifyOrientation(orientation: Int?): Bitmap {
    try {
        return when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> this.rotate(90f)

            ExifInterface.ORIENTATION_ROTATE_180 -> this.rotate(180f)

            ExifInterface.ORIENTATION_ROTATE_270 -> this.rotate(270f)

            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> this.flip(true)

            ExifInterface.ORIENTATION_FLIP_VERTICAL -> this.flip(false)

            else -> this
        }
    } catch (e: Exception) {
        Log.e(Constants.TAG, "modifyOrientation: ${e.message}")
        e.printStackTrace()
        return this
    }
}

fun Context.getBitmapFromAsset(path: String): Bitmap =
    assets.open(path).use { BitmapFactory.decodeStream(it) }

fun Context.createImageFile(): File {
    val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    return File.createTempFile(
        "JPEG_${timeStamp}_",
        ".jpg",
        storageDir
    )
}