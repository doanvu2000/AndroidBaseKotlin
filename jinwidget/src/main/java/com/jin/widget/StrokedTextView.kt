package com.jin.widget

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