package com.web3.airdrop.project.layeredge

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.blankj.utilcode.util.GsonUtils
import com.blankj.utilcode.util.UiMessageUtils
import com.drake.net.Get
import com.drake.net.Post
import com.drake.net.utils.scopeNet
import com.web3.airdrop.R
import com.web3.airdrop.data.AppDatabase
import com.web3.airdrop.extension.Extension.formatAddress
import com.web3.airdrop.extension.Web3Utils
import com.web3.airdrop.extension.setProxy
import com.web3.airdrop.project.layeredge.data.LayerEdgeAccountInfo
import com.web3.airdrop.project.log.LogData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import okhttp3.Headers
import okhttp3.Response
import org.json.JSONObject
import kotlin.random.Random
import kotlin.text.isEmpty

class LayerEdgeService : Service() {

    companion object {
        const val CHANNEL_ID = "LayerEdge"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        createNotification()

        registerCommand()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        return START_STICKY
    }

    private fun registerCommand() {
        UiMessageUtils.getInstance().addListener {
            when(it.id) {
                LayerEdgeCommand.MESSAGE_REQUEST_ACCOUNT -> {
                    val infoList = it.`object` as List<LayerEdgeAccountInfo>
                    refreshAccount(infoList)
                }
                LayerEdgeCommand.MESSAGE_REGISTER_ACCOUNT -> {
                    val infoList = it.`object` as List<LayerEdgeAccountInfo>
                    registerAccount(infoList)
                }
                LayerEdgeCommand.MESSAGE_CONNECT_NODE -> {
                    val infoList = it.`object` as List<LayerEdgeAccountInfo>
                    connectNode(infoList,true)
                }
                LayerEdgeCommand.MESSAGE_DISCONNECT_NODE -> {
                    val infoList = it.`object` as List<LayerEdgeAccountInfo>
                    connectNode(infoList,false)
                }
                LayerEdgeCommand.MESSAGE_SIGN_EVERYDAY -> {
                    val infoList = it.`object` as List<LayerEdgeAccountInfo>
                    sign(infoList)
                }
            }
        }
    }

    private fun refreshAccount(infoList: List<LayerEdgeAccountInfo>) {
        //https://api.ipify.org/
        scopeNet(Dispatchers.IO) {
            infoList.forEachIndexed { index,info ->
                val delayTime = Random.nextLong(1,5)
                LayerEdgeCommand.addLog(LogData(LayerEdgeCommand.LAYER_EDGE_PROJECT_ID, LogData.Level.SUCCESS,info.wallet?.id,
                    "请求用户信息 _id=${info.wallet?.address?.formatAddress()} ${index}/${infoList.size}  delayTime:$delayTime s"))
                try {
                    val detailResponse = Get<Response>("https://referralapi.layeredge.io/api/referral/wallet-details/${info.wallet?.address}") {
                        setHeaders(headers)
                        setClient {
                            setProxy(info.wallet?.proxy.toString())
                        }
                        converter = LayerEdgeConvert()
                    }.await()

                    val nodeStatusResponse = Get<Response>("https://referralapi.layeredge.io/api/light-node/node-status/${info.wallet?.address}"){
                        setHeaders(headers)
                        setClient {
                            setProxy(info.wallet?.proxy.toString())
                        }
                        converter = LayerEdgeConvert()
                    }.await()

                    val datailResult = JSONObject(detailResponse.body?.string())
                    datailResult.optString("data","").let {
                        if (it.isEmpty()) return@let
                        val accountInfo = GsonUtils.fromJson<LayerEdgeAccountInfo>(it,
                            LayerEdgeAccountInfo::class.java)

                        val nodeResult = JSONObject(nodeStatusResponse.body?.string())
                        val nodeData = nodeResult.optJSONObject("data")
                        if (nodeData != null) {
                            accountInfo.startTimestamp = nodeData.optLong("startTimestamp")
                        }
                        accountInfo.lastSyncTime = System.currentTimeMillis()
                        if (!accountInfo.isRegister) {
                            LayerEdgeCommand.addLog(LogData(LayerEdgeCommand.LAYER_EDGE_PROJECT_ID, LogData.Level.WARN,info.wallet?.id,"未注册 ${index}/${infoList.size} delayTime:$delayTime s"))
                        } else {
                            AppDatabase.getDatabase().layeredgeDao().insertOrUpdate(accountInfo)
                            LayerEdgeCommand.addLog(LogData(LayerEdgeCommand.LAYER_EDGE_PROJECT_ID, LogData.Level.SUCCESS,info.wallet?.id,
                                "同步用户信息成功 _id=${accountInfo.id} ${index}/${infoList.size} delayTime:$delayTime s"))
                            LayerEdgeCommand.requestAccountResult(accountInfo)
                        }
                    }
                }catch (e: Exception) {
                    LayerEdgeCommand.addLog(LogData(LayerEdgeCommand.LAYER_EDGE_PROJECT_ID, LogData.Level.WARN,info.wallet?.id,"同步用户信息失败 exception:${e.message} ${index}/${infoList.size} delayTime:$delayTime s"))
                }
                delay(delayTime*1000)
            }
        }
    }

