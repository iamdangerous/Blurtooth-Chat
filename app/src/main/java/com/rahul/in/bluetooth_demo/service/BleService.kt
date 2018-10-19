package com.rahul.`in`.bluetooth_demo.service

import android.app.Service
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.os.*
import com.rahul.`in`.bluetooth_demo.bleControllers.BleMeshController
import com.rahul.`in`.bluetooth_demo.eventBus.EventBleConnectionUpdated
import com.rahul.`in`.bluetooth_demo.eventBus.EventOnBleDeviceAdded
import com.rahul.`in`.bluetooth_demo.eventBus.EventPrintMessage
import com.rahul.`in`.bluetooth_demo.viewModel.BleUserViewModel
import org.greenrobot.eventbus.EventBus
import timber.log.Timber

class BleService:Service(), BleMeshController.BleMeshControllerCallback {

    private var mServiceLooper: Looper? = null
    var mServiceHandler: ServiceHandler? = null
    private var mBinder = LocalBinder()
    var mBleMeshController : BleMeshController? = null

    inner class ServiceHandler(looper: Looper) : Handler(looper) {

        override fun handleMessage(msg: Message) {
            //Do nothing
        }
    }

    override fun onCreate() {
        super.onCreate()
        Timber.d("onCreate")

        HandlerThread("ServiceStartArguments", Process.THREAD_PRIORITY_BACKGROUND).apply {
            start()

            // Get the HandlerThread's Looper and use it for our Handler
            mServiceLooper = looper
            mServiceHandler = ServiceHandler(looper)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Timber.d("onStartCommand")
        return START_STICKY
    }

    override fun onBind(p0: Intent?): IBinder? {
        return mBinder
    }

    inner class LocalBinder : Binder() {
        // Return this instance of LocalService so clients can call public methods
        fun getService(): BleService = this@BleService
    }

    override fun onLowMemory() {
        super.onLowMemory()
        Timber.d("onLowMemory")
    }

    override fun onDestroy() {
        super.onDestroy()
        Timber.d("onDestroy")
    }

    fun startScan(){
        if(mBleMeshController == null) {
            val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            mBleMeshController = BleMeshController(this, bluetoothManager, bluetoothManager.adapter)
            mBleMeshController?.callback = this@BleService
            mBleMeshController?.startScanProcess()
        }
    }

    override fun print(message: String) {
        EventBus.getDefault().post(EventPrintMessage(message))
    }

    override fun onDeviceAdded(added: Boolean, bleDevice: BluetoothDevice) {
        EventBus.getDefault().post((EventOnBleDeviceAdded(added, bleDevice)))
    }

    override fun onConnectionUpdated(connectedBleDevicesSet: HashSet<BluetoothDevice>) {
        EventBus.getDefault().post(EventBleConnectionUpdated(connectedBleDevicesSet))
    }

    override fun onScanStarted(scanStarted: Boolean) {
        mServiceHandler?.postDelayed({mBleMeshController?.onResume()}, 2000L)
    }


}