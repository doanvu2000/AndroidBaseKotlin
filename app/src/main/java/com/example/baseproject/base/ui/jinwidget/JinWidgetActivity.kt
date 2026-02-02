package com.example.baseproject.base.ui.jinwidget

import androidx.core.widget.doAfterTextChanged
import com.example.baseproject.base.base_view.screen.BaseActivity
import com.example.baseproject.databinding.ActivityJinWidgetBinding

class JinWidgetActivity : BaseActivity<ActivityJinWidgetBinding>() {

    override fun initView() {

    }

    override fun initData() {
        binding.edtInput.doAfterTextChanged { text ->
            binding.magicTextView.text = "MagicTextView: $text"
            binding.strokedTextView.text = "StrokedTextView: $text"
        }
    }

    override fun initListener() {
        binding.btnBack.clickSafe {
            onBack()
        }
    }
}