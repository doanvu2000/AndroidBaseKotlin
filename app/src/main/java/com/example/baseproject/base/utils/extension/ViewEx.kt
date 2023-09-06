package com.example.baseproject.base.utils.extension

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.view.View
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ScrollView
import androidx.core.widget.NestedScrollView
import com.example.baseproject.R

fun View.hideKeyboard() {
    val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
}

fun View.showKeyboard() {
    val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
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
    this.visibility = View.INVISIBLE
}

fun View.show() {
    this.visibility = View.VISIBLE
}

fun View.gone() {
    this.visibility = View.GONE
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

fun EditText.setDrawableEnd(drawableId: Int) {
    val drawable = if (drawableId == 0) {
        null
    } else {
        context.getDrawableById(drawableId)
    }
    this.setCompoundDrawablesWithIntrinsicBounds(
        null, null, drawable, null
    )
}

fun EditText.clear() {
    this.setText("")
}

fun ScrollView.scrollToTop() {
    this.fullScroll(ScrollView.FOCUS_UP)
}

fun NestedScrollView.scrollToTop() {
    fullScroll(ScrollView.FOCUS_UP)
    smoothScrollTo(0, 0)
}