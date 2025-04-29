package com.web3.airdrop.project.takersowing

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.scopeNetLife
import androidx.lifecycle.viewModelScope
import com.blankj.utilcode.util.GsonUtils
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.TimeUtils
import com.drake.net.Net
import com.web3.airdrop.base.BaseModel
import com.web3.airdrop.base.IPanelTaskModule
import com.web3.airdrop.data.AppDatabase
import com.web3.airdrop.data.ProjectConfig
import com.web3.airdrop.extension.Extension.formatAddress
import com.web3.airdrop.extension.Web3Utils
import com.web3.airdrop.extension.setProxy
import com.web3.airdrop.project.TakerProtocol.TakerModel
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
import org.web3j.abi.FunctionEncoder
import org.web3j.abi.datatypes.Function
import org.web3j.crypto.Credentials
import org.web3j.crypto.RawTransaction
import org.web3j.crypto.TransactionEncoder
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.protocol.core.methods.response.EthGetTransactionReceipt
import org.web3j.protocol.core.methods.response.EthSendTransaction
import org.web3j.protocol.core.methods.response.TransactionReceipt
import org.web3j.protocol.http.HttpService
import org.web3j.utils.Numeric
import java.math.BigInteger
import kotlin.random.Random

class TakerSowingModel : BaseModel<TakerSowingUser>() {

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

    override suspend fun getAccountByAddress(address: String): TakerSowingUser? {
        return AppDatabase.getDatabase().takerSowingDao().getAccountByAddress(address.lowercase())
    }

    override fun requestDetail(user: TakerSowingUser) {
        scopeNetLife(Dispatchers.IO) {
            runCatching {
                apiUserInfo(user)
            }.onFailure {
                sendLog(LogData(ProjectConfig.PROJECT_ID_TAKERPROTOCOL_SOWING, LogData.Level.ERROR,user.walletAddress.formatAddress(),"获取用户信息异常 ${it.message}"))
            }
        }
    }

    override suspend fun doTask(accountList:List<TakerSowingUser>, panelTask: List<IPanelTaskModule.PanelTask>) {
        LogUtils.d("doTask")
        accountList.forEachIndexed {index, account ->
            if (taskStart.value == false) {
                return@forEachIndexed
            }
            runCatching {
                val user = apiUserInfo(account)
                val userTask = apiTaskList(user) ?:return@runCatching

                val taskList = panelTask.shuffled().filter { task ->
                    userTask.find {
                        task.taskName == it.name && (it.taskStatus == 2 || System.currentTimeMillis() >= TimeUtils.string2Millis(it.endTime))
                    } == null
                }.apply {
                    sendLog(LogData(projectId = ProjectConfig.PROJECT_ID_TAKERPROTOCOL_SOWING, LogData.Level.NORMAL,account.walletAddress.formatAddress(),"开始任务:${this.map { it.taskName }}"))
                }
                taskList.forEachIndexed { index,task ->
                    sendLog(LogData(projectId = ProjectConfig.PROJECT_ID_TAKERPROTOCOL_SOWING, LogData.Level.NORMAL,account.walletAddress.formatAddress(),"开始-- ${task.taskName}"))
                    when(task.taskName) {
                        "每日签到" -> {
                            apiSign(user)
                        }
                        "BTC Basics Q&A" -> {
                            apiQuestionsTask6(user,6)
                        }
                    }
                    val isLastTask = index == taskList.size -1
                    if (!isLastTask && taskList.size>1) {
                        val delayTime = Random.nextLong(2000, 5000)
                        delay(delayTime)
                    }
                }
                sendLog(LogData(projectId = ProjectConfig.PROJECT_ID_TAKERPROTOCOL_SOWING, LogData.Level.NORMAL,account.walletAddress.formatAddress(),"全部任务完成，${index+1}/${accountList.size}"))
            }.onFailure {
                sendLog(LogData(projectId = ProjectConfig.PROJECT_ID_TAKERPROTOCOL_SOWING, LogData.Level.ERROR,account.walletAddress.formatAddress(),"异常 ${it.message}"))
                it.printStackTrace()
            }

        }
    }

    private suspend fun apiTaskList(user: TakerSowingUser) : List<TaskSowingTaskResult>? {
        return Net.get("https://sowing-api.taker.xyz/task/list?walletAddress=${user.localWallet?.address}", block = {
            setClient {
                setProxy(user.localWallet?.proxy)
            }
            setHeaders(TakerModel.Companion.getHeader(user.token, "https://sowing.taker.xyz/"))
            converter = TakerSowingConverter()
        }).toResult<List<TaskSowingTaskResult>>().getOrNull()
    }

