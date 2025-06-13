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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

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

    fun launchAction(dispatcher: CoroutineContext, action: () -> Unit) {
        try {
            view?.let {
                viewLifecycleOwner.lifecycleScope.launch(dispatcher) {
                    action()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun launchCoroutine(
        dispatcher: CoroutineContext, blockCoroutine: suspend CoroutineScope.() -> Unit
    ) {
        try {
            view?.let {
                viewLifecycleOwner.lifecycleScope.launch(dispatcher) {
                    blockCoroutine()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun launchCoroutineMain(blockCoroutine: suspend CoroutineScope.() -> Unit) {
        launchCoroutine(Dispatchers.Main) {
            blockCoroutine()
        }
    }

    fun launchCoroutineIO(blockCoroutine: suspend CoroutineScope.() -> Unit) {
        launchCoroutine(Dispatchers.IO) {
            blockCoroutine()
        }
    }

    fun launchCoroutineDefault(blockCoroutine: suspend CoroutineScope.() -> Unit) {
        launchCoroutine(Dispatchers.Default) {
            blockCoroutine()
        }
    }

    fun delayToAction(delayTime: Long = 200L, action: () -> Unit) {
        launchCoroutineIO {
            delay(delayTime)
            launchCoroutineMain {
                action()
            }
        }
    }

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