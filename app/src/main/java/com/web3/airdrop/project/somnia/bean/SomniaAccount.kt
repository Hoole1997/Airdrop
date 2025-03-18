package com.web3.airdrop.project.somnia.bean

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.PrimaryKey
import com.web3.airdrop.data.Wallet
import java.math.BigDecimal

@Entity(tableName = "SomniaAccount",indices = [Index(value = ["walletAddress"], unique = true)])
data class SomniaAccount(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val walletAddress: String,
    val totalPoints: String,
    val totalBoosters: String,
    val finalPoints: String,
    val rank: String,
    val seasonId: String,
    val totalReferrals: String,
    val questsCompleted: String,
    val dailyBooster: Double,
    val streakCount: String,
) {

    @Ignore
    var wallet: Wallet? = null

}