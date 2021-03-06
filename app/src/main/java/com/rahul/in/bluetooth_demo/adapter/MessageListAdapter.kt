package com.rahul.`in`.bluetooth_demo.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rahul.`in`.bluetooth_demo.R
import com.rahul.`in`.bluetooth_demo.room.entity.BleMessage
import com.rahul.`in`.bluetooth_demo.viewHolder.MessageListViewHolder

class MessageListAdapter(val bleMessageList: ArrayList<BleMessage>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    fun setData(bleMessageList: ArrayList<BleMessage>) {
        this.bleMessageList.clear()
        bleMessageList.addAll(bleMessageList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.list_item_message, parent, false)
        return MessageListViewHolder(v)
    }

    override fun getItemCount(): Int {
        return bleMessageList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val vh = holder as MessageListViewHolder
        val message = bleMessageList[position]
        val name = message.otherName
        vh.tvTitle.text = name
        vh.tvSubtitle.text = message.message

    }
}