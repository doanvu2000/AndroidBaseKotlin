package com.example.baseproject.base.utils.util

import android.content.Context
import android.content.SharedPreferences

object SharePrefUtils {
    private const val PREF_NAME = "AppName"
    lateinit var sharePref: SharedPreferences

    //TODO: please call it in splash or MyApplication before using below other function
    fun init(context: Context) {
        if (::sharePref.isInitialized) {
            return
        }
        sharePref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun <T> saveKey(key: String, value: T) {
        when (value) {
            is String -> sharePref.edit().putString(key, value).apply()
            is Int -> sharePref.edit().putInt(key, value).apply()
            is Boolean -> sharePref.edit().putBoolean(key, value).apply()
            is Long -> sharePref.edit().putLong(key, value).apply()
            is Float -> sharePref.edit().putFloat(key, value).apply()
        }

    }

    fun getString(key: String, defaultValue: String = ""): String {
        if (!::sharePref.isInitialized) {
            return defaultValue
        }
        return sharePref.getString(key, "")?.trim() ?: ""
    }

    fun getInt(key: String, defaultValue: Int = 0): Int {
        if (!::sharePref.isInitialized) {
            return defaultValue
        }
        return sharePref.getInt(key, defaultValue)
    }

    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean {
        if (!::sharePref.isInitialized) {
            return defaultValue
        }
        return sharePref.getBoolean(key, defaultValue)
    }

    fun getLong(key: String, defaultValue: Long = 0L): Long {
        if (!::sharePref.isInitialized) {
            return defaultValue
        }
        return sharePref.getLong(key, defaultValue)
    }

    fun getFloat(key: String, defaultValue: Float = 0f): Float {
        if (!::sharePref.isInitialized) {
            return defaultValue
        }
        return sharePref.getFloat(key, defaultValue)
    }

    fun isFistOpen() = getBoolean("is_first_open", true)
    fun setIsFirstOpen(isFirstOpen: Boolean) = saveKey("is_first_open", isFirstOpen)
}