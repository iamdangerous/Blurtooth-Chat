package com.rahul.`in`.bluetooth_demo.repository

import androidx.lifecycle.LiveData
import androidx.annotation.WorkerThread
import com.rahul.`in`.bluetooth_demo.room.dao.BleMessageDao
import com.rahul.`in`.bluetooth_demo.room.entity.BleMessage

open class BleMessageRepository(private val bleMessageDao: BleMessageDao){

    val allMessages: LiveData<ArrayList<BleMessage>> = bleMessageDao.getAllMessages()

    @WorkerThread
    suspend fun insert(BleMessage: BleMessage) {
        bleMessageDao.insert(BleMessage)
    }

    @WorkerThread
    suspend fun deleteMessage(BleMessage: BleMessage) {
        bleMessageDao.deleteMessage(BleMessage)
    }
}