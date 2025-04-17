package com.web3.airdrop.project.layeredge

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.scopeNetLife
import androidx.lifecycle.viewModelScope
import com.blankj.utilcode.util.GsonUtils
import com.drake.net.Get
import com.web3.airdrop.base.BaseModel
import com.web3.airdrop.data.AppDatabase
import com.web3.airdrop.extension.Extension.formatAddress
import com.web3.airdrop.extension.setProxy
import com.web3.airdrop.project.layeredge.data.LayerEdgeAccountInfo
import com.web3.airdrop.project.log.LogData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.Headers
import okhttp3.Response
import org.json.JSONObject

class LayerEdgeModel : BaseModel() {

    val headers = Headers.Builder()
        .add("Accept", "application/json, text/plain, */*")
        .add("Accept-Encoding", "gzip, deflate, br")
        .add("Accept-Language", "en-US,en;q=0.9")
        .add("Origin", "https://layeredge.io")
        .add("Referer", "https://layeredge.io/")
        .add("Sec-Fetch-Dest", "empty")
        .add("Sec-Fetch-Mode", "cors")
        .add("Sec-Fetch-Site", "same-site")
        .add("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/121.0.0.0 Safari/537.36")
        .add("sec-ch-ua", "\"Not A(Brand\";v=\"99\", \"Google Chrome\";v=\"121\", \"Chromium\";v=\"121\"")
        .add("sec-ch-ua-mobile", "?0")
        .add("sec-ch-ua-platform", "\"Windows\"")
        .build()

    val walletAccountEvent = MutableLiveData<MutableList<LayerEdgeAccountInfo>>(mutableListOf())

    override fun refreshPanelAccountInfo(info: Any,online: Boolean) {
        super.refreshPanelAccountInfo(info,online)
        if (info is LayerEdgeAccountInfo) {
            postPanelAccount(info)
            if (!online)return
            scopeNetLife(Dispatchers.IO) {
//                LayerEdgeCommand.addLog(LogData(LayerEdgeCommand.LAYER_EDGE_PROJECT_ID, LogData.Level.SUCCESS,info.wallet?.id,
//                    "请求用户信息 _id=${info.wallet?.address?.formatAddress()}"))
                try {
                    val detailResponse = Get<Response>("https://referralapi.layeredge.io/api/referral/wallet-details/${info.wallet?.address}") {
                        setHeaders(headers)
                        setClient {
                            setProxy(info.wallet?.proxy.toString())
                        }
                        converter = LayerEdgeConvert()
                    }.await()

                    val nodeStatusResponse = Get<Response>("https://referralapi.layeredge.io/api/light-node/node-status/${info.wallet?.address}"){
                        setHeaders(headers)
                        setClient {
                            setProxy(info.wallet?.proxy.toString())
                        }
                        converter = LayerEdgeConvert()
                    }.await()

                    val datailResult = JSONObject(detailResponse.body?.string())
                    datailResult.optString("data","").let {
                        if (it.isEmpty()) return@let
                        val accountInfo = GsonUtils.fromJson<LayerEdgeAccountInfo>(it,
                            LayerEdgeAccountInfo::class.java)

                        val nodeResult = JSONObject(nodeStatusResponse.body?.string())
                        val nodeData = nodeResult.optJSONObject("data")
                        if (nodeData != null) {
                            accountInfo.startTimestamp = nodeData.optLong("startTimestamp")
                        }
                        accountInfo.lastSyncTime = System.currentTimeMillis()
                        if (!accountInfo.isRegister) {
//                            LayerEdgeCommand.addLog(LogData(LayerEdgeCommand.LAYER_EDGE_PROJECT_ID, LogData.Level.WARN,info.wallet?.id,"未注册 "))
                        } else {
                            AppDatabase.getDatabase().layeredgeDao().insertOrUpdate(accountInfo)
//                            LayerEdgeCommand.addLog(LogData(LayerEdgeCommand.LAYER_EDGE_PROJECT_ID, LogData.Level.SUCCESS,info.wallet?.id,
//                                "同步用户信息成功 _id=${accountInfo.id} "))
//                            LayerEdgeCommand.requestAccountResult(accountInfo)
                            walletAccountEvent.value?.let {
                                val findIndex = it.indexOf(info)
                                accountInfo.wallet = info.wallet
                                val newList = it.toMutableList().apply {
                                    set(findIndex,accountInfo)
                                }
                                walletAccountEvent.postValue(newList)
                            }
                        }
                        postPanelAccount(accountInfo)
                    }
                }catch (e: Exception) {
//                    LayerEdgeCommand.addLog(LogData(LayerEdgeCommand.LAYER_EDGE_PROJECT_ID, LogData.Level.WARN,info.wallet?.id,"同步用户信息失败 exception:${e.message} "))
                }
            }
        }
    }

    private fun postPanelAccount(data: LayerEdgeAccountInfo) {
        panelAccountInfo.postValue(mutableListOf<Pair<String, String>>().apply {
            add(Pair("地址",data.wallet?.address?.formatAddress().toString()))
            add(Pair("注册",data.isRegister.toString()))
            add(Pair("boostNodePoints",data.boostNodePoints.toString()))
            add(Pair("cliBoostNodePoints",data.cliBoostNodePoints.toString()))
            add(Pair("cliNodePoints",data.cliNodePoints.toString()))
            add(Pair("confirmedReferralPoints",data.confirmedReferralPoints.toString()))
            add(Pair("createdAt",data.createdAt))
            add(Pair("dailyStreak",data.dailyStreak.toString()))
            add(Pair("id",data.id))
            add(Pair("isTwitterVerified",data.isTwitterVerified.toString()))
            add(Pair("nodePoints",data.nodePoints.toString()))
            add(Pair("rewardPoints",data.rewardPoints.toString()))
            add(Pair("totalPoints",data.totalPoints.toString()))
            add(Pair("twitterId",data.twitterId.toString()))
        })
    }

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
//                LayerEdgeCommand.requestAccountInfo(it)
            }
        }
    }

    fun registerAll() {
        viewModelScope.launch {
            walletAccountEvent.value?.shuffled()?.filter {
                !it.isRegister
            }?.let {
//                LayerEdgeCommand.registerAccount(it)
            }
        }
    }

    fun refreshNodeState() {
        viewModelScope.launch {
            walletAccountEvent.value?.shuffled()?.let {
//                LayerEdgeCommand.refreshNodeState(it)
            }
        }
    }

    fun connectNode(connect: Boolean) {
        viewModelScope.launch {
            walletAccountEvent.value?.shuffled()?.filter {
                if (connect) !it.nodeStart else it.nodeStart
            }?.let {
//                LayerEdgeCommand.connectNode(it,connect)
            }
        }
    }

    fun signEveryDay() {
        viewModelScope.launch {
            walletAccountEvent.value?.shuffled()?.let {
//                LayerEdgeCommand.signEveryDay(it)
            }
        }
    }



}