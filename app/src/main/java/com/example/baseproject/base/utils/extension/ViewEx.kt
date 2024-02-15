package com.example.baseproject.base.utils.extension

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.SeekBar
import com.example.baseproject.R
import com.google.android.material.card.MaterialCardView
import com.google.android.material.tabs.TabLayout

fun View.hideKeyboard() {
    val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
}

fun View.showKeyboard() {
    val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
}

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
                    // It’s very important to remove this listener once we are done.
                    viewTreeObserver.removeOnWindowFocusChangeListener(this)
                }
            }
        }
        viewTreeObserver.addOnWindowFocusChangeListener(listener)
    }
}

fun View.disableView() {
    this.isClickable = false
    this.postDelayed({ this.isClickable = true }, 500)
}

class SafeClickListener(val onSafeClickListener: (View) -> Unit) : View.OnClickListener {
    override fun onClick(v: View) {
        v.disableView()
        onSafeClickListener(v)
    }
}

fun View.setOnSafeClick(onSafeClickListener: (View) -> Unit) {
    val safeClick = SafeClickListener {
        onSafeClickListener(it)
    }
    setOnClickListener(safeClick)
}

fun View.hide() {
    if (!isHide) {
        this.visibility = View.INVISIBLE
    }
}

fun View.show() {
    if (!isShow) {
        this.visibility = View.VISIBLE
    }
}

fun View.gone() {
    if (!isGone) {
        this.visibility = View.GONE
    }
}

val View.isShow: Boolean
    get() = this.visibility == View.VISIBLE

val View.isHide: Boolean
    get() = this.visibility == View.INVISIBLE
val View.isGone: Boolean
    get() = this.visibility == View.GONE

fun View.showOrGone(isShow: Boolean) {
    if (isShow) {
        show()
    } else {
        gone()
    }
}

fun View.showOrHide(isShow: Boolean) {
    if (isShow) {
        show()
    } else {
        hide()
    }
}

fun View.getBitmapFromView(done: (Bitmap) -> Unit) {
    post {
        val b = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val c = Canvas(b)
        layout(left, top, right, bottom)
        draw(c)
        done(b)
    }
}

fun View.setSize(width: Int, height: Int) {
    val rootParam = this.layoutParams
    rootParam.width = width
    rootParam.height = height
    layoutParams = rootParam
    requestLayout()
}

fun View.showWithAnimation() {
    this.visibility = View.VISIBLE
    this.startAnimation(AnimationUtils.loadAnimation(this.context, R.anim.slide_in_bottom))
}

fun View.goneWithAnimation() {
    this.visibility = View.GONE
    this.startAnimation(AnimationUtils.loadAnimation(this.context, R.anim.slide_out_bottom))
}

fun View.isSoftKeyboardVisible(): Boolean {
    val rect = Rect()
    rootView.getWindowVisibleDisplayFrame(rect)
    val screenHeight = rootView.height
    val keyboardHeight = screenHeight - rect.bottom
    val threshold = screenHeight * 0.15 // Adjust this value as per your requirements
    return keyboardHeight > threshold
}

fun TabLayout.createTab(tabName: String): TabLayout.Tab {
    return newTab().setText(tabName)
}

/**
 * make view is free to add in another view group
 * */
fun View?.removeSelf() {
    this ?: return
    val parentView = parent as? ViewGroup ?: return
    parentView.removeView(this)
}

fun SeekBar.setOnProgressChange(
    action: (
        seekBar: SeekBar?, progress: Int, fromUser: Boolean
    ) -> Unit
) {
    this.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            action.invoke(seekBar, progress, fromUser)
        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) {

        }

        override fun onStopTrackingTouch(seekBar: SeekBar?) {

        }
    })
}

fun MaterialCardView.removeShadow() {
    this.cardElevation = 0f
}

fun MaterialCardView.addShadow(elevation: Float) {
    this.cardElevation = elevation.dpf
}