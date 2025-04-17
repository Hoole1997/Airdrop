package com.web3.airdrop.project.coresky

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.lifecycle.ViewModelProvider
import com.web3.airdrop.R
import com.web3.airdrop.base.BaseService
import com.web3.airdrop.data.ProjectConfig
import com.web3.airdrop.project.ActivityProject

class CoreSkyService : BaseService<CoreSkyModel>() {

    companion object {
        const val CHANNEL_ID = "CoreSky"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        createNotification()
    }

    override fun initProjectId(): Int {
        return ProjectConfig.PROJECT_ID_CORESKY
    }

    override fun initViewModel() : CoreSkyModel {
        return ViewModelProvider(this)[CoreSkyModel::class.java]
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "CoreSky",
                NotificationManager.IMPORTANCE_DEFAULT
            )

            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(serviceChannel)
        }
    }

    private fun createNotification() {
        val notificationIntent = Intent(this, ActivityProject::class.java)
        notificationIntent.putExtra("info", ProjectConfig.projectData().firstOrNull {
            it.projectId == ProjectConfig.PROJECT_ID_CORESKY
        })
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        // 创建通知
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("CoreSky running")
            .setContentText("点击以返回应用")
            .setSmallIcon(R.mipmap.icon_coresky)  // 更换为你的图标
            .setContentIntent(pendingIntent)
            .build()

        // 启动前台服务
        startForeground(2, notification);
    }

    override fun onDestroy() {
        super.onDestroy()

    }

}