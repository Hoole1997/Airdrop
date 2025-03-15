package com.web3.airdrop.project.layeredge

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.blankj.utilcode.util.ToastUtils
import com.blankj.utilcode.util.UiMessageUtils
import com.google.android.material.tabs.TabLayoutMediator
import com.web3.airdrop.R
import com.web3.airdrop.base.BaseActivity
import com.web3.airdrop.databinding.ActivityLayerEdgeBinding
import com.web3.airdrop.project.log.LogViewModel

class ActivityLayerEdge : BaseActivity<ActivityLayerEdgeBinding, LayerEdgeModel>() {

    private lateinit var logViewModel: LogViewModel

    override fun initBinding(savedInstanceState: Bundle?): ActivityLayerEdgeBinding {
        return ActivityLayerEdgeBinding.inflate(layoutInflater)
    }

    @SuppressLint("RestrictedApi")
    override fun initView() {
        useDefaultToolbar(binding.toolBar,"LayerEdge")

        binding.pageContent.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int {
                return 2
            }

            override fun createFragment(position: Int): Fragment {
                if (position == 0) {
                    return FragmentLayerEdgeAccount()
                } else {
                    return FragmentLayerEdgeLog()
                }
            }
        }
        TabLayoutMediator(binding.tab, binding.pageContent) { tab, position ->
            if (position == 0) {
                tab.text = "账号"
            } else {
                tab.text = "日志"
            }
        }.attach()

        startTask()
        model.refreshLocalWallet()
        model.walletAccountEvent.observe(this) {
            binding.tvRegisterAccount.text = "0/${it.size}"
        }

        var onlineCount = 0
        var countNodePoint = 0
        var countTaskPoint = 0
        UiMessageUtils.getInstance().addListener(LayerEdgeCommand.MESSAGE_REFRESH_ACCOUNT) {
            val account = it.`object` as LayerEdgeAccountInfo
            var count = model.walletAccountEvent.value?.size ?:0
            val list = model.walletAccountEvent.value?:arrayListOf()
            list.let { list ->
                list.forEachIndexed { index,data ->
                    if (account.wallet.address == data.wallet.address) {
                        list.set(index,account)
                        if (account.isRegister) {
                            onlineCount++
                            countNodePoint = countNodePoint+account.nodePoints
                            countTaskPoint = countTaskPoint+account.taskPoints
                        }
                    } else {
                        if (data.isRegister) {
                            onlineCount++
                            countNodePoint = data.nodePoints+account.nodePoints
                            countTaskPoint = data.taskPoints+account.taskPoints
                        }
                    }
                }
                count = list.size
                binding.tvRegisterAccount.text = "$onlineCount / $count"
                binding.tvCountNodePoint.text = "$countNodePoint"
                binding.tvCountTaskPoint.text = "$countTaskPoint"
                model.walletAccountEvent.postValue(list)
            }
        }

        logViewModel.registerLogListener(LayerEdgeCommand.MESSAGE_LAYEREDGE_LOG)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.layeredge_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_layeredge_sign -> {
                model.signEveryDay()
                true
            }
            R.id.menu_layeredge_register -> {
                model.registerAll()
                true
            }
            R.id.menu_layeredge_node_start -> {
                model.connectNode()
                true
            }
            R.id.menu_layeredge_refresh_info -> {
                model.allRequestAccountInfo()
                true
            }
            R.id.menu_layeredge_sort_register -> {
                model.sortRegister()
                true
            }
            R.id.menu_layeredge_refresh_node -> {
                model.refreshNodeState()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun initViewModel(): LayerEdgeModel {
        logViewModel = ViewModelProvider(this)[LogViewModel::class.java]
        return ViewModelProvider(this)[LayerEdgeModel::class.java]
    }

    private fun startTask() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            ToastUtils.showShort("暂时只支持安卓8.0以上")
            finish()
            return
        }
        startForegroundService(Intent(this, LayerEdgeService::class.java))
    }

    override fun onDestroy() {
        super.onDestroy()
        logViewModel.removeLogListener(LayerEdgeCommand.MESSAGE_LAYEREDGE_LOG)
    }

}