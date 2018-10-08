package com.rahul.`in`.bluetooth_demo.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.polidea.rxandroidble2.RxBleDevice
import com.rahul.`in`.bluetooth_demo.R

class BleDevicesAdapter(val devices:ArrayList<RxBleDevice>) :RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    var callback:BleAdapterCallback? = null
    val connectedDevices = HashSet<RxBleDevice>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return BleViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_item_ble_chat_message,parent,false))
    }

    override fun getItemCount(): Int {
        return devices.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val vh = holder as BleViewHolder
        val device = devices[position]
        device.macAddress?.let { vh.tvTitle.text = it }
        device.name?.let { vh.tvTitle.text = it }
        vh.btnConnect.setOnClickListener { callback?.onItemClick(device) }
        if(connectedDevices.contains(device)){
            vh.btnConnect.text = "Connected"
        }else{
            vh.btnConnect.text = "Connect"
        }
    }

    inner class BleViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){
        val tvTitle:TextView = itemView.findViewById(R.id.tvTitle)
        val btnConnect:Button = itemView.findViewById(R.id.btnConnect)
    }

    interface BleAdapterCallback{
        fun onItemClick(bleDevice: RxBleDevice)
    }
}