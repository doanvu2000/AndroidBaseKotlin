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
import androidx.core.net.toUri
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

//region Permission Management

/**
 * Kiểm tra permission
 */
fun Context.checkPermission(permission: String): Boolean {
    return ContextCompat.checkSelfPermission(this, permission) == PERMISSION_GRANTED
}

/**
 * Kiểm tra quyền write storage
 */
fun Context.hasWritePermission(): Boolean = if (isSdkR()) {
    true
} else {
    this.checkPermission(WRITE_EXTERNAL_STORAGE)
}

/**
 * Kiểm tra quyền read storage
 */
fun Context.hasReadStoragePermission(): Boolean = if (isSdk33()) {
    checkPermission(READ_MEDIA_IMAGE)
} else {
    checkPermission(READ_EXTERNAL_STORAGE)
}

/**
 * Kiểm tra quyền location
 */
fun Context.hasLocationPermission(): Boolean {
    return checkPermission(ACCESS_COARSE_LOCATION) && checkPermission(ACCESS_FINE_LOCATION)
}

/**
 * Kiểm tra quyền notification
 */
fun Context.hasNotificationPermission(): Boolean {
    return if (!isSdk33()) {
        true
    } else {
        checkPermission(POST_NOTIFICATION)
    }
}

/**
 * Kiểm tra quyền write settings
 */
fun Context.hasWriteSettingPermission(): Boolean = Settings.System.canWrite(this)

/**
 * Kiểm tra quyền overlay
 */
fun Context.hasOverlaySettingPermission(): Boolean = Settings.canDrawOverlays(this)

/**
 * Kiểm tra quyền answer call
 */
fun Context.hasAnswerCallComing(): Boolean {
    return if (isSdk26()) {
        checkPermission(Manifest.permission.ANSWER_PHONE_CALLS)
    } else {
        false
    }
}

/**
 * Kiểm tra quyền read contacts
 */
fun Context.hasReadContact(): Boolean = checkPermission(Manifest.permission.READ_CONTACTS)

/**
 * Request permission read storage
 */
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

//endregion

//region Activity Navigation

/**
 * Mở Activity với Bundle
 */
fun Context.openActivity(pClass: Class<out Activity>, bundle: Bundle?) {
    val intent = Intent(this, pClass)
    if (bundle != null) {
        intent.putExtras(bundle)
    }
    startActivity(intent)
}

/**
 * Mở Activity với finish option
 */
fun Context.openActivity(pClass: Class<out Activity>, isFinish: Boolean = false) {
    openActivity(pClass, null)
    if (isFinish) {
        (this as Activity).finish()
    }
}

/**
 * Mở Activity với finish và bundle
 */
fun Context.openActivity(pClass: Class<out Activity>, isFinish: Boolean = false, bundle: Bundle?) {
    openActivity(pClass, bundle)
    if (isFinish) {
        (this as Activity).finish()
    }
}

/**
 * Mở Activity với animation
 */
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

//endregion

//region Image Loading

/**
 * Load image từ URL
 */
fun Context.loadImage(
    imageView: ImageView,
    url: String,
    error: Int = R.drawable.ic_launcher_background
) {
    Glide.with(this).load(url).fitCenter().placeholder(error).into(imageView)
}

/**
 * Load image từ resource
 */
fun Context.loadImage(
    imageView: ImageView,
    url: Int,
    error: Int = R.drawable.ic_launcher_background
) {
    Glide.with(this).load(url).fitCenter().placeholder(error).into(imageView)
}

/**
 * Download bitmap từ URL
 */
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

        override fun onLoadCleared(placeholder: Drawable?) {}

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

//endregion

//region Resource Management

/**
 * Lấy drawable ID theo tên
 */
@SuppressLint("DiscouragedApi")
fun Context.getDrawableIdByName(name: String): Int {
    return resources.getIdentifier(name, "drawable", packageName)
}

/**
 * Inflate layout
 */
fun Context.inflateLayout(layoutResource: Int, parent: ViewGroup): View {
    return LayoutInflater.from(this).inflate(layoutResource, parent, false)
}

/**
 * Lấy color theo ID
 */
fun Context.getColorById(colorSource: Int): Int = ContextCompat.getColor(this, colorSource)

/**
 * Lấy color theo theme attribute
 */
fun Context.getColorByIdWithTheme(colorAttr: Int): Int {
    val typedValue = TypedValue()
    theme.resolveAttribute(colorAttr, typedValue, true)
    return typedValue.data
}

/**
 * Lấy drawable theo ID
 */
fun Context.getDrawableById(drawableId: Int): Drawable? =
    ContextCompat.getDrawable(this, drawableId)

