package com.base.cameraview.engine.orchestrator

import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import java.util.Locale
import java.util.concurrent.Callable

class CameraStateOrchestrator(callback: Callback) : CameraOrchestrator(callback) {
    var currentState: CameraState = CameraState.OFF
        private set
    var targetState: CameraState = CameraState.OFF
        private set
    private var mStateChangeCount = 0

    fun hasPendingStateChange(): Boolean {
        synchronized(mJobsLock) {
            for (job in mJobs) {
                if ((job.name.contains(" >> ") || job.name.contains(" << "))
                    && !job.source.getTask().isComplete
                ) {
                    return true
                }
            }
            return false
        }
    }

    fun <T> scheduleStateChange(
        fromState: CameraState,
        toState: CameraState,
        dispatchExceptions: Boolean,
        stateChange: Callable<Task<T?>?>
    ): Task<T?> {
        val changeCount = ++mStateChangeCount
        this.targetState = toState

        val isTearDown = !toState.isAtLeast(fromState)
        val name = if (isTearDown)
            fromState.name + " << " + toState.name
        else
            fromState.name + " >> " + toState.name
        return schedule<T?>(name, dispatchExceptions) {
            if (this.currentState != fromState) {
                println("abcxyz, forCanceled: ${name.uppercase(Locale.getDefault())}")
                LOG.w(
                    name.uppercase(Locale.getDefault()), "- State mismatch, aborting. current:",
                    this.currentState, "from:", fromState, "to:", toState
                )
                return@schedule Tasks.forCanceled<T?>()
            } else {
                println("abcxyz, continueWithTask: ${name.uppercase(Locale.getDefault())}")
                val executor = mCallback.getJobWorker(name).executor
                return@schedule stateChange.call()!!.continueWithTask<T?>(
                    executor
                ) { task: Task<T?>? ->
                    if (task!!.isSuccessful || isTearDown) {
                        this.currentState = toState
                    }
                    task
                }
            }
        }.addOnCompleteListener { task: Task<T?>? ->
            println("abcxyz, completeTask: ${name.uppercase(Locale.getDefault())} done")
            if (changeCount == mStateChangeCount) {
                this.targetState = this.currentState
            }
        }
    }

    fun scheduleStateful(
        name: String,
        atLeast: CameraState,
        job: Runnable
    ): Task<Void?> {
        return schedule(name, true) {
            if (currentState.isAtLeast(atLeast)) {
                job.run()
            }
        }
    }

    fun scheduleStatefulDelayed(
        name: String,
        atLeast: CameraState,
        delay: Long,
        job: Runnable
    ) {
        scheduleDelayed(name, true, delay) {
            if (currentState.isAtLeast(atLeast)) {
                job.run()
            }
        }
    }
}
