package com.rahul.`in`.bluetooth_demo.room.dao

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Delete
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Query
import com.rahul.`in`.bluetooth_demo.room.entity.BleMessage

@Dao
interface BleMessageDao{

    @Query("SELECT * from ble_message ORDER BY createdAtTime ASC")
    fun getAllMessages(): LiveData<ArrayList<BleMessage>>

    @Insert
    fun insert(bleMessage: BleMessage)

    @Delete()
    fun deleteMessage(bleMessage: BleMessage)

    @Query("DELETE FROM ble_message")
    fun deleteAll()
}