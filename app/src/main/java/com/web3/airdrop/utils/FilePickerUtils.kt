package com.web3.airdrop.utils

import android.app.Activity
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ThreadUtils
import java.io.BufferedReader
import java.io.InputStreamReader

class FilePickerUtils {

    companion object {

        const val FILE_PICKER_REQUEST_CODE = 1

        // 注册文件选择器，并指定 MIME 类型为 text/plain
        fun registerFilePicker(activity: FragmentActivity, callback: (String?) -> Unit) {
            val filePickerLauncher: ActivityResultLauncher<Array<String>> =
                activity.registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
                    uri?.let {
                        ThreadUtils.executeByIo<String>(object : ThreadUtils.Task<String>() {
                            override fun doInBackground(): String? {
                                return readTextFromUri(it, activity)
                            }

                            override fun onSuccess(result: String?) {
                                callback(result) // 返回文件内容
                            }

                            override fun onCancel() {
                                LogUtils.i("onCancel")
                            }

                            override fun onFail(t: Throwable?) {
                                LogUtils.e(t?.message)
                            }

                        })

                    }
                }

            // 调用文件选择器，指定 MIME 类型为 text/plain
            filePickerLauncher.launch(arrayOf("text/plain"))
        }

        // 使用 startActivityForResult 方法启动文件选择器
        fun startFilePicker(activity: FragmentActivity, requestCode: Int) {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "text/plain" // 指定 MIME 类型为 text/plain
            }
            activity.startActivityForResult(intent, requestCode)
        }

        // 读取指定 URI 的文件内容
        fun readTextFromUri(uri: Uri, activity: Activity): String? {
            return try {
                val contentResolver = activity.contentResolver
                val inputStream = contentResolver.openInputStream(uri) ?: return null
                val reader = BufferedReader(InputStreamReader(inputStream))
                val stringBuilder = StringBuilder()
                reader.use {
                    it.forEachLine { line ->
                        stringBuilder.append(line).append("\n") // 拼接每行内容
                    }
                }
                inputStream.close()
                reader.close()
                stringBuilder.toString().trim() // 返回文件内容
            } catch (e: Exception) {
                Log.e("FilePickerUtils", "Error reading file", e)
                null
            }
        }

        // 获取文件名
        fun getFileName(uri: Uri, activity: Activity): String? {
            var fileName: String? = null
            val cursor: Cursor? = activity.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    fileName = it.getString(nameIndex)
                }
            }
            cursor?.close()
            return fileName
        }
    }
}
