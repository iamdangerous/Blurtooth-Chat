package com.rahul.`in`.bluetooth_demo.room.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.rahul.`in`.bluetooth_demo.room.entity.BleMessage
import com.rahul.`in`.bluetooth_demo.room.entity.BleUser
import io.reactivex.Maybe
import io.reactivex.Single

@Dao
interface BleUserDao{

    @Query("SELECT * FROM ble_user ORDER BY createdAt ASC")
    fun getAllUsers(): LiveData<ArrayList<BleUser>>

    @Insert
    fun insert(bleUser: BleUser)

    @Query("SELECT * FROM ble_user WHERE userName =:userName LIMIT 1")
    fun getUser(userName:String): BleUser?

    @Delete()
    fun deleteUser(bleUser: BleUser)

    @Update(onConflict = OnConflictStrategy.IGNORE)
    fun updateUser(bleUser: BleUser)

    @Query("DELETE FROM ble_user")
    fun deleteAll()

}