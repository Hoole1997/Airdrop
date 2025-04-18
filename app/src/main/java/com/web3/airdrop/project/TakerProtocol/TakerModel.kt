package com.web3.airdrop.project.TakerProtocol

import androidx.core.util.Pools
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.scopeNetLife
import androidx.lifecycle.viewModelScope
import com.blankj.utilcode.util.GsonUtils
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.TimeUtils
import com.drake.net.Net
import com.twocaptcha.TwoCaptcha
import com.twocaptcha.captcha.ReCaptcha
import com.web3.airdrop.base.BaseModel
import com.web3.airdrop.base.IPanelTaskModule
import com.web3.airdrop.data.AppDatabase
import com.web3.airdrop.data.ProjectConfig
import com.web3.airdrop.extension.Extension.formatAddress
import com.web3.airdrop.extension.Web3Utils
import com.web3.airdrop.extension.setProxy
import com.web3.airdrop.project.TakerProtocol.data.TakerGenerateNonceResult
import com.web3.airdrop.project.TakerProtocol.data.TakerLoginResult
import com.web3.airdrop.project.TakerProtocol.data.TakerUser
import com.web3.airdrop.project.TakerProtocol.data.TaskStateResult
import com.web3.airdrop.project.coresky.CoreSkyModel.Companion.getHeader
import com.web3.airdrop.project.coresky.data.CoreSkyConverter
import com.web3.airdrop.project.coresky.data.CoreSkyUser
import com.web3.airdrop.project.coresky.data.LoginResult
import com.web3.airdrop.project.log.LogData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.Headers
import org.json.JSONObject
import kotlin.random.Random

class TakerModel : BaseModel() {

