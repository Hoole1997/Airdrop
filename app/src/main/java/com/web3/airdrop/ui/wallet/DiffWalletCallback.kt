package com.web3.airdrop.ui.wallet

import androidx.recyclerview.widget.DiffUtil
import com.web3.airdrop.data.Wallet

class DiffWalletCallback : DiffUtil.ItemCallback<Wallet>() {
    override fun areItemsTheSame(
        oldItem: Wallet,
        newItem: Wallet
    ): Boolean {
        return oldItem.address == newItem.address
    }

    override fun areContentsTheSame(
        oldItem: Wallet,
        newItem: Wallet
    ): Boolean {
        return oldItem.address == newItem.address &&
                oldItem.chain == newItem.chain &&
                oldItem.alias == newItem.alias &&
                oldItem.privateKey == newItem.privateKey
    }
}