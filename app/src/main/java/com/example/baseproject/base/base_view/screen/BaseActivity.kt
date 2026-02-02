package com.example.baseproject.base.base_view.screen

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.viewbinding.ViewBinding
import com.example.baseproject.base.ui.main.MyApplication
import com.example.baseproject.base.utils.extension.clickAnimation
import com.example.baseproject.base.utils.extension.finishWithSlide
import com.example.baseproject.base.utils.extension.getScreenHeight
import com.example.baseproject.base.utils.extension.getScreenWidth
import com.example.baseproject.base.utils.extension.handleBackPressed
import com.example.baseproject.base.utils.extension.setLayoutParamFullScreen
import com.example.baseproject.base.utils.util.AppLogger
import com.example.baseproject.base.utils.util.Constants
import com.example.baseproject.base.utils.util.RemoteConfigUtil
import com.example.baseproject.base.utils.util.SharePrefUtils
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.reflect.ParameterizedType
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * Base Activity cho tất cả các Activity trong app
 * Cung cấp các utility methods và setup cơ bản
 */
abstract class BaseActivity<VB : ViewBinding> : AppCompatActivity() {

    companion object {
        const val TAG = Constants.TAG
        const val TIME_DELAY_CLICK = 200L
    }

    // View binding instance
    lateinit var binding: VB

    // Click prevention
    private var isAvailableClick = true

    // Screen dimensions
    var screenWidth = 0
    var screenHeight = 0

    // Application instance
    val myApplication by lazy { application as MyApplication }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Setup fullscreen if needed
        if (isFullScreenMode()) setLayoutParamFullScreen()

        // Enable edge-to-edge display
        enableEdgeToEdge()

        super.onCreate(savedInstanceState)

        // Initialize view binding
        binding = createViewBinding()
        setContentView(binding.root)

        // Setup window insets
        setupWindowInsets()

        // Initialize utilities
        initializeUtilities()

        // Setup back press handling
        handleBackPressed { onBack() }

