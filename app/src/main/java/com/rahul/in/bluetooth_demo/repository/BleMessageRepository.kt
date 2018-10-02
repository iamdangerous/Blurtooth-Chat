package com.rahul.`in`.bluetooth_demo.repository

import android.arch.lifecycle.LiveData
import android.support.annotation.WorkerThread
import com.rahul.`in`.bluetooth_demo.room.dao.BleMessageDao
import com.rahul.`in`.bluetooth_demo.room.entity.BleMessage

class BleMessageRepository(private val bleMessageDao: BleMessageDao){

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