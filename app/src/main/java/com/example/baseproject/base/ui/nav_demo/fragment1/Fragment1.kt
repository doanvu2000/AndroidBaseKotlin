package com.example.baseproject.base.ui.nav_demo.fragment1

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import com.example.baseproject.R
import com.example.baseproject.base.base_view.screen.BaseFragment
import com.example.baseproject.base.utils.extension.launchOnStarted
import com.example.baseproject.base.utils.extension.safeNavigate
import com.example.baseproject.databinding.Fragment1Binding

class Fragment1 : BaseFragment<Fragment1Binding>() {
    override fun inflateLayout(
        inflater: LayoutInflater, container: ViewGroup?
    ): Fragment1Binding {
        return Fragment1Binding.inflate(inflater, container, false)
    }

    override fun onBack() {

    }

    private val viewModel by viewModels<Fragment1ViewModel>()

    override fun initView() {

    }

    override fun initData() {
        launchOnStarted {
            viewModel.event.collect { event ->
                when (event) {
                    is Fragment1ViewModel.EventScreen1.ClickGotoFragment2 -> {
                        safeNavigate(
                            R.id.action_fragment1_to_fragment2, bundle = bundleOf(
                                "userName" to event.userName
                            )
                        )
                    }
                }
            }
        }
    }

    override fun initListener() {
        binding.btnNext.clickSafe(isAnimationClick = true) {
            viewModel.clickGotoFragment2(binding.edtUserName.text.toString())
        }
    }
}