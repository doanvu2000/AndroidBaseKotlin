package com.base.stickerview

import android.content.Context
import android.widget.TextView
import androidx.annotation.FontRes
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import java.lang.reflect.Field


fun Context.getColorById(colorSource: Int): Int {
    return ContextCompat.getColor(this, colorSource)
}

fun TextView.setFont(@FontRes fontId: Int) {
    val font = ResourcesCompat.getFont(context, fontId)
    this.typeface = font
}

fun getResId(resName: String, cls: Class<*>): Int {
    try {
        val idField: Field = cls.getDeclaredField(resName)
        return idField.getInt(idField)
    } catch (e: Exception) {
        e.printStackTrace()
        return -1
    }
}