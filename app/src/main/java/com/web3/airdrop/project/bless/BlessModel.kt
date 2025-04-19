package com.web3.airdrop.project.bless

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.scopeNetLife
import androidx.lifecycle.viewModelScope
import com.blankj.utilcode.util.GsonUtils
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.TimeUtils
import com.drake.net.Get
import com.web3.airdrop.base.BaseModel
import com.web3.airdrop.base.IPanelTaskModule
import com.web3.airdrop.data.AppDatabase
import com.web3.airdrop.extension.Extension.formatAddress
import com.web3.airdrop.extension.setProxy
import com.web3.airdrop.project.bless.data.BlessNodeInfo
import com.web3.airdrop.project.coresky.CoreSkyModel
import com.web3.airdrop.project.coresky.data.CoreSkyUser
import kotlinx.coroutines.launch
import okhttp3.Headers
import org.json.JSONObject

class BlessModel : BaseModel() {

    companion object {
        fun getHeader(token: String?, requestUrl: String = "https://bless.network/"): Headers {
            return Headers.Builder().apply {
                add("Accept-Language", "en-US,en;q=0.9")
                add("hearder_gray_set", "0")
                add("Origin", "https://bless.network")
                add("Referer", requestUrl)
                add("cookie", "selectWallet=MetaMask")
                add("Sec-Fetch-Dest", "empty")
                add("Sec-Fetch-Mode", "cors")
                add("Sec-Fetch-Site", "same-site")
                add(
                    "User-Agent",
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/121.0.0.0 Safari/537.36"
                )
                add(
                    "sec-ch-ua",
                    "\"Not A(Brand\";v=\"99\", \"Google Chrome\";v=\"121\", \"Chromium\";v=\"121\""
                )
                add("sec-ch-ua-mobile", "?0")
                add("sec-ch-ua-platform", "\"Windows\"")
                if (token?.isNotEmpty() == true) {
                    add("authorization", token)
                }
            }.build()
        }
    }


    val walletAccountEvent = MutableLiveData<MutableList<BlessNodeInfo>>(mutableListOf())

    override fun startTask(panelTask: List<IPanelTaskModule.PanelTask>) {
        super.startTask(panelTask)

        scopeNetLife {
            val accountList : List<BlessNodeInfo> = if (globalMode.value == true) {
                walletAccountEvent.value
            } else {
                arrayListOf<BlessNodeInfo>(panelCurrentAccountInfo.value as BlessNodeInfo)
            } ?: arrayListOf()
            accountList.forEach { nodeInfo ->
                panelTask.forEach { task ->
                    when(task.taskName) {
                        "初始化节点" -> {
                            initNode(nodeInfo)
                        }
                        else -> {

                        }
                    }
                }
            }

        }

    }

    fun refreshLocalWallet() {
        viewModelScope.launch() {
            val ethWallet = AppDatabase.getDatabase().walletDao().getWalletsByChain("ETH")

            val list = mutableListOf<BlessNodeInfo>()
            ethWallet.forEach { localInfo ->
                val dbUser = AppDatabase.getDatabase().blessNodeDao()
                    .getNodeByAddress(localInfo.address)
                if (dbUser != null) {
                    list.add(dbUser.apply {
                        wallet = localInfo
                        address = localInfo.address
                    })
                } else {
                    list.add(BlessNodeInfo().apply {
                        wallet = localInfo
                        address = localInfo.address
                    })
                }
            }
            walletAccountEvent.postValue(list)
        }
    }

    override fun refreshPanelAccountInfo(data: Any, online: Boolean) {
        super.refreshPanelAccountInfo(data, online)
        if (data is BlessNodeInfo) {
            postPanelAccount(data)
            if (!online) return
            requestDetail(data)
        }
    }

    private fun postPanelAccount(data: BlessNodeInfo) {
        panelAccountInfo.postValue(mutableListOf<Pair<String, String>>().apply {
            val jsonString = GsonUtils.toJson(data)
            val json = JSONObject(jsonString)
            add(Pair("地址", data.wallet?.address?.formatAddress().toString()))
            add(
                Pair(
                    "最近更新",
                    if (data.lastSyncTime == 0L) "未同步" else TimeUtils.getFriendlyTimeSpanByNow(
                        data.lastSyncTime
                    )
                )
            )
            json.keys().forEach {
                add(Pair(it, json.opt(it).toString()))
            }
        })
    }

    private fun requestDetail(nodeInfo: BlessNodeInfo) {
        scopeNetLife {
            Get<String>("https://gateway-run-indexer.bls.dev/api/v1/users/socials") {
                setClient {
                    setProxy(nodeInfo.wallet?.proxy)
                }
                setHeaders(
                    getHeader(nodeInfo.token,"https://bless.network/")
                )
            }.await().apply {
                JSONObject(this).apply {
                    nodeInfo.discordConnected = optBoolean("discordConnected")
                    nodeInfo.discordId = optString("discordId")
                    nodeInfo.discordUsername = optString("discordUsername")
                    nodeInfo.xConnected = optBoolean("xConnected")
                    nodeInfo.xId = optString("xId")
                    nodeInfo.xUsername = optString("xUsername")
                }
                refreshNodeInfo(nodeInfo)
            }
        }
    }

    //初始化节点
    private suspend fun initNode(nodeInfo: BlessNodeInfo) : BlessNodeInfo{
//        LogUtils.d(BlessHardwareInfo.getHardwareIdentifierFromNodeId())
        LogUtils.d(BlessCpuData.generateDeviceIdentifier())
        return nodeInfo
    }

    private fun refreshNodeInfo(nodeInfo: BlessNodeInfo) {
        postPanelAccount(nodeInfo)
        walletAccountEvent.value?.let {
            val findIndex = it.indexOfFirst {
                it.address == nodeInfo.address
            }
            val newList = it.toMutableList().apply {
                if (findIndex < this.size && findIndex >= 0) {
                    set(findIndex, nodeInfo)
                }
            }
            walletAccountEvent.postValue(newList)
        }
    }

}