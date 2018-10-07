package com.rahul.`in`.bluetooth_demo.bleControllers

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import com.polidea.rxandroidble2.RxBleClient
import com.polidea.rxandroidble2.RxBleConnection
import com.polidea.rxandroidble2.RxBleDevice
import com.polidea.rxandroidble2.scan.ScanFilter
import com.polidea.rxandroidble2.scan.ScanResult
import com.polidea.rxandroidble2.scan.ScanSettings
import com.rahul.`in`.bluetooth_demo.App
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable

class RxBleController (rxPermissions: RxPermissions, context: Context, mBluetoothManager: BluetoothManager, mBluetoothAdapter: BluetoothAdapter) : BaseBleController(rxPermissions,context,mBluetoothManager,mBluetoothAdapter){

    private var scanDisposable: Disposable? = null
    val rxBleClient :RxBleClient
    val bleDevicesSet = HashSet<RxBleDevice>()
    var callback:RxBleControllerCallback? = null
    init {
        rxBleClient = (context.applicationContext as App).rxBleClient
    }

    override fun startScanProcess() {
        super.startScanProcess()

        if (isScanning()) {
            scanDisposable?.dispose()
            callback?.print("stopScanProcess")
            scanDisposable = null
        } else {
            callback?.print("startScanProcess")
            scanDisposable = rxBleClient.scanBleDevices(
                    ScanSettings.Builder()
                            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                            .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
                            .build(),
                    ScanFilter.Builder()
                            .build()
            )
                    .observeOn(AndroidSchedulers.mainThread())
                    .doFinally { dispose() }
                    .subscribe ({
                        addScanResult(it)
                    },{
                        onScanFailure(it)
                    })
        }
    }

    fun dispose(){

    }

    fun addScanResult(scanResult: ScanResult){
        if(!bleDevicesSet.contains(scanResult.bleDevice)){
            bleDevicesSet.add(scanResult.bleDevice)
            callback?.print("New device added")
            connectRxBle(scanResult.bleDevice)
        }
    }

    fun onScanFailure(it:Throwable){
        callback?.print("Error, ${it.message}")
    }

    fun connectRxBle(rxBleDevice:RxBleDevice){
        if(isConnected(rxBleDevice)){
            callback?.print("Already connected")
        }else{
            rxBleDevice.establishConnection(true)
                    .observeOn(AndroidSchedulers.mainThread())
                    .doFinally{dispose()}
                    .subscribe({
                        callback?.print("Connection success")
                    },{
                        callback?.print("Connection Error")
                    });
        }
    }

    internal fun isConnected(bleDevice:RxBleDevice): Boolean {
        return bleDevice.getConnectionState() == RxBleConnection.RxBleConnectionState.CONNECTED
    }

    internal fun isScanning(): Boolean {
        return scanDisposable != null
    }

    interface RxBleControllerCallback{
        fun print(message:String)
    }
}