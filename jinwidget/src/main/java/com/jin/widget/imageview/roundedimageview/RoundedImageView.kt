package com.jin.widget.imageview.roundedimageview

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.ColorFilter
import android.graphics.Shader.TileMode
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.net.Uri
import android.util.AttributeSet
import android.util.Log
import androidx.annotation.ColorInt
import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.res.ResourcesCompat
import androidx.core.content.withStyledAttributes
import androidx.core.graphics.drawable.toDrawable
import androidx.core.graphics.toColorInt
import com.jin.widget.R
import kotlin.math.max

/**
 * An `ImageView` that supports rounded corners, borders, and ovals.
 * This class enhances the standard `ImageView` to allow for more flexible and decorative image presentations,
 * commonly used for user avatars, thumbnails, and other UI elements requiring non-rectangular shapes.
 *
 * It can be configured via XML attributes or programmatically.
 *
 * XML Attributes:
 * - `riv_corner_radius`: Sets a global radius for all corners.
 * - `riv_corner_radius_top_left`, `riv_corner_radius_top_right`, `riv_corner_radius_bottom_left`, `riv_corner_radius_bottom_right`: Set radius for individual corners.
 * - `riv_border_width`: Sets the width of the border around the image.
 * - `riv_border_color`: Sets the color of the border. Can be a single color or a `ColorStateList`.
 * - `riv_oval`: If set to `true`, the image will be clipped to an oval shape, ignoring corner radii.
 * - `riv_tile_mode`, `riv_tile_mode_x`, `riv_tile_mode_y`: Define how the image should be tiled if it's smaller than the view.
 * - `riv_mutate_background`: If `true`, the background drawable will also be rounded according to the view's settings.
 *
 * @constructor Creates a RoundedImageView instance.
 * @param context The Context the view is running in, through which it can access the current theme, resources, etc.
 * @param attrs The attributes of the XML tag that is inflating the view.
 * @param defStyle An attribute in the current theme that contains a reference to a style resource that supplies default values for the view. Can be 0 to not look for defaults.
 */
class RoundedImageView : AppCompatImageView {
    private val mCornerRadii: FloatArray? =
        floatArrayOf(DEFAULT_RADIUS, DEFAULT_RADIUS, DEFAULT_RADIUS, DEFAULT_RADIUS)

    private var mBackgroundDrawable: Drawable? = null
    private var mBorderColor: ColorStateList? =
        ColorStateList.valueOf(RoundedDrawable.DEFAULT_BORDER_COLOR)
    var borderWidth: Float = DEFAULT_BORDER_WIDTH
        private set
    private var mColorFilter: ColorFilter? = null
    private var mColorMod = false
    private var mDrawable: Drawable? = null
    private var mHasColorFilter = false
    private var mIsOval = false
    private var mMutateBackground = false
    private var mResource = 0
    private var mBackgroundResource = 0
    private var mScaleType: ScaleType? = null
    private var mTileModeX: TileMode = DEFAULT_TILE_MODE
    private var mTileModeY: TileMode = DEFAULT_TILE_MODE

    var onDrawableLoaded: (() -> Unit)? = null

    constructor(context: Context) : super(context)

