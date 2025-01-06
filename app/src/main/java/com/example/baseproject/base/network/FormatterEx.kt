package com.example.baseproject.base.network

import android.annotation.SuppressLint
import android.content.Context
import android.text.format.Formatter
import kotlin.math.log10
import kotlin.math.pow

// 1024 bytes -> "1.0 KB"
fun Context.size1() = Formatter.formatFileSize(this, 1024)

// 1500000 bytes -> "1.5 MB"
fun Context.size2() = Formatter.formatFileSize(this, 1500000)

// 2048576 bytes -> "2.0 MB"
fun Context.size3() = Formatter.formatFileSize(this, 2048576)

@SuppressLint("DefaultLocale")
fun formatFileSize(bytes: Long): String {
    if (bytes <= 0) return "0 B"

    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    val digitGroups = (log10(bytes.toDouble()) / log10(1024.0)).toInt()

    return String.format(
        "%.1f %s",
        bytes / 1024.0.pow(digitGroups.toDouble()),
        units[digitGroups]
    )
}