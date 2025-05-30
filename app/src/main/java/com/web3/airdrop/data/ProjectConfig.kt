package com.web3.airdrop.data

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.web3.airdrop.R
import com.web3.airdrop.project.TakerProtocol.FragmentTakerProtocol
import com.web3.airdrop.project.bless.FragmentBless
import com.web3.airdrop.project.chainopera.FragmentChainOpera
import com.web3.airdrop.project.coresky.FragmentCoreSky
import com.web3.airdrop.project.layeredge.FragmentLayerEdge
import com.web3.airdrop.project.takersowing.FragmentTakerSowing
import java.io.Serializable

class ProjectConfig {

    companion object {

        const val PROJECT_ID_LAYEREDGE = 1
        const val PROJECT_ID_CORESKY = 2
        const val PROJECT_ID_TAKERPROTOCOL = 3
        const val PROJECT_ID_BLESS = 4
        const val PROJECT_ID_TAKERPROTOCOL_SOWING = 5
        const val PROJECT_ID_CHAINOPERA_AI = 6

        fun projectData(): List<ProjectInfo> {
            return mutableListOf<ProjectInfo>(
                ProjectInfo(
                    projectId = PROJECT_ID_LAYEREDGE,
                    name = "LayerEdge",
                    icon = R.mipmap.icon_layeredge,
                    twitterUrl = "https://x.com/layeredge",
                    website = "https://layeredge.io/",
                    describe = "LayerEdge | 🔥0撸项目，签到类\n" +
                            "印度阿三项目，听说融资千万美金，未披露。进度百分之80，等待TGE",
                    star = 3
                ),
                ProjectInfo(
                    projectId = PROJECT_ID_CORESKY,
                    name = "CoreSky",
                    icon = R.mipmap.icon_coresky,
                    twitterUrl = "https://x.com/Coreskyofficial",
                    website = "https://www.coresky.com/",
                    describe = "CoreSky | 🔥0撸项目，签到类\n" +
                            "国人项目，融资2000万，2025月5月3号TGE。签到，投票，抽奖",
                    star = 3
                ),
                ProjectInfo(
                    projectId = PROJECT_ID_TAKERPROTOCOL,
                    name = "TakerProtocol",
                    icon = R.mipmap.icon_takerprotocol,
                    twitterUrl = "https://x.com/TakerProtocol",
                    website = "https://earn.taker.xyz/",
                    describe = "TakerProtocol | 🔥0撸项目，签到类\n" +
                            "一共获得2次融资，已披露是300万种子轮融资，第二次融资金额未披露，由Electric Capital，DCG 领投，Dradonfly,Spartan Group 等众多VC参投",
                    star = 4
                ),
                ProjectInfo(
                    projectId = PROJECT_ID_BLESS,
                    name = "BLESS",
                    icon = R.mipmap.icon_bless,
                    twitterUrl = "https://x.com/theblessnetwork",
                    website = "https://bless.network/dashboard",
                    describe = "BLESS | 🔥0撸项目，DePin挂机\n" +
                            "融资800万美金的DePin挂机项目Bless，M31、NGC 等11个机构投资，预计2025年TGE空投，比例80%，该项目同时是加州大学伯克利分校区块链加速器 2023 年批次孵化项目",
                    star = 4
                ),
                ProjectInfo(
                    projectId = PROJECT_ID_TAKERPROTOCOL_SOWING,
                    name = "TakerSowing",
                    icon = R.mipmap.icon_takerprotocol,
                    twitterUrl = "https://x.com/TakerProtocol",
                    website = "https://sowing.taker.xyz/",
                    describe = "TakerSowing | 🔥0撸项目，签到类\n" +
                            "一共获得2次融资，已披露是300万种子轮融资，第二次融资金额未披露，由Electric Capital，DCG 领投，Dradonfly,Spartan Group 等众多VC参投",
                    star = 4
                ),
                ProjectInfo(
                    projectId = PROJECT_ID_CHAINOPERA_AI,
                    name = "ChainOpera AI",
                    icon = R.mipmap.icon_chain_opera_ai,
                    twitterUrl = "https://x.com/ChainOpera_AI",
                    website = "https://chainopera.ai/",
                    describe = "CHainOpera AI | 0撸签到 \n"+
                    "ChainOpera AI 提供 L1 区块链和协议，用于共同拥有和共同创建去中心化的 AI APP 和 Agent ，由 Federated AI 操作系统和平台提供支持。",
                    star = 4
                )
            )
        }

        fun projectName(projectId: Int) : String {
            return projectData().firstOrNull {
                it.projectId == projectId
            }?.name ?: "未知"
        }

    }

    data class ProjectInfo(
        val projectId: Int,
        val name: String,
        val icon: Int,
        val twitterUrl: String,
        val website: String,
        val describe: String,
        val star: Int
    ) : Serializable{
        fun fragment(): Fragment? {
            return when(projectId) {
                PROJECT_ID_LAYEREDGE -> {
                    FragmentLayerEdge()
                }
                PROJECT_ID_CORESKY -> {
                    FragmentCoreSky()
                }
                PROJECT_ID_TAKERPROTOCOL -> {
                    FragmentTakerProtocol()
                }
                PROJECT_ID_BLESS -> {
                    FragmentBless()
                }
                PROJECT_ID_TAKERPROTOCOL_SOWING -> {
                    FragmentTakerSowing()
                }
                PROJECT_ID_CHAINOPERA_AI -> {
                    FragmentChainOpera()
                }
                else -> null
            }.apply {
                this?.arguments = Bundle().apply {
                    putSerializable("info",this@ProjectInfo)
                }
            }
        }
    }

}