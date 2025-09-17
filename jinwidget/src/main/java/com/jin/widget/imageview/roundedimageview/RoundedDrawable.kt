package com.jin.widget.imageview.roundedimageview

import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader.TileMode
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.util.Log
import android.widget.ImageView.ScaleType
import androidx.annotation.ColorInt
import androidx.core.graphics.createBitmap
import kotlin.math.max
import kotlin.math.min

/**
 * A Drawable that wraps a bitmap and renders it with rounded corners, a border, and as an oval.
 * This class handles different scale types and allows for customization of corner radii, border width,
 * and border color. It can be created from a `Bitmap` or another `Drawable`.
 *
 * It is designed to be used with `ImageView` or any other view that accepts a `Drawable`.
 *
 * @param sourceBitmap The source bitmap to be rendered. This bitmap will be wrapped and drawn
 * according to the properties set on this drawable.
 */
class RoundedDrawable(val sourceBitmap: Bitmap) : Drawable() {
    private val mBounds = RectF()
    private val mDrawableRect = RectF()
    private val mBitmapRect = RectF()
    private val mBitmapPaint: Paint
    private val mBitmapWidth: Int = sourceBitmap.getWidth()
    private val mBitmapHeight: Int = sourceBitmap.getHeight()
    private val mBorderRect = RectF()
    private val mBorderPaint: Paint
    private val mShaderMatrix = Matrix()
    private val mSquareCornersRect = RectF()

    var tileModeX: TileMode = TileMode.CLAMP
        private set
    var tileModeY: TileMode = TileMode.CLAMP
        private set
    private var mRebuildShader = true

    /**
     * @return the corner radius.
     */
    var cornerRadius: Float = 0f
        private set

    // [ topLeft, topRight, bottomLeft, bottomRight ]
    private val mCornersRounded = booleanArrayOf(true, true, true, true)

    var isOval: Boolean = false
        private set
    var borderWidth: Float = 0f
        private set
    var borderColors: ColorStateList = ColorStateList.valueOf(DEFAULT_BORDER_COLOR)
        private set
    private var mScaleType: ScaleType? = ScaleType.FIT_CENTER

    init {
        mBitmapRect.set(0f, 0f, mBitmapWidth.toFloat(), mBitmapHeight.toFloat())

        mBitmapPaint = Paint()
        mBitmapPaint.style = Paint.Style.FILL
        mBitmapPaint.isAntiAlias = true

        mBorderPaint = Paint()
        mBorderPaint.style = Paint.Style.STROKE
        mBorderPaint.isAntiAlias = true
        mBorderPaint.setColor(borderColors.getColorForState(state, DEFAULT_BORDER_COLOR))
        mBorderPaint.strokeWidth = this.borderWidth
    }

    override fun isStateful(): Boolean {
        return borderColors.isStateful
    }

    override fun onStateChange(state: IntArray): Boolean {
        val newColor = borderColors.getColorForState(state, 0)
        if (mBorderPaint.color != newColor) {
            mBorderPaint.setColor(newColor)
            return true
        } else {
            return super.onStateChange(state)
        }
    }

