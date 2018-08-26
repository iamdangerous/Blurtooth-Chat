package com.rahul.`in`.bluetooth_demo

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.Toast
import com.polidea.rxandroidble2.RxBleClient
import com.polidea.rxandroidble2.scan.ScanFilter
import com.polidea.rxandroidble2.scan.ScanSettings
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.Flowable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.coroutines.experimental.bg
import org.jetbrains.anko.sdk25.coroutines.onCheckedChange
import java.util.*
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    val REQUEST_ENABLE_BT = 100
    lateinit var rxBleClient: RxBleClient
    var scanSubscription: Disposable? = null
    var bluetoothSocket: BluetoothSocket? = null

    val nearbyDevices = HashSet<BluetoothDevice>()
    val devicesList = arrayListOf<BluetoothDevice>()
    val processedNearbyDevices = HashSet<BluetoothDevice>()
    var mBluetoothAdapter: BluetoothAdapter? = null

    val APP_NAME = "rahul"
    val APP_UUID = "8ff5b74a-be5f-4cb4-adc7-124f39750b04"
    val uuid: UUID = UUID.fromString(APP_UUID)

    var connectAsServer = false
    val presenter = MainActivityPresenter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Register broadcast receiver when a device is discovered
        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        registerReceiver(mReceiver, filter)

        val discoverableFilter = IntentFilter(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED)
        registerReceiver(discoverAbleReceiver, discoverableFilter)


        initVars()
        getPermissions()
        btnTurnOnBlutooth.setOnClickListener { setupBluetooth() }
        btnStartScan.setOnClickListener {
            enableDiscoverablity()
//            startScan()
            mBluetoothAdapter?.startDiscovery()

        }
        btnStopScan.setOnClickListener { stopScan() }


        switchServer.setOnCheckedChangeListener { button, isChecked ->
            connectAsServer = isChecked
            if (isChecked) {
                switchServer.text = "Connect as server"
                btnStartServer.visibility = View.VISIBLE
                btnConAsClient.visibility = View.GONE
            } else {
                switchServer.text = "Connect as client"
                btnStartServer.visibility = View.GONE
                btnConAsClient.visibility = View.VISIBLE
            }
        }

        switchServer.isChecked = true

        btnStartServer.setOnClickListener {
            bg {
                if (connectAsServer)
                    connectAsServer()
            }
        }
        btnConAsClient.setOnClickListener {
            bg {
                if (!connectAsServer) {
                    connectAsClient()
                }
            }
        }

        btnSendData.setOnClickListener {
            writeDataToSocket()
        }

