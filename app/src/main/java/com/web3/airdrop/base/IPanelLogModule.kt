package com.web3.airdrop.base

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.web3.airdrop.project.log.FragmentLog

class IPanelLogModule<VM: BaseModel>(val activity: FragmentActivity,val model: VM?) : IPanelModule{

    override fun initFragment(): Fragment? {
        return PanelFragmentLog(model)
    }

    override fun initTabName(): String {
        return "日志"
    }

    class PanelFragmentLog<VM: BaseModel> (val viewModule: VM?) : FragmentLog<VM>() {
        override suspend fun initViewModel(): VM? {
            return viewModule
        }
    }

}