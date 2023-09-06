package com.example.baseproject.base.utils

import android.Manifest
import android.app.Activity
import android.app.ActivityManager
import android.app.Service
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.media.Ringtone
import android.media.RingtoneManager
import android.media.audiofx.AudioEffect
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.bumptech.glide.Glide
import com.example.baseproject.BuildConfig
import com.example.baseproject.R
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.util.*
import java.util.concurrent.Executor

fun Context.isReadPermissionGranted() = this.checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
fun Context.isWritePermissionGranted() = this.checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)

fun Context.checkPermission(permission: String): Boolean {
    return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
}

fun Fragment.checkPermission(permission: String): Boolean {
    return requireContext().checkPermission(permission)
}

fun Context.hasReadStoragePermission() = checkPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE)
fun Fragment.hasReadStoragePermission() = requireContext().hasReadStoragePermission()
fun Fragment.getActivityResultLauncher(callBack: (Map<String, Boolean>) -> Unit): ActivityResultLauncher<Array<String>> {
    return registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        callBack.invoke(permissions)
    }
}

fun ComponentActivity.getActivityResultLauncher(callBack: (Map<String, Boolean>) -> Unit): ActivityResultLauncher<Array<String>> {
    return registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        callBack.invoke(permissions)
    }
}

fun Context.requestPermissionReadStorage(permissionLauncher: ActivityResultLauncher<Array<String>>) {
    val isReadPermissionGranted = hasReadStoragePermission()

    val permissionRequest = mutableListOf<String>()
    if (!isReadPermissionGranted) {
        permissionRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)
    }
    if (permissionRequest.isNotEmpty()) {
        permissionLauncher.launch(permissionRequest.toTypedArray())
    }
}

fun Fragment.requestPermissionReadStorage(permissionLauncher: ActivityResultLauncher<Array<String>>) {
    requireContext().requestPermissionReadStorage(permissionLauncher)
}

