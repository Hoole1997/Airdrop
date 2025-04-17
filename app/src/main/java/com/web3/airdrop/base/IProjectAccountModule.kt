package com.web3.airdrop.base

import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import com.chad.library.adapter4.BaseDifferAdapter
import com.chad.library.adapter4.viewholder.DataBindingHolder

abstract class IProjectAccountModule<T: Any,DB: ViewDataBinding> {

    private var adapter: BaseDifferAdapter<T,DataBindingHolder<DB>>? = null
    private var onClickListener: ((data: T) -> Unit)? = null
    abstract fun initItemBinding(): DB
    abstract fun initRecyclerLayoutManager(): LayoutManager
    abstract fun initDiffCallback(): DiffUtil.ItemCallback<T>
    fun initAdapter(adapter: BaseDifferAdapter<T,DataBindingHolder<DB>>?) {
        this.adapter = adapter
    }

    open fun initItemView(data: T?,position:Int,itemDb: DB) {
        if (data == null) return
        itemDb.root.setOnClickListener {
            onClickListener?.invoke(data)
        }
    }

    fun onItemClickListener(onClickListener: (data: T) -> Unit) {
        this.onClickListener = onClickListener
    }

    fun refreshData(data: List<T>?) {
        adapter?.submitList(data)
    }
}