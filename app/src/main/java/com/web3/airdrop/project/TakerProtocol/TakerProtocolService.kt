package com.web3.airdrop.project.TakerProtocol

import android.content.Intent
import androidx.lifecycle.ViewModelProvider
import com.web3.airdrop.base.BaseService
import com.web3.airdrop.data.ProjectConfig
import com.web3.airdrop.project.ActivityProject

class TakerProtocolService : BaseService<TakerModel>() {
    override fun initProjectInfo(): ProjectConfig.ProjectInfo {
        return ProjectConfig.projectData().first {
            it.projectId == ProjectConfig.PROJECT_ID_TAKERPROTOCOL
        }
    }

    override fun initViewModel(): TakerModel {
        return ViewModelProvider(this)[TakerModel::class.java]
    }

    override fun notificationIntent(): Intent {
        return Intent().apply {
            setClass(this@TakerProtocolService, ActivityProject::class.java)
        }
    }
}