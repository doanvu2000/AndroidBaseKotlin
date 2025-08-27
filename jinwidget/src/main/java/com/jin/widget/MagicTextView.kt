package com.jin.widget

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.TypedArray
import android.graphics.Bitmap
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Paint.Join
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.Pair
import androidx.appcompat.widget.AppCompatTextView
import java.util.Locale
import java.util.WeakHashMap

class MagicTextView : AppCompatTextView {
    private var outerShadows: ArrayList<Shadow>? = null
    private var innerShadows: ArrayList<Shadow>? = null

    private var canvasStore: WeakHashMap<String?, Pair<Canvas, Bitmap>?>? = null

    private var tempCanvas: Canvas? = null
    private var tempBitmap: Bitmap? = null

    private var foregroundDrawable: Drawable? = null

    private var strokeWidth = 0f
    private var strokeColor: Int? = null
    private var strokeJoin: Join? = null
    private var strokeMiter = 0f

    private var lockedCompoundPadding: IntArray = intArrayOf()
    private var frozen = false

    constructor(context: Context) : super(context) {
        init(null)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        init(attrs)
    }

    fun init(attrs: AttributeSet?) {
        outerShadows = ArrayList<Shadow>()
        innerShadows = ArrayList<Shadow>()
        if (canvasStore == null) {
            canvasStore = WeakHashMap<String?, Pair<Canvas, Bitmap>?>()
        }

        if (attrs != null) {
            var a: TypedArray? = null
            try {
                a = getContext().obtainStyledAttributes(attrs, R.styleable.MagicTextView)

                //                int typefaceResId = a.getResourceId(R.styleable.MagicTextView_typeface_mg, R.font.font_bold); // Lấy ID của font
//                if (typefaceResId != 0) {
//                    Typeface typeface = ResourcesCompat.getFont(getContext(), typefaceResId);
//                    setTypeface(typeface);
//                }
//                if (a.hasValue(R.styleable.MagicTextView_foreground)) {
//                    Drawable foreground = a.getDrawable(R.styleable.MagicTextView_foreground);
//                    if (foreground != null) {
//                        this.setForegroundDrawable(foreground);
//                    } else {
//                        this.setTextColor(a.getColor(R.styleable.MagicTextView_foreground, 0xff000000));
//                    }
//                }
//
//                if (a.hasValue(R.styleable.MagicTextView_background)) {
//                    Drawable background = a.getDrawable(R.styleable.MagicTextView_background);
//                    if (background != null) {
//                        this.setBackgroundDrawable(background);
//                    } else {
//                        this.setBackgroundColor(a.getColor(R.styleable.MagicTextView_background, 0xff000000));
//                    }
//                }
                if (a.hasValue(R.styleable.MagicTextView_innerShadowColor)) {
                    this.addInnerShadow(
                        a.getDimensionPixelSize(R.styleable.MagicTextView_innerShadowRadius, 0)
                            .toFloat(),
                        a.getDimensionPixelOffset(R.styleable.MagicTextView_innerShadowDx, 0)
                            .toFloat(),
                        a.getDimensionPixelOffset(R.styleable.MagicTextView_innerShadowDy, 0)
                            .toFloat(),
                        a.getColor(R.styleable.MagicTextView_innerShadowColor, -0x1000000)
                    )
                }

                if (a.hasValue(R.styleable.MagicTextView_outerShadowColor)) {
                    this.addOuterShadow(
                        a.getDimensionPixelSize(R.styleable.MagicTextView_outerShadowRadius, 0)
                            .toFloat(),
                        a.getDimensionPixelOffset(R.styleable.MagicTextView_outerShadowDx, 0)
                            .toFloat(),
                        a.getDimensionPixelOffset(R.styleable.MagicTextView_outerShadowDy, 0)
                            .toFloat(),
                        a.getColor(R.styleable.MagicTextView_outerShadowColor, -0x1000000)
                    )
                }

                if (a.hasValue(R.styleable.MagicTextView_strokeColorMg)) {
                    val strokeWidth =
                        a.getDimensionPixelSize(R.styleable.MagicTextView_strokeWidthMg, 1)
                            .toFloat()
                    val strokeColor =
                        a.getColor(R.styleable.MagicTextView_strokeColorMg, -0x1000000)
                    val strokeMiter =
                        a.getDimensionPixelSize(R.styleable.MagicTextView_strokeMiterMg, 10)
                            .toFloat()
                    val strokeJoin = when (a.getInt(R.styleable.MagicTextView_strokeJoinStyle, 0)) {
                        (0) -> Join.MITER
                        (1) -> Join.BEVEL
                        (2) -> Join.ROUND
                        else -> null
                    }
                    this.setStroke(strokeWidth, strokeColor, strokeJoin, strokeMiter)
                }
            } finally {
                if (a != null) {
                    a.recycle()
                }
            }
        }
        if (!innerShadows!!.isEmpty() || foregroundDrawable != null
        ) {
            setLayerType(LAYER_TYPE_SOFTWARE, null)
        }
    }

    fun setStroke(width: Float, color: Int, join: Join?, miter: Float) {
        strokeWidth = width
        strokeColor = color
        strokeJoin = join
        strokeMiter = miter
    }

    fun setStroke(width: Float, color: Int) {
        setStroke(width, color, Join.MITER, 10f)
    }

    fun addOuterShadow(r: Float, dx: Float, dy: Float, color: Int) {
        var r = r
        if (r == 0f) {
            r = 0.0001f
        }
        outerShadows!!.add(Shadow(r, dx, dy, color))
    }

    fun addInnerShadow(r: Float, dx: Float, dy: Float, color: Int) {
        var r = r
        if (r == 0f) {
            r = 0.0001f
        }
        innerShadows!!.add(Shadow(r, dx, dy, color))
    }

