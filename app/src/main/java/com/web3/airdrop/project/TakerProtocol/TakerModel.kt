package com.web3.airdrop.project.TakerProtocol

import androidx.core.util.Pools
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
import com.web3.airdrop.project.TakerProtocol.data.TakerGenerateNonceResult
import com.web3.airdrop.project.TakerProtocol.data.TakerLoginResult
import com.web3.airdrop.project.TakerProtocol.data.TakerUser
import com.web3.airdrop.project.TakerProtocol.data.TaskStateResult
import com.web3.airdrop.project.log.LogData
import com.web3.airdrop.project.takersowing.TakerSowingConverter
import com.web3.airdrop.project.takersowing.TakerSowingModel
import com.web3.airdrop.project.takersowing.data.TakerSowingUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.Headers
import org.json.JSONObject
import kotlin.random.Random

import org.web3j.protocol.Web3j
import org.web3j.protocol.http.HttpService
import org.web3j.crypto.Credentials
import org.web3j.tx.RawTransactionManager
import org.web3j.tx.gas.DefaultGasProvider
import org.web3j.tx.TransactionManager
import org.web3j.tx.response.PollingTransactionReceiptProcessor
import org.web3j.abi.FunctionEncoder
import org.web3j.abi.datatypes.Function
import org.web3j.abi.datatypes.Type
import org.web3j.abi.datatypes.Utf8String
import org.web3j.crypto.RawTransaction
import org.web3j.crypto.TransactionEncoder
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.protocol.core.methods.response.EthGetTransactionReceipt
import org.web3j.protocol.core.methods.response.EthSendTransaction
import org.web3j.protocol.core.methods.response.TransactionReceipt
import org.web3j.utils.Numeric
import java.math.BigInteger

class TakerModel : BaseModel<TakerUser>() {

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

    override suspend fun getAccountByAddress(address: String): TakerUser? {
        return AppDatabase.getDatabase().takerDao().getAccountByAddress(address)
    }

