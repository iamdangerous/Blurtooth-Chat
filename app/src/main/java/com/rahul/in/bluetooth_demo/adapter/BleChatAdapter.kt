package com.rahul.`in`.bluetooth_demo.adapter

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.rahul.`in`.bluetooth_demo.R
import com.rahul.`in`.bluetooth_demo.model.BleChatMessage
import com.rahul.`in`.bluetooth_demo.viewHolder.BleChatMessageViewHolder

class BleChatAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>(){
    val  bleChatMessages = ArrayList<BleChatMessage>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return BleChatMessageViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_item_ble_chat_message,parent,false))
    }

    override fun getItemCount(): Int {
    return bleChatMessages.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val msg = bleChatMessages[position].bleMessage
        val vh = holder as BleChatMessageViewHolder
        val isOutbox = msg.isOutbox()

        val msgText = msg.message
        if(isOutbox){
            //align right
        }else{
            //align left
        }
        vh.tvTitle.text = msgText
    }
}
