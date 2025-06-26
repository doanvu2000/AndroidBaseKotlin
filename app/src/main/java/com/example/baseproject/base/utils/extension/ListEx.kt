package com.example.baseproject.base.utils.extension

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

//region List Sorting

/**
 * Sort list trong IO dispatcher để tránh block UI thread
 */
suspend inline fun <T> List<T>.sortInIO(comparator: Comparator<in T>): List<T> =
    withContext(Dispatchers.IO) {
        this@sortInIO.sortedWith(comparator)
    }

//endregion

//region List Utilities

/**
 * Lấy random item không trùng với default value
 * @param defaultValue giá trị default để tránh trùng lặp
 * @return random item khác với defaultValue, hoặc defaultValue nếu không có choice khác
 */
fun <T> List<T>.randomNotRepeat(defaultValue: T): T {
    if (this.isEmpty()) return defaultValue

    if (this.all { it == defaultValue }) return defaultValue

    var randomValue = this.random()
    while (randomValue == defaultValue) {
        randomValue = this.random()
    }

    return randomValue
}

/**
 * Lấy item tại position một cách an toàn
 * @param position vị trí cần lấy
 * @param defaultValue giá trị default nếu position không hợp lệ
 * @return item tại position hoặc defaultValue
 */
fun <T> List<T>.getItemSafe(position: Int, defaultValue: T = first()): T {
    if (this.isEmpty()) return defaultValue
    return this.getOrNull(position) ?: defaultValue
}

//endregion

//region Image URL Parsing

/**
 * Parse danh sách image URLs thành pairs của base và "a" variant URLs.
 *
 * Function này giả định URLs theo naming convention:
 * - `_(\d+)` captures một số (e.g., "_123").
 * - `([a-z]?)` optionally captures một chữ cái thường (e.g., "a").
 * - `\.[^/]+$` matches file extension.
 *
 * Nó group URLs theo số captured, sau đó pair base URL (không có chữ cái)
 * với "a" variant URL. Kết quả được sort theo số.
 *
 * @return List của `Pair<String, String>` với element đầu là base URL
 *         và element thứ hai là "a" variant URL.
 * @sample:
 * ```
 * val urls = listOf(
 *     "https://this-that.kuemiin.com/Girl%20test/baddie_vs_clean/baddie_vs_clean_1.webp",
 *     "https://this-that.kuemiin.com/Girl%20test/baddie_vs_clean/baddie_vs_clean_10.webp",
 *     "https://this-that.kuemiin.com/Girl%20test/baddie_vs_clean/baddie_vs_clean_10a.webp",
 *     "https://this-that.kuemiin.com/Girl%20test/baddie_vs_clean/baddie_vs_clean_1a.webp",
 *     "https://this-that.kuemiin.com/Girl%20test/baddie_vs_clean/baddie_vs_clean_2.webp",
 *     "https://this-that.kuemiin.com/Girl%20test/baddie_vs_clean/baddie_vs_clean_2a.webp",
 *     "https://this-that.kuemiin.com/Girl%20test/baddie_vs_clean/baddie_vs_clean_3.webp",
 *     "https://this-that.kuemiin.com/Girl%20test/baddie_vs_clean/baddie_vs_clean_3a.webp"
 * )
 * val pairs = urls.parseToPairImage()
 * // Kết quả:
 * // Pair: ...baddie_vs_clean_1.webp  <-->  ...baddie_vs_clean_1a.webp
 * // Pair: ...baddie_vs_clean_2.webp  <-->  ...baddie_vs_clean_2a.webp
 * // Pair: ...baddie_vs_clean_3.webp  <-->  ...baddie_vs_clean_3a.webp
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

//endregion
