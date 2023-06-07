package com.example.baseproject.base.utils

import android.content.Context
import android.content.SharedPreferences

object SharePrefUtils {
    private const val PREF_NAME = "AppName"
    lateinit var sharePref: SharedPreferences

    //TODO: please call it in splash or MyApplication before using below other function
    fun init(context: Context) {
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

    fun getString(key: String): String {
        return sharePref.getString(key, "")?.trim() ?: ""
    }

    fun getInt(key: String): Int {
        return sharePref.getInt(key, 0)
    }

    fun getBoolean(key: String): Boolean {
        return sharePref.getBoolean(key, false)
    }

    fun getLong(key: String): Long {
        return sharePref.getLong(key, 0L)
    }

    fun getFloat(key: String): Float {
        return sharePref.getFloat(key, 0f)
    }
}