package com.example.baseproject.base.utils.extension

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


fun <T> AppCompatActivity.asyncTask(
    onStartJob: () -> Unit, doInBackground: CoroutineScope.() -> T, onFinish: (T) -> Unit
) {
    lifecycleScope.launch(Dispatchers.Main) {
        onStartJob()
        val result = withContext(Dispatchers.IO) {
            return@withContext doInBackground()
        }
        onFinish(result)
    }
}

/**
 * @example: call in Activity:
 *  lifecycleScope.asyncTask(
 *      onStartJob = {},
 *      doInBackground = {},
 *      onFinish = {}
 *  )
 * */
fun CoroutineScope.asyncTask(
    onStartJob: () -> Unit, doInBackground: CoroutineScope.() -> Unit, onFinish: () -> Unit
) {
    launch(Dispatchers.Main) {
        onStartJob()
        val result = async {
            withContext(Dispatchers.IO) {
                doInBackground()
            }
        }
        result.await().also {
            onFinish()
        }
    }
}