/**
 * Lấy animation theo ID
 */
fun Context.getAnimation(animationId: Int): Animation? =
    AnimationUtils.loadAnimation(this, animationId)

//endregion

//region UI Utilities

/**
 * Hiển thị Toast
 */
fun Context.showToast(msg: String, isShowDurationLong: Boolean = false) {
    val duration = if (isShowDurationLong) Toast.LENGTH_LONG else Toast.LENGTH_SHORT
    Toast.makeText(this, msg, duration).show()
}

/**
 * Ẩn bàn phím
 */
fun Context.hideKeyboard(view: View) {
    val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
}

//endregion

//region App Sharing & Rating

/**
 * Share app
 */
fun Context.shareApp() {
    val subject = "Let go to record your emoji today!!"
    val sharingIntent = Intent(Intent.ACTION_SEND)
    sharingIntent.type = "text/plain"
    val shareBody = "https://play.google.com/store/apps/details?id=$packageName"
    sharingIntent.putExtra(Intent.EXTRA_SUBJECT, subject)
    sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody)
    this.startActivity(Intent.createChooser(sharingIntent, "Share to"))
}

/**
 * Navigate to market
 */
fun Context.navigateToMarket(publishNameStore: String) {
    val market = "market://details?id="
    val webPlayStore = "https://play.google.com/store/apps/details?id="
    try {
        startActivity(Intent(Intent.ACTION_VIEW, (market + publishNameStore).toUri()))
    } catch (e: ActivityNotFoundException) {
        startActivity(Intent(Intent.ACTION_VIEW, (webPlayStore + publishNameStore).toUri()))
    }
}

/**
 * Rate app
 */
fun Context.rateApp() {
    val uri = "market://details?id=$packageName".toUri()
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

/**
 * Share text
 */
fun Context.shareText(value: String) {
    val sendIntent: Intent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, value)
        type = "text/plain"
    }
    val shareIntent = Intent.createChooser(sendIntent, null)
    startActivity(shareIntent)
}

/**
 * Lấy link app
 */
fun Context.getLinkApp(): String = "https://play.google.com/store/apps/details?id=$packageName"

//endregion

//region Email & Communication

/**
 * Gửi email
 */
fun Context.sendEmail(toEmail: String, feedBackString: String) {
    val intent = Intent(Intent.ACTION_VIEW)
    val data = "mailto:$toEmail?subject=$feedBackString&body=".toUri()
    intent.data = data
    try {
        startActivity(intent)
    } catch (ex: Exception) {
        showToast("Not have email app to send email!")
        ex.printStackTrace()
    }
}

/**
 * Lấy ringtone
 */
fun Context.getRingTone(): Ringtone {
    val defaultRingtoneUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
    return RingtoneManager.getRingtone(this, defaultRingtoneUri)
}

//endregion

//region Network & Connectivity

/**
 * Kiểm tra network có available không
 */
fun Context.isNetworkAvailable(): Boolean {
    val manager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val capabilities = manager.getNetworkCapabilities(manager.activeNetwork)
    return if (capabilities != null) {
        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
    } else false
}

/**
 * Kiểm tra internet có available không
 * Require declare permission ACCESS_NETWORK_STATE in Manifest
 */
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

//endregion

//region Service Management

/**
 * Kết nối service
 */
fun Context.connectService(pClass: Class<out Service>) {
    startService(Intent(this, pClass))
}

/**
 * Kết thúc service
 */
fun Context.endService(pClass: Class<out Service>) {
    stopService(Intent(this, pClass))
}

/**
 * Kiểm tra service có đang chạy không
 */
fun Context.serviceIsRunning(serviceClass: Class<*>): Boolean {
    @Suppress("DEPRECATION")
    return (getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager)
        .getRunningServices(Int.MAX_VALUE)
        .any { serviceClass.name == it.service.className }
}

//endregion

//region Image & File Management

/**
 * Share image
 * Required permission WRITE_EXTERNAL_STORAGE (if sdk <= 29)
 */
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
 * Save image to local
 * Required permission WRITE_EXTERNAL_STORAGE (if sdk <= 29)
 */
fun Context.saveImageToLocal(bitmap: Bitmap, result: (Boolean) -> Unit) {
    if (!hasWritePermission()) {
        showToast("No have permission to write file")
        return
    }
    val resultSaveImage = ImageUtil.savePhotoToExternalStorage(
        contentResolver,
        UUID.randomUUID().toString(),
        bitmap
    )
    result.invoke(resultSaveImage)
}

/**
 * Lấy URI từ FileProvider
 */
