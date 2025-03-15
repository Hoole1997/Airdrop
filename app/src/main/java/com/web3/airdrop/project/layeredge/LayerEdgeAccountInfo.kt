package com.web3.airdrop.project.layeredge

import com.web3.airdrop.data.Wallet

data class LayerEdgeAccountInfo(
    val wallet: Wallet,
    var nodePoints: Int,
    var taskPoints: Int,
    var isRegister: Boolean = false,
    var layerEdgeId: String? = null,
    var nodeStart: Boolean = false
)
