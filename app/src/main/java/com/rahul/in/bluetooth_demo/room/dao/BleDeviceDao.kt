package com.rahul.`in`.bluetooth_demo.room.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.rahul.`in`.bluetooth_demo.room.entity.BleDevice
import com.rahul.`in`.bluetooth_demo.room.entity.BleMessage

@Dao
interface BleDeviceDao{
    @Query("SELECT * FROM ble_device ORDER BY createdAt ASC")
    fun getAllDevices(): LiveData<ArrayList<BleDevice>>

    @Insert
    fun insert(bleDevice: BleDevice)

    @Delete()
    fun deleteDevice(bleDevice: BleDevice)

    @Query("SELECT * FROM ble_device WHERE otherId = :otherId")
    fun getDevice(otherId:String, otherType:Int): LiveData<BleDevice>

}