    private fun updateShaderMatrix() {
        val scale: Float
        var dx: Float
        var dy: Float

        when (mScaleType) {
            ScaleType.CENTER -> {
                mBorderRect.set(mBounds)
                mBorderRect.inset(this.borderWidth / 2, this.borderWidth / 2)

                mShaderMatrix.reset()
                mShaderMatrix.setTranslate(
                    ((mBorderRect.width() - mBitmapWidth) * 0.5f + 0.5f).toInt().toFloat(),
                    ((mBorderRect.height() - mBitmapHeight) * 0.5f + 0.5f).toInt().toFloat()
                )
            }

            ScaleType.CENTER_CROP -> {
                mBorderRect.set(mBounds)
                mBorderRect.inset(this.borderWidth / 2, this.borderWidth / 2)

                mShaderMatrix.reset()

                dx = 0f
                dy = 0f

                if (mBitmapWidth * mBorderRect.height() > mBorderRect.width() * mBitmapHeight) {
                    scale = mBorderRect.height() / mBitmapHeight.toFloat()
                    dx = (mBorderRect.width() - mBitmapWidth * scale) * 0.5f
                } else {
                    scale = mBorderRect.width() / mBitmapWidth.toFloat()
                    dy = (mBorderRect.height() - mBitmapHeight * scale) * 0.5f
                }

                mShaderMatrix.setScale(scale, scale)
                mShaderMatrix.postTranslate(
                    (dx + 0.5f).toInt() + this.borderWidth / 2,
                    (dy + 0.5f).toInt() + this.borderWidth / 2
                )
            }

            ScaleType.CENTER_INSIDE -> {
                mShaderMatrix.reset()

                scale = if (mBitmapWidth <= mBounds.width() && mBitmapHeight <= mBounds.height()) {
                    1.0f
                } else {
                    min(
                        mBounds.width() / mBitmapWidth.toFloat(),
                        mBounds.height() / mBitmapHeight.toFloat()
                    )
                }

                dx = ((mBounds.width() - mBitmapWidth * scale) * 0.5f + 0.5f).toInt().toFloat()
                dy = ((mBounds.height() - mBitmapHeight * scale) * 0.5f + 0.5f).toInt().toFloat()

                mShaderMatrix.setScale(scale, scale)
                mShaderMatrix.postTranslate(dx, dy)

                mBorderRect.set(mBitmapRect)
                mShaderMatrix.mapRect(mBorderRect)
                mBorderRect.inset(this.borderWidth / 2, this.borderWidth / 2)
                mShaderMatrix.setRectToRect(mBitmapRect, mBorderRect, Matrix.ScaleToFit.FILL)
            }

            ScaleType.FIT_CENTER -> {
                mBorderRect.set(mBitmapRect)
                mShaderMatrix.setRectToRect(mBitmapRect, mBounds, Matrix.ScaleToFit.CENTER)
                mShaderMatrix.mapRect(mBorderRect)
                mBorderRect.inset(this.borderWidth / 2, this.borderWidth / 2)
                mShaderMatrix.setRectToRect(mBitmapRect, mBorderRect, Matrix.ScaleToFit.FILL)
            }

            ScaleType.FIT_END -> {
                mBorderRect.set(mBitmapRect)
                mShaderMatrix.setRectToRect(mBitmapRect, mBounds, Matrix.ScaleToFit.END)
                mShaderMatrix.mapRect(mBorderRect)
                mBorderRect.inset(this.borderWidth / 2, this.borderWidth / 2)
                mShaderMatrix.setRectToRect(mBitmapRect, mBorderRect, Matrix.ScaleToFit.FILL)
            }

            ScaleType.FIT_START -> {
                mBorderRect.set(mBitmapRect)
                mShaderMatrix.setRectToRect(mBitmapRect, mBounds, Matrix.ScaleToFit.START)
                mShaderMatrix.mapRect(mBorderRect)
                mBorderRect.inset(this.borderWidth / 2, this.borderWidth / 2)
                mShaderMatrix.setRectToRect(mBitmapRect, mBorderRect, Matrix.ScaleToFit.FILL)
            }

            ScaleType.FIT_XY -> {
                mBorderRect.set(mBounds)
                mBorderRect.inset(this.borderWidth / 2, this.borderWidth / 2)
                mShaderMatrix.reset()
                mShaderMatrix.setRectToRect(mBitmapRect, mBorderRect, Matrix.ScaleToFit.FILL)
            }

            else -> {
                mBorderRect.set(mBitmapRect)
                mShaderMatrix.setRectToRect(mBitmapRect, mBounds, Matrix.ScaleToFit.CENTER)
                mShaderMatrix.mapRect(mBorderRect)
                mBorderRect.inset(this.borderWidth / 2, this.borderWidth / 2)
                mShaderMatrix.setRectToRect(mBitmapRect, mBorderRect, Matrix.ScaleToFit.FILL)
            }
        }

        mDrawableRect.set(mBorderRect)
        mRebuildShader = true
    }

    override fun onBoundsChange(bounds: Rect) {
        super.onBoundsChange(bounds)

        mBounds.set(bounds)

        updateShaderMatrix()
    }

