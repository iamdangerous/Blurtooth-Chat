package com.rahul.`in`.bluetooth_demo.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.rahul.`in`.bluetooth_demo.R
import com.rahul.`in`.bluetooth_demo.model.BleChatMessage
import com.rahul.`in`.bluetooth_demo.viewHolder.BleChatMessageViewHolder
import com.rahul.`in`.bluetooth_demo.viewModel.BleMessageListViewModel

class BleChatAdapter :RecyclerView.Adapter<RecyclerView.ViewHolder>(){
    val  bleChatMessages = ArrayList<BleChatMessage>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return BleChatMessageViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_item_ble_chat_message,parent,false))
    }

    override fun getItemCount(): Int {
    return bleChatMessages.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

    }
}
