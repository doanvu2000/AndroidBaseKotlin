package com.example.baseproject.base.base_view.screen

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.IntRange
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.example.baseproject.base.utils.extension.clickAnimation

/**
 * Base Adapter cho RecyclerView với ViewBinding
 * Cung cấp các utility methods cơ bản cho việc quản lý data và click events
 *
 * @param T kiểu dữ liệu của item trong list
 * @param VB ViewBinding class cho item layout
 * @param dataList danh sách dữ liệu ban đầu (optional)
 */
abstract class BaseAdapterRecyclerView<T, VB : ViewBinding>(
    dataList: MutableList<T>? = null
) : RecyclerView.Adapter<BaseViewHolder<VB>>() {

    // ViewBinding instance for creating ViewHolder
    private var binding: VB? = null

    // Click listeners for item interactions
    private var onClickItem: ((item: T?, position: Int) -> Unit)? = null
    private var onLongClickItem: ((item: T?, position: Int) -> Boolean)? = null

    // Animation control flags
    private var enableClickAnimation = false
    private var enableLongClickAnimation = false

    /**
     * Danh sách dữ liệu của adapter
     * Internal setter để đảm bảo data integrity
     */
    var dataList: MutableList<T> = dataList ?: arrayListOf()
        internal set

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<VB> {
        binding = inflateBinding(LayoutInflater.from(parent.context), parent, viewType)
        return BaseViewHolder(requireNotNull(binding)).apply {
            bindViewClick(this, viewType)
        }
    }

    override fun onBindViewHolder(holder: BaseViewHolder<VB>, position: Int) {
        bindData(holder.binding, dataList[position], position)
    }

    override fun onBindViewHolder(
        holder: BaseViewHolder<VB>, position: Int, payloads: MutableList<Any>
    ) {
        if (payloads.isEmpty()) {
            // No payloads, use normal binding
            onBindViewHolder(holder, position)
        } else {
            // Has payloads, still bind data (can be customized in subclasses)
            bindData(holder.binding, dataList[position], position)
        }
    }

    override fun getItemCount(): Int = dataList.size

    //region Abstract Methods - Must be implemented by subclasses

    /**
     * Inflate ViewBinding cho item layout
     * @param inflater LayoutInflater instance
     * @param parent parent ViewGroup
     * @return ViewBinding instance cho item
     */
    protected abstract fun inflateBinding(
        inflater: LayoutInflater, parent: ViewGroup, viewType: Int
    ): VB

    /**
     * Bind data vào ViewBinding
     * @param binding ViewBinding instance
     * @param item data item tại position
     * @param position vị trí của item trong list
     */
    protected abstract fun bindData(binding: VB, item: T, position: Int)

    //endregion

    //region Click Management

    /**
     * Set listener cho item click events
     * @param enableAnimation có enable click animation hay không
     * @param listener callback function nhận item và position khi click
     */
    fun setOnClickItem(
        enableAnimation: Boolean = false, listener: ((item: T?, position: Int) -> Unit)? = null
    ) {
        this.enableClickAnimation = enableAnimation
        this.onClickItem = listener
    }

    /**
     * Set listener cho item long click events
     * @param enableAnimation có enable long click animation hay không
     * @param listener callback function nhận item và position khi long click, return true nếu consumed
     */
    fun setOnLongClickItem(
        enableAnimation: Boolean = false, listener: ((item: T?, position: Int) -> Boolean)? = null
    ) {
        this.enableLongClickAnimation = enableAnimation
        this.onLongClickItem = listener
    }

    /**
     * Bind click listeners cho ViewHolder
     * Override method này để customize click behavior
     * @param viewHolder ViewHolder instance
     * @param viewType view type của item
     */
    open fun bindViewClick(viewHolder: BaseViewHolder<VB>, viewType: Int) {
        // Setup normal click listener
        viewHolder.itemView.setOnClickListener {
            val position = viewHolder.bindingAdapterPosition
            if (position != RecyclerView.NO_POSITION) {
                // Apply animation if enabled
                if (enableClickAnimation) {
                    it.clickAnimation()
                }
                onClickItem?.invoke(dataList.getOrNull(position), position)
            }
        }

        // Setup long click listener
        viewHolder.itemView.setOnLongClickListener {
            val position = viewHolder.bindingAdapterPosition
            if (position != RecyclerView.NO_POSITION) {
                // Apply animation if enabled
                if (enableLongClickAnimation) {
                    it.clickAnimation()
                }
                onLongClickItem?.invoke(dataList.getOrNull(position), position) ?: false
            } else {
                false
            }
        }
    }

    //endregion

    //region Data Management Methods

    /**
     * Update data tại vị trí cụ thể
     * @param index vị trí cần update (>= 0)
     * @param data data mới
     */
    open fun setData(@IntRange(from = 0) index: Int, data: T) {
        if (index < dataList.size) {
            dataList[index] = data
            notifyItemChanged(index)
        }
    }

    /**
     * Xóa item tại vị trí cụ thể
     * @param position vị trí cần xóa (>= 0)
     */
    open fun removeAt(@IntRange(from = 0) position: Int) {
        if (position < dataList.size) {
            dataList.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    /**
     * Xóa item theo data object
     * @param data data object cần xóa
     */
    open fun remove(data: T) {
        val index = dataList.indexOf(data)
        if (index != -1) {
            removeAt(index)
        }
    }

    /**
     * Clear tất cả data trong adapter
     */
    @SuppressLint("NotifyDataSetChanged")
    open fun clearData() {
        dataList.clear()
        notifyDataSetChanged()
    }

    /**
     * Set danh sách data mới (replace toàn bộ)
     * @param data collection data mới
     */
    @SuppressLint("NotifyDataSetChanged")
    open fun setDataList(data: Collection<T>) {
        dataList.clear()
        dataList.addAll(data)
        notifyDataSetChanged()
    }

    /**
     * Thêm data vào cuối danh sách
     * Thường dùng cho load more functionality
     * @param data collection data cần thêm
     */
    open fun addDataList(data: Collection<T>) {
        val start = dataList.size
        dataList.addAll(data)
        notifyItemRangeInserted(start, data.size)
    }

    //endregion
}