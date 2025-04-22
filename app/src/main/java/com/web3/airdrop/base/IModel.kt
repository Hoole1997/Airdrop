package com.web3.airdrop.base

import com.web3.airdrop.project.log.LogData
import com.web3.airdrop.project.takersowing.data.TakerSowingUser

interface IModel<USER: BaseUser> {

    fun refreshPanelAccountInfo(data: USER,online: Boolean)

    suspend fun startTask(panelTask: List<IPanelTaskModule.PanelTask>)

    suspend fun sendLog(log: LogData)

    fun requestDetail(user: USER)

    suspend fun getAccountByAddress(address: String):USER?
}