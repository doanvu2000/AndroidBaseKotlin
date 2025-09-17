package com.jin.widget.progress.progressbarpink

import android.R
import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.widget.ProgressBar
import androidx.core.graphics.toColorInt

/**
 * A custom [ProgressBar] that displays a pink indeterminate spinner.
 *
 * This view extends the standard [ProgressBar] and sets its `indeterminate` property to true
 * and its `indeterminateTintList` to a specific pink color (`#FB80BA`). It is designed to be
 * a simple, reusable component for showing a loading state with a consistent brand color.
 *
 * @param context The Context the view is running in, through which it can
 *        access the current theme, resources, etc.
 * @param attrs The attributes of the XML tag that is inflating the view.
 * @param defStyleAttr An attribute in the current theme that contains a
 *        reference to a style resource that supplies default values for
 *        the view. Can be 0 to not look for defaults.
 */
class ProgressBarPink @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.progressBarStyle
) : ProgressBar(
    context, attrs, defStyleAttr
) {

    init {
        isIndeterminate = true
        indeterminateTintList = ColorStateList.valueOf("#FB80BA".toColorInt())
    }
}