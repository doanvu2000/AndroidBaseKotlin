package com.example.baseproject.base.viewmodel

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.pdf.PdfDocument
import android.view.View
import androidx.core.graphics.createBitmap
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.baseproject.base.utils.extension.now
import com.example.baseproject.base.utils.extension.showToast
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.File
import java.io.FileOutputStream
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

abstract class BaseViewModel : ViewModel(), DefaultLifecycleObserver {
    private val viewModelJob = SupervisorJob()
    private val viewModelScope: CoroutineScope =
        CoroutineScope(viewModelJob + Dispatchers.Main.immediate)

    /**catch exception when using coroutine*/
    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        throwable.printStackTrace()
        handlerError(throwable)
        isLoading.value = false
        setStateIsFailed()
    }

    private var onError: ((err: String?) -> Unit)? = null

    protected fun launchHandler(
        onError: ((err: String?) -> Unit)? = null,
        block: suspend CoroutineScope.() -> Unit,
    ) {
        this@BaseViewModel.onError = onError
        viewModelScope.launch(exceptionHandler, block = block)
    }

    /**observer to show or hide loading*/
    val isLoading by lazy { MutableLiveData<Boolean>() }

    private val _loadingState: MutableStateFlow<LoadingState> = MutableStateFlow(LoadingState.Init)
    val loadingState: StateFlow<LoadingState> = _loadingState.asStateFlow()

    private val _errorExceptionHandler: MutableStateFlow<Throwable?> = MutableStateFlow(null)
    val errorExceptionHandler: StateFlow<Throwable?> = _errorExceptionHandler.asStateFlow()

    fun launchSafe(
        context: CoroutineContext = EmptyCoroutineContext, block: suspend CoroutineScope.() -> Unit
    ) {
        viewModelScope.launch(context + exceptionHandler) { block() }
    }

    fun updateLoadingState(state: LoadingState) {
        launchSafe {
            _loadingState.update { state }
        }
    }

    fun setStateIsLoading() {
        updateLoadingState(LoadingState.Loading)
    }

    fun setStateIsSuccess() {
        updateLoadingState(LoadingState.Success)
    }

    fun setStateIsFailed() {
        updateLoadingState(LoadingState.Failed)
    }

    //TODO: err: ErrorResponse
    private fun handlerError(
        throwable: Throwable? = null,
        errors: ((err: String) -> Unit)? = null
    ) {
        launchSafe {
            _errorExceptionHandler.update { throwable }
        }

        throwable?.let {
            println("ddd - ${it.cause}")
            when (it.cause) {
                is UnknownHostException -> {
                    onError?.invoke(it.message ?: "UnknownHostException")
                }

                is java.net.ConnectException -> {
                    onError?.invoke(it.message ?: "java.net.ConnectException")
                }

                is SocketTimeoutException -> {
                    onError?.invoke(it.message ?: "SocketTimeoutException")
                }

                is HttpException -> {
                    (it as HttpException).response()?.let { response ->
                        when (response.code()) {
                            401 -> {
                                response.errorBody()?.string()?.let { errMsg ->
                                    errors?.invoke(errMsg)
                                }
                            }

                            else -> {
                                response.errorBody()?.string()?.let { errMsg ->
                                    errors?.invoke(errMsg)
                                }
                            }
                        }
                    }
                }

                else -> {
                    onError?.invoke(null)
                }
            }
        } ?: kotlin.run {
            onError?.invoke(null)
        }
    }

    protected fun launchHandler(
        block: suspend CoroutineScope.() -> Unit
    ) {
        viewModelScope.launch(exceptionHandler) {
            block()
        }
    }

    fun <T> flowOnIO(value: T) = flow {
        emit(value)
    }.flowOn(Dispatchers.IO)

    fun <T> Flow<T>.execute(
        errors: ((err: String) -> Unit)? = null,
        success: (T) -> Unit
    ) {
        onStart {
            isLoading.value = true
        }.onEach {
            it?.let { data ->
                withContext(Dispatchers.Main) {
                    success.invoke(data)
                    isLoading.value = false
                }
            } ?: kotlin.run {
                isLoading.value = false
            }
        }.catch {
            isLoading.value = false
            Throwable(it).also { throwable ->
                handlerError(throwable, errors)
                throwable.printStackTrace()
            }
        }.launchIn(viewModelScope)
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
        viewModelScope.cancel()
    }

    /**
     * just call in onDestroy of Activity and Fragment
     * */
    fun clear() {
        onCleared()
    }

    fun exportPdf(view: View, totalWidth: Int, totalHeight: Int, onDone: (file: File) -> Unit) {
        val context = view.context
        launchHandler {
            flow {
                val fileName = "Demo_${now()}.pdf"
                val dir = File(context.cacheDir, "shared_files")
                if (!dir.exists()) {
                    dir.mkdirs()
                }
                val file = File(dir, fileName)
                val bitmap = getBitmapFromView(view, totalWidth, totalHeight)
                if (bitmap == null) {
                    emit(null)
                    return@flow
                }
                val document = PdfDocument()
                val pageInfo = PdfDocument.PageInfo.Builder(totalWidth, totalHeight, 1).create()
                val page = document.startPage(pageInfo)
                bitmap.prepareToDraw()
                val c: Canvas = page.canvas
                c.drawBitmap(bitmap, 0f, 0f, null)
                document.finishPage(page)
                document.writeTo(FileOutputStream(file))
                document.close()
                bitmap.recycle()
                emit(file)
            }.flowOn(Dispatchers.IO).catch {
                it.printStackTrace()
            }.execute { file ->
                if (file == null) {
                    context.showToast("Export Error!")
                } else {
                    onDone.invoke(file)
                }
            }
        }
    }

    private fun getBitmapFromView(view: View, totalWidth: Int, totalHeight: Int): Bitmap? {
        return try {
            val returnedBitmap = createBitmap(totalWidth, totalHeight)
            val canvas = Canvas(returnedBitmap)
            val bgDrawable = view.background
            if (bgDrawable != null) bgDrawable.draw(canvas) else canvas.drawColor(Color.WHITE)
            view.draw(canvas)
            returnedBitmap
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

}

sealed class LoadingState {
    object Init : LoadingState()
    object Loading : LoadingState()
    object Success : LoadingState()
    object Failed : LoadingState()
}

fun <T> BaseViewModel.sendEvent(channel: Channel<T>, value: T) {
    launchSafe {
        channel.send(value)
    }
}