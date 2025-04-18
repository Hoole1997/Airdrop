package com.web3.airdrop.project.TakerProtocol

import androidx.fragment.app.FragmentActivity
import com.web3.airdrop.base.IPanelTaskModule
import com.web3.airdrop.project.coresky.CoreSkyModel

class FragmentTakerPanelTask(activity: FragmentActivity, model: TakerModel?) : IPanelTaskModule<TakerModel>(activity,model) {

    override fun initTask(): List<PanelTask> {
        return mutableListOf<PanelTask>().apply {
            add(PanelTask("每日签到","🔥0撸！！24小时签到一次 有钱包交互", supportCombinationMode = true, support = true))
            add(PanelTask("Like a Tweet","点赞推特，每日任务 +60积分", supportCombinationMode = true, support = true))
            add(PanelTask("Retweet a Tweet","转发推特，每日任务，+60积分", supportCombinationMode = true, support = true))
            add(PanelTask("Comment on a Tweet","评论推特，每日任务，+60积分", supportCombinationMode = true, support = true))
            add(PanelTask("Follow Taker X","关注推特，需要授权推特，不支持自动化 +500积分", supportCombinationMode = true, support = true))
            add(PanelTask("Join Taker Discord","加入Discord，需要授权，不支持自动化 +500积分", supportCombinationMode = false, support = false))
            add(PanelTask("Join Taker Telegram Group","加入TG，需要授权，不支持自动化，+500积分", supportCombinationMode = false,support = false))
            add(PanelTask("Follow Taker Telegram Announcements","进入TG频道 ,+500积分", supportCombinationMode = false, support = false))
            add(PanelTask("Share a Tweet About Lite-Mining Campaign","分享推特 ,+500积分", supportCombinationMode = true, support = true))
        }
    }

    override fun taskClick(panelTask: List<PanelTask>) {
        model?.startTask(panelTask)
    }

}