package com.example.baseproject.base.utils.extension

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Activity.OVERRIDE_TRANSITION_CLOSE
import android.app.Activity.OVERRIDE_TRANSITION_OPEN
import android.app.role.RoleManager
import android.content.ActivityNotFoundException
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Context.ROLE_SERVICE
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.OpenableColumns
import android.provider.Settings
import android.telecom.TelecomManager
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.baseproject.R
import com.example.baseproject.base.base_view.screen.BaseActivity
import com.example.baseproject.base.utils.util.Constants
import com.example.baseproject.base.utils.util.DownloadUtil
import com.google.android.gms.ads.AdSize
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.Executor
import kotlin.coroutines.CoroutineContext

//region Fullscreen và Screen Management

/**
 * Thiết lập chế độ fullscreen cho Activity
 * @param isFullScreen true để bật fullscreen, false để tắt
 */
fun Activity.setFullScreenMode(isFullScreen: Boolean = false) {
    if (isFullScreen) {
        if (isSdkR()) {
            window.insetsController?.apply {
                hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            @Suppress("DEPRECATION")
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
    } else {
        if (isSdkR()) {
            window.insetsController?.apply {
                show(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            @Suppress("DEPRECATION")
            window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        }
    }
}

/**
 * Thiết lập layout fullscreen cho Activity (deprecated method support)
 */
@Suppress("DEPRECATION")
fun Activity.setLayoutParamFullScreen() {
    window?.let {
        it.decorView.apply {
            systemUiVisibility =
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN
        }
        it.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
    }
}

/**
 * Lấy chiều rộng màn hình
 */
fun Activity.getScreenWidth(): Int {
    return if (isSdkR()) {
        val windowMetrics = windowManager.currentWindowMetrics
        val insets =
            windowMetrics.windowInsets.getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())
        windowMetrics.bounds.width() - insets.left - insets.right
    } else {
        val displayMetrics = DisplayMetrics()
        @Suppress("DEPRECATION")
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        displayMetrics.widthPixels
    }
}

/**
 * Lấy chiều cao màn hình
 */
fun Activity.getScreenHeight(): Int {
    return if (isSdkR()) {
        val windowMetrics = windowManager.currentWindowMetrics
        val insets =
            windowMetrics.windowInsets.getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())
        windowMetrics.bounds.height() - insets.top - insets.bottom
    } else {
        val displayMetrics = DisplayMetrics()
        @Suppress("DEPRECATION")
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        displayMetrics.heightPixels
    }
}

/**
 * Lấy AdSize theo kích thước màn hình
 */
fun Activity.getAdSizeFollowScreen(): AdSize {
    val display = windowManager.defaultDisplay
    val outMetrics = DisplayMetrics()
    display.getMetrics(outMetrics)
    val density = outMetrics.density
    var adWidthPixels = resources.displayMetrics.widthPixels.toFloat()
    if (adWidthPixels == 0f) {
        adWidthPixels = outMetrics.widthPixels.toFloat()
    }
    val adWidth = (adWidthPixels / density).toInt()
    return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(this, adWidth)
}

//endregion

//region Screen Control

/**
 * Bật chế độ giữ màn hình sáng
 */
fun Activity.enableScreenOn() {
    window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
}

/**
 * Tắt chế độ giữ màn hình sáng
 */
fun Activity.disableScreenOn() {
    window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
}

//endregion

//region Keyboard Management

/**
 * Ẩn bàn phím
 */
fun Activity.hideKeyboard() {
    hideKeyboard(currentFocus ?: View(this))
}

//endregion

//region UI Components

/**
 * Hiển thị SnackBar
 * @param msg tin nhắn hiển thị
 * @param duration thời gian hiển thị
 * @param isShowActionOK có hiển thị button OK hay không
 * @param action callback khi click OK
 */
fun Activity.showSnackBar(
    msg: String,
    duration: Int = 500,
    isShowActionOK: Boolean = false,
    action: (() -> Unit)? = null
) {
    val view = window.decorView.findViewById<View>(android.R.id.content)
    view?.let {
        val snackBar = Snackbar.make(it, msg, duration)
        if (isShowActionOK) {
            snackBar.setAction("OK") { action?.invoke() }
        }
        snackBar.show()
    }
}

//endregion

//region Permission Management

/**
 * Đăng ký ActivityResultLauncher cho multiple permissions
 */
fun ComponentActivity.getActivityResultLauncher(
    callBack: (Map<String, Boolean>) -> Unit
): ActivityResultLauncher<Array<String>> {
    return registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        callBack.invoke(permissions)
    }
}

/**
 * Request multiple permissions
 */
fun AppCompatActivity.requestMultiplePermission(permission: List<String>) {
    registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        // Handle result here if needed
    }.launch(permission.toTypedArray())
}

