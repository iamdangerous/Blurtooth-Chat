package com.rahul.`in`.bluetooth_demo.viewModel

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import com.rahul.`in`.bluetooth_demo.repository.BleMessageRepository
import com.rahul.`in`.bluetooth_demo.room.db.BleChatDatabase
import com.rahul.`in`.bluetooth_demo.room.entity.BleMessage
import kotlinx.coroutines.experimental.*
import kotlinx.coroutines.experimental.android.Main
import kotlin.coroutines.experimental.CoroutineContext

class BleMessageListViewModel(app:Application) :AndroidViewModel(app){
    private val repositoryBle:BleMessageRepository
    val allBleMessages: LiveData<ArrayList<BleMessage>>
    private var parentJob = Job()
    private val coroutineContext: CoroutineContext
        get() = parentJob + Dispatchers.Main
    private val scope = CoroutineScope(coroutineContext)

    init {
        val wordDao = BleChatDatabase.getDatabase(app,scope).messageDao()
        repositoryBle = BleMessageRepository(wordDao)
        allBleMessages = repositoryBle.allMessages
    }

    fun insert(bleMessage:BleMessage) = scope.launch(Dispatchers.IO) { repositoryBle.insert(bleMessage) }

    fun removeMessage(bleMessage:BleMessage) = scope.launch(Dispatchers.IO) { repositoryBle.deleteMessage(bleMessage) }

    override fun onCleared() {
        super.onCleared()
        parentJob.cancel()
    }
}