package com.rahul.`in`.bluetooth_demo.bleControllers

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.*
import android.bluetooth.le.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.ParcelUuid
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.tbruyelle.rxpermissions2.RxPermissions
import timber.log.Timber
import java.io.UnsupportedEncodingException
import java.nio.charset.Charset
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class BleMeshController(rxPermissions: RxPermissions, context: Context, mBluetoothManager: BluetoothManager, mBluetoothAdapter: BluetoothAdapter) : BaseBleController(rxPermissions,context,mBluetoothManager,mBluetoothAdapter){

    var hasPermissions = false

    var mScanning = false
    val APP_UUID = "8ff5b74a-be5f-4cb4-adc7-124f39750b04"
    val CHARACTERISTIC_ID = "8af00abb-1eff-4847-b0a2-d312cdbc6d17"
    val SERVICE_UUID = UUID.fromString(APP_UUID)
    val CHARACTERISTIC_UUID = UUID.fromString(CHARACTERISTIC_ID)
    var mScanCallback: ScanCallback? = null
    val mScanResults = HashMap<String, BluetoothDevice>()
    var mBluetoothLeScanner: BluetoothLeScanner? = null
    var mBluetoothLeAdvertiser: BluetoothLeAdvertiser? = null;
    var mGattServer: BluetoothGattServer? = null
    val mGattMap = HashMap<BluetoothDevice, BluetoothGatt>()
    var mConnectedMap = HashMap<BluetoothGatt, Boolean>()
    val mDevices: ArrayList<BluetoothDevice> = ArrayList()
    var callback: BleMeshControllerCallback? = null


    override fun startScanProcess() {
        if (mScanning) {
            return
        }
        callback?.print("startScanProcess")
        val filters = ArrayList<ScanFilter>()
        val settings = ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
                .build()

        mScanCallback = BtleScanCallback()
        mBluetoothLeScanner = mBluetoothAdapter.bluetoothLeScanner
//        mBluetoothLeScanner!!.startScan(filters, settings, mScanCallback)
//        mBluetoothLeScanner?.startScan(mScanCallback)
        mBluetoothAdapter?.startLeScan(object :BluetoothAdapter.LeScanCallback{
            override fun onLeScan(p0: BluetoothDevice?, p1: Int, p2: ByteArray?) {
                callback?.print("onLeScan")
            }

        })

        mScanning = true
    }

    fun stopScan() {
        if (mScanning && mBluetoothAdapter != null && mBluetoothAdapter.isEnabled() && mBluetoothLeScanner != null) {
            mBluetoothLeScanner!!.stopScan(mScanCallback);
//            scanComplete();
        }

        mScanCallback = null;
        mScanning = false;
    }

    fun onResume() {
        setupGattAdvertising()
    }

    fun setupGattAdvertising() {
        Timber.d("setupGattAdvertising")
        if (!mBluetoothAdapter.isMultipleAdvertisementSupported()) {

            mBluetoothLeAdvertiser = mBluetoothAdapter.getBluetoothLeAdvertiser();
            val gattServerCallback = GattServerCallback()
            mGattServer = mBluetoothManager.openGattServer(context, gattServerCallback)
            setupServer()
            startAdvertising()
        }
    }

    fun startAdvertising() {
        if (mBluetoothLeAdvertiser == null) {
            return
        }
        val settings = AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED)
                .setConnectable(true)
                .setTimeout(0)
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_LOW)
                .build()

        val parcelUuid = ParcelUuid(SERVICE_UUID)
        val data = AdvertiseData.Builder()
                .setIncludeDeviceName(true)
                .addServiceUuid(parcelUuid)
                .build()

        mBluetoothLeAdvertiser?.startAdvertising(settings, data, mAdvertiseCallback);
    }

    private val mAdvertiseCallback = object : AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings) {
            Timber.d("Peripheral advertising started.")
        }

        override fun onStartFailure(errorCode: Int) {
            Timber.d("Peripheral advertising failed: $errorCode")
        }
    }

    fun setupServer() {
        Timber.d("setupServer")
        val service = BluetoothGattService(SERVICE_UUID, BluetoothGattService.SERVICE_TYPE_PRIMARY)
        mGattServer?.addService(service)

        val writeCharacteristic = BluetoothGattCharacteristic(
                CHARACTERISTIC_UUID,
                BluetoothGattCharacteristic.PROPERTY_WRITE,
                BluetoothGattCharacteristic.PERMISSION_WRITE)
        service.addCharacteristic(writeCharacteristic)
        mGattServer?.addService(service)
    }

    private fun stopServer() {
        mGattServer?.close()
    }

    private fun stopAdvertising() {
        mBluetoothLeAdvertiser?.stopAdvertising(mAdvertiseCallback)
    }

    inner class GattServerCallback : BluetoothGattServerCallback() {
        override fun onConnectionStateChange(device: BluetoothDevice?, status: Int, newState: Int) {
            super.onConnectionStateChange(device, status, newState);
            device?.apply {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    mDevices.add(this)
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    mDevices.remove(this)
                }
            }
        }

        override fun onCharacteristicWriteRequest(device: BluetoothDevice?, requestId: Int, characteristic: BluetoothGattCharacteristic?, preparedWrite: Boolean, responseNeeded: Boolean, offset: Int, value: ByteArray?) {
            super.onCharacteristicWriteRequest(device, requestId, characteristic, preparedWrite, responseNeeded, offset, value)
            characteristic?.run {
                if (characteristic.getUuid().equals(CHARACTERISTIC_UUID)) {
                    mGattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, null);

                    mGattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, null)
                    val length = value?.size
                    val reversed = ByteArray(length!!)
                    for (i in 0 until length) {
                        reversed[i] = value[length - (i + 1)]
                    }
                    characteristic.value = reversed
                    for (device in mDevices) {
                        mGattServer?.notifyCharacteristicChanged(device, characteristic, false)
                    }
                }
            }
        }
    }

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
            callback?.print("BLE Scan Failed with code $errorCode")
        }

        private fun addScanResult(result: ScanResult) {
            callback?.print("addScanResult")
            val device = result.getDevice()
            val deviceAddress = device.getAddress()
            mScanResults[deviceAddress] = device
            connectDevice(device)
        }
    }

    private fun connectDevice(device: BluetoothDevice) {
        val gattClientCallback = GattClientCallback()
        var gat = mGattMap[device]
        if (gat == null) {
            val mGatt = device.connectGatt(context, false, gattClientCallback)
            mGattMap[device] = mGatt
        }
    }

    inner class GattClientCallback : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            if (status == BluetoothGatt.GATT_FAILURE) {
                disconnectGattServer(gatt)
                return
            } else if (status != BluetoothGatt.GATT_SUCCESS) {
                disconnectGattServer(gatt)
                return
            }
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                mConnectedMap[gatt] = true
                gatt.discoverServices()
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                disconnectGattServer(gatt)
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            super.onServicesDiscovered(gatt, status)
            if (status != BluetoothGatt.GATT_SUCCESS) {
                return;
            }
            gatt?.run {
                val service = gatt.getService(SERVICE_UUID)
                val characteristic = service.getCharacteristic(CHARACTERISTIC_UUID)

                characteristic.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
                val mInitialized = gatt.setCharacteristicNotification(characteristic, true)
            }
        }

        override fun onCharacteristicChanged(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?) {
            super.onCharacteristicChanged(gatt, characteristic)
            val messageBytes = characteristic?.getValue()
            var messageString: String? = null
            try {
                messageString = messageBytes.toString()
            } catch (e: UnsupportedEncodingException) {
                Timber.e("Unable to convert message bytes to string");
            }
            Timber.d("Received message: " + messageString);
        }
    }

    fun disconnectGattServer(gatt: BluetoothGatt) {
        mConnectedMap[gatt] = false
        gatt.disconnect()
        gatt.close()
    }

    fun sendMessage(bleDevice: BluetoothDevice) {
        val mConnected = mConnectedMap[mGattMap[bleDevice]]
        if (mConnected == null || !mConnected) {
            return
        }
        val service = mGattMap[bleDevice]?.getService(SERVICE_UUID)
        val characteristic = service?.getCharacteristic(CHARACTERISTIC_UUID)
        val message = "First message"

        var messageBytes = ByteArray(0)
        try {
            messageBytes = message.toByteArray(Charset.forName("UTF-8"))
        } catch (e: UnsupportedEncodingException) {
            Timber.e("Failed to convert message string to byte array")
        }
        characteristic?.setValue(messageBytes)
        val success = mGattMap[bleDevice]?.writeCharacteristic(characteristic)

    }

    interface BleMeshControllerCallback{
        fun print(message:String)
    }


}