/**
 * Kiểm tra xem có nên hiển thị rationale cho permission không
 */
fun Activity.checkShowRationale(permission: String): Boolean =
    ActivityCompat.shouldShowRequestPermissionRationale(this, permission)

//endregion

//region System Actions

/**
 * Mở SMS với nội dung và số điện thoại
 */
fun Activity.openSMS(smsBody: String, phone: String) {
    val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:$phone"))
    intent.putExtra("sms_body", smsBody)
    startActivity(intent)
}

/**
 * Gọi điện thoại
 */
fun Activity.callPhone(phone: String) {
    val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$phone"))
    startActivity(intent)
}

/**
 * Mở cài đặt mạng
 */
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

/**
 * Mở trang cài đặt chi tiết của app
 */
fun Activity.gotoDetailSetting() {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
    intent.data = Uri.parse("package:$packageName")
    startActivity(intent)
}

/**
 * Mở trang cài đặt Schedule Exact Alarm (API 31+)
 */
fun Activity.openExactAlarmSettingPage() {
    if (isSdkS()) {
        startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM))
    }
}

/**
 * Mở cài đặt Write Settings (API 23+)
 */
fun Activity.openManageWriteSetting() {
    val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
    intent.data = Uri.parse("package:$packageName")
    startActivity(intent)
}

/**
 * Mở cài đặt Overlay Permission (API 23+)
 */
fun Activity.openSettingOverlay() {
    val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
    intent.data = Uri.fromParts("package", packageName, null)
    startActivity(intent)
}

//endregion

//region Biometric Authentication

/**
 * Hiển thị xác thực vân tay
 */
fun FragmentActivity.showAuthenticatorWithFinger(
    title: String = "Title",
    subtitle: String = "Subtitle",
    negativeButtonText: String = "Cancel"
) {
    val executor: Executor = ContextCompat.getMainExecutor(this)
    val biometricPrompt = BiometricPrompt(this, executor, biometricCall)
    val promptInfo = getPromptInfo(title, subtitle, negativeButtonText)
    biometricPrompt.authenticate(promptInfo)
}

/**
 * Kiểm tra thiết bị có hỗ trợ vân tay hay không
 */
fun Activity.checkDeviceHasFingerprint(): Boolean {
    val biometricManager = BiometricManager.from(this)
    return when (biometricManager.canAuthenticate(
        BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL
    )) {
        BiometricManager.BIOMETRIC_SUCCESS -> {
            Log.d(Constants.TAG, "App can authenticate using biometrics.")
            true
        }
        BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
            Log.d(Constants.TAG, "No biometric features available on this device.")
            false
        }
        BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
            Log.d(Constants.TAG, "Biometric features are currently unavailable.")
            false
        }
        BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
            Log.d(Constants.TAG, "Device has fingerprint but not set.")
            false
        }

        else -> false
    }
}

//endregion

//region Default App Settings

/**
 * Thiết lập app làm ứng dụng gọi điện mặc định
 */
fun Activity.setDefaultPhoneApp() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val roleManager = getSystemService(ROLE_SERVICE) as RoleManager
        val isHasRole = roleManager.isRoleAvailable(RoleManager.ROLE_DIALER)
        val isAppRoleHeld = roleManager.isRoleHeld(RoleManager.ROLE_DIALER)
        if (isHasRole && !isAppRoleHeld) {
            val intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_DIALER)
            // startChangeDefaultDialler.launch(intent)
        }
    } else {
        val intent = Intent(TelecomManager.ACTION_CHANGE_DEFAULT_DIALER)
        intent.putExtra(TelecomManager.EXTRA_CHANGE_DEFAULT_DIALER_PACKAGE_NAME, packageName)
        // startChangeDefaultDialler.launch(intent)
    }
}

//endregion

//region BroadcastReceiver Management

/**
 * Đăng ký BroadcastReceiver với flags phù hợp theo API level
 */
@SuppressLint("UnspecifiedRegisterReceiverFlag")
fun Activity.registerBroadcast(
    broadcastReceiver: BroadcastReceiver,
    intentFilter: IntentFilter,
    listenToBroadcastsFromOtherApps: Boolean = false
) {
    if (isSdk33()) {
        val receiverFlags = if (listenToBroadcastsFromOtherApps) {
            Context.RECEIVER_EXPORTED
        } else {
            Context.RECEIVER_NOT_EXPORTED
        }
        registerReceiver(broadcastReceiver, intentFilter, receiverFlags)
    } else {
        registerReceiver(broadcastReceiver, intentFilter)
    }
}

