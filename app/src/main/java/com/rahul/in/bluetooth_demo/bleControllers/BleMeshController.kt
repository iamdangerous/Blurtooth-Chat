package com.rahul.`in`.bluetooth_demo.bleControllers

import android.bluetooth.*
import android.bluetooth.BluetoothGatt.GATT_SUCCESS
import android.bluetooth.le.*
import android.content.Context
import android.os.Handler
import android.os.ParcelUuid
import com.google.gson.Gson
import com.rahul.`in`.bluetooth_demo.activity.BleMeshActivity
import com.tbruyelle.rxpermissions2.RxPermissions
import timber.log.Timber
import java.io.UnsupportedEncodingException
import java.nio.charset.Charset
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.collections.HashSet


class BleMeshController(context: Context, mBluetoothManager: BluetoothManager, mBluetoothAdapter: BluetoothAdapter) : BaseBleController(context, mBluetoothManager, mBluetoothAdapter) {

    var mScanning = false
    val SERVICE_ID = "8ff5b74a-be5f-4cb4-adc7-124f39750b04"
    val CHARACTERISTIC_ID = "8af00abb-1eff-4847-b0a2-d312cdbc6d17"
    val DESCRIPTOR_ID = "ca71c89d-738b-4632-a6d5-bad4346f1b79"
    val USER_META_DATA_ID = "536be339-20fe-4bb9-bccf-6d78f7b40109"
    val USER_META_DATA_DESCRIPTOR_ID = "cf2b8b43-9d09-441c-8576-380487df9d5b"
    val GROUP_CHAT_MSG_ID = "6b33da74-4683-40ba-82c2-61a217f47f55"

    val SERVICE_UUID = UUID.fromString(SERVICE_ID)
    val CHARACTERISTIC_UUID = UUID.fromString(CHARACTERISTIC_ID)
    val DESCRIPTOR_UUID = UUID.fromString(DESCRIPTOR_ID)
    val USER_META_DATA_UUID = UUID.fromString(USER_META_DATA_ID)
    val USER_META_DATA_DESCRIPTOR_UUID = UUID.fromString(USER_META_DATA_DESCRIPTOR_ID)
//    val ONE_TO_ONE_MSG_UUID = UUID.fromString(ONE_TO_ONE_MSG_ID)
//    val GROUP_CHAT_MSG_UUID = UUID.fromString(GROUP_CHAT_MSG_ID)

    var mScanCallback: ScanCallback? = null
    var mBluetoothLeScanner: BluetoothLeScanner? = null
    var mBluetoothLeAdvertiser: BluetoothLeAdvertiser? = null;
    var mGattServer: BluetoothGattServer? = null
    val mGattMap = HashMap<BluetoothDevice, BluetoothGatt>()
    val mGattDeviceMap = HashMap<BluetoothGatt, BluetoothDevice>()
    val mGattClientCallbackMap = HashMap<BluetoothDevice, GattClientCallback>()
    var mConnectedMap = HashMap<BluetoothGatt, Boolean>()
    val mDevices: ArrayList<BluetoothDevice> = ArrayList()
    val mScannedDevices = HashSet<BluetoothDevice>()
    var callback: BleMeshControllerCallback? = null


    override fun startScanProcess() {
        if (mScanning) {
            return
        }
        callback?.print("SERVICE_UUID = $SERVICE_UUID")
        callback?.print("CHARACTERISTIC_UUID = $CHARACTERISTIC_UUID")
        callback?.print("DESCRIPTOR_UUID = $DESCRIPTOR_UUID")
        callback?.print("startScanProcess")
        val filters = ArrayList<ScanFilter>()
        val settings = ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
                .build()
        filters.add(ScanFilter.Builder()
                .setServiceUuid(ParcelUuid(SERVICE_UUID))
                .build())

        mScanCallback = BtleScanCallback()
        mBluetoothLeScanner = mBluetoothAdapter.bluetoothLeScanner
        mBluetoothLeScanner!!.startScan(filters, settings, mScanCallback)

        mScanning = true
        callback?.onScanStarted(mScanning)
    }

    fun stopScan() {
        if (mScanning && mBluetoothAdapter != null && mBluetoothAdapter.isEnabled() && mBluetoothLeScanner != null) {
            mBluetoothLeScanner!!.stopScan(mScanCallback)
//            scanComplete();
        }

        mScanCallback = null;
        mScanning = false;
    }

    fun onResume() {
        setupGattAdvertising()
    }

    fun setupGattAdvertising() {
        callback?.print("setupGattAdvertising")
        if (mBluetoothAdapter.isMultipleAdvertisementSupported()) {

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
        callback?.print("startAdvertising")
        val settings = AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED)
                .setConnectable(true)
                .setTimeout(0)
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_LOW)
                .build()

