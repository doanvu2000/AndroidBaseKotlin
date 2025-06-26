package com.example.baseproject.base.utils.extension

import com.example.baseproject.base.base_view.screen.BaseActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Run a background task with start and finish callbacks in an Activity.
 */
fun <T> BaseActivity<*>.asyncTask(
    onStart: () -> Unit,
    doInBackground: suspend () -> T,
    onFinish: (T) -> Unit
) {
    launchCoroutineMain {
        onStart()
        val result = withContext(Dispatchers.IO) { doInBackground() }
        onFinish(result)
    }
}

/**
 * Run a background task with start and finish callbacks in a CoroutineScope.
 */
fun CoroutineScope.asyncTask(
    onStart: () -> Unit,
    doInBackground: suspend () -> Unit,
    onFinish: () -> Unit
) {
    launch(Dispatchers.Main) {
        onStart()
        val job = async(Dispatchers.IO) { doInBackground() }
        job.await()
        onFinish()
    }
}