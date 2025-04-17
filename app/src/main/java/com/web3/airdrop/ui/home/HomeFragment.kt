package com.web3.airdrop.ui.home

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.chad.library.adapter4.BaseQuickAdapter
import com.chad.library.adapter4.viewholder.DataBindingHolder
import com.web3.airdrop.R
import com.web3.airdrop.base.BaseFragment
import com.web3.airdrop.data.ProjectConfig
import com.web3.airdrop.databinding.FragmentHomeBinding
import com.web3.airdrop.databinding.ItemHomeProjectBinding
import com.web3.airdrop.project.ActivityProject

class HomeFragment : BaseFragment<FragmentHomeBinding, HomeViewModel>() {

    override fun initBinding(savedInstanceState: Bundle?): FragmentHomeBinding {
        return FragmentHomeBinding.inflate(layoutInflater)
    }

//    override fun initViewModel(): HomeViewModel {
//        return ViewModelProvider(this)[HomeViewModel::class.java]
//    }

    override fun initView(activity: FragmentActivity) {
        initToolbar()

        val dividerItemDecoration = DividerItemDecoration(activity, LinearLayoutManager.VERTICAL)
        binding.rvProject.addItemDecoration(dividerItemDecoration)
        binding.rvProject.adapter = object : BaseQuickAdapter<ProjectConfig.ProjectInfo, DataBindingHolder<ItemHomeProjectBinding>>(
            ProjectConfig.projectData()) {
                override fun onBindViewHolder(
                    holder: DataBindingHolder<ItemHomeProjectBinding>,
                    position: Int,
                    item: ProjectConfig.ProjectInfo?
                ) {
                    item?.let {
                        holder.binding.ivIcon.setImageResource(it.icon)
                        holder.binding.tvDescribe.text = it.describe
                        holder.binding.tvStar.text = it.star.toString()
                        holder.binding.tvTwitter.text = it.twitterUrl
                        holder.binding.tvWebsite.text = it.website
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
                    startActivity(Intent().apply {
                        setClass(activity, ActivityProject::class.java)
                        putExtra("info",data)
                    })
//                    data?.let {
//                        when(it.name) {
//                            "LayerEdge" -> {
//                                ActivityUtils.startActivity(ActivityLayerEdge::class.java)
//                            }
//                            "SomNia" -> {
//                                ActivityUtils.startActivity(SomniaActivity::class.java)
//                            }
//                            "CoreSky" -> {
//                                ActivityUtils.startActivity(ActivityCoreSky::class.java)
//                            }
//                            else -> {
//
//                            }
//                        }
//                    }
                }
            }
    }

    private fun initToolbar() {
        binding.toolbar.title = "项目"
    }

}