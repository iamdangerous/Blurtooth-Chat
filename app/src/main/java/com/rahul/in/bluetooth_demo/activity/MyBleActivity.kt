package com.rahul.`in`.bluetooth_demo.activity

import android.os.Bundle
import com.polidea.rxandroidble2.scan.ScanFilter
import com.polidea.rxandroidble2.scan.ScanSettings
import com.rahul.`in`.bluetooth_demo.R
import kotlinx.android.synthetic.main.activity_main.*

class MyBleActivity : BleBaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun setClicks(){
        btnTurnOnBlutooth.setOnClickListener { setupBluetooth() }
        btnStartScan.setOnClickListener { startScan() }
    }

    fun startScan() {

        if (scanSubscription != null) {
            scanSubscription!!.dispose()
            scanSubscription = null
        }
        scanSubscription = rxBleClient
                .scanBleDevices(ScanSettings.Builder().build(), ScanFilter.empty())
                .subscribe({
                    if (it == null) {
                        toast("error in discovery")
                        printLogInScreen("error in discovery")
                    } else {
                        val macAddress = "macAddress = ${it.bleDevice?.macAddress}"
                        log(macAddress)
                        if (it.bleDevice?.bluetoothDevice != null) {
                            val deviceAdded = nearbyDevices.add(it.bleDevice?.bluetoothDevice!!)
                            if (deviceAdded) {
                                val text = "found devices ${macAddress}"
                                printLogInScreen(text)
                            }
                        }
                    }
                }, {
                    toast(it.localizedMessage)
                })

    }


    fun stopScan() {
        if (scanSubscription != null) {
            scanSubscription!!.dispose()
            scanSubscription = null
        }
        toast("Scanning stopped")
    }

    override fun enableDiscoverablity() {
        startScan()
    }


}
