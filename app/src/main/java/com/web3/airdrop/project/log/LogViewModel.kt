package com.web3.airdrop.project.log

import android.R
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.blankj.utilcode.util.UiMessageUtils

class LogViewModel : ViewModel() {

    val logEvent = MutableLiveData<List<LogData>>()
    fun registerLogListener(id:Int) {
        UiMessageUtils.getInstance().addListener(id) {
            if (it.`object` is LogData) {
                val list = arrayListOf<LogData>()
                if (logEvent.value?.isNotEmpty() == true) {
                    logEvent.value?.let { c -> list.addAll(c) }
                }
                list.add(it.`object` as LogData)
                logEvent.postValue(list)
            }
        }
    }

    fun removeLogListener(id: Int) {
        UiMessageUtils.getInstance().removeListeners(id)
    }


}