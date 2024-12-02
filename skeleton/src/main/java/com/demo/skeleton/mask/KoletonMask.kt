package com.demo.skeleton.mask

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.text.LineBreaker
import android.text.StaticLayout
import android.text.TextPaint
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.ColorInt
import com.demo.skeleton.util.NUMBER_ONE
import com.demo.skeleton.util.NUMBER_ZERO
import com.demo.skeleton.util.WRAPPING_LIMIT
import com.demo.skeleton.util.background
import com.demo.skeleton.util.children
import com.demo.skeleton.util.isZero
import com.demo.skeleton.util.spannable
import kotlin.math.absoluteValue

internal class KoletonMask(
    val view: View,
    @ColorInt private val color: Int,
    private val cornerRadius: Float,
    lineSpacing: Float
) {

    private val paint: Paint by lazy { Paint().apply { color = this@KoletonMask.color } }
    private val bitmap: Bitmap by lazy {
        Bitmap.createBitmap(
            view.width,
            view.height,
            Bitmap.Config.ALPHA_8
        )
    }
    private val canvas: Canvas by lazy { Canvas(bitmap) }
    private val lineSpacingPerLine: Float by lazy { lineSpacing / 2 }

    init {
        val paint = Paint().apply {
            xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC)
            isAntiAlias = cornerRadius > NUMBER_ZERO
        }
        mask(view, view as ViewGroup, paint)
    }

    fun draw(canvas: Canvas) {
        canvas.drawBitmap(bitmap, 0f, 0f, paint)
    }

    private fun mask(view: View, root: ViewGroup, paint: Paint) {
        when (view) {
            is ViewGroup -> view.children().forEach { v -> mask(v, root, paint) }
            is TextView -> maskTextView(view, root)
            else -> maskView(view, root, paint)
        }
    }

    private fun maskView(view: View, root: ViewGroup, paint: Paint) {
        val rect = Rect().also {
            view.getDrawingRect(it)
            root.offsetDescendantRectToMyCoords(view, it)
        }
        canvas.drawRoundRect(RectF(rect), cornerRadius, cornerRadius, paint)
    }

    private fun maskTextView(
        view: TextView,
        root: ViewGroup
    ) {
        val rect = Rect().also {
            view.getDrawingRect(it)
            root.offsetDescendantRectToMyCoords(view, it)
        }
        val textPaint = view.paint.apply { isAntiAlias = cornerRadius > NUMBER_ZERO }
        maskStaticLayout(view, rect, textPaint)
    }

    private fun maskStaticLayout(
        view: TextView,
        rect: Rect,
        textPaint: TextPaint
    ) {
        val spannable = spannable { background(color, cornerRadius, lineSpacingPerLine, view.text) }
        val staticLayout = StaticLayout.Builder
            .obtain(
                spannable,
                0,
                spannable.length,
                textPaint.apply { color = Color.TRANSPARENT },
                view.width
            )
            .setBreakStrategy(LineBreaker.BREAK_STRATEGY_SIMPLE)
            .setIncludePad(view.includeFontPadding)
            .setMaxLines(view.lineCount)
            .build()
        canvas.save()
        canvas.translate(rect.left.toFloat(), rect.top.toFloat())
        staticLayout.draw(canvas)
        canvas.restore()
    }

    private fun maskLineWrapping(
        view: TextView,
        rect: Rect,
        textPaint: TextPaint
    ) {
        if (view.lineCount.isZero()) return
        val measuredWidth = floatArrayOf(0F)
        var startIndex = 0
        var count: Int
        var lineIndex = 0
        while (startIndex < view.text.length) {
            count = textPaint.breakText(
                view.text,
                startIndex,
                view.text.length,
                true,
                view.width.toFloat(),
                measuredWidth
            )
            val topOffset = view.height * lineIndex / view.lineCount
            val bottomOffset =
                view.height * (lineIndex - (view.lineCount - NUMBER_ONE)).absoluteValue / view.lineCount
            val top = rect.top.toFloat() + (topOffset + lineSpacingPerLine)
            val bottom = rect.bottom.toFloat() - (bottomOffset + lineSpacingPerLine)
            val right = rect.left.toFloat() + measuredWidth[0]
            val rectF = RectF(
                rect.left.toFloat(),
                top,
                if (right > rect.right * WRAPPING_LIMIT) rect.right.toFloat() else right,
                bottom
            )
            canvas.drawRoundRect(rectF, cornerRadius, cornerRadius, textPaint)
            startIndex += count
            lineIndex++
        }
    }
}