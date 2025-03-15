package com.web3.airdrop.ui.wallet

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.chad.library.adapter4.BaseDifferAdapter
import com.chad.library.adapter4.viewholder.DataBindingHolder
import com.web3.airdrop.R
import com.web3.airdrop.data.Wallet
import com.web3.airdrop.databinding.ItemWalletBinding

class WalletAdapter : BaseDifferAdapter<Wallet, DataBindingHolder<ItemWalletBinding>>(
    DiffWalletCallback()
) {

    private var editMode = false

    override fun onBindViewHolder(
        holder: DataBindingHolder<ItemWalletBinding>,
        position: Int,
        item: Wallet?
    ) {
        item?.let {
            holder.binding.tvSerial.text = position.plus(1).toString()
            holder.binding.tvAddress.text = it.address
            holder.binding.ivCheck.setImageResource(if (it.check) android.R.drawable.checkbox_on_background else android.R.drawable.checkbox_off_background)
        }
    }

    override fun onBindViewHolder(
        holder: DataBindingHolder<ItemWalletBinding>,
        position: Int,
        item: Wallet?,
        payloads: List<Any>
    ) {
        super.onBindViewHolder(holder, position, item, payloads)
        if (item == null)return
        if (editMode) {
            holder.binding.ivCheck.isVisible = true
            holder.binding.ivCheck.setImageResource(if (item.check) android.R.drawable.checkbox_on_background else android.R.drawable.checkbox_off_background)
        } else {
            holder.binding.ivCheck.isVisible = false
            holder.binding.ivCheck.setImageResource(android.R.drawable.checkbox_off_background)
        }
    }

    override fun onCreateViewHolder(
        context: Context,
        parent: ViewGroup,
        viewType: Int
    ): DataBindingHolder<ItemWalletBinding> {
        return DataBindingHolder<ItemWalletBinding>(
            LayoutInflater.from(context).inflate(R.layout.item_wallet, parent,false)
        )
    }

    fun switchEditMode(editMode: Boolean) {
        this.editMode = editMode
        items.forEach {
            it.check = editMode
        }
        notifyItemRangeChanged(0,itemCount,editMode)
    }

    fun switchEditMode(check: Boolean,position: Int) {
        val itemData = getItem(position)
        itemData?.check = check
        notifyItemChanged(position,itemData)
    }

}