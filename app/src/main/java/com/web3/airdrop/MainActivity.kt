package com.web3.airdrop

import android.Manifest
import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.blankj.utilcode.util.BarUtils
import com.blankj.utilcode.util.PermissionUtils
import com.web3.airdrop.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        BarUtils.setStatusBarLightMode(this,true)
        BarUtils.transparentNavBar(this)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        NavigationUI.setupWithNavController(navView, navController)
        checkPermission()
    }

    private fun checkPermission() {
        if (!PermissionUtils.isGranted(Manifest.permission.POST_NOTIFICATIONS)) {
            PermissionUtils.permission(Manifest.permission.POST_NOTIFICATIONS).request()
        }
    }

}