    @JvmOverloads
    constructor(context: Context, attrs: AttributeSet?, defStyle: Int = 0) : super(
        context,
        attrs,
        defStyle
    ) {
        context.withStyledAttributes(attrs, R.styleable.RoundedImageView, defStyle, 0) {

            val index: Int = getInt(R.styleable.RoundedImageView_android_scaleType, -1)
            if (index >= 0) {
                setScaleType(SCALE_TYPES[index]!!)
            } else {
                // default scaleType to FIT_CENTER
                setScaleType(ScaleType.FIT_CENTER)
            }

            var cornerRadiusOverride =
                getDimensionPixelSize(R.styleable.RoundedImageView_riv_corner_radius, -1).toFloat()

            mCornerRadii!![Corner.TOP_LEFT] =
                getDimensionPixelSize(R.styleable.RoundedImageView_riv_corner_radius_top_left, -1)
                    .toFloat()
            mCornerRadii[Corner.TOP_RIGHT] =
                getDimensionPixelSize(R.styleable.RoundedImageView_riv_corner_radius_top_right, -1)
                    .toFloat()
            mCornerRadii[Corner.BOTTOM_RIGHT] =
                getDimensionPixelSize(
                    R.styleable.RoundedImageView_riv_corner_radius_bottom_right,
                    -1
                )
                    .toFloat()
            mCornerRadii[Corner.BOTTOM_LEFT] =
                getDimensionPixelSize(
                    R.styleable.RoundedImageView_riv_corner_radius_bottom_left,
                    -1
                )
                    .toFloat()

            var any = false
            var i = 0
            val len = mCornerRadii.size
            while (i < len) {
                if (mCornerRadii[i] < 0) {
                    mCornerRadii[i] = 0f
                } else {
                    any = true
                }
                i++
            }

            if (!any) {
                if (cornerRadiusOverride < 0) {
                    cornerRadiusOverride = DEFAULT_RADIUS
                }
                var i = 0
                val len = mCornerRadii.size
                while (i < len) {
                    mCornerRadii[i] = cornerRadiusOverride
                    i++
                }
            }

            borderWidth =
                getDimensionPixelSize(R.styleable.RoundedImageView_riv_border_width, -1).toFloat()
            if (borderWidth < 0) {
                borderWidth = DEFAULT_BORDER_WIDTH
            }

            mBorderColor = getColorStateList(R.styleable.RoundedImageView_riv_border_color)
            if (mBorderColor == null) {
                mBorderColor = ColorStateList.valueOf(RoundedDrawable.DEFAULT_BORDER_COLOR)
            }

            mMutateBackground =
                getBoolean(R.styleable.RoundedImageView_riv_mutate_background, false)
            mIsOval = getBoolean(R.styleable.RoundedImageView_riv_oval, false)

            val tileMode: Int =
                getInt(R.styleable.RoundedImageView_riv_tile_mode, TILE_MODE_UNDEFINED)
            if (tileMode != TILE_MODE_UNDEFINED) {
                parseTileMode(tileMode)?.let {
                    setTileModeX(it)
                    setTileModeY(it)
                }
            }

            val tileModeX: Int =
                getInt(R.styleable.RoundedImageView_riv_tile_mode_x, TILE_MODE_UNDEFINED)
            if (tileModeX != TILE_MODE_UNDEFINED) {
                parseTileMode(tileModeX)?.let {
                    setTileModeX(it)
                }
            }

            val tileModeY: Int =
                getInt(R.styleable.RoundedImageView_riv_tile_mode_y, TILE_MODE_UNDEFINED)
            if (tileModeY != TILE_MODE_UNDEFINED) {
                parseTileMode(tileModeY)?.let {
                    setTileModeY(it)
                }
            }

            updateDrawableAttrs()
            updateBackgroundDrawableAttrs(true)

            if (mMutateBackground) {
                super.setBackgroundDrawable(mBackgroundDrawable)
            }

        }
    }

    override fun drawableStateChanged() {
        super.drawableStateChanged()
        invalidate()
    }

    override fun getScaleType(): ScaleType? {
        return mScaleType
    }

    override fun setScaleType(scaleType: ScaleType) {
        checkNotNull(scaleType)

        if (mScaleType != scaleType) {
            mScaleType = scaleType

            when (scaleType) {
                ScaleType.CENTER, ScaleType.CENTER_CROP, ScaleType.CENTER_INSIDE, ScaleType.FIT_CENTER, ScaleType.FIT_START, ScaleType.FIT_END, ScaleType.FIT_XY -> super.setScaleType(
                    ScaleType.FIT_XY
                )

                else -> super.setScaleType(scaleType)
            }

            updateDrawableAttrs()
            updateBackgroundDrawableAttrs(false)
            invalidate()
        }
    }

    override fun setImageDrawable(drawable: Drawable?) {
        mResource = 0
        mDrawable = RoundedDrawable.fromDrawable(drawable)
        updateDrawableAttrs()
        super.setImageDrawable(mDrawable)
        if (drawable != null) {
            onDrawableLoaded?.invoke()
        }
    }

