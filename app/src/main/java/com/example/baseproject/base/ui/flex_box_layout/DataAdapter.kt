package com.example.baseproject.base.ui.flex_box_layout

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import com.example.baseproject.R
import com.example.baseproject.base.base_view.screen.BaseAdapterRecyclerView
import com.example.baseproject.databinding.LayoutItemFlexBoxBinding

class DataAdapter : BaseAdapterRecyclerView<DataEntity, LayoutItemFlexBoxBinding>() {
    override fun inflateBinding(inflater: LayoutInflater, parent: ViewGroup): LayoutItemFlexBoxBinding {
        return LayoutItemFlexBoxBinding.inflate(inflater, parent, false)
    }

    override fun bindData(binding: LayoutItemFlexBoxBinding, item: DataEntity, position: Int) {
        val strokeColor = if (item.isSelected) {
            R.color.orange
        } else {
            R.color.black_30
        }
        binding.root.setStrokeColor(ColorStateList.valueOf(strokeColor))
        binding.tvNumber.text = item.number.toString()
        binding.tvContent.text = item.content
    }

    fun setSelected(position: Int) {
        if (position !in dataList.indices) {
            return
        }
        val currentSelected = dataList.indexOfFirst { it.isSelected }
        if (currentSelected == position) {
            return
        }
        dataList[position].isSelected = true
        notifyItemChanged(position)
        if (currentSelected != -1) {
            dataList[currentSelected].isSelected = false
            notifyItemChanged(currentSelected)
        }
    }
}