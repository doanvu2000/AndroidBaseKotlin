package com.example.baseproject.base.utils.extension

import android.os.Build

//region SDK Version Checks

/**
 * Check if device is running Android O (API 26) or higher
 */
fun isSdk26(): Boolean = isSdkO()

/**
 * Check if device is running Android O (API 26) or higher
 */
fun isSdkO(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O

/**
 * Check if device is running Android Q (API 29) or higher
 */
fun isSdk29(): Boolean = isSdkQ()

/**
 * Check if device is running Android Q (API 29) or higher
 */
fun isSdkQ(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

/**
 * Check if device is running Android R (API 30) or higher
 */
fun isSdk30(): Boolean = isSdkR()

/**
 * Check if device is running Android R (API 30) or higher
 */
fun isSdkR(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R

/**
 * Check if device is running Android S (API 31) or higher
 */
fun isSdk31(): Boolean = isSdkS()

/**
 * Check if device is running Android S (API 31) or higher
 */
fun isSdkS(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

/**
 * Check if device is running Android Tiramisu (API 33) or higher
 */
fun isSdk33(): Boolean = isSdkTIRAMISU()

/**
 * Check if device is running Android Tiramisu (API 33) or higher
 */
fun isSdkTIRAMISU(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

/**
 * Check if device is running Android Upside Down Cake (API 34) or higher
 */
fun isSdk34(): Boolean = isSdkUpSideDownCake()

/**
 * Check if device is running Android Upside Down Cake (API 34) or higher
 */
fun isSdkUpSideDownCake(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE

//endregion
