package com.web3.airdrop.base

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.work.Data
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.GsonUtils
import com.blankj.utilcode.util.SPUtils
import com.blankj.utilcode.util.ToastUtils
import com.chad.library.adapter4.BaseQuickAdapter
import com.chad.library.adapter4.viewholder.DataBindingHolder
import com.web3.airdrop.R
import com.web3.airdrop.data.TaskConfig
import com.web3.airdrop.databinding.FragmentBasePanelTaskBinding
import com.web3.airdrop.databinding.ItemTaskPanelTaskBinding
import com.web3.airdrop.project.log.LogData
import com.web3.airdrop.project.takersowing.TakerSowingTimingWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

abstract class IPanelTaskModule<VM: BaseModel<USER>, USER: BaseUser>(val activity: FragmentActivity, val model: VM?) : IPanelModule{

    override fun initFragment(): Fragment? {
        return PanelTaskFragment(initTask(),model).apply {
            addTaskStartListener { startState,taskList ->
                model?.viewModelScope?.launch(Dispatchers.IO) {
                    if (startState) {
                        taskClick(taskList)
                    }
                }
            }
            addModeListener(
                globalModeListener = {
                    this@IPanelTaskModule.model?.globalMode?.setValue(it)
                },
                onRandomModeListener = {
//                    this@IPanelTaskModule.model?.randomMode?.value = it
                },
                onCombinationModeListener = {
//                    this@IPanelTaskModule.model?.combinationMode?.value = it
                },
                onTimingEnableListener = { enable,config ->
                    initTaskTimingWorker(enable,config)
                }
            )
        }
    }

    override fun initTabName(): String {
        return "任务"
    }

    abstract fun initTask(): List<PanelTask>

    abstract suspend fun taskClick(panelTask: List<PanelTask>)

    abstract fun initTaskTimingWorker(enable: Boolean,config: TaskConfig)

    data class PanelTask(
        val taskName: String,
        val taskDescr: String,
        val supportCombinationMode : Boolean,
        val support: Boolean,
        var check: Boolean = false
    )

    class PanelTaskFragment<VM: BaseModel<USER>, USER: BaseUser>(val taskPanelList: List<PanelTask>,val viewModel: VM?) : BaseFragment<FragmentBasePanelTaskBinding, VM>() {

        private var adapter: BaseQuickAdapter<PanelTask,DataBindingHolder<ItemTaskPanelTaskBinding>>? = null
        private var onTaskStartListener: ((Boolean,task: List<PanelTask>) -> Unit)? = null
        private var onGlobalModeListener: ((global: Boolean) -> Unit)? = null
        private var onRandomModeListener: ((random: Boolean) -> Unit)? = null
        private var onCombinationModeListener: ((combination: Boolean) -> Unit)? = null
        private var onTimingListener: ((timingEnable: Boolean,taskConfig: TaskConfig) -> Unit)? = null

        //组合模式，勾选任务
        var combinationMode = true

        override fun initBinding(savedInstanceState: Bundle?): FragmentBasePanelTaskBinding {
            return FragmentBasePanelTaskBinding.inflate(activity?.layoutInflater ?: ActivityUtils.getTopActivity().layoutInflater)
        }

        override suspend fun initViewModel(): VM? {
            return viewModel
        }

        override fun initView(activity: FragmentActivity) {
            setAdapter()
            binding.btnStart.setOnClickListener {
                val startState = model?.taskStart?.value ?:false
                model?.taskStart?.setValue(!startState)
                if (!startState) {
                    //开启
                    adapter?.items?.filter {
                        it.check
                    }?.let {
                        onTaskStartListener?.invoke(true,it)
                    }
                } else {
                    //关闭

                }
            }
            model?.taskInfo?.observe(this) {
                setTaskConfig()
            }
            model?.taskStart?.observe(this) {
                binding.btnStart.text = "${if (it) "STOP" else "START"}"
            }
            binding.switchAll.setOnCheckedChangeListener { _,check ->
                onGlobalModeListener?.invoke(check)
                setTaskConfig()
            }
            binding.switchTiming.setOnCheckedChangeListener { _,check ->
                if (binding.etTaskTiming.text.isNullOrBlank()|| binding.etTaskTiming.text.toString().toLong() <= 0L) {
                    ToastUtils.showShort("时间不能小于0")
                    binding.switchTiming.isChecked = false
                    return@setOnCheckedChangeListener
                }
                setTaskConfig()?.let {
                    onTimingListener?.invoke(check,it)
                }
            }
            binding.etTaskTiming.doAfterTextChanged {
                if (it.isNullOrBlank() || it.toString().toLong() <= 0L) {
                    ToastUtils.showShort("时间不能小于0")
                    return@doAfterTextChanged
                }
                setTaskConfig()
            }
        }

        private fun setTaskConfig(): TaskConfig?{
            val projectInfo = model?.taskInfo?.value ?: return null
            val config = TaskConfig(
                globalMode = binding.switchAll.isChecked,
                timingMode = binding.switchTiming.isChecked,
                binding.etTaskTiming.text.toString().toLong(),
                adapter?.items
            )
            SPUtils.getInstance(projectInfo.projectId.toString()).put("TaskConfig",GsonUtils.toJson(config))
            lifecycleScope.launch(Dispatchers.IO) {
                model?.sendLog(LogData(projectInfo.projectId, LogData.Level.NORMAL,"","保存定时参数 \n ${GsonUtils.toJson(config)}"))
            }
            return config
        }

        private fun setAdapter() {
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
                            onTaskStartListener?.invoke(true,arrayListOf(it))
                        }
                        holder.binding.cbCombination.setOnCheckedChangeListener { _,check ->
                            item.check = check
                            setTaskConfig()
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

            model?.taskInfo?.value?.let {
                val spConfig = SPUtils.getInstance(it.projectId.toString()).getString("TaskConfig","")
                if (spConfig.isNotBlank()) {
                    val config = GsonUtils.fromJson<TaskConfig>(spConfig, TaskConfig::class.java)
                    adapter?.submitList(config.taskList)
                    binding.switchAll.isChecked = config.globalMode
                    binding.switchTiming.isChecked = config.timingMode
                    binding.etTaskTiming.setText(config.timing.toString())
                    model?.globalMode?.setValue(config.globalMode)
                }
            }
        }

        fun addTaskStartListener(taskStartListener : (Boolean, List<PanelTask>) -> Unit) {
            onTaskStartListener = taskStartListener
        }

        fun addModeListener(globalModeListener : (Boolean) -> Unit,
                            onRandomModeListener : (Boolean) -> Unit,
                            onCombinationModeListener : (Boolean) -> Unit,
                            onTimingEnableListener : (Boolean, TaskConfig) -> Unit) {
            this.onGlobalModeListener = globalModeListener
            this.onRandomModeListener = onRandomModeListener
            this.onCombinationModeListener = onCombinationModeListener
            this.onTimingListener = onTimingEnableListener
        }
    }

}