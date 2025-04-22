package com.web3.airdrop.project.layeredge

import android.content.Context
import android.view.LayoutInflater
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.TimeUtils
import com.web3.airdrop.R
import com.web3.airdrop.base.IProjectAccountModule
import com.web3.airdrop.databinding.ItemLayeredgeWalletBinding
import com.web3.airdrop.extension.Extension.formatAddress
import com.web3.airdrop.project.layeredge.data.LayerEdgeAccountInfo

class LayerEdgeAccountModule(val context: Context,val recyclerView: RecyclerView) : IProjectAccountModule<LayerEdgeAccountInfo, ItemLayeredgeWalletBinding>() {

    override fun initItemBinding(): ItemLayeredgeWalletBinding {
        return ItemLayeredgeWalletBinding.bind(LayoutInflater.from(context).inflate(R.layout.item_layeredge_wallet,recyclerView,false))
    }

    override fun initRecyclerLayoutManager(): RecyclerView.LayoutManager {
        return LinearLayoutManager(context).apply {
            orientation = LinearLayoutManager.VERTICAL
        }
    }

    override fun initDiffCallback(): DiffUtil.ItemCallback<LayerEdgeAccountInfo> {
        return object : DiffUtil.ItemCallback<LayerEdgeAccountInfo>() {
            override fun areItemsTheSame(
                oldItem: LayerEdgeAccountInfo,
                newItem: LayerEdgeAccountInfo
            ): Boolean {
                return oldItem.localWallet == newItem.localWallet
            }

            override fun areContentsTheSame(
                oldItem: LayerEdgeAccountInfo,
                newItem: LayerEdgeAccountInfo
            ): Boolean {
                return oldItem.lastSyncTime == newItem.lastSyncTime
            }
        }
    }

    override fun initItemView(data: LayerEdgeAccountInfo?,position:Int, itemDb: ItemLayeredgeWalletBinding) {
        super.initItemView(data,position, itemDb)
        data?.let {
            itemDb.tvNo.text = "No.${position+1}"
            itemDb.ethAddress.text = "${it.localWallet?.address?.formatAddress()}"
            itemDb.tvConnect.text = "连接: ${it.nodeStart}"
            itemDb.tvConnectDownTime.text = "最后同步:${if (it.lastSyncTime == 0L) 0 else TimeUtils.getFriendlyTimeSpanByNow(it.lastSyncTime) }"
            itemDb.tvCheckinDay.text = "签到:${it.dailyStreak}天"
            itemDb.llRoot.setBackgroundResource(if (it.isRegister()) R.drawable.registered_background else R.drawable.unregistered_background)
        }
    }
}