package com.example.baseproject.base.utils.extension

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import java.util.Locale

//region Language Management

/**
 * Set ngôn ngữ cho app
 * @param code mã ngôn ngữ (e.g., "en", "vi", "zh")
 */
fun setLanguageApp(code: String) {
    val localeList = LocaleListCompat.forLanguageTags(code)
    AppCompatDelegate.setApplicationLocales(localeList)
}

/**
 * Lấy ngôn ngữ hiện tại của app
 * @return mã ngôn ngữ hiện tại hoặc ngôn ngữ mặc định của device
 */
fun getApplicationLocales(): String =
    AppCompatDelegate.getApplicationLocales().toLanguageTags()
        .ifEmpty { Locale.getDefault().language }

//endregion
