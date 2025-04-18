package com.web3.airdrop.base

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.blankj.utilcode.util.LogUtils
import com.chad.library.adapter4.BaseDifferAdapter
import com.chad.library.adapter4.viewholder.DataBindingHolder
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.tabs.TabLayoutMediator
import com.web3.airdrop.R
import com.web3.airdrop.data.ProjectConfig.ProjectInfo
import com.web3.airdrop.databinding.FragmentBaseProjectBinding
import kotlinx.coroutines.suspendCancellableCoroutine

abstract class BaseProjectFragment<VM : BaseModel> : BaseFragment<FragmentBaseProjectBinding,VM>(){

    lateinit var bottomSheetBehavior: BottomSheetBehavior<CardView>

    override fun initBinding(savedInstanceState: Bundle?): FragmentBaseProjectBinding {
        return FragmentBaseProjectBinding.inflate(layoutInflater)
    }

    override suspend fun initViewModel(): VM? {
        if (BaseService.modelMap.get(initProjectInfo().projectId) == null) {
            startTaskService()
            return suspendCancellableCoroutine { continuation ->
                // 注册广播接收器
                val createModelAction = "createModel_"+initProjectInfo().projectId
                val filter = IntentFilter(createModelAction)
                val receiver = object : BroadcastReceiver() {
                    override fun onReceive(
                        context: Context?,
                        intent: Intent?
                    ) {
                        if (intent?.action == createModelAction) {
                            continuation.resume(BaseService.modelMap.get(initProjectInfo().projectId) as VM?,{

                            })
                        }
                    }
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    activity?.registerReceiver(receiver, filter,Context.RECEIVER_EXPORTED)
                } else {
                    activity?.registerReceiver(receiver, filter)
                }

                // 取消时注销接收器
                continuation.invokeOnCancellation {
                    activity?.unregisterReceiver(receiver)
                }
            }
        } else {
            return BaseService.modelMap.get(initProjectInfo().projectId) as VM?
        }
    }

    override fun initView(activity: FragmentActivity) {
        bottomSheetBehavior = BottomSheetBehavior.from(binding.bottomCard)
        initProjectInfo().let {
            binding.ivIcon.setImageResource(it.icon)
            binding.tvDescribe.text = it.describe
            binding.tvStar.text = it.star.toString()
            binding.tvTwitter.text = it.twitterUrl
            binding.tvWebsite.text = it.website
            binding.toolBar.title = it.name
            binding.toolBar.setNavigationOnClickListener {
                activity.finish()
            }
        }
        binding.toolBar.inflateMenu(R.menu.menu_project)
        binding.toolBar.setOnMenuItemClickListener {
            if (it.itemId == R.id.menu_close) {
                stopTaskService()
                activity.finish()
            }
            true
        }
    }

    abstract fun initProjectInfo(): ProjectInfo

    abstract fun startTaskService()

    abstract fun stopTaskService()

    fun <INFo: Any,DB: ViewDataBinding> loadItemAccountModule(projectAccountModule : IProjectAccountModule<INFo,DB>) {
        val adapter = object : BaseDifferAdapter<INFo, DataBindingHolder<DB>>(projectAccountModule.initDiffCallback()){
            override fun onBindViewHolder(
                holder: DataBindingHolder<DB>,
                position: Int,
                item: INFo?
            ) {
                projectAccountModule.initItemView(item,position,holder.binding)
            }

            override fun onBindViewHolder(
                holder: DataBindingHolder<DB>,
                position: Int,
                item: INFo?,
                payloads: List<Any>
            ) {
                super.onBindViewHolder(holder, position, item, payloads)
            }

            override fun onCreateViewHolder(
                context: Context,
                parent: ViewGroup,
                viewType: Int
            ): DataBindingHolder<DB> {
                return DataBindingHolder(projectAccountModule.initItemBinding())
            }
        }
        projectAccountModule.initAdapter(adapter)
        projectAccountModule.onItemClickListener {
            if (bottomSheetBehavior.state != BottomSheetBehavior.STATE_EXPANDED) {
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
            }
            model?.refreshPanelAccountInfo(it,false)
        }
        binding.rvAccount.layoutManager = projectAccountModule.initRecyclerLayoutManager()
        binding.rvAccount.adapter = adapter
    }

    fun loadTaskPanelModule(
        accountInfoModule: IPanelAccountInfoModule<VM>?,
        taskModule: IPanelTaskModule<VM>?
    ) {
        val tabTitleList = mutableListOf<String>()
        val fragmentList = mutableListOf<Fragment>()
        accountInfoModule?.apply {
            tabTitleList.add(initTabName())
        }?.initFragment()?.let {
            fragmentList.add(it)
        }
        taskModule?.apply {
            tabTitleList.add(initTabName())
        }?.initFragment()?.let {
            fragmentList.add(it)
        }
        activity?.let {
            IPanelLogModule<VM>(it,model).let {
                tabTitleList.add(it.initTabName())
                it.initFragment()?.let {
                    fragmentList.add(it)
                }
            }
        }


        binding.panelContent.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int {
                return fragmentList.size
            }

            override fun createFragment(position: Int): Fragment {
                return fragmentList[position]
            }
        }
        taskModule?.initTask()
        TabLayoutMediator(binding.panelTab, binding.panelContent) { tab, position ->
            tab.text = tabTitleList[position]
        }.attach()
    }

}