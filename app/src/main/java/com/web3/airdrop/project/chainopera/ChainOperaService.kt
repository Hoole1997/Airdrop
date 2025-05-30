package com.web3.airdrop.project.chainopera

import android.content.Intent
import androidx.lifecycle.ViewModelProvider
import com.web3.airdrop.base.BaseService
import com.web3.airdrop.data.ProjectConfig
import com.web3.airdrop.project.ActivityProject
import com.web3.airdrop.project.takersowing.TakerSowingModel
import com.web3.airdrop.project.takersowing.data.TakerSowingUser

class ChainOperaService : BaseService<ChainOperaModel, ChainOperaUser>() {
    override fun initProjectInfo(): ProjectConfig.ProjectInfo {
        return ProjectConfig.projectData().first {
            it.projectId == ProjectConfig.PROJECT_ID_CHAINOPERA_AI
        }
    }

    override fun initViewModel(): ChainOperaModel {
        return ViewModelProvider(this)[ChainOperaModel::class.java]
    }

    override fun notificationIntent(): Intent {
        return Intent().apply {
            setClass(this@ChainOperaService, ActivityProject::class.java)
        }
    }
}