    private fun registerAccount(infoList: List<LayerEdgeAccountInfo>) {
        val refCode = "S3RiufC8"
        scopeNet {
            infoList.forEachIndexed { index,info ->
                val delayTime = Random.nextLong(10,30)
                try {
                    if (info.wallet?.address.isNullOrBlank()) return@forEachIndexed
                    LayerEdgeCommand.addLog(LogData(LayerEdgeCommand.LAYER_EDGE_PROJECT_ID, LogData.Level.NORMAL,info.wallet?.id,
                        "开始注册 ${info.wallet?.address?.formatAddress()} 校验邀请码 ${index}/${infoList.size}"))
                    val verifyResponse = Post<Response>("https://referralapi.layeredge.io/api/referral/verify-referral-code") {
                        param("invite_code",refCode)
                        json("invite_code" to refCode)
                        setHeaders(headers)
                        setClient {
                            setProxy(info.wallet?.proxy)
                        }
                        converter = LayerEdgeConvert()
                    }.await()
                    val verifyResult = JSONObject(verifyResponse.body?.string())
                    LayerEdgeCommand.addLog(LogData(LayerEdgeCommand.LAYER_EDGE_PROJECT_ID, LogData.Level.NORMAL,info.wallet?.id,
                        "${info.wallet?.address?.formatAddress()} result = ${verifyResult}"))
                    val data = verifyResult.optJSONObject("data")
                    val message = verifyResult.optString("message")
                    if (data == null) {
                        LayerEdgeCommand.addLog(LogData(LayerEdgeCommand.LAYER_EDGE_PROJECT_ID, LogData.Level.ERROR,info.wallet?.id,
                            "${info.wallet?.address?.formatAddress()} 注册失败 $message ${index}/${infoList.size} delay:$delayTime s"))
                        return@scopeNet
                    }
                    Post<Response>("https://referralapi.layeredge.io/api/referral/register-wallet/$refCode"){
                        param("walletAddress",info.wallet?.address)
                        json("walletAddress" to info.wallet?.address)
                        setHeaders(headers)
                        setClient {
                            setProxy(info.wallet?.proxy)
                        }
                        converter = LayerEdgeConvert()
                    }.await()?.let { response ->
                        val code = response.code
                        val result = JSONObject(response.body?.string())
                        val data = result.optString("data")

                        data?.let {
                            if (data.isEmpty())return@let
                            val accountInfo = GsonUtils.fromJson<LayerEdgeAccountInfo>(it,
                                LayerEdgeAccountInfo::class.java)
                            accountInfo.lastSyncTime = System.currentTimeMillis()
                            if (!info.isRegister) {
                                LayerEdgeCommand.addLog(LogData(LayerEdgeCommand.LAYER_EDGE_PROJECT_ID, LogData.Level.WARN,info.wallet?.id,"未注册 delayTime:$delayTime s"))
                            } else {
                                LayerEdgeCommand.addLog(LogData(LayerEdgeCommand.LAYER_EDGE_PROJECT_ID, LogData.Level.SUCCESS,info.wallet?.id,
                                    "同步用户信息成功 _id=${accountInfo.id}  delayTime:$delayTime s"))
                                LayerEdgeCommand.addLog(LogData(LayerEdgeCommand.LAYER_EDGE_PROJECT_ID, LogData.Level.SUCCESS,info.wallet?.id,
                                    "注册成功 ${info.wallet?.address?.formatAddress()} _id = ${accountInfo.id} ${index}/${infoList.size} delay:$delayTime s"))
                                AppDatabase.getDatabase().layeredgeDao().insertOrUpdate(accountInfo)
                                LayerEdgeCommand.requestAccountResult(accountInfo)
                            }
                        }
                    }
                }catch (e: Exception) {
                    LayerEdgeCommand.addLog(LogData(LayerEdgeCommand.LAYER_EDGE_PROJECT_ID, LogData.Level.ERROR,info.wallet?.id,
                        "${info.wallet?.address?.formatAddress()} 注册失败 ${e.message} ${index}/${infoList.size} delay:$delayTime s" ))
                }
                delay(delayTime*1000)
            }
        }
    }

