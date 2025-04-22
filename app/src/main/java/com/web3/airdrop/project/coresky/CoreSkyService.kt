package com.web3.airdrop.project.coresky

import android.content.Intent
import androidx.lifecycle.ViewModelProvider
import com.web3.airdrop.R
import com.web3.airdrop.base.BaseService
import com.web3.airdrop.data.ProjectConfig
import com.web3.airdrop.project.ActivityProject
import com.web3.airdrop.project.coresky.data.CoreSkyUser

class CoreSkyService : BaseService<CoreSkyModel, CoreSkyUser>() {

    override fun initProjectInfo(): ProjectConfig.ProjectInfo {
        return ProjectConfig.projectData().first {
            it.projectId == ProjectConfig.PROJECT_ID_CORESKY
        }
    }

    override fun initViewModel() : CoreSkyModel {
        return ViewModelProvider(this)[CoreSkyModel::class.java]
    }

    override fun notificationIntent(): Intent {
        return Intent().apply {
            setClass(this@CoreSkyService, ActivityProject::class.java)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()

    }

}