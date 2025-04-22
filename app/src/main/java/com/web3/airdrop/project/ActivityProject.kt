package com.web3.airdrop.project

import android.os.Bundle
import android.view.Menu
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.blankj.utilcode.util.BarUtils
import com.google.android.material.color.MaterialColors
import com.web3.airdrop.R
import com.web3.airdrop.base.BaseActivity
import com.web3.airdrop.base.BaseModel
import com.web3.airdrop.data.ProjectConfig
import com.web3.airdrop.databinding.ActivityProjectBinding

class ActivityProject : BaseActivity<ActivityProjectBinding>() {
    override fun initBinding(savedInstanceState: Bundle?): ActivityProjectBinding {
        return ActivityProjectBinding.inflate(layoutInflater)
    }

    override fun initView() {
        BarUtils.setStatusBarLightMode(this,true)
        BarUtils.setStatusBarColor(this, MaterialColors.getColor(this,
            com.google.android.material.R.attr.colorSurface,com.google.android.material.R.attr.colorSurface))
        val info = intent.getSerializableExtra("info") as ProjectConfig.ProjectInfo
        info.fragment()?.let {
            supportFragmentManager.beginTransaction()
                .replace(R.id.page_content,it)
                .commit()
        }
    }
}