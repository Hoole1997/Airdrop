package com.web3.airdrop.project.TakerProtocol.data

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.PrimaryKey
import com.web3.airdrop.data.Wallet

@Entity(tableName = "TakerUser", indices = [Index(value = ["walletAddress"], unique = true)])
data class TakerUser(
    val userId: Long,
    @PrimaryKey val walletAddress: String,
    val invitationCode: String,
    val rewardAmount: String,
    val inviteCount: Int,
    val invitationReward: String,
    val totalReward: String,
    val tgId: String?,
    val dcId: String?,
    val twId: String?,
    val twName: String?
) {
    var token: String? = ""
    var lastSyncTime: Long = 0L
    var lastMiningTime: Long = 0L
    var totalMiningTime: Long = 0L

    @Ignore
    var wallet: Wallet? = null

    constructor(localWallet: Wallet) : this(
        userId = 0L,
        walletAddress = localWallet.address,
        invitationCode = "",
        rewardAmount = "",
        inviteCount = 0,
        invitationReward = "",
        totalReward = "",
        tgId = "",
        dcId = "",
        twId = "",
        twName = ""
    )

    @Ignore
    fun isLogin(): Boolean {
        return !(token?.isEmpty() == true || ((System.currentTimeMillis() - lastSyncTime) > 1000 * 60 * 60 * 24) && lastSyncTime != 0L)
    }

    @Ignore
    fun getNewUser(user: TakerUser) : TakerUser {
        user.wallet = wallet
        user.token = token
        user.lastSyncTime = System.currentTimeMillis()
        user.lastMiningTime = lastMiningTime
        user.totalMiningTime = totalMiningTime
        return user
    }
}
