package com.example.baseproject.user

import android.view.LayoutInflater
import android.view.ViewGroup
import com.example.baseproject.base.base_view.screen.BaseAdapterRecyclerView
import com.example.baseproject.base.entity.User
import com.example.baseproject.databinding.LayoutItemUserBinding

class UserAdapter : BaseAdapterRecyclerView<User, LayoutItemUserBinding>() {
    override fun inflateBinding(
        inflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ): LayoutItemUserBinding {
        return LayoutItemUserBinding.inflate(inflater, parent, false)
    }

    override fun bindData(binding: LayoutItemUserBinding, item: User, position: Int) {
        binding.tvId.text = item.id
        binding.tvName.text = item.name
        binding.tvAge.text = "${item.age}"
    }
}