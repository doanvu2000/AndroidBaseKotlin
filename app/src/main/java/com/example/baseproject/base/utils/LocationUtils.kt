package com.example.baseproject.base.utils

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import java.io.IOException
import kotlin.math.absoluteValue

object LocationUtils {
    var DIRECTION_N = "N"
    var DIRECTION_S = "S"
    var DIRECTION_W = "W"
    var DIRECTION_E = "E"
    fun latitudeAsDMS(latitude: Double, decimalPlace: Int): String {
        val direction = if (latitude > 0) DIRECTION_N else DIRECTION_S
        var strLatitude = Location.convert(latitude.absoluteValue, Location.FORMAT_SECONDS)
        strLatitude = replaceDelimiters(strLatitude, decimalPlace)
        strLatitude += " $direction"
        return strLatitude
    }

    fun longitudeAsDMS(longitude: Double, decimalPlace: Int): String {
        val direction = if (longitude > 0) DIRECTION_W else DIRECTION_E
        var strLongitude = Location.convert(longitude.absoluteValue, Location.FORMAT_SECONDS)
        strLongitude = replaceDelimiters(strLongitude, decimalPlace)
        strLongitude += " $direction"
        return strLongitude
    }

    /**
     * @param decimalPlace: length of result string, include digit, ' , ", (N, E, W, S) ~9
     * */
    private fun replaceDelimiters(root: String, decimalPlace: Int): String {
        var str = root
        val char1 = ":"
        val char2 = "°"
        val char3 = "'"
        val char4 = "."
        val char5 = ","
        str = str.replaceFirst(char1.toRegex(), char2)
        str = str.replaceFirst(char1.toRegex(), char3)
        val pointIndex = str.indexOf(char4).coerceAtLeast(str.indexOf(char5))
        if (pointIndex > -1) {
            str = str.substring(0, pointIndex)
        }
        str += "\""
        return str
    }

    fun getAddressNameByLocation(
        context: Context,
        latitude: Double,
        longitude: Double,
        countResult: Int = 1
    ): MutableList<Address>? {
        try {
            val geocoder = Geocoder(context)
            if (!isInternetAvailable(context)) {
                context.showToast("Internet is not available")
                return null
            }
            return geocoder.getFromLocation(latitude, longitude, countResult)
        } catch (ex: IOException) {
            return null
        }
    }

    private fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }

    private fun isInternetAvailable(context: Context): Boolean {
        var result = false
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val networkCapabilities = connectivityManager.activeNetwork ?: return false
            val actNw =
                connectivityManager.getNetworkCapabilities(networkCapabilities) ?: return false
            result = when {
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                else -> false
            }
        } else {
            @Suppress("DEPRECATION")
            connectivityManager.run {
                connectivityManager.activeNetworkInfo?.run {
                    result = when (type) {
                        ConnectivityManager.TYPE_WIFI -> true
                        ConnectivityManager.TYPE_MOBILE -> true
                        ConnectivityManager.TYPE_ETHERNET -> true
                        else -> false
                    }

                }
            }
        }

        return result
    }

}