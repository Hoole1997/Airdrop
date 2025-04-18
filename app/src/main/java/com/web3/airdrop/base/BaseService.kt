package com.web3.airdrop.base

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import com.web3.airdrop.R
import com.web3.airdrop.data.ProjectConfig

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
        createNotificationChannel()
        createNotification()
        modelMap.put(initProjectInfo().projectId,initViewModel())
        sendBroadcast(Intent().apply {
            setAction("createModel_${initProjectInfo().projectId}")
        })
    }

    abstract fun initProjectInfo(): ProjectConfig.ProjectInfo

    abstract fun initViewModel(): VM

    abstract fun notificationIntent(): Intent

    fun model(): VM? {
        return modelMap[initProjectInfo().projectId] as VM?
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                initProjectInfo().projectId.toString(),
                initProjectInfo().name,
                NotificationManager.IMPORTANCE_DEFAULT
            )

            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(serviceChannel)
        }
    }

    private fun createNotification() {
        val notificationIntent = notificationIntent()
        notificationIntent.putExtra("info",initProjectInfo())
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        // 创建通知
        val notification = NotificationCompat.Builder(this, initProjectInfo().projectId.toString())
            .setContentTitle("${initProjectInfo().name} running")
            .setContentText("点击以返回应用")
            .setSmallIcon(initProjectInfo().icon)
            .setContentIntent(pendingIntent)
            .build()

        startForeground(initProjectInfo().projectId, notification);
    }

    override val viewModelStore: ViewModelStore
        get() = mViewModelStore

    override fun onDestroy() {
        super.onDestroy()
        mViewModelStore.clear()
        sendBroadcast(Intent().apply {
            setAction("destroyModel_${initProjectInfo()}")
        })
    }
}