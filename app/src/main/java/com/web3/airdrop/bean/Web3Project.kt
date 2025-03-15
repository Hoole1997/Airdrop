package com.web3.airdrop.bean

import com.web3.airdrop.project.layeredge.LayerEdgeCommand

data class Web3Project(
    val projectId:Int,
    val name:String,
    val icon:Int,
    val twitter:String,
    val website:String
) {
    companion object {
        fun getProjectName(projectId: Int) : String{
            return when(projectId) {
                LayerEdgeCommand.LAYER_EDGE_PROJECT_ID -> {
                    "LayerEdge"
                }
                else -> {
                    ""
                }
            }
        }
    }
}
