package com.web3.airdrop.base

import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import com.chad.library.adapter4.BaseDifferAdapter
import com.chad.library.adapter4.viewholder.DataBindingHolder

abstract class IProjectAccountModule<USER: BaseUser,DB: ViewDataBinding> {

    private var adapter: BaseDifferAdapter<USER,DataBindingHolder<DB>>? = null
    var onClickListener: ((data: USER) -> Unit)? = null
    abstract fun initItemBinding(): DB
    abstract fun initRecyclerLayoutManager(): LayoutManager
    abstract fun initDiffCallback(): DiffUtil.ItemCallback<USER>
    fun initAdapter(adapter: BaseDifferAdapter<USER,DataBindingHolder<DB>>?) {
        this.adapter = adapter
    }

    open fun initItemView(data: USER?,position:Int,itemDb: DB) {
        if (data == null) return
        itemDb.root.setOnClickListener {
            onClickListener?.invoke(data)
        }
    }

    fun onItemClickListener(onClickListener: (data: USER) -> Unit) {
        this.onClickListener = onClickListener
    }

    fun refreshData(data: List<USER>?) {
        adapter?.submitList(data)
    }
}