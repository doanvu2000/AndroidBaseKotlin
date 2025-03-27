package com.jin

import android.graphics.Color
import androidx.core.graphics.toColorInt

object ColorsDefault {
    val rgb102: Int = "#666666".toColorInt()
    val rgb227: Int = "#e3e3e3".toColorInt()
    val rgb230: Int = "#e6e6e6".toColorInt()
    val rgb245: Int = "#f5f5f5".toColorInt()

    val aliceBlue = "#EFF7FE".toColorInt()
    val cyanBlue = "#2789E4".toColorInt()

    val defaultEventColor = "#9fc6e7".toColorInt()
    val newEventColor = "#3c93d9".toColorInt()

    fun getRgbColor(r: Int, g: Int, b: Int): Int = Color.rgb(r, g, b)
}
