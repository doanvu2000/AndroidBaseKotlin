package com.example.baseproject.base.utils.extension

import android.icu.text.Transliterator
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

//region String Formatting

/**
 * Uppercase chữ cái đầu tiên của string
 */
fun String.upperFirstCase(): String {
    if (this.isEmpty()) return this
    if (this.length == 1) return this.uppercase()
    return this.first().toString().uppercase() + this.substring(1)
}

/**
 * Capitalize từng từ trong string
 * "JIN VU di hOC" => "Jin Vu Di Hoc"
 */
fun String.capitalizeWord(): String {
    if (this.isEmpty()) return this
    return this.lowercase().split(" ")
        .joinToString(separator = " ") { str ->
            str.replaceFirstChar { char -> char.titlecase() }
        }
}

/**
 * Capitalize từng từ trong string (version 2)
 */
fun String.capitalizeWordV2(): String {
    if (this.isEmpty()) return this
    return this.lowercase().split(" ")
        .joinToString(separator = " ", transform = String::upperFirstCase)
}

//endregion

//region String Validation

/**
 * Contains ignore case
 */
fun String?.containsIgnoreCase(regex: String): Boolean {
    return this?.contains(regex, true) == true
}

/**
 * Kiểm tra string chỉ chứa số
 */
fun String.isDigitOnly(): Boolean = matches(Regex("^\\d*\$"))

/**
 * Kiểm tra string chỉ chứa chữ cái
 */
fun String.isAlphabeticOnly(): Boolean = matches(Regex("^[a-zA-Z]*\$"))

/**
 * Kiểm tra string chỉ chứa chữ cái và số
 */
fun String.isAlphanumericOnly(): Boolean = matches(Regex("^[a-zA-Z\\d]*\$"))

/**
 * Kiểm tra có phải link GIF không
 */
fun String.isLinkGif(): Boolean = this.takeLast(4).contains(".gif")

//endregion

//region Security & Encryption

/**
 * Encrypt string thành Long bằng SHA-256
 */
fun String.encryptStringToLong(): Long {
    val digest = MessageDigest.getInstance("SHA-256")
    val hashBytes = digest.digest(this.toByteArray(StandardCharsets.UTF_8))

    var result: Long = 0
    for (i in 0 until 8) {
        result = (result shl 8) or (hashBytes[i].toLong() and 0xff)
    }
    return result
}

//endregion

//region Character Width Conversion

/**
 * Convert Japanese string half-width to full-width
 */
fun String.toFullWidth(): String {
    val builder = StringBuilder()
    for (c in this.toCharArray()) {
        builder.append((c.code + 65248).toChar())
    }
    return builder.toString()
}

/**
 * Convert Japanese string full-width to half-width
 */
fun String.toHalfWidth(): String {
    val builder = StringBuilder()
    for (c in this.toCharArray()) {
        builder.append((c.code - 65248).toChar())
    }
    return builder.toString()
}

/**
 * Convert to full-width với Transliterator (API Q+)
 */
fun String.convertToFullWidth(): String {
    return if (isSdkQ()) {
        Transliterator.getInstance("Fullwidth-Halfwidth").transliterate(this)
    } else {
        toFullWidth()
    }
}

/**
 * Convert to half-width với Transliterator (API Q+)
 */
fun String.convertToHalfWidth(): String {
    return if (isSdkQ()) {
        Transliterator.getInstance("Halfwidth-Fullwidth").transliterate(this)
    } else {
        toHalfWidth()
    }
}

//endregion

//region Date Conversion

/**
 * Convert string to Date với format tùy chỉnh
 */
fun String.toDate(format: String = "yyyy-MM-dd HH:mm:ss"): Date? {
    val dateFormatter = SimpleDateFormat(format, Locale.getDefault())
    return dateFormatter.parse(this)
}

/**
 * Format Date thành string
 */
fun Date.toStringFormat(format: String = "yyyy-MM-dd HH:mm:ss"): String {
    val dateFormatter = SimpleDateFormat(format, Locale.getDefault())
    return dateFormatter.format(this)
}

//endregion

//region String Utilities

/**
 * Substring an toàn với default value
 */
