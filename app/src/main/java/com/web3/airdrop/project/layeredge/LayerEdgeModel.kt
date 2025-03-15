package com.web3.airdrop.project.layeredge

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.web3.airdrop.data.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

class LayerEdgeModel : ViewModel() {

    val walletAccountEvent = MutableLiveData<MutableList<LayerEdgeAccountInfo>>(mutableListOf())

    fun refreshLocalWallet() {
        viewModelScope.launch(Dispatchers.IO) {
            AppDatabase.getDatabase().walletDao().getWalletsByChain("ETH").let {
                walletAccountEvent.postValue(it.map {
                    LayerEdgeAccountInfo(it, nodePoints = 0, taskPoints = 0).apply {
                    }
                }.toMutableList())
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
            walletAccountEvent.value?.shuffled()?.forEach {
                LayerEdgeCommand.requestAccountInfo(it)
                delay(Random.nextLong(10,30))
            }
        }
    }

    fun registerAll() {
        viewModelScope.launch {
            walletAccountEvent.value?.shuffled()?.forEach {
                if (!it.isRegister) {
                    LayerEdgeCommand.registerAccount(it)
                    delay(Random.nextLong(10,30))
                }
            }
        }
    }

    fun refreshNodeState() {
        viewModelScope.launch {
            walletAccountEvent.value?.shuffled()?.forEach {
                LayerEdgeCommand.refreshNodeState(it)
                delay(Random.nextLong(10,30))
            }
        }
    }

    fun connectNode() {
        viewModelScope.launch {
            walletAccountEvent.value?.shuffled()?.forEach {
                if (!it.nodeStart) {
                    LayerEdgeCommand.connectNode(it)
                    delay(Random.nextLong(10,30))
                }
            }
        }
    }

    fun signEveryDay() {
        viewModelScope.launch {
            walletAccountEvent.value?.shuffled()?.filter {
                !it.isSign
            }?.let {
                LayerEdgeCommand.signEveryDay(it)
            }
        }
    }

}