package com.web3.airdrop.base

import com.web3.airdrop.project.log.LogData

interface IModel {

    fun refreshPanelAccountInfo(data: Any,online: Boolean)

    fun startTask(panelTask: List<IPanelTaskModule.PanelTask>)

    suspend fun sendLog(log: LogData)
}