        val parcelUuid = ParcelUuid(SERVICE_UUID)
        val data = AdvertiseData.Builder()
                .setIncludeDeviceName(false)// because data is greater than 31 bytes
                .addServiceUuid(parcelUuid)
                .build()

        mBluetoothLeAdvertiser?.startAdvertising(settings, data, mAdvertiseCallback);
    }

    private val mAdvertiseCallback = object : AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings) {
            Timber.d("Peripheral advertising started.")
            callback?.print("Peripheral advertising started.")
        }

        override fun onStartFailure(errorCode: Int) {
            Timber.d("Peripheral advertising failed: $errorCode")
            callback?.print("Peripheral advertising failed")
        }
    }

    fun setupServer() {
        callback?.print("setupServer")
        val service = BluetoothGattService(SERVICE_UUID, BluetoothGattService.SERVICE_TYPE_PRIMARY)

        val characteristic = BluetoothGattCharacteristic(
                CHARACTERISTIC_UUID,
                BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE,
                BluetoothGattCharacteristic.PERMISSION_WRITE)

        characteristic.writeType = BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE

        val descriptor = BluetoothGattDescriptor(DESCRIPTOR_UUID, BluetoothGattDescriptor.PERMISSION_READ or BluetoothGattDescriptor.PERMISSION_WRITE)
        characteristic.addDescriptor(descriptor)
        service.addCharacteristic(characteristic)


        val userMetaDataCharacteristic = BluetoothGattCharacteristic(
                USER_META_DATA_UUID,
                BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE,
                BluetoothGattCharacteristic.PERMISSION_WRITE)

        userMetaDataCharacteristic.writeType = BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE

        val metaDataDescriptor = BluetoothGattDescriptor(USER_META_DATA_DESCRIPTOR_UUID, BluetoothGattDescriptor.PERMISSION_READ or BluetoothGattDescriptor.PERMISSION_WRITE)
        userMetaDataCharacteristic.addDescriptor(metaDataDescriptor)
        service.addCharacteristic(userMetaDataCharacteristic)

        mGattServer?.addService(service)
    }

    private fun stopServer() {
        mGattServer?.close()
    }

    private fun stopAdvertising() {
        mBluetoothLeAdvertiser?.stopAdvertising(mAdvertiseCallback)
    }

    inner class GattServerCallback : BluetoothGattServerCallback() {

        override fun onDescriptorReadRequest(device: BluetoothDevice?, requestId: Int, offset: Int, descriptor: BluetoothGattDescriptor?) {
            Timber.d("Server onDescriptorReadRequest Started")
            super.onDescriptorReadRequest(device, requestId, offset, descriptor)
            Timber.d("Server onDescriptorReadRequest Ended")
        }

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
            Timber.d("Server onCharacteristicWriteRequest Started")
            super.onCharacteristicWriteRequest(device, requestId, characteristic, preparedWrite, responseNeeded, offset, value)
            callback?.print("onCharacteristicWriteRequest")
            Timber.d("Server onCharacteristicWriteRequest Ended")
            val success = mGattServer?.sendResponse(device, requestId, GATT_SUCCESS, 0, value)

            var textReceived = value?.toString(Charset.forName("UTF-8"))
            Timber.d("Server onCharacteristicWriteRequest, success = $success, value = ${textReceived}")
            characteristic?.value = value
            textReceived?.apply { callback?.print(textReceived) }
            mBluetoothAdapter.bondedDevices.map {
                if(it!=null) {
                    val notifySuccess = mGattServer?.notifyCharacteristicChanged(it, characteristic, true)
                    callback?.print("notifySuccess = $notifySuccess")
                }
            }
        }

        override fun onCharacteristicReadRequest(device: BluetoothDevice?, requestId: Int, offset: Int, characteristic: BluetoothGattCharacteristic?) {
            Timber.d("Server onCharacteristicReadRequest Started")
            super.onCharacteristicReadRequest(device, requestId, offset, characteristic)
            Timber.d("Server onCharacteristicReadRequest Ended")
            val value = ("1").toByteArray()
            val success = mGattServer?.sendResponse(device, requestId, GATT_SUCCESS, 0, value)
            Timber.d("Server onCharacteristicReadRequest, success = $success")
        }

        override fun onDescriptorWriteRequest(device: BluetoothDevice?, requestId: Int, descriptor: BluetoothGattDescriptor?, preparedWrite: Boolean, responseNeeded: Boolean, offset: Int, value: ByteArray?) {
            Timber.d("Server onDescriptorWriteRequest Started")
            super.onDescriptorWriteRequest(device, requestId, descriptor, preparedWrite, responseNeeded, offset, value)
            Timber.d("Server onDescriptorWriteRequest Ended")

            val success = mGattServer?.sendResponse(device, requestId, GATT_SUCCESS, 0, value)
            Timber.d("Server onDescriptorWriteRequest send Response success = $success")

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
//            mScanResults[deviceAddress] = device
//            connectDevice(device)

            if (!mScannedDevices.contains(device)) {
                    val skipBleDevices = arrayListOf<String>(
                            "51:8D:28:74:99:B1",
                            "5D:FB:21:F4:19:3A",
                            "30:35:AD:C5:DC:DD",
                            "15:16:E4:0C:15:61",
                            "15:16:E4:0C:15:61",
                            "REFLEX_FD41D15FB3D6"
                    )
                    if (skipBleDevices.contains(device.address)) {
                        return
                    }
                    mScannedDevices.add(device)
                    callback?.print("onLeScan")
                    callback?.onDeviceAdded(true, device)
                    Handler().postDelayed({ connectDevice(device)}, 1000)
                }
        }
    }

    fun connectDevice(device: BluetoothDevice) {
        callback?.print("connectDevice")
        var gat = mGattMap[device]
        var mGattClientCallback = mGattClientCallbackMap[device]
        if (mGattClientCallback == null) {
            mGattClientCallback = GattClientCallback()
            mGattClientCallbackMap[device] = mGattClientCallback
        }
//        if (gat == null) {
        val mGatt = device.connectGatt(context, false, mGattClientCallbackMap[device])
        mGattMap[device] = mGatt
        mGattDeviceMap[mGatt] = device
//        }
    }

    inner class GattClientCallback : BluetoothGattCallback() {
        override fun onCharacteristicRead(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int) {
            super.onCharacteristicRead(gatt, characteristic, status)
            if (status == BluetoothGatt.GATT_SUCCESS) {
                callback?.print("Client onCharacteristicRead: Wrote GATT Characteristic successfully.")
            } else {
                callback?.print("Client onCharacteristicRead: Error writing GATT Characteristic: " + status);
            }

        }

        override fun onCharacteristicWrite(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int) {
            super.onCharacteristicWrite(gatt, characteristic, status)
            if (status == BluetoothGatt.GATT_SUCCESS) {
                callback?.print("Client onCharacteristicWrite: Wrote GATT Characteristic successfully.")
                if(characteristic!!.uuid == CHARACTERISTIC_UUID){

                }else if (characteristic.uuid == DESCRIPTOR_UUID){
                    sendUserMetaData(gatt)
                }
            } else {
                callback?.print("Client onCharacteristicWrite: Error writing GATT Characteristic: " + status);
            }
        }

        override fun onDescriptorWrite(gatt: BluetoothGatt?, descriptor: BluetoothGattDescriptor?, status: Int) {
            super.onDescriptorWrite(gatt, descriptor, status)
            if (status == BluetoothGatt.GATT_SUCCESS) {
                //Device Added success fully
                callback?.print("Client onDescriptorWrite: Wrote GATT Descriptor successfully.")
                if(descriptor!!.uuid == USER_META_DATA_DESCRIPTOR_UUID){
                    sendUserMetaData(gatt)
                }
            } else {
                callback?.print("Client CalonDescriptorWrite: Error writing GATT Descriptor: " + status);
            }

        }

        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            callback?.print("onConnectionStateChange, status = $status, newState = $newState")
            if (status == BluetoothGatt.GATT_FAILURE) {
                disconnectGattServer(gatt)
                return
            } else if (status != BluetoothGatt.GATT_SUCCESS) {
                disconnectGattServer(gatt, true)
                return
            }
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                mConnectedMap[gatt] = true
                gatt.discoverServices()
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                disconnectGattServer(gatt, true)
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            super.onServicesDiscovered(gatt, status)
            callback?.print("onServicesDiscovered, status = $status")
            if (status != BluetoothGatt.GATT_SUCCESS) {
                return
            }
            gatt?.apply {
                //print available services
                printAvailableServices(gatt)
                val service = gatt.getService(SERVICE_UUID)
//                var characteristic = service.getCharacteristic(CHARACTERISTIC_UUID)

//                characteristic?.apply {
//                    characteristic.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
//                    val mInitialized = gatt.setCharacteristicNotification(characteristic, true)
//                    callback?.print("onServicesDiscovered, CH write success initiated = $mInitialized")
//
//                    var descriptor = characteristic.getDescriptor(DESCRIPTOR_UUID)?.apply {
//                        value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
//                    }
//                    if (descriptor != null) {
//                        val descriptorWriteStartedSuccess = writeDescriptor(descriptor)
//                        callback?.print("onServicesDiscovered, DESC write success initiated = $descriptorWriteStartedSuccess")
//                    } else {
//                        callback?.print("onServicesDiscovered DESCRIPTOR is null")
//                    }
//                }

                var metaDataCh = service.getCharacteristic(USER_META_DATA_UUID)
                metaDataCh?.apply {
                    metaDataCh.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
                    val mInitialized = gatt.setCharacteristicNotification(metaDataCh, true)
//                    val writeCh_Success = writeCharacteristic(metaDataCh)
                    callback?.print("onServicesDiscovered, CH write success initiated = $mInitialized")
//                    callback?.print("onServicesDiscovered, CH write success writeCh_Success = $writeCh_Success")

                    var descriptor = metaDataCh.getDescriptor(USER_META_DATA_DESCRIPTOR_UUID)?.apply {
                                                value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                    }
                    if (descriptor != null) {
                        val descriptorWriteStartedSuccess = writeDescriptor(descriptor)
                        callback?.print("onServicesDiscovered, DESC write success initiated = $descriptorWriteStartedSuccess")
                    } else {
                        callback?.print("onServicesDiscovered DESCRIPTOR is null")
                    }

                }

            }
        }

        override fun onCharacteristicChanged(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?) {
            super.onCharacteristicChanged(gatt, characteristic)
            callback?.print("onCharacteristicChanged")
            characteristic?.apply {
                when(characteristic.uuid){
                    CHARACTERISTIC_UUID -> {
                        //meta data received
                        val messageBytes = characteristic.value
                        var messageString: String? = null
                        try {
                            messageString = messageBytes.toString()
                        } catch (e: UnsupportedEncodingException) {
                            Timber.e("Unable to convert message bytes to string")
                        }
                        Timber.d("Received message: " + messageString)


                    }
                }
            }

        }

        override fun onDescriptorRead(gatt: BluetoothGatt?, descriptor: BluetoothGattDescriptor?, status: Int) {
            super.onDescriptorRead(gatt, descriptor, status)
        }
    }

    fun printAvailableServices(gatt: BluetoothGatt) {
        var services = gatt.services
        if (services != null) {
            services.forEach {
                it?.apply {
                    var serviceUUID = it.uuid
                    serviceUUID?.let {
                        Timber.d("Found service UUID = $it")
                    }
                    var serviceCharacteristics = it.characteristics
                    serviceCharacteristics?.apply {
                        forEach {
                            var characteristicUUID = it?.uuid
                            characteristicUUID?.let {
                                Timber.d("Found characteristic UUID = $it")
                            }
                        }
                    }
                }
            }
        }
    }

    fun disconnectGattServer(gatt: BluetoothGatt, retry:Boolean = false) {
        Timber.d("disconnectGattServer")
        callback?.print("disconnectGattServer")
        mConnectedMap[gatt] = false
        if(retry ){
            Timber.d("Reconnecting!!")
            gatt.connect()
        }else {
            gatt.disconnect()
            gatt.close()
        }

    }

    fun sendMessage(bleDevice: BluetoothDevice) {
        sendUserMetaData(mGattMap[bleDevice])
    }

    fun sendGenericMessage(message:String, gatt:BluetoothGatt?, characteristicUuid:UUID = CHARACTERISTIC_UUID ){
        try{
            if(gatt!=null){
                callback?.print("Sending Generic message to ${gatt.device?.address}")
                val mConnected = mConnectedMap[gatt]
                if ((mConnected == null || !mConnected) && !mDevices.contains(gatt.device)) {
                    return
                }
                val service = gatt.getService(SERVICE_UUID)
                if (service != null) {
                    val characteristic = service.getCharacteristic(characteristicUuid)
                    var messageBytes = ByteArray(0)
                    try {
                        messageBytes = message.toByteArray(Charset.forName("UTF-8"))
                    } catch (e: UnsupportedEncodingException) {
                        Timber.e("Failed to convert message string to byte array")
                    }
                    characteristic?.value = messageBytes
                    characteristic?.writeType = BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
                    val success = gatt.writeCharacteristic(characteristic)
                    callback?.print("Sending message to ${gatt.device.address} SUCCESS = $success")
                } else {
                    callback?.print("service is null")
                }
            }else{
                callback?.print("Send Generic message - GATT is null")
            }
        }catch (E:NullPointerException){
            callback?.print("EXCEPTION - GATT is null")
            Timber.e("EXCEPTION - GATT is null")
        }

    }

    fun sendUserMetaData(gatt: BluetoothGatt?){
        var json = Gson().toJson(BleMeshActivity.fakeUser)
        json = "Hi this is Iron man, from Avengers and I love my suit. And I love to play video games"
        sendGenericMessage(json, gatt, USER_META_DATA_UUID)
    }

    interface BleMeshControllerCallback {
        fun print(message: String)
        fun onDeviceAdded(added: Boolean, bleDevice: BluetoothDevice)
        fun onConnectionUpdated(connectedBleDevicesSet: HashSet<BluetoothDevice>)
        fun onScanStarted(scanStarted: Boolean)
    }


}