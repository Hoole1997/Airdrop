package com.web3.airdrop.project.bless

import android.content.Intent
import androidx.fragment.app.FragmentActivity
import com.web3.airdrop.base.BaseProjectFragment
import com.web3.airdrop.base.IPanelAccountInfoModule
import com.web3.airdrop.data.ProjectConfig
import com.web3.airdrop.databinding.ItemBlessWalletBinding
import com.web3.airdrop.project.bless.data.BlessNodeInfo

class FragmentBless: BaseProjectFragment<BlessModel>() {

    lateinit var info: ProjectConfig.ProjectInfo

    override fun initProjectInfo(): ProjectConfig.ProjectInfo {
        info = arguments?.getSerializable("info") as ProjectConfig.ProjectInfo
        return info
    }

    override fun startTaskService() {
        activity?.let {
            it.startForegroundService(Intent(it, BlessService::class.java))
        }
    }

    override fun stopTaskService() {
        activity?.let {
            it.stopService(Intent(it, BlessService::class.java))
        }
    }

    override fun initView(activity: FragmentActivity) {
        super.initView(activity)

        loadTaskPanelModule(
            accountInfoModule = IPanelAccountInfoModule<BlessModel>(activity,model),
            taskModule = FragmentBlessPanelTask(activity,model)
        )
        loadItemAccountModule<BlessNodeInfo, ItemBlessWalletBinding>(BlessNodeListModule(activity, binding.rvAccount,model))
    }

}