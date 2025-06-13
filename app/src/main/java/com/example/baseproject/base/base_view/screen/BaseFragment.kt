package com.example.baseproject.base.base_view.screen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.viewbinding.ViewBinding
import com.example.baseproject.base.utils.extension.handleBackPressed
import com.example.baseproject.base.utils.util.AppLogger
import com.example.baseproject.base.utils.util.Constants
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

abstract class BaseFragment<VB : ViewBinding> : Fragment() {
    companion object {
        const val TAG = Constants.TAG
    }

    private var _binding: VB? = null
    val binding: VB get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = inflateLayout(inflater, container)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initData()
        initListener()
//        handleOnBackPressed()
    }

    private fun handleOnBackPressed() {
        requireActivity().handleBackPressed {
            onBack()
        }
    }

    open fun onBack() {
        requireActivity().onBackPressedDispatcher.onBackPressed()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    /**override it and inflate your view binding, demo in HomeFragment*/
    abstract fun inflateLayout(inflater: LayoutInflater, container: ViewGroup?): VB

    abstract fun initView()
    abstract fun initData()
    abstract fun initListener()

    var isAvailableClick = true

    fun delayClick() {
        launchCoroutineIO {
            isAvailableClick = false
            delay(200)
            isAvailableClick = true
        }
    }

    fun View.clickSafe(action: () -> Unit) {
        this.setOnClickListener {
            if (isAvailableClick) {
                action()
                delayClick()
            }
        }
    }

    //region launch coroutine with lifecycleScope

    protected val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        handleError(throwable)
    }

    open fun handleError(throwable: Throwable) {
        val errorMessage = throwable.message ?: ""
        logError(errorMessage)
        throwable.printStackTrace()
    }

    fun launchCoroutine(
        dispatcher: CoroutineContext = EmptyCoroutineContext, action: suspend () -> Unit
    ) {
        if (view != null && isAdded) {
            viewLifecycleOwner.lifecycleScope.launch(dispatcher + coroutineExceptionHandler) {
                action()
            }
        }
    }

    fun launchCoroutineIO(action: suspend () -> Unit) {
        launchCoroutine(Dispatchers.IO) {
            action()
        }
    }

    fun launchCoroutineMain(action: suspend () -> Unit) {
        launchCoroutine(Dispatchers.Main) {
            action()
        }
    }

    fun launchCoroutineDefault(action: suspend () -> Unit) {
        launchCoroutine(Dispatchers.Default) {
            action()
        }
    }

    fun delayToAction(delayTime: Long = 200L, action: () -> Unit) {
        launchCoroutine(Dispatchers.IO) {
            delay(delayTime)
            launchCoroutine(Dispatchers.Main) {
                action()
            }

        }
    }
    //endregion

    //region logcat
    fun logError(msg: String) {
        AppLogger.e(TAG, "Fragment ${this.javaClass.simpleName} - Error: $msg")
    }

    fun logDebug(msg: String) {
        AppLogger.d(TAG, "Fragment ${this.javaClass.simpleName}: $msg")
    }

    fun logInfo(msg: String) {
        AppLogger.i(TAG, "Fragment ${this.javaClass.simpleName}: $msg")
    }

    fun logWarning(msg: String) {
        AppLogger.w(TAG, "Fragment ${this.javaClass.simpleName}: $msg")
    }
    //endregion
}