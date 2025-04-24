package com.example.baseproject.base.utils.util

import android.util.Log
import com.example.baseproject.base.utils.extension.isDebugMode

object AppLogger {
    fun i(tag: String, msg: String) {
        if (checkBeforeLog(tag)) return
        Log.i(tag, msg)
    }

    fun d(tag: String, msg: String) {
        if (checkBeforeLog(tag)) return
        Log.d(tag, msg)
    }

    fun w(tag: String, msg: String) {
        if (checkBeforeLog(tag)) return
        Log.w(tag, msg)
    }

    fun e(tag: String, msg: String) {
        if (checkBeforeLog(tag)) return
        Log.e(tag, msg)
    }

    private fun checkBeforeLog(tag: String): Boolean {
        if (!isDebugMode()) {
            return true
        }
        if (tag.length > 22) {
            return true
        }
        return false
    }
}