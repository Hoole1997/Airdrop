package com.web3.airdrop.base

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.web3.airdrop.data.ProjectConfig
import com.web3.airdrop.project.log.LogData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

open class BaseModel : ViewModel(), IModel {

    //面板--账号信息
    val panelAccountInfo = MutableLiveData<List<Pair<String, String>>>()
    //点击的账号
    val panelCurrentAccountInfo = MutableLiveData<Any?>()
    //全局模式
    val globalMode = MutableLiveData<Boolean>(false)
    //随机模式
    val randomMode = MutableLiveData<Boolean>(false)
    //组合模式
    val combinationMode = MutableLiveData<Boolean>(false)
    //日志
    val logEvent = MutableLiveData<ArrayList<LogData>>(arrayListOf<LogData>())

    override fun refreshPanelAccountInfo(data: Any,online: Boolean) {
        if (!online) {
            panelCurrentAccountInfo.postValue(data)
        }
    }

    override fun startTask(panelTask: List<IPanelTaskModule.PanelTask>) {

    }

    override suspend fun sendLog(log: LogData) {
        withContext(Dispatchers.Main) {
            val list = arrayListOf<LogData>().apply {
                addAll(logEvent.value!!)
                add(log)
            }
            logEvent.postValue(list)
        }
    }

}