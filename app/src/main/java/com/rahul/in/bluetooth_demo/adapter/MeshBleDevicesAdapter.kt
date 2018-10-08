package com.rahul.`in`.bluetooth_demo.adapter

import android.bluetooth.BluetoothDevice
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.polidea.rxandroidble2.RxBleDevice
import com.rahul.`in`.bluetooth_demo.R

class MeshBleDevicesAdapter(val devices : ArrayList<BluetoothDevice>) :RecyclerView.Adapter<RecyclerView.ViewHolder>(){
    val connectedDevices = HashSet<BluetoothDevice>()
    var callback: BleAdapterCallback? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return RxBleDevicesAdapter.BleViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_item_ble_chat_message, parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val vh = holder as RxBleDevicesAdapter.BleViewHolder
        val device = devices[position]
        device.address?.let { vh.tvTitle.text = it }
        device.name?.let { vh.tvTitle.text = it }
        vh.btnConnect.setOnClickListener { callback?.onItemClick(device) }
        if(connectedDevices.contains(device)){
            vh.btnConnect.text = "Connected"
        }else{
            vh.btnConnect.text = "Connect"
        }
    }

    override fun getItemCount(): Int {
        return devices.size
    }

    interface BleAdapterCallback{
        fun onItemClick(bleDevice: BluetoothDevice)
    }

}