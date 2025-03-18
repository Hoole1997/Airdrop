package com.web3.airdrop.project.layeredge

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import com.blankj.utilcode.util.UiMessageUtils
import com.web3.airdrop.base.BaseFragment
import com.web3.airdrop.data.AppDatabase
import com.web3.airdrop.databinding.FragmentLayeredgeAccountBinding
import com.web3.airdrop.project.layeredge.data.LayerEdgeAccountInfo

class FragmentLayerEdgeAccount : BaseFragment<FragmentLayeredgeAccountBinding, LayerEdgeModel>() {

    lateinit var accountAdapter: LayerEdgeAccountAdapter

    override fun initBinding(savedInstanceState: Bundle?): FragmentLayeredgeAccountBinding {
        return FragmentLayeredgeAccountBinding.inflate(layoutInflater)
    }

    override fun initViewModel(): LayerEdgeModel {
        return ViewModelProvider(requireActivity())[LayerEdgeModel::class.java]
    }

    override fun initView(activity: FragmentActivity) {
        accountAdapter = LayerEdgeAccountAdapter()
        binding.rvWallet.adapter = accountAdapter
        accountAdapter.setOnItemClickListener { _,_,position ->
            accountAdapter.getItem(position)?.let {
                LayerEdgeAccountBottomSheet(it).show(childFragmentManager,"")
            }
        }
        model.walletAccountEvent.observe(this) {
            accountAdapter.submitList(it)
        }
//        AppDatabase.getDatabase().layeredgeDao().getAccountList().observe(this) {
//
//            accountAdapter.submitList(it)
//        }
        UiMessageUtils.getInstance().addListener(LayerEdgeCommand.MESSAGE_REQUEST_ACCOUNT_RESULT) {
            val account = it.`object` as LayerEdgeAccountInfo
            accountAdapter.items.forEachIndexed { index,item ->
                if (item.wallet?.address == account.wallet?.address) {
                    accountAdapter.set(index,account)
                    accountAdapter.notifyItemChanged(index)
                }
            }
        }
    }

}