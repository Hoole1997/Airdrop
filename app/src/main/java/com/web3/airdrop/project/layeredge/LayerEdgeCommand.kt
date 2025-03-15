package com.web3.airdrop.project.layeredge

import com.blankj.utilcode.util.UiMessageUtils
import com.web3.airdrop.project.log.LogData

object LayerEdgeCommand {

    const val LAYER_EDGE_PROJECT_ID = 0
    const val MESSAGE_REQUEST_ACCOUNT = 101
    const val MESSAGE_LAYEREDGE_LOG = 102
    const val MESSAGE_REFRESH_ACCOUNT = 103
    const val MESSAGE_REGISTER_ACCOUNT = 104
    const val MESSAGE_REFRESH_NODE_STATE = 105
    const val MESSAGE_CONNECT_NODE = 106

    fun requestAccountInfo(accountInfo: LayerEdgeAccountInfo) {
        UiMessageUtils.getInstance().send(MESSAGE_REQUEST_ACCOUNT,accountInfo)
    }

    fun refreshAccountInfo(accountInfo: LayerEdgeAccountInfo) {
        UiMessageUtils.getInstance().send(MESSAGE_REFRESH_ACCOUNT,accountInfo)
    }

    fun refreshNodeState(accountInfo: LayerEdgeAccountInfo) {
        UiMessageUtils.getInstance().send(MESSAGE_REFRESH_NODE_STATE,accountInfo)
    }

    fun registerAccount(accountInfo: LayerEdgeAccountInfo) {
        UiMessageUtils.getInstance().send(MESSAGE_REGISTER_ACCOUNT,accountInfo)
    }

    fun addLog(log: LogData) {
        UiMessageUtils.getInstance().send(MESSAGE_LAYEREDGE_LOG,log)
    }

    fun connectNode(accountInfo: LayerEdgeAccountInfo) {
        UiMessageUtils.getInstance().send(MESSAGE_CONNECT_NODE,accountInfo)
    }

}