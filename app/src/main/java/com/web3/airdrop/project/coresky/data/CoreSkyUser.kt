package com.web3.airdrop.project.coresky.data

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.PrimaryKey
import com.web3.airdrop.base.BaseUser
import com.web3.airdrop.data.Wallet
import com.web3.airdrop.project.layeredge.data.LayerEdgeAccountInfo

@Entity(tableName = "CoreSkyUser",indices = [Index(value = ["address"], unique = true)])
data class CoreSkyUser(
    @PrimaryKey var address: String,
    var background: String,
    var blog: String,
    var discord: String,
    var id: Int,
    var ins: String,
    var isBD: Int,
    var nickname: String,
    var other: String,
    var photo: String,
    var refCode: String,
    var refUrl: String?,
    var rewards: Int,
    var score: Double,
    var telegram: String,
    var twitter: String,
    var userVerify: Int,
) : BaseUser() {

    constructor(localWallet: Wallet) : this(
        address = localWallet.address.toString(),
        background = "",
        blog = "",
        discord = "",
        id = 0,
        ins = "",
        isBD = 0,
        nickname = "",
        other = "",
        photo = "",
        refCode = "",
        refUrl = "",
        rewards = 0,
        score = 0.0,
        telegram = "",
        twitter = "",
        userVerify = 0
    )

    @Ignore
    fun isLogin() : Boolean{
        return !(id == 0 || token?.isEmpty() == true || (System.currentTimeMillis() - lastSyncTime) > 1000*60*60*24)
    }
}

data class LoginResult(
    val user: CoreSkyUser,
    val token: String
)