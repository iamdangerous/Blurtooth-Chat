package com.rahul.`in`.bluetooth_demo.activity

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.os.Bundle
import com.rahul.`in`.bluetooth_demo.R
import com.rahul.`in`.bluetooth_demo.bleControllers.BleMeshController
import com.tbruyelle.rxpermissions2.RxPermissions
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Handler
import android.os.IBinder
import android.widget.Button
import android.widget.EditText
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.rahul.`in`.bluetooth_demo.adapter.MeshBleDevicesAdapter
import com.rahul.`in`.bluetooth_demo.eventBus.EventBleConnectionUpdated
import com.rahul.`in`.bluetooth_demo.eventBus.EventOnBleDeviceAdded
import com.rahul.`in`.bluetooth_demo.eventBus.EventPrintMessage
import com.rahul.`in`.bluetooth_demo.fake.FakeDataProvider
import com.rahul.`in`.bluetooth_demo.fake.FakeUserModel
import com.rahul.`in`.bluetooth_demo.service.BleService
import kotlinx.android.synthetic.main.activity_main.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*


class BleMeshActivity : BleBaseActivity(), BleMeshController.BleMeshControllerCallback {


    lateinit var btnTurnOnBlutooth:Button
    lateinit var btnStartScan:Button
    lateinit var etUserId:EditText
    lateinit var btnSetUser:Button

    lateinit var rvDevices: RecyclerView
    lateinit var devicesAdapter: MeshBleDevicesAdapter
    val bleDevices = ArrayList<BluetoothDevice>()
    lateinit var rxPermissions:RxPermissions

    companion object {
        var fakeUser:FakeUserModel? = null
    }


    private lateinit var mService: BleService
    private var mBound: Boolean = false
    val fakeDataProvider = FakeDataProvider()

    private val mConnection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            val binder = service as BleService.LocalBinder
            mService = binder.getService()
            mBound = true
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            mBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ble_mesh)

        rvDevices = findViewById(R.id.rv_devices)
        btnTurnOnBlutooth = findViewById(R.id.btnTurnOnBlutooth)
        btnStartScan = findViewById(R.id.btnStartScan)
        etUserId = findViewById(R.id.etUserId)
        btnSetUser = findViewById(R.id.btnSetUser)

        rvDevices.layoutManager = LinearLayoutManager(this)
        rxPermissions = RxPermissions(this)

        devicesAdapter = MeshBleDevicesAdapter(bleDevices)

        devicesAdapter.callback = object : MeshBleDevicesAdapter.BleAdapterCallback {
            override fun onSendMessage(bleDevice: BluetoothDevice) {
                mService.mServiceHandler?.post {mService.mBleMeshController?.sendMessage(bleDevice)}
            }

            override fun onItemClick(bleDevice: BluetoothDevice) {
                printLogInScreen("Connecting with with ${bleDevice.name}")
                mService.mServiceHandler?.post {mService.mBleMeshController?.connectDevice(bleDevice)}
            }
        }

        rvDevices.adapter = devicesAdapter
        setClicks()

        Handler().postDelayed({startBleService()}, 2000L)
        EventBus.getDefault().register(this)
    }

    override fun setClicks(){
        btnTurnOnBlutooth.setOnClickListener {
            //Ask all permissions
            setupBluetooth()
            enableDiscoverablity()
        }

        btnStartScan.setOnClickListener {
            requestLocationPermission()
        }

        btnSetUser.setOnClickListener {
            val userId = etUserId.text.toString()
            fakeUser = fakeDataProvider.provideFakeUser(userId.toInt())

            print(fakeUser!!.name)
        }
    }

    override fun onDeviceAdded(added: Boolean, bleDevice: BluetoothDevice) {
        runOnUiThread {
            bleDevices.add(bleDevice)
            devicesAdapter.notifyDataSetChanged()
        }
    }

    override fun onConnectionUpdated(connectedBleDevicesSet: HashSet<BluetoothDevice>) {

    }

    override fun onScanStarted(scanStarted: Boolean) {
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

    fun startBleService(){
        Intent(this, BleService::class.java).also { intent ->
            bindService(intent, mConnection, Context.BIND_AUTO_CREATE)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(mConnection)
        mBound = false
        EventBus.getDefault().unregister(this)
    }

    @SuppressLint("CheckResult")
    fun requestLocationPermission() {
        rxPermissions.request(Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.READ_PHONE_STATE
        ).subscribe { granted ->
            if (granted) {
                //all granted
                mService.startScan()
            } else {
                //one is rejected
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: EventOnBleDeviceAdded){
        bleDevices.add(event.bleDevice)
        devicesAdapter.notifyDataSetChanged()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: EventBleConnectionUpdated){
        //Do nothing
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: EventPrintMessage){
        printLogInScreen(event.text)
    }

}