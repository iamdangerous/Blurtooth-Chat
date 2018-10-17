package com.rahul.`in`.bluetooth_demo.room.entity

import android.text.TextUtils
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.rahul.`in`.bluetooth_demo.util.TimeUtil

@Entity(tableName = "ble_message")
data class BleMessage(
        @ColumnInfo(name = "createdAt") val createdAt: String,
        @ColumnInfo(name = "senderId") var senderId: String,
        @ColumnInfo(name = "message") val message: String,
        @ColumnInfo(name = "receiverId") var receiverId: String,
        @ColumnInfo(name = "isRead") var isRead: Boolean,
        @ColumnInfo(name = "isSent") var isSent: Boolean,
        @ColumnInfo(name = "otherId") var otherId: String,
        @ColumnInfo(name = "otherName") var otherName: String,
        @ColumnInfo(name = "otherIdType") var otherIdType: Int = 0,
        @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "messageId") val messageId: String = "0"
) {
    fun isOutbox() = (TextUtils.isEmpty(senderId))

    companion object {
        fun createSendingMessage(text:String, receiverId:String, otherName: String) :BleMessage {
            val createdAt = TimeUtil.getCurrentUTCTimestamp()
            val ownerId = "Rahul"
            val senderEmail = "Rahul@gmail.com"
            return BleMessage(createdAt = createdAt,
                    senderId = ownerId,
                    message = text,
                    receiverId = receiverId,
                    isRead = true,
                    isSent = false,
                    otherId = receiverId,
                    otherName = otherName
            )
        }
    }

}