package com.rahul.bluetooth_demo.repository

import androidx.lifecycle.LiveData
import com.rahul.`in`.bluetooth_demo.repository.BleMessageRepository
import com.rahul.`in`.bluetooth_demo.room.dao.BleMessageDao
import com.rahul.`in`.bluetooth_demo.room.entity.BleMessage

class BleChatRepository(private val bleMessageDao: BleMessageDao) : BleMessageRepository(bleMessageDao) {

    fun getUserMessages(otherId:String, otherType:Int):LiveData<ArrayList<BleMessage>> {
        return bleMessageDao.getUserMessages(otherId,otherType)
    }

}