    override fun setImageBitmap(bm: Bitmap?) {
        mResource = 0
        mDrawable = RoundedDrawable.fromBitmap(bm)
        updateDrawableAttrs()
        super.setImageDrawable(mDrawable)
    }

    override fun setImageResource(@DrawableRes resId: Int) {
        if (mResource != resId) {
            mResource = resId
            mDrawable = resolveResource()
            updateDrawableAttrs()
            super.setImageDrawable(mDrawable)
        }
    }

    override fun setImageURI(uri: Uri?) {
        super.setImageURI(uri)
        setImageDrawable(getDrawable())
    }

    private fun resolveResource(): Drawable? {
        val srcs = resources
        if (srcs == null) {
            return null
        }

        var d: Drawable? = null

        if (mResource != 0) {
            try {
                d = ResourcesCompat.getDrawable(resources, mResource, null)
            } catch (e: Exception) {
                Log.w(TAG, "Unable to find resource: $mResource", e)
                // Don't try again.
                mResource = 0
            }
        }
        return RoundedDrawable.fromDrawable(d)
    }

    override fun setBackground(background: Drawable?) {
        setBackgroundDrawable(background)
    }

    override fun setBackgroundResource(@DrawableRes resId: Int) {
        if (mBackgroundResource != resId) {
            mBackgroundResource = resId
            mBackgroundDrawable = resolveBackgroundResource()
            setBackgroundDrawable(mBackgroundDrawable)
        }
    }

    override fun setBackgroundColor(color: Int) {
        mBackgroundDrawable = color.toDrawable()
        setBackgroundDrawable(mBackgroundDrawable)
    }

    private fun resolveBackgroundResource(): Drawable? {
        val srcs = resources
        if (srcs == null) {
            return null
        }

        var d: Drawable? = null

        if (mBackgroundResource != 0) {
            try {
                d = ResourcesCompat.getDrawable(resources, mBackgroundResource, null)
            } catch (e: Exception) {
                Log.w(TAG, "Unable to find resource: $mBackgroundResource", e)
                // Don't try again.
                mBackgroundResource = 0
            }
        }
        return RoundedDrawable.fromDrawable(d)
    }

    private fun updateDrawableAttrs() {
        updateAttrs(mDrawable, mScaleType)
    }

    private fun updateBackgroundDrawableAttrs(convert: Boolean) {
        if (mMutateBackground) {
            if (convert) {
                mBackgroundDrawable = RoundedDrawable.fromDrawable(mBackgroundDrawable)
            }
            updateAttrs(mBackgroundDrawable, ScaleType.FIT_XY)
        }
    }

    override fun setColorFilter(cf: ColorFilter?) {
        if (mColorFilter !== cf) {
            mColorFilter = cf
            mHasColorFilter = true
            mColorMod = true
            applyColorMod()
            invalidate()
        }
    }

    private fun applyColorMod() {
        // Only mutate and apply when modifications have occurred. This should
        // not reset the mColorMod flag, since these filters need to be
        // re-applied if the Drawable is changed.
        if (mDrawable != null && mColorMod) {
            mDrawable = mDrawable!!.mutate()
            if (mHasColorFilter) {
                mDrawable!!.colorFilter = mColorFilter
            }
            // TODO: support, eventually...
            //mDrawable.setXFerMode(mXFerMode);
            //mDrawable.setAlpha(mAlpha * mViewAlphaScale >> 8);
        }
    }

