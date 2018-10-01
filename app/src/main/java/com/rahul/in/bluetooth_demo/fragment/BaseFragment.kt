package com.rahul.`in`.bluetooth_demo.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.rahul.`in`.bluetooth_demo.R

class BaseFragment:Fragment(){

    fun getLayout() = R.layout.fragment_base
    lateinit var rootView:View
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(getLayout(),container,false)
        setupViews()
        return rootView
    }

    protected fun setupViews(){

    }
}