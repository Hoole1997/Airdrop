package com.web3.airdrop.project.layeredge.data

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.PrimaryKey
import com.web3.airdrop.data.Wallet

@Entity(
    tableName = "LayerEdgeAccountInfo",
    indices = [Index(value = ["walletAddress"], unique = true)]
)
data class LayerEdgeAccountInfo(
    var boostNodePoints: Int,
    @Ignore val claimStreakPoints: List<ClaimStreakPoint>? = null,
    var cliBoostNodePoints: Int,
    var cliNodePoints: Int,
    var confirmedReferralPoints: Int,
    var createdAt: String,
    var dailyStreak: Int,
    var id: String,
    var isTwitterVerified: Boolean,
    var lastClaimed: String,
    @Ignore val lastResync: Any? = null,
    var level: Int,
    var nodePoints: Int,
    var referralCode: String,
    @Ignore val referrals: List<Referral>? = null,
    var rewardPoints: Int,
    var totalPoints: Int,
    var twitterId: String?,
    @Ignore val updateNotification: Any? = null,
    var updatedAt: String,
    @Ignore val verifiedProofs: List<Any>? = null,
    var verifiedReferralPoints: Int,
    var startTimestamp: Long = 0L,
    @PrimaryKey var walletAddress: String,
    var lastSyncTime: Long = 0L
) {

    constructor() : this(
        boostNodePoints = 0,
        cliBoostNodePoints = 0,
        cliNodePoints = 0,
        confirmedReferralPoints = 0,
        createdAt = "",
        dailyStreak = 0,
        id = "",
        isTwitterVerified = false,
        lastClaimed = "",
        level = 0,
        nodePoints = 0,
        referralCode = "",
        rewardPoints = 0,
        totalPoints = 0,
        twitterId = "",
        updatedAt = "",
        verifiedReferralPoints = 0,
        startTimestamp = 0L,
        walletAddress = ""
    )

    @Ignore
    var wallet: Wallet? = null

    @Ignore
    var isRegister = false
        get() = id.isNotEmpty()

    @Ignore
    var nodeStart = false
        get() = if (startTimestamp == 0L) {
            false
        } else {
            true
        }
}

data class ClaimStreakPoint(
    val _id: String,
    val createdAt: String,
    val day: Int,
    val id: String,
    val streakPoints: Int,
    val updatedAt: String
)

data class Referral(
    val _id: String,
    val confirmationPoints: Int,
    val confirmed: Boolean,
    val createdAt: String,
    val id: String,
    val points: Int,
    val proof: Any,
    val referredUser: ReferredUser,
    val subType: String,
    val type: String,
    val updatedAt: String,
    val verified: Boolean
)

data class ReferredUser(
    val _id: String,
    val confirmedReferralPoints: Int,
    val dailyStreak: Int,
    val id: String,
    val nodePoints: Int,
    val totalPoints: Int,
    val walletAddress: String
)

