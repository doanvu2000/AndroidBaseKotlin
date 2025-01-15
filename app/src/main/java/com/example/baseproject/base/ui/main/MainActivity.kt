package com.example.baseproject.base.ui.main

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.baseproject.R
import com.example.baseproject.base.base_view.screen.BaseActivity
import com.example.baseproject.base.base_view.screen.HomeFragment
import com.example.baseproject.base.ui.ads.DemoAdsActivity
import com.example.baseproject.base.ui.demo_viewpager.DemoViewPagerActivity
import com.example.baseproject.base.ui.flex_box_layout.DemoFlexBoxLayoutActivity
import com.example.baseproject.base.ui.location.LocationActivity
import com.example.baseproject.base.ui.lunar_calendar.LunarCalendarActivity
import com.example.baseproject.base.ui.network_demo.NetWorkDemoActivity
import com.example.baseproject.base.ui.pick_image.PickImageActivity
import com.example.baseproject.base.ui.pick_image.TakePictureActivity
import com.example.baseproject.base.ui.test_coil.CoilActivity
import com.example.baseproject.base.utils.extension.clear
import com.example.baseproject.base.utils.extension.hide
import com.example.baseproject.base.utils.extension.isEmulator
import com.example.baseproject.base.utils.extension.openActivity
import com.example.baseproject.base.utils.extension.removeFragment
import com.example.baseproject.base.utils.extension.runOnDispatcherIO
import com.example.baseproject.base.utils.extension.show
import com.example.baseproject.base.utils.extension.showToast
import com.example.baseproject.base.utils.extension.toStringFormat
import com.example.baseproject.base.utils.util.Constants
import com.example.baseproject.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch
import kotlin.random.Random


class MainActivity : BaseActivity<ActivityMainBinding>() {
    companion object {
        const val TAG = Constants.TAG
    }

    override fun initView() {
        //nothing
        Log.d(TAG, "isEmulator: ${isEmulator()}")
    }

    private val startDemoAdsActivityResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            result.data
            result.resultCode
        }

    override fun initData() {
//        val intent = Intent(this, DemoAdsActivity::class.java)
//        startDemoAdsActivityResult.launch(intent)
    }

    override fun initListener() {
        binding.btnOpenFragment.setOnClickListener {
            supportFragmentManager.beginTransaction().replace(R.id.frameContainer, HomeFragment())
                .commit()
//            randomAndSortList()
        }
        binding.btnRemoveFragment.setOnClickListener {
            val oldFragment: Fragment? =
                supportFragmentManager.findFragmentById(R.id.frameContainer)
            oldFragment?.let {
                removeFragment(it)
            }
        }

        binding.btnDemoViewPager.clickSafety {
            openActivity(DemoViewPagerActivity::class.java)
        }
        binding.btnDemoLocation.clickSafety {
            openActivity(LocationActivity::class.java)
        }
        binding.btnDemoAds.clickSafety {
            openActivity(DemoAdsActivity::class.java)
        }
        binding.btnTestCoilLib.clickSafety {
            openActivity(CoilActivity::class.java)
        }
        binding.btnFlexBoxManager.clickSafety {
            openActivity(DemoFlexBoxLayoutActivity::class.java)
        }
        binding.btnPickImage.clickSafety {
            openActivity(PickImageActivity::class.java)
        }

        binding.btnCameraTakePicture.clickSafety {
            openActivity(TakePictureActivity::class.java)
        }

        binding.btnNetWorkDemo.clickSafety {
            openActivity(NetWorkDemoActivity::class.java)
        }

        binding.btnLunarCalendar.clickSafety {
            openActivity(LunarCalendarActivity::class.java)
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

    private val coroutineException = CoroutineExceptionHandler { _, throwable ->
        binding.tvResult.text = throwable.message
    }

    @SuppressLint("SetTextI18n")
    private fun mapList(size: Int) {
        val list = mutableListOf<Int>()
        var timeStart: Long
        var timeEnd: Long
        binding.tvTitle.text = "Init and sort list random with size: ${size.toStringFormat()}"
        binding.tvResult.clear()
        lifecycleScope.launch(coroutineException) {

            timeStart = System.currentTimeMillis()
            showLoading()
            runOnDispatcherIO {
                repeat(size) {
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