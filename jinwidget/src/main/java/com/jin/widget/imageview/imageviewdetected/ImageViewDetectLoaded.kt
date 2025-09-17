package com.jin.widget.imageview.imageviewdetected

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView

/**
 * An [AppCompatImageView] subclass that provides a callback to detect when a [Drawable] has been successfully set.
 * This is useful for scenarios where you need to perform an action immediately after an image has been loaded,
 * for example, by image loading libraries like Glide or Picasso.
 *
 * Example usage:
 * ```kotlin
 * val imageView = ImageViewDetectLoaded(context)
 * imageView.onDrawableLoaded = {
 *     // This block will be executed when an image is set.
 *     println("Image has been loaded!")
 * }
 *
 * // When Glide/Picasso/etc. loads an image into this ImageView,
 * // the onDrawableLoaded lambda will be invoked.
 * Glide.with(this).load(imageUrl).into(imageView)
 * ```
 */
class ImageViewDetectLoaded : AppCompatImageView {

    constructor(context: Context) : super(context) {
//        initView()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
//        initView()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context, attrs, defStyleAttr
    ) {
//        initView()
    }

    var onDrawableLoaded: () -> Unit = {}

    override fun setImageDrawable(drawable: Drawable?) {
        super.setImageDrawable(drawable)
        if (drawable != null) {
            onDrawableLoaded.invoke()
        }
    }
}