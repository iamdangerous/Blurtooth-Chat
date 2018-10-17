package com.rahul.`in`.bluetooth_demo.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.rahul.`in`.bluetooth_demo.repository.BleUserRepository
import com.rahul.`in`.bluetooth_demo.room.db.BleChatDatabase
import com.rahul.`in`.bluetooth_demo.room.entity.BleUser
import kotlinx.coroutines.experimental.*
import kotlinx.coroutines.experimental.android.Main
import kotlin.coroutines.experimental.CoroutineContext

class BleUserViewModel(application: Application):AndroidViewModel(application){
    private val repository:BleUserRepository
    val allUsers: LiveData<ArrayList<BleUser>>
    private var parentJob = Job()
    private val coroutineContext: CoroutineContext
        get() = parentJob + Dispatchers.Main
    private val scope = CoroutineScope(coroutineContext)

    init {
        val bleUserDao = BleChatDatabase.getDatabase(application,scope).bleUserDao()
        repository = BleUserRepository(bleUserDao)
        allUsers = repository.allUsers
    }

    fun insert(bleUser: BleUser) = scope.launch(Dispatchers.IO) { repository.insert(bleUser) }

    fun removeUser(bleUser: BleUser) = scope.launch(Dispatchers.IO) { repository.deleteUser(bleUser) }

    override fun onCleared() {
        super.onCleared()
        parentJob.cancel()
    }
}