    fun clearInnerShadows() {
        innerShadows!!.clear()
    }

    fun clearOuterShadows() {
        outerShadows!!.clear()
    }

    fun setForegroundDrawable(d: Drawable?) {
        this.foregroundDrawable = d
    }

    override fun getForeground(): Drawable? {
        return if (this.foregroundDrawable == null) this.foregroundDrawable else ColorDrawable(this.getCurrentTextColor())
    }


    @SuppressLint("DrawAllocation")
    public override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        freeze()
        val restoreBackground = this.getBackground()
        val restoreDrawables = this.getCompoundDrawables()
        val restoreColor = this.getCurrentTextColor()

        this.setCompoundDrawables(null, null, null, null)

        for (shadow in outerShadows!!) {
            this.setShadowLayer(shadow.r, shadow.dx, shadow.dy, shadow.color)
            super.onDraw(canvas)
        }
        this.setShadowLayer(0f, 0f, 0f, 0)
        this.setTextColor(restoreColor)

        if (this.foregroundDrawable != null && this.foregroundDrawable is BitmapDrawable) {
            generateTempCanvas()
            super.onDraw(tempCanvas!!)
            val paint = (this.foregroundDrawable as BitmapDrawable).getPaint()
            paint.setXfermode(PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP))
            this.foregroundDrawable!!.setBounds(canvas.getClipBounds())
            (this.foregroundDrawable as BitmapDrawable).draw(tempCanvas!!)
            canvas.drawBitmap(tempBitmap!!, 0f, 0f, null)
            tempCanvas!!.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
        }

        if (strokeColor != null) {
            val paint = this.getPaint()
            paint.setStyle(Paint.Style.STROKE)
            paint.setStrokeJoin(strokeJoin)
            paint.setStrokeMiter(strokeMiter)
            this.setTextColor(strokeColor!!)
            paint.setStrokeWidth(strokeWidth)
            super.onDraw(canvas)
            paint.setStyle(Paint.Style.FILL)
            this.setTextColor(restoreColor)
        }
        if (!innerShadows!!.isEmpty()) {
            generateTempCanvas()
            val paint = this.getPaint()
            for (shadow in innerShadows!!) {
                this.setTextColor(shadow.color)
                super.onDraw(tempCanvas!!)
                this.setTextColor(-0x1000000)
                paint.setXfermode(PorterDuffXfermode(PorterDuff.Mode.DST_OUT))
                paint.setMaskFilter(BlurMaskFilter(shadow.r, BlurMaskFilter.Blur.NORMAL))

                tempCanvas!!.save()
                tempCanvas!!.translate(shadow.dx, shadow.dy)
                super.onDraw(tempCanvas!!)
                tempCanvas!!.restore()
                canvas.drawBitmap(tempBitmap!!, 0f, 0f, null)
                tempCanvas!!.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)

                paint.setXfermode(null)
                paint.setMaskFilter(null)
                this.setTextColor(restoreColor)
                this.setShadowLayer(0f, 0f, 0f, 0)
            }
        }


        this.setCompoundDrawablesWithIntrinsicBounds(
            restoreDrawables[0],
            restoreDrawables[1],
            restoreDrawables[2],
            restoreDrawables[3]
        )
        this.setBackgroundDrawable(restoreBackground)
        this.setTextColor(restoreColor)

        unfreeze()
    }

    private fun generateTempCanvas() {
        val key = String.format(Locale.ENGLISH, "%dx%d", getWidth(), getHeight())
        val stored = canvasStore!!.get(key)
        if (stored != null) {
            tempCanvas = stored.first
            tempBitmap = stored.second
        } else {
            tempCanvas = Canvas()
            tempBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888)
            tempCanvas!!.setBitmap(tempBitmap)
            canvasStore!!.put(key, Pair<Canvas, Bitmap>(tempCanvas, tempBitmap))
        }
    }


    // Keep these things locked while onDraw in processing
    fun freeze() {
        lockedCompoundPadding = intArrayOf(
            getCompoundPaddingLeft(),
            getCompoundPaddingRight(),
            getCompoundPaddingTop(),
            getCompoundPaddingBottom()
        )
        frozen = true
    }

    fun unfreeze() {
        frozen = false
    }


    override fun requestLayout() {
        if (!frozen) super.requestLayout()
    }

    override fun postInvalidate() {
        if (!frozen) super.postInvalidate()
    }

    override fun postInvalidate(left: Int, top: Int, right: Int, bottom: Int) {
        if (!frozen) super.postInvalidate(left, top, right, bottom)
    }

    override fun invalidate() {
        if (!frozen) super.invalidate()
    }

    override fun invalidate(rect: Rect?) {
        if (!frozen) super.invalidate(rect)
    }

    override fun invalidate(l: Int, t: Int, r: Int, b: Int) {
        if (!frozen) super.invalidate(l, t, r, b)
    }

    override fun getCompoundPaddingLeft(): Int {
        return if (!frozen) super.getCompoundPaddingLeft() else lockedCompoundPadding[0]
    }

    override fun getCompoundPaddingRight(): Int {
        return if (!frozen) super.getCompoundPaddingRight() else lockedCompoundPadding[1]
    }

    override fun getCompoundPaddingTop(): Int {
        return if (!frozen) super.getCompoundPaddingTop() else lockedCompoundPadding[2]
    }

    override fun getCompoundPaddingBottom(): Int {
        return if (!frozen) super.getCompoundPaddingBottom() else lockedCompoundPadding[3]
    }

    class Shadow(var r: Float, var dx: Float, var dy: Float, var color: Int)
}