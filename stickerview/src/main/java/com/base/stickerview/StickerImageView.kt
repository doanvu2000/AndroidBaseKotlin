package com.base.stickerview


import android.content.Context
import android.graphics.Bitmap
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import androidx.annotation.DrawableRes

class StickerImageView : StickerView {

    var ownerId: String? = null
    var ivMain: ImageView? = null

    override val mainView: View
        get() {
            if (this.ivMain == null) {
                this.ivMain = ImageView(context)
                this.ivMain!!.scaleType = ImageView.ScaleType.FIT_XY
            }
            return ivMain as ImageView
        }

    var imageBitmap: Bitmap
        get() = (this.ivMain!!.drawable as BitmapDrawable).bitmap
        set(bmp) = this.ivMain!!.setImageBitmap(bmp)

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
        context, attrs, defStyle
    ) {
    }

    fun setImageResource(@DrawableRes resId: Int) {
        this.ivMain!!.setImageResource(resId)
    }

    fun setImageDrawable(drawable: Drawable) {
        this.ivMain!!.setImageDrawable(drawable)
    }

    fun blendViews(mode: PorterDuff.Mode?) {
        if (mode == null) {
            ivMain!!.setLayerType(LAYER_TYPE_HARDWARE, null)
            return
        }

        val paint = Paint()
        paint.setXfermode(PorterDuffXfermode(mode))
        ivMain!!.setLayerType(LAYER_TYPE_HARDWARE, paint)
    }
}