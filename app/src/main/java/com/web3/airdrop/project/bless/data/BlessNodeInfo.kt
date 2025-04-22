package com.web3.airdrop.project.bless.data

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.PrimaryKey
import com.web3.airdrop.base.BaseUser
import com.web3.airdrop.data.Wallet

@Entity(tableName = "BlessNodeInfo", indices = [Index(value = ["address"], unique = true)])
class BlessNodeInfo() : BaseUser() {

    // 用户社交媒体信息
    var discordConnected: Boolean = false
    var discordId: String = ""
    var discordUsername: String = ""
    var xConnected: Boolean = false
    var xId: String = ""
    var xUsername: String = ""

    // 节点信息
    var _id: String =""
    var pubKey: String =""
    var userId: String =""
    var __v: Int =0
    var createdAt: String = ""
    var extensionVersion: String = ""
    var isRetired: Boolean = false
    var updatedAt: String = ""
    var totalReward: Int = 0
    var todayReward: Int = 0
    var lastPingAt: String = ""
    var isConnected: Boolean = false

    @PrimaryKey
    var address = ""

    @Ignore
    var wallet: Wallet? = null
}