//        createConnection()
    }

    fun writeDataToSocket() {
        presenter.writeData(bluetoothSocket, mBluetoothAdapter);
    }


    fun readDataFromSocket() {
        bg {
            presenter.readData(bluetoothSocket);
        }
    }


    val mReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {
            val action = p1?.action
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                var device: BluetoothDevice? = p1?.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                var deviceName: String? = null
                var deviceHardwareAddress = device?.address


                if (device != null) {
                    deviceName = device.name + " (" + (deviceHardwareAddress) + ")"

                } else {
                    deviceName = deviceHardwareAddress
                }

                if (deviceName == null) {
                    deviceName = deviceHardwareAddress
                }


                if (device != null) {
                    val newDevice = nearbyDevices.add(device)
                    if (newDevice) {
                        addDevice(deviceName!!,device)
                        printLogInScreen("BR - device found MAC = ${deviceHardwareAddress}")
                        printLogInScreen("BR - device name = ${deviceName}")
                    }
                }

            }
        }
    }

    fun addDevice(deviceName: String, device: BluetoothDevice) {
        val box = CheckBox(this)
        val lparams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        box.layoutParams = lparams
        box.tag = nearbyDevices.size - 1
        box.text = deviceName

        box.onCheckedChange { buttonView, isChecked ->

            val currentIndex = buttonView!!.tag as Int
            if (isChecked) {
                (0 until ll_container.childCount).map {
                    val box = ll_container.getChildAt(it) as CheckBox
                    if (ll_container.getChildAt(it).tag == currentIndex) {
                        //Do nothing
                    } else {
                        box.isChecked = false
                    }
                }
            } else {
                // Do nothing
            }
        }

        ll_container.addView(box)
        devicesList.add(device)
    }

    val discoverAbleReceiver = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {
            var action = p1?.action
            if (BluetoothAdapter.ACTION_SCAN_MODE_CHANGED.equals(action)) {
                var mode = p1?.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, BluetoothAdapter.ERROR)
                when (mode) {
                    BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE ->
                        printLogInScreen("The device is in discoverable mode.")
                    BluetoothAdapter.SCAN_MODE_CONNECTABLE ->
                        printLogInScreen("The device isn't in discoverable mode but can still receive connections.")
                    BluetoothAdapter.SCAN_MODE_NONE ->
                        printLogInScreen("The device isn't in discoverable mode and cannot receive connections.")
                    else ->
                        printLogInScreen("unknown discover mode.")
                }
            }
        }

    }

    fun initVars() {
        rxBleClient = (application as App).rxBleClient

        presenter.callback = MainActivityPresenter.PresenterCallback { msg -> printLogInScreen(msg!!) }
    }

    fun enableDiscoverablity() {
        printLogInScreen("enable discover mode")
        val discoverableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 3600)
        startActivity(discoverableIntent)
    }

    fun getPermissions() {

        val rxPermissions = RxPermissions(this)

        rxPermissions.request(Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.READ_PHONE_STATE
        )
                .subscribe { _ -> }
    }


    fun setupBluetooth() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            toast("Device doesn't support Bluetooth")
            return
        }
        if (!mBluetoothAdapter!!.isEnabled()) {
            printLogInScreen("Enable adapter")
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
        } else {
            printLogInScreen("bluetooth already enabled")
        }
    }

    fun toggleBluetooth() {

    }

    fun printLogInScreen(msg: String) {
        runOnUiThread {
            var text = tvLog.text
            val newMsg = msg + "\n" + text.toString()
            tvLog.text = newMsg
        }
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


    fun connectAsServer(device: BluetoothDevice? = null) {
        printLogInScreen("starting server")
        try {
//            val serverSocket: BluetoothServerSocket = mBluetoothAdapter!!.listenUsingRfcommWithServiceRecord(APP_NAME, uuid)
            val serverSocket: BluetoothServerSocket = mBluetoothAdapter!!.listenUsingInsecureRfcommWithServiceRecord(APP_NAME, uuid)

            bluetoothSocket = serverSocket.accept() //blocking call

            log("connected as server")
            printLogInScreen("connected as server")
            if (bluetoothSocket != null) {
//                bluetoothSocket!!.close()
            }
            readDataFromSocket()
        } catch (e: Exception) {
            e.printStackTrace()
            runOnUiThread {
                printLogInScreen("exception - server - device = ${device?.address}")
            }

//            connectAsServer(device)
        }
    }

    fun createConnection() {

        fun manageSocket(bluetoothSocket: BluetoothSocket) {

        }

        val scheduler = Schedulers.newThread()
        Flowable.interval(10, TimeUnit.SECONDS)
                .onBackpressureLatest()
                .observeOn(scheduler)
                .subscribe {
                    for (device in nearbyDevices) {
                        if (connectAsServer) {
                            connectAsServer(device)
                        } else {
                            connectAsClient()
                        }

                    }
                }
    }

    fun connectAsClient() {

        if (nearbyDevices.size == 0) {
            printLogInScreen("No devices found yet")
            return
        }
        var device:BluetoothDevice ? = null
        (0 until ll_container.childCount)
                .map { if((ll_container.getChildAt(it) as CheckBox).isChecked){device = devicesList[it]} }
        try {
//            val bluetoothSocket = device.createRfcommSocketToServiceRecord(uuid)
            bluetoothSocket = device!!.createInsecureRfcommSocketToServiceRecord(uuid)

//                mBluetoothAdapter!!.cancelDiscovery()
            bluetoothSocket!!.connect() //blocking call
            log("connected as client")
            printLogInScreen("connected as client")

            if (bluetoothSocket != null) {
//                manageSocket(bluetoothSocket)
//                bluetoothSocket!!.close()
            }

            readDataFromSocket()
        } catch (e: Exception) {
            e.printStackTrace()
            runOnUiThread {
                printLogInScreen("client - exception device - ${device!!.address}")
                printLogInScreen(e.localizedMessage)
            }

//            connectAsClient(device)
        }

    }

    fun stopScan() {
        if (scanSubscription != null) {
            scanSubscription!!.dispose()
            scanSubscription = null
        }
        toast("Scanning stopped")
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
            if (resultCode == Activity.RESULT_OK) {
                enableDiscoverablity()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(mReceiver)
    }
}
