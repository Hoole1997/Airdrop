package com.web3.airdrop.project.chainopera

import com.web3.airdrop.base.BaseUser
import com.web3.airdrop.data.Wallet
import com.web3.airdrop.project.takersowing.data.TakerSowingUser

data class ChainOperaUser(
    val totalPoints: Long,
) : BaseUser() {

    constructor() : this(
        totalPoints = 0L
    )

}
