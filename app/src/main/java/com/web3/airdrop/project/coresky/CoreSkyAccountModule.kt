package com.web3.airdrop.project.coresky

import android.content.Context
import android.view.LayoutInflater
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.TimeUtils
import com.web3.airdrop.R
import com.web3.airdrop.base.IProjectAccountModule
import com.web3.airdrop.databinding.ItemCoreskyWalletBinding
import com.web3.airdrop.extension.Extension.formatAddress
import com.web3.airdrop.project.coresky.data.CoreSkyUser

class CoreSkyAccountModule(val context: Context,val recyclerView: RecyclerView) : IProjectAccountModule<CoreSkyUser, ItemCoreskyWalletBinding>() {
    override fun initItemBinding(): ItemCoreskyWalletBinding {
        return ItemCoreskyWalletBinding.bind(LayoutInflater.from(context).inflate(R.layout.item_coresky_wallet,recyclerView,false))
    }

    override fun initRecyclerLayoutManager(): RecyclerView.LayoutManager {
        return LinearLayoutManager(context).apply {
            orientation = LinearLayoutManager.VERTICAL
        }
    }

    override fun initDiffCallback(): DiffUtil.ItemCallback<CoreSkyUser> {
        return object : DiffUtil.ItemCallback<CoreSkyUser>() {
            override fun areItemsTheSame(
                oldItem: CoreSkyUser,
                newItem: CoreSkyUser
            ): Boolean {
                return oldItem.wallet == newItem.wallet
            }

            override fun areContentsTheSame(
                oldItem: CoreSkyUser,
                newItem: CoreSkyUser
            ): Boolean {
                return oldItem.lastSyncTime == newItem.lastSyncTime
            }
        }
    }

    override fun initItemView(data: CoreSkyUser?, position: Int, itemDb: ItemCoreskyWalletBinding) {
        super.initItemView(data, position, itemDb)
        data?.let {
            itemDb.tvNo.text = "No.${position+1}"
            itemDb.ethAddress.text = it.wallet?.address?.formatAddress()
            itemDb.tvSyncTime.text = "SyncTime:${if (it.lastSyncTime == 0L) 0 else TimeUtils.getFriendlyTimeSpanByNow(it.lastSyncTime) }"
            itemDb.tvPoints.text = "Points:${it.score}"
            itemDb.tvFollowTwitter.text = "Twitter: false"
            itemDb.tvJoinDiscord.text = "Discord: false"
        }
    }
}