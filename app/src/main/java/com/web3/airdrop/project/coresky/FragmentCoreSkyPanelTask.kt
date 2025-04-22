package com.web3.airdrop.project.coresky

import androidx.fragment.app.FragmentActivity
import com.web3.airdrop.base.IPanelTaskModule
import com.web3.airdrop.project.coresky.data.CoreSkyUser

class FragmentCoreSkyPanelTask(activity: FragmentActivity, model: CoreSkyModel?) : IPanelTaskModule<CoreSkyModel, CoreSkyUser>(activity,model) {

    override fun initTask(): List<PanelTask> {
        return mutableListOf<PanelTask>().apply {
            add(PanelTask("每日签到","🔥0撸！！24小时免费签到一次 20积分", supportCombinationMode = true, support = true))
            add(PanelTask("关注推特","推特需要授权，不支持自动化 50积分", supportCombinationMode = false, support = false))
            add(PanelTask("加入Discord","Discord需要授权，不支持自动化 50积分", supportCombinationMode = false, support = false))
            add(PanelTask("抽奖","需要少量gsa（忽略不计），抽奖获得积分 每日3次", supportCombinationMode = true, support = true))
            add(PanelTask("MEME投票","使用积分投票给MEME，影响空投权重(随机投5-20积分)", supportCombinationMode = true, support = true))
        }
    }

    override suspend fun taskClick(panelTask: List<PanelTask>) {
        model?.startTask(panelTask)
    }

}