        // Initialize views and data
        initView()
        initData()
        initListener()
    }

    /**
     * Setup window insets for edge-to-edge display
     */
    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    /**
     * Initialize various utilities and services
     */
    private fun initializeUtilities() {
        SharePrefUtils.init(this)
        screenWidth = getScreenWidth()
        screenHeight = getScreenHeight()
        checkInitRemoteConfig()
    }

    /**
     * Initialize RemoteConfig if not already initialized
     */
    private fun checkInitRemoteConfig() {
        try {
            if (!RemoteConfigUtil.isInitialize(this)) {
                RemoteConfigUtil.init(this)
            }
        } catch (e: Exception) {
            Log.e(TAG, "checkInitRemoteConfig: error when try init RemoteConfig", e)
        }
    }

    /**
     * Handle back press action
     * Override this method to customize back press behavior
     */
    open fun onBack() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            onBackPressedDispatcher.onBackPressed()
        } else {
            finishWithSlide()
        }
    }

    /**
     * Determine if activity should run in fullscreen mode
     * Override this method to enable fullscreen
     */
    open fun isFullScreenMode(): Boolean = false

    /**
     * Inflate view binding - must be implemented by subclasses
     */
    @Suppress("UNCHECKED_CAST")
    private fun createViewBinding(): VB {
        return try {
            val type = getGenericSuperclass(javaClass)
            val vbClass = type.actualTypeArguments[0] as Class<VB>
            val method = vbClass.getMethod("inflate", LayoutInflater::class.java)
            method.invoke(null, layoutInflater) as VB
        } catch (e: Exception) {
            logError("Failed to create ViewBinding: ${e.message}")
            throw RuntimeException(
                "Cannot create ViewBinding for ${javaClass.simpleName}. Make sure you extend BaseActivityViewBinding with proper generic type.",
                e
            )
        }
    }

    private fun getGenericSuperclass(clazz: Class<*>): ParameterizedType {
        val genericSuperclass = clazz.genericSuperclass
        return if (genericSuperclass is ParameterizedType) {
            genericSuperclass
        } else {
            // Kế thừa nhiều tầng, tìm parent class
            clazz.superclass?.let { getGenericSuperclass(it) }
                ?: throw IllegalStateException("Cannot find ParameterizedType for ${clazz.simpleName}")
        }
    }

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

    /**
     * preventing font scaling from device settings
     * */
    override fun attachBaseContext(newBase: Context?) {
        val newOverrideConfiguration =
            Configuration(newBase?.resources?.configuration).apply { fontScale = 1.0f }
        applyOverrideConfiguration(newOverrideConfiguration)
        super.attachBaseContext(newBase)
    }

    /**
     * Delay click to prevent multiple rapid clicks
     */
    private fun delayClick() {
        isAvailableClick = false
        delayToAction(TIME_DELAY_CLICK) {
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

    //region Coroutine Management

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
     * Launch coroutine with error handling
     * @param dispatcher coroutine dispatcher (default: EmptyCoroutineContext)
     * @param blockCoroutine coroutine block to execute
     */
    fun launchCoroutine(
        dispatcher: CoroutineContext = EmptyCoroutineContext,
        blockCoroutine: suspend CoroutineScope.() -> Unit
    ) {
        try {
            if (isFinishing) {
                return
            }
            lifecycleScope.launch(dispatcher + coroutineExceptionHandler) {
                blockCoroutine()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Launch coroutine on Main dispatcher
     */
    fun launchCoroutineMain(blockCoroutine: suspend CoroutineScope.() -> Unit) =
        launchCoroutine(Dispatchers.Main, blockCoroutine)

    /**
     * Launch coroutine on IO dispatcher
     */
    fun launchCoroutineIO(blockCoroutine: suspend CoroutineScope.() -> Unit) =
        launchCoroutine(Dispatchers.IO, blockCoroutine)

    fun main(action: () -> Unit) {
        launchCoroutineMain {
            action()
        }
    }

    fun io(action: () -> Unit) {
        launchCoroutineIO {
            action()
        }
    }

    /**
     * Execute action after delay
     * @param delayTime delay in milliseconds (default: 200ms)
     * @param action action to perform after delay
     */
    fun delayToAction(delayTime: Long = 200L, action: () -> Unit) {
        launchCoroutineIO {
            delay(delayTime)
            launchCoroutineMain { action() }
        }
    }

    //endregion

    /**
     * Adjust view insets for bottom navigation compatibility
     * Useful when system actions like sharing might interfere
     * @param viewBottom view to adjust margins for
     */
    protected fun adjustInsetsForBottomNavigation(viewBottom: View) {
        ViewCompat.setOnApplyWindowInsetsListener(viewBottom) { view, insets ->
            try {
                val params = view.layoutParams as ViewGroup.MarginLayoutParams
                val displayCutout =
                    insets.getInsetsIgnoringVisibility(WindowInsetsCompat.Type.systemBars())
                params.topMargin = displayCutout.top
                params.leftMargin = displayCutout.left
                params.rightMargin = displayCutout.right
                params.bottomMargin = displayCutout.bottom
                view.layoutParams = params
            } catch (e: Exception) {
                e.printStackTrace()
            }
            insets
        }
    }

    override fun onResume() {
        super.onResume()
        // Reset click availability when activity is resumed
        isAvailableClick = true
    }

    override fun onPause() {
        super.onPause()
        // Reset click availability when activity is paused
        isAvailableClick = true
    }

    //region logging
    fun logInfo(msg: String) = AppLogger.i(TAG, "Rev[${this.javaClass.simpleName}] " + msg)
    fun logWarn(msg: String) = AppLogger.w(TAG, "Rev[${this.javaClass.simpleName}] " + msg)
    fun logDebug(msg: String) = AppLogger.d(TAG, "Rev[${this.javaClass.simpleName}] " + msg)
    fun logError(msg: String) = AppLogger.e(TAG, "Rev[${this.javaClass.simpleName}] " + msg)
    //endregion
}