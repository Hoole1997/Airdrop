package com.web3.airdrop.project.coresky

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewModelScope
import androidx.work.Data
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.blankj.utilcode.util.GsonUtils
import com.web3.airdrop.base.IPanelTaskModule
import com.web3.airdrop.data.TaskConfig
import com.web3.airdrop.project.TakerProtocol.TakerTimingWorker
import com.web3.airdrop.project.coresky.data.CoreSkyUser
import com.web3.airdrop.project.log.LogData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class FragmentCoreSkyPanelTask(activity: FragmentActivity, model: CoreSkyModel?) : IPanelTaskModule<CoreSkyModel, CoreSkyUser>(activity,model) {

    override fun initTask(): List<PanelTask> {
        return mutableListOf<PanelTask>().apply {
            add(PanelTask("每日签到","🔥0撸！！24小时免费签到一次 20积分", supportCombinationMode = true, support = true))
            add(PanelTask("关注推特","推特需要授权，不支持自动化 50积分", supportCombinationMode = false, support = false))
            add(PanelTask("加入Discord","Discord需要授权，不支持自动化 50积分", supportCombinationMode = false, support = false))
            add(PanelTask("抽奖","需要少量gsa（忽略不计），抽奖获得积分 每日3次", supportCombinationMode = true, support = true))
            add(PanelTask("MEME投票","使用积分投票给MEME，影响空投权重(随机投5-20积分)", supportCombinationMode = true, support = true))
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
                CoreSkyTimingWorker::class.java,
                config.timing, TimeUnit.HOURS
            ).setInputData(Data.Builder().putInt("PROJECT_ID",projectInfo.projectId).build())
                .build()

            WorkManager.getInstance(activity).enqueueUniquePeriodicWork(
                projectInfo.projectId.toString(), // 唯一标签，避免重复调度
                androidx.work.ExistingPeriodicWorkPolicy.KEEP, // 替换已有任务
                workRequest
            )
            model?.viewModelScope?.launch(Dispatchers.IO) {
                model?.sendLog(LogData(projectInfo.projectId, LogData.Level.NORMAL,"","开启定时任务 \n ${GsonUtils.toJson(config)}"))
            }
        } else {
            //关闭定时任务
            WorkManager.getInstance(activity).cancelUniqueWork(projectInfo.projectId.toString())
            model?.viewModelScope?.launch(Dispatchers.IO) {
                model?.sendLog(LogData(projectInfo.projectId, LogData.Level.NORMAL,"","关闭定时任务"))
            }
        }
    }

}