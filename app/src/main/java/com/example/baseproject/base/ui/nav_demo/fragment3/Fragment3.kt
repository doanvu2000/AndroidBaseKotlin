package com.example.baseproject.base.ui.nav_demo.fragment3

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.example.baseproject.R
import com.example.baseproject.base.base_view.screen.BaseFragment
import com.example.baseproject.base.utils.extension.launchOnStarted
import com.example.baseproject.base.utils.extension.safeNavigate
import com.example.baseproject.databinding.Fragment3Binding

class Fragment3 : BaseFragment<Fragment3Binding>() {
    override fun inflateLayout(
        inflater: LayoutInflater, container: ViewGroup?
    ): Fragment3Binding {
        return Fragment3Binding.inflate(inflater, container, false)
    }

    override fun onBack() {
        //disable back pressed
    }

    private val viewModel by viewModels<Fragment3ViewModel>()

    override fun initView() {

    }

    override fun initData() {
        launchOnStarted {
            viewModel.event.collect { event ->
                when (event) {
                    Fragment3ViewModel.EventScreen3.OnBack -> {
                        safeNavigate(R.id.action_fragment3_to_fragment1)
                    }
                }
            }
        }
    }

    override fun initListener() {
        binding.btnBack.clickSafe(isAnimationClick = true) {
            viewModel.onBack()
        }
    }
}