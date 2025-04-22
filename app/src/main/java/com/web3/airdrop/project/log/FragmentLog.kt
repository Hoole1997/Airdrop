package com.web3.airdrop.project.log

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import com.blankj.utilcode.util.UiMessageUtils
import com.web3.airdrop.base.BaseFragment
import com.web3.airdrop.base.BaseModel
import com.web3.airdrop.base.BaseUser
import com.web3.airdrop.databinding.FragmentLogBinding

abstract class FragmentLog<VM: BaseModel<USER>, USER: BaseUser> : BaseFragment<FragmentLogBinding, VM>() {

    private lateinit var logAdapter: LogAdapter

    override fun initBinding(savedInstanceState: Bundle?): FragmentLogBinding {
        return FragmentLogBinding.inflate(layoutInflater)
    }

    override fun initView(activity: FragmentActivity) {
        logAdapter = LogAdapter()
        binding.rvLog.adapter = logAdapter
        logAdapter.submitList(arrayListOf())
        model?.logEvent?.observe(this) {
            logAdapter.submitList(it)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }

}