    override fun draw(canvas: Canvas) {
        if (mRebuildShader) {
            val bitmapShader = BitmapShader(
                this.sourceBitmap, this.tileModeX, this.tileModeY
            )
            if (this.tileModeX == TileMode.CLAMP && this.tileModeY == TileMode.CLAMP) {
                bitmapShader.setLocalMatrix(mShaderMatrix)
            }
            mBitmapPaint.setShader(bitmapShader)
            mRebuildShader = false
        }

        if (this.isOval) {
            if (this.borderWidth > 0) {
                canvas.drawOval(mDrawableRect, mBitmapPaint)
                canvas.drawOval(mBorderRect, mBorderPaint)
            } else {
                canvas.drawOval(mDrawableRect, mBitmapPaint)
            }
        } else {
            if (any(mCornersRounded)) {
                val radius = this.cornerRadius
                if (this.borderWidth > 0) {
                    canvas.drawRoundRect(mDrawableRect, radius, radius, mBitmapPaint)
                    canvas.drawRoundRect(mBorderRect, radius, radius, mBorderPaint)
                    redrawBitmapForSquareCorners(canvas)
                    redrawBorderForSquareCorners(canvas)
                } else {
                    canvas.drawRoundRect(mDrawableRect, radius, radius, mBitmapPaint)
                    redrawBitmapForSquareCorners(canvas)
                }
            } else {
                canvas.drawRect(mDrawableRect, mBitmapPaint)
                if (this.borderWidth > 0) {
                    canvas.drawRect(mBorderRect, mBorderPaint)
                }
            }
        }
    }

    private fun redrawBitmapForSquareCorners(canvas: Canvas) {
        if (all(mCornersRounded)) {
            // no square corners
            return
        }

        if (this.cornerRadius == 0f) {
            return  // no round corners
        }

        val left = mDrawableRect.left
        val top = mDrawableRect.top
        val right = left + mDrawableRect.width()
        val bottom = top + mDrawableRect.height()
        val radius = this.cornerRadius

        if (!mCornersRounded[Corner.TOP_LEFT]) {
            mSquareCornersRect.set(left, top, left + radius, top + radius)
            canvas.drawRect(mSquareCornersRect, mBitmapPaint)
        }

        if (!mCornersRounded[Corner.TOP_RIGHT]) {
            mSquareCornersRect.set(right - radius, top, right, radius)
            canvas.drawRect(mSquareCornersRect, mBitmapPaint)
        }

        if (!mCornersRounded[Corner.BOTTOM_RIGHT]) {
            mSquareCornersRect.set(right - radius, bottom - radius, right, bottom)
            canvas.drawRect(mSquareCornersRect, mBitmapPaint)
        }

        if (!mCornersRounded[Corner.BOTTOM_LEFT]) {
            mSquareCornersRect.set(left, bottom - radius, left + radius, bottom)
            canvas.drawRect(mSquareCornersRect, mBitmapPaint)
        }
    }

