package com.rahul.`in`.bluetooth_demo.bleControllers

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager
import android.Manifest.permission
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.annotation.SuppressLint
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.app.ActivityCompat.startActivityForResult
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.tbruyelle.rxpermissions2.RxPermissions
import android.view.InputDevice.getDevice
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.*
import timber.log.Timber


class BleMeshController (val rxPermissions: RxPermissions, val context: Context, val bleManager:BluetoothManager, val mBluetoothAdapter:BluetoothAdapter){

    var hasPermissions = false
    var mScanning = false
    val REQUEST_ENABLE_BT = 100
    var mScanCallback : ScanCallback? = null
    val mScanResults = HashMap<String, BluetoothDevice>();
    var mBluetoothLeScanner  : BluetoothLeScanner? = null;

    fun startScanProcess(){
        if(mScanning){
            return
        }
        val filters = ArrayList<ScanFilter>()
        val settings = ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
                .build()

        mScanCallback = BtleScanCallback()
        mBluetoothLeScanner = mBluetoothAdapter.bluetoothLeScanner
        mBluetoothLeScanner!!.startScan(mScanCallback)

        mScanning = true
    }

    fun stopScan(){
        if (mScanning && mBluetoothAdapter != null && mBluetoothAdapter.isEnabled() && mBluetoothLeScanner != null) {
            mBluetoothLeScanner!!.stopScan(mScanCallback);
//            scanComplete();
        }

        mScanCallback = null;
        mScanning = false;
    }

    fun checkPermissions(){
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            requestBluetoothEnable();
        }
        requestLocationPermission()
    }

    private fun requestBluetoothEnable() {
        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        (context as AppCompatActivity).startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
    }

    private fun hasLocationPermissions(): Boolean {
        return ActivityCompat.checkSelfPermission(context,Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("CheckResult")
    private fun requestLocationPermission() {
        rxPermissions.request(Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.READ_PHONE_STATE
        ).subscribe { granted->
            if(granted){
                //all granted
                startScanProcess()
            }else{
                //one is rejected
            }
        }
    }

    fun checkIfDeviceHasBle(context:Context)= context.packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)

    private inner class BtleScanCallback : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            addScanResult(result)
        }

        override fun onBatchScanResults(results: List<ScanResult>) {
            for (result in results) {
                addScanResult(result)
            }
        }

        override fun onScanFailed(errorCode: Int) {
            Timber.e("BLE Scan Failed with code $errorCode")
        }

        private fun addScanResult(result: ScanResult) {
            val device = result.getDevice()
            val deviceAddress = device.getAddress()
            mScanResults[deviceAddress] = device
        }
    };

}