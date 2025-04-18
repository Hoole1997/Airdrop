package com.web3.airdrop.project.TakerProtocol.data

import java.util.Date

data class TaskStateResult(
    val assignmentId: Int,
    val title: String,
    val describe: String,
    val url: String,
    val done: Boolean,
    val assignmentType: String,
    val reward: String,
    val project: String,
    val logo: String,
    val complete: String?,
    val top: Boolean,
    val timestamp: Any?,
    val completeTime: String,
    val sort: Int,
    val cfVerify: Boolean
)