    private fun updateAttrs(drawable: Drawable?, scaleType: ScaleType?) {
        if (drawable == null) {
            return
        }

        if (drawable is RoundedDrawable) {
            drawable
                .setScaleType(scaleType)
                .setBorderWidth(this.borderWidth)
                .setBorderColor(mBorderColor)
                .setOval(mIsOval)
                .setTileModeX(mTileModeX)
                .setTileModeY(mTileModeY)

            if (mCornerRadii != null) {
                drawable.setCornerRadius(
                    mCornerRadii[Corner.TOP_LEFT],
                    mCornerRadii[Corner.TOP_RIGHT],
                    mCornerRadii[Corner.BOTTOM_RIGHT],
                    mCornerRadii[Corner.BOTTOM_LEFT]
                )
            }

            applyColorMod()
        } else if (drawable is LayerDrawable) {
            // loop through layers to and set drawable attrs
            val ld: LayerDrawable = drawable
            var i = 0
            val layers: Int = ld.numberOfLayers
            while (i < layers) {
                updateAttrs(ld.getDrawable(i), scaleType)
                i++
            }
        }
    }

    @Deprecated("")
    override fun setBackgroundDrawable(background: Drawable?) {
        mBackgroundDrawable = background
        updateBackgroundDrawableAttrs(true)
        super.setBackgroundDrawable(mBackgroundDrawable)
    }

    var cornerRadius: Float
        /**
         * @return the largest corner radius.
         */
        get() = this.maxCornerRadius
        /**
         * Set the corner radii of all corners in px.
         *
         * @param radius the radius to set.
         */
        set(radius) {
            setCornerRadius(radius, radius, radius, radius)
        }

    val maxCornerRadius: Float
        /**
         * @return the largest corner radius.
         */
        get() {
            var maxRadius = 0f
            for (r in mCornerRadii!!) {
                maxRadius = max(r, maxRadius)
            }
            return maxRadius
        }

    /**
     * Get the corner radius of a specified corner.
     *
     * @param corner the corner.
     * @return the radius.
     */
    fun getCornerRadius(@Corner corner: Int): Float {
        return mCornerRadii!![corner]
    }

    /**
     * Set all the corner radii from a dimension resource id.
     *
     * @param resId dimension resource id of radii.
     */
    fun setCornerRadiusDimen(@DimenRes resId: Int) {
        val radius = resources.getDimension(resId)
        setCornerRadius(radius, radius, radius, radius)
    }

    /**
     * Set the corner radius of a specific corner from a dimension resource id.
     *
     * @param corner the corner to set.
     * @param resId the dimension resource id of the corner radius.
     */
    fun setCornerRadiusDimen(@Corner corner: Int, @DimenRes resId: Int) {
        setCornerRadius(corner, resources.getDimensionPixelSize(resId).toFloat())
    }

    /**
     * Set the corner radius of a specific corner in px.
     *
     * @param corner the corner to set.
     * @param radius the corner radius to set in px.
     */
    fun setCornerRadius(@Corner corner: Int, radius: Float) {
        if (mCornerRadii!![corner] == radius) {
            return
        }
        mCornerRadii[corner] = radius

        updateDrawableAttrs()
        updateBackgroundDrawableAttrs(false)
        invalidate()
    }

    /**
     * Set the corner radii of each corner individually. Currently only one unique nonzero value is
     * supported.
     *
     * @param topLeft radius of the top left corner in px.
     * @param topRight radius of the top right corner in px.
     * @param bottomRight radius of the bottom right corner in px.
     * @param bottomLeft radius of the bottom left corner in px.
     */
    fun setCornerRadius(topLeft: Float, topRight: Float, bottomLeft: Float, bottomRight: Float) {
        if (mCornerRadii!![Corner.TOP_LEFT] == topLeft && mCornerRadii[Corner.TOP_RIGHT] == topRight && mCornerRadii[Corner.BOTTOM_RIGHT] == bottomRight && mCornerRadii[Corner.BOTTOM_LEFT] == bottomLeft) {
            return
        }

        mCornerRadii[Corner.TOP_LEFT] = topLeft
        mCornerRadii[Corner.TOP_RIGHT] = topRight
        mCornerRadii[Corner.BOTTOM_LEFT] = bottomLeft
        mCornerRadii[Corner.BOTTOM_RIGHT] = bottomRight

        updateDrawableAttrs()
        updateBackgroundDrawableAttrs(false)
        invalidate()
    }

    fun setBorderWidth(@DimenRes resId: Int) {
        setBorderWidth(resources.getDimension(resId))
    }

