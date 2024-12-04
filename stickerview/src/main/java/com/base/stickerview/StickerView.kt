package com.base.stickerview

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.pow
import kotlin.math.sqrt

abstract class StickerView : FrameLayout {

    var onFlipListener: (() -> Unit)? = null
    var onDeleteListener: (() -> Unit)? = null
    var onEditListener: (() -> Unit)? = null
    var onDoubleClickListener: (() -> Unit)? = null

    fun setDeleteListener(listener: () -> Unit) {
        onDeleteListener = listener
    }

    fun setEditListener(listener: () -> Unit) {
        onEditListener = listener
    }

    fun setOnDoubleClick(listener: () -> Unit) {
        onDoubleClickListener = listener
    }

    private var ivBorder: BorderView? = null
    private var ivScale: ImageView? = null
    private var ivDelete: ImageView? = null
    private var ivFlip: ImageView? = null
    private var ivEdit: ImageView? = null

    // For scaling
    private var thisOrgX = -1f
    private var thisOrgY = -1f
    private var scaleOrgX = -1f
    private var scaleOrgY = -1f
    private var scaleOrgWidth = -1.0
    private var scaleOrgHeight = -1.0

    // For rotating
    private var rotateOrgX = -1f
    private var rotateOrgY = -1f
    private var rotateNewX = -1f
    private var rotateNewY = -1f

    // For moving
    private var moveOrgX = -1f
    private var moveOrgY = -1f

    private var centerX: Double = 0.toDouble()
    private var centerY: Double = 0.toDouble()

    val isFlip: Boolean
        get() = mainView.rotationY == -180f

    protected abstract val mainView: View

    private var firstTouch = false
    private var timeClick = 0L

