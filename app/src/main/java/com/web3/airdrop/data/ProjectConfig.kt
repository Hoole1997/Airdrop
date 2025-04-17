package com.web3.airdrop.data

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.web3.airdrop.R
import com.web3.airdrop.project.coresky.FragmentCoreSky
import com.web3.airdrop.project.layeredge.FragmentLayerEdge
import java.io.Serializable

class ProjectConfig {

    companion object {

        const val PROJECT_ID_LAYEREDGE = 1
        const val PROJECT_ID_CORESKY = 2

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
                else -> null
            }.apply {
                this?.arguments = Bundle().apply {
                    putSerializable("info",this@ProjectInfo)
                }
            }
        }
    }

}