package com.example.baseproject.base.ui.main

import android.annotation.SuppressLint
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.baseproject.R
import com.example.baseproject.base.base_view.BaseActivity
import com.example.baseproject.base.base_view.HomeFragment
import com.example.baseproject.base.ui.demo_viewpager.DemoViewPagerActivity
import com.example.baseproject.base.utils.Constant
import com.example.baseproject.base.utils.extension.clear
import com.example.baseproject.base.utils.extension.hide
import com.example.baseproject.base.utils.extension.openActivity
import com.example.baseproject.base.utils.extension.runOnDispatcherIO
import com.example.baseproject.base.utils.extension.show
import com.example.baseproject.base.utils.extension.showToast
import com.example.baseproject.base.utils.extension.toStringFormat
import com.example.baseproject.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch
import kotlin.random.Random


class MainActivity : BaseActivity<ActivityMainBinding>() {
    companion object {
        const val TAG = Constant.TAG
    }

    override fun initView() {
        //nothing
    }

    override fun initData() {
    }

    override fun initListener() {
        binding.btnOpenFragment.setOnClickListener {
            supportFragmentManager.beginTransaction().replace(R.id.frameContainer, HomeFragment()).commit()
//            randomAndSortList()
        }
        binding.btnRemoveFragment.setOnClickListener {
            val oldFragment: Fragment? = supportFragmentManager.findFragmentById(R.id.frameContainer)
            if (oldFragment != null) {
                supportFragmentManager.beginTransaction().remove(oldFragment).commit()
            }
        }
        binding.btnDemoViewPager.clickSafe {
            openActivity(DemoViewPagerActivity::class.java, isFinish = false, true, null)
        }
    }

    private fun randomAndSortList() {
        val number = binding.edtNumberSize.text.toString().toIntOrNull()
        number?.let {
            if (it > 5000000) {
                showToast("Size is too large!!")
                return@let
            }
            mapList(it)
        } ?: kotlin.run {
            showToast("Enter list size!!")
        }
    }

    private val coroutineException = CoroutineExceptionHandler { coroutineContext, throwable ->
        binding.tvResult.text = throwable.message
    }

    @SuppressLint("SetTextI18n")
    private fun mapList(size: Int) {
        val list = mutableListOf<Int>()
        var timeStart = 0L
        var timeEnd = 0L
        binding.tvTitle.text = "Init and sort list random with size: ${size.toStringFormat()}"
        binding.tvResult.clear()
        lifecycleScope.launch(coroutineException) {

            timeStart = System.currentTimeMillis()
            showLoading()
            runOnDispatcherIO {
                for (i in 1..size) {
                    list.add(Random.nextInt(-10000, 10000))
                }
            }
            var rs = "Init list random int: ${System.currentTimeMillis() - timeStart} ms\n"
            timeStart = System.currentTimeMillis()
            binding.tvResult.text = rs
            runOnDispatcherIO {
                list.sort()
            }
            timeEnd = System.currentTimeMillis()
            rs += "Sort list: ${timeEnd - timeStart} ms"
            binding.tvResult.text = rs
            hideLoading()
            list.clear()
        }
    }

    private fun showLoading() {
        binding.loading.show()
    }

    private fun hideLoading() {
        binding.loading.hide()
    }

    var rs = 0
    override fun inflateViewBinding(inflater: LayoutInflater): ActivityMainBinding {
        return ActivityMainBinding.inflate(inflater)
    }
}