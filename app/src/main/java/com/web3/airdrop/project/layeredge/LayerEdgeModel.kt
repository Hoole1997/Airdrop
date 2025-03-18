package com.web3.airdrop.project.layeredge

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.web3.airdrop.data.AppDatabase
import com.web3.airdrop.project.layeredge.data.LayerEdgeAccountInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LayerEdgeModel : ViewModel() {

    val walletAccountEvent = MutableLiveData<MutableList<LayerEdgeAccountInfo>>(mutableListOf())

    fun refreshLocalWallet() {
        viewModelScope.launch(Dispatchers.IO) {
            AppDatabase.getDatabase().walletDao().getWalletsByChain("ETH").let {
                val list = mutableListOf<LayerEdgeAccountInfo>()
                it.forEach { localInfo ->
                    val dbList = AppDatabase.getDatabase().layeredgeDao().getAccountByAddress(localInfo.address)
                    if (dbList.isNotEmpty()) {
                        list.add(dbList[0].apply {
                            wallet = localInfo
                        })
                    } else {
                        list.add(LayerEdgeAccountInfo().apply {
                            wallet = localInfo
                        })
                    }
                }
                walletAccountEvent.postValue(list)
            }
        }
    }

    fun sortRegister() {
        val list = walletAccountEvent.value ?: arrayListOf()
        list.sortedBy {
            it.isRegister
        }
        walletAccountEvent.postValue(list)
    }

    fun sortNode() {
        val list = walletAccountEvent.value ?: arrayListOf()
        list.sortedBy {
            it.nodeStart
        }
        walletAccountEvent.postValue(list)
    }

    fun allRequestAccountInfo() {
        viewModelScope.launch {
            walletAccountEvent.value?.shuffled()?.let {
                LayerEdgeCommand.requestAccountInfo(it)
            }
        }
    }

    fun registerAll() {
        viewModelScope.launch {
            walletAccountEvent.value?.shuffled()?.filter {
                !it.isRegister
            }?.let {
                LayerEdgeCommand.registerAccount(it)
            }
        }
    }

    fun refreshNodeState() {
        viewModelScope.launch {
            walletAccountEvent.value?.shuffled()?.let {
                LayerEdgeCommand.refreshNodeState(it)
            }
        }
    }

    fun connectNode(connect: Boolean) {
        viewModelScope.launch {
            walletAccountEvent.value?.shuffled()?.filter {
                if (connect) !it.nodeStart else it.nodeStart
            }?.let {
                LayerEdgeCommand.connectNode(it,connect)
            }
        }
    }

    fun signEveryDay() {
        viewModelScope.launch {
            walletAccountEvent.value?.shuffled()?.let {
                LayerEdgeCommand.signEveryDay(it)
            }
        }
    }



}