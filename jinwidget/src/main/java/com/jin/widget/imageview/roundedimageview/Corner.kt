package com.jin.widget.imageview.roundedimageview

import androidx.annotation.IntDef
import com.jin.widget.imageview.roundedimageview.Corner.Companion.BOTTOM_LEFT
import com.jin.widget.imageview.roundedimageview.Corner.Companion.BOTTOM_RIGHT
import com.jin.widget.imageview.roundedimageview.Corner.Companion.TOP_LEFT
import com.jin.widget.imageview.roundedimageview.Corner.Companion.TOP_RIGHT

/**
 * Defines the corners that can be rounded.
 *
 * This annotation is used with an [IntDef] to ensure that only valid corner constants
 * are used where a corner identifier is expected.
 *
 * @see TOP_LEFT
 * @see TOP_RIGHT
 * @see BOTTOM_RIGHT
 * @see BOTTOM_LEFT
 */
@Retention(AnnotationRetention.SOURCE)
@IntDef(
    Corner.Companion.TOP_LEFT,
    Corner.Companion.TOP_RIGHT,
    Corner.Companion.BOTTOM_LEFT,
    Corner.Companion.BOTTOM_RIGHT
)
annotation class Corner {
    companion object {
        const val TOP_LEFT: Int = 0
        const val TOP_RIGHT: Int = 1
        const val BOTTOM_RIGHT: Int = 2
        const val BOTTOM_LEFT: Int = 3
    }
}