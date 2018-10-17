package com.rahul.`in`.bluetooth_demo.eventBus

import android.bluetooth.BluetoothDevice

data class EventOnBleDeviceAdded(val added: Boolean, val bleDevice: BluetoothDevice)