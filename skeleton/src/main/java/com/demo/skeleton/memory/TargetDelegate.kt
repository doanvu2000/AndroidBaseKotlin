package com.demo.skeleton.memory

import androidx.annotation.MainThread
import com.demo.skeleton.custom.KoletonView
import com.demo.skeleton.skeleton.Skeleton
import com.demo.skeleton.target.Target

internal sealed class TargetDelegate {

    @MainThread
    open fun start() {
    }

    @MainThread
    open fun success(skeleton: KoletonView) {
    }

    @MainThread
    open fun error() {
    }

    @MainThread
    open fun clear() {
    }
}

internal class ViewTargetDelegate(
    private val skeleton: Skeleton,
    private val target: Target?
) : TargetDelegate() {

    override fun start() {
        target?.onStart()
    }

    override fun success(skeleton: KoletonView) {
        target?.onSuccess(skeleton)
    }

    override fun error() {
        target?.onError()
    }

    override fun clear() {}
}