package com.example.baseproject.base.utils.util

import kotlin.math.roundToInt

object ColorUtils {

    //region Constants

    const val TAG: String = "ColorTransparentUtils"

    //endregion

    //region Color Transparency

    /**
     * Convert transparency percentage to hex value
     * @param trans transparency percentage (0-100)
     * @return hex string for transparency
     */
    private fun convertTransToHex(trans: Int): String {
        val hexString = Integer.toHexString((255 * trans / 100).toFloat().roundToInt())
        return (if (hexString.length < 2) "0" else "") + hexString
    }

    /**
     * Apply transparency to color
     * @param color base color string (e.g., "#FFFFFF")
     * @param trans transparency percentage (0-100, where 100 is fully opaque)
     * @return color string with transparency applied
     */
    fun transparentColor(color: String, trans: Int): String {
        if (trans == 100) {
            return color
        }
        return "#" + convertTransToHex(trans) + color.removePrefix("#")
    }

    //endregion
}

//region Global Helper Functions

/**
 * Get color with transparency applied
 * @param color base color code string (e.g., "#FFFFFF")
 * @param alpha transparency value (0-100)
 * @return color string with alpha applied
 */
fun getColorHex(color: String, alpha: Int): String {
    return ColorUtils.transparentColor(color, alpha)
}

//endregion
