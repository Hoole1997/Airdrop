package com.web3.airdrop.project.takersowing

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewModelScope
import androidx.work.Data
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.blankj.utilcode.util.GsonUtils
import com.web3.airdrop.base.IPanelTaskModule
import com.web3.airdrop.data.TaskConfig
import com.web3.airdrop.project.TakerProtocol.TakerModel
import com.web3.airdrop.project.coresky.CoreSkyModel
import com.web3.airdrop.project.log.LogData
import com.web3.airdrop.project.takersowing.data.TakerSowingUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class FragmentTakerSowingPanelTask(activity: FragmentActivity, model: TakerSowingModel?) : IPanelTaskModule<TakerSowingModel, TakerSowingUser>(activity,model) {

    override fun initTask(): List<PanelTask> {
        return mutableListOf<PanelTask>().apply {
            add(PanelTask("每日签到","🔥0撸！！3小时签到一次 +100积分", supportCombinationMode = true, support = true))
            add(PanelTask("BTC Basics Q&A","答题 一次性任务 +200积分 1NFT", supportCombinationMode = true, support = true))
            add(PanelTask("Explore and Understand Taker Protocol","手动授权推特 一次性任务，+100积分", supportCombinationMode = false, support = false))
        }
    }

    override suspend fun taskClick(panelTask: List<PanelTask>) {
        model?.startTask(panelTask)
    }

    override fun initTaskTimingWorker(
        enable: Boolean,
        config: TaskConfig
    ) {
        val projectInfo = model?.taskInfo?.value ?: return
        if (enable) {
            //开启定时任务
            val workRequest = PeriodicWorkRequest.Builder(
                TakerSowingTimingWorker::class.java,
                config.timing, TimeUnit.HOURS
            ).setInputData(Data.Builder().putInt("PROJECT_ID",projectInfo.projectId).build())
                .build()

            WorkManager.getInstance(activity).enqueueUniquePeriodicWork(
                projectInfo.projectId.toString(), // 唯一标签，避免重复调度
                androidx.work.ExistingPeriodicWorkPolicy.KEEP, // 保持已有任务
                workRequest
            )
            model.viewModelScope.launch(Dispatchers.IO) {
                model.sendLog(LogData(projectInfo.projectId, LogData.Level.NORMAL,"","开启定时任务 \n ${GsonUtils.toJson(config)}"))
            }
        } else {
            //关闭定时任务
            WorkManager.getInstance(activity).cancelUniqueWork(projectInfo.projectId.toString())
            model.viewModelScope.launch(Dispatchers.IO) {
                model.sendLog(LogData(projectInfo.projectId, LogData.Level.NORMAL,"","关闭定时任务"))
            }
        }
    }

}