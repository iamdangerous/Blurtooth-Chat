package com.rahul.`in`.bluetooth_demo.activity

import android.bluetooth.BluetoothDevice
import android.os.Bundle
import com.rahul.`in`.bluetooth_demo.R
import com.rahul.`in`.bluetooth_demo.bleControllers.BleMeshController
import com.tbruyelle.rxpermissions2.RxPermissions
import android.bluetooth.BluetoothManager
import android.content.Context
import android.widget.Button
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.polidea.rxandroidble2.RxBleDevice
import com.rahul.`in`.bluetooth_demo.adapter.MeshBleDevicesAdapter
import com.rahul.`in`.bluetooth_demo.adapter.RxBleDevicesAdapter
import kotlinx.android.synthetic.main.activity_main.*
import java.util.HashSet


class BleMeshActivity : BleBaseActivity(), BleMeshController.BleMeshControllerCallback {

    lateinit var btnTurnOnBlutooth:Button
    lateinit var btnStartScan:Button
    var bleMeshController:BleMeshController? = null
    lateinit var rvDevices: RecyclerView
    lateinit var devicesAdapter: MeshBleDevicesAdapter
    val bleDevices = ArrayList<BluetoothDevice>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        rvDevices = findViewById(R.id.rv_devices)
        btnTurnOnBlutooth = findViewById(R.id.btnTurnOnBlutooth)
        btnStartScan = findViewById(R.id.btnStartScan)

        rvDevices.layoutManager = LinearLayoutManager(this)

        devicesAdapter = MeshBleDevicesAdapter(bleDevices)

        devicesAdapter.callback = object : MeshBleDevicesAdapter.BleAdapterCallback {
            override fun onItemClick(bleDevice: BluetoothDevice) {
//                printLogInScreen("Connecting with with ${bleDevice.name}")
//                bleMeshController?.connectRxBle(bleDevice)
            }
        }

        rvDevices.adapter = devicesAdapter
        setClicks()

    }

    override fun setClicks(){
        btnTurnOnBlutooth.setOnClickListener {
            setupBluetooth()
            val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            bleMeshController = BleMeshController(RxPermissions(this),this, bluetoothManager, bluetoothManager.adapter)
            bleMeshController?.callback = this@BleMeshActivity
            enableDiscoverablity()
        }
        btnStartScan.setOnClickListener {
            bleMeshController?.checkPermissions()
        }
    }

    override fun onDeviceAdded(added: Boolean, bleDevice: BluetoothDevice) {
        runOnUiThread {
            bleDevices.add(bleDevice)
            devicesAdapter.notifyDataSetChanged()
        }
    }

    override fun onConnectionUpdated(connectedBleDevicesSet: HashSet<BluetoothDevice>) {

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

}