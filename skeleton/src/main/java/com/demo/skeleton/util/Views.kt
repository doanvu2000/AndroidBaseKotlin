package com.demo.skeleton.util

import android.graphics.Color
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.ViewParent
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.demo.skeleton.R
import com.demo.skeleton.SkeletonLoader
import com.demo.skeleton.custom.Attributes
import com.demo.skeleton.custom.KoletonView
import com.demo.skeleton.custom.RecyclerKoletonView
import com.demo.skeleton.custom.RecyclerViewAttributes
import com.demo.skeleton.custom.SimpleKoletonView
import com.demo.skeleton.custom.TextKoletonView
import com.demo.skeleton.custom.TextViewAttributes
import com.demo.skeleton.memory.ViewTargetSkeletonManager
import com.demo.skeleton.skeleton.RecyclerViewSkeleton
import com.demo.skeleton.skeleton.TextViewSkeleton
import com.demo.skeleton.skeleton.ViewSkeleton

internal fun View.visible() {
    visibility = View.VISIBLE
}

internal fun View.invisible() {
    visibility = View.INVISIBLE
}

internal fun View.isVisible(): Boolean {
    return this.visibility == View.VISIBLE
}

internal fun View.gone() {
    visibility = View.GONE
}

internal fun View.getParentView(): ViewParent {
    return checkNotNull(parent) { "The view has not attach to any view" }
}

internal fun View.getParentViewGroup(): ViewGroup {
    return getParentView() as ViewGroup
}

internal fun ViewGroup.children(): List<View> {
    return (0 until childCount).map { child -> getChildAt(child) }
}

internal fun View.generateSimpleKoletonView(attributes: Attributes): SimpleKoletonView {
    val parent = parent as? ViewGroup
    return SimpleKoletonView(context).also {
        validateBackground()
        it.id = id
        it.layoutParams = layoutParams
        it.cloneTranslations(this)
        parent?.removeView(this)
        ViewCompat.setLayoutDirection(it, ViewCompat.getLayoutDirection(this))
        it.addView(this.lparams(layoutParams))
        parent?.addView(it)
        it.attributes = attributes
    }
}

internal fun View.validateBackground() {
    if (this is FrameLayout) setBackgroundColor(Color.TRANSPARENT)
}

internal fun RecyclerView.generateRecyclerKoletonView(attributes: RecyclerViewAttributes): RecyclerKoletonView {
    val parent = parent as? ViewGroup
    return RecyclerKoletonView(context).also {
        it.id = id
        it.layoutParams = layoutParams
        it.cloneTranslations(this)
        parent?.removeView(this)
        ViewCompat.setLayoutDirection(it, ViewCompat.getLayoutDirection(this))
        it.addView(this.lparams(layoutParams))
        parent?.addView(it)
        it.attributes = attributes
    }
}

internal fun TextView.generateTextKoletonView(attributes: TextViewAttributes): TextKoletonView {
    val parent = parent as? ViewGroup
    return TextKoletonView(context).also {
        it.id = id
        it.layoutParams = layoutParams
        it.cloneTranslations(this)
        parent?.removeView(this)
        ViewCompat.setLayoutDirection(it, ViewCompat.getLayoutDirection(this))
        it.addView(this.lparams(layoutParams))
        parent?.addView(it)
        it.attributes = attributes
    }
}

internal fun <T : View> T.lparams(source: ViewGroup.LayoutParams): T {
    val layoutParams = FrameLayout.LayoutParams(source).apply {
        if (width.isZero()) {
            width =
                if (this@lparams.width.isZero() && source is ConstraintLayout.LayoutParams) MATCH_PARENT
                else this@lparams.width
        }
        if (height.isZero()) {
            height =
                if (this@lparams.height.isZero() && source is ConstraintLayout.LayoutParams) MATCH_PARENT
                else this@lparams.height
        }
    }
    this@lparams.layoutParams = layoutParams
    return this
}

internal fun View.removeOnGlobalLayoutListener(listener: ViewTreeObserver.OnGlobalLayoutListener) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
        @Suppress("DEPRECATION") this.viewTreeObserver.removeGlobalOnLayoutListener(listener)
    } else {
        this.viewTreeObserver.removeOnGlobalLayoutListener(listener)
    }
}

internal fun View.isMeasured() =
    layoutParams.width == WRAP_CONTENT || layoutParams.height == WRAP_CONTENT || (measuredWidth > 0 && measuredHeight > 0)

