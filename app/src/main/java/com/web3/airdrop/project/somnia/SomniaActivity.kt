package com.web3.airdrop.project.somnia

import android.os.Bundle
import android.view.Menu
import androidx.lifecycle.ViewModelProvider
import com.web3.airdrop.R
import com.web3.airdrop.base.BaseActivity
import com.web3.airdrop.databinding.ActivitySomniaBinding

class SomniaActivity : BaseActivity<ActivitySomniaBinding, SomniaViewModel>() {

    private lateinit var accountSomniaAdapter: SomniaAdapter

    override fun initBinding(savedInstanceState: Bundle?): ActivitySomniaBinding {
        return ActivitySomniaBinding.inflate(layoutInflater)
    }

    override fun initView() {
        useDefaultToolbar(binding.toolbar,"Somnia")

        accountSomniaAdapter = SomniaAdapter()
        binding.rvAccount.adapter = accountSomniaAdapter
        model.accountEvent.observe(this) {
            accountSomniaAdapter.submitList(it)
        }
        model.loadLocalAccount()
    }

    override fun initViewModel(): SomniaViewModel {
        return ViewModelProvider(this)[SomniaViewModel::class.java]
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.somnia_menu, menu)
        return true
    }
}