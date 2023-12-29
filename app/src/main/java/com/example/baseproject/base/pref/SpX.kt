package com.example.baseproject.base.pref

import android.os.Parcel
import android.os.Parcelable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.util.UUID
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

internal fun ByteArray.toCharArray(): CharArray {
    val charArray = CharArray(this.size)
    for (i in indices) {
        charArray[i] = get(i).toInt().toChar()
    }
    return charArray
}

internal fun CharArray.byteArray(): ByteArray {
    val bytes = ByteArray(this.size)
    for (i in indices) {
        bytes[i] = get(i).code.toByte()
    }
    return bytes
}

private fun ByteArray.toSerializedString(): String = String(toCharArray())
private fun String.deserialize(): ByteArray = toCharArray().byteArray()

@Suppress("UNCHECKED_CAST")
internal fun <T : java.io.Serializable> String.toSerializable(type: Class<T>): T? {
    return this.deserialize().toSerializable(type)
}

private fun java.io.Serializable.toByteArray(): ByteArray {
    val buffer = ByteArrayOutputStream()
    val oos = ObjectOutputStream(buffer)
    oos.writeObject(this)
    oos.close()
    return buffer.toByteArray()
}

internal fun java.io.Serializable.toSerializedString(): String {
    return this.toByteArray().toSerializedString()
}

@Suppress("UNCHECKED_CAST")
private fun <T : java.io.Serializable> ByteArray.toSerializable(type: Class<T>): T? {
    return try {
        ObjectInputStream(ByteArrayInputStream(this)).readObject() as T
    } catch (ex: Throwable) {
        ex.printStackTrace()
        null
    }
}

private fun Parcelable.toByteArray(): ByteArray {
    val parcel = Parcel.obtain()
    this.writeToParcel(parcel, 0)
    val bytes = parcel.marshall()
    parcel.recycle()
    return bytes
}

internal fun Parcelable.toSerializedString(): String {
    return this.toByteArray().toSerializedString()
}

internal fun <T : Parcelable> String.toParcelable(creator: Parcelable.Creator<T>): T =
    byteArrayToParcelable(toCharArray().byteArray(), creator)


internal fun <T : Parcelable> byteArrayToParcelable(bytes: ByteArray, creator: Parcelable.Creator<T>): T {
    val parcel = byteArrayToParcel(bytes)
    val data = creator.createFromParcel(parcel)
    parcel.recycle()
    return data
}

internal fun byteArrayToParcel(bytes: ByteArray): Parcel {
    val parcel = Parcel.obtain()
    parcel.unmarshall(bytes, 0, bytes.size)
    parcel.setDataPosition(0)
    return parcel
}

internal suspend fun <T : Any> runSuspended(task: () -> T?): T? {
    coroutineContext().let {
        return withContext(it) {
            return@withContext async(Dispatchers.IO) { task() }.await()
        }
    }
}

internal suspend fun coroutineContext(): CoroutineContext = suspendCoroutine { it.resume(it.context) }

private val STRING_TAG_SEPARATOR = "!@#$%!@#$%!@#$%"
internal fun String.addTag(tag: String? = null): String {
    val stringTag = when {
        tag != null -> tag
        else -> UUID.randomUUID().toString()
    }
    return "$stringTag$STRING_TAG_SEPARATOR$this"
}

internal fun String.removeTag(): Pair<String, String>? {
//    val tagSeparatorStartIndex = this.indexOf(STRING_TAG_SEPARATOR)
//    if (tagSeparatorStartIndex == -1){
//        return null
//    }
    try {
        this.split(STRING_TAG_SEPARATOR).let {
            return Pair(it[0], it[1])
        }
    } catch (ex: Throwable) {
        ex.printStackTrace()
        return null
    }

//    return substring(tagSeparatorStartIndex+STRING_TAG_SEPARATOR.length)
}