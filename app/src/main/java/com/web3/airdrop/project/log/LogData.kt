package com.web3.airdrop.project.log


data class LogData(
    val projectId: Int,
    val level: Level,
    val address: String? = "",
    val content: String,
    val time: Long = System.currentTimeMillis()
) {
    enum class Level {
        ERROR,
        NORMAL,
        WARN,
        SUCCESS
    }
}