    private fun redrawBorderForSquareCorners(canvas: Canvas) {
        if (all(mCornersRounded)) {
            // no square corners
            return
        }

        if (this.cornerRadius == 0f) {
            return  // no round corners
        }

        val left = mDrawableRect.left
        val top = mDrawableRect.top
        val right = left + mDrawableRect.width()
        val bottom = top + mDrawableRect.height()
        val radius = this.cornerRadius
        val offset = this.borderWidth / 2

        if (!mCornersRounded[Corner.TOP_LEFT]) {
            canvas.drawLine(left - offset, top, left + radius, top, mBorderPaint)
            canvas.drawLine(left, top - offset, left, top + radius, mBorderPaint)
        }

        if (!mCornersRounded[Corner.TOP_RIGHT]) {
            canvas.drawLine(right - radius - offset, top, right, top, mBorderPaint)
            canvas.drawLine(right, top - offset, right, top + radius, mBorderPaint)
        }

        if (!mCornersRounded[Corner.BOTTOM_RIGHT]) {
            canvas.drawLine(right - radius - offset, bottom, right + offset, bottom, mBorderPaint)
            canvas.drawLine(right, bottom - radius, right, bottom, mBorderPaint)
        }

        if (!mCornersRounded[Corner.BOTTOM_LEFT]) {
            canvas.drawLine(left - offset, bottom, left + radius, bottom, mBorderPaint)
            canvas.drawLine(left, bottom - radius, left, bottom, mBorderPaint)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun getOpacity(): Int {
        return PixelFormat.TRANSLUCENT
    }

    override fun getAlpha(): Int {
        return mBitmapPaint.alpha
    }

    override fun setAlpha(alpha: Int) {
        mBitmapPaint.setAlpha(alpha)
        invalidateSelf()
    }

    override fun getColorFilter(): ColorFilter? {
        return mBitmapPaint.colorFilter
    }

    override fun setColorFilter(cf: ColorFilter?) {
        mBitmapPaint.setColorFilter(cf)
        invalidateSelf()
    }

    @Deprecated("Deprecated in Java")
    override fun setDither(dither: Boolean) {
        mBitmapPaint.isDither = dither
        invalidateSelf()
    }

    override fun setFilterBitmap(filter: Boolean) {
        mBitmapPaint.isFilterBitmap = filter
        invalidateSelf()
    }

    override fun getIntrinsicWidth(): Int {
        return mBitmapWidth
    }

    override fun getIntrinsicHeight(): Int {
        return mBitmapHeight
    }

    /**
     * @param corner the specific corner to get radius of.
     * @return the corner radius of the specified corner.
     */
    fun getCornerRadius(@Corner corner: Int): Float {
        return if (mCornersRounded[corner]) this.cornerRadius else 0f
    }

    /**
     * Sets all corners to the specified radius.
     *
     * @param radius the radius.
     * @return the [RoundedDrawable] for chaining.
     */
    fun setCornerRadius(radius: Float): RoundedDrawable {
        setCornerRadius(radius, radius, radius, radius)
        return this
    }

    /**
     * Sets the corner radius of one specific corner.
     *
     * @param corner the corner.
     * @param radius the radius.
     * @return the [RoundedDrawable] for chaining.
     */
    fun setCornerRadius(@Corner corner: Int, radius: Float): RoundedDrawable {
        require(!(radius != 0f && this.cornerRadius != 0f && this.cornerRadius != radius)) { "Multiple nonzero corner radii not yet supported." }

        if (radius == 0f) {
            if (only(corner, mCornersRounded)) {
                this.cornerRadius = 0f
            }
            mCornersRounded[corner] = false
        } else {
            if (this.cornerRadius == 0f) {
                this.cornerRadius = radius
            }
            mCornersRounded[corner] = true
        }

        return this
    }

    /**
     * Sets the corner radii of all the corners.
     *
     * @param topLeft     top left corner radius.
     * @param topRight    top right corner radius
     * @param bottomRight bototm right corner radius.
     * @param bottomLeft  bottom left corner radius.
     * @return the [RoundedDrawable] for chaining.
     */
    fun setCornerRadius(
        topLeft: Float, topRight: Float, bottomRight: Float, bottomLeft: Float
    ): RoundedDrawable {
        val radiusSet: MutableSet<Float?> = HashSet(4)
        radiusSet.add(topLeft)
        radiusSet.add(topRight)
        radiusSet.add(bottomRight)
        radiusSet.add(bottomLeft)

        radiusSet.remove(0f)

        require(radiusSet.size <= 1) { "Multiple nonzero corner radii not yet supported." }

        if (!radiusSet.isEmpty()) {
            val radius: Float = radiusSet.iterator().next()!!
            if (radius >= 0) {
                this.cornerRadius = radius
            }
        } else {
            this.cornerRadius = 0f
        }

        mCornersRounded[Corner.TOP_LEFT] = topLeft > 0
        mCornersRounded[Corner.TOP_RIGHT] = topRight > 0
        mCornersRounded[Corner.BOTTOM_RIGHT] = bottomRight > 0
        mCornersRounded[Corner.BOTTOM_LEFT] = bottomLeft > 0
        return this
    }

    fun setBorderWidth(width: Float): RoundedDrawable {
        this.borderWidth = width
        mBorderPaint.strokeWidth = this.borderWidth
        return this
    }

    val borderColor: Int
        get() = borderColors.defaultColor

    fun setBorderColor(@ColorInt color: Int): RoundedDrawable {
        return setBorderColor(ColorStateList.valueOf(color))
    }

    fun setBorderColor(colors: ColorStateList?): RoundedDrawable {
        this.borderColors = colors ?: ColorStateList.valueOf(0)
        mBorderPaint.setColor(borderColors.getColorForState(state, DEFAULT_BORDER_COLOR))
        return this
    }

    fun setOval(oval: Boolean): RoundedDrawable {
        this.isOval = oval
        return this
    }

    val scaleType: ScaleType
        get() = mScaleType ?: ScaleType.FIT_CENTER

    fun setScaleType(scaleType: ScaleType?): RoundedDrawable {
        var scaleType = scaleType
        if (scaleType == null) {
            scaleType = ScaleType.FIT_CENTER
        }
        if (mScaleType != scaleType) {
            mScaleType = scaleType
            updateShaderMatrix()
        }
        return this
    }

    fun setTileModeX(tileModeX: TileMode?): RoundedDrawable {
        tileModeX?.let {
            if (this.tileModeX != tileModeX) {
                this.tileModeX = tileModeX
                mRebuildShader = true
                invalidateSelf()
            }
        }
        return this
    }

    fun setTileModeY(tileModeY: TileMode?): RoundedDrawable {
        tileModeY?.let {
            if (this.tileModeY != tileModeY) {
                this.tileModeY = tileModeY
                mRebuildShader = true
                invalidateSelf()
            }
        }
        return this
    }

    fun toBitmap(): Bitmap? {
        return drawableToBitmap(this)
    }

    companion object {
        const val TAG: String = "RoundedDrawable"
        const val DEFAULT_BORDER_COLOR: Int = Color.BLACK

        fun fromBitmap(bitmap: Bitmap?): RoundedDrawable? {
            return if (bitmap != null) {
                RoundedDrawable(bitmap)
            } else {
                null
            }
        }

        fun fromDrawable(drawable: Drawable?): Drawable? {
            if (drawable != null) {
                if (drawable is RoundedDrawable) {
                    // just return if it's already a RoundedDrawable
                    return drawable
                } else if (drawable is LayerDrawable) {
                    val cs = drawable.mutate().constantState
                    val ld = (cs?.newDrawable() ?: drawable) as LayerDrawable

                    val num = ld.numberOfLayers

                    // loop through layers to and change to RoundedDrawables if possible
                    for (i in 0..<num) {
                        val d = ld.getDrawable(i)
                        ld.setDrawableByLayerId(ld.getId(i), fromDrawable(d))
                    }
                    return ld
                }

                // try to get a bitmap from the drawable and
                val bm: Bitmap? = drawableToBitmap(drawable)
                if (bm != null) {
                    return RoundedDrawable(bm)
                }
            }
            return drawable
        }

        fun drawableToBitmap(drawable: Drawable): Bitmap? {
            if (drawable is BitmapDrawable) {
                return drawable.bitmap
            }

            var bitmap: Bitmap?
            val width = max(drawable.intrinsicWidth, 2)
            val height = max(drawable.intrinsicHeight, 2)
            try {
                bitmap = createBitmap(width, height)
                val canvas = Canvas(bitmap)
                drawable.setBounds(0, 0, canvas.width, canvas.height)
                drawable.draw(canvas)
            } catch (e: Throwable) {
                e.printStackTrace()
                Log.w(TAG, "Failed to create bitmap from drawable!")
                bitmap = null
            }

            return bitmap
        }

        private fun only(index: Int, booleans: BooleanArray): Boolean {
            var i = 0
            val len = booleans.size
            while (i < len) {
                if (booleans[i] != (i == index)) {
                    return false
                }
                i++
            }
            return true
        }

        private fun any(booleans: BooleanArray): Boolean {
            for (b in booleans) {
                if (b) {
                    return true
                }
            }
            return false
        }

        private fun all(booleans: BooleanArray): Boolean {
            for (b in booleans) {
                if (b) {
                    return false
                }
            }
            return true
        }
    }
}