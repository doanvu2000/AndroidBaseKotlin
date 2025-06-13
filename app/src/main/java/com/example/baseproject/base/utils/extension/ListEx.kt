package com.example.baseproject.base.utils.extension

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

suspend inline fun <T> List<T>.sortInIO(comparator: Comparator<in T>): List<T> = withContext(Dispatchers.IO) {
    this@sortInIO.sortedWith(comparator)
}

fun <T> List<T>.randomNotRepeat(defaultValue: T): T {
    if (this.isEmpty()) return defaultValue

    if (this.all { it == defaultValue }) return defaultValue

    var randomValue = this.random()
    while (randomValue == defaultValue) {
        randomValue = this.random()
    }

    return randomValue
}

fun <T> List<T>.getItemSafe(position: Int, defaultValue: T = first()): T {
    if (this.isEmpty()) return defaultValue

    return this.getOrNull(position) ?: defaultValue
}

/**
 * Parses a list of image URLs into pairs of base and "a" variant URLs.
 *
 * This function assumes URLs follow a specific naming convention:
 * - `_(\d+)` captures a number (e.g., "_123").
 * - `([a-z]?)` optionally captures a lowercase letter (e.g., "a").
 * - `\.[^/]+$` matches the file extension.
 *
 * It groups URLs by the captured number, then pairs the base URL (no letter)
 * with the "a" variant URL. The resulting pairs are sorted by the number.
 *
 * @return A list of `Pair<String, String>` where the first element is the base URL
 *         and the second is the "a" variant URL.
 * @sample:
 * ```
 * val urls = listOf(
 *         "https://this-that.kuemiin.com/Girl%20test/baddie_vs_clean/baddie_vs_clean_1.webp",
 *         "https://this-that.kuemiin.com/Girl%20test/baddie_vs_clean/baddie_vs_clean_10.webp",
 *         "https://this-that.kuemiin.com/Girl%20test/baddie_vs_clean/baddie_vs_clean_10a.webp",
 *         "https://this-that.kuemiin.com/Girl%20test/baddie_vs_clean/baddie_vs_clean_1a.webp",
 *         "https://this-that.kuemiin.com/Girl%20test/baddie_vs_clean/baddie_vs_clean_2.webp",
 *         "https://this-that.kuemiin.com/Girl%20test/baddie_vs_clean/baddie_vs_clean_2a.webp",
 *         "https://this-that.kuemiin.com/Girl%20test/baddie_vs_clean/baddie_vs_clean_3.webp",
 *         "https://this-that.kuemiin.com/Girl%20test/baddie_vs_clean/baddie_vs_clean_3a.webp")
 * val pairs = urls.parseToPairImage()
 * Pair: ...baddie_vs_clean_1.webp  <-->  ...baddie_vs_clean_1a.webp
 * Pair: ...baddie_vs_clean_2.webp  <-->  ...baddie_vs_clean_2a.webp
 * Pair: ...baddie_vs_clean_3.webp  <-->  ...baddie_vs_clean_3a.webp
 * ```
 */
fun List<String>.parseToPairImage(): List<Pair<String, String>> {
    val regex = Regex("""_(\d+)([a-z]?)\.[^/]+$""")
    return this.groupBy { url ->
        val match = regex.find(url)
        match?.groupValues?.get(1) ?: "__no_number__"
    }.mapNotNull { (_, listForNumber) ->
        val baseUrl = listForNumber.find { regex.find(it)!!.groupValues[2].isEmpty() }
        val aUrl = listForNumber.find { regex.find(it)!!.groupValues[2] == "a" }
        if (baseUrl != null && aUrl != null) {
            baseUrl to aUrl
        } else {
            null
        }
    }.sortedBy { (url, _) ->
        val num = regex.find(url)!!.groupValues[1].toInt()
        num
    }
}