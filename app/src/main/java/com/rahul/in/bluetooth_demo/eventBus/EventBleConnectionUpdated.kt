package com.rahul.`in`.bluetooth_demo.eventBus

import android.bluetooth.BluetoothDevice

data class EventBleConnectionUpdated(val connectedBleDevicesSet: HashSet<BluetoothDevice>)