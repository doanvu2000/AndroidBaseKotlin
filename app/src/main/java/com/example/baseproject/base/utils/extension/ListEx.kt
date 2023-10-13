package com.example.baseproject.base.utils.extension

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

suspend inline fun <T> List<T>.sortInIO(comparator: Comparator<in T>): List<T> = withContext(Dispatchers.IO) {
    this@sortInIO.sortedWith(comparator)
}