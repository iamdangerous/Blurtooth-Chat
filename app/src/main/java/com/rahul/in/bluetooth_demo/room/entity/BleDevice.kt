package com.rahul.`in`.bluetooth_demo.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "ble_device")
data class BleDevice(
        @ColumnInfo(name = "createdAtTime") val createdAtTime: String,
        @ColumnInfo(name = "deviceId") val deviceId: String,
        @ColumnInfo(name = "otherId") val otherId: String,
        @ColumnInfo(name = "userName") val userName: String,
        @ColumnInfo(name = "userEmail") val userEmail: String,
        @ColumnInfo(name = "avatarUrl") val avatarUrl: String,
        @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") val id: String = "0"
        )