package com.web3.airdrop.extension

import com.blankj.utilcode.util.LogUtils
import com.web3.airdrop.data.Wallet
import com.web3.airdrop.data.Wallet.Companion.getCredential
import okhttp3.Credentials
import okhttp3.OkHttpClient
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

fun OkHttpClient.Builder.setProxy(proxyContent: String) {
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

