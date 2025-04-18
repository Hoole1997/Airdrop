package com.web3.airdrop.project.coresky

import android.content.Intent
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import com.web3.airdrop.base.BaseProjectFragment
import com.web3.airdrop.base.IPanelAccountInfoModule
import com.web3.airdrop.data.ProjectConfig
import com.web3.airdrop.databinding.ItemCoreskyWalletBinding
import com.web3.airdrop.project.coresky.data.CoreSkyUser

class FragmentCoreSky: BaseProjectFragment<CoreSkyModel>() {

    lateinit var info: ProjectConfig.ProjectInfo
    var accountModule: CoreSkyAccountModule? = null

    override fun initProjectInfo(): ProjectConfig.ProjectInfo {
        info = arguments?.getSerializable("info") as ProjectConfig.ProjectInfo
        return info
    }

    override fun startTaskService() {
        activity?.let {
            it.startForegroundService(Intent(it, CoreSkyService::class.java))
        }
    }

    override fun stopTaskService() {
        activity?.let {
            it.stopService(Intent(it, CoreSkyService::class.java))
        }
    }

    override fun initView(activity: FragmentActivity) {
        super.initView(activity)

        loadTaskPanelModule(
            accountInfoModule = IPanelAccountInfoModule<CoreSkyModel>(activity,model),
            taskModule = FragmentCoreSkyPanelTask(activity,model)
        )
        accountModule = CoreSkyAccountModule(activity,binding.rvAccount).apply {
            loadItemAccountModule<CoreSkyUser, ItemCoreskyWalletBinding>(this)
        }
        model?.walletAccountEvent?.observe(this) {
            accountModule?.refreshData(it)
        }
        model?.refreshLocalWallet()
    }

}