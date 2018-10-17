package com.rahul.`in`.bluetooth_demo.repository

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import com.rahul.`in`.bluetooth_demo.room.dao.BleUserDao
import com.rahul.`in`.bluetooth_demo.room.entity.BleUser

open class BleUserRepository(private val bleUserDao: BleUserDao) {

    val allUsers : LiveData<ArrayList<BleUser>> = bleUserDao.getAllUsers()

    @WorkerThread
    suspend fun insert(bleUser: BleUser) {
        bleUserDao.insert(bleUser)
    }

    @WorkerThread
    suspend fun deleteUser(bleUser: BleUser) {
        bleUserDao.deleteMessage(bleUser)
    }
}