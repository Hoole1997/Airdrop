package com.web3.airdrop.ui.home

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.blankj.utilcode.util.ActivityUtils
import com.chad.library.adapter4.BaseQuickAdapter
import com.chad.library.adapter4.viewholder.DataBindingHolder
import com.web3.airdrop.R
import com.web3.airdrop.base.BaseFragment
import com.web3.airdrop.bean.Web3Project
import com.web3.airdrop.databinding.FragmentHomeBinding
import com.web3.airdrop.databinding.ItemHomeProjectBinding
import com.web3.airdrop.project.layeredge.ActivityLayerEdge

class HomeFragment : BaseFragment<FragmentHomeBinding, HomeViewModel>() {

    override fun initBinding(savedInstanceState: Bundle?): FragmentHomeBinding {
        return FragmentHomeBinding.inflate(layoutInflater)
    }

    override fun initViewModel(): HomeViewModel {
        return ViewModelProvider(this)[HomeViewModel::class.java]
    }

    override fun initView(activity: FragmentActivity) {
        initToolbar()

        val dividerItemDecoration = DividerItemDecoration(activity, LinearLayoutManager.VERTICAL)
        binding.rvProject.addItemDecoration(dividerItemDecoration)
        binding.rvProject.adapter = object : BaseQuickAdapter<Web3Project, DataBindingHolder<ItemHomeProjectBinding>>(projectData()) {
                override fun onBindViewHolder(
                    holder: DataBindingHolder<ItemHomeProjectBinding>,
                    position: Int,
                    item: Web3Project?
                ) {
                    item?.let {
                        holder.binding.ivIcon.setImageResource(item.icon)
                        holder.binding.tvName.text = item.name
                    }
                }

                override fun onCreateViewHolder(
                    context: Context,
                    parent: ViewGroup,
                    viewType: Int
                ): DataBindingHolder<ItemHomeProjectBinding> {
                    return DataBindingHolder(layoutInflater.inflate(R.layout.item_home_project,parent,false))
                }

            }.apply {
                setOnItemClickListener { _, _, position ->
                    val data = getItem(position)
                    data?.let {
                        when(it.name) {
                            "LayerEdge" -> {
                                ActivityUtils.startActivity(ActivityLayerEdge::class.java)
                            }
                            else -> {

                            }
                        }
                    }
                }
            }
    }

    private fun projectData() : List<Web3Project>{
        return arrayListOf<Web3Project>().apply {
            add(Web3Project(0,"LayerEdge", R.mipmap.icon_layeredge,"https://x.com/layeredge","https://dashboard.layeredge.io/"))
        }
    }

    private fun initToolbar() {
        binding.toolbar.title = "项目"
    }

}