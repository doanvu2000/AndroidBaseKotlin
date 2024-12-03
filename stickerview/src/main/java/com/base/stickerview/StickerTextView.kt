package com.base.stickerview

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.FontRes

class StickerTextView : StickerView {

    companion object {

        fun pixelsToSp(context: Context, px: Float): Float {
            val scaledDensity = context.resources.displayMetrics.scaledDensity
            return px / scaledDensity
        }
    }

    var tvMain: AutoResizeTextView? = null

    override val mainView: View
        get() {
            if (tvMain != null)
                return tvMain as AutoResizeTextView

            tvMain = AutoResizeTextView(context)
            tvMain!!.setTextColor(Color.WHITE)
//            tvMain!!.setFont(R.font.inter_semi_bold_600)
            tvMain!!.gravity = Gravity.CENTER
            tvMain!!.textSize = 400f
            tvMain!!.setShadowLayer(4f, 0f, 0f, Color.BLACK)
            tvMain!!.maxLines = 1
            val params = LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            params.gravity = Gravity.CENTER_VERTICAL
            tvMain!!.layoutParams = params
//            if (imageViewFlip != null)
//                imageViewFlip!!.visibility = View.GONE
            return tvMain as AutoResizeTextView
        }

    var text: String?
        get() = if (tvMain != null) tvMain!!.text.toString() else null
        set(text) {
            if (tvMain != null)
                tvMain!!.text = text
        }

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    )

    override fun onScaling(scaleUp: Boolean) {
        super.onScaling(scaleUp)

    }

    fun getTypeFaceMain() = tvMain?.typeface

    fun initText(text: String) {
        tvMain?.setText(text, TextView.BufferType.NORMAL)
    }

    fun setMaxLineText(maxLine: Int) {
        if (tvMain?.maxLines != maxLine) {
            tvMain?.maxLines = maxLine
        }
    }

    fun setTextFont(@FontRes fontId: Int) {
        tvMain?.setFont(fontId)
    }

    fun setTextFont(typeFace: Typeface?) {
        tvMain?.typeface = typeFace
    }

    fun resetFont() {
        setTextFont(null)
    }

    fun setMainTextColor(color: Int) {
        tvMain?.setTextColor(color)
    }

    fun setMainTextColor(color: String) {
        tvMain?.setTextColor(Color.parseColor(color))
    }

//    fun setMainShadow(shadow: TextShadowEnum) {
//        tvMain?.setShadow(shadow)
//    }
}