package com.example.baseproject.base.utils.extension

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment

//region Permission Constants

const val PERMISSION_GRANTED = PackageManager.PERMISSION_GRANTED

// Storage permissions
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
const val READ_MEDIA_IMAGE = android.Manifest.permission.READ_MEDIA_IMAGES

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
const val READ_MEDIA_AUDIO = android.Manifest.permission.READ_MEDIA_AUDIO

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
const val READ_MEDIA_VIDEO = android.Manifest.permission.READ_MEDIA_VIDEO

const val READ_EXTERNAL_STORAGE = android.Manifest.permission.READ_EXTERNAL_STORAGE
const val WRITE_EXTERNAL_STORAGE = android.Manifest.permission.WRITE_EXTERNAL_STORAGE

// Notification permission
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
const val POST_NOTIFICATION = android.Manifest.permission.POST_NOTIFICATIONS

// Location permissions
const val ACCESS_COARSE_LOCATION = android.Manifest.permission.ACCESS_COARSE_LOCATION
const val ACCESS_FINE_LOCATION = android.Manifest.permission.ACCESS_FINE_LOCATION

// Camera permission
const val CAMERA_PERMISSION = android.Manifest.permission.CAMERA

//endregion

//region Storage Permission Extensions

/**
 * Check if app has read permission below Android Q
 */
fun Context.hasReadPermissionBelowQ(): Boolean = checkPermission(READ_EXTERNAL_STORAGE)

/**
 * Check if Fragment has read permission below Android Q
 */
fun Fragment.hasReadPermissionBelowQ(): Boolean = requireContext().hasReadPermissionBelowQ()

//endregion

//region Camera Permission Extensions

/**
 * Check if app has camera permission
 */
fun Context.hasCameraPermission(): Boolean = checkPermission(CAMERA_PERMISSION)

/**
 * Check if Fragment has camera permission
 */
fun Fragment.hasCameraPermission(): Boolean = requireContext().hasCameraPermission()

//endregion
