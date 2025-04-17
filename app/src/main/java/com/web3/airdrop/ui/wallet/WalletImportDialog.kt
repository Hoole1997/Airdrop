package com.web3.airdrop.ui.wallet

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ThreadUtils
import com.blankj.utilcode.util.ToastUtils
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.web3.airdrop.R
import com.web3.airdrop.databinding.DialogWalletImportBinding
import com.web3.airdrop.utils.FilePickerUtils.Companion.FILE_PICKER_REQUEST_CODE
import com.web3.airdrop.utils.FilePickerUtils.Companion.readTextFromUri

class WalletImportDialog(val model: WalletViewModel?) : BottomSheetDialogFragment() {

    private lateinit var binding: DialogWalletImportBinding

    private val chainList = arrayListOf<String>("ETH", "SOL", "BTC")
    private var currentChain = chainList[0]

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_wallet_import, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = DialogWalletImportBinding.bind(view)

        activity?.let { act ->
            binding.btnFileImport.setOnClickListener {
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "text/plain" // 指定 MIME 类型为 text/plain
                }
                startActivityForResult(intent, FILE_PICKER_REQUEST_CODE)
//                FilePickerUtils.startFilePicker(act,FILE_PICKER_REQUEST_CODE)
            }
            binding.btnImport.setOnClickListener {
                val content = binding.etInput.text.toString()
                if (content.isEmpty()) {
                    ToastUtils.showShort("内容不能为空")
                    return@setOnClickListener
                }
                model?.insertWallet(content)
                ToastUtils.showShort("导入成功")
                dismiss()
            }
        }

    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == FILE_PICKER_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val uri: Uri? = data?.data
            uri?.let {
                ThreadUtils.executeByIo<String>(object : ThreadUtils.Task<String>() {
                    override fun doInBackground(): String? {
                        return readTextFromUri(it, requireActivity())
                    }

                    override fun onSuccess(result: String?) {
                        // 处理文件内容
                        binding.etInput.setText(result)
                    }

                    override fun onCancel() {
                        LogUtils.i("onCancel")
                    }

                    override fun onFail(t: Throwable?) {
                        ToastUtils.showShort("解析失败 ${t?.message}")
                    }

                })
            }
        }
    }

}