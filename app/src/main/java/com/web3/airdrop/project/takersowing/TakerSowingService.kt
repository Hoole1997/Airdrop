package com.web3.airdrop.project.takersowing

import android.content.Intent
import androidx.lifecycle.ViewModelProvider
import com.web3.airdrop.base.BaseService
import com.web3.airdrop.data.ProjectConfig
import com.web3.airdrop.project.ActivityProject
import com.web3.airdrop.project.takersowing.data.TakerSowingUser

class TakerSowingService : BaseService<TakerSowingModel, TakerSowingUser>() {
    override fun initProjectInfo(): ProjectConfig.ProjectInfo {
        return ProjectConfig.projectData().first {
            it.projectId == ProjectConfig.PROJECT_ID_TAKERPROTOCOL_SOWING
        }
    }

    override fun initViewModel(): TakerSowingModel {
        return ViewModelProvider(this)[TakerSowingModel::class.java]
    }

    override fun notificationIntent(): Intent {
        return Intent().apply {
            setClass(this@TakerSowingService, ActivityProject::class.java)
        }
    }
}