internal val View.koletonManager: ViewTargetSkeletonManager
    get() {
        var manager = getTag(R.id.koleton_manager) as? ViewTargetSkeletonManager
        if (manager == null) {
            manager = ViewTargetSkeletonManager().apply {
                viewTreeObserver.addOnGlobalLayoutListener(this)
                setTag(R.id.koleton_manager, this)
            }
        }
        return manager
    }

internal fun View.cloneTranslations(view: View) {
    translationX = view.translationX
    translationY = view.translationY
    view.clearTranslations()
}

internal fun View.clearTranslations() {
    translationX = 0f
    translationY = 0f
}

/**
 * This is the type-unsafe version of [View.loadSkeleton].
 *
 * Example:
 * ```
 * view.loadSkeleton {
 *      color(R.color.colorExample)
 * }
 * ```
 *
 * @param skeletonLoader The [SkeletonLoader] that will be used to create the [ViewSkeleton].
 * @param builder An optional lambda to configure the skeleton before it is loaded.
 */
@JvmSynthetic
inline fun View.loadSkeleton(
    skeletonLoader: SkeletonLoader = Koleton.skeletonLoader(context),
    builder: ViewSkeleton.Builder.() -> Unit = {}
) {
    val skeleton = ViewSkeleton.Builder(context).target(this).apply(builder).build()
    skeletonLoader.load(skeleton)
}


/**
 * This is the type-unsafe version of [View.generateSkeleton].
 *
 * Example:
 * ```
 * val koletonView = view.generateSkeleton {
 *      color(R.color.colorSkeleton)
 * }
 * koletonView.showSkeleton()
 * ```
 *
 * @param skeletonLoader The [SkeletonLoader] that will be used to create the [ViewSkeleton].
 * @param builder An optional lambda to configure the skeleton.
 * @return the [KoletonView] that contains the skeleton
 */
@JvmSynthetic
inline fun View.generateSkeleton(
    skeletonLoader: SkeletonLoader = Koleton.skeletonLoader(context),
    builder: ViewSkeleton.Builder.() -> Unit = {}
): KoletonView {
    val skeleton = ViewSkeleton.Builder(context).target(this).apply(builder).build()
    return skeletonLoader.generate(skeleton)
}

/**
 * Load the skeleton referenced by [itemLayout] and set it on this [RecyclerView].
 *
 * This is the type-unsafe version of [RecyclerView.loadSkeleton].
 *
 * Example:
 * ```
 * recyclerView.loadSkeleton(R.layout.item_example) {
 *      color(R.color.colorExample)
 * }
 * ```
 * @param itemLayout Layout resource of the itemView that will be used to create the skeleton view.
 * @param skeletonLoader The [SkeletonLoader] that will be used to create the [RecyclerViewSkeleton].
 * @param builder An optional lambda to configure the skeleton before it is loaded.
 */
@JvmSynthetic
inline fun RecyclerView.loadSkeleton(
    @LayoutRes itemLayout: Int,
    skeletonLoader: SkeletonLoader = Koleton.skeletonLoader(context),
    builder: RecyclerViewSkeleton.Builder.() -> Unit = {}
) {
    val skeleton =
        RecyclerViewSkeleton.Builder(context, itemLayout).target(this).apply(builder).build()
    skeletonLoader.load(skeleton)
}

/**
 * Set [length] of the [TextView].
 *
 * This is the type-unsafe version of [TextView.loadSkeleton].
 *
 * Example:
 * ```
 * textView.loadSkeleton(10) {
 *      color(R.color.colorExample)
 * }
 * ```
 * @param length Length of the [TextView].
 * @param skeletonLoader The [SkeletonLoader] that will be used to create the [TextViewSkeleton].
 * @param builder An optional lambda to configure the skeleton before it is loaded.
 */
@JvmSynthetic
inline fun TextView.loadSkeleton(
    length: Int,
    skeletonLoader: SkeletonLoader = Koleton.skeletonLoader(context),
    builder: TextViewSkeleton.Builder.() -> Unit = {}
) {
    val skeleton = TextViewSkeleton.Builder(context, length).target(this).apply(builder).build()
    skeletonLoader.load(skeleton)
}


/**
 * @return True if the skeleton associated with this [View] is shown.
 */
fun View.isSkeletonShown(): Boolean {
    return KoletonUtils.isSkeletonShown(this)
}

/**
 * Calls the specified function [block] after the skeleton is hidden.
 */
fun View.afterHideSkeleton(block: () -> Unit) {
    KoletonUtils.afterHide(this, block)
}

/**
 * Hide all skeletons associated with this [View].
 */
fun View.hideSkeleton() {
    KoletonUtils.hide(this)
}