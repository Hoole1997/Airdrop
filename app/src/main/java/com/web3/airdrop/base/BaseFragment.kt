package com.web3.airdrop.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel

abstract class BaseFragment<V : ViewDataBinding, VM : ViewModel> : Fragment() {

    open lateinit var binding: V
    open lateinit var model: VM

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = initBinding(savedInstanceState)
        model = initViewModel()
        activity?.let {
            initView(it)
        }
        return binding.root
    }

    abstract fun initBinding(savedInstanceState: Bundle?): V

    abstract fun initViewModel() : VM

    abstract fun initView(activity: FragmentActivity)

}