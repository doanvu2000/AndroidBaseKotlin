package com.example.baseproject.base.ui.nav_demo.fragment2

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.example.baseproject.R
import com.example.baseproject.base.base_view.screen.BaseFragment
import com.example.baseproject.base.utils.extension.launchOnStarted
import com.example.baseproject.base.utils.extension.navigateUp
import com.example.baseproject.base.utils.extension.safeNavigate
import com.example.baseproject.databinding.Fragment2Binding

class Fragment2 : BaseFragment<Fragment2Binding>() {
    override fun inflateLayout(
        inflater: LayoutInflater, container: ViewGroup?
    ): Fragment2Binding {
        return Fragment2Binding.inflate(inflater, container, false)
    }

    override fun onBack() {
        //disable back pressed
    }

    private val viewModel by viewModels<Fragment2ViewModel>()

    @SuppressLint("SetTextI18n")
    override fun initView() {
        val userName = arguments?.getString("userName", "") ?: ""
        binding.tvResult.text = "Text from Fragment 1: $userName"
    }

    override fun initData() {
        launchOnStarted {
            viewModel.event.collect { event ->
                when (event) {
                    Fragment2ViewModel.EventScreen2.ClickGotoFragment3 -> {
                        safeNavigate(R.id.action_fragment2_to_fragment3)
                    }

                    Fragment2ViewModel.EventScreen2.OnBack -> {
                        navigateUp()
                    }
                }
            }
        }
    }

    override fun initListener() {
        binding.btnNextTo3.clickSafe(isAnimationClick = true) {
            viewModel.clickGotoFragment3()
        }

        binding.btnBack.clickSafe(isAnimationClick = true) {
            viewModel.onBack()
        }
    }
}