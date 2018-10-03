package com.rahul.`in`.bluetooth_demo.room.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.rahul.`in`.bluetooth_demo.room.entity.BleMessage

@Dao
interface BleMessageDao{

    @Query("SELECT * FROM ble_message ORDER BY createdAtTime ASC")
    fun getAllMessages(): LiveData<ArrayList<BleMessage>>

    @Insert
    fun insert(bleMessage: BleMessage)

    @Delete()
    fun deleteMessage(bleMessage: BleMessage)

    @Query("DELETE FROM ble_message")
    fun deleteAll()

    @Query("SELECT * FROM ble_message WHERE otherId = :otherId AND otherIdType = :otherType")
    fun getUserMessages(otherId:String, otherType:Int): LiveData<ArrayList<BleMessage>>

}