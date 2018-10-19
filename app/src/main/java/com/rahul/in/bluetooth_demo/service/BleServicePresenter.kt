package com.rahul.`in`.bluetooth_demo.service

import android.annotation.SuppressLint
import android.app.Application
import com.rahul.`in`.bluetooth_demo.room.db.BleChatDatabase
import com.rahul.`in`.bluetooth_demo.room.entity.BleUser
import io.reactivex.Completable
import io.reactivex.Scheduler
import io.reactivex.functions.Action
import io.reactivex.schedulers.Schedulers

class BleServicePresenter(application: Application) {

    val bleUserDao = BleChatDatabase.getDatabase(application).bleUserDao()

    @SuppressLint("CheckResult")
    fun insertUser(bleUser: BleUser){
        Completable.fromAction {
            var user = bleUserDao.getUser(bleUser.userName)
            if(user == null){
                bleUserDao.insert(bleUser)
            }else{
                bleUserDao.updateUser(bleUser)
            }
        }.subscribeOn(Schedulers.io()).subscribe({
            //success
        },{
            //error
        })
    }
}