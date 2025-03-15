package com.web3.airdrop.project.layeredge

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.blankj.utilcode.util.UiMessageUtils
import com.web3.airdrop.base.BaseFragment
import com.web3.airdrop.databinding.FragmentLayeredgeAccountBinding
import okhttp3.internal.wait

class FragmentLayerEdgeAccount : BaseFragment<FragmentLayeredgeAccountBinding, LayerEdgeModel>() {

    lateinit var accountAdapter: LayerEdgeAccountAdapter

    override fun initBinding(savedInstanceState: Bundle?): FragmentLayeredgeAccountBinding {
        return FragmentLayeredgeAccountBinding.inflate(layoutInflater)
    }

    override fun initViewModel(): LayerEdgeModel {
        return ViewModelProvider(requireActivity())[LayerEdgeModel::class.java]
    }

    override fun initView(activity: FragmentActivity) {
        binding.rvWallet.layoutManager = LinearLayoutManager(activity).apply {
            orientation = RecyclerView.VERTICAL
        }
        val dividerItemDecoration = DividerItemDecoration(activity, LinearLayoutManager.VERTICAL)
        binding.rvWallet.addItemDecoration(dividerItemDecoration)
        accountAdapter = LayerEdgeAccountAdapter()
        binding.rvWallet.adapter = accountAdapter
        model.walletAccountEvent.observe(this) {
            accountAdapter.submitList(it)
        }
    }

}