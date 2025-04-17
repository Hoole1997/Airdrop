package com.web3.airdrop.ui.wallet

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.blankj.utilcode.util.ClipboardUtils
import com.blankj.utilcode.util.ToastUtils
import com.google.android.material.tabs.TabLayout
import com.web3.airdrop.R
import com.web3.airdrop.base.BaseFragment
import com.web3.airdrop.databinding.FragmentWalletBinding
import kotlin.getValue

class WalletFragment : BaseFragment<FragmentWalletBinding, WalletViewModel>(),
    TabLayout.OnTabSelectedListener {

    private val chainList = arrayListOf<String>("ETH", "SOL", "BTC")
    private lateinit var walletAdapter: WalletAdapter
    private var walletImportDialog: WalletImportDialog? = null

    private var editMode = false

    override fun initBinding(savedInstanceState: Bundle?): FragmentWalletBinding {
        return FragmentWalletBinding.inflate(layoutInflater)
    }

    override suspend fun initViewModel(): WalletViewModel {
        return ViewModelProvider(this)[WalletViewModel::class.java]
    }

    override fun initView(activity: FragmentActivity) {
        binding.tabChain.let {
            chainList.forEach { chain ->
                binding.tabChain.addTab(it.newTab().setText(chain))
            }
        }
        binding.tabChain.addOnTabSelectedListener(this)
        walletAdapter = WalletAdapter()
        walletAdapter.setOnItemClickListener { _, _, position ->
            val data = walletAdapter.getItem(position)

        }
        walletAdapter.addOnItemChildClickListener(R.id.iv_copy) { _, _, position ->
            val data = walletAdapter.getItem(position)
            ClipboardUtils.copyText(data?.address)
            ToastUtils.showShort("复制成功")
        }
        walletAdapter.addOnItemChildClickListener(R.id.iv_check) { _, _, position ->
            walletAdapter.getItem(position)?.let {
                walletAdapter.switchEditMode(!it.check, position)
                refreshChooseCount()
            }
        }
        binding.rvWallet.adapter = walletAdapter
        binding.btnImport.setOnClickListener {
            if (walletImportDialog == null) {
                walletImportDialog = WalletImportDialog(model)
            }
            walletImportDialog?.show(childFragmentManager, walletImportDialog?.tag)
        }
        binding.btnChoose.setOnClickListener {
            switchMode()
        }
        binding.btnDelete.setOnClickListener {
            val count = walletAdapter.items.count {
                it.check == true
            }
            AlertDialog.Builder(activity)
                .setTitle("提示")
                .setMessage("是否要删除 $count 个钱包 ?")
                .setPositiveButton("确定") { dialog,_,->
                    deleteWallet()
                    dialog.dismiss()
                }
                .setNegativeButton("取消") { dialog,_,->
                    dialog.dismiss()
                }
                .show()
        }
        model?.wallet?.observe(this) {
            walletAdapter.submitList(it)
        }
        model?.queryWallet(chainList[binding.tabChain.selectedTabPosition])
    }

    private fun deleteWallet() {
        model?.deleteWallet(walletAdapter.items.filter {
            it.check == true
        })
        switchMode()
    }

    private fun refreshChooseCount() {
        val count = walletAdapter.items.count {
            it.check == true
        }
        binding.btnDelete.text = "删除($count)"
    }

    private fun switchMode() {
        if (editMode) {
            editMode = false
            binding.btnChoose.text = "全选"
            binding.btnDelete.visibility = View.GONE
        } else {
            editMode = true
            binding.btnChoose.text = "取消"
            binding.btnDelete.visibility = View.VISIBLE
        }
        walletAdapter.switchEditMode(editMode)
        refreshChooseCount()
    }

    override fun onTabSelected(tab: TabLayout.Tab?) {
        tab?.let {
            model?.queryWallet(chainList[tab.position])
        }
    }

    override fun onTabUnselected(tab: TabLayout.Tab?) {

    }

    override fun onTabReselected(tab: TabLayout.Tab?) {

    }

}