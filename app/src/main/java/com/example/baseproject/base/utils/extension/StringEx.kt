package com.example.baseproject.base.utils.extension

import android.icu.text.Transliterator
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


fun String.upperFirstCase(): String {
    if (this.isEmpty()) {
        return this
    }
    if (this.length == 1) {
        return this.uppercase()
    }
    val firstCase = this.first().toString().uppercase()
    return firstCase + this.substring(1)
}

fun String?.containsIgnoreCase(regex: String): Boolean {
    return this?.contains(regex, true) == true
}

fun String.encryptStringToLong(): Long {
    val digest = MessageDigest.getInstance("SHA-256")
    val hashBytes = digest.digest(this.toByteArray(StandardCharsets.UTF_8))

    // Convert the hash bytes to a long value
    var result: Long = 0
    for (i in 0 until 8) {
        result = (result shl 8) or (hashBytes[i].toLong() and 0xff)
    }

    return result
}

//"JIN VU di hOC" => "Jin Vu Di Hoc"
fun String.capitalizeWord(): String {
    if (this.isEmpty()) {
        return this
    }
    return this.lowercase().split(" ")
        .joinToString(separator = " ", transform = { str ->
            return@joinToString str.replaceFirstChar { char ->
                char.titlecase()
            }
        })
}

fun String.capitalizeWordV2(): String {
    if (this.isEmpty()) {
        return this
    }
    return this.lowercase().split(" ")
        .joinToString(separator = " ", transform = String::upperFirstCase)
}

//convert Japanese string half-width to full-width
fun String.toFullWidth(): String {
    val builder = StringBuilder()
    for (c in this.toCharArray()) {
        builder.append((c.code + 65248).toChar())
    }
    return builder.toString()
}

fun String.toHalfWidth(): String {
    val builder = StringBuilder()
    for (c in this.toCharArray()) {
        builder.append((c.code - 65248).toChar())
    }
    return builder.toString()
}

fun String.convertToFullWidth(): String {
    if (isSdkQ()) {
        return Transliterator.getInstance("Fullwidth-Halfwidth").transliterate(this)
    }
    return toFullWidth()
}

fun String.convertToHalfWidth(): String {
    if (isSdkQ()) {
        return Transliterator.getInstance("Halfwidth-Fullwidth").transliterate(this)
    }
    return toHalfWidth()
}

fun String.isLinkGif(): Boolean = this.takeLast(4).contains(".gif")
fun String.isDigitOnly(): Boolean = matches(Regex("^\\d*\$"))

fun String.isAlphabeticOnly(): Boolean = matches(Regex("^[a-zA-Z]*\$"))

fun String.isAlphanumericOnly(): Boolean = matches(Regex("^[a-zA-Z\\d]*\$"))

val isValidNumber = "1234".isDigitOnly() // Return true
val isValid = "1234abc".isDigitOnly() // Return false
val isOnlyAlphabetic = "abcABC".isAlphabeticOnly() // Return true
val isOnlyAlphabetic2 = "abcABC123".isAlphabeticOnly() // Return false
val isOnlyAlphanumeric = "abcABC123".isAlphanumericOnly() // Return true
val isOnlyAlphanumeric2 = "abcABC@123.".isAlphanumericOnly() // Return false


fun String.toDate(format: String = "yyyy-MM-dd HH:mm:ss"): Date? {
    val dateFormatter = SimpleDateFormat(format, Locale.getDefault())
    return dateFormatter.parse(this)
}

fun Date.toStringFormat(format: String = "yyyy-MM-dd HH:mm:ss"): String {
    val dateFormatter = SimpleDateFormat(format, Locale.getDefault())
    return dateFormatter.format(this)
}
fun String.subStringSafety(startIndex: Int, endIndex: Int, default: String): String {
    if (startIndex > endIndex) {
        return default
    }
    if (length < startIndex || length < endIndex) {
        return default
    }
    return substring(startIndex, endIndex)
}