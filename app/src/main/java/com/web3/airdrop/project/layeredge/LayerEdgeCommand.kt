package com.web3.airdrop.project.layeredge

import com.blankj.utilcode.util.UiMessageUtils
import com.web3.airdrop.project.layeredge.data.LayerEdgeAccountInfo
import com.web3.airdrop.project.log.LogData

object LayerEdgeCommand {

    const val LAYER_EDGE_PROJECT_ID = 0
    const val MESSAGE_REQUEST_ACCOUNT = 101
    const val MESSAGE_LAYEREDGE_LOG = 102
    const val MESSAGE_REFRESH_ACCOUNT = 103
    const val MESSAGE_REGISTER_ACCOUNT = 104
    const val MESSAGE_REFRESH_NODE_STATE = 105
    const val MESSAGE_CONNECT_NODE = 106
    const val MESSAGE_SIGN_EVERYDAY = 107
    const val MESSAGE_REQUEST_ACCOUNT_RESULT = 108
    const val MESSAGE_DISCONNECT_NODE = 109


    fun requestAccountInfo(accountList: List<LayerEdgeAccountInfo>) {
        UiMessageUtils.getInstance().send(MESSAGE_REQUEST_ACCOUNT,accountList)
    }

    fun refreshAccountInfo(accountList: List<LayerEdgeAccountInfo>) {
        UiMessageUtils.getInstance().send(MESSAGE_REFRESH_ACCOUNT,accountList)
    }

    fun refreshNodeState(accountList: List<LayerEdgeAccountInfo>) {
        UiMessageUtils.getInstance().send(MESSAGE_REFRESH_NODE_STATE,accountList)
    }

    fun registerAccount(accountList: List<LayerEdgeAccountInfo>) {
        UiMessageUtils.getInstance().send(MESSAGE_REGISTER_ACCOUNT,accountList)
    }

    fun addLog(log: LogData) {
        UiMessageUtils.getInstance().send(MESSAGE_LAYEREDGE_LOG,log)
    }

    fun connectNode(accountList: List<LayerEdgeAccountInfo>,connect: Boolean) {
        if (connect) {
            UiMessageUtils.getInstance().send(MESSAGE_CONNECT_NODE,accountList)
        } else {
            UiMessageUtils.getInstance().send(MESSAGE_DISCONNECT_NODE,accountList)
        }
    }

    fun signEveryDay(accountList: List<LayerEdgeAccountInfo>) {
        UiMessageUtils.getInstance().send(MESSAGE_SIGN_EVERYDAY,accountList)
    }

    fun requestAccountResult(account: LayerEdgeAccountInfo) {
        UiMessageUtils.getInstance().send(MESSAGE_REQUEST_ACCOUNT_RESULT,account)
    }

}