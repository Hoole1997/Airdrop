package com.web3.airdrop.project.log

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import com.chad.library.adapter4.BaseDifferAdapter
import com.chad.library.adapter4.viewholder.QuickViewHolder
import com.web3.airdrop.R
import com.web3.airdrop.bean.Web3Project

class LogAdapter : BaseDifferAdapter<LogData, QuickViewHolder>(object : DiffUtil.ItemCallback<LogData>() {
    override fun areItemsTheSame(
        oldItem: LogData,
        newItem: LogData
    ): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(
        oldItem: LogData,
        newItem: LogData
    ): Boolean {
        return oldItem.content == newItem.content
    }
}) {
    override fun onBindViewHolder(
        holder: QuickViewHolder,
        position: Int,
        item: LogData?
    ) {
        item?.let {
            holder.setText(R.id.tv_project, Web3Project.getProjectName(it.projectId)+":")
            holder.setText(R.id.tv_account,"[${it.accountId}]")
            holder.setText(R.id.tv_content,it.content)
            when (it.level) {
                LogData.Level.ERROR -> {
                    // 设置为错误颜色，红色
                    holder.setTextColor(R.id.tv_project, context.getColor(android.R.color.holo_red_light))
                    holder.setTextColor(R.id.tv_account, context.getColor(android.R.color.holo_red_light))
                    holder.setTextColor(R.id.tv_content, context.getColor(android.R.color.holo_red_light))
                }
                LogData.Level.WARN -> {
                    // 设置为警告颜色，黄色
                    holder.setTextColor(R.id.tv_project, context.getColor(android.R.color.holo_orange_light)) // 黄色
                    holder.setTextColor(R.id.tv_account, context.getColor(android.R.color.holo_orange_light)) // 黄色
                    holder.setTextColor(R.id.tv_content, context.getColor(android.R.color.holo_orange_light)) // 黄色
                }
                LogData.Level.SUCCESS -> {
                    // 设置为成功颜色，绿色
                    holder.setTextColor(R.id.tv_project, context.getColor(android.R.color.holo_green_light))
                    holder.setTextColor(R.id.tv_account, context.getColor(android.R.color.holo_green_light))
                    holder.setTextColor(R.id.tv_content, context.getColor(android.R.color.holo_green_light))
                }
                else -> {
                    // 处理其他情况，如果需要的话
                    holder.setTextColor(R.id.tv_project, context.getColor(android.R.color.darker_gray)) // 默认灰色
                    holder.setTextColor(R.id.tv_account, context.getColor(android.R.color.darker_gray)) // 默认灰色
                    holder.setTextColor(R.id.tv_content, context.getColor(android.R.color.darker_gray)) // 默认灰色
                }
            }
        }
    }

    override fun onCreateViewHolder(
        context: Context,
        parent: ViewGroup,
        viewType: Int
    ): QuickViewHolder {
        return QuickViewHolder(LayoutInflater.from(context).inflate(R.layout.item_log,parent,false))
    }
}