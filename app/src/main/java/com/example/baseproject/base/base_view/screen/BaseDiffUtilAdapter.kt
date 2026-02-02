package com.example.baseproject.base.base_view.screen

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.example.baseproject.base.utils.extension.tryCatch

abstract class BaseDiffUtilAdapter<T, VB : ViewBinding>(
    diffCallback: DiffUtil.ItemCallback<T>
) : ListAdapter<T, BaseViewHolder<VB>>(diffCallback) {

    private var binding: VB? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<VB> {
        binding = inflateBinding(LayoutInflater.from(parent.context), parent, viewType)
        return BaseViewHolder(requireNotNull(binding)).apply {
            bindViewClick(this, viewType)
        }
    }

    private var setOnClickItem: ((item: T?, position: Int) -> Unit)? = null
    private var setOnLongClickItem: ((item: T?, position: Int) -> Unit)? = null

    fun setOnClickItem(listener: ((item: T?, position: Int) -> Unit)? = null) {
        setOnClickItem = listener
    }

    fun setOnLongClickItem(listener: ((item: T?, position: Int) -> Unit)? = null) {
        setOnLongClickItem = listener
    }

    open fun bindViewClick(viewHolder: BaseViewHolder<VB>, viewType: Int) {
        viewHolder.itemView.setOnClickListener {
            val position = viewHolder.bindingAdapterPosition
            if (position == RecyclerView.NO_POSITION) {
                return@setOnClickListener
            }
            setOnClickItem?.invoke(getItem(position), position)
        }

        viewHolder.itemView.setOnLongClickListener {
            val position = viewHolder.bindingAdapterPosition
            if (position == RecyclerView.NO_POSITION) {
                return@setOnLongClickListener false
            }
            setOnLongClickItem?.invoke(getItem(position), position)
            false
        }
    }

    override fun onBindViewHolder(holder: BaseViewHolder<VB>, position: Int) {
        bindData(holder.binding, getItem(position), position)
    }

    override fun onBindViewHolder(
        holder: BaseViewHolder<VB>,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position)
            return
        }
        bindData(holder.binding, getItem(position), position, payloads)
    }

    protected abstract fun inflateBinding(
        inflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ): VB

    protected abstract fun bindData(binding: VB, item: T, position: Int)

    protected open fun bindData(binding: VB, item: T, position: Int, payloads: MutableList<Any>) {
        bindData(binding, item, position)
    }

    /**
     * Add more data at the end of list (for load more)
     * @param data: collection to add
     */
    open fun addDataList(data: Collection<T>) {
        val newList = currentList.toMutableList().apply {
            addAll(data)
        }
        submitList(newList)
    }

    /**
     * Add single item at specific position
     * @param item: item to add
     * @param index: position to insert
     */
    open fun addItem(item: T, index: Int) {
        tryCatch(tryBlock = {
            val newList = currentList.toMutableList().apply {
                add(index, item)
            }
            submitList(newList)
        })
    }

    /**
     * Remove item at specific position
     * @param index: position to remove
     */
    open fun removeItem(index: Int) {
        tryCatch(tryBlock = {
            if (index in currentList.indices) {
                val newList = currentList.toMutableList().apply {
                    removeAt(index)
                }
                submitList(newList)
            }
        })
    }

    /**
     * Update item at specific position
     * @param item: new item
     * @param index: position to update
     */
    open fun updateItem(item: T, index: Int) {
        tryCatch(tryBlock = {
            if (index in currentList.indices) {
                val newList = currentList.toMutableList().apply {
                    set(index, item)
                }
                submitList(newList)
            }
        })
    }

    /**
     * Get item at position (override to make it public)
     */
    public override fun getItem(position: Int): T {
        return super.getItem(position)
    }
}