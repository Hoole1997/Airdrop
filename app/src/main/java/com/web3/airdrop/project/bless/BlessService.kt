package com.web3.airdrop.project.bless

import android.content.Intent
import androidx.lifecycle.ViewModelProvider
import com.web3.airdrop.base.BaseService
import com.web3.airdrop.data.ProjectConfig
import com.web3.airdrop.project.ActivityProject
import com.web3.airdrop.project.bless.data.BlessNodeInfo

class BlessService : BaseService<BlessModel, BlessNodeInfo>() {

    override fun initProjectInfo(): ProjectConfig.ProjectInfo {
        return ProjectConfig.projectData().first {
            it.projectId == ProjectConfig.PROJECT_ID_BLESS
        }
    }

    override fun initViewModel() : BlessModel {
        return ViewModelProvider(this)[BlessModel::class.java]
    }

    override fun notificationIntent(): Intent {
        return Intent().apply {
            setClass(this@BlessService, ActivityProject::class.java)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()

    }

}