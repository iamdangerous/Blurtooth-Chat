package com.rahul.`in`.bluetooth_demo.room.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.rahul.`in`.bluetooth_demo.room.entity.BleMessage
import com.rahul.`in`.bluetooth_demo.room.entity.BleUser

@Dao
interface BleUserDao{

    @Query("SELECT * FROM ble_user ORDER BY createdAt ASC")
    fun getAllUsers(): LiveData<ArrayList<BleUser>>

    @Insert
    fun insert(bleUser: BleUser)

    @Delete()
    fun deleteMessage(bleUser: BleUser)

    @Query("DELETE FROM ble_user")
    fun deleteAll()

}