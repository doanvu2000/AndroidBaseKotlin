package com.demo.skeleton

import android.content.Context
import android.util.Log
import android.view.View
import androidx.lifecycle.LifecycleObserver
import com.demo.skeleton.custom.KoletonView
import com.demo.skeleton.custom.RecyclerKoletonView
import com.demo.skeleton.custom.RecyclerViewAttributes
import com.demo.skeleton.custom.SimpleKoletonView
import com.demo.skeleton.custom.SimpleViewAttributes
import com.demo.skeleton.custom.TextKoletonView
import com.demo.skeleton.custom.TextViewAttributes
import com.demo.skeleton.memory.DelegateService
import com.demo.skeleton.memory.SkeletonService
import com.demo.skeleton.skeleton.RecyclerViewSkeleton
import com.demo.skeleton.skeleton.Skeleton
import com.demo.skeleton.skeleton.TextViewSkeleton
import com.demo.skeleton.skeleton.ViewSkeleton
import com.demo.skeleton.target.RecyclerViewTarget
import com.demo.skeleton.target.SimpleViewTarget
import com.demo.skeleton.target.TextViewTarget
import com.demo.skeleton.target.ViewTarget
import com.demo.skeleton.util.cloneTranslations
import com.demo.skeleton.util.generateRecyclerKoletonView
import com.demo.skeleton.util.generateSimpleKoletonView
import com.demo.skeleton.util.generateTextKoletonView
import com.demo.skeleton.util.getParentViewGroup
import com.demo.skeleton.util.isMeasured
import com.demo.skeleton.util.koletonManager
import com.facebook.shimmer.ShimmerFrameLayout
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal class MainSkeletonLoader(
    private val context: Context, override val defaults: DefaultSkeletonOptions
) : SkeletonLoader {

    companion object {
        private const val TAG = "MainSkeletonLoader"
    }

    private val loaderScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Log.e(TAG, throwable.message.orEmpty())
    }

    private val delegateService = DelegateService(this)

    override fun load(skeleton: Skeleton) {
        val job = loaderScope.launch(exceptionHandler) { loadInternal(skeleton) }
        val target = skeleton.target as? ViewTarget<*>
        target?.view?.koletonManager?.setCurrentSkeletonJob(job)
    }

    override fun generate(skeleton: Skeleton): KoletonView {
        return generateKoletonView(skeleton)
    }

    private suspend fun loadInternal(skeleton: Skeleton) = withContext(Dispatchers.Main.immediate) {
        val (lifecycle, mainDispatcher) = SkeletonService().lifecycleInfo(skeleton)
        val targetDelegate = delegateService.createTargetDelegate(skeleton)
        val deferred = async(mainDispatcher, CoroutineStart.LAZY) {
            val target = skeleton.target
            if (target is ViewTarget<*> && target is LifecycleObserver) {
                lifecycle.addObserver(target)
                with(target.view) {
                    if (parent !is KoletonView && isMeasured()) {
                        val koletonView = generateKoletonView(skeleton)
                        koletonManager.setCurrentKoletonView(koletonView)
                        targetDelegate.success(koletonView)
                    }
                }
            }
        }
        val skeletonDelegate = delegateService.createSkeletonDelegate(
            skeleton, targetDelegate, lifecycle, mainDispatcher, deferred
        )
        deferred.invokeOnCompletion { throwable ->
            loaderScope.launch(Dispatchers.Main.immediate) { skeletonDelegate.onComplete() }
        }
        deferred.await()
    }

    override fun hide(view: View, koletonView: KoletonView) {
        koletonView.hideSkeleton()
        val skeletonView = koletonView as ShimmerFrameLayout
        val originalParams = skeletonView.layoutParams
        val originalParent = skeletonView.getParentViewGroup()
        skeletonView.removeView(view)
        originalParent.removeView(skeletonView)
        view.cloneTranslations(skeletonView)
        originalParent.addView(view, originalParams)
    }

    private fun generateKoletonView(skeleton: Skeleton): KoletonView {
        return when (skeleton) {
            is RecyclerViewSkeleton -> generateRecyclerView(skeleton)
            is ViewSkeleton -> generateSimpleView(skeleton)
            is TextViewSkeleton -> generateTextView(skeleton)
        }
    }

    private fun generateTextView(skeleton: TextViewSkeleton) = with(skeleton) {
        return@with if (target is TextViewTarget) {
            val attributes = TextViewAttributes(
                view = target.view,
                color = color ?: defaults.color,
                cornerRadius = cornerRadius ?: defaults.cornerRadius,
                isShimmerEnabled = isShimmerEnabled ?: defaults.isShimmerEnabled,
                shimmer = shimmer ?: defaults.shimmer,
                lineSpacing = lineSpacing ?: defaults.lineSpacing,
                length = length
            )
            target.view.generateTextKoletonView(attributes)
        } else {
            TextKoletonView(context)
        }
    }

    private fun generateRecyclerView(skeleton: RecyclerViewSkeleton) = with(skeleton) {
        return@with if (target is RecyclerViewTarget) {
            val attributes = RecyclerViewAttributes(
                view = target.view,
                color = color ?: defaults.color,
                cornerRadius = cornerRadius ?: defaults.cornerRadius,
                isShimmerEnabled = isShimmerEnabled ?: defaults.isShimmerEnabled,
                shimmer = shimmer ?: defaults.shimmer,
                lineSpacing = lineSpacing ?: defaults.lineSpacing,
                itemLayout = itemLayoutResId,
                itemCount = itemCount ?: defaults.itemCount
            )
            target.view.generateRecyclerKoletonView(attributes)
        } else {
            RecyclerKoletonView(context)
        }
    }

    private fun generateSimpleView(skeleton: ViewSkeleton) = with(skeleton) {
        return@with if (target is SimpleViewTarget) {
            val attributes = SimpleViewAttributes(
                color = color ?: defaults.color,
                cornerRadius = cornerRadius ?: defaults.cornerRadius,
                isShimmerEnabled = isShimmerEnabled ?: defaults.isShimmerEnabled,
                shimmer = shimmer ?: defaults.shimmer,
                lineSpacing = lineSpacing ?: defaults.lineSpacing
            )
            target.view.generateSimpleKoletonView(attributes)
        } else {
            SimpleKoletonView(context)
        }
    }
}