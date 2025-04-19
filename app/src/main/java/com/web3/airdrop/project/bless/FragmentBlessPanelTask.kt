package com.web3.airdrop.project.bless

import androidx.fragment.app.FragmentActivity
import com.web3.airdrop.base.IPanelTaskModule
import com.web3.airdrop.project.coresky.CoreSkyModel

class FragmentBlessPanelTask(activity: FragmentActivity, model: BlessModel?) : IPanelTaskModule<BlessModel>(activity,model) {

    override fun initTask(): List<PanelTask> {
        return mutableListOf<PanelTask>().apply {
            add(PanelTask("初始化节点","刚注册的账号需要生成一个节点", supportCombinationMode = false, support = true))
        }
    }

    override fun taskClick(panelTask: List<PanelTask>) {
        model?.startTask(panelTask)
    }

}