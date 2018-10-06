package com.rahul.`in`.bluetooth_demo.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.rahul.`in`.bluetooth_demo.R
import com.rahul.`in`.bluetooth_demo.adapter.BleChatAdapter
import com.rahul.`in`.bluetooth_demo.room.entity.BleMessage
import com.rahul.`in`.bluetooth_demo.viewModel.BleChatViewModel
import kotlinx.android.synthetic.main.activity_ble_chat.*

class BleChatActivity : AppCompatActivity() {

    lateinit var rvAdapter: BleChatAdapter
    lateinit var bleChatViewModel: BleChatViewModel
    lateinit var otherId: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ble_chat)

        bleChatViewModel = ViewModelProviders.of(this).get(BleChatViewModel::class.java)
        rv_message.layoutManager = LinearLayoutManager(this)

        prepareDataFromIntent()
        prepareAdapter()
        setClicks()

    }

    fun prepareDataFromIntent() {
        otherId = "Ron"
    }

    fun prepareBleMessage(text: String) = BleMessage.createSendingMessage(text, otherId)

    fun sendMessage(msg: BleMessage) {

    }

    fun prepareAdapter() {
        rvAdapter = BleChatAdapter()
        rv_message.adapter = rvAdapter
        bleChatViewModel.allBleMessages.observe(this, Observer { allmessages ->
            allmessages?.let {
                rvAdapter.setData(it)
            }
        })
    }

    fun setClicks() {
        btnSend.setOnClickListener {
            val bleMessage = prepareBleMessage(et_msg.text.toString())
            et_msg.text.isNotEmpty().run {
                bleChatViewModel.insert(bleMessage)
                sendMessage(bleMessage)
            }
            et_msg.setText("")
        }
    }

}
