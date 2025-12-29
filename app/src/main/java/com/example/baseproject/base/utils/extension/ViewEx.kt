package com.example.baseproject.base.utils.extension

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Outline
import android.graphics.Path
import android.graphics.RectF
import android.graphics.drawable.LayerDrawable
import android.os.Build
import android.util.Log
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.view.ViewTreeObserver
import android.view.animation.AnimationUtils
import android.view.animation.ScaleAnimation
import android.view.inputmethod.InputMethodManager
import android.widget.SeekBar
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresApi
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.createBitmap
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import com.base.stickerview.StickerImageView
import com.example.baseproject.R
import com.example.baseproject.base.utils.util.AppLogger
import com.example.baseproject.base.utils.util.Constants
import com.google.android.material.card.MaterialCardView
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import com.google.android.material.tabs.TabLayout
import com.jin.widget.utils.pxToSpF

//region Keyboard Management

/**
 * Ẩn bàn phím
 */
fun View.hideKeyboard() {
    val inputMethodManager =
        context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
}

/**
 * Hiển thị bàn phím
 */
fun View.showKeyboard() {
    val inputMethodManager =
        context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
}

/**
 * Focus và hiển thị bàn phím
 */
fun View.focusAndShowKeyboard() {
    /**
     * This is to be called when the window already has focus.
     */
    fun View.showTheKeyboardNow() {
        if (!isFocused) return
        postDelayed(
            {
                // We still post the call, just in case we are being notified of the windows focus
                // but InputMethodManager didn't get properly setup yet.
                val imm =
                    context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                imm?.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
            },
            200
        )
    }

    requestFocus()
    if (hasWindowFocus()) {
        // No need to wait for the window to get focus.
        showTheKeyboardNow()
    } else {
        // We need to wait until the window gets focus.
        val listener = object : ViewTreeObserver.OnWindowFocusChangeListener {
            override fun onWindowFocusChanged(hasFocus: Boolean) {
                // This notification will arrive just before the InputMethodManager gets set up.
                if (hasFocus) {
                    this@focusAndShowKeyboard.showTheKeyboardNow()
                    // It's very important to remove this listener once we are done.
                    viewTreeObserver.removeOnWindowFocusChangeListener(this)
                }
            }
        }
        viewTreeObserver.addOnWindowFocusChangeListener(listener)
    }
}

/**
 * Call in Activity/Fragment, param is rootView.
 */
fun View.detectSoftKeyboardVisibleState(keyboardState: (isShow: Boolean, keyboardHeight: Int) -> Unit) {
    ViewCompat.setOnApplyWindowInsetsListener(rootView) { view, insets ->
        val imeVisible = insets.isVisible(WindowInsetsCompat.Type.ime())
        val imeHeight = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom

        AppLogger.d(Constants.TAG, "isShowKeyboard: $imeVisible")
        AppLogger.d(Constants.TAG, "keyboardHeight: $imeHeight")

        keyboardState.invoke(imeVisible, imeHeight)
        insets
    }
}

//endregion

//region View Visibility & State Management

/**
 * Ẩn view (INVISIBLE)
 */
fun View.hide() {
    if (!isHide) {
        this.visibility = View.INVISIBLE
    }
}

/**
 * Hiển thị view (VISIBLE)
 */
fun View.show() {
    if (!isShow) {
        this.visibility = View.VISIBLE
    }
}

/**
 * Ẩn view hoàn toàn (GONE)
 */
fun View.gone() {
    if (!isGone) {
        this.visibility = View.GONE
    }
}

/**
 * Show hoặc Gone view
 */
fun View.showOrGone(isShow: Boolean) {
    if (isShow) {
        show()
    } else {
        gone()
    }
}

/**
 * Show hoặc Hide view
 */
fun View.showOrHide(isShow: Boolean) {
    if (isShow) {
        show()
    } else {
        hide()
    }
}

/**
 * Các extension properties để check visibility
 */
val View.isShow: Boolean
    get() = this.isVisible

val View.isHide: Boolean
    get() = this.isInvisible

val View.isGone: Boolean
    get() = this.visibility == View.GONE

