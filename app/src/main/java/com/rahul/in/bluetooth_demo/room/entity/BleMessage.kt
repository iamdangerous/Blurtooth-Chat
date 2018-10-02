package com.rahul.`in`.bluetooth_demo.room.entity

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import android.text.TextUtils

@Entity(tableName = "ble_message")
data class BleMessage(
        @PrimaryKey @ColumnInfo(name = "messageId") val messageId: String,
        @ColumnInfo(name = "createdAtTime") val createdAtTime: String,
        @ColumnInfo(name = "to") var to: String,
        @ColumnInfo(name = "senderId") var senderId: String,
        @ColumnInfo(name = "message") val message: String,
        @ColumnInfo(name = "from") var from: String,
        @ColumnInfo(name = "receiverId") var receiverId: String,
        @ColumnInfo(name = "isRead") var isRead: Boolean,
        @ColumnInfo(name = "isSent") var isSent: Boolean
        )
{
        fun isOutbox() = (TextUtils.isEmpty(senderId))
}