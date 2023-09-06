package com.example.baseproject.base.base_view

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.example.baseproject.base.utils.extension.gone
import com.example.baseproject.base.utils.extension.setLinearLayoutManager
import com.example.baseproject.base.utils.extension.show
import com.example.baseproject.base.viewmodel.UserViewModel
import com.example.baseproject.databinding.FragmentHomeBinding
import com.example.baseproject.user.UserAdapter

class HomeFragment : BaseFragment<FragmentHomeBinding>() {
    private val viewModel by lazy {
        ViewModelProvider(this)[UserViewModel::class.java]
    }
    private val adapter by lazy {
        UserAdapter()
    }

    override fun initView() {
        viewModel.isLoading.observe(this) {
            if (it) showLoading() else hideLoading()
        }
        binding.rcvUser.setLinearLayoutManager(requireContext(), adapter)
    }

    override fun initData() {
        viewModel.getUsers(onSuccess = {
            adapter.setDataList(it)
        }, onError = {
            Log.d("ddd", "initData: $it")
            Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
        })

    }

    private fun showLoading() {
        binding.loading.show()
    }

    private fun hideLoading() {
        binding.loading.gone()
    }

    override fun initListener() {
        adapter.setOnClickItem { item, position ->
            Toast.makeText(requireContext(), "$item - $position", Toast.LENGTH_SHORT).show()
        }
    }

    override fun inflateLayout(inflater: LayoutInflater, container: ViewGroup?): FragmentHomeBinding {
        return FragmentHomeBinding.inflate(inflater, container, false)
    }

}