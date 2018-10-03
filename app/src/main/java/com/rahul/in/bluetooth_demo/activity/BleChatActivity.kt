package com.rahul.`in`.bluetooth_demo.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.rahul.`in`.bluetooth_demo.R
import com.rahul.`in`.bluetooth_demo.adapter.BleChatAdapter
import com.rahul.`in`.bluetooth_demo.viewModel.BleChatViewModel
import kotlinx.android.synthetic.main.activity_ble_chat.*

class BleChatActivity : AppCompatActivity() {

    lateinit var rvAdapter:BleChatAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ble_chat)

        rvAdapter = BleChatAdapter()
        rv_message.layoutManager = LinearLayoutManager(this)

        ViewModelProviders.of(this).get(BleChatViewModel::class.java)

    }
}
