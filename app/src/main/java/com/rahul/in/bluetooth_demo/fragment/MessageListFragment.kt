package com.rahul.`in`.bluetooth_demo.fragment

import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.rahul.`in`.bluetooth_demo.R
import com.rahul.`in`.bluetooth_demo.adapter.MessageListAdapter
import com.rahul.`in`.bluetooth_demo.room.entity.BleMessage
import com.rahul.`in`.bluetooth_demo.viewModel.BleMessageListViewModel

class MessageListFragment : BaseFragment() {

    lateinit var rvMessages: RecyclerView
    lateinit var rvAdapter: MessageListAdapter
    val messages = arrayListOf<BleMessage>()

    private lateinit var bleMessageVM: BleMessageListViewModel

    override fun setupViews() {
        super.setupViews()
        rvMessages = rootView.findViewById(R.id.rv_message)
        rvMessages.layoutManager = LinearLayoutManager(activity)
        rvAdapter = MessageListAdapter(messages)
        rvMessages.adapter = rvAdapter

        bleMessageVM = ViewModelProviders.of(this).get(BleMessageListViewModel::class.java)

        bleMessageVM.allBleMessages.observe(this, Observer {
            messages -> messages?.let { rvAdapter.setData(messages) }
        })


    }
}