fun Context.getUriByFileProvider(file: File): Uri {
    return FileProvider.getUriForFile(this, "$packageName.fileprovider", file)
}

/**
 * Share file GIF
 */
fun Context.shareFileGif(file: File) {
    val fileUri = getUriByFileProvider(file)
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "image/gif"
        putExtra(Intent.EXTRA_STREAM, fileUri)
    }
    startActivity(Intent.createChooser(shareIntent, "Share File"))
}

/**
 * Lấy bitmap từ assets
 */
fun Context.getBitmapFromAsset(path: String): Bitmap =
    assets.open(path).use { BitmapFactory.decodeStream(it) }

/**
 * Tạo image file
 */
fun Context.createImageFile(): File {
    val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir)
}

//endregion

//region Assets & JSON

/**
 * Load JSON from assets
 * Ex: assets/data.json => path = data.json
 *     assets/db/data.json => path = db/data.json
 */
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

//endregion

//region Device Information

/**
 * Lấy version name
 */
fun Context.getVersionName(): String {
    return try {
        val packageInfo: PackageInfo = packageManager.getPackageInfo(packageName, 0)
        packageInfo.versionName ?: BuildConfig.VERSION_NAME
    } catch (e: Exception) {
        BuildConfig.VERSION_NAME
    }
}

/**
 * Lấy device ID
 */
@SuppressLint("HardwareIds")
fun Context.getDeviceId(): String {
    return Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
}

/**
 * Lấy battery level
 */
fun Context.getBatteryLevel(): Int {
    val batteryManager = getSystemService(Context.BATTERY_SERVICE) as BatteryManager
    return batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
}

/**
 * Kiểm tra GPS có enable không
 */
fun Context.isGpsEnable(): Boolean {
    val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
    return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
}

/**
 * Kiểm tra notification listener service có enable không
 */
fun Context.isNotificationListenerServiceEnabled(): Boolean {
    val flat = Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
    return flat?.contains(packageName) ?: false
}

/**
 * Kiểm tra kilobyte base trên 1000 hay 1024
 */
fun Context.isKilobyteBasedOn1000(): Boolean {
    val formatter = MeasureFormat.getInstance(
        resources.configuration.locales[0], MeasureFormat.FormatWidth.SHORT
    )
    val oneKilobyte = Measure(1, MeasureUnit.KILOBYTE)
    return formatter.format(oneKilobyte).contains("1000")
}

//endregion

//region Location Services

/**
 * Lấy location của user
 */
@SuppressLint("MissingPermission")
fun Context.getLocationUser(
    onGetLocationComplete: ((location: Location) -> Unit)? = null,
    onMissingPermission: () -> Unit,
    onFail: (() -> Unit)? = null
) {
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    if (!checkPermission(Manifest.permission.ACCESS_FINE_LOCATION) &&
        !checkPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
    ) {
        showToast("Missing location permission")
        onMissingPermission.invoke()
        return
    }

    fusedLocationClient.getCurrentLocation(
        Priority.PRIORITY_HIGH_ACCURACY, CancellationTokenSource().token
    ).addOnSuccessListener { location: Location? ->
        location?.let {
            onGetLocationComplete?.invoke(it)
        } ?: run {
            onFail?.invoke()
        }
    }
}

//endregion

//region Image Orientation

/**
 * Lấy orientation của image
 */
fun Context.getOrientationImage(uri: Uri): Int? {
    try {
        val inputStream: InputStream? = try {
            contentResolver.openInputStream(Uri.parse(uri.toString()))
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            return null
        }

        inputStream?.let {
            val ei = ExifInterface(inputStream)
            return ei.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
        }
    } catch (e: Exception) {
        Log.e(Constants.TAG, "modifyOrientation: ${e.message}")
        e.printStackTrace()
        return null
    }
    return null
}

/**
 * Kiểm tra có cần rotate hoặc flip image không
 */
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

/**
 * Modify orientation
 */
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

/**
 * Modify orientation for Bitmap
 */
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

//endregion

//region In-App Review

/**
 * Hiển thị in-app review
 * @author doanvv
 * @contributor HuanND
 */
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

//endregion

//region Biometric

/**
 * Tạo PromptInfo cho biometric
 */
fun getPromptInfo(title: String, subtitle: String, negativeButtonText: String) =
    BiometricPrompt.PromptInfo.Builder()
        .setTitle(title)
        .setSubtitle(subtitle)
        .setNegativeButtonText(negativeButtonText)
        .build()

/**
 * Biometric callback
 */
val biometricCall = object : BiometricPrompt.AuthenticationCallback() {
    // Implement callbacks as needed
}

//endregion