//endregion

//region Activity Transitions

/**
 * Mở Activity với animation slide
 */
fun Activity.openWithSlide() {
    if (isSdk34()) {
        overrideActivityTransition(
            OVERRIDE_TRANSITION_OPEN,
            R.anim.slide_in_right,
            R.anim.slide_out_left,
            Color.TRANSPARENT
        )
    } else {
        @Suppress("DEPRECATION")
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }
}

/**
 * Finish Activity với animation slide
 */
fun Activity.finishWithSlide() {
    finish()
    if (isSdk34()) {
        overrideActivityTransition(
            OVERRIDE_TRANSITION_CLOSE,
            R.anim.slide_in_left,
            R.anim.slide_out_right,
            Color.TRANSPARENT
        )
    } else {
        @Suppress("DEPRECATION")
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }
}

//endregion

//region Fragment Management

/**
 * Tìm Fragment theo tag
 */
fun AppCompatActivity.findFragment(tag: String): Fragment? {
    return supportFragmentManager.findFragmentByTag(tag)
}

/**
 * Replace Fragment với options
 */
fun AppCompatActivity.replaceFragment(
    frameId: Int,
    fragment: Fragment,
    addToBackStack: Boolean = true,
    bundle: Bundle? = null
) {
    bundle?.let { fragment.arguments = it }
    if (addToBackStack) {
        replaceFragmentAddBackStack(frameId, fragment)
    } else {
        replaceFragmentNoAddBackStack(frameId, fragment)
    }
}

/**
 * Replace Fragment không add vào BackStack
 */
fun AppCompatActivity.replaceFragmentNoAddBackStack(frameId: Int, fragment: Fragment) {
    Log.d(Constants.TAG, "replaceFragment no add backstack: ${fragment.javaClass.name}")
    supportFragmentManager.beginTransaction().replace(frameId, fragment).commit()
}

/**
 * Replace Fragment và add vào BackStack
 */
fun AppCompatActivity.replaceFragmentAddBackStack(frameId: Int, fragment: Fragment) {
    Log.d(Constants.TAG, "replaceFragment add backstack: ${fragment.javaClass.name}")
    supportFragmentManager.beginTransaction()
        .replace(frameId, fragment)
        .addToBackStack(fragment.javaClass.name)
        .commit()
}

/**
 * Remove Fragment
 */
fun AppCompatActivity.removeFragment(fragment: Fragment) {
    supportFragmentManager.beginTransaction().remove(fragment).commit()
}

//endregion

//region Back Press Management

/**
 * Handle back press với callback
 */
fun ComponentActivity.handleBackPressed(action: () -> Unit) {
    onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            action()
        }
    })
}

//endregion

//region File Utilities

/**
 * Lấy MIME type của file từ Uri
 */
fun Activity.getMimeType(uri: Uri): String? {
    return contentResolver.getType(uri)
}

/**
 * Lấy tên và kích thước file từ Uri
 */
fun Activity.getNameAndSizeFile(uri: Uri, onQueryFile: (name: String, size: Long) -> Unit) {
    contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
        cursor.moveToFirst()
        val name = cursor.getString(nameIndex)
        val size = cursor.getLong(sizeIndex)
        onQueryFile.invoke(name, size)
    }
}

//endregion

//region Coroutine Utilities

/**
 * Delay trước khi thực hiện action
 */
fun AppCompatActivity.delayBeforeAction(timeDelay: Long = 300, action: () -> Unit) {
    lifecycleScope.launch(Dispatchers.Main) {
        withContext(Dispatchers.IO) { delay(timeDelay) }
        action()
    }
}

/**
 * Launch coroutine với dispatcher
 */
fun AppCompatActivity.launchWitCoroutine(
    dispatcher: CoroutineContext = Dispatchers.Main,
    action: () -> Unit
) {
    lifecycleScope.launch(dispatcher) { action() }
}

/**
 * Download audio file
 */
fun AppCompatActivity.downloadAudio(
    fileName: String,
    src: String,
    timeDelay: Long = 300,
    onDone: (File) -> Unit,
    onFail: () -> Unit
) {
    DownloadUtil.downloadAudio(lifecycleScope, cacheDir, fileName, src, timeDelay, onDone, onFail)
}

/**
 * Launch coroutine khi Activity ở trạng thái STARTED
 */
fun BaseActivity<*>.launchOnStarted(block: suspend CoroutineScope.() -> Unit) {
    launchCoroutine(Dispatchers.Main) {
        repeatOnLifecycle(Lifecycle.State.STARTED) {
            block()
        }
    }
}