package com.web3.airdrop.project.takersowing

import android.content.Intent
import android.os.Build
import androidx.fragment.app.FragmentActivity
import com.web3.airdrop.base.BaseProjectFragment
import com.web3.airdrop.base.IPanelAccountInfoModule
import com.web3.airdrop.data.ProjectConfig
import com.web3.airdrop.databinding.ItemTakerProtocolWalletBinding
import com.web3.airdrop.project.TakerProtocol.FragmentTakerPanelTask
import com.web3.airdrop.project.TakerProtocol.TakerAccountModule
import com.web3.airdrop.project.TakerProtocol.TakerModel
import com.web3.airdrop.project.TakerProtocol.TakerProtocolService
import com.web3.airdrop.project.TakerProtocol.data.TakerUser
import com.web3.airdrop.project.takersowing.data.TakerSowingUser


class FragmentTakerSowing : BaseProjectFragment<TakerSowingModel, TakerSowingUser>() {

    var accountModule: TakerSowingAccountModule? = null

    override fun startTaskService() {
        activity?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                it.startForegroundService(Intent(it, TakerSowingService::class.java))
            } else {

            }
        }
    }

    override fun stopTaskService() {
        activity?.let {
            it.stopService(Intent(it, TakerSowingService::class.java))
        }
    }

    override fun initView(activity: FragmentActivity) {
        super.initView(activity)

        loadTaskPanelModule(
            accountInfoModule = IPanelAccountInfoModule<TakerSowingModel, TakerSowingUser>(activity,model),
            taskModule = FragmentTakerSowingPanelTask(activity, model)
        )

        accountModule = TakerSowingAccountModule(activity, binding.rvAccount).apply {
            loadItemAccountModule<ItemTakerProtocolWalletBinding>(this)
        }
        model?.walletAccountEvent?.observe(this) {
            accountModule?.refreshData(it)
        }
        model?.refreshLocalWallet(creator = {
            TakerSowingUser(it)
        })
    }

}