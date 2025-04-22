package com.web3.airdrop.project.takersowing.data

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.PrimaryKey
import com.web3.airdrop.base.BaseUser
import com.web3.airdrop.data.Wallet

@Entity(tableName = "TakerSowingUser", indices = [Index(value = ["walletAddress"], unique = true)])
data class TakerSowingUser(
    val id:Long,
    @PrimaryKey val walletAddress: String,
    val invitationCode: String,
    val takerPoints: Double,
    val consecutiveSignInCount:Int,
    val nextTimestamp:Long,
    val rewardCount:Int,
    val discordBindStatus: Boolean,
    val tgBindStatus: Boolean,
    var firstSign: Boolean,
    val bindingBtcWallet: Boolean,
    val xbindStatus: Boolean
): BaseUser() {

    @Ignore
    var canSign: Boolean = false

    constructor(localWallet: Wallet) : this(
        id = 0L,
        walletAddress = localWallet.address.lowercase(),
        invitationCode = "",
        takerPoints = 0.0,
        consecutiveSignInCount = 0,
        nextTimestamp = 0L,
        rewardCount = 0,
        discordBindStatus = false,
        tgBindStatus = false,
        firstSign = false,
        bindingBtcWallet = false,
        xbindStatus = false
    )

    @Ignore
    fun getNewUser(user: TakerSowingUser) : TakerSowingUser {
        user.localWallet = localWallet
        user.token = token
        user.lastSyncTime = System.currentTimeMillis()
        return user
    }
}
