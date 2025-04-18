package com.web3.airdrop.project.TakerProtocol

import android.content.Intent
import android.os.Build
import androidx.fragment.app.FragmentActivity
import com.web3.airdrop.base.BaseProjectFragment
import com.web3.airdrop.base.IPanelAccountInfoModule
import com.web3.airdrop.data.ProjectConfig
import com.web3.airdrop.databinding.ItemTakerProtocolWalletBinding
import com.web3.airdrop.project.TakerProtocol.data.TakerUser


class FragmentTakerProtocol : BaseProjectFragment<TakerModel>() {

    var accountModule: TakerAccountModule? = null

    override fun initProjectInfo(): ProjectConfig.ProjectInfo {
        return arguments?.getSerializable("info") as ProjectConfig.ProjectInfo
    }

    override fun startTaskService() {
        activity?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                it.startForegroundService(Intent(it, TakerProtocolService::class.java))
            } else {

            }
        }
    }

    override fun stopTaskService() {
        activity?.let {
            it.stopService(Intent(it, TakerProtocolService::class.java))
        }
    }

    override fun initView(activity: FragmentActivity) {
        super.initView(activity)

        loadTaskPanelModule(
            accountInfoModule = IPanelAccountInfoModule<TakerModel>(activity,model),
            taskModule = FragmentTakerPanelTask(activity,model)
        )

        accountModule = TakerAccountModule(activity,binding.rvAccount).apply {
            loadItemAccountModule<TakerUser, ItemTakerProtocolWalletBinding>(this)
        }
        model?.walletAccountEvent?.observe(this) {
            accountModule?.refreshData(it)
        }
        model?.refreshLocalWallet()
    }

}