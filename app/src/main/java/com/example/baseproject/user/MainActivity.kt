package com.example.baseproject.user

import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import com.example.baseproject.R
import com.example.baseproject.base.base_view.BaseActivity
import com.example.baseproject.base.base_view.HomeFragment
import com.example.baseproject.base.utils.Constant
import com.example.baseproject.databinding.ActivityMainBinding


class MainActivity : BaseActivity<ActivityMainBinding>() {
    companion object {
        const val TAG = Constant.TAG
    }

    override fun initView() {

    }

    override fun initData() {
    }

    override fun initListener() {
        binding.btnOpenFragment.setOnClickListener {
            supportFragmentManager.beginTransaction().replace(R.id.frameContainer, HomeFragment())
                .commit()
        }
        binding.btnRemoveFragment.setOnClickListener {
            val oldFragment: Fragment? = supportFragmentManager.findFragmentById(R.id.frameContainer)
            if (oldFragment != null) {
                supportFragmentManager.beginTransaction()
                    .remove(oldFragment).commit()
            }
        }
    }

    var rs = 0
    override fun inflateViewBinding(inflater: LayoutInflater): ActivityMainBinding {
        return ActivityMainBinding.inflate(inflater)
    }
}