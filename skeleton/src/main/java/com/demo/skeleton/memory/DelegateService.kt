package com.demo.skeleton.memory

import androidx.annotation.MainThread
import androidx.lifecycle.Lifecycle
import com.demo.skeleton.SkeletonLoader
import com.demo.skeleton.skeleton.RecyclerViewSkeleton
import com.demo.skeleton.skeleton.Skeleton
import com.demo.skeleton.skeleton.TextViewSkeleton
import com.demo.skeleton.skeleton.ViewSkeleton
import com.demo.skeleton.target.ViewTarget
import com.demo.skeleton.util.koletonManager
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Deferred

internal class DelegateService(
    private val imageLoader: SkeletonLoader
) {

    fun createTargetDelegate(
        skeleton: Skeleton
    ): TargetDelegate {
        return ViewTargetDelegate(skeleton, skeleton.target)
    }

    @MainThread
    fun createSkeletonDelegate(
        skeleton: Skeleton,
        targetDelegate: TargetDelegate,
        lifecycle: Lifecycle,
        mainDispatcher: CoroutineDispatcher,
        deferred: Deferred<*>
    ): SkeletonDelegate {
        val skeletonDelegate: SkeletonDelegate
        when (skeleton) {
            is ViewSkeleton, is RecyclerViewSkeleton, is TextViewSkeleton -> when (val target =
                skeleton.target) {
                is ViewTarget<*> -> {
                    skeletonDelegate = ViewTargetSkeletonDelegate(
                        imageLoader = imageLoader,
                        skeleton = skeleton,
                        target = targetDelegate,
                        lifecycle = lifecycle,
                        dispatcher = mainDispatcher,
                        job = deferred
                    )
                    lifecycle.addObserver(skeletonDelegate)
                    target.view.koletonManager.setCurrentSkeleton(skeletonDelegate)
                }

                else -> {
                    skeletonDelegate = BaseRequestDelegate(lifecycle, mainDispatcher, deferred)
                    lifecycle.addObserver(skeletonDelegate)
                }
            }
        }
        return skeletonDelegate
    }
}