    private fun connectNode(infoList: List<LayerEdgeAccountInfo>,connect: Boolean) {
        scopeNet(Dispatchers.IO) {
            infoList.forEachIndexed { index,info ->
                val delayTime = Random.nextLong(2,10)
                try {
                    val timestamp = System.currentTimeMillis()
                    LayerEdgeCommand.addLog(LogData(LayerEdgeCommand.LAYER_EDGE_PROJECT_ID, LogData.Level.NORMAL,info.wallet?.id,
                        "${if (connect) "连接" else "断开"}节点 ${info.wallet?.address?.formatAddress()} ${index}/${infoList.size} $timestamp"))
                    // 要签名的信息
                    //Node activation request for 0x99de99385C41b0FA7AE9df5c068399E7082276F1 at 1742309571657
                    //断开
                    //Node deactivation request for 0x99de99385C41b0FA7AE9df5c068399E7082276F1 at 1742311562836
                    val message = if (connect) {
                        "Node activation request for ${info.wallet?.address} at ${timestamp}"
                    } else {
                        "Node deactivation request for ${info.wallet?.address} at ${timestamp}"
                    }
                    val sign = Web3Utils.signPrefixedMessage(message,info.wallet?.privateKey)

                    val response = Post<Response>("https://referralapi.layeredge.io/api/light-node/node-action/${info.wallet?.address}/${if (connect) "start" else "stop"}") {
                        json("sign" to sign,"timestamp" to timestamp)
                        setHeaders(headers)
                        setClient {
                            setProxy(info.wallet?.proxy)
                        }
                        converter = LayerEdgeConvert()
                    }.await()

                    val result = JSONObject(response.body?.string())
                    val data = result.optJSONObject("data")
                    if (data != null) {
                        val startTimestamp = data.optLong("startTimestamp")
                        info.startTimestamp = startTimestamp
                        LayerEdgeCommand.addLog(LogData(LayerEdgeCommand.LAYER_EDGE_PROJECT_ID, LogData.Level.SUCCESS,info.wallet?.id,
                            "${if (connect) "连接" else "断开"}成功 ${info.wallet?.address?.formatAddress()} ${index}/${infoList.size} delayTime = $delayTime s"))
                        if (info.id.isNotEmpty()) {
                            info.startTimestamp = startTimestamp
                            info.lastSyncTime = System.currentTimeMillis()
                            AppDatabase.getDatabase().layeredgeDao().insertOrUpdate(info)
                            LayerEdgeCommand.requestAccountResult(info)
                        }
                    } else {
                        LayerEdgeCommand.addLog(LogData(LayerEdgeCommand.LAYER_EDGE_PROJECT_ID, LogData.Level.ERROR,info.wallet?.id,
                            "${if (connect) "连接" else "断开"}失败 ${info.wallet?.address?.formatAddress()} ${index}/${infoList.size} delayTime = $delayTime s"))
                    }
                }catch (e: Exception) {
                    LayerEdgeCommand.addLog(LogData(LayerEdgeCommand.LAYER_EDGE_PROJECT_ID, LogData.Level.ERROR,info.wallet?.id,
                        "${if (connect) "连接" else "断开"}失败 ${info.wallet?.address?.formatAddress()} exception:${e.message} ${index}/${infoList.size} delayTime = $delayTime s"))
                }
                delay(delayTime*1000)
            }
        }
    }

    val headers = Headers.Builder()
        .add("Accept", "application/json, text/plain, */*")
        .add("Accept-Encoding", "gzip, deflate, br")
        .add("Accept-Language", "en-US,en;q=0.9")
        .add("Origin", "https://layeredge.io")
        .add("Referer", "https://layeredge.io/")
        .add("Sec-Fetch-Dest", "empty")
        .add("Sec-Fetch-Mode", "cors")
        .add("Sec-Fetch-Site", "same-site")
        .add("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/121.0.0.0 Safari/537.36")
        .add("sec-ch-ua", "\"Not A(Brand\";v=\"99\", \"Google Chrome\";v=\"121\", \"Chromium\";v=\"121\"")
        .add("sec-ch-ua-mobile", "?0")
        .add("sec-ch-ua-platform", "\"Windows\"")
        .build()

