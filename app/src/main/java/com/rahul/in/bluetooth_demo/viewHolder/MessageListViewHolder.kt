package com.rahul.`in`.bluetooth_demo.viewHolder

import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.widget.TextView
import com.rahul.`in`.bluetooth_demo.R

class MessageListViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){

    val tvTitle:TextView
    val tvSubtitle:TextView
    init {
        tvTitle = itemView.findViewById(R.id.tv_title)
        tvSubtitle = itemView.findViewById(R.id.tv_subtitle)
    }
}