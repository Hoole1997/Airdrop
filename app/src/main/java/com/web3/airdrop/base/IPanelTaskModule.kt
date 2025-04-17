package com.web3.airdrop.base

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.blankj.utilcode.util.ActivityUtils
import com.chad.library.adapter4.BaseQuickAdapter
import com.chad.library.adapter4.viewholder.DataBindingHolder
import com.web3.airdrop.R
import com.web3.airdrop.databinding.FragmentBasePanelTaskBinding
import com.web3.airdrop.databinding.ItemTaskPanelTaskBinding

abstract class IPanelTaskModule<VM: BaseModel>(val activity: FragmentActivity,val model: VM?) : IPanelModule{


    override fun initFragment(): Fragment? {
        return PanelTaskFragment(initTask(),model).apply {
            addTaskClickListener {
                taskClick(it)
            }
            addModeListener(
                globalModeListener = {
                    this@IPanelTaskModule.model?.globalMode?.value = it
                },
                onRandomModeListener = {
                    this@IPanelTaskModule.model?.randomMode?.value = it
                },
                onCombinationModeListener = {
                    this@IPanelTaskModule.model?.combinationMode?.value = it
                }
            )
        }
    }

    override fun initTabName(): String {
        return "任务"
    }

    abstract fun initTask(): List<PanelTask>

    abstract fun taskClick(panelTask: List<PanelTask>)

    data class PanelTask(
        val taskName: String,
        val taskDescr: String,
        val supportCombinationMode : Boolean,
        val support: Boolean,
        var check: Boolean = false
    )

    class PanelTaskFragment<VM : BaseModel>(val taskPanelList: List<PanelTask>,val viewModel: VM?) : BaseFragment<FragmentBasePanelTaskBinding, VM>() {

        private var adapter: BaseQuickAdapter<PanelTask,DataBindingHolder<ItemTaskPanelTaskBinding>>? = null
        private var onTaskStartListener: ((task: List<PanelTask>) -> Unit)? = null
        private var onGlobalModeListener: ((global: Boolean) -> Unit)? = null
        private var onRandomModeListener: ((random: Boolean) -> Unit)? = null
        private var onCombinationModeListener: ((combination: Boolean) -> Unit)? = null

        //组合模式，勾选任务
        var combinationMode = false
        //随机模式，防女巫
        var randomMode = false

        override fun initBinding(savedInstanceState: Bundle?): FragmentBasePanelTaskBinding {
            return FragmentBasePanelTaskBinding.inflate(activity?.layoutInflater ?: ActivityUtils.getTopActivity().layoutInflater)
        }

        override suspend fun initViewModel(): VM? {
            return viewModel
        }

        override fun initView(activity: FragmentActivity) {
            binding.switchAll.setOnCheckedChangeListener { _,check ->
                onGlobalModeListener?.invoke(check)
            }
            binding.switchRandom.setOnCheckedChangeListener { _,check ->
                randomMode = check
                onRandomModeListener?.invoke(check)
            }
            binding.switchCombination.setOnCheckedChangeListener { _,check ->
                binding.btnStart.isVisible = check
                onCombinationModeListener?.invoke(check)
                this.combinationMode = check
                adapter?.notifyDataSetChanged()
            }
            binding.btnStart.setOnClickListener {
                var list = adapter?.items?.filter {
                    it.check
                }?.toList()
                if (randomMode) {
                    list = list?.shuffled()
                }
                list?.let {
                    onTaskStartListener?.invoke(it)
                }
            }
            adapter = object : BaseQuickAdapter<PanelTask,DataBindingHolder<ItemTaskPanelTaskBinding>>(taskPanelList) {

                override fun onBindViewHolder(
                    holder: DataBindingHolder<ItemTaskPanelTaskBinding>,
                    position: Int,
                    item: PanelTask?
                ) {
                    item?.let {
                        holder.binding.tvTaskName.text = it.taskName
                        holder.binding.tvTaskDesc.text = it.taskDescr
                        holder.binding.btnGo.isGone = combinationMode
                        holder.binding.cbCombination.isVisible = combinationMode
                        holder.binding.btnGo.isEnabled = it.support
                        holder.binding.cbCombination.isEnabled = it.supportCombinationMode
                        holder.binding.cbCombination.isChecked = it.check
                        holder.binding.btnGo.setOnClickListener { _ ->
                            onTaskStartListener?.invoke(arrayListOf(it))
                        }
                        holder.binding.cbCombination.setOnCheckedChangeListener { _,check ->
                            item.check = check
                        }
                    }
                }

                override fun onCreateViewHolder(
                    context: Context,
                    parent: ViewGroup,
                    viewType: Int
                ): DataBindingHolder<ItemTaskPanelTaskBinding> {
                    return DataBindingHolder(ItemTaskPanelTaskBinding.bind(LayoutInflater.from(recyclerView.context).inflate(R.layout.item_task_panel_task,recyclerView,false)))
                }

            }
            binding.rvAccountInfo.layoutManager = LinearLayoutManager(activity).apply {
                orientation = LinearLayoutManager.VERTICAL
            }
            binding.rvAccountInfo.adapter = adapter
        }

        fun addTaskClickListener(taskClickListener : (List<PanelTask>) -> Unit) {
            onTaskStartListener = taskClickListener
        }

        fun addModeListener(globalModeListener : (Boolean) -> Unit,onRandomModeListener : (Boolean) -> Unit,onCombinationModeListener : (Boolean) -> Unit) {
            this.onGlobalModeListener = globalModeListener
            this.onRandomModeListener = onRandomModeListener
            this.onCombinationModeListener = onCombinationModeListener
        }
    }

}