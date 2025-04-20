package com.web3.airdrop.project.takersowing.data

data class TaskSowingTaskResult(
    val id: Int,
    val name: String,
    val description: String,
    val completionCount:Long,
    val module:Int,
    val rewardType:Int,
    val rewardPoints:Int,
    val nftImages: String,
    val nftName: String,
    val topStatus:Int,
    val publishStatus:Int,
    val hideStatus:Int,
    val orderNum:Int,
    val taskStatus:Int,
    val startTime: String,
    val endTime: String,
    val taskCycle:Int,
    val isPermanent:Int
)
