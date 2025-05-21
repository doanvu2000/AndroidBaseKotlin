package com.base.cameraview.internal

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.VisibleForTesting
import androidx.core.graphics.drawable.toDrawable
import com.base.cameraview.controls.Grid

/**
 * A layout overlay that draws grid lines based on the [Grid] parameter.
 */
class GridLinesLayout @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
    View(context, attrs) {
    private val width: Float = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, 0.9f,
        context.resources.displayMetrics
    )

    @VisibleForTesting
    var callback: DrawCallback? = null
    private var gridMode: Grid? = null
    private var gridColor: Int = DEFAULT_COLOR
    private val horiz: ColorDrawable = gridColor.toDrawable()
    private val vert: ColorDrawable = gridColor.toDrawable()

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        horiz.setBounds(left, 0, right, width.toInt())
        vert.setBounds(0, top, width.toInt(), bottom)
    }

    /**
     * Returns the current grid value.
     *
     * @return the grid mode
     */
    fun getGridMode(): Grid {
        return gridMode!!
    }

    /**
     * Sets a new grid value
     *
     * @param gridMode the new value
     */
    fun setGridMode(gridMode: Grid) {
        this.gridMode = gridMode
        postInvalidate()
    }

    /**
     * Returns the current grid color.
     *
     * @return the grid color
     */
    fun getGridColor(): Int {
        return gridColor
    }

    /**
     * Sets a new grid color.
     *
     * @param gridColor the new color
     */
    fun setGridColor(@ColorInt gridColor: Int) {
        this.gridColor = gridColor
        horiz.color = gridColor
        vert.color = gridColor
        postInvalidate()
    }

    private val lineCount: Int
        get() {
            return when (gridMode) {
                Grid.OFF -> return 0
                Grid.DRAW_3X3 -> return 2
                Grid.DRAW_PHI -> return 2
                Grid.DRAW_4X4 -> return 3
                else -> 0
            }
        }

    private fun getLinePosition(lineNumber: Int): Float {
        val lineCount = this.lineCount
        if (gridMode == Grid.DRAW_PHI) {
            // 1 = 2x + GRIx
            // 1 = x(2+GRI)
            // x = 1/(2+GRI)
            val delta: Float = 1f / (2 + GOLDEN_RATIO_INV)
            return if (lineNumber == 1) delta else (1 - delta)
        } else {
            return (1f / (lineCount + 1)) * (lineNumber + 1f)
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val count = this.lineCount
        for (n in 0..<count) {
            val pos = getLinePosition(n)

            // Draw horizontal line
            canvas.translate(0f, pos * height)
            horiz.draw(canvas)
            canvas.translate(0f, -pos * height)

            // Draw vertical line
            canvas.translate(pos * width, 0f)
            vert.draw(canvas)
            canvas.translate(-pos * width, 0f)
        }
        if (callback != null) {
            callback!!.onDraw(count)
        }
    }

    interface DrawCallback {
        fun onDraw(lines: Int)
    }

    companion object {
        @JvmField
        val DEFAULT_COLOR: Int = Color.argb(160, 255, 255, 255)
        private const val GOLDEN_RATIO_INV = 0.61803398874989f
    }
}
