package com.web3.airdrop.project.layeredge

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import com.blankj.utilcode.util.TimeUtils
import com.chad.library.adapter4.BaseDifferAdapter
import com.chad.library.adapter4.viewholder.DataBindingHolder
import com.web3.airdrop.R
import com.web3.airdrop.databinding.ItemLayeredgeWalletBinding
import com.web3.airdrop.extension.Extension.formatAddress
import com.web3.airdrop.project.layeredge.data.LayerEdgeAccountInfo

class LayerEdgeAccountAdapter : BaseDifferAdapter<LayerEdgeAccountInfo, DataBindingHolder<ItemLayeredgeWalletBinding>>(object : DiffUtil.ItemCallback<LayerEdgeAccountInfo>() {
    override fun areItemsTheSame(
        oldItem: LayerEdgeAccountInfo,
        newItem: LayerEdgeAccountInfo
    ): Boolean {
        return oldItem.wallet == newItem.wallet
    }

    override fun areContentsTheSame(
        oldItem: LayerEdgeAccountInfo,
        newItem: LayerEdgeAccountInfo
    ): Boolean {
        return oldItem.boostNodePoints == newItem.boostNodePoints &&
                oldItem.cliBoostNodePoints == newItem.cliBoostNodePoints &&
                oldItem.cliNodePoints == newItem.cliNodePoints &&
                oldItem.confirmedReferralPoints == newItem.confirmedReferralPoints &&
                oldItem.createdAt == newItem.createdAt &&
                oldItem.dailyStreak == newItem.dailyStreak &&
                oldItem.id == newItem.id &&
                oldItem.isTwitterVerified == newItem.isTwitterVerified &&
                oldItem.lastClaimed == newItem.lastClaimed &&
                oldItem.level == newItem.level &&
                oldItem.nodePoints == newItem.nodePoints &&
                oldItem.referralCode == newItem.referralCode &&
                oldItem.rewardPoints == newItem.rewardPoints &&
                oldItem.totalPoints == newItem.totalPoints &&
                oldItem.twitterId == newItem.twitterId &&
                oldItem.updatedAt == newItem.updatedAt &&
                oldItem.verifiedReferralPoints == newItem.verifiedReferralPoints &&
                oldItem.startTimestamp == newItem.startTimestamp &&
                oldItem.lastSyncTime == newItem.lastSyncTime
    }
}) {
    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(
        holder: DataBindingHolder<ItemLayeredgeWalletBinding>,
        position: Int,
        item: LayerEdgeAccountInfo?
    ) {
        item?.let {
            holder.binding.ethAddress.text = it.wallet?.address?.formatAddress()
            holder.binding.tvConnect.text = "Connect: ${it.nodeStart}"
            holder.binding.tvConnectDownTime.text = "LastSync:${if (it.lastSyncTime == 0L) 0 else TimeUtils.millis2String(it.lastSyncTime) }"
            holder.binding.tvCheckinDay.text = "CheckinDay:${it.dailyStreak}"
            holder.binding.llRoot.setBackgroundResource(if (it.isRegister) R.drawable.registered_background else R.drawable.unregistered_background)
        }
    }

    override fun onCreateViewHolder(
        context: Context,
        parent: ViewGroup,
        viewType: Int
    ): DataBindingHolder<ItemLayeredgeWalletBinding> {
        return DataBindingHolder<ItemLayeredgeWalletBinding>(
            LayoutInflater.from(context).inflate(R.layout.item_layeredge_wallet, parent,false)
        )
    }
}