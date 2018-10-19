package com.rahul.`in`.bluetooth_demo.activity

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.polidea.rxandroidble2.RxBleClient
import com.rahul.`in`.bluetooth_demo.App
import com.rahul.`in`.bluetooth_demo.service.BleService
import io.reactivex.disposables.Disposable
import java.util.*

open class BleBaseActivity : AppCompatActivity() {

    val APP_NAME = "rahul"
    val APP_UUID = "8ff5b74a-be5f-4cb4-adc7-124f39750b04"
    val uuid: UUID = UUID.fromString(APP_UUID)


    val REQUEST_ENABLE_BT = 100
    var mBluetoothAdapter: BluetoothAdapter? = null
    lateinit var rxBleClient: RxBleClient;
    var scanSubscription: Disposable? = null
    val nearbyDevices = HashSet<BluetoothDevice>()

    open fun initVars() {
        rxBleClient = (application as App).rxBleClient
    }

    open fun setClicks(){
        //Do nothing
    }

    fun setupBluetooth() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            toast("Device doesn't support Bluetooth")
            return
        }
        if (!mBluetoothAdapter!!.isEnabled) {
            printLogInScreen("Enable adapter")
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
        } else {
            printLogInScreen("bluetooth already enabled")
        }
    }

    open fun printLogInScreen(msg: String) {
        //Do nothing
    }

    fun toast(msg: String) {
        val TAG = "MAIN_ACTIVITY"
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        Log.e(TAG, msg)
    }

    fun log(msg: String) {
        val TAG = "MAIN_ACTIVITY"
        Log.d(TAG, msg)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == AppCompatActivity.RESULT_OK) {
                enableDiscoverablity()
            }
        }
    }


    open fun enableDiscoverablity(){
        printLogInScreen("enable discover mode")
        val discoverableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 3600)
        startActivity(discoverableIntent)
    }
}