package com.example.baseproject.base.base_view.widget

import android.R
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener

@SuppressLint("AppCompatCustomView")
class SmoothSeekBar : SeekBar, OnSeekBarChangeListener {
    private var mOnSeekBarChangeListener: OnSeekBarChangeListener? = null
    private var mContext: Context? = null
    private var mNeedCallListener = true
    private var mAnimator: ValueAnimator? = null

    constructor(context: Context?) : super(context) {
        init(context)
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init(context)
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context)
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(
        context,
        attrs,
        defStyleAttr,
        defStyleRes
    ) {
        init(context)
    }

    fun init(context: Context?) {
        mContext = context
    }

    override fun setOnSeekBarChangeListener(
        onSeekBarChangeListener: OnSeekBarChangeListener
    ) {
        mOnSeekBarChangeListener = onSeekBarChangeListener
        super.setOnSeekBarChangeListener(this)
    }

    override fun setProgress(progress: Int) {
        val currentProgress = getProgress()
        if (mAnimator != null) {
            mAnimator!!.cancel()
            mAnimator!!.removeAllUpdateListeners()
            mAnimator!!.removeAllListeners()
            mAnimator = null
            mNeedCallListener = true
        }
        mAnimator = ValueAnimator.ofInt(currentProgress, progress)
        val duration = resources.getInteger(R.integer.config_mediumAnimTime).toLong()//400
        mAnimator?.setDuration(duration)
        mAnimator?.interpolator = AccelerateDecelerateInterpolator()
        mAnimator?.addUpdateListener { animation ->
            val value = animation.animatedValue as Int
            mNeedCallListener = value == progress
            super@SmoothSeekBar.setProgress(value)
        }
        mAnimator?.start()
    }

    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
        if (fromUser || mNeedCallListener) {
            if (mOnSeekBarChangeListener != null) {
                mOnSeekBarChangeListener!!.onProgressChanged(seekBar, progress, fromUser)
            }
        }
    }

    override fun onStartTrackingTouch(seekBar: SeekBar) {
        if (mOnSeekBarChangeListener != null) {
            mOnSeekBarChangeListener!!.onStartTrackingTouch(seekBar)
        }
    }

    override fun onStopTrackingTouch(seekBar: SeekBar) {
        if (mOnSeekBarChangeListener != null) {
            mOnSeekBarChangeListener!!.onStopTrackingTouch(seekBar)
        }
    }
}