//endregion

//region Click Management

/**
 * Disable view temporarily
 */
fun View.disableView() {
    this.isClickable = false
    this.postDelayed({ this.isClickable = true }, 500)
}

/**
 * Safe click listener để prevent double click
 */
class SafeClickListener(val onSafeClickListener: (View) -> Unit) : View.OnClickListener {
    override fun onClick(v: View) {
        v.disableView()
        onSafeClickListener(v)
    }
}

/**
 * Set safe click để tránh double click
 */
fun View.setOnSafeClick(onSafeClickListener: (View) -> Unit) {
    val safeClick = SafeClickListener {
        onSafeClickListener(it)
    }
    setOnClickListener(safeClick)
}

//endregion

//region Animation & Effects

/**
 * Click animation
 */
fun clickAnimation(mContext: Context?, view: View) {
    if (mContext != null) {
        val scaleAnimation = ScaleAnimation(
            0.97f, 1f, 0.97f, 1f,
            0.5f, 0.5f
        ).apply {
            duration = 100
        }

        view.startAnimation(scaleAnimation)
//        val myAnim = AnimationUtils.loadAnimation(mContext, R.anim.bounce)
//        view.startAnimation(myAnim)
    }
}

/**
 * Click animation cho view
 */
fun View.clickAnimation() {
    try {
        if (!isAttachedToWindow) {
            return
        }
        context ?: return
        clickAnimation(context, this)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

/**
 * Click với animation
 */
fun View.clickWithAnimation(action: (View) -> Unit) {
    setOnClickListener {
        clickAnimation()
        action.invoke(this)
    }
}

/**
 * Show với animation
 */
fun View.showWithAnimation() {
    this.visibility = View.VISIBLE
    this.startAnimation(AnimationUtils.loadAnimation(this.context, R.anim.slide_in_bottom))
}

/**
 * Gone với animation
 */
fun View.goneWithAnimation() {
    this.visibility = View.GONE
    this.startAnimation(AnimationUtils.loadAnimation(this.context, R.anim.slide_out_bottom))
}

//endregion

//region Size & Layout Management

/**
 * Set size cho view
 */
fun View.setSize(width: Int, height: Int) {
    val rootParam = this.layoutParams
    rootParam.width = width
    rootParam.height = height
    layoutParams = rootParam
    requestLayout()
}

/**
 * Remove view khỏi parent
 */
fun View?.removeSelf() {
    this ?: return
    val parentView = parent as? ViewGroup ?: return
    parentView.removeView(this)
}

//endregion

//region Bitmap & Canvas

/**
 * Lấy bitmap từ view
 */
fun View.getBitmapFromView(done: (Bitmap) -> Unit) {
    post {
        val b = createBitmap(width, height)
        val c = Canvas(b)
        layout(left, top, right, bottom)
        draw(c)
        done(b)
    }
}

//endregion

//region Touch & Gesture Handling

/**
 * Set touch listener với các action
 */
@SuppressLint("ClickableViewAccessibility")
fun View.setTouchListener(
    consumeEvent: Boolean = false,
    onActionDown: (() -> Unit)? = null,
    onActionUp: (() -> Unit)? = null,
    onActionMove: (() -> Unit)? = null,
    onActionScroll: (() -> Unit)? = null,
) {
    setOnTouchListener { v, event ->
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                onActionDown?.invoke()
            }

            MotionEvent.ACTION_UP -> {
                onActionUp?.invoke()
            }

            MotionEvent.ACTION_MOVE -> {
                onActionMove?.invoke()
            }

            MotionEvent.ACTION_SCROLL -> {
                onActionScroll?.invoke()
            }
        }
        consumeEvent
    }
}

/**
 * Set touch action với options
 */
@SuppressLint("ClickableViewAccessibility")
fun View.setOnTouchAction(
    isDisableClick: Boolean = false,
    onDownAction: (() -> Unit)? = null,
    onUpAction: (() -> Unit)? = null,
    onMoveAction: (() -> Unit)? = null
) {
    setOnTouchListener { _, event ->
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                onDownAction?.invoke()
            }

            MotionEvent.ACTION_UP -> {
                onUpAction?.invoke()
            }

            MotionEvent.ACTION_MOVE -> {
                onMoveAction?.invoke()
            }
        }
        isDisableClick
    }
}

