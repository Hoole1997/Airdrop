package com.web3.airdrop.ui.dashboard

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ThreadUtils
import com.blankj.utilcode.util.ToastUtils
import com.web3.airdrop.base.BaseFragment
import com.web3.airdrop.data.AppDatabase
import com.web3.airdrop.databinding.FragmentDashboardBinding
import com.web3.airdrop.ui.home.HomeViewModel
import com.web3.airdrop.utils.ExcelUtils
import com.web3.airdrop.utils.FilePickerUtils.Companion.FILE_PICKER_REQUEST_CODE
import com.web3.airdrop.utils.FilePickerUtils.Companion.readTextFromUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DashboardFragment : BaseFragment<FragmentDashboardBinding, DashboardViewModel>() {

    override fun initBinding(savedInstanceState: Bundle?): FragmentDashboardBinding {
        return FragmentDashboardBinding.inflate(layoutInflater)
    }

//    override fun initViewModel(): DashboardViewModel {
//        return ViewModelProvider(this)[DashboardViewModel::class.java]
//    }

    override fun initView(activity: FragmentActivity) {
        binding.btnChoose.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "*/*" // 指定 MIME 类型为 text/plain
            }
            startActivityForResult(intent, FILE_PICKER_REQUEST_CODE)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == FILE_PICKER_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val uri: Uri? = data?.data
            uri?.let {
                val contentResolver = requireActivity().contentResolver
                val inputStream = contentResolver.openInputStream(uri)
                inputStream?.let {
                    lifecycleScope.launch(Dispatchers.IO) {
                        val list = ExcelUtils.parseExcel(it)
                        AppDatabase.getDatabase().walletDao().insertWalletList(list)
                    }
                }
            }
        }
    }

}