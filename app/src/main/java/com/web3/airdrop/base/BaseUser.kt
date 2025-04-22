package com.web3.airdrop.base

import androidx.room.Ignore
import com.web3.airdrop.data.Wallet

open class BaseUser {

    var token: String? = ""
    var lastSyncTime: Long = 0L
    @Ignore
    var localWallet: Wallet? = null

    @Ignore
    open fun isRegister() : Boolean{
        return !token.isNullOrBlank()
    }
}