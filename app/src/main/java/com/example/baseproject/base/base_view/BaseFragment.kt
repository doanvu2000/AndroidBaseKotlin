package com.example.baseproject.base.base_view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.viewbinding.BuildConfig
import androidx.viewbinding.ViewBinding
import com.example.baseproject.base.utils.Constant

abstract class BaseFragment<VB : ViewBinding> : Fragment(), LifecycleObserver {
    private var _binding: VB? = null
    protected val binding
        get() = requireNotNull(_binding)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = inflateLayout(inflater, container)
        return requireNotNull(_binding).root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            if (BuildConfig.DEBUG) {
                println("${Constant.TAG} SCREEN_APP ${this@BaseFragment::class.java.name}")
            }
        }
        viewLifecycleOwner.lifecycle.addObserver(this)
        initView()
        initData()
        initListener()
    }

    abstract fun initView()
    abstract fun initData()
    abstract fun initListener()

    /**override it and inflate your view binding, demo in HomeFragment*/
    abstract fun inflateLayout(inflater: LayoutInflater, container: ViewGroup?): VB

    override fun onDestroyView() {
        _binding = null
        viewLifecycleOwner.lifecycle.removeObserver(this)
        super.onDestroyView()
    }
}