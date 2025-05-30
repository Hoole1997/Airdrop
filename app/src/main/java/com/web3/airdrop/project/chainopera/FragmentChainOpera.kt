package com.web3.airdrop.project.chainopera

import android.content.Intent
import android.os.Build
import androidx.fragment.app.FragmentActivity
import com.web3.airdrop.base.BaseProjectFragment
import com.web3.airdrop.base.IPanelAccountInfoModule
import com.web3.airdrop.databinding.ItemTakerProtocolWalletBinding

class FragmentChainOpera : BaseProjectFragment<ChainOperaModel, ChainOperaUser>() {

    var accountModule: ChainOperaAccountModule? = null

    override fun startTaskService() {
        activity?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                it.startForegroundService(Intent(it, ChainOperaService::class.java))
            } else {

            }
        }
    }

    override fun stopTaskService() {
        activity?.let {
            it.stopService(Intent(it, ChainOperaService::class.java))
        }
    }

    override fun initView(activity: FragmentActivity) {
        super.initView(activity)

        loadTaskPanelModule(
            accountInfoModule = IPanelAccountInfoModule<ChainOperaModel, ChainOperaUser>(activity,model),
            taskModule = FragmentChainOperaPanelTask(activity, model)
        )

        accountModule = ChainOperaAccountModule(activity, binding.rvAccount).apply {
            loadItemAccountModule<ItemTakerProtocolWalletBinding>(this)
        }
        model?.walletAccountEvent?.observe(this) {
            accountModule?.refreshData(it)
        }
        model?.refreshLocalWallet(creator = {
            ChainOperaUser()
        })
    }

}