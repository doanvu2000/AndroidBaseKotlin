package com.example.baseproject.base.utils.extension

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.provider.Settings
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.example.baseproject.R
import com.example.baseproject.base.utils.Constant
import com.google.android.material.snackbar.Snackbar
import java.util.concurrent.Executor

fun Activity.setFullScreenMode(isFullScreen: Boolean = false) {
    if (isFullScreen) {
        if (isSdkR()) {
            val controller = window.insetsController

            if (controller != null) {
                controller.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                controller.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            @Suppress("DEPRECATION")
            window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        }
    } else {
        if (isSdkR()) {
            val controller = window.insetsController

            if (controller != null) {
                controller.show(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                controller.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            @Suppress("DEPRECATION")
            window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        }
    }
}

fun Activity.hideKeyboard() {
    hideKeyboard(currentFocus ?: View(this))
}

fun Activity.showSnackBar(msg: String, duration: Int = 500) {
    val view = window.decorView.findViewById<View>(android.R.id.content)
    view?.let {
        val snackBar = Snackbar.make(it, msg, duration)
//            .setTextColor(getColorById(R.color.text_selected))
//        val snackView = snackBar.view
//        snackView.setBackgroundColor(getColorById(R.color.color_app))
        snackBar.show()
    }
}

fun ComponentActivity.getActivityResultLauncher(callBack: (Map<String, Boolean>) -> Unit): ActivityResultLauncher<Array<String>> {
    return registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        callBack.invoke(permissions)
    }
}

fun AppCompatActivity.requestMultiplePermission(permission: List<String>) {
    registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {

    }.launch(permission.toTypedArray())
}

fun AppCompatActivity.findFragment(TAG: String): Fragment? {
    return supportFragmentManager.findFragmentByTag(TAG)
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
*/
        }

        BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> {
            return false
        }

        BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED -> {
            return false
        }

        BiometricManager.BIOMETRIC_STATUS_UNKNOWN -> {
            return false
        }
    }
    return false
}

@SuppressLint("UnspecifiedRegisterReceiverFlag")
fun Activity.registerBroadcast(
    broadcastReceiver: BroadcastReceiver, intentFilter: IntentFilter, listenToBroadcastsFromOtherApps: Boolean = false
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

fun Activity.getScreenWidth(): Int {
    return if (isSdkR()) {
        val windowMetrics = windowManager.currentWindowMetrics
        val insets = windowMetrics.windowInsets.getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())
        windowMetrics.bounds.width() - insets.left - insets.right
    } else {
        val displayMetrics = DisplayMetrics()
        @Suppress("DEPRECATION")
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        displayMetrics.widthPixels
    }
}

fun Activity.getScreenHeight(): Int {
    return if (isSdkR()) {
        val windowMetrics = windowManager.currentWindowMetrics
        val insets = windowMetrics.windowInsets.getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())
        windowMetrics.bounds.height() - insets.top - insets.bottom
    } else {
        val displayMetrics = DisplayMetrics()
        @Suppress("DEPRECATION")
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        displayMetrics.heightPixels
    }
}

fun Activity.openWithSlide() {
    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
}

/**
 * call when finish activity to apply animation
 *
 * */
fun Activity.finishWithSlide() {
    finish()
    overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
}