package com.example.baseproject.base.utils.util

import android.util.Log
import com.example.baseproject.base.utils.extension.isDebugMode

/**
 * Utility object for app-wide logging
 * Chỉ log khi app ở debug mode và tag hợp lệ
 */
object AppLogger {

    //region Constants

    private const val MAX_TAG_LENGTH = 22

    //endregion

    //region Logging Methods

    /**
     * Log info message
     * @param tag log tag
     * @param msg message to log
     */
    fun i(tag: String, msg: String) {
        if (isDisableLog(tag)) return
        Log.i(tag, msg)
    }

    /**
     * Log debug message
     * @param tag log tag
     * @param msg message to log
     */
    fun d(tag: String, msg: String) {
        if (isDisableLog(tag)) return
        Log.d(tag, msg)
    }

    /**
     * Log warning message
     * @param tag log tag
     * @param msg message to log
     */
    fun w(tag: String, msg: String) {
        if (isDisableLog(tag)) return
        Log.w(tag, msg)
    }

    /**
     * Log error message
     * @param tag log tag
     * @param msg message to log
     */
    fun e(tag: String, msg: String) {
        if (isDisableLog(tag)) return
        Log.e(tag, msg)
    }

    //endregion

    //region Validation

    /**
     * Check if logging should be performed
     * @param tag log tag to validate
     * @return true if logging should be skipped, false otherwise
     */
    private fun isDisableLog(tag: String): Boolean {
        // Không log khi không phải debug mode
        if (!isDebugMode()) {
            return true
        }

        // Không log khi tag quá dài (Android limitation)
        if (tag.length > MAX_TAG_LENGTH) {
            return true
        }

        return false
    }

    //endregion
}