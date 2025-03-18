package com.web3.airdrop.extension

import com.blankj.utilcode.util.LogUtils
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.web3.airdrop.data.Wallet
import com.web3.airdrop.data.Wallet.Companion.getCredential
import okhttp3.Credentials
import okhttp3.OkHttpClient
import org.json.JSONArray
import org.json.JSONObject
import java.net.InetSocketAddress
import java.net.Proxy
import java.security.MessageDigest

object Extension {

    fun sha256(input: ByteArray): ByteArray {
        val digest = MessageDigest.getInstance("SHA-256")
        return digest.digest(input)
    }

    fun String.formatAddress(): String {
        // 检查字符串长度是否满足要求
        return if (this.length >= 12) {
            // 截取前6和后6个字符
            val prefix = this.substring(0, 6)
            val suffix = this.substring(this.length - 6)
            "$prefix...$suffix" // 用省略号连接
        } else {
            this // 如果地址长度小于12，返回原始字符串
        }
    }

}

fun OkHttpClient.Builder.setProxy(proxyContent: String?) {
    if (proxyContent == null)return
    val account = getCredential(proxyContent, Wallet.PROXY_ACCOUNT)?:""
    val password = getCredential(proxyContent, Wallet.PROXY_PASSWORD)?:""
    val ip = getCredential(proxyContent, Wallet.PROXY_IP) ?:""
    val port = getCredential(proxyContent, Wallet.PROXY_PORT)?.toInt() ?:0
    LogUtils.d("account = $account,password = $password, ip = $ip,port = $port")
    proxy(Proxy(Proxy.Type.HTTP, InetSocketAddress(ip, port)))
    proxyAuthenticator { _, response ->
        response.request.newBuilder()
            .header("Proxy-Authorization", Credentials.basic(account, password))
            .build()
    }
}

fun String.formatJson(): String {
    return try {
        // 创建一个 GsonBuilder 实例，并设置为格式化模式
        val gson: Gson = GsonBuilder().setPrettyPrinting().create()

        // 将 JSON 字符串解析为对象并再转换为格式化的字符串
        val jsonElement = gson.fromJson(this, Any::class.java)
        gson.toJson(jsonElement)
    } catch (e: Exception) {
        "Invalid JSON format: ${e.message}"
    }
}

