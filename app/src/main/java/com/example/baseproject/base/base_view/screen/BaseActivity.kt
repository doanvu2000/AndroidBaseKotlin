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
import com.example.baseproject.base.utils.util.Constants
import com.example.baseproject.base.utils.util.RemoteConfigUtil
import com.example.baseproject.base.utils.util.SharePrefUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

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
        initView()
        initData()
        initListener()
        handleBackPressed {
            onBack()
        }
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

//    fun View.clickSafe(action: () -> Unit) {
//        this.setOnClickListener {
//            if (isAvailableClick) {
//                action()
//                delayClick()
//            }
//        }
//    }

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

    fun launchAction(dispatcher: CoroutineContext, action: () -> Unit) {
        try {
            lifecycleScope.launch(dispatcher) {
                action()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun launchCoroutine(
        dispatcher: CoroutineContext, blockCoroutine: suspend CoroutineScope.() -> Unit
    ) {
        try {
            lifecycleScope.launch(dispatcher) {
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
}