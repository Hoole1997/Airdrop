package com.web3.airdrop.project.coresky

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.scopeNetLife
import androidx.lifecycle.viewModelScope
import com.blankj.utilcode.util.GsonUtils
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.TimeUtils
import com.drake.net.Net
import com.drake.net.Post
import com.web3.airdrop.base.BaseModel
import com.web3.airdrop.base.IPanelTaskModule
import com.web3.airdrop.data.AppDatabase
import com.web3.airdrop.data.ProjectConfig
import com.web3.airdrop.data.Wallet
import com.web3.airdrop.extension.Extension.formatAddress
import com.web3.airdrop.extension.Web3Utils
import com.web3.airdrop.extension.setProxy
import com.web3.airdrop.project.coresky.data.CoreSkyConverter
import com.web3.airdrop.project.coresky.data.CoreSkyUser
import com.web3.airdrop.project.coresky.data.LoginResult
import com.web3.airdrop.project.coresky.data.ScoreDetailResult
import com.web3.airdrop.project.coresky.data.SignResult
import com.web3.airdrop.project.coresky.db.CoreSkyDao
import com.web3.airdrop.project.layeredge.data.LayerEdgeAccountInfo
import com.web3.airdrop.project.log.LogData
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.Headers
import org.json.JSONObject
import kotlin.concurrent.thread
import kotlin.random.Random
import kotlin.random.nextInt
import kotlin.random.nextLong

class CoreSkyModel : BaseModel() {

