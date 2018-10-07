package com.rahul.`in`.bluetooth_demo.activity

import android.os.Bundle
import com.rahul.`in`.bluetooth_demo.R
import com.rahul.`in`.bluetooth_demo.bleControllers.BleMeshController
import com.tbruyelle.rxpermissions2.RxPermissions
import android.bluetooth.BluetoothManager
import android.content.Context
import android.widget.Button
import kotlinx.android.synthetic.main.activity_main.*


class BleMeshActivity : BleBaseActivity(), BleMeshController.BleMeshControllerCallback {

    lateinit var btnTurnOnBlutooth:Button
    lateinit var btnStartScan:Button
    var bleMeshController:BleMeshController? = null
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
            bleMeshController = BleMeshController(RxPermissions(this),this, bluetoothManager, bluetoothManager.adapter)
            bleMeshController?.callback = this@BleMeshActivity
            enableDiscoverablity()
        }
        btnStartScan.setOnClickListener {
            bleMeshController?.checkPermissions()
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

}