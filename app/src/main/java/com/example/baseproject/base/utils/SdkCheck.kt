package com.example.baseproject.base.utils

import android.os.Build

/** SDK 23 - M*/
fun isSdk23() = isSdkM()
fun isSdkM() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M

/** SDK 24 - N*/
fun isSdk24() = isSdkN()
fun isSdkN() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N

/** SDK 26 - O*/
fun isSdk26() = isSdkO()
fun isSdkO() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O

/** SDK 29 - Q*/
fun isSdk29() = isSdkQ()
fun isSdkQ() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

/** SDK 30 - R*/
fun isSdk30() = isSdkR()
fun isSdkR() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R

/** SDK 31 - S*/
fun isSdk31() = isSdkS()
fun isSdkS() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

/** SDK 33 - TIRAMISU*/
fun isSdk33() = isSdkTIRAMISU()
fun isSdkTIRAMISU() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU