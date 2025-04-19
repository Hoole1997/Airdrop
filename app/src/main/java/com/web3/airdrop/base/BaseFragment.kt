package com.web3.airdrop.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import com.blankj.utilcode.util.LogUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

abstract class BaseFragment<V : ViewDataBinding, VM : ViewModel> : Fragment() {

    open lateinit var binding: V
    open var model: VM? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = initBinding(savedInstanceState)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        val dialog = showLoadingDialog()
        lifecycleScope.launch() {
            model = initViewModel()
            withContext(Dispatchers.Main) {
//                dialog.dismiss()
                initView(requireActivity())
            }
        }
    }

    fun showLoadingDialog(): AlertDialog {
        val dialog = AlertDialog.Builder(requireActivity())
            .setTitle("Loading")
            .setMessage("Please wait...")
            .setCancelable(false)
            .create()
        dialog.show()
        return dialog
    }

    abstract fun initBinding(savedInstanceState: Bundle?): V

    open suspend fun initViewModel() : VM? {
        return null
    }

    abstract fun initView(activity: FragmentActivity)

}