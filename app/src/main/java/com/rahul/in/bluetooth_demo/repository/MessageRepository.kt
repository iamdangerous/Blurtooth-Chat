package com.rahul.`in`.bluetooth_demo.repository

import android.arch.lifecycle.LiveData
import android.support.annotation.WorkerThread
import com.rahul.`in`.bluetooth_demo.room.dao.MessageDao
import com.rahul.`in`.bluetooth_demo.room.entity.Message

class MessageRepository(private val messageDao: MessageDao){

    val allMessages: LiveData<List<Message>> = messageDao.getAllMessages()

    @WorkerThread
    suspend fun insert(Message: Message) {
        messageDao.insert(Message)
    }

    @WorkerThread
    suspend fun deleteMessage(Message: Message) {
        messageDao.deleteMessage(Message)
    }
}