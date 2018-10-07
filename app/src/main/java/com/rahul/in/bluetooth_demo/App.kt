package com.rahul.`in`.bluetooth_demo

import android.app.Application
import com.polidea.rxandroidble2.RxBleClient
import com.polidea.rxandroidble2.internal.RxBleLog
import com.rahul.`in`.bluetooth_demo.util.MyDebugTree
import timber.log.Timber



class App : Application() {

    lateinit var rxBleClient: RxBleClient
    override fun onCreate() {
        super.onCreate()
        rxBleClient = RxBleClient.create(this)
        RxBleClient.setLogLevel(RxBleLog.DEBUG);
        setupTimber()
    }

    fun setupTimber(){
        if (BuildConfig.DEBUG) {
            Timber.plant(MyDebugTree())
        } else {
            //TODO plant your Production Tree
        }
    }
}