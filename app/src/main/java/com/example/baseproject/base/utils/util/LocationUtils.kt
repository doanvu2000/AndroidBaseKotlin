package com.example.baseproject.base.utils.util

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.location.Location
import com.example.baseproject.base.utils.extension.isInternetAvailable
import com.example.baseproject.base.utils.extension.isSdk33
import com.example.baseproject.base.utils.extension.showToast
import kotlin.math.absoluteValue

object LocationUtils {

    //region Constants

    private const val DIRECTION_N = "N"
    private const val DIRECTION_S = "S"
    private const val DIRECTION_W = "W"
    private const val DIRECTION_E = "E"

    //endregion

    //region DMS Conversion (Degrees, Minutes, Seconds)

    /**
     * Convert latitude to DMS format
     * @param latitude latitude value
     * @param decimalPlace decimal places for formatting
     * @return formatted latitude string with direction
     */
    fun latitudeAsDMS(latitude: Double, decimalPlace: Int): String {
        val direction = if (latitude > 0) DIRECTION_N else DIRECTION_S
        var strLatitude = Location.convert(latitude.absoluteValue, Location.FORMAT_SECONDS)
        strLatitude = replaceDelimiters(strLatitude, decimalPlace)
        strLatitude += " $direction"
        return strLatitude
    }

    /**
     * Convert longitude to DMS format
     * @param longitude longitude value
     * @param decimalPlace decimal places for formatting
     * @return formatted longitude string with direction
     */
    fun longitudeAsDMS(longitude: Double, decimalPlace: Int): String {
        val direction = if (longitude > 0) DIRECTION_E else DIRECTION_W
        var strLongitude = Location.convert(longitude.absoluteValue, Location.FORMAT_SECONDS)
        strLongitude = replaceDelimiters(strLongitude, decimalPlace)
        strLongitude += " $direction"
        return strLongitude
    }

    /**
     * Replace delimiters in coordinate string
     * @param root original coordinate string
     * @param decimalPlace length of result string, include digit, ' , ", (N, E, W, S) ~9
     * @return formatted coordinate string
     */
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

    //endregion

    //region Geocoding

    /**
     * Get address name by location coordinates
     * @param context application context
     * @param latitude latitude coordinate
     * @param longitude longitude coordinate
     * @param countResult maximum number of results
     * @param onGetAddress callback with address list
     */
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
                return
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
            onGetAddress(null)
        }
    }

    //endregion
}