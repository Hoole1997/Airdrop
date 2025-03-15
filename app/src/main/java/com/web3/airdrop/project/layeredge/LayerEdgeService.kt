package com.web3.airdrop.project.layeredge

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.blankj.utilcode.util.UiMessageUtils
import com.drake.net.Get
import com.drake.net.Post
import com.drake.net.utils.scopeNet
import com.web3.airdrop.R
import com.web3.airdrop.extension.Web3Utils
import com.web3.airdrop.extension.setProxy
import com.web3.airdrop.project.log.LogData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import okhttp3.Headers
import okhttp3.Response
import org.json.JSONObject

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

    private fun refreshAccount(info: LayerEdgeAccountInfo) {
        //https://api.ipify.org/
        scopeNet(Dispatchers.IO) {
            val detailResponse = Get<Response>("https://referralapi.layeredge.io/api/referral/wallet-details/${info.wallet.address}") {
                setHeaders(headers)
                setClient {
                    setProxy(info.wallet.proxy)
                }
                converter = LayerEdgeConvert()
            }.await()
            val datailResult = JSONObject(detailResponse.body?.string())
            val statusCode = datailResult.optInt("statusCode")
            //未注册
            info.isRegister = statusCode != 404
            if (!info.isRegister) {
                LayerEdgeCommand.addLog(LogData(LayerEdgeCommand.LAYER_EDGE_PROJECT_ID, LogData.Level.WARN,info.wallet.id,"未注册"))
            } else {
                val data = datailResult.optJSONObject("data") ?:return@scopeNet
                info.layerEdgeId = data.optString("_id")
                info.nodePoints = data.optInt("nodePoints")
                info.taskPoints = data.optInt("rewardPoints")
                LayerEdgeCommand.addLog(LogData(LayerEdgeCommand.LAYER_EDGE_PROJECT_ID, LogData.Level.SUCCESS,info.wallet.id,
                    "已注册 _id=${info.layerEdgeId}  "))
            }
            LayerEdgeCommand.refreshAccountInfo(info)
        }
    }

    private fun refreshNodeState(info: LayerEdgeAccountInfo) {
        scopeNet {
            val nodeStatusResponse = Get<Response>("https://referralapi.layeredge.io/api/light-node/node-status/${info.wallet.address}"){
                setHeaders(headers)
                setClient {
                    setProxy(info.wallet.proxy)
                }
                converter = LayerEdgeConvert()
            }.await()
            val nodeResult = JSONObject(nodeStatusResponse.body?.string())
            val nodeData = nodeResult.optJSONObject("data")
            if (nodeData != null) {
                info.nodeStart = nodeData.optLong("startTimestamp") > 0L
                if (info.nodeStart) {
                    LayerEdgeCommand.addLog(LogData(LayerEdgeCommand.LAYER_EDGE_PROJECT_ID, LogData.Level.SUCCESS,info.wallet.id,
                        nodeResult.toString()))
                } else {
                    LayerEdgeCommand.addLog(LogData(LayerEdgeCommand.LAYER_EDGE_PROJECT_ID, LogData.Level.NORMAL,info.wallet.id,
                        nodeResult.toString()))
                }
            }
            LayerEdgeCommand.refreshAccountInfo(info)
            //https://referralapi.layeredge.io/api/light-node/node-status/
        }
    }

    private fun registerAccount(info: LayerEdgeAccountInfo) {
        if (info.wallet.address.isNullOrBlank()) return
        val refCode = "S3RiufC8"
        scopeNet {
            LayerEdgeCommand.addLog(LogData(LayerEdgeCommand.LAYER_EDGE_PROJECT_ID, LogData.Level.NORMAL,info.wallet.id,
                "开始注册 ${info.wallet.address} 校验邀请码"))
            val verifyResponse = Post<Response>("https://referralapi.layeredge.io/api/referral/verify-referral-code") {
                param("invite_code",refCode)
                json("invite_code" to refCode)
                setHeaders(headers)
                setClient {
                    setProxy(info.wallet.proxy)
                }
                converter = LayerEdgeConvert()
            }.await()
            val code = verifyResponse.code
            val verifyResult = JSONObject(verifyResponse.body?.string())
            LayerEdgeCommand.addLog(LogData(LayerEdgeCommand.LAYER_EDGE_PROJECT_ID, LogData.Level.NORMAL,info.wallet.id,
                "${info.wallet.address} result = ${verifyResult}"))
            val data = verifyResult.optJSONObject("data")
            val message = verifyResult.optString("message")
            if (data == null) {
                LayerEdgeCommand.addLog(LogData(LayerEdgeCommand.LAYER_EDGE_PROJECT_ID, LogData.Level.ERROR,info.wallet.id,
                    "${info.wallet.address} 注册失败 $message"))
                return@scopeNet
            }
            delay(2000)
            Post<Response>("https://referralapi.layeredge.io/api/referral/register-wallet/$refCode"){
                param("walletAddress",info.wallet.address)
                json("walletAddress" to info.wallet.address)
                setHeaders(headers)
                setClient {
                    setProxy(info.wallet.proxy)
                }
                converter = LayerEdgeConvert()
            }.await()?.let { response ->
                val code = response.code
                val result = JSONObject(response.body?.string())
                val data = result.optJSONObject("data")
                if (data != null) {
                    val _id = data.optString("id")
                    info.layerEdgeId = _id
                    LayerEdgeCommand.addLog(LogData(LayerEdgeCommand.LAYER_EDGE_PROJECT_ID, LogData.Level.SUCCESS,info.wallet.id,
                        "注册成功 ${info.wallet.address} _id = ${info.layerEdgeId}"))
                }
            }
        }
    }

    private fun connectNode(info: LayerEdgeAccountInfo) {
        scopeNet {
            LayerEdgeCommand.addLog(LogData(LayerEdgeCommand.LAYER_EDGE_PROJECT_ID, LogData.Level.NORMAL,info.wallet.id,
                "连接节点 ${info.wallet.address}"))

            // 要签名的信息
            val timestamp = System.currentTimeMillis()
            val message = "Node activation request for ${info.wallet.address} at ${timestamp}"
            val sign = Web3Utils.signPrefixedMessage(message,info.wallet.privateKey)

            val response = Post<Response>("https://referralapi.layeredge.io/api/light-node/node-action/${info.wallet.address}/start") {
                json("sign" to sign,"timestamp" to timestamp)
                setHeaders(headers)
                setClient {
                    setProxy(info.wallet.proxy)
                }
                converter = LayerEdgeConvert()
            }.await()

            val result = JSONObject(response.body?.string())
            val data = result.optJSONObject("data")
            if (data != null && data.optLong("startTimestamp") > 0L) {
                LayerEdgeCommand.addLog(LogData(LayerEdgeCommand.LAYER_EDGE_PROJECT_ID, LogData.Level.SUCCESS,info.wallet.id,
                    "连接成功 ${info.wallet.address}"))
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


    private fun registerCommand() {
        UiMessageUtils.getInstance().addListener(LayerEdgeCommand.MESSAGE_REQUEST_ACCOUNT) {
            if (it.`object` is LayerEdgeAccountInfo) {
                val info = it.`object` as LayerEdgeAccountInfo
                refreshAccount(info)
            }
        }
        UiMessageUtils.getInstance().addListener(LayerEdgeCommand.MESSAGE_REGISTER_ACCOUNT) {
            if (it.`object` is LayerEdgeAccountInfo) {
                val info = it.`object` as LayerEdgeAccountInfo
                registerAccount(info)
            }
        }
        UiMessageUtils.getInstance().addListener(LayerEdgeCommand.MESSAGE_REFRESH_NODE_STATE) {
            if (it.`object` is LayerEdgeAccountInfo) {
                val info = it.`object` as LayerEdgeAccountInfo
                refreshNodeState(info)
            }
        }
        UiMessageUtils.getInstance().addListener(LayerEdgeCommand.MESSAGE_CONNECT_NODE) {
            if (it.`object` is LayerEdgeAccountInfo) {
                val info = it.`object` as LayerEdgeAccountInfo
                connectNode(info)
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