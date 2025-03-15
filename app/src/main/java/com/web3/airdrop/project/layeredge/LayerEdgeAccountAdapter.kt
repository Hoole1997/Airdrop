package com.web3.airdrop.project.layeredge

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import com.chad.library.adapter4.BaseDifferAdapter
import com.chad.library.adapter4.viewholder.DataBindingHolder
import com.web3.airdrop.R
import com.web3.airdrop.databinding.ItemLayeredgeWalletBinding

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
        return oldItem.wallet == newItem.wallet ||
                oldItem.isRegister == newItem.isRegister ||
                oldItem.layerEdgeId == newItem.layerEdgeId ||
                oldItem.nodeStart == newItem.nodeStart ||
                oldItem.nodePoints == newItem.nodePoints ||
                oldItem.taskPoints == newItem.taskPoints
    }
}) {
    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(
        holder: DataBindingHolder<ItemLayeredgeWalletBinding>,
        position: Int,
        item: LayerEdgeAccountInfo?
    ) {
        item?.let {
            holder.binding.tvSerial.text = (position+1).toString()
            holder.binding.tvAddress.text = it.wallet.address
            holder.binding.tvRegisterState.text = "isRegister:${it.isRegister}"
            holder.binding.tvLightNode.text = "LightNode:${it.nodeStart}"
        }
    }

    override fun onBindViewHolder(
        holder: DataBindingHolder<ItemLayeredgeWalletBinding>,
        position: Int,
        item: LayerEdgeAccountInfo?,
        payloads: List<Any>
    ) {
        super.onBindViewHolder(holder, position, item, payloads)
        item?.let {
            holder.binding.tvRegisterState.text = "isRegister:${it.isRegister}"
            holder.binding.tvLightNode.text = "LightNode:${it.nodeStart}"
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