package com.example.baseproject.base.utils.extension

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.example.baseproject.R

const val FACEBOOK = "com.facebook.katana"
const val MESSENGER = "com.facebook.orca"
const val INSTAGRAM = "com.instagram.android"
const val WHATSAPP = "com.whatsapp"
const val TIKTOK = "com.ss.android.ugc.trill"

const val FACEBOOK_NOT_INSTALLED = "Facebook have not been installed..."
const val MESSENGER_NOT_INSTALLED = "Messenger have not been installed..."
const val INSTAGRAM_NOT_INSTALLED = "Instagram have not been installed..."
const val WHATSAPP_NOT_INSTALLED = "WhatsApp have not been installed..."
const val TIKTOK_NOT_INSTALLED = "Tiktok have not been installed..."

/**
 * If file is video or other, change type to video/* or */*
 * */
fun type() = when (typeImage) {
    TypeImage.JPEG -> {
        "image/jpeg"
    }

    TypeImage.PNG -> {
        "image/png"
    }
}

fun Context.shareImage(uriForFile: Uri, packageAppShare: String?) {
    try {
        val intent2 = Intent(Intent.ACTION_SEND)
        val sb3 = StringBuilder()
        sb3.append("share: ")
        sb3.append(uriForFile.toString())
        Log.e("URI_FILE", sb3.toString())
        intent2.setAction("android.intent.action.SEND")
        intent2.setDataAndType(uriForFile, type())
        if (packageAppShare != null) {
            intent2.setPackage(packageAppShare)
        }
        intent2.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent2.putExtra(Intent.EXTRA_STREAM, uriForFile)
        intent2.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name))
        startActivity(Intent.createChooser(intent2, "Select"))
    } catch (e: Exception) {
        e.printStackTrace()
        if (packageAppShare != null) {
            val str3 = when (packageAppShare) {
                WHATSAPP -> {
                    WHATSAPP_NOT_INSTALLED
                }

                INSTAGRAM -> {
                    INSTAGRAM_NOT_INSTALLED
                }

                FACEBOOK -> {
                    FACEBOOK_NOT_INSTALLED
                }

                MESSENGER -> {
                    MESSENGER_NOT_INSTALLED
                }

                TIKTOK -> {
                    TIKTOK_NOT_INSTALLED
                }

                else -> "error share image"
            }
            showToast(str3)
        }
    }
}

fun Context.shareImageToFacebook(uri: Uri) {
    shareImage(uri, FACEBOOK)
}

fun Context.shareImageToWhatsApp(uri: Uri) {
    shareImage(uri, WHATSAPP)
}

fun Context.shareImageToInstagram(uri: Uri) {
    shareImage(uri, INSTAGRAM)
}

fun Context.shareImageToMessenger(uri: Uri) {
    shareImage(uri, MESSENGER)
}

fun Context.shareImageToTiktok(uri: Uri) {
    shareImage(uri, TIKTOK)
}