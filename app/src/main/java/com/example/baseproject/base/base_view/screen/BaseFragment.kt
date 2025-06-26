package com.example.baseproject.base.base_view.screen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.viewbinding.ViewBinding
import com.example.baseproject.base.utils.extension.clickAnimation
import com.example.baseproject.base.utils.extension.handleBackPressed
import com.example.baseproject.base.utils.util.AppLogger
import com.example.baseproject.base.utils.util.Constants
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * Base Fragment cho tất cả các Fragment trong app
 * Cung cấp các utility methods và setup cơ bản cho Fragment
 */
abstract class BaseFragment<VB : ViewBinding> : Fragment() {

    companion object {
        const val TAG = Constants.TAG
        const val TIME_DELAY_CLICK = 200L
    }

    // View binding instance với null safety
    private var _binding: VB? = null
    val binding: VB get() = _binding!!

    // Click prevention
    private var isAvailableClick = true

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

        // Initialize fragment components
        initView()
        initData()
        initListener()

        // Setup back press handling if needed
        // handleOnBackPressed() // Uncomment if needed
    }

    /**
     * Handle back press for fragment
     * Setup back press callback for this fragment
     */
    private fun handleOnBackPressed() {
        requireActivity().handleBackPressed { onBack() }
    }

    /**
     * Override this method to customize back press behavior
     * Default behavior: delegate to activity's back press dispatcher
     */
    open fun onBack() {
        requireActivity().onBackPressedDispatcher.onBackPressed()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Clear binding reference to prevent memory leaks
        _binding = null
    }

    //region Abstract Methods - Must be implemented by subclasses

    /**
     * Inflate layout và tạo view binding
     * Override method này và inflate your view binding, demo in HomeFragment
     */
    abstract fun inflateLayout(inflater: LayoutInflater, container: ViewGroup?): VB

    /**
     * Initialize views - must be implemented by subclasses
     */
    abstract fun initView()

    /**
     * Initialize data - must be implemented by subclasses
     */
    abstract fun initData()

    /**
     * Initialize listeners - must be implemented by subclasses
     */
    abstract fun initListener()

    //endregion

    //region Click Management

    /**
     * Delay click to prevent multiple rapid clicks
     */
    private fun delayClick() {
        launchCoroutineIO {
            isAvailableClick = false
            delay(TIME_DELAY_CLICK)
            isAvailableClick = true
        }
    }

    /**
     * Safe click extension to prevent multiple rapid clicks
     * @param isAnimationClick enable click animation
     * @param action action to perform on click
     */
    fun View.clickSafe(isAnimationClick: Boolean = false, action: () -> Unit) {
        setOnClickListener {
            if (isAvailableClick) {
                if (isAnimationClick) clickAnimation()
                action()
                delayClick()
            }
        }
    }

    //endregion

    //region Coroutine Management với lifecycleScope

    /**
     * Global exception handler for coroutines
     */
    protected val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        handleError(throwable)
    }

    /**
     * Handle coroutine errors
     * Override this method to customize error handling
     */
    open fun handleError(throwable: Throwable) {
        val errorMessage = throwable.message ?: "Unknown error"
        logError(errorMessage)
        throwable.printStackTrace()
    }

    /**
     * Launch coroutine with lifecycle awareness and error handling
     * @param dispatcher coroutine dispatcher (default: EmptyCoroutineContext)
     * @param action coroutine block to execute
     */
    fun launchCoroutine(
        dispatcher: CoroutineContext = EmptyCoroutineContext,
        action: suspend () -> Unit
    ) {
        // Check if fragment is still valid before launching coroutine
        if (view != null && isAdded) {
            viewLifecycleOwner.lifecycleScope.launch(dispatcher + coroutineExceptionHandler) {
                action()
            }
        }
    }

    /**
     * Launch coroutine on IO dispatcher
     */
    fun launchCoroutineIO(action: suspend () -> Unit) =
        launchCoroutine(Dispatchers.IO, action)

    /**
     * Launch coroutine on Main dispatcher
     */
    fun launchCoroutineMain(action: suspend () -> Unit) =
        launchCoroutine(Dispatchers.Main, action)

    /**
     * Launch coroutine on Default dispatcher
     */
    fun launchCoroutineDefault(action: suspend () -> Unit) =
        launchCoroutine(Dispatchers.Default, action)

    /**
     * Execute action after delay
     * @param delayTime delay in milliseconds (default: 200ms)
     * @param action action to perform after delay
     */
    fun delayToAction(delayTime: Long = TIME_DELAY_CLICK, action: () -> Unit) {
        launchCoroutine(Dispatchers.IO) {
            delay(delayTime)
            launchCoroutine(Dispatchers.Main) { action() }
        }
    }

    //endregion

    //region Logging Utilities

    /**
     * Log error message with fragment info
     */
    fun logError(msg: String) =
        AppLogger.e(TAG, "Fragment ${this.javaClass.simpleName} - Error: $msg")

    /**
     * Log debug message with fragment info
     */
    fun logDebug(msg: String) =
        AppLogger.d(TAG, "Fragment ${this.javaClass.simpleName}: $msg")

    /**
     * Log info message with fragment info
     */
    fun logInfo(msg: String) =
        AppLogger.i(TAG, "Fragment ${this.javaClass.simpleName}: $msg")

    /**
     * Log warning message with fragment info
     */
    fun logWarning(msg: String) =
        AppLogger.w(TAG, "Fragment ${this.javaClass.simpleName}: $msg")

    //endregion
}