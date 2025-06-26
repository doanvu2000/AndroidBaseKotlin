package com.example.baseproject.base.utils.extension

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import java.io.Serializable

//region Serializable Data Handling

/**
 * Safely get Serializable data from Bundle with proper API level handling
 * @param key bundle key
 * @param clazz class type
 * @return Serializable object or null
 */
inline fun <reified T : Serializable> Bundle.getDataSerializable(key: String, clazz: Class<T>): T? {
    return if (isSdk33()) {
        getSerializable(key, clazz)
    } else {
        @Suppress("DEPRECATION") getSerializable(key) as? T
    }
}

//endregion

//region Parcelable Data Handling

/**
 * Safely get Parcelable from Intent with proper API level handling
 * @param key intent extra key
 * @return Parcelable object or null
 */
inline fun <reified T : Parcelable> Intent.parcelable(key: String): T? = when {
    isSdk33() -> getParcelableExtra(key, T::class.java)
    else -> @Suppress("DEPRECATION") getParcelableExtra(key) as? T
}

/**
 * Safely get Parcelable from Bundle with proper API level handling
 * @param key bundle key
 * @return Parcelable object or null
 */
inline fun <reified T : Parcelable> Bundle.parcelable(key: String): T? = when {
    isSdk33() -> getParcelable(key, T::class.java)
    else -> @Suppress("DEPRECATION") getParcelable(key) as? T
}

/**
 * Safely get Parcelable from Activity Intent with class name as key
 * @return Parcelable object or null
 */
inline fun <reified T : Parcelable> Activity.getParcelable(): T? = when {
    isSdk33() -> intent?.extras?.getParcelable(T::class.java.name, T::class.java)
    else -> @Suppress("DEPRECATION") intent?.extras?.getParcelable(T::class.java.name) as? T
}

/**
 * Safely get Parcelable ArrayList from Bundle with proper API level handling
 * @param key bundle key
 * @return ArrayList of Parcelable objects or null
 */
inline fun <reified T : Parcelable> Bundle.parcelableArrayList(key: String): ArrayList<T>? = when {
    isSdk33() -> getParcelableArrayList(key, T::class.java)
    else -> @Suppress("DEPRECATION") getParcelableArrayList(key)
}

/**
 * Safely get Parcelable ArrayList from Intent with proper API level handling
 * @param key intent extra key
 * @return ArrayList of Parcelable objects or null
 */
inline fun <reified T : Parcelable> Intent.parcelableArrayList(key: String): ArrayList<T>? = when {
    isSdk33() -> getParcelableArrayListExtra(key, T::class.java)
    else -> @Suppress("DEPRECATION") getParcelableArrayListExtra(key)
}

//endregion

//region Bundle Creation Utilities

/**
 * Create Bundle with data using class name as key
 * @param data data to put in bundle
 * @return Bundle with data
 */
inline fun <reified T> pushBundle(data: T): Bundle {
    val bundle = Bundle()
    when (data) {
        is Long -> bundle.putLong(T::class.java.name, data)
        is Int -> bundle.putInt(T::class.java.name, data)
        is Float -> bundle.putFloat(T::class.java.name, data)
        is Boolean -> bundle.putBoolean(T::class.java.name, data)
        is String -> bundle.putString(T::class.java.name, data)
        is Parcelable -> bundle.putParcelable(T::class.java.name, data)
        is Serializable -> bundle.putSerializable(T::class.java.name, data)
        else -> bundle.putString(T::class.java.name, data.toString())
    }
    return bundle
}

/**
 * Create Bundle with data using custom key
 * @param key custom key for bundle
 * @param data data to put in bundle
 * @return Bundle with data
 */
fun <T> pushBundle(key: String, data: T): Bundle {
    val bundle = Bundle()
    when (data) {
        is Long -> bundle.putLong(key, data)
        is Int -> bundle.putInt(key, data)
        is Float -> bundle.putFloat(key, data)
        is Boolean -> bundle.putBoolean(key, data)
        is String -> bundle.putString(key, data)
        is Parcelable -> bundle.putParcelable(key, data)
        is Serializable -> bundle.putSerializable(key, data)
        else -> bundle.putString(key, data.toString())
    }
    return bundle
}

//endregion
