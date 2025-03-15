package com.web3.airdrop.ui.wallet

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.scopeLife
import androidx.lifecycle.viewModelScope
import com.blankj.utilcode.util.GsonUtils
import com.google.gson.reflect.TypeToken
import com.web3.airdrop.data.AppDatabase
import com.web3.airdrop.data.Wallet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WalletViewModel : ViewModel() {

    val wallet = MutableLiveData<MutableList<Wallet>>(arrayListOf<Wallet>())
    var currentChain = "ETH"

    fun queryWallet(chain: String) {
        currentChain = chain
        viewModelScope.launch(Dispatchers.IO) {
            AppDatabase.getDatabase().walletDao().getWalletsByChain(chain).let {
                wallet.postValue(it.toMutableList())
            }
        }
    }

    fun insertWallet(content: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val walletList: List<Wallet> = GsonUtils.fromJson(content, object : TypeToken<List<Wallet>>() {}.type)
            AppDatabase.getDatabase().walletDao().insertWalletList(walletList.toList())
            queryWallet(currentChain)
        }
    }

    fun deleteWallet(wallets: List<Wallet>) {
        viewModelScope.launch(Dispatchers.IO) {
            AppDatabase.getDatabase().walletDao().deleteWallet(wallets)
            queryWallet(currentChain)
        }
    }

}