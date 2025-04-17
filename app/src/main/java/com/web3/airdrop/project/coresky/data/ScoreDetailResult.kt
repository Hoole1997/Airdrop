package com.web3.airdrop.project.coresky.data

data class ScoreDetailResult(
    val score: Double,
    val detail: Detail
) {

    data class Detail(
        val curPage: Double,
        val listData: List<ListData>,
        val pageSize: Double,
        val listCount: Double,
        val maxPage: Double
    )

    data class ListData(
        val type: Int,
        val typeStr: String,
        val asset: String,
        val score: String,
        val itemName: String,
        val sourceType: String,
        val chain: String,
        val createTime: Long
    )

}