package com.rahul.`in`.bluetooth_demo.bleControllers

import android.bluetooth.*
import android.bluetooth.BluetoothGatt.GATT_SUCCESS
import android.bluetooth.le.*
import android.content.Context
import android.os.Handler
import android.os.ParcelUuid
import com.rahul.`in`.bluetooth_demo.activity.BleMeshActivity
import com.rahul.`in`.bluetooth_demo.bleControllers.BleMeshController.MessageQueueData.Companion.MESSAGE_BETWEEN
import com.rahul.`in`.bluetooth_demo.bleControllers.BleMeshController.MessageQueueData.Companion.MESSAGE_END
import com.rahul.`in`.bluetooth_demo.bleControllers.BleMeshController.MessageQueueData.Companion.MESSAGE_SINGLE
import com.rahul.`in`.bluetooth_demo.bleControllers.BleMeshController.MessageQueueData.Companion.MESSAGE_START
import com.rahul.`in`.bluetooth_demo.util.BleMessageUtil
import org.json.JSONObject
import timber.log.Timber
import java.io.UnsupportedEncodingException
import java.nio.charset.Charset
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.collections.HashSet


class BleMeshController(context: Context, mBluetoothManager: BluetoothManager, mBluetoothAdapter: BluetoothAdapter) : BaseBleController(context, mBluetoothManager, mBluetoothAdapter) {

    companion object {
        const val DEFAULT_CHUNK_SIZE = 20
        private val SERVICE_ID = "0d2bd6b0-6120-4ed2-90bc-7bb42d25261d"
        private val CHARACTERISTIC_ID = "f4c5fb1f-ac1e-475a-b442-167b829642e6"
        private val DESCRIPTOR_ID = "c9c19a7d-8013-4d1d-a1db-f72434c188fd"
        private val USER_META_DATA_ID = "02deb964-eca6-4e90-bb99-899aafd6f118"
        private val USER_META_DATA_DESCRIPTOR_ID = "5acef6cd-3019-45ab-869e-6d80aba85b08"
        private val ONE_TO_ONE_MSG_ID = "83eaf60f-f30f-42ef-8a54-977360dcd370"

        val SERVICE_UUID = UUID.fromString(SERVICE_ID)
        val CHARACTERISTIC_UUID = UUID.fromString(CHARACTERISTIC_ID)
        val DESCRIPTOR_UUID = UUID.fromString(DESCRIPTOR_ID)
        val USER_META_DATA_UUID = UUID.fromString(USER_META_DATA_ID)
        val USER_META_DATA_DESCRIPTOR_UUID = UUID.fromString(USER_META_DATA_DESCRIPTOR_ID)
        val ONE_TO_ONE_MSG_UUID = UUID.fromString(ONE_TO_ONE_MSG_ID)

    }

    var mScanning = false
//      val ONE_TO_ONE_MSG_UUID = UUID.fromString(ONE_TO_ONE_MSG_ID)
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
    val outboxMessageQueue = ArrayList<MessageQueueData>()
    val inboxMessageQueue = ArrayList<MessageQueueData>()
    var MAX_TRANSFER_BYTE = 18


