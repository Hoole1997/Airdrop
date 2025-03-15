package com.web3.airdrop.project.log

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import com.blankj.utilcode.util.UiMessageUtils
import com.web3.airdrop.base.BaseFragment
import com.web3.airdrop.databinding.FragmentLogBinding

open abstract class FragmentLog : BaseFragment<FragmentLogBinding, LogViewModel>() {

    private lateinit var logAdapter: LogAdapter

    override fun initViewModel(): LogViewModel {
        return ViewModelProvider(requireActivity())[LogViewModel::class.java]
    }

    override fun initBinding(savedInstanceState: Bundle?): FragmentLogBinding {
        return FragmentLogBinding.inflate(layoutInflater)
    }

    abstract fun initProjectLogId() : Int

    override fun initView(activity: FragmentActivity) {
        logAdapter = LogAdapter()
        binding.rvLog.adapter = logAdapter
        logAdapter.submitList(arrayListOf())
        model.logEvent.observe(this) {
            logAdapter.submitList(it)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }

}