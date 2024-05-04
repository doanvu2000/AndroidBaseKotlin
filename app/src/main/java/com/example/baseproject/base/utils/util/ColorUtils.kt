package com.example.baseproject.base.utils.util

object ColorUtils {
    const val TAG: String = "ColorTransparentUtils"

    /**
     * This method convert number into hex number or we can say transparent code
     *
     * @param trans number of transparency you want
     * @return it return hex decimal number or transparency code
     */
    private fun convertTransToHex(trans: Int): String {
        val hexString = Integer.toHexString(Math.round((255 * trans / 100).toFloat()))
        return (if (hexString.length < 2) "0" else "") + hexString
    }

    fun transparentColor(color: String, trans: Int): String {
        if (trans == 100) {
            return color
        }
        return "#" + convertTransToHex(trans) + color.removePrefix(color.first().toString())
    }
}

/**
 * @param color it is color code string: #FFFFFF
 * @param alpha it is alpha value of transparency
 * */
fun getColorHex(color: String, alpha: Int): String {
    return ColorUtils.transparentColor(color, alpha)
}