    //https://referralapi.layeredge.io/api/task/connect-twitter
    //{
    //  "walletAddress": "0x0Ed62f9972Aa73DB886C53B3FeF3C6D6f5791f9a",
    //  "sign": "0xf32a3cacc636c80fe01370bf8c419ca68df226db728332573d8e7a4761773bfd464caf3a8ae9d62fcedc373509dc67177799f08f1c5448f39bf011805609eb571b",
    //  "timestamp": "1742071837180",
    //  "twitterId": "1940266898"
    //}
    //I am verifying my Twitter authentication for 0x0Ed62f9972Aa73DB886C53B3FeF3C6D6f5791f9a at 1742072019740

    //浏览器任务
    //I am claiming my light node run task node points for 0x0Ed62f9972Aa73DB886C53B3FeF3C6D6f5791f9a at 1742072251183

    //证明任务
    //I am submitting a proof for LayerEdge at 2025-03-15T21:00:10.612Z

    //I am claiming my proof submission node points for 0x0Ed62f9972Aa73DB886C53B3FeF3C6D6f5791f9a at 1742072460682
    private fun sign(list: List<LayerEdgeAccountInfo>) {
        scopeNet {
            list.forEachIndexed { index,accountInfo ->
                val delayTime = Random.nextLong(10,30)
                try {
                    LayerEdgeCommand.addLog(LogData(LayerEdgeCommand.LAYER_EDGE_PROJECT_ID, LogData.Level.NORMAL,accountInfo.wallet?.id,
                        "签到 ${accountInfo.wallet?.address?.formatAddress()} ${index}/${list.size}"))
                    val timestamp = System.currentTimeMillis()
                    val message = "I am claiming my daily node point for ${accountInfo.wallet?.address} at ${timestamp}"
                    val sign = Web3Utils.signPrefixedMessage(message,accountInfo.wallet?.privateKey)
                    val response = Post<Response>("https://referralapi.layeredge.io/api/light-node/claim-node-points") {
                        json(
                            "sign" to sign,
                            "timestamp" to timestamp,
                            "walletAddress" to accountInfo.wallet?.address
                        )
                        setHeaders(headers)
                        setClient {
                            setProxy(accountInfo.wallet?.proxy)
                        }
                        converter = LayerEdgeConvert()
                    }.await()
                    JSONObject(response.body?.string()).let {
                        val statusCode = it.optInt("statusCode")
                        val message = it.optString("message")
                        if (statusCode == 0) {
                            LayerEdgeCommand.addLog(LogData(LayerEdgeCommand.LAYER_EDGE_PROJECT_ID, LogData.Level.SUCCESS,accountInfo.wallet?.id,
                                "${accountInfo.wallet?.address?.formatAddress()} 签到成功 ${index}/${list.size} delayTime:$delayTime"))
                        } else {
                            LayerEdgeCommand.addLog(LogData(LayerEdgeCommand.LAYER_EDGE_PROJECT_ID, LogData.Level.ERROR,accountInfo.wallet?.id,
                                "${accountInfo.wallet?.address?.formatAddress()} $message ${index}/${list.size} delayTime:$delayTime"))
                        }
                    }
                }catch (e: Exception) {
                    LayerEdgeCommand.addLog(LogData(LayerEdgeCommand.LAYER_EDGE_PROJECT_ID, LogData.Level.ERROR,accountInfo.wallet?.id,
                        e.message.toString() +"${index}/${list.size} delayTime:$delayTime"
                    ))
                }
                delay(delayTime*1000)
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "LayerEdge",
                NotificationManager.IMPORTANCE_DEFAULT
            )

            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(serviceChannel)
        }
    }

    private fun createNotification() {
        val notificationIntent = Intent(this, ActivityLayerEdge::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        // 创建通知
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("LayerEdge running")
            .setContentText("点击以返回应用")
            .setSmallIcon(R.mipmap.icon_layeredge)  // 更换为你的图标
            .setContentIntent(pendingIntent)
            .build();

        // 启动前台服务
        startForeground(1, notification);
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        UiMessageUtils.getInstance().removeListeners(LayerEdgeCommand.MESSAGE_REQUEST_ACCOUNT)
    }

}