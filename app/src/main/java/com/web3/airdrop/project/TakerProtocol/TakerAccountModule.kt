package com.web3.airdrop.project.TakerProtocol

import android.content.Context
import android.view.LayoutInflater
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.TimeUtils
import com.web3.airdrop.R
import com.web3.airdrop.base.IProjectAccountModule
import com.web3.airdrop.databinding.ItemTakerProtocolWalletBinding
import com.web3.airdrop.extension.Extension.formatAddress
import com.web3.airdrop.project.TakerProtocol.data.TakerUser
import java.math.BigDecimal

class TakerAccountModule(val context: Context, val recyclerView: RecyclerView) : IProjectAccountModule<TakerUser, ItemTakerProtocolWalletBinding>() {
    override fun initItemBinding(): ItemTakerProtocolWalletBinding {
        return ItemTakerProtocolWalletBinding.bind(LayoutInflater.from(context).inflate(R.layout.item_taker_protocol_wallet,recyclerView,false))
    }

    override fun initRecyclerLayoutManager(): RecyclerView.LayoutManager {
        return LinearLayoutManager(context).apply {
            orientation = LinearLayoutManager.VERTICAL
        }
    }

    override fun initDiffCallback(): DiffUtil.ItemCallback<TakerUser> {
        return object : DiffUtil.ItemCallback<TakerUser>() {
            override fun areItemsTheSame(
                oldItem: TakerUser,
                newItem: TakerUser
            ): Boolean {
                return oldItem.wallet == newItem.wallet
            }

            override fun areContentsTheSame(
                oldItem: TakerUser,
                newItem: TakerUser
            ): Boolean {
                return oldItem.lastSyncTime == newItem.lastSyncTime
            }
        }
    }

    override fun initItemView(data: TakerUser?, position: Int, itemDb: ItemTakerProtocolWalletBinding) {
        super.initItemView(data, position, itemDb)
        data?.let {
            itemDb.tvNo.text = "No.${position+1}"
            itemDb.ethAddress.text = it.wallet?.address?.formatAddress()
            itemDb.tvSyncTime.text = "SyncTime:${if (it.lastSyncTime == 0L) 0 else TimeUtils.getFriendlyTimeSpanByNow(it.lastSyncTime) }"
            itemDb.tvPoints.text = "Pts:${if (it.rewardAmount.isBlank()) 0 else BigDecimal(it.rewardAmount).toInt()}"
            itemDb.tvFollowTwitter.text = "Twitter: false"
            itemDb.tvShutdown.text = "lastMining: ${if (it.lastMiningTime == 0L) 0 else TimeUtils.millis2String(it.lastMiningTime*1000)}"
        }
    }
}