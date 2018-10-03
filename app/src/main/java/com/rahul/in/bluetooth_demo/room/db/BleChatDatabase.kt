package com.rahul.`in`.bluetooth_demo.room.db

import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import com.rahul.`in`.bluetooth_demo.room.dao.BleMessageDao
import com.rahul.`in`.bluetooth_demo.room.entity.BleMessage
import kotlinx.coroutines.experimental.CoroutineScope
import kotlinx.coroutines.experimental.Dispatchers
import kotlinx.coroutines.experimental.IO
import kotlinx.coroutines.experimental.launch

@Database(entities = arrayOf(BleMessage::class), version = 1)
public abstract class BleChatDatabase : RoomDatabase() {

    abstract fun messageDao(): BleMessageDao

    companion object {
        @Volatile
        private var INSTANCE: BleChatDatabase? = null
        private val DB_NAME = "Ble_Chat_database"

        fun getDatabase(context: Context, scope: CoroutineScope): BleChatDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                        context.applicationContext,
                        BleChatDatabase::class.java,
                        DB_NAME
                ).addCallback(BleDatabaseCallback(scope))
                        .build()
                INSTANCE = instance
                return instance
            }
        }
    }

    class BleDatabaseCallback(private val scope: CoroutineScope):RoomDatabase.Callback(){
        override fun onOpen(db: SupportSQLiteDatabase) {
            super.onOpen(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
//                    populateDatabase(database.messageDao())
                }
            }
        }
        fun populateDatabase(bleMessageDao: BleMessageDao) {
//            bleMessageDao.deleteAll()
//            var word = Word("Hello")
//            wordDao.insert(word)
//            word = Word("World!")
//            wordDao.insert(word)
        }
    }

}
