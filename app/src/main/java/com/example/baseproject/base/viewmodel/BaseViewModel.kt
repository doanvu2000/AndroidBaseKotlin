package com.example.baseproject.base.viewmodel

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.baseproject.base.entity.MessageVM
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

abstract class BaseViewModel : ViewModel(), DefaultLifecycleObserver {
    private val viewModelJob = SupervisorJob()
    private val viewModelScope: CoroutineScope =
        CoroutineScope(viewModelJob + Dispatchers.Main.immediate)

    /**catch exception when using coroutine*/
    private val exceptionHandler = CoroutineExceptionHandler { context, throwable ->
        throwable.printStackTrace()
        handlerError(throwable)
        isLoading.value = false
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

    //TODO: err: ErrorResponse
    private fun handlerError(
        throwable: Throwable? = null,
        errors: ((err: String) -> Unit)? = null
    ) {
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
                        when(response.code()){
                            401 ->{
                                response.errorBody()?.string()?.let { errMsg ->
                                    errors?.invoke(errMsg)
                                }
                            }
                            else ->{
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
}