package com.example.baseproject.base.utils.extension

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.example.baseproject.R

//region Social Media Constants

const val FACEBOOK = "com.facebook.katana"
const val MESSENGER = "com.facebook.orca"
const val INSTAGRAM = "com.instagram.android"
const val WHATSAPP = "com.whatsapp"
const val TIKTOK = "com.ss.android.ugc.trill"

//endregion

//region Error Messages

const val FACEBOOK_NOT_INSTALLED = "Facebook have not been installed..."
const val MESSENGER_NOT_INSTALLED = "Messenger have not been installed..."
const val INSTAGRAM_NOT_INSTALLED = "Instagram have not been installed..."
const val WHATSAPP_NOT_INSTALLED = "WhatsApp have not been installed..."
const val TIKTOK_NOT_INSTALLED = "Tiktok have not been installed..."

//endregion

//region Utility Functions

/**
 * Get MIME type based on current image type setting
 * If file is video or other, change type to video/* or */*
 */
fun type(): String = when (typeImage) {
    TypeImage.JPEG -> "image/jpeg"
    TypeImage.PNG -> "image/png"
}

//endregion

//region General Sharing

/**
 * Share image to specific app or show chooser
 * @param uriForFile URI of the file to share
 * @param packageAppShare package name of target app (null for chooser)
 */
fun Context.shareImage(uriForFile: Uri, packageAppShare: String?) {
    try {
        val intent = Intent(Intent.ACTION_SEND).apply {
            action = Intent.ACTION_SEND
            setDataAndType(uriForFile, type())
            packageAppShare?.let { setPackage(it) }
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            putExtra(Intent.EXTRA_STREAM, uriForFile)
            putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name))
        }

        Log.e("URI_FILE", "share: $uriForFile")
        startActivity(Intent.createChooser(intent, "Select"))
    } catch (e: Exception) {
        e.printStackTrace()
        packageAppShare?.let {
            val errorMessage = when (it) {
                WHATSAPP -> WHATSAPP_NOT_INSTALLED
                INSTAGRAM -> INSTAGRAM_NOT_INSTALLED
                FACEBOOK -> FACEBOOK_NOT_INSTALLED
                MESSENGER -> MESSENGER_NOT_INSTALLED
                TIKTOK -> TIKTOK_NOT_INSTALLED
                else -> "Error sharing image"
            }
            showToast(errorMessage)
        }
    }
}

//endregion

//region Specific App Sharing

/**
 * Share image to Facebook
 * @param uri URI of the image to share
 */
fun Context.shareImageToFacebook(uri: Uri) {
    shareImage(uri, FACEBOOK)
}

/**
 * Share image to WhatsApp
 * @param uri URI of the image to share
 */
fun Context.shareImageToWhatsApp(uri: Uri) {
    shareImage(uri, WHATSAPP)
}

/**
 * Share image to Instagram
 * @param uri URI of the image to share
 */
fun Context.shareImageToInstagram(uri: Uri) {
    shareImage(uri, INSTAGRAM)
}

/**
 * Share image to Messenger
 * @param uri URI of the image to share
 */
fun Context.shareImageToMessenger(uri: Uri) {
    shareImage(uri, MESSENGER)
}

/**
 * Share image to TikTok
 * @param uri URI of the image to share
 */
fun Context.shareImageToTiktok(uri: Uri) {
    shareImage(uri, TIKTOK)
}

//endregion
