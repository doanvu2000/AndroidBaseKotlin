package com.jin.widget.progress.capsuleprogress

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.core.content.withStyledAttributes
import androidx.core.graphics.toColorInt
import com.jin.widget.R
import kotlin.math.max
import kotlin.math.min

/**
 * A custom `View` that displays progress in a stylized capsule or pill shape.
 *
 * This progress bar has a distinct visual appearance composed of three layers:
 * 1.  An outer "outline" layer, which also defines the capsule shape.
 * 2.  An inner "background" layer, visible where the progress has not yet filled.
 * 3.  The "progress" layer itself, which fills from left to right based on the current progress value.
 *
 * The thickness of the border can be adjusted internally, creating a pseudo-3D or sticker-like effect.
 *
 * Key properties like `progress`, `max`, `progressColor`, `barBackgroundColor`, and `outlineColor`
 * can be set both in XML layouts and programmatically.
 *
 * The progress can be updated instantly by setting the [progress] property, or with a smooth
 * animation using the [setProgressAnimated] method.
 *
 * @attr ref R.styleable#CapsuleProgressBar_ccpb_progress The current progress value.
 * @attr ref R.styleable#CapsuleProgressBar_cpb_max The maximum progress value.
 * @attr ref R.styleable#CapsuleProgressBar_cpb_backgroundColor The color of the bar's background area.
 * @attr ref R.styleable#CapsuleProgressBar_cpb_progressColor The color of the progress indicator.
 * @attr ref R.styleable#CapsuleProgressBar_cpb_outlineColor The color of the outer border/outline.
 *
 * @constructor Creates an instance of the CapsuleProgressBar.
 * @param context The Context the view is running in, through which it can access the current theme, resources, etc.
 * @param attrs The attributes of the XML tag that is inflating the view.
 */
class CapsuleProgressBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var _progress: Int = 0
    var progress: Int
        get() = _progress
        set(value) {
            val clampedValue = value.coerceIn(0, _max)
            if (_progress != clampedValue) {
                _progress = clampedValue
                invalidate() // Yêu cầu vẽ lại View
            }
        }

    private var _max: Int = 100
    var max: Int
        get() = _max
        set(value) {
            _max = max(1, value)
            progress = _progress
            invalidate()
        }

    private var barBackgroundColor: Int = Color.WHITE
    private var progressColor: Int = "#FF8846".toColorInt()
    private var outlineColor: Int = Color.BLACK

    // --- Độ dày viền (tính bằng pixel) ---
    private var borderTopPx: Float = 0f
    private var borderSidesPx: Float = 0f // Dùng chung cho trái và phải
    private var borderBottomPx: Float = 0f

    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
    private val outlinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    } // Dùng để vẽ nền đen ngoài cùng

    private val outerRect = RectF()
    private val innerRect = RectF()
    private val progressClipRect = RectF()

    // --- Animation ---
    private var progressAnimator: ObjectAnimator? = null
    private val defaultAnimationDuration = 300L

    init {
        calculateBorderPixels()
        // Đọc các thuộc tính tùy chỉnh từ XML layout (nếu có)
        attrs?.let {
            context.withStyledAttributes(
                it,
                R.styleable.CapsuleProgressBar,
                defStyleAttr,
                0
            ) {
                _progress = getInt(R.styleable.CapsuleProgressBar_ccpb_progress, _progress)
                _max = getInt(R.styleable.CapsuleProgressBar_cpb_max, _max).coerceAtLeast(1)
                barBackgroundColor = getColor(
                    R.styleable.CapsuleProgressBar_cpb_backgroundColor,
                    Color.WHITE
                )
                progressColor = getColor(
                    R.styleable.CapsuleProgressBar_cpb_progressColor,
                    "#FF8846".toColorInt()
                )
                outlineColor = getColor(
                    R.styleable.CapsuleProgressBar_cpb_outlineColor,
                    Color.BLACK
                )
            }
        }


        // Cập nhật màu cho Paint
        backgroundPaint.color = barBackgroundColor
        progressPaint.color = progressColor
        outlinePaint.color = outlineColor // Màu đen cho lớp ngoài cùng
    }

    // Hàm chuyển đổi và lưu trữ giá trị pixel của viền
    private fun calculateBorderPixels() {
        val density = resources.displayMetrics.density
        borderTopPx = 1f * density
        borderSidesPx = 1f * density
        borderBottomPx = 2.5f * density
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // --- Đo đạc kích thước ---
        // Tính chiều cao tối thiểu mong muốn dựa trên viền dày nhất và một khoảng padding nhỏ
        val minHeight =
            (borderTopPx + borderBottomPx + 10 * resources.displayMetrics.density).toInt()
        val desiredHeight = max(minHeight, suggestedMinimumHeight) // Lấy giá trị lớn hơn

        val measuredHeight = resolveSize(desiredHeight, heightMeasureSpec)
        val measuredWidth = resolveSize(suggestedMinimumWidth, widthMeasureSpec)
        setMeasuredDimension(measuredWidth, measuredHeight)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Lấy kích thước khu vực vẽ (đã trừ padding)
        val drawLeft = paddingLeft.toFloat()
        val drawTop = paddingTop.toFloat()
        val drawRight = (width - paddingRight).toFloat()
        val drawBottom = (height - paddingBottom).toFloat()
        val drawWidth = drawRight - drawLeft
        val drawHeight = drawBottom - drawTop

        if (drawWidth <= 0 || drawHeight <= 0) {
            return // Không có gì để vẽ
        }

        // --- Vẽ theo phương pháp nhiều lớp Fill ---

        // 1. Vẽ lớp ngoài cùng (màu viền - đen)
        val outerRadius = drawHeight / 2f // Bán kính cho hình capsule ngoài
        outerRect.set(drawLeft, drawTop, drawRight, drawBottom)
        canvas.drawRoundRect(
            outerRect,
            outerRadius,
            outerRadius,
            outlinePaint
        ) // Dùng outlinePaint (đã set FILL và màu đen)

        // 2. Tính toán kích thước lớp bên trong (màu nền - trắng)
        val innerLeft = drawLeft + borderSidesPx
        val innerTop = drawTop + borderTopPx
        val innerRight = drawRight - borderSidesPx
        val innerBottom = drawBottom - borderBottomPx
        val innerWidth = innerRight - innerLeft
        val innerHeight = innerBottom - innerTop

        // Chỉ vẽ bên trong nếu kích thước hợp lệ
        if (innerWidth > 0 && innerHeight > 0) {
            val innerRadius = innerHeight / 2f // Bán kính cho hình capsule bên trong

            // 3. Vẽ lớp nền trắng bên trong
            innerRect.set(innerLeft, innerTop, innerRight, innerBottom)
            canvas.drawRoundRect(
                innerRect,
                innerRadius,
                innerRadius,
                backgroundPaint
            ) // Dùng backgroundPaint (FILL, màu trắng)

            // 4. Vẽ lớp tiến trình màu cam (bên trong lớp trắng)
            if (_progress > 0) {
                val progressRatio = _progress.toFloat() / _max.toFloat()
                val progressFillWidth = innerWidth * progressRatio

                // Tính toán cạnh phải thực tế của progress
                val progressActualRight = min(innerLeft + progressFillWidth, innerRight)

                // Chỉ vẽ nếu chiều rộng > 0
                if (progressActualRight > innerLeft) {
                    progressClipRect.set(innerLeft, innerTop, progressActualRight, innerBottom)
                    // Vẽ hình bo tròn cho progress
                    canvas.drawRoundRect(
                        progressClipRect,
                        innerRadius,
                        innerRadius,
                        progressPaint
                    ) // Dùng progressPaint (FILL, màu cam)
                }
            }
        }
    }

    // Các phương thức tiện ích để đặt giá trị từ code (tùy chọn)
    fun setProgressColor(color: Int) {
        progressColor = color
        progressPaint.color = color
        invalidate()
    }

    fun setBarBackgroundColor(color: Int) {
        barBackgroundColor = color
        backgroundPaint.color = color
        invalidate()
    }

    fun setOutlineColor(color: Int) {
        outlineColor = color
        outlinePaint.color = color
        invalidate()
    }

    // --- Phương thức mới để đặt progress với animation ---
    /**
     * Đặt giá trị progress mới với hiệu ứng animation.
     * @param newProgress Giá trị progress mới (sẽ được giới hạn trong khoảng 0 đến max).
     * @param duration Thời gian animation (ms). Mặc định là [defaultAnimationDuration].
     */
    fun setProgressAnimated(newProgress: Int, duration: Long = defaultAnimationDuration) {
        // Hủy animation cũ nếu đang chạy
        progressAnimator?.cancel()

        val targetProgress = newProgress.coerceIn(0, _max)

        // Tạo ObjectAnimator để thay đổi thuộc tính "progress"
        // Nó sẽ tự động gọi hàm setProgress(value) của chúng ta
        val animator = ObjectAnimator.ofInt(this, "progress", progress, targetProgress)
        animator.duration = duration
        animator.interpolator = AccelerateDecelerateInterpolator() // Hiệu ứng mượt mà

        // Xóa tham chiếu khi animation kết thúc hoặc bị hủy
        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                progressAnimator = null
            }

            override fun onAnimationCancel(animation: Animator) {
                progressAnimator = null
            }
        })

        // Lưu tham chiếu và bắt đầu animation
        progressAnimator = animator
        animator.start()
    }

    // Hủy animation khi View bị detach khỏi cửa sổ (quan trọng)
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        progressAnimator?.cancel()
        progressAnimator = null
    }
}