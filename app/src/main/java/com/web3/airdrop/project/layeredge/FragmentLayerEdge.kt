package com.web3.airdrop.project.layeredge

import android.content.Intent
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import com.blankj.utilcode.util.LogUtils
import com.web3.airdrop.base.BaseProjectFragment
import com.web3.airdrop.base.IPanelAccountInfoModule
import com.web3.airdrop.data.ProjectConfig
import com.web3.airdrop.databinding.ItemLayeredgeWalletBinding
import com.web3.airdrop.project.coresky.CoreSkyService
import com.web3.airdrop.project.layeredge.data.LayerEdgeAccountInfo

class FragmentLayerEdge : BaseProjectFragment<LayerEdgeModel>() {

    lateinit var info: ProjectConfig.ProjectInfo
    var accountModule: LayerEdgeAccountModule? = null

//    suspend fun initModel(): LayerEdgeModel {
//        return ViewModelProvider(requireActivity())[LayerEdgeModel::class.java]
//    }

    override fun initProjectInfo(): ProjectConfig.ProjectInfo {
        info = arguments?.getSerializable("info") as ProjectConfig.ProjectInfo
        return info
    }

    override fun startTaskService() {
        activity?.let {
            it.startForegroundService(Intent(it, LayerEdgeService::class.java))
        }
    }

    override fun initView(activity: FragmentActivity) {
        super.initView(activity)

        accountModule = LayerEdgeAccountModule(activity,binding.rvAccount).apply {
            loadItemAccountModule<LayerEdgeAccountInfo,ItemLayeredgeWalletBinding>(this)
        }
        loadTaskPanelModule(
            accountInfoModule = IPanelAccountInfoModule<LayerEdgeModel>(activity,model),
            taskModule = null
        )

        model?.walletAccountEvent?.observe(this) {
            accountModule?.refreshData(it)
        }
        model?.refreshLocalWallet()
    }

}