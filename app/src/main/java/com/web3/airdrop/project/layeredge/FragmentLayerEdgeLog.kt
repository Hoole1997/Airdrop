package com.web3.airdrop.project.layeredge

import androidx.lifecycle.ViewModelProvider
import com.web3.airdrop.project.log.FragmentLog
import com.web3.airdrop.project.log.LogViewModel

class FragmentLayerEdgeLog : FragmentLog() {

    override fun initProjectLogId(): Int {
        return LayerEdgeCommand.MESSAGE_LAYEREDGE_LOG
    }

    override fun initViewModel(): LogViewModel {
        return ViewModelProvider(requireActivity())[LogViewModel::class.java]
    }

}