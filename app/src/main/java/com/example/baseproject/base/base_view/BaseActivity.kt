package com.example.baseproject.base.base_view

import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.lifecycleScope
import androidx.viewbinding.BuildConfig
import androidx.viewbinding.ViewBinding
import com.example.baseproject.base.utils.Constant

abstract class BaseActivity<VB : ViewBinding> : AppCompatActivity(), LifecycleObserver {
    private var _binding: VB? = null
    protected val binding: VB
        get() = requireNotNull(_binding)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = inflateViewBinding(layoutInflater)
        setContentView(requireNotNull(_binding).root)
        lifecycle.addObserver(this)
        initView()
        initData()
        initListener()
        lifecycleScope.launchWhenResumed {
            if (BuildConfig.DEBUG) {
                println("${Constant.TAG} SCREEN_APP ${this@BaseActivity::class.java.name}")
            }
        }
    }

    abstract fun initView()
    abstract fun initData()
    abstract fun initListener()

    /**override it and inflate your view binding, demo in MainActivity*/
    abstract fun inflateViewBinding(inflater: LayoutInflater): VB

    override fun onDestroy() {
        _binding = null
        lifecycle.removeObserver(this)
        super.onDestroy()
    }
}