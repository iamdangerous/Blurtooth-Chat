package com.rahul.`in`.bluetooth_demo.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.rahul.`in`.bluetooth_demo.room.db.BleChatDatabase
import com.rahul.`in`.bluetooth_demo.room.entity.BleMessage
import com.rahul.bluetooth_demo.repository.BleChatRepository
import kotlinx.coroutines.experimental.*
import kotlinx.coroutines.experimental.android.Main
import kotlin.coroutines.experimental.CoroutineContext

class BleChatViewModel (otherId:String, otherType:Int, app:Application):AndroidViewModel(app){
    val repositoryBle : BleChatRepository
    val allBleMessages: LiveData<ArrayList<BleMessage>>

    private var parentJob = Job()
    private val coroutineContext: CoroutineContext
        get() = parentJob + Dispatchers.Main
    private val scope = CoroutineScope(coroutineContext)


    init {
        val messageDao = BleChatDatabase.getDatabase(app, scope).messageDao()
        repositoryBle = BleChatRepository(messageDao)
        allBleMessages = repositoryBle.getUserMessages(otherId, otherType)
    }

    fun insert(bleMessage:BleMessage) = scope.launch(Dispatchers.IO) { repositoryBle.insert(bleMessage) }

    fun removeMessage(bleMessage:BleMessage) = scope.launch(Dispatchers.IO) { repositoryBle.deleteMessage(bleMessage) }

    override fun onCleared() {
        super.onCleared()
        parentJob.cancel()
    }
}