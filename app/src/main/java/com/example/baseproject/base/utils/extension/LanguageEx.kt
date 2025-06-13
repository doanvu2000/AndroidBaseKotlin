package com.example.baseproject.base.utils.extension

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import java.util.Locale

fun setLanguageApp(code: String) {
    val localeList = LocaleListCompat.forLanguageTags(code)
    AppCompatDelegate.setApplicationLocales(localeList)
}

fun getApplicationLocales(): String =
    AppCompatDelegate.getApplicationLocales().toLanguageTags()
        .ifEmpty { Locale.getDefault().language }