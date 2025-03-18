package com.web3.airdrop.project.somnia

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import com.chad.library.adapter4.BaseDifferAdapter
import com.chad.library.adapter4.viewholder.DataBindingHolder
import com.web3.airdrop.R
import com.web3.airdrop.databinding.ItemSomniaAccountBinding
import com.web3.airdrop.extension.Extension.formatAddress
import com.web3.airdrop.project.somnia.bean.SomniaAccount

class SomniaAdapter : BaseDifferAdapter<SomniaAccount, DataBindingHolder<ItemSomniaAccountBinding>>(object : DiffUtil.ItemCallback<SomniaAccount>() {
    override fun areItemsTheSame(
        oldItem: SomniaAccount,
        newItem: SomniaAccount
    ): Boolean {
        return oldItem.wallet == newItem.wallet
    }

    override fun areContentsTheSame(
        oldItem: SomniaAccount,
        newItem: SomniaAccount
    ): Boolean {
        return true
    }
}) {
    override fun onBindViewHolder(
        holder: DataBindingHolder<ItemSomniaAccountBinding>,
        position: Int,
        item: SomniaAccount?
    ) {
        item?.let {
            holder.binding.ethAddress.text = it.walletAddress.formatAddress()
            holder.binding.txCount.text = "Task: ${it.questsCompleted}"
            holder.binding.tokenBalance.text = "Logins: ${it.streakCount}"
//            holder.binding.registrationStatus.text = "UnRegister"
        }
    }

    override fun onCreateViewHolder(
        context: Context,
        parent: ViewGroup,
        viewType: Int
    ): DataBindingHolder<ItemSomniaAccountBinding> {
        return DataBindingHolder<ItemSomniaAccountBinding>(LayoutInflater.from(context).inflate(R.layout.item_somnia_account,parent,false))
    }
}