    fun setBorderWidth(width: Float) {
        if (this.borderWidth == width) {
            return
        }

        this.borderWidth = width
        updateDrawableAttrs()
        updateBackgroundDrawableAttrs(false)
        invalidate()
    }

    @get:ColorInt
    var borderColor: Int
        get() = mBorderColor?.defaultColor ?: "#00000000".toColorInt()
        set(color) {
            setBorderColor(ColorStateList.valueOf(color))
        }

    val borderColors: ColorStateList?
        get() = mBorderColor

    fun setBorderColor(colors: ColorStateList?) {
        if (mBorderColor == colors) {
            return
        }

        mBorderColor = colors ?: ColorStateList.valueOf(RoundedDrawable.DEFAULT_BORDER_COLOR)
        updateDrawableAttrs()
        updateBackgroundDrawableAttrs(false)
        if (this.borderWidth > 0) {
            invalidate()
        }
    }

    var isOval: Boolean
        /**
         * Return true if this view should be oval and always set corner radii to half the height or
         * width.
         *
         * @return if this [RoundedImageView] is set to oval.
         */
        get() = mIsOval
        /**
         * Set if the drawable should ignore the corner radii set and always round the source to
         * exactly half the height or width.
         *
         * @param oval if this [RoundedImageView] should be oval.
         */
        set(oval) {
            mIsOval = oval
            updateDrawableAttrs()
            updateBackgroundDrawableAttrs(false)
            invalidate()
        }

    val tileModeX: TileMode
        get() = mTileModeX

    fun setTileModeX(tileModeX: TileMode) {
        if (this.mTileModeX == tileModeX) {
            return
        }

        this.mTileModeX = tileModeX
        updateDrawableAttrs()
        updateBackgroundDrawableAttrs(false)
        invalidate()
    }

    val tileModeY: TileMode
        get() = mTileModeY

    fun setTileModeY(tileModeY: TileMode) {
        if (this.mTileModeY == tileModeY) {
            return
        }

        this.mTileModeY = tileModeY
        updateDrawableAttrs()
        updateBackgroundDrawableAttrs(false)
        invalidate()
    }

    /**
     * If `true`, we will also round the background drawable according to the settings on this
     * ImageView.
     *
     * @return whether the background is mutated.
     */
    fun mutatesBackground(): Boolean {
        return mMutateBackground
    }

    /**
     * Set whether the [RoundedImageView] should round the background drawable according to
     * the settings in addition to the source drawable.
     *
     * @param mutate true if this view should mutate the background drawable.
     */
    fun mutateBackground(mutate: Boolean) {
        if (mMutateBackground == mutate) {
            return
        }

        mMutateBackground = mutate
        updateBackgroundDrawableAttrs(true)
        invalidate()
    }

    companion object {
        // Constants for tile mode attributes
        private const val TILE_MODE_UNDEFINED = -2
        private const val TILE_MODE_CLAMP = 0
        private const val TILE_MODE_REPEAT = 1
        private const val TILE_MODE_MIRROR = 2

        const val TAG: String = "RoundedImageView"
        const val DEFAULT_RADIUS: Float = 0f
        const val DEFAULT_BORDER_WIDTH: Float = 0f
        val DEFAULT_TILE_MODE: TileMode = TileMode.CLAMP
        private val SCALE_TYPES = arrayOf<ScaleType?>(
            ScaleType.MATRIX,
            ScaleType.FIT_XY,
            ScaleType.FIT_START,
            ScaleType.FIT_CENTER,
            ScaleType.FIT_END,
            ScaleType.CENTER,
            ScaleType.CENTER_CROP,
            ScaleType.CENTER_INSIDE
        )

        private fun parseTileMode(tileMode: Int): TileMode? {
            return when (tileMode) {
                TILE_MODE_CLAMP -> TileMode.CLAMP
                TILE_MODE_REPEAT -> TileMode.REPEAT
                TILE_MODE_MIRROR -> TileMode.MIRROR
                else -> null
            }
        }
    }
}