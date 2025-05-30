package com.web3.airdrop.project.chainopera

import androidx.lifecycle.scopeNetLife
import com.blankj.utilcode.util.TimeUtils
import com.drake.net.Net
import com.web3.airdrop.base.BaseModel
import com.web3.airdrop.data.AppDatabase
import com.web3.airdrop.data.ProjectConfig
import com.web3.airdrop.extension.Extension.formatAddress
import com.web3.airdrop.extension.Web3Utils
import com.web3.airdrop.extension.setProxy
import com.web3.airdrop.project.chainopera.bean.SIWEMessageResult
import com.web3.airdrop.project.log.LogData
import com.web3.airdrop.project.takersowing.TakerSowingConverter
import com.web3.airdrop.project.takersowing.TakerSowingModel
import com.web3.airdrop.project.takersowing.data.TakerSowingUser
import kotlinx.coroutines.Dispatchers
import okhttp3.Headers

class ChainOperaModel : BaseModel<ChainOperaUser>() {

    companion object {
        fun getHeader(token: String?, originUrl: String, requestUrl: String): Headers {
            return Headers.Builder().apply {
                add("Accept-Language", "en-US,en;q=0.9")
                add("hearder_gray_set", "0")
                add("Origin", originUrl)
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
//                if (turnstile?.isNotEmpty() == true) {
//                    add("Cf-Turnstile-Token", turnstile)
//                }
            }.build()
        }
    }

    override suspend fun getAccountByAddress(address: String): ChainOperaUser? {
        return null
    }

    override fun requestDetail(user: ChainOperaUser) {
        scopeNetLife(Dispatchers.IO) {
            runCatching {
                apiUserInfo(user)
            }.onFailure {
                sendLog(LogData(ProjectConfig.PROJECT_ID_CHAINOPERA_AI, LogData.Level.ERROR,user.localWallet?.address?.formatAddress(),"获取用户信息异常 ${it.message}"))
            }
        }
    }

    private fun apiUserInfo(user: ChainOperaUser) {
        //第一次登录
        if (!user.isRegister()) {
            user.token = apiLogin(user)
        }
    }

    private fun apiLogin(user: ChainOperaUser) : String{
        val siweMessageResult = Net.post("https://chainopera.ai/userCenter/api/v1/wallet/getSIWEMessage") {
            setClient {
                setProxy(user.localWallet?.proxy)
            }
            setHeaders(getHeader(token = null, originUrl = "https://chainopera.ai", requestUrl = "https://chainopera.ai/quest/?inviteCode=8JKLQXO3"))
            converter = TakerSowingConverter()
        }.toResult<SIWEMessageResult>()
        val nonce = siweMessageResult.getOrNull()?.nonce ?:return ""

        val message = "https://chainopera.ai wants you to sign in with your Ethereum account:\\n${user.localWallet?.address}\\n\\nSign in with Ethereum\\n\\nURI: https://chainopera.ai\\nVersion: 1\\nChain ID: 1\\nNonce: ${nonce}\\nIssued At: ${TimeUtils.getNowString()}"
        val signature = Web3Utils.signPrefixedMessage(message, user.localWallet?.privateKey)
        Net.post("https://chainopera.ai/userCenter/api/v1/wallet/login") {
            setClient {
                setProxy(user.localWallet?.proxy)
            }
            json(
                "address" to user.localWallet?.address,
                "signature" to signature,
                "messageToSign" to message,
            )
            setHeaders(getHeader(token = null, originUrl = "https://chainopera.ai", requestUrl = "https://chainopera.ai/quest/?inviteCode=8JKLQXO3"))
            converter = TakerSowingConverter()
        }.toResult<Any>()
        return ""
    }

}