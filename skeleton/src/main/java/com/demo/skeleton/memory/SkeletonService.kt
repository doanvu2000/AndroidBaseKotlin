package com.demo.skeleton.memory

import androidx.annotation.MainThread
import androidx.lifecycle.Lifecycle
import com.demo.skeleton.lifecycle.GlobalLifecycle
import com.demo.skeleton.lifecycle.LifecycleCoroutineDispatcher
import com.demo.skeleton.skeleton.RecyclerViewSkeleton
import com.demo.skeleton.skeleton.Skeleton
import com.demo.skeleton.skeleton.TextViewSkeleton
import com.demo.skeleton.skeleton.ViewSkeleton
import com.demo.skeleton.target.Target
import com.demo.skeleton.target.ViewTarget
import com.demo.skeleton.util.getLifecycle
import com.demo.skeleton.util.isNotNull
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

internal class SkeletonService {

    @MainThread
    fun lifecycleInfo(skeleton: Skeleton): LifecycleInfo {
        when (skeleton) {
            is ViewSkeleton, is RecyclerViewSkeleton, is TextViewSkeleton -> {
                val lifecycle = skeleton.getLifecycle()
                return if (lifecycle != null) {
                    val mainDispatcher = LifecycleCoroutineDispatcher
                        .createUnlessStarted(Dispatchers.Main.immediate, lifecycle)
                    LifecycleInfo(lifecycle, mainDispatcher)
                } else {
                    LifecycleInfo.GLOBAL
                }
            }
        }
    }

    private fun Skeleton.getLifecycle(): Lifecycle? {
        return when {
            lifecycle.isNotNull() -> lifecycle
            this is ViewSkeleton || this is RecyclerViewSkeleton -> target?.getLifecycle()
            else -> context.getLifecycle()
        }
    }

    private fun Target.getLifecycle(): Lifecycle? {
        return (this as? ViewTarget<*>)?.view?.context?.getLifecycle()
    }

    data class LifecycleInfo(
        val lifecycle: Lifecycle,
        val mainDispatcher: CoroutineDispatcher
    ) {

        companion object {
            val GLOBAL = LifecycleInfo(
                lifecycle = GlobalLifecycle,
                mainDispatcher = Dispatchers.Main.immediate
            )
        }
    }
}