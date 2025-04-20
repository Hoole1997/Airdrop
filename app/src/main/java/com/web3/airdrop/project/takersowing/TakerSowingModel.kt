package com.web3.airdrop.project.takersowing

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.scopeNetLife
import androidx.lifecycle.viewModelScope
import com.blankj.utilcode.util.GsonUtils
import com.blankj.utilcode.util.TimeUtils
import com.drake.net.Net
import com.web3.airdrop.base.BaseModel
import com.web3.airdrop.base.IPanelTaskModule
import com.web3.airdrop.data.AppDatabase
import com.web3.airdrop.data.ProjectConfig
import com.web3.airdrop.extension.Extension.formatAddress
import com.web3.airdrop.extension.Web3Utils
import com.web3.airdrop.extension.setProxy
import com.web3.airdrop.project.TakerProtocol.TakerConverter
import com.web3.airdrop.project.TakerProtocol.TakerModel
import com.web3.airdrop.project.TakerProtocol.data.TakerGenerateNonceResult
import com.web3.airdrop.project.TakerProtocol.data.TakerLoginResult
import com.web3.airdrop.project.TakerProtocol.data.TakerUser
import com.web3.airdrop.project.TakerProtocol.data.TaskStateResult
import com.web3.airdrop.project.log.LogData
import com.web3.airdrop.project.takersowing.data.TakerSowingLoginResult
import com.web3.airdrop.project.takersowing.data.TakerSowingNonceResult
import com.web3.airdrop.project.takersowing.data.TakerSowingUser
import com.web3.airdrop.project.takersowing.data.TaskSowingTaskResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.Headers
import org.json.JSONObject
import kotlin.random.Random

class TakerSowingModel : BaseModel() {

    companion object {
        fun getHeader(token: String?, requestUrl: String,turnstile: String? = null): Headers {
            return Headers.Builder().apply {
                add("Accept-Language", "en-US,en;q=0.9")
                add("hearder_gray_set", "0")
                add("Origin", "https://sowing.taker.xyz")
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
                if (turnstile?.isNotEmpty() == true) {
                    add("Cf-Turnstile-Token", turnstile)
                }
            }.build()
        }
    }

    val walletAccountEvent = MutableLiveData<MutableList<TakerSowingUser>>(mutableListOf())

    fun refreshLocalWallet() {
        viewModelScope.launch() {
            val ethWallet = AppDatabase.getDatabase().walletDao().getWalletsByChain("ETH")

            val list = mutableListOf<TakerSowingUser>()
            ethWallet.forEach { localInfo ->
                val dbUser = AppDatabase.getDatabase().takerSowingDao()
                    .getAccountByAddress(localInfo.address.lowercase())
                if (dbUser != null) {
                    list.add(dbUser.apply {
                        wallet = localInfo
                    })
                } else {
                    list.add(TakerSowingUser(localInfo).apply {
                        wallet = localInfo
                    })
                }
            }
            walletAccountEvent.postValue(list)
        }
    }

    override fun refreshPanelAccountInfo(data: Any, online: Boolean) {
        super.refreshPanelAccountInfo(data, online)
        if (data is TakerSowingUser) {
            postPanelAccount(data)
            if (!online) return
            requestDetail(data)
        }
    }

