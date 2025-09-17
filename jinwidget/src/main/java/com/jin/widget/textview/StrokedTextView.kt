package com.jin.widget.textview

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.graphics.toColorInt
import androidx.core.graphics.withTranslation
import com.jin.widget.R
import com.jin.widget.utils.dpToPx

/**
 * A custom [AppCompatTextView] that adds a stroke effect around the text.
 *
 * This view allows for customization of the stroke's width and color. It also supports
 * applying a linear gradient to the text fill color and can force the text to be uppercase.
 *
 * Custom attributes can be set via XML:
 * - `stroke_width`: The width of the stroke in pixels.
 * - `stroke_color`: The color of the stroke.
 * - `stroke_textAllCaps`: A boolean to indicate if the text should be rendered in uppercase.
 * - `is_gradient_text_color`: A boolean to enable or disable the gradient text color.
 * - `start_color_gradient`: The starting color of the text gradient.
 * - `end_color_gradient`: The ending color of the text gradient.
 * - `is_alignment_center`: A boolean to force the text alignment to be centered, especially useful for multi-line text.
 *
 * @param context The Context the view is running in, through which it can access the current theme, resources, etc.
 * @param attrs The attributes of the XML tag that is inflating the view.
 * @param defStyleAttr An attribute in the current theme that contains a reference to a style resource
 * that supplies default values for the view. Can be 0 to not look for defaults.
 */
class StrokedTextView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {

    companion object {
        private val DEFAULT_STROKE_COLOR = "#036838".toColorInt()
        private val STROKE_WIDTH_DEFAULT = 4.dpToPx
    }

    private var strokeWidth = 4f
    private var strokeColor = Color.RED
    private var textAllCaps = false
    private var startColorGradient = "#000000".toColorInt()
    private var endColorGradient = "#000000".toColorInt()
    private var isGradientTextColor = false
    private var isAlignCenter = false

    private val strokePaint: TextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
    }

    private val fillPaint: TextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    init {
        context.theme.obtainStyledAttributes(attrs, R.styleable.StrokedTextView, 0, 0).apply {
            try {
                strokeWidth = getDimensionPixelSize(
                    R.styleable.StrokedTextView_stroke_width, STROKE_WIDTH_DEFAULT
                ).toFloat()
                strokeColor = getColor(
                    R.styleable.StrokedTextView_stroke_color,
                    DEFAULT_STROKE_COLOR
                )
                textAllCaps = getBoolean(R.styleable.StrokedTextView_stroke_textAllCaps, false)
                isGradientTextColor =
                    getBoolean(R.styleable.StrokedTextView_is_gradient_text_color, false)

                startColorGradient = getColor(
                    R.styleable.StrokedTextView_start_color_gradient,
                    "#000000".toColorInt()
                )
                endColorGradient =
                    getColor(R.styleable.StrokedTextView_end_color_gradient, "#000000".toColorInt())

                isAlignCenter = getBoolean(R.styleable.StrokedTextView_is_alignment_center, false)
            } finally {
                recycle()
            }
        }
        strokePaint.strokeWidth = strokeWidth
        strokePaint.color = strokeColor
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        val textToDraw = if (textAllCaps) text.toString().uppercase() else text.toString()

        // Set up stroke paint
        strokePaint.textSize = textSize
        strokePaint.typeface = typeface
        strokePaint.color = strokeColor

        // Set up fill paint
        fillPaint.textSize = textSize
        fillPaint.typeface = typeface

        // Apply gradient if enabled, otherwise use normal text color
        if (isGradientTextColor) {
            val width = measuredWidth
            fillPaint.shader = LinearGradient(
                0f, 0f, width.toFloat(), height.toFloat(),
                startColorGradient,
                endColorGradient,
                Shader.TileMode.CLAMP
            )
        } else {
            fillPaint.shader = null
            fillPaint.color = currentTextColor
        }

        // Calculate text layout
        val width = measuredWidth
        val alignment = Layout.Alignment.ALIGN_CENTER

        val staticBuilderStroke =
            StaticLayout.Builder.obtain(textToDraw, 0, textToDraw.length, strokePaint, width)

        if (isAlignCenter) {
            staticBuilderStroke.setAlignment(alignment)
        }
        val strokeLayout = staticBuilderStroke.build()

        val fillStaticLayoutBuilder =
            StaticLayout.Builder.obtain(textToDraw, 0, textToDraw.length, fillPaint, width)
        if (isAlignCenter) {
            fillStaticLayoutBuilder.setAlignment(alignment)
        }
        val fillLayout = fillStaticLayoutBuilder.build()

        // Draw stroke first
        canvas.withTranslation(0f, (height - strokeLayout.height) / 2f) {
            strokeLayout.draw(this)
        }

        // Draw fill on top
        canvas.withTranslation(0f, (height - fillLayout.height) / 2f) {
            fillLayout.draw(this)
        }
    }
}