fun AppCompatActivity.requestMultiplePermission(permission: List<String>) {
    registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {

    }.launch(permission.toTypedArray())
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

fun AppCompatActivity.findFragment(TAG: String): Fragment? {
    return supportFragmentManager.findFragmentByTag(TAG)
}

fun Fragment.findChildFragment(TAG: String): Fragment? {
    return childFragmentManager.findFragmentByTag(TAG)
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

fun Activity.openSMS(smsBody: String, phone: String) {
    val intent = Intent(
        Intent.ACTION_SENDTO,
        Uri.parse("smsto:$phone")
    )
    intent.putExtra("sms_body", smsBody)
    this.startActivity(intent)
}

fun Activity.callPhone(phone: String) {
    val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$phone"))
    startActivity(intent)
}

fun FragmentActivity.openNetWorkSetting() {
    try {
        val intent = Intent(Intent.ACTION_MAIN, null)
        intent.addCategory(Intent.CATEGORY_LAUNCHER)
        val cn = ComponentName("com.android.settings", "com.android.settings.wifi.WifiSettings")
        intent.component = cn
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    } catch (ignored: ActivityNotFoundException) {
        startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
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
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
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

fun Fragment.showToast(msg: String, isShowDurationLong: Boolean = false) {
    requireContext().showToast(msg, isShowDurationLong)
}

fun Context.connectService(pClass: Class<out Service>) {
    startService(Intent(this, pClass))
}

fun Context.endService(pClass: Class<out Service>) {
    stopService(Intent(this, pClass))
}

//required permission WRITE_EXTERNAL_STORAGE ( if sdk <= 29)
fun Context.shareImage(bitmap: Bitmap) {
    ImageUtil.insertSharingImage(contentResolver, bitmap)?.also { uri ->
        val intent = Intent(Intent.ACTION_SEND)
        intent.putExtra(Intent.EXTRA_STREAM, uri)
        intent.type = ImageUtil.TYPE_JPEG
        startActivity(Intent.createChooser(intent, getString(R.string.app_name)))
    }
}

//required permission WRITE_EXTERNAL_STORAGE ( if sdk <= 29)
fun Context.saveImageToLocal(bitmap: Bitmap, result: (Boolean) -> Unit) {
    val resultSaveImage = ImageUtil.savePhotoToExternalStorage(contentResolver, UUID.randomUUID().toString(), bitmap)
    result.invoke(resultSaveImage)
}

//using finger to authentication in activity
fun FragmentActivity.showAuthenticatorWithFinger(
    title: String = "Title", subtitle: String = "Subtitle", negativeButtonText: String = "Cancel"
) {
    val biometricPrompt: BiometricPrompt
    val executor: Executor = ContextCompat.getMainExecutor(this)
    biometricPrompt = BiometricPrompt(this, executor, biometricCall)

    val promptInfo: BiometricPrompt.PromptInfo = getPromptInfo(title, subtitle, negativeButtonText)
    biometricPrompt.authenticate(promptInfo)
}

fun Activity.checkDeviceHasFingerprint(): Boolean {
    val biometricManager = BiometricManager.from(this)
    when (biometricManager.canAuthenticate(
        BiometricManager.Authenticators.BIOMETRIC_STRONG or
                BiometricManager.Authenticators.DEVICE_CREDENTIAL
    )) {
        BiometricManager.BIOMETRIC_SUCCESS -> {
            Log.d(Constant.TAG, "App can authenticate using biometrics.")
            return true
        }

        BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
            Log.d(Constant.TAG, "No biometric features available on this device.")
            return false
        }

        BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
            Log.d(Constant.TAG, "Biometric features are currently unavailable.")
            return false
        }

        BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
            Log.d(Constant.TAG, "Device has fingerprint but not set.")
            /*
            // Prompts the user to create credentials that your app accepts.
//            if (isMinSdk30) {
//                val enrollIntent = Intent(Settings.ACTION_BIOMETRIC_ENROLL).apply {
//                    putExtra(
//                        Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
//                        BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL
//                    )
//                }
//                startActivityForResult(enrollIntent, 1234)
//            }
        }
//        BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> {
//
//        }
//        BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED -> {
//
//        }
//        BiometricManager.BIOMETRIC_STATUS_UNKNOWN -> {
//
//        }
*/
        }
    }
    return false
}

//using finger to authentication in fragment
fun Fragment.showAuthenticatorWithFinger(
    title: String = "Title", subtitle: String = "Subtitle", negativeButtonText: String = "Cancel"
) {
    val biometricPrompt: BiometricPrompt
    val executor: Executor = ContextCompat.getMainExecutor(requireContext())
    biometricPrompt = BiometricPrompt(this, executor, biometricCall)

    val promptInfo: BiometricPrompt.PromptInfo = getPromptInfo(title, subtitle, negativeButtonText)
    biometricPrompt.authenticate(promptInfo)
}

private fun getPromptInfo(title: String, subtitle: String, negativeButtonText: String) = BiometricPrompt.PromptInfo.Builder()
    .setTitle(title)
    .setSubtitle(subtitle)
    .setNegativeButtonText(negativeButtonText)
    .build()

val biometricCall = object : BiometricPrompt.AuthenticationCallback() {
    override fun onAuthenticationError(
        errorCode: Int,
        errString: CharSequence
    ) {
        super.onAuthenticationError(errorCode, errString)
//                    showToast(getString(R.string.txt_authen_finger_error) + errString)
    }

    override fun onAuthenticationSucceeded(
        result: BiometricPrompt.AuthenticationResult
    ) {
        super.onAuthenticationSucceeded(result)
//                    showToast(getString(R.string.txt_authen_finger_success))
    }

    override fun onAuthenticationFailed() {
        super.onAuthenticationFailed()
//                    showToast(getString(R.string.txt_authen_finger_failed_v2))
    }
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

fun Fragment.getVersionName(): String {
    return requireContext().getVersionName()
}

fun Fragment.openEqualizerSetting(audioSessionId: Int) {
    try {
        val equalizerIntent = Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL)
        equalizerIntent.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, audioSessionId)
        equalizerIntent.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, requireContext().packageName)
        equalizerIntent.putExtra(AudioEffect.EXTRA_CONTENT_TYPE, AudioEffect.CONTENT_TYPE_MUSIC)
        startActivityForResult(equalizerIntent, 133)
    } catch (e: Exception) {
        showToast("Equalizer feature not supported!")
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

private fun saveStringToFile(context: Context, content: String, fileName: String): File? {
    if (context.isWritePermissionGranted()) {
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

private fun shareFile(context: Context, file: File) {
    val fileUri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", file)

    val shareIntent = Intent(Intent.ACTION_SEND)
    shareIntent.type = "application/json"
    shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri)
    context.startActivity(Intent.createChooser(shareIntent, "Share JSON File"))
}
