package com.web3.airdrop.project.coresky

import android.content.Context
import androidx.lifecycle.viewModelScope
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.blankj.utilcode.util.GsonUtils
import com.blankj.utilcode.util.SPUtils
import com.web3.airdrop.base.BaseService
import com.web3.airdrop.base.BaseTimingWork
import com.web3.airdrop.data.ProjectConfig
import com.web3.airdrop.data.TaskConfig
import com.web3.airdrop.project.TakerProtocol.TakerModel
import com.web3.airdrop.project.log.LogData
import com.web3.airdrop.project.takersowing.TakerSowingModel
import com.web3.airdrop.project.takersowing.data.TakerSowingUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CoreSkyTimingWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
    override fun doWork(): Result {
        val projectId = inputData.getInt("PROJECT_ID", 0)
        val model = BaseService.Companion.modelMap[projectId] as CoreSkyModel?
        if (model == null)return Result.failure()

        val spConfig = SPUtils.getInstance(projectId.toString()).getString("TaskConfig","")
        if (spConfig.isNotBlank()) {
            model.viewModelScope.launch(Dispatchers.IO) {
                val config = GsonUtils.fromJson<TaskConfig>(spConfig, TaskConfig::class.java)
                config.taskList?.let {
                    model.taskStart.postValue(true)
                    model.startTask(it.filter {
                        it.check
                    })
                    model.sendLog(
                        LogData(
                            ProjectConfig.Companion.PROJECT_ID_TAKERPROTOCOL_SOWING,
                            LogData.Level.NORMAL,
                            "",
                            "定时任务开始 每${config.timing}执行一次"
                        )
                    )
                }
            }
        }

        return Result.success()
    }


}