    companion object {
        fun getHeader(token: String?, requestUrl: String): Headers {
            return Headers.Builder().apply {
                add("Accept-Language", "en-US,en;q=0.9")
                add("hearder_gray_set", "0")
                add("Origin", "https://earn.taker.xyz")
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
                    add("Authorization", "Bearer $token")
                }
            }.build()
        }
    }

    val walletAccountEvent = MutableLiveData<MutableList<TakerUser>>(mutableListOf())

    fun refreshLocalWallet() {
        viewModelScope.launch() {
            val ethWallet = AppDatabase.getDatabase().walletDao().getWalletsByChain("ETH")

            val list = mutableListOf<TakerUser>()
            ethWallet.forEach { localInfo ->
                val dbUser = AppDatabase.getDatabase().takerDao()
                    .getAccountByAddress(localInfo.address.lowercase())
                if (dbUser != null) {
                    list.add(dbUser.apply {
                        wallet = localInfo
                    })
                } else {
                    list.add(TakerUser(localInfo).apply {
                        wallet = localInfo
                    })
                }
            }
            walletAccountEvent.postValue(list)
        }
    }

    override fun refreshPanelAccountInfo(data: Any, online: Boolean) {
        super.refreshPanelAccountInfo(data, online)
        if (data is TakerUser) {
            postPanelAccount(data)
            if (!online) return
            requestDetail(data)
        }
    }

    private fun postPanelAccount(data: TakerUser) {
        panelAccountInfo.postValue(mutableListOf<Pair<String, String>>().apply {
            val jsonString = GsonUtils.toJson(data)
            val json = JSONObject(jsonString)
            add(Pair("地址", data.wallet?.address?.formatAddress().toString()))
            add(Pair("注册", (data.userId > 0).toString()))
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

    private suspend fun apiLogin(user: TakerUser): String? {

        var message = ""
        Net.post("https://lightmining-api.taker.xyz/wallet/generateNonce", block = {
            json(
                "walletAddress" to user.wallet?.address
            )
            setClient {
                setProxy(user.wallet?.proxy)
            }
            setHeaders(getHeader(null, "https://earn.taker.xyz/"))
            converter = TakerConverter()
        }).toResult<TakerGenerateNonceResult>().let {
            if (it.isSuccess) {
                message = it.getOrNull()?.nonce ?:""
            }
        }
        if (message.isBlank()) return null

        val loginResult = Net.post("https://lightmining-api.taker.xyz/wallet/login", block = {
            val signature = Web3Utils.signPrefixedMessage(message, user.wallet?.privateKey)
            json(
                "address" to user.wallet?.address,
                "message" to message,
                "signature" to signature,
                "invitationCode" to if (user.walletAddress.endsWith("2276f1")) "" else "8YPC6"
            )
            setClient {
                setProxy(user.wallet?.proxy)
            }
            setHeaders(getHeader(null, "https://earn.taker.xyz/"))
            converter = TakerConverter()
        }).toResult<TakerLoginResult>()

        if (loginResult.isFailure) {
            sendLog(LogData(ProjectConfig.PROJECT_ID_TAKERPROTOCOL, LogData.Level.ERROR,user.walletAddress.formatAddress(),"登录失败，获取token失败 ${loginResult.exceptionOrNull()?.message}"))
            return null
        }
        val token = loginResult.getOrNull()?.token

        user.token = token
        AppDatabase.getDatabase().takerDao().insertOrUpdate(user)
        sendLog(LogData(ProjectConfig.PROJECT_ID_TAKERPROTOCOL, LogData.Level.SUCCESS,user.walletAddress.formatAddress(),"登录成功"))
        return token
    }

    private suspend fun apiUserInfo(user: TakerUser) : TakerUser? {
        if (!user.isLogin()) {
            user.token = apiLogin(user)
            if (user.token?.isNullOrBlank() == true) {
                return null
            }
        }

        val userResult = Net.get("https://lightmining-api.taker.xyz/user/getUserInfo", block = {
            setClient {
                setProxy(user.wallet?.proxy)
            }
            setHeaders(getHeader(user.token, "https://earn.taker.xyz/"))
            converter = TakerConverter()
        }).toResult<TakerUser>().getOrNull()

        if (userResult != null) {
            val newUser = user.getNewUser(userResult)

            Net.get("https://lightmining-api.taker.xyz/assignment/totalMiningTime", block = {
                setClient {
                    setProxy(user.wallet?.proxy)
                }
                setHeaders(getHeader(user.token, "https://earn.taker.xyz/"))
                converter = TakerConverter()
            }).toResult<String>().let {
                JSONObject(it.getOrNull()).let { json ->
                    val lastMiningTime = json.optJSONObject("data")?.optLong("lastMiningTime")
                    val totalMiningTime = json.optJSONObject("data")?.optLong("totalMiningTime")
                    newUser.lastMiningTime = lastMiningTime ?:0L
                    newUser.totalMiningTime = totalMiningTime ?:0L
                }
            }

            AppDatabase.getDatabase().takerDao().insertOrUpdate(newUser)
            walletAccountEvent.value?.let {
                val findIndex = it.indexOfFirst {
                    it.walletAddress == user.walletAddress
                }
                val newList = it.toMutableList().apply {
                    if (findIndex < this.size && findIndex >= 0) {
                        set(findIndex, newUser)
                    }
                }
                walletAccountEvent.postValue(newList)
            }
            postPanelAccount(newUser)
            sendLog(LogData(ProjectConfig.PROJECT_ID_TAKERPROTOCOL, LogData.Level.SUCCESS,user.walletAddress.formatAddress(),"获取用户信息成功"))
            return newUser
        } else {
            sendLog(LogData(ProjectConfig.PROJECT_ID_TAKERPROTOCOL, LogData.Level.ERROR,user.walletAddress.formatAddress(),"获取用户信息失败"))
            return null
        }
    }

    private fun requestDetail(user: TakerUser) {
        scopeNetLife(Dispatchers.IO) {
            runCatching {
                apiUserInfo(user)
            }.onFailure {
                sendLog(LogData(ProjectConfig.PROJECT_ID_TAKERPROTOCOL, LogData.Level.ERROR,user.walletAddress.formatAddress(),"获取用户信息异常 ${it.message}"))
            }
        }
    }

    override fun startTask(panelTask: List<IPanelTaskModule.PanelTask>) {
        super.startTask(panelTask)

        val accountList : List<TakerUser> = if (globalMode.value == true) {
            walletAccountEvent.value
        } else {
            arrayListOf<TakerUser>(panelCurrentAccountInfo.value as TakerUser)
        } ?: return

        scopeNetLife(Dispatchers.IO) {
            accountList.forEachIndexed {index, account ->
                runCatching {
                    sendLog(LogData(projectId = ProjectConfig.PROJECT_ID_TAKERPROTOCOL, LogData.Level.SUCCESS,account.walletAddress.formatAddress(),""))
                    if (!account.isLogin()) {
                        account.token = apiLogin(account)
                    }
                    val userTask = taskStateList(account)
                    panelTask.filter { task ->
                        userTask.find {
                            task.taskName == it.title && it.done
                        } == null
                    }.apply {
                        if (randomMode.value == true) {
                            shuffled()
                        }
                        sendLog(LogData(projectId = ProjectConfig.PROJECT_ID_TAKERPROTOCOL, LogData.Level.NORMAL,account.walletAddress.formatAddress(),"开始任务:${this.map { it.taskName }}"))
                        delay(1500)
                    }.forEachIndexed { index,task ->
                        sendLog(LogData(projectId = ProjectConfig.PROJECT_ID_TAKERPROTOCOL, LogData.Level.NORMAL,account.walletAddress.formatAddress(),"开始-- ${task.taskName}"))
                        when(task.taskName) {
                            "每日签到" -> {
                                apiSign(account)
                            }
                            "Like a Tweet" -> {
                                dailyTasks(13,task.taskName,account)
                            }
                            "Retweet a Tweet" -> {
                                dailyTasks(14,task.taskName,account)
                            }
                            "Comment on a Tweet" -> {
                                dailyTasks(15,task.taskName,account)
                            }
                            "Follow Taker X" -> {
                                dailyTasks(1,task.taskName,account)
                            }
                            "Share a Tweet About Lite-Mining Campaign" -> {
                                dailyTasks(5,task.taskName,account)
                            }
                        }
                        val delayTime = Random.nextLong(5000, 15000)
                        sendLog(LogData(projectId = ProjectConfig.PROJECT_ID_TAKERPROTOCOL, LogData.Level.NORMAL,account.walletAddress.formatAddress(),"${task.taskName}完成，等待${delayTime/1000}秒"))
                        delay(delayTime)
                    }
                    if (index < accountList.size-1) {
                        val delayTime = Random.nextLong(5000, 21000)
                        sendLog(LogData(projectId = ProjectConfig.PROJECT_ID_TAKERPROTOCOL, LogData.Level.NORMAL,account.walletAddress.formatAddress(),"全部任务完成，${index+1}/${accountList.size}，等待${delayTime/1000}秒"))
                        delay(delayTime)
                    }
                }.onFailure {
                    sendLog(LogData(projectId = ProjectConfig.PROJECT_ID_TAKERPROTOCOL, LogData.Level.ERROR,account.walletAddress.formatAddress(),"异常 ${it.message}"))
                }

            }

        }

    }

    private suspend fun apiSign(user: TakerUser) {
        if (!user.isLogin()) {
            user.token = apiLogin(user)
        }
        if ((System.currentTimeMillis() - user.lastMiningTime*1000) < 1000*60*60*24) {
            sendLog(LogData(projectId = ProjectConfig.PROJECT_ID_TAKERPROTOCOL, LogData.Level.WARN,user.walletAddress.formatAddress(),"上次签到未超过24小时"))
            return
        }
        Net.post("https://lightmining-api.taker.xyz/assignment/startMining", block = {
            json(
                "status" to true,
                //                "verifyResp" to ""
            )
            setClient {
                setProxy(user.wallet?.proxy)
            }
            setHeaders(getHeader(user.token, "https://earn.taker.xyz/"))
        }).toResult<String>().apply {
            val json = JSONObject(this.getOrNull())
            if (json.optInt("code") == 200) {
                sendLog(LogData(projectId = ProjectConfig.PROJECT_ID_TAKERPROTOCOL, LogData.Level.SUCCESS,user.walletAddress.formatAddress(),"签到成功"))
            } else {
                val msg = json.optString("msg")
                sendLog(LogData(projectId = ProjectConfig.PROJECT_ID_TAKERPROTOCOL, LogData.Level.ERROR,user.walletAddress.formatAddress(),"签到失败 $msg"))
            }
        }
    }

    private suspend fun dailyTasks(assignmentId:Int, taskName: String, user: TakerUser) {
        if (!user.isLogin()) {
            user.token = apiLogin(user)
        }
        val verifyResp = verifyResp()
        if (verifyResp.isBlank()) {
            return
        }
        Net.post("https://lightmining-api.taker.xyz/assignment/do", block = {
            json(
                "assignmentId" to assignmentId,
                "verifyResp" to verifyResp
            )
            setClient {
                setProxy(user.wallet?.proxy)
            }
            setHeaders(getHeader(user.token, "https://earn.taker.xyz/"))
        }).toResult<String>().apply {
            val json = JSONObject(this.getOrNull())
            if (json.optInt("code") == 200) {
                sendLog(LogData(projectId = ProjectConfig.PROJECT_ID_TAKERPROTOCOL, LogData.Level.SUCCESS,user.walletAddress.formatAddress(),"$taskName 成功"))
            } else {
                val msg = json.optString("msg")
                sendLog(LogData(projectId = ProjectConfig.PROJECT_ID_TAKERPROTOCOL, LogData.Level.ERROR,user.walletAddress.formatAddress(),"$taskName 签到失败 $msg"))
            }
        }
    }

    private suspend fun verifyResp() : String {

        val result = Net.post("https://2captcha.com/in.php", block = {
            json(
                "key" to "4bbaf9020e107f5c89ae4c28e12bbc24",
                "method" to "turnstile",
                "sitekey" to "0x4AAAAAAA4ve7ZW4oTHaChP",
                "pageurl" to "https://earn.taker.xyz/",
                "json" to 1
            )
        }).toResult<String>()
        var request = JSONObject(result.getOrNull()).optString("request")
        var verifyResp = ""
        for(index in 1..3) {
            sendLog(LogData(projectId = ProjectConfig.PROJECT_ID_TAKERPROTOCOL, LogData.Level.NORMAL,"","识别验证码中...$index/3"))
            delay(10000)
            val string = Net.get("https://2captcha.com/res.php?key=4bbaf9020e107f5c89ae4c28e12bbc24&action=get&id=$request&json=1").toResult<String>()
            val json = JSONObject(string.getOrNull())
            if (json.optInt("status") == 1) {
                verifyResp = json.optString("request")
                break
            }
        }
        if (verifyResp.isBlank()) {
            sendLog(LogData(projectId = ProjectConfig.PROJECT_ID_TAKERPROTOCOL, LogData.Level.ERROR,"","识别验证码失败"))
        }
        return verifyResp
    }

    private suspend fun taskStateList(user: TakerUser) : List<TaskStateResult> {
        return Net.post("https://lightmining-api.taker.xyz/assignment/list", block = {
            setClient {
                setProxy(user.wallet?.proxy)
            }
            setHeaders(getHeader(user.token, "https://earn.taker.xyz/"))
            converter = TakerConverter()
        }).toResult<List<TaskStateResult>>().getOrNull() ?: arrayListOf()
    }

}