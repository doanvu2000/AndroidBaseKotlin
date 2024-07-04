package com.demo.skeleton.target

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.demo.skeleton.custom.KoletonView

/** A [Target] that handles setting skeleton on a [RecyclerView]. */
open class RecyclerViewTarget(override val view: RecyclerView) : ViewTarget<RecyclerView>,
    DefaultLifecycleObserver {

    override fun onStart() = Unit

    /** Show the [skeleton] view */
    override fun onSuccess(skeleton: KoletonView) = skeleton.showSkeleton()

    override fun onError() = Unit

    override fun onStart(owner: LifecycleOwner) {}

    override fun onStop(owner: LifecycleOwner) {}
}