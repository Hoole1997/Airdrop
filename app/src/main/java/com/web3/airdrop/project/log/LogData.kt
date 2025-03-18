package com.web3.airdrop.project.log


data class LogData(
    val projectId: Int,
    val level: Level,
    val accountId: Int? = 0,
    val content: String
) {
    enum class Level {
        ERROR,
        NORMAL,
        WARN,
        SUCCESS
    }
}
