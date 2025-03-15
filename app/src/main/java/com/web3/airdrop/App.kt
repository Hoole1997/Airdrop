package com.web3.airdrop

import android.app.Application
import android.util.Log.VERBOSE
import com.blankj.utilcode.util.Utils
import com.drake.net.BuildConfig
import com.drake.net.NetConfig
import com.drake.net.interceptor.LogRecordInterceptor
import com.drake.net.okhttp.setConverter
import com.drake.net.okhttp.setDebug
import com.ihsanbal.logging.Level
import com.ihsanbal.logging.LoggingInterceptor
import okhttp3.Credentials
import okhttp3.internal.UTC
import java.net.InetSocketAddress
import java.net.Proxy
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLSocketFactory

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        Utils.init(this)
        NetConfig.initialize("", this) {
            // 超时配置, 默认是10秒, 设置太长时间会导致用户等待过久
            connectTimeout(300, TimeUnit.SECONDS)
            readTimeout(300, TimeUnit.SECONDS)
            writeTimeout(300, TimeUnit.SECONDS)
            addInterceptor(
                LoggingInterceptor.Builder()
                .setLevel(Level.BASIC)
                .log(VERBOSE)
                .build())
            setDebug(BuildConfig.DEBUG)
        }
    }

}