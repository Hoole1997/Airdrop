package com.web3.airdrop.base

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.blankj.utilcode.util.ActivityUtils
import com.chad.library.adapter4.BaseQuickAdapter
import com.chad.library.adapter4.viewholder.DataBindingHolder
import com.web3.airdrop.R
import com.web3.airdrop.databinding.FragmentBasePanelAccountBinding
import com.web3.airdrop.databinding.ItemTaskPanelAccountinfoBinding
 import kotlin.jvm.java

class IPanelAccountInfoModule<VM: BaseModel<USER>, USER: BaseUser>(val activity: FragmentActivity, val model: VM?) : IPanelModule{


    private var fragment: PanelAccountFragment<VM,USER>? = null

    init {
        initAccountInfoView()
    }

    private fun initAccountInfoView() {
        fragment = PanelAccountFragment(model)
        fragment?.let {
            model?.panelAccountInfo?.observe(it) { data ->
                it.refreshData(data)
            }
        }
    }

    override fun initFragment(): Fragment? {
        return fragment
    }

    override fun initTabName(): String {
        return "账号详情"
    }

    class PanelAccountFragment<VM: BaseModel<USER>, USER: BaseUser>(val viewModel: VM?) : BaseFragment<FragmentBasePanelAccountBinding, VM>() {

        private var adapter: BaseQuickAdapter<Pair<String, String>,DataBindingHolder<ItemTaskPanelAccountinfoBinding>>? = null

        override fun initBinding(savedInstanceState: Bundle?): FragmentBasePanelAccountBinding {
            return FragmentBasePanelAccountBinding.inflate(activity?.layoutInflater ?: ActivityUtils.getTopActivity().layoutInflater)
        }

        override suspend fun  initViewModel(): VM? {
            return viewModel
        }

        override fun initView(activity: FragmentActivity) {
            adapter = object : BaseQuickAdapter<Pair<String, String>,DataBindingHolder<ItemTaskPanelAccountinfoBinding>>() {
                override fun onBindViewHolder(
                    holder: DataBindingHolder<ItemTaskPanelAccountinfoBinding>,
                    position: Int,
                    item: Pair<String, String>?
                ) {
                    item?.let {
                        holder.binding.tvName.text = it.first
                        holder.binding.tvValue.text = it.second
                    }
                }

                override fun onCreateViewHolder(
                    context: Context,
                    parent: ViewGroup,
                    viewType: Int
                ): DataBindingHolder<ItemTaskPanelAccountinfoBinding> {
                    return DataBindingHolder(ItemTaskPanelAccountinfoBinding.bind(LayoutInflater.from(recyclerView.context).inflate(R.layout.item_task_panel_accountinfo,recyclerView,false)))
                }

            }
            binding.refreshLayout.setOnRefreshListener {
                binding.refreshLayout.finishRefresh()
                viewModel?.panelCurrentAccountInfo?.value?.let {
                    viewModel?.refreshPanelAccountInfo(it,true)
                }
            }
            binding.rvAccountInfo.layoutManager = LinearLayoutManager(activity).apply {
                orientation = LinearLayoutManager.VERTICAL
            }
            binding.rvAccountInfo.adapter = adapter

            viewModel?.panelAccountInfo?.observe(this) {
                adapter?.submitList(it)
            }
        }

        fun refreshData(data:List<Pair<String, String>>?) {
            adapter?.submitList(data)
        }

    }

}