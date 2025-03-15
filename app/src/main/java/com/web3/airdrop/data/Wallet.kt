package com.web3.airdrop.data

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "Wallet",indices = [Index(value = ["address"], unique = true)])
data class Wallet(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val chain: String,
    val address: String,
    val privateKey: String,
    val alias: String,
    val proxy: String
) {

    @Ignore
    var check: Boolean = false

    companion object {

        const val PROXY_ACCOUNT = "account"
        const val PROXY_PASSWORD = "password"
        const val PROXY_IP = "ip"
        const val PROXY_PORT = "port"

        fun parseCredentials(credentials: String): Map<String, String>? {
            // 使用正则表达式来解析字符串
            val regex = """([^:]+):([^@]+)@([^:]+):(\d+)""".toRegex()
            val matchResult = regex.find(credentials)

            return matchResult?.let {
                mapOf(
                    PROXY_ACCOUNT to (it.groups[1]?.value ?: ""),
                    PROXY_PASSWORD to (it.groups[2]?.value ?: ""),
                    PROXY_IP to (it.groups[3]?.value ?: ""),
                    PROXY_PORT to (it.groups[4]?.value ?: "")
                )
            }
        }

        fun getCredential(credentials: String, key: String): String? {
            val parsedCredentials = parseCredentials(credentials)
            return parsedCredentials?.get(key)
        }
    }

}