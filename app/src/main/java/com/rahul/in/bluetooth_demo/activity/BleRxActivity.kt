package com.rahul.`in`.bluetooth_demo.activity

import android.bluetooth.BluetoothAdapter
import android.os.Bundle
import com.rahul.`in`.bluetooth_demo.R
import com.rahul.`in`.bluetooth_demo.bleControllers.BleMeshController
import com.tbruyelle.rxpermissions2.RxPermissions
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.widget.Button
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.polidea.rxandroidble2.RxBleDevice
import com.rahul.`in`.bluetooth_demo.activity.BleBaseActivity
import com.rahul.`in`.bluetooth_demo.adapter.BleDevicesAdapter
import com.rahul.`in`.bluetooth_demo.bleControllers.RxBleController
import kotlinx.android.synthetic.main.activity_main.*


class BleRxActivity : BleBaseActivity(), RxBleController.RxBleControllerCallback {


    lateinit var btnTurnOnBlutooth:Button
    lateinit var btnStartScan:Button
    var bleController:RxBleController? = null
    lateinit var rvDevices:RecyclerView
    lateinit var devicesAdapter:BleDevicesAdapter
    val bleDevices = ArrayList<RxBleDevice>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        rvDevices = findViewById(R.id.rv_devices)
        btnTurnOnBlutooth = findViewById(R.id.btnTurnOnBlutooth)
        btnStartScan = findViewById(R.id.btnStartScan)

        rvDevices.layoutManager = LinearLayoutManager(this)


        devicesAdapter = BleDevicesAdapter(bleDevices)

        devicesAdapter.callback = object :BleDevicesAdapter.BleAdapterCallback{
            override fun onItemClick(bleDevice: RxBleDevice) {
                //connect
                printLogInScreen("Connecting with with ${bleDevice.name}")
                bleController?.connectRxBle(bleDevice)
            }

        }

        rvDevices.adapter = devicesAdapter
        setClicks()

    }

    override fun setClicks(){
        btnTurnOnBlutooth.setOnClickListener {
            setupBluetooth()
            val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            if(bleController == null) {
                bleController = RxBleController(RxPermissions(this), this, bluetoothManager, bluetoothManager.adapter)
                bleController?.callback = this@BleRxActivity
                enableDiscoverablity()
            }
        }
        btnStartScan.setOnClickListener {
            bleController?.checkPermissions()
        }
    }

    override fun onDeviceAdded(added: Boolean, bleDevice: RxBleDevice) {
        bleDevices.add(bleDevice)
        devicesAdapter.connectedDevices.clear()
        devicesAdapter.connectedDevices.addAll(bleController!!.bleDevicesSet)
        devicesAdapter.notifyDataSetChanged()
    }

    override fun print(message: String) {
        printLogInScreen(message)
    }

    override fun printLogInScreen(msg: String) {
        runOnUiThread {
            var text = tvLog.text
            val newMsg = msg + "\n" + text.toString()
            tvLog.text = newMsg
        }
    }

    override fun enableDiscoverablity() {
        printLogInScreen("enable discover mode")
        val discoverableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 3600)
        startActivity(discoverableIntent)
    }

}