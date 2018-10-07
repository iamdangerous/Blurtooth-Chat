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
import com.rahul.`in`.bluetooth_demo.activity.BleBaseActivity
import com.rahul.`in`.bluetooth_demo.bleControllers.RxBleController
import kotlinx.android.synthetic.main.activity_main.*


class BleRxActivity : BleBaseActivity(), RxBleController.RxBleControllerCallback {

    lateinit var btnTurnOnBlutooth:Button
    lateinit var btnStartScan:Button
    var bleController:RxBleController? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnTurnOnBlutooth = findViewById(R.id.btnTurnOnBlutooth)
        btnStartScan = findViewById(R.id.btnStartScan)

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