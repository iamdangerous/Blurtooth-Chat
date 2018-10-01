package com.rahul.`in`.bluetooth_demo.room.dao

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Delete
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Query
import com.rahul.`in`.bluetooth_demo.room.entity.Message

@Dao
interface MessageDao{

    @Query("SELECT * from message ORDER BY createdAtTime ASC")
    fun getAllMessages(): LiveData<List<Message>>

    @Insert
    fun insert(message: Message)

    @Delete()
    fun deleteMessage(message: Message)

    @Query("DELETE FROM message")
    fun deleteAll()
}