    companion object {
        fun getHeader(token: String?, requestUrl: String): Headers {
            return Headers.Builder().apply {
                add("Accept-Language", "en-US,en;q=0.9")
                add("hearder_gray_set", "0")
                add("Origin", "https://www.coresky.com")
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
                    add("token", token)
                }
            }.build()
        }
    }

    val walletAccountEvent = MutableLiveData<MutableList<CoreSkyUser>>(mutableListOf())

    fun refreshLocalWallet() {
        viewModelScope.launch() {
            val ethWallet = AppDatabase.getDatabase().walletDao().getWalletsByChain("ETH")

            val list = mutableListOf<CoreSkyUser>()
            ethWallet.forEach { localInfo ->
                val dbUser = AppDatabase.getDatabase().coreSkyDao()
                    .getAccountByAddress(localInfo.address.lowercase())
                if (dbUser != null) {
                    list.add(dbUser.apply {
                        wallet = localInfo
                    })
                } else {
                    list.add(CoreSkyUser(localInfo).apply {
                        wallet = localInfo
                    })
                }
            }
            walletAccountEvent.postValue(list)
        }
    }

    override fun refreshPanelAccountInfo(data: Any, online: Boolean) {
        super.refreshPanelAccountInfo(data, online)
        if (data is CoreSkyUser) {
            postPanelAccount(data)
            if (!online) return
            requestDetail(data)
        }
    }

    private fun postPanelAccount(data: CoreSkyUser) {
        panelAccountInfo.postValue(mutableListOf<Pair<String, String>>().apply {
            val jsonString = GsonUtils.toJson(data)
            val json = JSONObject(jsonString)
            add(Pair("地址", data.wallet?.address?.formatAddress().toString()))
            add(Pair("注册", (data.id > 0).toString()))
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

    private suspend fun apiLogin(user: CoreSkyUser): LoginResult? {
        val message = "Welcome to CoreSky!\n" +
                "\n" +
                "Click to sign in and accept the CoreSky Terms of Service.\n" +
                "\n" +
                "This request will not trigger a blockchain transaction or cost any gas fees.\n" +
                "\n" +
                "Your authentication status will reset after 24 hours.\n" +
                "\n" +
                "Wallet address:\n" +
                "\n" +
                "${user.wallet?.address}"
        return Net.post("https://www.coresky.com/api/user/login", block = {
            val signature = Web3Utils.signPrefixedMessage(message, user.wallet?.privateKey)
            json(
                "address" to user.wallet?.address,
                "projectId" to "",
                "refCode" to "",
                "signature" to signature
            )
            setClient {
                setProxy(user.wallet?.proxy)
            }
            setHeaders(getHeader(null, "https://www.coresky.com/tasks-rewards"))
            converter = CoreSkyConverter()
        }).toResult<LoginResult>().apply {
            if (isSuccess && this.getOrNull() != null) {
                val resultUser = this.getOrNull()!!.user
                resultUser.lastSyncTime = System.currentTimeMillis()
                resultUser.wallet = user.wallet
                resultUser.token = this.getOrNull()!!.token
                user.token = this.getOrNull()!!.token
                AppDatabase.getDatabase().coreSkyDao().insertOrUpdate(resultUser)
                walletAccountEvent.value?.let {
                    val findIndex = it.indexOfFirst {
                        it.address == user.address
                    }
                    resultUser.wallet = user.wallet
                    val newList = it.toMutableList().apply {
                        set(findIndex, resultUser)
                    }
                    walletAccountEvent.postValue(newList)
                }
                postPanelAccount(resultUser)
                sendLog(LogData(ProjectConfig.PROJECT_ID_CORESKY, LogData.Level.SUCCESS,resultUser.address.formatAddress(),"登录成功"))
            } else {
                sendLog(LogData(ProjectConfig.PROJECT_ID_CORESKY, LogData.Level.ERROR,user.address.formatAddress(),"登录失败 ${this.exceptionOrNull()?.message}"))
            }
        }.getOrNull()
    }

    private suspend fun apiToken(user: CoreSkyUser): CoreSkyUser? {
        return Net.post("https://www.coresky.com/api/user/token", block = {
            setClient {
                setProxy(user.wallet?.proxy)
            }
            setHeaders(getHeader(user.token, "https://www.coresky.com/tasks-rewards"))
            converter = CoreSkyConverter()
        }).toResult<CoreSkyUser>().apply {
            val it = this.getOrNull()
            if (isSuccess && it != null) {
                val loginUser = it
                loginUser.lastSyncTime = System.currentTimeMillis()
                loginUser.wallet = user.wallet
                loginUser.token = user.token
                AppDatabase.getDatabase().coreSkyDao().insertOrUpdate(loginUser)
                walletAccountEvent.value?.let {
                    val findIndex = it.indexOfFirst {
                        it.address == user.address
                    }
                    loginUser.wallet = user.wallet
                    val newList = it.toMutableList().apply {
                        set(findIndex, loginUser)
                    }
                    walletAccountEvent.postValue(newList)
                }
                sendLog(LogData(ProjectConfig.PROJECT_ID_CORESKY, LogData.Level.SUCCESS,loginUser.address.formatAddress(),"获取用户信息成功"))
            } else {
                sendLog(LogData(ProjectConfig.PROJECT_ID_CORESKY, LogData.Level.ERROR,user.address.formatAddress(),"获取用户信息失败 ${this.exceptionOrNull()?.message}"))
            }
        }.getOrNull()
    }

    //签到
    private suspend fun apiSign(user: CoreSkyUser): SignResult? {
        var token = user.token
        if (!user.isLogin()) {
            token = apiLogin(user)?.token ?: return null
        }
        val canSign = apiScoreDetail(user)
        if (!canSign) {
            return null
        }
        return Net.post("https://www.coresky.com/api/taskwall/meme/sign", block = {
            setClient {
                setProxy(user.wallet?.proxy)
            }
            setHeaders(getHeader(user.token, "https://www.coresky.com/api/taskwall/meme/sign"))
            converter = CoreSkyConverter()
        }).toResult<SignResult>().apply {
            if (isSuccess && getOrNull() != null) {
                sendLog(LogData(ProjectConfig.PROJECT_ID_CORESKY, LogData.Level.SUCCESS,user.address.formatAddress(),"签到成功 ${this.getOrNull()?.signDay}"))
            } else {
                sendLog(LogData(ProjectConfig.PROJECT_ID_CORESKY, LogData.Level.ERROR,user.address.formatAddress(),"签到失败"))
            }
        }.getOrNull()
    }

    //判断是否可签到
    private suspend fun apiScoreDetail(user: CoreSkyUser): Boolean {
        val result = Net.post("https://www.coresky.com/api/user/score/detail", block = {
            json(
                "address" to user.wallet?.address,
                "limit" to 10,
                "page" to 1
            )
            setClient {
                setProxy(user.wallet?.proxy)
            }
            setHeaders(getHeader(user.token, "https://www.coresky.com/tasks-rewards"))
            converter = CoreSkyConverter()
        }).toResult<ScoreDetailResult>()
        result.let {
            if (it.isSuccess && it.getOrNull() != null) {
                it.getOrNull()?.detail?.listData?.firstOrNull {
                    it.score == "20"
                }?.let {
                    val canSign = (System.currentTimeMillis() - it.createTime*1000) > 1000*60*60*24
                    if (canSign) {

                        sendLog(LogData(ProjectConfig.PROJECT_ID_CORESKY, LogData.Level.NORMAL,user.address.formatAddress(),"最新积分 ${result.getOrNull()?.score}，上次签到时间：${TimeUtils.millis2String(it.createTime*1000)}"))
                    }else {
                        sendLog(LogData(ProjectConfig.PROJECT_ID_CORESKY, LogData.Level.WARN,user.address.formatAddress(),"最新积分 ${result.getOrNull()?.score}，上次签到时间：${TimeUtils.millis2String(it.createTime*1000)}，未超过24小时，不可签到"))
                    }
                    return canSign
                }
            } else {
                sendLog(LogData(ProjectConfig.PROJECT_ID_CORESKY, LogData.Level.ERROR,user.address.formatAddress(),"获取上次签到时间失败，跳过"))
                return false
            }

            return false
        }
        return false
    }

    //meme投票
    private suspend fun apiVote(user: CoreSkyUser) {
        var token = user.token
        if (!user.isLogin()) {
            token = apiLogin(user)?.token ?: return
        }
        val projectId = arrayListOf("4519","51","6281","1462","2893","6406","1880").shuffled().first()
        val voteNum = Random.nextInt(5, 20)
        Net.post("https://www.coresky.com/api/taskwall/meme/vote", block = {
            json(
                "projectId" to projectId,
                "voteNum" to voteNum
            )
            setClient {
                setProxy(user.wallet?.proxy)
            }
            setHeaders(getHeader(user.token, "https://www.coresky.com/meme"))
            converter = CoreSkyConverter()
        }).toResult<String>().let {

            if (it.isSuccess) {
                LogUtils.d("投票成功")
                sendLog(LogData(ProjectConfig.PROJECT_ID_CORESKY, LogData.Level.SUCCESS,user.address.formatAddress(),"投票成功 projectId:$projectId voteNum:$voteNum"))
            } else {
                sendLog(LogData(ProjectConfig.PROJECT_ID_CORESKY, LogData.Level.ERROR,user.address.formatAddress(),"投票失败"))
            }
        }
    }

    override fun startTask(panelTask: List<IPanelTaskModule.PanelTask>) {
        super.startTask(panelTask)

        val accountList : List<CoreSkyUser> = if (globalMode.value == true) {
            walletAccountEvent.value
        } else {
            arrayListOf<CoreSkyUser>(panelCurrentAccountInfo.value as CoreSkyUser)
        } ?: return

        scopeNetLife(Dispatchers.IO) {
            accountList.forEachIndexed {index, account ->
                runCatching {
                    sendLog(LogData(projectId = ProjectConfig.PROJECT_ID_CORESKY, LogData.Level.SUCCESS,account.address.formatAddress(),""))
                    panelTask.apply {
                        if (randomMode.value == true) {
                            shuffled()
                        }
                        sendLog(LogData(projectId = ProjectConfig.PROJECT_ID_CORESKY, LogData.Level.NORMAL,account.address.formatAddress(),"开始任务:${this.map { it.taskName }}"))
                        delay(1500)
                    }.forEachIndexed { index,task ->
                        sendLog(LogData(projectId = ProjectConfig.PROJECT_ID_CORESKY, LogData.Level.NORMAL,account.address.formatAddress(),"开始-- ${task.taskName}"))
                        when(task.taskName) {
                            "每日签到" -> {
                                apiSign(account)
                            }
                            "抽奖" -> {
                                delay(1000)
                                sendLog(LogData(projectId = ProjectConfig.PROJECT_ID_CORESKY, LogData.Level.WARN,account.address.formatAddress(),"${task.taskName}开发中"))
                            }
                            "MEME投票" -> {
                                apiVote(account)
                            }
                        }
                        val delayTime = Random.nextLong(5000, 15000)


                        if (index == panelTask.size-1) {
                            delay(1000)
                            sendLog(LogData(projectId = ProjectConfig.PROJECT_ID_CORESKY, LogData.Level.NORMAL,account.address.formatAddress(),"${task.taskName}完成"))
                            delay(1000)
                        } else {
                            sendLog(LogData(projectId = ProjectConfig.PROJECT_ID_CORESKY, LogData.Level.NORMAL,account.address.formatAddress(),"${task.taskName}完成，等待${delayTime/1000}秒"))
                            delay(delayTime)
                        }
                    }
                    if (index < accountList.size-1) {
                        val delayTime = Random.nextLong(5000, 21000)
                        sendLog(LogData(projectId = ProjectConfig.PROJECT_ID_CORESKY, LogData.Level.NORMAL,account.address.formatAddress(),"全部任务完成，等待${delayTime/1000}秒"))
                        delay(delayTime)
                    }
                }.onFailure {
                    sendLog(LogData(projectId = ProjectConfig.PROJECT_ID_CORESKY, LogData.Level.ERROR,account.address.formatAddress(),"异常 ${it.message}"))
                }

            }

        }
    }

    private fun requestDetail(user: CoreSkyUser) {
        scopeNetLife(Dispatchers.IO) {

            runCatching {
                if (!user.isLogin()) {
                    apiLogin(user)?.let {
                        user.token = it.token
                        val loginUser = it.user
                        loginUser.lastSyncTime = System.currentTimeMillis()
                        loginUser.wallet = user.wallet
                        loginUser.token = it.token
                        AppDatabase.getDatabase().coreSkyDao().insertOrUpdate(loginUser)
                        walletAccountEvent.value?.let {
                            val findIndex = it.indexOfFirst {
                                it.address == user.address
                            }
                            loginUser.wallet = user.wallet
                            val newList = it.toMutableList().apply {
                                if (findIndex < this.size) {
                                    set(findIndex, loginUser)
                                }
                            }
                            walletAccountEvent.postValue(newList)
                        }
                    } ?: return@scopeNetLife
                }

                apiToken(user)?.let {
                    val apiUser = it
                    apiUser.token = user.token
                    apiUser.wallet = user.wallet
                    apiUser.lastSyncTime = System.currentTimeMillis()
                    AppDatabase.getDatabase().coreSkyDao().insertOrUpdate(apiUser)
                    postPanelAccount(apiUser)
                    walletAccountEvent.value?.let {
                        val findIndex = it.indexOfFirst {
                            it.address == user.address
                        }
                        apiUser.wallet = user.wallet
                        val newList = it.toMutableList().apply {
                            if (findIndex < this.size) {
                                set(findIndex, apiUser)
                            }
                        }
                        walletAccountEvent.postValue(newList)
                    }
                }
            }.onFailure {
                it.printStackTrace()
                LogUtils.d(it.message)
            }
        }
    }


}