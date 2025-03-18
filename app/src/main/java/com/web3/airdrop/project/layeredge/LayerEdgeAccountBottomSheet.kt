package com.web3.airdrop.project.layeredge

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.blankj.utilcode.util.GsonUtils
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.TimeUtils
import com.blankj.utilcode.util.UiMessageUtils
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.web3.airdrop.R
import com.web3.airdrop.data.AppDatabase
import com.web3.airdrop.databinding.DialogLayeredgeInfoBinding
import com.web3.airdrop.extension.Extension.formatAddress
import com.web3.airdrop.extension.formatJson
import com.web3.airdrop.project.layeredge.data.LayerEdgeAccountInfo

class LayerEdgeAccountBottomSheet(val accountInfo: LayerEdgeAccountInfo) : BottomSheetDialogFragment() {

    lateinit var binding: DialogLayeredgeInfoBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_layeredge_info, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = DialogLayeredgeInfoBinding.bind(view)

        refreshInfo(accountInfo)

        binding.btnCheckin.setOnClickListener {
            LayerEdgeCommand.signEveryDay(arrayListOf(accountInfo))
        }
        binding.btnRegister.setOnClickListener {
            LayerEdgeCommand.registerAccount(arrayListOf(accountInfo))
        }
        binding.btnConnect.setOnClickListener {
            LayerEdgeCommand.connectNode(arrayListOf(accountInfo),true)
        }
        binding.btnDisconnect.setOnClickListener {
            LayerEdgeCommand.connectNode(arrayListOf(accountInfo),false)
        }

        UiMessageUtils.getInstance().addListener(LayerEdgeCommand.MESSAGE_REQUEST_ACCOUNT_RESULT) {
            val account = it.`object` as LayerEdgeAccountInfo
            refreshInfo(account)
        }
        LayerEdgeCommand.requestAccountInfo(arrayListOf(accountInfo))
    }

    private fun refreshInfo(accountInfo: LayerEdgeAccountInfo) {
        binding.ethAddress.text = accountInfo.wallet?.address?.formatAddress()
        binding.tvConnect.text = "Connect: ${accountInfo.nodeStart}"
        binding.tvConnectDownTime.text = "LastSync:${if (accountInfo.lastSyncTime == 0L) 0 else TimeUtils.millis2String(accountInfo.lastSyncTime) }"
        binding.tvCheckinDay.text = "CheckinDay:${accountInfo.dailyStreak}"
        binding.tvContent.text = GsonUtils.toJson(accountInfo).formatJson()
    }

}