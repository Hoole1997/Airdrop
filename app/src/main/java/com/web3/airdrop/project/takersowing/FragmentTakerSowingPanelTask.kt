package com.web3.airdrop.project.takersowing

import androidx.fragment.app.FragmentActivity
import com.web3.airdrop.base.IPanelTaskModule
import com.web3.airdrop.project.TakerProtocol.TakerModel
import com.web3.airdrop.project.coresky.CoreSkyModel
import com.web3.airdrop.project.takersowing.data.TakerSowingUser

class FragmentTakerSowingPanelTask(activity: FragmentActivity, model: TakerSowingModel?) : IPanelTaskModule<TakerSowingModel, TakerSowingUser>(activity,model) {

    override fun initTask(): List<PanelTask> {
        return mutableListOf<PanelTask>().apply {
            add(PanelTask("每日签到","🔥0撸！！3小时签到一次 +100积分", supportCombinationMode = true, support = true))
            add(PanelTask("BTC Basics Q&A","答题 一次性任务 +200积分 1NFT", supportCombinationMode = true, support = true))
            add(PanelTask("Explore and Understand Taker Protocol","手动授权推特 一次性任务，+100积分", supportCombinationMode = false, support = false))
        }
    }

    override suspend fun taskClick(panelTask: List<PanelTask>) {
        model?.startTask(panelTask)
    }

}