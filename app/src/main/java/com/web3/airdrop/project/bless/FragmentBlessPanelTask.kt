package com.web3.airdrop.project.bless

import androidx.fragment.app.FragmentActivity
import com.web3.airdrop.base.IPanelTaskModule
import com.web3.airdrop.data.TaskConfig
import com.web3.airdrop.project.bless.data.BlessNodeInfo
import com.web3.airdrop.project.coresky.CoreSkyModel

class FragmentBlessPanelTask(activity: FragmentActivity, model: BlessModel?) : IPanelTaskModule<BlessModel, BlessNodeInfo>(activity,model) {

    override fun initTask(): List<PanelTask> {
        return mutableListOf<PanelTask>().apply {
            add(PanelTask("初始化节点","刚注册的账号需要生成一个节点", supportCombinationMode = false, support = true))
        }
    }

    override suspend fun taskClick(panelTask: List<PanelTask>) {
        model?.startTask(panelTask)
    }

    override fun initTaskTimingWorker(
        enable: Boolean,
        config: TaskConfig
    ) {

    }

}