//endregion

//region SeekBar Extensions

/**
 * Set progress change listener cho SeekBar
 */
fun SeekBar.setOnProgressChange(
    action: (
        seekBar: SeekBar?, progress: Int, fromUser: Boolean
    ) -> Unit
) {
    this.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            action.invoke(seekBar, progress, fromUser)
        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) {}

        override fun onStopTrackingTouch(seekBar: SeekBar?) {}
    })
}

/**
 * Set full listener cho SeekBar
 */
fun SeekBar.setListener(
    onProgressChanged: ((seekBar: SeekBar?, progress: Int, fromUser: Boolean) -> Unit)? = null,
    onStartTrackingTouch: ((seekBar: SeekBar?) -> Unit)? = null,
    onStopTrackingTouch: ((seekBar: SeekBar?) -> Unit)? = null
) {
    setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            onProgressChanged?.invoke(seekBar, progress, fromUser)
        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) {
            onStartTrackingTouch?.invoke(seekBar)
        }

        override fun onStopTrackingTouch(seekBar: SeekBar?) {
            onStopTrackingTouch?.invoke(seekBar)
        }
    })
}

/**
 * Set track source cho SeekBar
 */
fun SeekBar.setTrackSrc(@DrawableRes drawable: Int) {
    val layerDrawable = ResourcesCompat.getDrawable(resources, drawable, null) as LayerDrawable
    progressDrawable = layerDrawable
}

/**
 * Set thumb source cho SeekBar
 */
fun SeekBar.setThumbSrc(@DrawableRes drawable: Int) {
    val thumbDrawable = ResourcesCompat.getDrawable(resources, drawable, null)
    thumb = thumbDrawable
}

//endregion

//region Material Components

/**
 * Remove shadow cho MaterialCardView
 */
fun MaterialCardView.removeShadow() {
    this.cardElevation = 0f
}

/**
 * Add shadow cho MaterialCardView
 */
fun MaterialCardView.addShadow(elevation: Float) {
    this.cardElevation = elevation.dpf
}

/**
 * Create tab cho TabLayout
 */
fun TabLayout.createTab(tabName: String): TabLayout.Tab {
    return newTab().setText(tabName)
}

//endregion

//region Sticker View

/**
 * Load source cho StickerImageView
 */
fun StickerImageView.loadSrc(src: Any) {
    ivMain?.loadSrc(src)
}

//endregion

//region Rounded Corners

/**
 * Set rounded corner cho view (tất cả 4 góc)
 * @param roundedCorner: pass int number, it will convert to px
 */
fun View.setRoundedCornerView(roundedCorner: Int) {
    try {
        outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View?, outline: Outline?) {
                view?.let { it ->
                    outline?.setRoundRect(
                        0, 0, it.width, it.height, roundedCorner.dpToPx().toFloat()
                    )
                }
            }
        }
        clipToOutline = true
    } catch (e: Exception) {
        Log.e(Constants.TAG, "Error when set outlineProvider of view - $e")
    }
}

/**
 * Thiết lập độ cong khác nhau cho các góc của View.
 * Các giá trị bán kính được truyền vào dưới dạng Int (dp) và sẽ được chuyển đổi sang px.
 *
 * @param topLeft Bán kính góc trên bên trái (dp).
 * @param topRight Bán kính góc trên bên phải (dp).
 * @param bottomLeft Bán kính góc dưới bên trái (dp).
 * @param bottomRight Bán kính góc dưới bên phải (dp).
 */
