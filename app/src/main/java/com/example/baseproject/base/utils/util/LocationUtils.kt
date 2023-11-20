package com.example.baseproject.base.utils.util

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.location.Location
import com.example.baseproject.base.utils.extension.isInternetAvailable
import com.example.baseproject.base.utils.extension.isSdk33
import com.example.baseproject.base.utils.extension.showToast
import kotlin.math.absoluteValue

//TODO: get user current location (last location)
object LocationUtils {
    private var DIRECTION_N = "N"
    private var DIRECTION_S = "S"
    private var DIRECTION_W = "W"
    private var DIRECTION_E = "E"
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
        countResult: Int = 1,
        onGetAddress: (MutableList<Address>?) -> Unit
    ) {
        try {
            val geocoder = Geocoder(context)
            if (!context.isInternetAvailable()) {
                context.showToast("Internet is not available")
            }
            if (isSdk33()) {
                geocoder.getFromLocation(latitude, longitude, countResult) { address ->
                    onGetAddress(address)
                }
            } else {
                @Suppress("DEPRECATION")
                onGetAddress(geocoder.getFromLocation(latitude, longitude, countResult))
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }
}