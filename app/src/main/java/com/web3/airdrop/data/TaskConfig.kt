package com.web3.airdrop.data

import com.web3.airdrop.base.IPanelTaskModule

data class TaskConfig(
    val globalMode: Boolean = false,//全局模式
    val timingMode: Boolean = false,//定时模式
    val timing: Long = 0,//每隔小时执行
    val taskList: List<IPanelTaskModule.PanelTask>? //任务
)
