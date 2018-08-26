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
import android.graphics.*
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.AttributeSet
import android.util.Log
import android.view.View
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
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.*
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    val REQUEST_ENABLE_BT = 100
    lateinit var rxBleClient: RxBleClient
    var scanSubscription: Disposable? = null
    var bluetoothSocket: BluetoothSocket? = null

    val nearbyDevices = HashSet<BluetoothDevice>()
    val processedNearbyDevices = HashSet<BluetoothDevice>()
    var mBluetoothAdapter: BluetoothAdapter? = null

    val APP_NAME = "rahul"
    val APP_UUID = "8ff5b74a-be5f-4cb4-adc7-124f39750b04"
    val uuid: UUID = UUID.fromString(APP_UUID)

    var connectAsServer = false

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
        btnDraw.setOnClickListener { draw() }
        switchServer.setOnCheckedChangeListener { button, isChecked ->
            connectAsServer = isChecked
            if (isChecked) {
                switchServer.text = "Connect as server"
            } else {
                switchServer.text = "Connect as client"
            }
        }

        btnStartServer.setOnClickListener {
            bg {
                if (connectAsServer)
                    connectAsServer()
            }
        }
        btnConAsClient.setOnClickListener {
            bg {
                if (!connectAsServer) {
                    connectAsClient(nearbyDevices.first())
                }
            }
        }

        btnSendData.setOnClickListener {
            try {
                if (bluetoothSocket != null) {
                    val outputStream: OutputStream = bluetoothSocket!!.outputStream
                    val output = ByteArrayOutputStream(4)
                    output.write(10)
                    outputStream.write(output.toByteArray())
                }
            } catch (e: IOException) {
                printLogInScreen("IO exception in write")
            }
        }

//        createConnection()
    }

    fun readDataFromSocket() {
        bg {

            try {
                if (bluetoothSocket != null) {
                    var inputStream = bluetoothSocket!!.inputStream
                    val mmBuffer = ByteArray(1024)
                    var numBytes = 0

                    //Keep listening unless exception is thrown
                    printLogInScreen("Read data from socket")
                    while (true) {
                        numBytes = inputStream.read(mmBuffer)
                        val inputAsString = inputStream.bufferedReader().use { it.readText() }
                        printLogInScreen(inputAsString)
                    }
                }
            } catch (e: IOException) {
                printLogInScreen("IO exception in read")
                e.printStackTrace()
            }
        }
    }

    val mReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {
            val action = p1?.action
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                var device: BluetoothDevice? = p1?.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                var deviceName = device?.name
                var deviceHardwareAddress = device?.address
                if (device != null) {
                    nearbyDevices.add(device)
                }
                printLogInScreen("BR - device found MAC = ${deviceHardwareAddress}")
            }
        }
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
    }

    fun enableDiscoverablity() {
        printLogInScreen("enable discover mode")
        val discoverableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 3600)
        startActivity(discoverableIntent)
    }

    fun getPermissions() {

        val rxPermissions = RxPermissions(this)

        rxPermissions.request(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.BLUETOOTH_ADMIN)
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
                            connectAsClient(device)
                        }

                    }
                }
    }

    fun connectAsClient(device: BluetoothDevice) {

        try {
//            val bluetoothSocket = device.createRfcommSocketToServiceRecord(uuid)
            bluetoothSocket = device.createInsecureRfcommSocketToServiceRecord(uuid)

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
                printLogInScreen("client - exception device - ${device.address}")
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


    fun draw() {
        val myView = MyView(this)
//        llContainer.addView(myView)
    }

    inner class MyView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : View(context, attrs, defStyleAttr) {

        val paint = Paint()
        val shadowPaint = Paint()
        lateinit var ovalRect: RectF

        init {
            paint.style = Paint.Style.FILL
            shadowPaint.style = Paint.Style.STROKE
            shadowPaint.color = Color.BLACK
            shadowPaint.isAntiAlias = true
            shadowPaint.strokeWidth = 10f
            shadowPaint.maskFilter = BlurMaskFilter(8f, BlurMaskFilter.Blur.OUTER)

            //oval
//            RectF(x,y,x+width,y+height);
            val x = 100f
            val y = 100f
            val width = 200f
            val height = 100f
            ovalRect = RectF(x, y, x + width, y + height)
        }

        override fun draw(canvas: Canvas?) {
            super.draw(canvas)

            paint.color = Color.RED
//            canvas?.drawCircle(200f, 200f, 200f, paint)
//            canvas?.drawCircle(200f, 200f, 201f, shadowPaint)
            canvas?.drawOval(ovalRect, paint)

            //draw shadow
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(mReceiver)
    }
}
