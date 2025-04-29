package com.web3.airdrop.base

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blankj.utilcode.util.GsonUtils
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.TimeUtils
import com.web3.airdrop.data.AppDatabase
import com.web3.airdrop.data.ProjectConfig
import com.web3.airdrop.data.Wallet
import com.web3.airdrop.extension.Extension.formatAddress
import com.web3.airdrop.project.TakerProtocol.data.TakerUser
import com.web3.airdrop.project.log.LogData
import com.web3.airdrop.project.takersowing.data.TakerSowingUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import kotlin.reflect.KClass

open class BaseModel<USER: BaseUser> : ViewModel(), IModel<USER> {
    //项目信息
    val taskInfo: MutableLiveData<ProjectConfig.ProjectInfo> = MutableLiveData<ProjectConfig.ProjectInfo>()
    //是否开始
    val taskStart: MutableLiveData<Boolean> = MutableLiveData<Boolean>(false)
    //面板--账号信息
    val panelAccountInfo = MutableLiveData<List<Pair<String, String>>>()
    //点击的账号
    val panelCurrentAccountInfo = MutableLiveData<USER?>()
    //全局模式
    val globalMode = MutableLiveData<Boolean>(false)
    //随机模式
    val randomMode = MutableLiveData<Boolean>(false)
    //组合模式
    val combinationMode = MutableLiveData<Boolean>(false)
    //日志
    val logEvent = MutableLiveData<ArrayList<LogData>>(arrayListOf<LogData>())
    //所有账户
    val walletAccountEvent = MutableLiveData<MutableList<USER>>(mutableListOf())

    override fun refreshPanelAccountInfo(data: USER,online: Boolean) {
        if (!online) {
            panelCurrentAccountInfo.postValue(data)
        }
        postPanelAccount(data)
        if (!online) return
        requestDetail(data)
    }

    override suspend fun startTask(panelTask: List<IPanelTaskModule.PanelTask>) {
        LogUtils.d("startTask globalMode=${globalMode.value}")
        val accountList : List<USER> = if (globalMode.value == true) {
            walletAccountEvent.value
        } else {
            if (panelCurrentAccountInfo.value == null) null else arrayListOf<USER>(panelCurrentAccountInfo.value as USER)
        } ?: arrayListOf()
        LogUtils.d("startTask ${accountList.size}")
        doTask(accountList.shuffled(),panelTask)
        taskStart.postValue(false)
    }

    open suspend fun doTask(accountList:List<USER>,panelTask: List<IPanelTaskModule.PanelTask>) {

    }

    override suspend fun sendLog(log: LogData) {
        withContext(Dispatchers.Main) {
            val list = arrayListOf<LogData>().apply {
                addAll(logEvent.value!!)
                add(log)
            }
            logEvent.postValue(list)
        }
    }

    override fun requestDetail(user: USER) {

    }

    override suspend fun getAccountByAddress(address: String): USER? {
        return null
    }

    fun clearLog() {
        logEvent.postValue(arrayListOf<LogData>())
    }

    fun postPanelAccount(data: USER) {
        panelAccountInfo.postValue(mutableListOf<Pair<String, String>>().apply {
            val jsonString = GsonUtils.toJson(data)
            val json = JSONObject(jsonString)
            add(Pair("地址", data.localWallet?.address?.formatAddress().toString()))
            add(Pair("注册", (data.isRegister()).toString()))
            add(
                Pair(
                    "最近更新",
                    if (data.lastSyncTime == 0L) "未同步" else TimeUtils.getFriendlyTimeSpanByNow(
                        data.lastSyncTime
                    )
                )
            )
            json.keys().forEach {
                add(Pair(it,json.opt(it)?.toString() ?:""))
            }
        })
    }

    fun refreshLocalWallet(creator: (Wallet) -> USER) {
        viewModelScope.launch(Dispatchers.IO) {
            val ethWallet = AppDatabase.getDatabase().walletDao().getWalletsByChain("ETH")
            val list = mutableListOf<USER>()
            ethWallet.forEach { localInfo ->
                val dbUser = getAccountByAddress(localInfo.address)
                if (dbUser != null) {
                    list.add(dbUser.apply {
                        localWallet = localInfo
                    })
                } else {
                    list.add(creator(localInfo).apply {
                        localWallet = localInfo
                    })
                }
            }
            walletAccountEvent.postValue(list)
        }
    }

}