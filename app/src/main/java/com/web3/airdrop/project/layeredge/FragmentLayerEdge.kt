package com.web3.airdrop.project.layeredge

import android.content.Intent
import androidx.fragment.app.FragmentActivity
import com.web3.airdrop.base.BaseProjectFragment
import com.web3.airdrop.base.IPanelAccountInfoModule
import com.web3.airdrop.data.ProjectConfig
import com.web3.airdrop.databinding.ItemLayeredgeWalletBinding
import com.web3.airdrop.project.layeredge.data.LayerEdgeAccountInfo

class FragmentLayerEdge : BaseProjectFragment<LayerEdgeModel, LayerEdgeAccountInfo>() {

    var accountModule: LayerEdgeAccountModule? = null

    override fun startTaskService() {
        activity?.let {
            it.startForegroundService(Intent(it, LayerEdgeService::class.java))
        }
    }

    override fun stopTaskService() {
        activity?.let {
            it.stopService(Intent(it, LayerEdgeService::class.java))
        }
    }

    override fun initView(activity: FragmentActivity) {
        super.initView(activity)

        accountModule = LayerEdgeAccountModule(activity,binding.rvAccount).apply {
            loadItemAccountModule<ItemLayeredgeWalletBinding>(this)
        }
        loadTaskPanelModule(
            accountInfoModule = IPanelAccountInfoModule<LayerEdgeModel, LayerEdgeAccountInfo>(activity,model),
            taskModule = null
        )

        model?.walletAccountEvent?.observe(this) {
            accountModule?.refreshData(it)
        }
        model?.refreshLocalWallet(creator = {
            LayerEdgeAccountInfo()
        })
    }

}