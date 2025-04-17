package com.web3.airdrop.base

import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner

abstract class BaseService<VM : BaseModel> : Service(), ViewModelStoreOwner {

    private var mViewModelStore = ViewModelStore()

    companion object {
        val modelMap = mutableMapOf<Int, ViewModel?>()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        modelMap.put(initProjectId(),initViewModel())
        sendBroadcast(Intent().apply {
            setAction("createModel_${initProjectId()}")
        })
    }

    abstract fun initProjectId(): Int

    abstract fun initViewModel(): VM

    fun model(): VM? {
        return modelMap[initProjectId()] as VM
    }

    override val viewModelStore: ViewModelStore
        get() = mViewModelStore

    override fun onDestroy() {
        super.onDestroy()
        mViewModelStore.clear()
        sendBroadcast(Intent().apply {
            setAction("destroyModel_${initProjectId()}")
        })
    }
}