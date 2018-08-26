package com.rahul.`in`.bluetooth_demo

import android.app.Application
import com.polidea.rxandroidble2.RxBleClient
import com.polidea.rxandroidble2.internal.RxBleLog

class App : Application() {

    lateinit var rxBleClient: RxBleClient
    override fun onCreate() {
        super.onCreate()
        rxBleClient = RxBleClient.create(this)
        RxBleClient.setLogLevel(RxBleLog.DEBUG);

    }
}