    @SuppressLint("ClickableViewAccessibility")
    private val mTouchListener = OnTouchListener { view, event ->
        if (view.tag == "DraggableViewGroup") {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    if (firstTouch && System.currentTimeMillis() - timeClick <= 300) {
                        firstTouch = false
                        onDoubleClickListener?.invoke()
                    } else {
                        firstTouch = true
                        timeClick = System.currentTimeMillis()
                    }
                    Log.v(TAG, "sticker view action down")
                    moveOrgX = event.rawX
                    moveOrgY = event.rawY

                    setControlItemsHidden(false)
                }

                MotionEvent.ACTION_MOVE -> {
                    Log.v(TAG, "sticker view action move")
                    val offsetX = event.rawX - moveOrgX
                    val offsetY = event.rawY - moveOrgY
                    this@StickerView.x += offsetX
                    this@StickerView.y += offsetY
                    moveOrgX = event.rawX
                    moveOrgY = event.rawY
                }

                MotionEvent.ACTION_UP -> Log.v(TAG, "sticker view action up")
            }
        } else if (view.tag == IV_SCALE_TAG) {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    Log.v(TAG, "$IV_SCALE_TAG action down")

                    thisOrgX = this@StickerView.x
                    thisOrgY = this@StickerView.y

                    scaleOrgX = event.rawX
                    scaleOrgY = event.rawY
                    scaleOrgWidth = this@StickerView.layoutParams.width.toDouble()
                    scaleOrgHeight = this@StickerView.layoutParams.height.toDouble()

                    rotateOrgX = event.rawX
                    rotateOrgY = event.rawY

                    centerX =
                        (this@StickerView.x + (this@StickerView.parent as View).x + this@StickerView.width.toFloat() / 2).toDouble()

                    centerY =
                        this@StickerView.y.toDouble() + (this@StickerView.parent as View).y.toDouble() + (this@StickerView.height.toFloat() / 2).toDouble()
                }

                MotionEvent.ACTION_MOVE -> {
                    Log.v(TAG, "$IV_SCALE_TAG action move")

                    rotateNewX = event.rawX
                    rotateNewY = event.rawY

                    val angleDiff = abs(
                        atan2(
                            (event.rawY - scaleOrgY).toDouble(), (event.rawX - scaleOrgX).toDouble()
                        ) - atan2(scaleOrgY - centerY, scaleOrgX - centerX)
                    ) * 180 / Math.PI

                    Log.v(TAG, "angle_diff: $angleDiff")

                    val length1 =
                        getLength(centerX, centerY, scaleOrgX.toDouble(), scaleOrgY.toDouble())
                    val length2 =
                        getLength(centerX, centerY, event.rawX.toDouble(), event.rawY.toDouble())

                    val size = convertDpToPixel(SELF_SIZE_DP.toFloat(), context)
                    if (length2 > length1 && (angleDiff < 25 || abs(angleDiff - 180) < 25)) {
                        //scale up
                        val offsetX = abs(event.rawX - scaleOrgX).toDouble()
                        val offsetY = abs(event.rawY - scaleOrgY).toDouble()
                        var offset = offsetX.coerceAtLeast(offsetY)
                        offset = Math.round(offset).toDouble()
                        this@StickerView.layoutParams.width += offset.toInt()
                        this@StickerView.layoutParams.height += offset.toInt()
                        onScaling(true)
                        //DraggableViewGroup.this.setX((float) (getX() - offset / 2));
                        //DraggableViewGroup.this.setY((float) (getY() - offset / 2));
                    } else if (length2 < length1 && (angleDiff < 25 || abs(angleDiff - 180) < 25) && this@StickerView.layoutParams.width > size / 2 && this@StickerView.layoutParams.height > size / 2) {
                        //scale down
                        val offsetX = abs(event.rawX - scaleOrgX).toDouble()
                        val offsetY = abs(event.rawY - scaleOrgY).toDouble()
                        var offset = offsetX.coerceAtLeast(offsetY)
                        offset = Math.round(offset).toDouble()
                        this@StickerView.layoutParams.width -= offset.toInt()
                        this@StickerView.layoutParams.height -= offset.toInt()
                        onScaling(false)
                    }

                    //rotate

                    val angle = atan2(event.rawY - centerY, event.rawX - centerX) * 180 / Math.PI
                    Log.v(TAG, "log angle: $angle")

                    //setRotation((float) angle - 45);
                    rotation = angle.toFloat() - 45
                    Log.v(TAG, "getRotation(): $rotation")

                    onRotating()

                    rotateOrgX = rotateNewX
                    rotateOrgY = rotateNewY

                    scaleOrgX = event.rawX
                    scaleOrgY = event.rawY

                    postInvalidate()
                    requestLayout()
                }

                MotionEvent.ACTION_UP -> Log.v(TAG, "$IV_SCALE_TAG action up")
            }
        }
        true
    }

    protected val imageViewFlip: View?
        get() = ivFlip


    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
        context, attrs, defStyle
    ) {
        init(context)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun init(context: Context) {
        this.ivBorder = BorderView(context)
        this.ivScale = ImageView(context)
        this.ivDelete = ImageView(context)
        this.ivFlip = ImageView(context)
        this.ivEdit = ImageView(context)

        this.ivScale!!.setImageResource(R.drawable.zoominout)
        this.ivDelete!!.setImageResource(R.drawable.remove)
        this.ivFlip!!.setImageResource(R.drawable.flip)
        this.ivEdit!!.setImageResource(R.drawable.edit)

        this.tag = "DraggableViewGroup"
        this.ivBorder!!.tag = IV_BORDER_TAG
        this.ivScale!!.tag = IV_SCALE_TAG
        this.ivDelete!!.tag = IV_DELETE_TAG
        this.ivFlip!!.tag = IV_FLIP_TAG
        this.ivEdit!!.tag = IV_EDIT_TAG

        val margin = convertDpToPixel(BUTTON_SIZE_DP.toFloat(), getContext()) / 2
        val size = convertDpToPixel(SELF_SIZE_DP.toFloat(), getContext())

        //todo: gravity and position of tag flip-delete-scale
        val thisParams = LayoutParams(size, size)
//        val thisParams = LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        thisParams.gravity = Gravity.CENTER

        val ivMainParams =
            LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        ivMainParams.setMargins(margin / 2, margin / 2, margin / 2, margin / 2)

        val ivBorderParams = LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
        )
        ivBorderParams.setMargins(margin / 2, margin / 2, margin / 2, margin / 2)

        val bottomLeftParams = LayoutParams(
            convertDpToPixel(BUTTON_SIZE_DP.toFloat() * 1.5f, getContext()),
            convertDpToPixel(BUTTON_SIZE_DP.toFloat(), getContext())
        )
        bottomLeftParams.gravity = Gravity.BOTTOM or Gravity.START

        val bottomRightParams = LayoutParams(
            convertDpToPixel(BUTTON_SIZE_DP.toFloat(), getContext()),
            convertDpToPixel(BUTTON_SIZE_DP.toFloat(), getContext())
        )
        bottomRightParams.gravity = Gravity.BOTTOM or Gravity.END

        val topRightParams = LayoutParams(
            convertDpToPixel(BUTTON_SIZE_DP.toFloat(), getContext()),
            convertDpToPixel(BUTTON_SIZE_DP.toFloat(), getContext())
        )
        topRightParams.gravity = Gravity.TOP or Gravity.END

        val topLeftParams = LayoutParams(
            convertDpToPixel(BUTTON_SIZE_DP.toFloat(), getContext()),
            convertDpToPixel(BUTTON_SIZE_DP.toFloat(), getContext())
        )
        topLeftParams.gravity = Gravity.TOP or Gravity.START

        this.layoutParams = thisParams
        this.addView(mainView, ivMainParams)
        this.addView(ivBorder, ivBorderParams)
        this.addView(ivScale, bottomRightParams)
        this.addView(ivDelete, topLeftParams)
        this.addView(ivFlip, topRightParams)
        this.addView(ivEdit, bottomLeftParams)

        this.setOnTouchListener(mTouchListener)
        this.ivScale!!.setOnTouchListener(mTouchListener)
        this.ivDelete!!.setOnClickListener {
            if (this@StickerView.parent != null) {
                val myCanvas = this@StickerView.parent as ViewGroup
                myCanvas.removeView(this@StickerView)
                onDeleteListener?.invoke()
            }
        }
        this.ivFlip!!.setOnClickListener {
            Log.v(TAG, "flip the view")

            val mainView = mainView
            mainView.rotationY = if (mainView.rotationY == -180f) 0f else -180f
            mainView.invalidate()
            requestLayout()
            onFlipListener?.invoke()
        }
        this.ivEdit!!.setOnClickListener {
            Log.v(TAG, "edit this sticker")
            onEditListener?.invoke()
        }
    }

    private fun getLength(x1: Double, y1: Double, x2: Double, y2: Double): Double {
        return sqrt((y2 - y1).pow(2.0) + (x2 - x1).pow(2.0))
    }

    private fun getRelativePos(absX: Float, absY: Float): FloatArray {
        Log.v("ken", "getRelativePos getX:" + (this.parent as View).x)
        Log.v("ken", "getRelativePos getY:" + (this.parent as View).y)
        val pos = floatArrayOf(absX - (this.parent as View).x, absY - (this.parent as View).y)
        Log.v(TAG, "getRelativePos absY:$absY")
        Log.v(TAG, "getRelativePos relativeY:" + pos[1])
        return pos
    }

    fun setControlItemsHidden(isHidden: Boolean) {
        if (isHidden) {
            ivBorder!!.visibility = View.INVISIBLE
            ivScale!!.visibility = View.INVISIBLE
            ivDelete!!.visibility = View.INVISIBLE
            ivFlip!!.visibility = View.INVISIBLE
            ivEdit!!.visibility = View.INVISIBLE
        } else {
            ivBorder!!.visibility = View.VISIBLE
            ivScale!!.visibility = View.VISIBLE
            ivDelete!!.visibility = View.VISIBLE
            ivFlip!!.visibility = View.VISIBLE
            ivEdit!!.visibility = View.VISIBLE
        }
    }

    protected open fun onScaling(scaleUp: Boolean) {}

    protected fun onRotating() {}

    private inner class BorderView : View {

        constructor(context: Context) : super(context)

        constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

        constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
            context, attrs, defStyle
        )

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            // Draw sticker border

            val params = this.layoutParams as LayoutParams

            Log.v(TAG, "params.leftMargin: " + params.leftMargin)

            val border = Rect()
            border.left = this.left - params.leftMargin
            border.top = this.top - params.topMargin
            border.right = this.right - params.rightMargin
            border.bottom = this.bottom - params.bottomMargin
            val borderPaint = Paint()
            borderPaint.strokeWidth = 8f
            borderPaint.color = context.getColorById(R.color.n0)
            borderPaint.style = Paint.Style.STROKE
            borderPaint.setPathEffect(DashPathEffect(floatArrayOf(10f, 10f), 0f))
            canvas.drawRect(border, borderPaint)

        }
    }

    companion object {
        const val TAG = "doanvv.sticker.view"

        private const val BUTTON_SIZE_DP = 28
        const val SELF_SIZE_DP = 200

        const val IV_SCALE_TAG = "iv_scale"
        const val IV_BORDER_TAG = "iv_border"
        const val IV_DELETE_TAG = "iv_delete"
        const val IV_FLIP_TAG = "iv_flip"
        const val IV_EDIT_TAG = "iv_edit"

        private fun convertDpToPixel(dp: Float, context: Context): Int {
            val resources = context.resources
            val metrics = resources.displayMetrics
            val px = dp * (metrics.densityDpi / 160f)
            return px.toInt()
        }
    }
}