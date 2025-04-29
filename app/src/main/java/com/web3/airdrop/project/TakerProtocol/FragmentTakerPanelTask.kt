package com.web3.airdrop.project.TakerProtocol

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewModelScope
import androidx.work.Data
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.blankj.utilcode.util.GsonUtils
import com.web3.airdrop.base.IPanelTaskModule
import com.web3.airdrop.data.TaskConfig
import com.web3.airdrop.project.TakerProtocol.data.TakerUser
import com.web3.airdrop.project.coresky.CoreSkyModel
import com.web3.airdrop.project.log.LogData
import com.web3.airdrop.project.takersowing.TakerSowingTimingWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class FragmentTakerPanelTask(activity: FragmentActivity, model: TakerModel?) : IPanelTaskModule<TakerModel, TakerUser>(activity,model) {

    override fun initTask(): List<PanelTask> {
        return mutableListOf<PanelTask>().apply {
            add(PanelTask("每日签到","🔥0撸！！24小时签到一次 有钱包交互", supportCombinationMode = true, support = true))
            add(PanelTask("Like a Tweet","点赞推特，每日任务 +60积分", supportCombinationMode = true, support = true))
            add(PanelTask("Retweet a Tweet","转发推特，每日任务，+60积分", supportCombinationMode = true, support = true))
            add(PanelTask("Comment on a Tweet","评论推特，每日任务，+60积分", supportCombinationMode = true, support = true))
            add(PanelTask("Follow Taker X","关注推特，需要授权推特，不支持自动化 +500积分", supportCombinationMode = true, support = true))
            add(PanelTask("Join Taker Discord","加入Discord，需要授权，不支持自动化 +500积分", supportCombinationMode = false, support = false))
            add(PanelTask("Join Taker Telegram Group","加入TG，需要授权，不支持自动化，+500积分", supportCombinationMode = false,support = false))
            add(PanelTask("Follow Taker Telegram Announcements","进入TG频道 ,+500积分", supportCombinationMode = false, support = false))
            add(PanelTask("Share a Tweet About Lite-Mining Campaign","分享推特 ,+500积分", supportCombinationMode = true, support = true))
        }
    }

    override suspend fun taskClick(panelTask: List<PanelTask>) {
        model?.startTask(panelTask)
    }

    override fun initTaskTimingWorker(enable: Boolean, config: TaskConfig) {
        val projectInfo = model?.taskInfo?.value ?: return
        if (enable) {
            //开启定时任务
            val workRequest = PeriodicWorkRequest.Builder(
                TakerTimingWorker::class.java,
                config.timing, TimeUnit.HOURS
            ).setInputData(Data.Builder().putInt("PROJECT_ID",projectInfo.projectId).build())
                .build()

            WorkManager.getInstance(activity).enqueueUniquePeriodicWork(
                projectInfo.projectId.toString(), // 唯一标签，避免重复调度
                androidx.work.ExistingPeriodicWorkPolicy.KEEP, // 替换已有任务
                workRequest
            )
            model.viewModelScope?.launch(Dispatchers.IO) {
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