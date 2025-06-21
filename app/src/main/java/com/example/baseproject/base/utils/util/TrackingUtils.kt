package com.example.baseproject.base.utils.util

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.example.baseproject.base.utils.extension.isDebugMode
import com.example.baseproject.base.utils.extension.tryCatch
import com.google.firebase.analytics.FirebaseAnalytics

object TrackingUtils {
    const val TAG = Constants.TAG + "-Tracking"
    fun logFirebaseEvent(context: Context, rawEvent: String) {
        val cleanedEvent = rawEvent.trim().lowercase().replace(Regex("[^a-z0-9_]+"), "_").take(40)

        AppLogger.d(TAG, "event = $cleanedEvent")

        if (isDebugMode()) {
            return
        }

        if (cleanedEvent.startsWith("firebase_") || cleanedEvent.startsWith("google_")) {
            AppLogger.e(TAG, "Event name must not start with reserved prefixes.")
            return
        }

        val bundle = Bundle().apply {
            putString(cleanedEvent, cleanedEvent)
        }

        FirebaseAnalytics.getInstance(context).logEvent(cleanedEvent, bundle)
    }
}

fun Context.logEventFirebase(event: String) {
    tryCatch(tryBlock = {
        TrackingUtils.logFirebaseEvent(this, event)
    })
}

fun Fragment.logEventFirebase(event: String) {
    requireContext().logEventFirebase(event)
}