package com.web3.airdrop.base

import androidx.fragment.app.Fragment

interface IPanelModule {

    fun initFragment() : Fragment?

    fun initTabName() : String

}