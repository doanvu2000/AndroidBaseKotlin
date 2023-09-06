package com.example.baseproject.base.utils.extension

import android.content.Intent
import android.media.audiofx.AudioEffect
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import java.util.concurrent.Executor

fun Fragment.checkPermission(permission: String): Boolean {
    return requireContext().checkPermission(permission)
}

fun Fragment.hasReadStoragePermission() = requireContext().hasReadStoragePermission()
fun Fragment.getActivityResultLauncher(callBack: (Map<String, Boolean>) -> Unit): ActivityResultLauncher<Array<String>> {
    return registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        callBack.invoke(permissions)
    }
}

fun Fragment.requestPermissionReadStorage(permissionLauncher: ActivityResultLauncher<Array<String>>) {
    requireContext().requestPermissionReadStorage(permissionLauncher)
}

fun Fragment.findChildFragment(TAG: String): Fragment? {
    return childFragmentManager.findFragmentByTag(TAG)
}

fun Fragment.showToast(msg: String, isShowDurationLong: Boolean = false) {
    requireContext().showToast(msg, isShowDurationLong)
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

fun Fragment.hideKeyboard() {
    view?.let { activity?.hideKeyboard(it) }
}

fun Fragment.showSnackBar(msg: String, duration: Int = 500) {
    view?.let {
        val snackBar = Snackbar.make(it, msg, duration)
//            .setTextColor(getColorById(R.color.text_selected))
        val snackView = snackBar.view
//        snackView.setBackgroundColor(getColorById(R.color.color_app))
        snackBar.show()
    }
}