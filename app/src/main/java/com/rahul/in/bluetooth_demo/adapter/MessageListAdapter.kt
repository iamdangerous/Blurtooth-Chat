package com.rahul.`in`.bluetooth_demo.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.rahul.`in`.bluetooth_demo.R
import com.rahul.`in`.bluetooth_demo.room.entity.Message
import com.rahul.`in`.bluetooth_demo.viewHolder.MessageListViewHolder

class MessageListAdapter(val messageList: ArrayList<Message>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.list_item_message,parent,false)
        return MessageListViewHolder(v)
    }

    override fun getItemCount(): Int {
        return messageList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val vh = holder as MessageListViewHolder
        val message = messageList[position]
        if(message.isOutbox()){
            val name = message.to
            vh.tvTitle.text = name
        }else{
            vh.tvTitle.text = message.from
        }
        vh.tvSubtitle.text = message.message

    }
}