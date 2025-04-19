package com.web3.airdrop.project.bless

import android.content.Context
import android.view.LayoutInflater
import android.widget.EditText
import androidx.annotation.Dimension
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.TimeUtils
import com.drake.net.utils.scopeNet
import com.web3.airdrop.R
import com.web3.airdrop.base.BaseModel
import com.web3.airdrop.base.IProjectAccountModule
import com.web3.airdrop.data.AppDatabase
import com.web3.airdrop.databinding.DialogBlessTokenEditBinding
import com.web3.airdrop.databinding.ItemBlessWalletBinding
import com.web3.airdrop.databinding.ItemCoreskyWalletBinding
import com.web3.airdrop.extension.Extension.formatAddress
import com.web3.airdrop.project.bless.data.BlessNodeInfo
import com.web3.airdrop.project.coresky.data.CoreSkyUser
import kotlinx.coroutines.Dispatchers

class BlessNodeListModule(val context: FragmentActivity, val recyclerView: RecyclerView, val model: BlessModel?) : IProjectAccountModule<BlessNodeInfo, ItemBlessWalletBinding>() {

    init {
        model?.walletAccountEvent?.observe(context) {
            refreshData(it)
        }
        model?.refreshLocalWallet()
    }

    override fun initItemBinding(): ItemBlessWalletBinding {
        return ItemBlessWalletBinding.bind(LayoutInflater.from(context).inflate(R.layout.item_bless_wallet,recyclerView,false))
    }

    override fun initRecyclerLayoutManager(): RecyclerView.LayoutManager {
        return LinearLayoutManager(context).apply {
            orientation = LinearLayoutManager.VERTICAL
        }
    }

    override fun initDiffCallback(): DiffUtil.ItemCallback<BlessNodeInfo> {
        return object : DiffUtil.ItemCallback<BlessNodeInfo>() {
            override fun areItemsTheSame(
                oldItem: BlessNodeInfo,
                newItem: BlessNodeInfo
            ): Boolean {
                return oldItem.wallet == newItem.wallet
            }

            override fun areContentsTheSame(
                oldItem: BlessNodeInfo,
                newItem: BlessNodeInfo
            ): Boolean {
                return oldItem.lastSyncTime == newItem.lastSyncTime
            }
        }
    }

    override fun initItemView(data: BlessNodeInfo?, position: Int, itemDb: ItemBlessWalletBinding) {
        super.initItemView(data, position, itemDb)
        data?.let {
            itemDb.tvNo.text = "No.${position+1}"
            itemDb.ethAddress.text = it.wallet?.address?.formatAddress()
            itemDb.tvSyncTime.text = "SyncTime:${if (it.lastSyncTime == 0L) 0 else TimeUtils.getFriendlyTimeSpanByNow(it.lastSyncTime) }"
            itemDb.tvPoints.text = "runing:${it.isConnected}"
            itemDb.tvFollowTwitter.text = "Twitter: ${it.xConnected}"
            itemDb.tvJoinDiscord.text = "Discord: ${it.discordConnected}"
            itemDb.llRoot.setBackgroundResource(if (it.isRegister()) R.drawable.registered_background else R.drawable.unregistered_background)
            itemDb.clContent.setOnClickListener {
                onClickListener?.invoke(data)
            }
            itemDb.btnEdit.setOnClickListener {
                showEditDialog(data)
            }
        }
    }

    private fun showEditDialog(nodeInfo: BlessNodeInfo) {
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_bless_token_edit,null)
        val etInput = view.findViewById<EditText>(R.id.et_input)
        AlertDialog.Builder(context)
            .setTitle("编辑")
            .setView(view)
            .setPositiveButton("保存") { dialog,_->
                val token = etInput.text.toString() ?:return@setPositiveButton
                nodeInfo.token = token
                scopeNet (Dispatchers.IO){
                    AppDatabase.getDatabase().blessNodeDao().insertOrUpdate(nodeInfo)
                }
                model?.panelCurrentAccountInfo?.value = nodeInfo
                model?.walletAccountEvent?.value?.forEach {
                    if (it.wallet?.address == nodeInfo.wallet?.address) {
                        it.token = nodeInfo.token
                    }
                }
            }
            .show()
    }

}