fun String.subStringSafety(startIndex: Int, endIndex: Int, default: String): String {
    if (startIndex > endIndex) return default
    if (length < startIndex || length < endIndex) return default
    return substring(startIndex, endIndex)
}

/**
 * Lấy extension của file
 */
fun String.getExtensionFile(default: String): String {
    return try {
        this.substring(this.lastIndexOf('.') + 1, this.length)
    } catch (e: Exception) {
        default
    }
}

//endregion

//region Pretty Print & Debug

/**
 * Pretty print object với format đẹp
 */
fun Any.prettyPrint(): String {
    var indentLevel = 0
    val indentWidth = 4

    fun padding() = "".padStart(indentLevel * indentWidth)

    val toString = toString()
    val stringBuilder = StringBuilder(toString.length)

    var i = 0
    while (i < toString.length) {
        when (val char = toString[i]) {
            '(', '[', '{' -> {
                indentLevel++
                stringBuilder.appendLine(char).append(padding())
            }

            ')', ']', '}' -> {
                indentLevel--
                stringBuilder.appendLine().append(padding()).append(char)
            }

            ',' -> {
                stringBuilder.appendLine(char).append(padding())
                val nextChar = toString.getOrElse(i + 1) { char }
                if (nextChar == ' ') i++
            }

            else -> {
                stringBuilder.append(char)
            }
        }
        i++
    }
    return stringBuilder.toString()
}

/**
 * Pretty print với Kotlin-style format
 */
fun Any?.toPrettyString2(): String {
    if (this == null) return "(null)"

    var indentLevel = 0
    val indentWidth = 2

    fun padding() = "".padStart(indentLevel * indentWidth)

    val toString = toString()
    val stringBuilder = StringBuilder(toString.length)

    var nestingContext: Char? = null
    var i = 0

    while (i < toString.length) {
        when (val char = toString[i]) {
            '(', '[', '{' -> {
                indentLevel++
                nestingContext = char
                stringBuilder.appendLine(
                    when (char) {
                        '[' -> "listOf("
                        '{' -> "mapOf("
                        else -> '('
                    }
                ).append(padding())
            }

            ')', ']', '}' -> {
                indentLevel--
                nestingContext = null
                stringBuilder.appendLine().append(padding()).append(')')
            }

            ',' -> {
                stringBuilder.appendLine(char).append(padding())
                val nextChar = toString.getOrElse(i + 1) { char }
                if (nextChar == ' ') i++
            }

            '=' -> {
                when (nestingContext) {
                    '{' -> stringBuilder.append(" to ")
                    else -> stringBuilder.append(" = ")
                }
            }

            else -> {
                stringBuilder.append(char)
            }
        }
        i++
    }
    return stringBuilder.toString()
}

/**
 * Pretty print function (same as prettyPrint)
 */
fun Any.pretty() = toString().let { toString ->
    var indentLevel = 0
    val indentWidth = 4
    fun padding() = "".padStart(indentLevel * indentWidth)
    buildString {
        var ignoreSpace = false
        toString.onEach { char ->
            when (char) {
                '(', '[', '{' -> {
                    indentLevel++
                    appendLine(char)
                    append(padding())
                }

                ')', ']', '}' -> {
                    indentLevel--
                    appendLine()
                    append(padding())
                    append(char)
                }

                ',' -> {
                    appendLine(char)
                    append(padding())
                    ignoreSpace = true
                }

                ' ' -> {
                    if (!ignoreSpace) append(char)
                    ignoreSpace = false
                }

                else -> append(char)
            }
        }
    }
}

/**
 * Debug string với indent tùy chỉnh
 */
fun Any.toPrettyDebugString(indentWidth: Int = 4) = buildString {
    fun StringBuilder.indent(level: Int) = append("".padStart(level * indentWidth))
    var ignoreSpace = false
    var indentLevel = 0
    this@toPrettyDebugString.toString().onEach {
        when (it) {
            '(', '[', '{' -> appendLine(it).indent(++indentLevel)
            ')', ']', '}' -> appendLine().indent(--indentLevel).append(it)
            ',' -> appendLine(it).indent(indentLevel).also { ignoreSpace = true }
            ' ' -> if (ignoreSpace) ignoreSpace = false else append(it)
            '=' -> append(" = ")
            else -> append(it)
        }
    }
}

//endregion
