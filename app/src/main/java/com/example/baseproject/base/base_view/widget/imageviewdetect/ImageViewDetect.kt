package com.example.baseproject.base.base_view.widget.imageviewdetect

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.widget.ImageView

/**
 * @author doanvv
 * @since 06/06/2024
 *
 * detect when drawable loaded
 * */
@SuppressLint("AppCompatCustomView")
class ImageViewDetect : ImageView {
    var onDrawableLoaded: () -> Unit = {}

    constructor(context: Context) : super(context) {
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {

    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context, attrs, defStyleAttr
    ) {

    }

    override fun setImageDrawable(drawable: Drawable?) {
        super.setImageDrawable(drawable)
        if (drawable != null) {
            onDrawableLoaded.invoke()
        }
    }
}