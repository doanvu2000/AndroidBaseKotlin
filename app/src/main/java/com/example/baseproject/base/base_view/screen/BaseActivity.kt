package com.example.baseproject.base.base_view.screen

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
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

abstract class BaseActivity<VB : ViewBinding> : AppCompatActivity() {
    //region variable
    companion object {
        const val TAG = Constants.TAG
        const val TIME_DELAY_CLICK = 200L
    }

    lateinit var binding: VB
    private var isAvailableClick = true

    var screenWidth = 0
    var screenHeight = 0

    val myApplication by lazy {
        application as MyApplication
    }
    //endregion

    override fun onCreate(savedInstanceState: Bundle?) {
        if (isFullScreenMode()) {
            setLayoutParamFullScreen()
        }
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        binding = inflateViewBinding(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        SharePrefUtils.init(this)

        screenWidth = getScreenWidth()

        screenHeight = getScreenHeight()
        checkInitRemoteConfig()

        handleBackPressed {
            onBack()
        }

        initView()
        initData()
        initListener()
    }

    private fun checkInitRemoteConfig() {
        try {
            if (!RemoteConfigUtil.isInitialize(this)) {
                RemoteConfigUtil.init(this)
            }
        } catch (e: Exception) {
            Log.e(TAG, "checkInitRemoteConfig: error when try init RemoteConfig")
        }
    }

    open fun onBack() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            onBackPressedDispatcher.onBackPressed()
        } else {
            finishWithSlide()
        }
    }

    open fun isFullScreenMode(): Boolean {
        return false
    }

    /**override it and inflate your view binding, demo in MainActivity*/
    abstract fun inflateViewBinding(inflater: LayoutInflater): VB

    abstract fun initView()
    abstract fun initData()
    abstract fun initListener()


    private fun delayClick() {
        launchCoroutineIO {
            isAvailableClick = false
            delay(TIME_DELAY_CLICK)
            isAvailableClick = true
        }
    }

    fun View.clickSafe(isAnimationClick: Boolean = false, action: () -> Unit) {
        this.setOnClickListener {
            if (isAvailableClick) {
                if (isAnimationClick) {
                    clickAnimation()
                }
                action()
                delayClick()
            }
        }
    }

    protected val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        handleError(throwable)
    }

    open fun handleError(throwable: Throwable) {
        val errorMessage = throwable.message ?: ""
        logError(errorMessage)
        throwable.printStackTrace()
    }

    fun launchCoroutine(
        dispatcher: CoroutineContext = EmptyCoroutineContext,
        blockCoroutine: suspend CoroutineScope.() -> Unit
    ) {
        try {
            lifecycleScope.launch(dispatcher + coroutineExceptionHandler) {
                blockCoroutine()
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

    fun delayToAction(delayTime: Long = 200L, action: () -> Unit) {
        launchCoroutineIO {
            delay(delayTime)
            launchCoroutineMain {
                action()
            }
        }
    }

    /**
     * Ignore margin bottom when action compat of system, such as: share
     * */
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

    override fun onPause() {
        super.onPause()
        isAvailableClick = true
    }

    //region logcat
    fun logDebug(msg: String) {
        AppLogger.d(TAG, "${this.javaClass.simpleName}: $msg")
    }

    fun logWarning(msg: String) {
        AppLogger.w(TAG, "${this.javaClass.simpleName}: $msg")
    }

    fun logError(msg: String) {
        AppLogger.e(TAG, "${this.javaClass.simpleName}: $msg")
    }

    fun logInfo(msg: String) {
        AppLogger.i(TAG, "${this.javaClass.simpleName}: $msg")
    }
    //endregion
}