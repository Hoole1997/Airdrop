package com.web3.airdrop.project.coresky.data

data class SignResult(
    val infos: List<Info>,
    val isSign: Int,
    val signDay: Int,
    val task: Task
)

data class Info(
    val createTime: Any,
    val dayName: String,
    val dayNo: Int,
    val deleted: Any,
    val id: Int,
    val reward: Int,
    val rewardType: Int,
    val signType: Any,
    val updateTime: Any
)

data class Task(
    val createTime: Any,
    val deleted: Any,
    val description: Any,
    val groupType: Any,
    val id: Int,
    val link: Any,
    val projectId: Int,
    val rewardPoint: Int,
    val showFlag: Any,
    val sortNum: Any,
    val targetId: Any,
    val taskIcon: Any,
    val taskName: Any,
    val taskTagId: Int,
    val updateTime: Any,
    val uuid: String
)