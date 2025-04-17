package com.web3.airdrop.project.coresky

import androidx.lifecycle.ViewModelProvider
import com.web3.airdrop.base.BaseModel
import com.web3.airdrop.base.BaseService
import com.web3.airdrop.data.ProjectConfig
import com.web3.airdrop.project.log.FragmentLog

class FragmentCoreSkyPanelLog : FragmentLog<CoreSkyModel>() {
    override suspend fun initViewModel(): CoreSkyModel? {
        return BaseService.modelMap.get(ProjectConfig.PROJECT_ID_CORESKY) as CoreSkyModel
    }
}