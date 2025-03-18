package com.web3.airdrop.project.somnia

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.web3.airdrop.data.AppDatabase
import com.web3.airdrop.project.somnia.bean.SomniaAccount
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SomniaViewModel : ViewModel() {

    val accountEvent = MutableLiveData<MutableList<SomniaAccount>>(arrayListOf())

    fun loadLocalAccount() {
        viewModelScope.launch(Dispatchers.IO) {
            AppDatabase.getDatabase().walletDao().getWalletsByChain("ETH").apply {
                val list = mutableListOf<SomniaAccount>()
                forEach { wallet ->
                    list.add(SomniaAccount(
                        walletAddress = wallet.address,
                        totalPoints = "0",
                        totalBoosters = "0",
                        finalPoints = "0",
                        rank = "",
                        seasonId = "0",
                        totalReferrals = "0",
                        questsCompleted = "0",
                        dailyBooster = 0.0,
                        streakCount = "0"
                    ).apply {
                        this.wallet = wallet
                    })
                }
                accountEvent.postValue(list)
            }
        }
    }

    fun queryAccountInfo(address: String) {

    }

}