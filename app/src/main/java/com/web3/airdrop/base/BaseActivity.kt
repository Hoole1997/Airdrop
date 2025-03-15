package com.web3.airdrop.base

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.ViewModel
import com.blankj.utilcode.util.BarUtils

abstract class BaseActivity<V : ViewDataBinding, VM : ViewModel> : AppCompatActivity() {

    open lateinit var binding: V
    open lateinit var model: VM

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = initBinding(savedInstanceState)
        setContentView(binding.root)
        model = initViewModel()
        initView()
    }

    abstract fun initBinding(savedInstanceState: Bundle?): V

    abstract fun initView()

    abstract fun initViewModel(): VM

    @SuppressLint("RestrictedApi")
    open fun useDefaultToolbar(toolbar: Toolbar, title: String) {
        BarUtils.setStatusBarLightMode(this,true)
        setSupportActionBar(toolbar)
        supportActionBar?.setDefaultDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = title
        toolbar.setNavigationOnClickListener {
            closePage()
        }
    }

    open fun closePage() {
        finish()
    }
}