    override fun startScanProcess() {
        if (mScanning) {
            return
        }
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
        if (mBluetoothAdapter.isMultipleAdvertisementSupported) {

            mBluetoothLeAdvertiser = mBluetoothAdapter.bluetoothLeAdvertiser
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

        mBluetoothLeAdvertiser?.startAdvertising(settings, data, mAdvertiseCallback)
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

        service.addCharacteristic(prepareCharateristic(CHARACTERISTIC_UUID, DESCRIPTOR_UUID))
        service.addCharacteristic(prepareCharateristic(USER_META_DATA_UUID, USER_META_DATA_DESCRIPTOR_UUID))

        mGattServer?.addService(service)
    }

    fun prepareCharateristic(characteristicUuid: UUID, descriptorUuid: UUID): BluetoothGattCharacteristic {
        val userMetaDataCharacteristic = BluetoothGattCharacteristic(
                characteristicUuid,
                BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE,
                BluetoothGattCharacteristic.PERMISSION_WRITE)

        userMetaDataCharacteristic.writeType = BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE

        val metaDataDescriptor = BluetoothGattDescriptor(descriptorUuid, BluetoothGattDescriptor.PERMISSION_READ or BluetoothGattDescriptor.PERMISSION_WRITE)
        userMetaDataCharacteristic.addDescriptor(metaDataDescriptor)
        return userMetaDataCharacteristic
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

        //RECEIVER END
        override fun onCharacteristicWriteRequest(device: BluetoothDevice?, requestId: Int, characteristic: BluetoothGattCharacteristic?, preparedWrite: Boolean, responseNeeded: Boolean, offset: Int, value: ByteArray?) {
            super.onCharacteristicWriteRequest(device, requestId, characteristic, preparedWrite, responseNeeded, offset, value)
            callback?.print("onCharacteristicWriteRequest")
            val success = mGattServer?.sendResponse(device, requestId, GATT_SUCCESS, 0, value)

            var textReceived = value?.toString(Charset.forName("UTF-8"))

            Timber.d("Server onCharacteristicWriteRequest, success = $success, value = ${textReceived}, device id = ${device!!.address} NEW")
            characteristic?.value = value
            if (textReceived != null) {
                callback?.print(textReceived)
            }
            if (value != null) {
                processInboxByte(value, characteristic!!.uuid)

            }
            mBluetoothAdapter.bondedDevices.map {
                if (it != null) {
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
            val device = result.device
            val deviceAddress = device.address
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
                Timber.d("mScannedDevices id = ${device.address} NEW")
                mScannedDevices.add(device)
                callback?.print("onLeScan")
                callback?.onDeviceAdded(true, device)
                Handler().postDelayed({ connectDevice(device) }, 1000)
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
        Timber.d("connectDevice id = ${device.address} NEW")
        val mGatt = device.connectGatt(context, false, mGattClientCallbackMap[device])
        mGattMap[device] = mGatt
        mGattDeviceMap[mGatt] = device
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

        //SENDER
        override fun onCharacteristicWrite(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int) {
            super.onCharacteristicWrite(gatt, characteristic, status)
            if (status == BluetoothGatt.GATT_SUCCESS) {
                callback?.print("Client onCharacteristicWrite: Wrote GATT Characteristic successfully.")

                if (characteristic!!.uuid == CHARACTERISTIC_UUID) {

                } else if (characteristic.uuid == USER_META_DATA_UUID) {
                    outboxMessageQueue.removeAt(0)
                    sendAllMessagesOfQueue(gatt)
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
                if (descriptor!!.uuid == USER_META_DATA_DESCRIPTOR_UUID) {
//                    sendUserMetaData(gatt)
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
                writeFirstCharacteristic(this)
            }
        }


        override fun onCharacteristicChanged(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?) {
            super.onCharacteristicChanged(gatt, characteristic)
            callback?.print("onCharacteristicChanged")
            characteristic?.apply {
                when (characteristic.uuid) {
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

    fun sendAllMessagesOfQueue(gatt: BluetoothGatt?) {
        if (outboxMessageQueue.size > 0) {
            val data = outboxMessageQueue[0]
            sendGenericMessage(gatt = gatt, bytes = data!!.bytes)
        }
    }

    fun writeFirstCharacteristic(gatt: BluetoothGatt) {
        val service = gatt.getService(SERVICE_UUID)
        var metaDataCh = service.getCharacteristic(USER_META_DATA_UUID)
        metaDataCh?.apply {
            metaDataCh.writeType = BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
            val mInitialized = gatt.setCharacteristicNotification(metaDataCh, true)
            callback?.print("onServicesDiscovered, CH write success initiated = $mInitialized")
//                    callback?.print("onServicesDiscovered, CH write success writeCh_Success = $writeCh_Success")

            var descriptor = metaDataCh.getDescriptor(USER_META_DATA_DESCRIPTOR_UUID)?.apply {
                value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
            }
            if (descriptor != null) {
                val descriptorWriteStartedSuccess = gatt.writeDescriptor(descriptor)
                callback?.print("onServicesDiscovered, DESC write success initiated = $descriptorWriteStartedSuccess")
            } else {
                callback?.print("onServicesDiscovered DESCRIPTOR is null")
            }

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

    fun disconnectGattServer(gatt: BluetoothGatt, retry: Boolean = false) {
        Timber.d("disconnectGattServer")
        callback?.print("disconnectGattServer")
        mConnectedMap[gatt] = false
        if (retry) {
            Timber.d("Reconnecting!!")
            gatt.connect()
        } else {
            gatt.disconnect()
            gatt.close()
        }

    }

    fun sendMessage(bleDevice: BluetoothDevice) {
        sendUserMetaData(mGattMap[bleDevice])
    }

    private fun sendGenericMessage(gatt: BluetoothGatt?, characteristicUuid: UUID = USER_META_DATA_UUID, bytes: ByteArray? = null) {
        try {
            if (gatt != null) {
                callback?.print("Sending Generic message to ${gatt.device?.address}")
                val mConnected = mConnectedMap[gatt]
                if ((mConnected == null || !mConnected) && !mDevices.contains(gatt.device)) {
                    return
                }
                val service = gatt.getService(SERVICE_UUID)
                if (service != null) {
                    val characteristic = service.getCharacteristic(characteristicUuid)
                    characteristic?.value = bytes
                    characteristic?.writeType = BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
                    val success = gatt.writeCharacteristic(characteristic)
                    callback?.print("Sending message to ${gatt.device.address} SUCCESS = $success")
                } else {
                    callback?.print("service is null")
                }
            } else {
                callback?.print("Send Generic message - GATT is null")
            }
        } catch (E: NullPointerException) {
            callback?.print("sendGenericMessage EXCEPTION - GATT is null")
            Timber.e("sendGenericMessage EXCEPTION - GATT is null")
        }
    }

    fun sendUserMetaData(gatt: BluetoothGatt?) {
        val jsonObject = JSONObject()
        jsonObject.put("n", BleMeshActivity.fakeUser!!.name)
        jsonObject.put("un", BleMeshActivity.fakeUser!!.userName)
        jsonObject.put("e", BleMeshActivity.fakeUser!!.email)
//        jsonObject.put("au", BleMeshActivity.fakeUser!!.avatarUrl)
        //break the message into 20 bytes val and enqueue them in the queue
        enqueueMessage(jsonObject.toString(), gatt, USER_META_DATA_UUID)
    }

    fun enqueueMessage(message: String, gatt: BluetoothGatt?, characteristicUuid: UUID) {
        var messageByte = message.toByteArray(Charset.defaultCharset())

        if (messageByte.size > MAX_TRANSFER_BYTE) {

            val chunkSize = 20
            val lisOfByteArray = BleMessageUtil.stringToListOfByteArray(message, chunkSize)

            var index = 0

            while (index < lisOfByteArray.size) {
                if (index == 0) {
                    //start
                    val byteArray = lisOfByteArray[index]
                    val messageQueueData = MessageQueueData("",byteArray.toString(Charset.defaultCharset()), byteArray, MESSAGE_START, characteristicUuid = characteristicUuid)
                    outboxMessageQueue.add(messageQueueData)
                } else if (index == lisOfByteArray.size - 1) {
                    //end
                    val byteArray = lisOfByteArray[index]
                    val messageQueueData = MessageQueueData("",byteArray.toString(Charset.defaultCharset()), byteArray, MESSAGE_END, characteristicUuid = characteristicUuid)
                    outboxMessageQueue.add(messageQueueData)
                } else {
                    //between
                    val byteArray = lisOfByteArray[index]
                    val messageQueueData = MessageQueueData("",byteArray.toString(Charset.defaultCharset()), byteArray, MESSAGE_BETWEEN, characteristicUuid = characteristicUuid)
                    outboxMessageQueue.add(messageQueueData)
                }
                index = index + 1
            }
            sendMessageViaQueue(gatt)

        } else {
            val formattedMessage = "s3" + message
            messageByte = formattedMessage.toByteArray(Charset.defaultCharset())
            val messageQueueData = MessageQueueData("",formattedMessage, messageByte, MESSAGE_SINGLE, characteristicUuid = characteristicUuid)
            outboxMessageQueue.add(messageQueueData)
            sendMessageViaQueue(gatt)
        }
    }

    fun sendMessageViaQueue(gatt: BluetoothGatt?) {
        if (outboxMessageQueue.size > 0) {
            sendGenericMessage(gatt = gatt, bytes = outboxMessageQueue[0]!!.bytes, characteristicUuid = outboxMessageQueue[0]!!.characteristicUuid)
        }
    }

    fun processInboxByte(byteArray: ByteArray, characteristicUuid: UUID) {
        val messageQueueData = BleMessageUtil.byteArrayToMessageQueueData(byteArray, characteristicUuid)
        inboxMessageQueue.add(messageQueueData)
        if (messageQueueData.tag == MESSAGE_END) {
            popInboxMessageAndRead(characteristicUuid)
        } else if (messageQueueData.tag == MESSAGE_SINGLE) {
            popInboxMessageAndRead(characteristicUuid)
        } else {
        }
    }

    fun popInboxMessageAndRead(characteristicUuid: UUID, deviceId: String = "") {
        val listOfBytes = arrayListOf<ByteArray>()
        val listOfStrings = arrayListOf<String>()
        var index = 0
        while (index < inboxMessageQueue.size) {
            var inboxMessageQueueData = inboxMessageQueue[0]

            listOfBytes.add(inboxMessageQueueData.bytes)
            listOfStrings.add(inboxMessageQueueData.message)
            inboxMessageQueue.removeAt(0)

            if (inboxMessageQueueData.tag == MessageQueueData.MESSAGE_SINGLE) {
                inboxMessageQueue.clear()
                break
            } else if (inboxMessageQueueData.tag == MessageQueueData.MESSAGE_END) {
                inboxMessageQueue.clear()
                break
            }
        }

        BleMessageUtil.parseInboxDataWithUUID(listOfBytes, characteristicUuid)
    }

    interface BleMeshControllerCallback {
        fun print(message: String)
        fun onDeviceAdded(added: Boolean, bleDevice: BluetoothDevice)
        fun onConnectionUpdated(connectedBleDevicesSet: HashSet<BluetoothDevice>)
        fun onScanStarted(scanStarted: Boolean)
    }

    class MessageQueueData(val bleAddress: String, val message: String, val bytes: ByteArray, val tag: Int, val characteristicUuid: UUID) : Comparable<MessageQueueData> {
        override fun compareTo(other: MessageQueueData): Int {
            return 1
        }

        companion object {
            const val MESSAGE_START = 49
            const val MESSAGE_BETWEEN = 50
            const val MESSAGE_END = 51
            const val MESSAGE_SINGLE = 52
        }
    }


}