    private suspend fun apiLogin(user: TakerUser): String? {

        var message = ""
        Net.post("https://lightmining-api.taker.xyz/wallet/generateNonce", block = {
            json(
                "walletAddress" to user.localWallet?.address
            )
            setClient {
                setProxy(user.localWallet?.proxy)
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
            val signature = Web3Utils.signPrefixedMessage(message, user.localWallet?.privateKey)
            json(
                "address" to user.localWallet?.address,
                "message" to message,
                "signature" to signature,
                "invitationCode" to if (user.walletAddress.endsWith("2276f1")) "" else "8YPC6"
            )
            setClient {
                setProxy(user.localWallet?.proxy)
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
        //第一次登录
        if (user.token.isNullOrBlank()) {
            user.token = apiLogin(user)
        }

        val userRequest: (String?) -> TakerUser? = { token ->
            Net.get("https://lightmining-api.taker.xyz/user/getUserInfo", block = {
                setClient {
                    setProxy(user.localWallet?.proxy)
                }
                setHeaders(getHeader(token, "https://earn.taker.xyz/"))
                converter = TakerConverter()
            }).toResult<TakerUser>().getOrNull()
        }

        //接口报错重新登录
        var newUser = user.getNewUser(userRequest.invoke(user.token))
        if (newUser == null) {
            user.token = apiLogin(user)
            newUser = user.getNewUser(userRequest.invoke(user.token))
        }

        Net.get("https://lightmining-api.taker.xyz/assignment/totalMiningTime", block = {
            setClient {
                setProxy(user.localWallet?.proxy)
            }
            setHeaders(getHeader(newUser?.token, "https://earn.taker.xyz/"))
            converter = TakerConverter()
        }).toResult<String>().let {
            JSONObject(it.getOrNull()).let { json ->
                val lastMiningTime = json.optJSONObject("data")?.optLong("lastMiningTime")
                val totalMiningTime = json.optJSONObject("data")?.optLong("totalMiningTime")
                newUser?.lastMiningTime = lastMiningTime ?:0L
                newUser?.totalMiningTime = totalMiningTime ?:0L
            }
        }

        val checkResult : (TakerUser?) -> TakerUser? = { newUser ->
            if (newUser != null) {
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
                newUser
            } else {
                null
            }
        }
        return checkResult.invoke(newUser)
    }

    override fun requestDetail(user: TakerUser) {
        scopeNetLife(Dispatchers.IO) {
            runCatching {
                apiUserInfo(user)
            }.onFailure {
                sendLog(LogData(ProjectConfig.PROJECT_ID_TAKERPROTOCOL, LogData.Level.ERROR,user.walletAddress.formatAddress(),"获取用户信息异常 ${it.message}"))
            }
        }
    }

    override suspend fun doTask(accountList:List<TakerUser>, panelTask: List<IPanelTaskModule.PanelTask>) {
        accountList.shuffled().forEachIndexed {index, account ->
            if (taskStart.value == false) {
                return@forEachIndexed
            }
            runCatching {
                sendLog(LogData(projectId = ProjectConfig.PROJECT_ID_TAKERPROTOCOL, LogData.Level.NORMAL,account.walletAddress.formatAddress(),"开始"))
                val user = apiUserInfo(account)
                if (user == null)return@runCatching
                val userTask = taskStateList(user) ?:return@runCatching
                val taskList = panelTask.shuffled().filter { task ->
                    userTask.find {
                        task.taskName == it.title && it.done
                    } == null
                }
                taskList.forEachIndexed { index,task ->
                    sendLog(LogData(projectId = ProjectConfig.PROJECT_ID_TAKERPROTOCOL, LogData.Level.NORMAL,account.walletAddress.formatAddress(),"开始-- ${task.taskName}"))
                    when(task.taskName) {
                        "每日签到" -> {
                            apiSign(user)
                        }
                        "Like a Tweet" -> {
                            dailyTasks(13,task.taskName,user)
                        }
                        "Retweet a Tweet" -> {
                            dailyTasks(14,task.taskName,user)
                        }
                        "Comment on a Tweet" -> {
                            dailyTasks(15,task.taskName,user)
                        }
                        "Follow Taker X" -> {
                            dailyTasks(1,task.taskName,user)
                        }
                        "Share a Tweet About Lite-Mining Campaign" -> {
                            dailyTasks(5,task.taskName,user)
                        }
                    }
                    val isLastTask = index == taskList.size -1
                    if (!isLastTask && taskList.size>1) {
                        val delayTime = Random.nextLong(2000, 5000)
                        delay(delayTime)
                    }
                }
                sendLog(LogData(projectId = ProjectConfig.PROJECT_ID_TAKERPROTOCOL, LogData.Level.NORMAL,account.walletAddress.formatAddress(),"全部任务完成，${index+1}/${accountList.size}"))
            }.onFailure {
                sendLog(LogData(projectId = ProjectConfig.PROJECT_ID_TAKERPROTOCOL, LogData.Level.ERROR,account.walletAddress.formatAddress(),"异常 ${it.message}"))
                it.printStackTrace()
            }
        }

    }

    private suspend fun apiSign(user: TakerUser) {
        var hash: String? = ""
        if ((System.currentTimeMillis() - user.lastMiningTime*1000) < 1000*60*60*24) {
            sendLog(LogData(projectId = ProjectConfig.PROJECT_ID_TAKERPROTOCOL, LogData.Level.WARN,user.walletAddress.formatAddress(),"上次签到未超过24小时"))
            return
        } else {
            LogUtils.d(user.lastMiningTime)
            if (user.lastMiningTime > 0L) {
                hash = chainActive(user)
                if (hash.isNullOrEmpty() == true) {
                    sendLog(LogData(projectId = ProjectConfig.PROJECT_ID_TAKERPROTOCOL, LogData.Level.ERROR,user.walletAddress.formatAddress(),"链上active 失败"))
                    return
                } else {
                    sendLog(LogData(projectId = ProjectConfig.PROJECT_ID_TAKERPROTOCOL, LogData.Level.SUCCESS,user.walletAddress.formatAddress(),"链上active 成功 $hash"))
                }
            }
        }

        Net.post("https://lightmining-api.taker.xyz/assignment/startMining", block = {
            json(
                "status" to hash.isNullOrEmpty(),
                //                "verifyResp" to ""
            )
            setClient {
                setProxy(user.localWallet?.proxy)
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
                setProxy(user.localWallet?.proxy)
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

    private suspend fun taskStateList(user: TakerUser) : List<TaskStateResult>? {
        return Net.post("https://lightmining-api.taker.xyz/assignment/list", block = {
            setClient {
                setProxy(user.localWallet?.proxy)
            }
            setHeaders(getHeader(user.token, "https://earn.taker.xyz/"))
            converter = TakerConverter()
        }).toResult<List<TaskStateResult>>().getOrNull()
    }

    private suspend fun chainActive(user: TakerUser): String? {
        // 连接到节点
        val nodeUrl = "https://rpc-mainnet.taker.xyz/" // 替换为您的节点地址
        val web3j = Web3j.build(HttpService(nodeUrl))
        // 你的私钥
        val privateKey = user.localWallet?.privateKey // 替换为您的私钥
        val credentials = Credentials.create(privateKey)
        val fromAddress = credentials.address
        // 合约地址
        val contractAddress = "0xB3eFE5105b835E5Dd9D206445Dbd66DF24b912AB"

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
            }
            if (System.currentTimeMillis() - startTime > timeoutMillis) {
                // 超时
                return null
            }
            delay(pollIntervalSeconds * 1000) // 暂停等待
        }
    }

}