    private fun postPanelAccount(data: TakerSowingUser) {
        panelAccountInfo.postValue(mutableListOf<Pair<String, String>>().apply {
            val jsonString = GsonUtils.toJson(data)
            val json = JSONObject(jsonString)
            add(Pair("地址", data.wallet?.address?.formatAddress().toString()))
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
                add(Pair(it, json.opt(it).toString()))
            }
        })
    }

    private fun requestDetail(user: TakerSowingUser) {
        scopeNetLife(Dispatchers.IO) {
            runCatching {
                apiUserInfo(user)
            }.onFailure {
                sendLog(LogData(ProjectConfig.PROJECT_ID_TAKERPROTOCOL_SOWING, LogData.Level.ERROR,user.walletAddress.formatAddress(),"获取用户信息异常 ${it.message}"))
            }
        }
    }

    override fun startTask(panelTask: List<IPanelTaskModule.PanelTask>) {
        super.startTask(panelTask)

        val accountList : List<TakerSowingUser> = if (globalMode.value == true) {
            walletAccountEvent.value
        } else {
            arrayListOf<TakerSowingUser>(panelCurrentAccountInfo.value as TakerSowingUser)
        } ?: return

        scopeNetLife(Dispatchers.IO) {
            accountList.forEachIndexed {index, account ->
                runCatching {
                    if (!account.isLogin()) {
                        account.token = apiLogin(account)
                        account.lastSyncTime = System.currentTimeMillis()
                    }
                    val userTask = apiTaskList(account)
                    panelTask.filter { task ->
                        userTask.find {
                            task.taskName == it.name && it.taskStatus == 2
                        } == null
                    }.apply {
                        if (randomMode.value == true) {
                            shuffled()
                        }
                        sendLog(LogData(projectId = ProjectConfig.PROJECT_ID_TAKERPROTOCOL_SOWING, LogData.Level.NORMAL,account.walletAddress.formatAddress(),"开始任务:${this.map { it.taskName }}"))
                        delay(1500)
                    }.forEachIndexed { index,task ->
                        sendLog(LogData(projectId = ProjectConfig.PROJECT_ID_TAKERPROTOCOL_SOWING, LogData.Level.NORMAL,account.walletAddress.formatAddress(),"开始-- ${task.taskName}"))
                        when(task.taskName) {
                            "每日签到" -> {
                                apiSign(account)
                            }
                            "BTC Basics Q&A" -> {
                                apiQuestionsTask(account,6)
                            }
                        }
                        val delayTime = Random.nextLong(5000, 15000)
                        sendLog(LogData(projectId = ProjectConfig.PROJECT_ID_TAKERPROTOCOL_SOWING, LogData.Level.NORMAL,account.walletAddress.formatAddress(),"${task.taskName}完成，等待${delayTime/1000}秒"))
                        delay(delayTime)
                    }
                    if (index < accountList.size-1) {
                        val delayTime = Random.nextLong(5000, 21000)
                        sendLog(LogData(projectId = ProjectConfig.PROJECT_ID_TAKERPROTOCOL_SOWING, LogData.Level.NORMAL,account.walletAddress.formatAddress(),"全部任务完成，${index+1}/${accountList.size}，等待${delayTime/1000}秒"))
                        delay(delayTime)
                    }
                }.onFailure {
                    sendLog(LogData(projectId = ProjectConfig.PROJECT_ID_TAKERPROTOCOL_SOWING, LogData.Level.ERROR,account.walletAddress.formatAddress(),"异常 ${it.message}"))
                }

            }

        }

    }

    private suspend fun apiTaskList(user: TakerSowingUser) : List<TaskSowingTaskResult> {
        return Net.get("https://sowing-api.taker.xyz/task/list?walletAddress=${user.wallet?.address}", block = {
            setClient {
                setProxy(user.wallet?.proxy)
            }
            setHeaders(TakerModel.Companion.getHeader(user.token, "https://sowing.taker.xyz/"))
            converter = TakerSowingConverter()
        }).toResult<List<TaskSowingTaskResult>>().getOrNull() ?: arrayListOf()
    }

    private suspend fun apiUserInfo(user: TakerSowingUser) : TakerSowingUser? {
        if (!user.isLogin()) {
            user.token = apiLogin(user)
            if (user.token?.isNullOrBlank() == true) {
                return null
            }
        }

        val userResult = Net.get("https://sowing-api.taker.xyz/user/info", block = {
            setClient {
                setProxy(user.wallet?.proxy)
            }
            setHeaders(getHeader(user.token, "https://sowing.taker.xyz/"))
            converter = TakerSowingConverter()
        }).toResult<TakerSowingUser>().getOrNull()

        if (userResult != null) {
            val newUser = user.getNewUser(userResult)
            AppDatabase.getDatabase().takerSowingDao().insertOrUpdate(newUser)
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
            sendLog(LogData(ProjectConfig.PROJECT_ID_TAKERPROTOCOL_SOWING, LogData.Level.SUCCESS,user.walletAddress.formatAddress(),"获取用户信息成功"))
            return newUser
        } else {
            sendLog(LogData(ProjectConfig.PROJECT_ID_TAKERPROTOCOL_SOWING, LogData.Level.ERROR,user.walletAddress.formatAddress(),"获取用户信息失败"))
            return null
        }
    }

    private suspend fun apiLogin(user: TakerSowingUser): String? {

        var message = ""
        Net.post("https://sowing-api.taker.xyz/wallet/generateNonce", block = {
            json(
                "walletAddress" to user.wallet?.address
            )
            setClient {
                setProxy(user.wallet?.proxy)
            }
            setHeaders(getHeader(null, "https://sowing.taker.xyz/"))
            converter = TakerSowingConverter()
        }).toResult<TakerSowingNonceResult>().let {
            if (it.isSuccess) {
                message = it.getOrNull()?.nonce ?:""
            }
        }
        if (message.isBlank()) return null

        val loginResult = Net.post("https://sowing-api.taker.xyz/wallet/login", block = {
            val signature = Web3Utils.signPrefixedMessage(message, user.wallet?.privateKey)
            json(
                "address" to user.wallet?.address,
                "message" to message,
                "signature" to signature,
                "invitationCode" to "181GVE62"
            )
            setClient {
                setProxy(user.wallet?.proxy)
            }
            setHeaders(TakerModel.Companion.getHeader(null, "https://sowing.taker.xyz/"))
            converter = TakerSowingConverter()
        }).toResult<TakerSowingLoginResult>()

        if (loginResult.isFailure) {
            sendLog(LogData(ProjectConfig.PROJECT_ID_TAKERPROTOCOL_SOWING, LogData.Level.ERROR,user.walletAddress.formatAddress(),"登录失败，获取token失败 ${loginResult.exceptionOrNull()?.message}"))
            return null
        }
        val token = loginResult.getOrNull()?.token

        user.token = token
        AppDatabase.getDatabase().takerSowingDao().insertOrUpdate(user)
        sendLog(LogData(ProjectConfig.PROJECT_ID_TAKERPROTOCOL_SOWING, LogData.Level.SUCCESS,user.walletAddress.formatAddress(),"登录成功"))
        return token
    }

    private suspend fun apiSign(user: TakerSowingUser) {
        val verifyToken = apiVerifyResp("https://sowing.taker.xyz/")
        if (verifyToken.isBlank()) {
            return
        }
        Net.get("https://sowing-api.taker.xyz/task/signIn?status=true", block = {
            setClient {
                setProxy(user.wallet?.proxy)
            }
            setHeaders(getHeader(user.token, "https://sowing.taker.xyz/",verifyToken))
        }).toResult<String>().apply {
            JSONObject(getOrNull()).apply {
                if (optInt("code") == 200) {
                    sendLog(LogData(ProjectConfig.PROJECT_ID_TAKERPROTOCOL_SOWING, LogData.Level.SUCCESS,user.walletAddress.formatAddress(),"签到成功"))
                } else {
                    sendLog(LogData(ProjectConfig.PROJECT_ID_TAKERPROTOCOL_SOWING, LogData.Level.SUCCESS,user.walletAddress.formatAddress(),"签到失败"))
                }
            }
        }
    }

    private suspend fun apiQuestionsTask(user: TakerSowingUser,taskId:Int) {
        val checkAnswer: (Int, Array<String>) -> Result<Boolean> = { taskEventId, answerList ->
            Net.post("https://sowing-api.taker.xyz/task/check", block = {
                json(
                    "taskId" to taskId,
                    "taskEventId" to taskEventId,
                    "answerList" to answerList
                )
                setClient {
                    setProxy(user.wallet?.proxy)
                }
                setHeaders(getHeader(user.token, "https://sowing.taker.xyz/detail/$taskId"))
                converter = TakerSowingConverter()
            }).toResult<Boolean>()
        }

        checkAnswer.invoke(1,arrayOf("C"))
        delay(Random.nextLong(1500,3000))
        checkAnswer.invoke(2,arrayOf("A"))
        delay(Random.nextLong(1500,3000))
        checkAnswer.invoke(3,arrayOf("D"))
        delay(Random.nextLong(1500,3000))

        val verify = apiVerifyResp("https://sowing.taker.xyz/detail/$taskId")
        Net.post("https://sowing-api.taker.xyz/task/claim-reward?taskId=$taskId", block = {
            setClient {
                setProxy(user.wallet?.proxy)
            }
            setHeaders(getHeader(user.token, "https://sowing.taker.xyz/detail/$taskId",verify))
        }).toResult<String>()
    }

    private suspend fun apiVerifyResp(pageUrl: String) : String {

        val result = Net.post("https://2captcha.com/in.php", block = {
            json(
                "key" to "4bbaf9020e107f5c89ae4c28e12bbc24",
                "method" to "turnstile",
                "sitekey" to "0x4AAAAAABNqF8H4KF9TDs2O",
                "pageurl" to pageUrl,
                "json" to 1
            )
        }).toResult<String>()
        var request = JSONObject(result.getOrNull()).optString("request")
        var verifyResp = ""
        for(index in 1..3) {
            sendLog(LogData(projectId = ProjectConfig.PROJECT_ID_TAKERPROTOCOL_SOWING, LogData.Level.NORMAL,"","识别验证码中...$index/3"))
            delay(10000)
            val string = Net.get("https://2captcha.com/res.php?key=4bbaf9020e107f5c89ae4c28e12bbc24&action=get&id=$request&json=1").toResult<String>()
            val json = JSONObject(string.getOrNull())
            if (json.optInt("status") == 1) {
                verifyResp = json.optString("request")
                break
            }
        }
        if (verifyResp.isBlank()) {
            sendLog(LogData(projectId = ProjectConfig.PROJECT_ID_TAKERPROTOCOL_SOWING, LogData.Level.ERROR,"","识别验证码失败"))
        }
        return verifyResp
    }

}