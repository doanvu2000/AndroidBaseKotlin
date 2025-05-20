package com.base.cameraview.engine.orchestrator

import androidx.annotation.GuardedBy
import com.base.cameraview.CameraLogger
import com.base.cameraview.internal.WorkerHandler
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.android.gms.tasks.Tasks
import java.util.ArrayDeque
import java.util.Locale
import java.util.concurrent.Callable
import java.util.concurrent.CancellationException
import kotlin.math.max

/**
 * so that they always run on the same thread.
 *
 *
 * We need to be extra careful (not as easy as posting on a Handler) because the engine
 * has different states, and some actions will modify the engine state - turn it on or
 * tear it down. Other actions might need a specific state to be executed.
 * And most importantly, some actions will finish asynchronously, so subsequent actions
 * should wait for the previous to finish, but without blocking the thread.
 */
open class CameraOrchestrator(@JvmField protected val mCallback: Callback) {
    @JvmField
    protected val mJobs: ArrayDeque<Job<*>> = ArrayDeque<Job<*>>()

    @JvmField
    protected val mJobsLock: Any = Any()
    protected var mJobRunning: Boolean = false

    fun schedule(
        name: String,
        dispatchExceptions: Boolean,
        job: Runnable
    ): Task<Void?> {
        return scheduleDelayed(name, dispatchExceptions, 0L, job)
    }

    fun scheduleDelayed(
        name: String,
        dispatchExceptions: Boolean,
        minDelay: Long,
        job: Runnable
    ): Task<Void?> {
        return scheduleInternal<Void?>(name, dispatchExceptions, minDelay) {
            job.run()
            Tasks.forResult<Void?>(null)
        }
    }

    fun <T> schedule(
        name: String,
        dispatchExceptions: Boolean,
        scheduler: Callable<Task<T?>>
    ): Task<T?> {
        return scheduleInternal<T?>(name, dispatchExceptions, 0L, scheduler)
    }

    private fun <T> scheduleInternal(
        name: String,
        dispatchExceptions: Boolean,
        minDelay: Long,
        scheduler: Callable<Task<T?>>
    ): Task<T?> {
        LOG.i(name.uppercase(Locale.getDefault()), "- Scheduling.")
        val job = Job(
            name, scheduler, dispatchExceptions,
            System.currentTimeMillis() + minDelay
        )
        synchronized(mJobsLock) {
            mJobs.addLast(job)
            sync<T>(minDelay)
        }
        return job.source.getTask()
    }

    @GuardedBy("mJobsLock")
    private fun <T> sync(after: Long) {
        // Jumping on the message handler even if after = 0L should avoid StackOverflow errors.
        mCallback.getJobWorker("_sync").post(after) {
            var job: Job<T?>? = null
            synchronized(mJobsLock) {
                if (!mJobRunning) {
                    val now = System.currentTimeMillis()
                    for (candidate in mJobs) {
                        if (candidate.startTime <= now) {
                            job = candidate as Job<T?>?
                            break
                        }
                    }
                    if (job != null) {
                        mJobRunning = true
                    }
                }
            }
            // This must be out of mJobsLock! See comments in execute().
            val currentJob = job
            currentJob?.let {
                execute(it)
            }
        }
    }

    // Since we use WorkerHandler.run(), the job can end up being executed on the current thread.
    // For this reason, it's important that this method is never guarded by mJobsLock! Because
    // all threads can be waiting on that, even the UI thread e.g. through scheduleInternal.
    private fun <T> execute(job: Job<T?>) {
        val worker = mCallback.getJobWorker(job.name)
        worker.run {
            try {
                LOG.i(job.name.uppercase(Locale.getDefault()), "- Executing.")
                val task = job.scheduler.call()
                onComplete<T?>(task, worker) { task1: Task<T?>? ->
                    val e = task1!!.exception
                    if (e != null) {
                        LOG.w(job.name.uppercase(Locale.getDefault()), "- Finished with ERROR.", e)
                        if (job.dispatchExceptions) {
                            mCallback.handleJobException(job.name, e)
                        }
                        job.source.trySetException(e)
                    } else if (task1.isCanceled) {
                        LOG.i(
                            job.name.uppercase(Locale.getDefault()),
                            "- Finished because ABORTED."
                        )
                        job.source.trySetException(CancellationException())
                    } else {
                        LOG.i(job.name.uppercase(Locale.getDefault()), "- Finished.")
                        job.source.trySetResult(task1.getResult())
                    }
                    synchronized(mJobsLock) {
                        executed<T?>(job)
                    }
                }
            } catch (e: Exception) {
                LOG.i(job.name.uppercase(Locale.getDefault()), "- Finished with ERROR.", e)
                if (job.dispatchExceptions) {
                    mCallback.handleJobException(job.name, e)
                }
                job.source.trySetException(e)
                synchronized(mJobsLock) {
                    executed<T?>(job)
                }
            }
        }
    }

    @GuardedBy("mJobsLock")
    private fun <T> executed(job: Job<T?>) {
        check(mJobRunning) { "mJobRunning was not true after completing job=" + job.name }
        mJobRunning = false
        mJobs.remove(job)
        sync<T>(0L)
    }

    fun remove(name: String) {
        trim(name, 0)
    }

    fun trim(name: String, allowed: Int) {
        synchronized(mJobsLock) {
            var scheduled: MutableList<Job<*>?> = mutableListOf()
            for (job in mJobs) {
                if (job.name == name) {
                    scheduled.add(job)
                }
            }
            LOG.v("trim: name=", name, "scheduled=", scheduled.size, "allowed=", allowed)
            val existing = max(scheduled.size - allowed, 0)
            if (existing > 0) {
                // To remove the oldest ones first, we must reverse the list.
                // Note that we will potentially remove a job that is being executed: we don't
                // have a mechanism to cancel the ongoing execution, but it shouldn't be a problem.
                scheduled.reverse()
                scheduled = scheduled.subList(0, existing)
                for (job in scheduled) {
                    mJobs.remove(job)
                }
            }
        }
    }

    fun reset() {
        synchronized(mJobsLock) {
            val all: MutableSet<String> = HashSet()
            for (job in mJobs) {
                all.add(job.name)
            }
            for (job in all) {
                remove(job)
            }
        }
    }

    interface Callback {
        fun getJobWorker(job: String): WorkerHandler

        fun handleJobException(job: String, exception: Exception)
    }

    protected class Job<T>(
        @JvmField val name: String,
        val scheduler: Callable<Task<T?>>,
        val dispatchExceptions: Boolean,
        val startTime: Long
    ) {
        @JvmField
        val source: TaskCompletionSource<T?> = TaskCompletionSource<T?>()
    }

    companion object {
        val TAG: String = CameraOrchestrator::class.java.simpleName

        @JvmField
        val LOG: CameraLogger = CameraLogger.create(TAG)
        private fun <T> onComplete(
            task: Task<T?>,
            handler: WorkerHandler,
            listener: OnCompleteListener<T?>
        ) {
            if (task.isComplete) {
                handler.run { listener.onComplete(task) }
            } else {
                task.addOnCompleteListener(handler.executor, listener)
            }
        }
    }
}