    private suspend fun apiUserInfo(user: TakerSowingUser) : TakerSowingUser {
        //第一次登录
        if (!user.isRegister()) {
            user.token = apiLogin(user)
        }

        val userRequest: (String?) -> TakerSowingUser? = { token ->
            Net.get("https://sowing-api.taker.xyz/user/info", block = {
                setClient {
                    setProxy(user.localWallet?.proxy)
                }
                setHeaders(getHeader(token, "https://sowing.taker.xyz/"))
                converter = TakerSowingConverter()
            }).toResult<TakerSowingUser>().getOrNull()
        }

        //接口报错重新登录
        var result = userRequest.invoke(user.token)
        if (result == null) {
            user.token = apiLogin(user)
            result = userRequest.invoke(user.token)
        }

        val checkResult : (TakerSowingUser?) -> TakerSowingUser = { userResult ->
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
                newUser.canSign = kotlin.math.abs(System.currentTimeMillis() - newUser.nextTimestamp) <= 20_000
                newUser
            } else {
                user
            }
        }

        return checkResult.invoke(result)
    }

    private suspend fun apiLogin(user: TakerSowingUser): String? {

        var message = ""
        Net.post("https://sowing-api.taker.xyz/wallet/generateNonce", block = {
            json(
                "walletAddress" to user.localWallet?.address
            )
            setClient {
                setProxy(user.localWallet?.proxy)
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
            val signature = Web3Utils.signPrefixedMessage(message, user.localWallet?.privateKey)
            json(
                "address" to user.localWallet?.address,
                "message" to message,
                "signature" to signature,
                "invitationCode" to "181GVE62"
            )
            setClient {
                setProxy(user.localWallet?.proxy)
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
        if (!user.canSign) {
            sendLog(LogData(projectId = ProjectConfig.PROJECT_ID_TAKERPROTOCOL, LogData.Level.WARN,user.walletAddress.formatAddress(),"未到签到时间 下次签到时间：${TimeUtils.millis2String(user.nextTimestamp)}"))
            return
        }
        val verifyToken = apiVerifyResp("https://sowing.taker.xyz/")
        if (verifyToken.isBlank()) {
            return
        }
        var hash: String? = ""
        if (!user.firstSign) {
            hash = chainActive(user)
            if (hash.isNullOrEmpty() == true) {
                sendLog(LogData(projectId = ProjectConfig.PROJECT_ID_TAKERPROTOCOL, LogData.Level.ERROR,user.walletAddress.formatAddress(),"链上active 失败"))
                return
            } else {
                sendLog(LogData(projectId = ProjectConfig.PROJECT_ID_TAKERPROTOCOL, LogData.Level.SUCCESS,user.walletAddress.formatAddress(),"链上active 成功 $hash"))
            }
        }
        Net.get("https://sowing-api.taker.xyz/task/signIn?status=${hash.isNullOrBlank()}", block = {
            setClient {
                setProxy(user.localWallet?.proxy)
            }
            setHeaders(getHeader(user.token, "https://sowing.taker.xyz/",verifyToken))
        }).toResult<String>().getOrNull()?.apply {
            JSONObject(this).apply {
//                if (optInt("code") == 200) {
//
//                } else {
//                    sendLog(LogData(ProjectConfig.PROJECT_ID_TAKERPROTOCOL_SOWING, LogData.Level.SUCCESS,user.walletAddress.formatAddress(),"签到失败"))
//                }
                sendLog(LogData(ProjectConfig.PROJECT_ID_TAKERPROTOCOL_SOWING, LogData.Level.SUCCESS,user.walletAddress.formatAddress(),"签到成功"))
            }
        }
    }

    private suspend fun apiQuestionsTask6(user: TakerSowingUser, taskId:Int) {
        val checkAnswer: (Int, Array<String>) -> Result<Boolean> = { taskEventId, answerList ->
            Net.post("https://sowing-api.taker.xyz/task/check", block = {
                json(
                    "taskId" to taskId,
                    "taskEventId" to taskEventId,
                    "answerList" to answerList
                )
                setClient {
                    setProxy(user.localWallet?.proxy)
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
                setProxy(user.localWallet?.proxy)
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

    private suspend fun chainActive(user: TakerSowingUser): String? {
        // 连接到节点
        val nodeUrl = "https://rpc-mainnet.taker.xyz/" // 替换为您的节点地址
        val web3j = Web3j.build(HttpService(nodeUrl))
        // 你的私钥
        val privateKey = user.localWallet?.privateKey // 替换为您的私钥
        val credentials = Credentials.create(privateKey)
        val fromAddress = credentials.address
        // 合约地址
        val contractAddress = "0xF929AB815E8BfB84Cdab8d1bb53F22eB1e455378"

        var hash: String? = null

        try {
            hash = sendTransactionAndWait(web3j,credentials,fromAddress,contractAddress)
        }catch (e: Exception) {
            //重试一次
            hash = sendTransactionAndWait(web3j,credentials,fromAddress,contractAddress)
        }

        return hash
    }

    suspend fun sendTransactionAndWait(
        web3j: Web3j,
        credentials: Credentials,
        fromAddress: String,
        contractAddress: String
    ) : String {
        // 构建调用函数
        val function = Function("active", emptyList(), emptyList())
        val dataEncoded = FunctionEncoder.encode(function)

        // 估算gas
        val estimateGas = web3j.ethEstimateGas(
            org.web3j.protocol.core.methods.request.Transaction.createFunctionCallTransaction(
                fromAddress,
                null,
                null,
                null,
                contractAddress,
                BigInteger.ZERO,
                dataEncoded
            )
        ).send()

        if (estimateGas.hasError()) {
            sendLog(LogData(ProjectConfig.PROJECT_ID_TAKERPROTOCOL, LogData.Level.WARN,"","Error estimating gas: ${estimateGas.error.message}"))
            return ""
        }

        val gasLimit = estimateGas.amountUsed

        // 获取 nonce
        val nonce = web3j.ethGetTransactionCount(fromAddress, DefaultBlockParameterName.PENDING)
            .send().transactionCount

        // 获取 gasPrice
        val gasPrice = web3j.ethGasPrice().send().gasPrice

        // 构造原始交易
        val rawTransaction = RawTransaction.createTransaction(
            nonce,
            gasPrice,
            gasLimit,
            contractAddress,
            BigInteger.ZERO,
            dataEncoded
        )

        // 签名交易
        val signedMessage = TransactionEncoder.signMessage(rawTransaction, 1125, credentials) // 1125为chainId
        val hexValue = Numeric.toHexString(signedMessage)

        // 发送交易
        val ethSendTransaction: EthSendTransaction = web3j.ethSendRawTransaction(hexValue).send()

        if (ethSendTransaction.hasError()) {
            sendLog(LogData(ProjectConfig.PROJECT_ID_TAKERPROTOCOL, LogData.Level.WARN,"","Error sending transaction: ${ethSendTransaction.error.message}"))
            return ""
        }

        val txHash = ethSendTransaction.transactionHash
        sendLog(LogData(ProjectConfig.PROJECT_ID_TAKERPROTOCOL, LogData.Level.NORMAL,"","Transaction hash: $txHash"))

        // 等待交易确认
        val receipt = waitForTransactionReceipt(web3j, txHash)
        if (receipt != null) {
            sendLog(LogData(ProjectConfig.PROJECT_ID_TAKERPROTOCOL, LogData.Level.SUCCESS,"","Transaction confirmed in block: ${receipt.blockNumber}"))
            return txHash
        } else {
            sendLog(LogData(ProjectConfig.PROJECT_ID_TAKERPROTOCOL, LogData.Level.ERROR,"","Transaction receipt not found."))
            return ""
        }
    }


    // 轮询等待交易回执（可以在协程中调用）
    suspend fun waitForTransactionReceipt(
        web3j: Web3j,
        transactionHash: String,
        timeoutSeconds: Long = 300,
        pollIntervalSeconds: Long = 5
    ): TransactionReceipt? {
        val startTime = System.currentTimeMillis()
        val timeoutMillis = timeoutSeconds * 1000

        while (true) {
            val receiptResponse: EthGetTransactionReceipt = web3j.ethGetTransactionReceipt(transactionHash).send()
            if (receiptResponse.transactionReceipt.isPresent) {
                return receiptResponse.transactionReceipt.get()
                break
            }
            if (System.currentTimeMillis() - startTime > timeoutMillis) {
                // 超时
                return null
                break
            }
            delay(pollIntervalSeconds * 1000) // 暂停等待
        }
    }

}