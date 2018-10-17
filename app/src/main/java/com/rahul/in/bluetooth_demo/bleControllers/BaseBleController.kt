package com.rahul.`in`.bluetooth_demo.bleControllers

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.tbruyelle.rxpermissions2.RxPermissions

open class BaseBleController (val context: Context, val mBluetoothManager: BluetoothManager, val mBluetoothAdapter: BluetoothAdapter){

    val REQUEST_ENABLE_BT = 100

    open fun startScanProcess(){
        //DO nothing
    }


    open fun checkPermissions() {
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            requestBluetoothEnable();
        }
//        requestLocationPermission()
    }

    fun requestBluetoothEnable() {
        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        (context as AppCompatActivity).startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
    }

    fun hasLocationPermissions(): Boolean {
        return ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    fun checkIfDeviceHasBle(context: Context) = context.packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)

}