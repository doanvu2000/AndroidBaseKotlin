package com.example.baseproject.base.pref

import android.content.Context
import android.os.Parcelable
import com.tencent.mmkv.MMKV
import java.util.Collections

class LocalCache private constructor() {

    companion object {
        @Volatile
        private var instance: LocalCache? = null

        @JvmStatic
        fun getInstance() =
            instance ?: synchronized(this) {
                instance ?: LocalCache().also { instance = it }
            }


        fun initialize(context: Context) {
            MMKV.initialize(context)
        }
    }

    private var cache: MMKV = MMKV.defaultMMKV()!!

    fun put(key: String, value: Any?): Boolean {
        when (value) {
            is String -> return cache.encode(key, value)
            is Float -> return cache.encode(key, value)
            is Boolean -> return cache.encode(key, value)
            is Int -> return cache.encode(key, value)
            is Long -> return cache.encode(key, value)
            is Double -> return cache.encode(key, value)
            is ByteArray -> return cache.encode(key, value)
        }
        return false
    }

    fun <T : Parcelable> putParcelableCollection(
        data: Collection<T>,
        key: String,
        onComplete: (() -> Unit)? = null
    ) {
        data.map {
            it.toSerializedString().addTag()
        }.toMutableSet().let {
            cache.encode(key, it).let { finish ->
                if (finish) {
                    onComplete?.invoke()
                }
            }
        }
    }

    fun <T : Parcelable> getParcelableCollection(
        creator: Parcelable.Creator<T>,
        key: String
    ): Collection<T>? {
        try {
            cache.decodeStringSet(key, mutableSetOf())?.let { dataSet ->
                if (!dataSet.isNullOrEmpty()) {
                    return dataSet.mapNotNull { result ->
                        result.removeTag()?.second?.toParcelable(creator)
                    }.map {
                        it
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    fun <T : Parcelable> put(key: String, t: T?): Boolean {
        return cache.encode(key, t)
    }

    fun <T : Parcelable> getParcelable(key: String, tClass: Class<T>): T? {
        return cache.decodeParcelable(key, tClass)
    }

    fun put(key: String, sets: Set<String>?): Boolean {
        return cache.encode(key, sets)
    }

    fun getStringSet(key: String): Set<String>? {
        return cache.decodeStringSet(key, Collections.emptySet())
    }

    fun getInt(key: String): Int? {
        return cache.decodeInt(key, 0)
    }

    fun getDouble(key: String): Double? {
        return cache.decodeDouble(key, 0.00)
    }

    fun getLong(key: String): Long? {
        return cache.decodeLong(key, 0L)
    }

    fun getBoolean(key: String): Boolean? {
        return cache.decodeBool(key, false)
    }

    fun getFloat(key: String): Float? {
        return cache.decodeFloat(key, 0F)
    }

    fun getString(key: String): String? {
        return cache.decodeString(key, "")
    }

    fun getBytes(key: String): ByteArray? {
        return cache.decodeBytes(key)
    }

    fun removeKey(key: String) {
        cache.removeValueForKey(key)
    }

    fun removeKeys(keys: Array<String>) {
        cache.removeValuesForKeys(keys)
    }

    fun clearAll() {
        cache.clearAll()
    }

    fun isKeyExisted(key: String): Boolean {
        return cache.containsKey(key)
    }

    fun getTotalSize(): Long {
        return cache.totalSize()
    }
}