@RequiresApi(Build.VERSION_CODES.R)
fun View.setRoundedCorners(
    topLeft: Int = 0, topRight: Int = 0, bottomLeft: Int = 0, bottomRight: Int = 0
) {
    try {
        outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View?, outline: Outline?) {
                if (view == null || outline == null) return

                // Chuyển đổi dp sang px (Float)
                val tl = topLeft.dpToPx().toFloat()
                val tr = topRight.dpToPx().toFloat()
                val bl = bottomLeft.dpToPx().toFloat()
                val br = bottomRight.dpToPx().toFloat()

                // Mảng radii theo thứ tự: topLeft, topRight, bottomRight, bottomLeft
                // Mỗi góc cần 2 giá trị (X và Y), thường là giống nhau.
                val radii = floatArrayOf(
                    tl, tl,     // Top left x, y
                    tr, tr,     // Top right x, y
                    br, br,     // Bottom right x, y
                    bl, bl      // Bottom left x, y
                )

                // Tạo Path
                val path = Path()
                val rect = RectF(0f, 0f, view.width.toFloat(), view.height.toFloat())
                path.addRoundRect(
                    rect, radii, Path.Direction.CW
                ) // CW = Clockwise (Chiều kim đồng hồ)

                // Set path cho outline
                outline.setPath(path)
            }
        }
        clipToOutline = true // Rất quan trọng để clipping hoạt động
    } catch (e: Exception) {
        AppLogger.e("ViewExtension", "Error setting rounded corners: $e")
    }
}

/**
 * Thiết lập độ cong khác nhau cho các góc của View bằng cách sử dụng MaterialShapeDrawable làm background.
 * Yêu cầu thư viện Material Components.
 *
 * @param topLeft Bán kính góc trên bên trái (dp).
 * @param topRight Bán kính góc trên bên phải (dp).
 * @param bottomLeft Bán kính góc dưới bên trái (dp).
 * @param bottomRight Bán kính góc dưới bên phải (dp).
 * @param backgroundColor Màu nền (tùy chọn).
 * @param strokeWidth Độ rộng đường viền (dp, tùy chọn).
 * @param strokeColor Màu đường viền (tùy chọn).
 * @sample: myView.setRoundedCornersMaterial(topLeft = 16, topRight = 8, backgroundColor = Color.RED)
 */
fun View.setRoundedCornersMaterial(
    topLeft: Int = 0,
    topRight: Int = 0,
    bottomLeft: Int = 0,
    bottomRight: Int = 0,
    backgroundColor: Int? = null,
    strokeWidth: Int = 0,
    strokeColor: Int? = null
) {
    val tl = topLeft.dpToPx().toFloat()
    val tr = topRight.dpToPx().toFloat()
    val bl = bottomLeft.dpToPx().toFloat()
    val br = bottomRight.dpToPx().toFloat()

    // Xây dựng mô hình hình dạng
    val shapeAppearanceModel = ShapeAppearanceModel.builder()
        .setTopLeftCorner(CornerFamily.ROUNDED, tl)
        .setTopRightCorner(CornerFamily.ROUNDED, tr)
        .setBottomLeftCorner(CornerFamily.ROUNDED, bl)
        .setBottomRightCorner(CornerFamily.ROUNDED, br).build()

    // Tạo MaterialShapeDrawable
    val shapeDrawable = MaterialShapeDrawable(shapeAppearanceModel).apply {
        // Đặt màu nền
        backgroundColor?.let { color ->
            fillColor = android.content.res.ColorStateList.valueOf(color)
        } ?: run {
            // Nếu không có màu nền, đặt màu fill mặc định là trong suốt
            fillColor =
                android.content.res.ColorStateList.valueOf(android.graphics.Color.TRANSPARENT)
        }

        // Đặt đường viền
        if (strokeWidth > 0 && strokeColor != null) {
            setStroke(strokeWidth.dpToPx().toFloat(), strokeColor)
        }
    }

    // Đặt làm background
    background = shapeDrawable

    // MaterialShapeDrawable thường xử lý clipping tốt hơn
    outlineProvider = ViewOutlineProvider.BACKGROUND
    clipToOutline = true
}

//endregion

fun TextView.changeTextSize(size: Int) {
    changeTextSize(size.toFloat())
}

fun TextView.changeTextSize(size: Float) {
    setTextSize(TypedValue.COMPLEX_UNIT_SP, size.pxToSpF)
}