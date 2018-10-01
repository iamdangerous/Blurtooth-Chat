package com.rahul.`in`.bluetooth_demo.room.db

import android.arch.persistence.db.SupportSQLiteDatabase
import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.content.Context
import com.rahul.`in`.bluetooth_demo.room.dao.MessageDao
import com.rahul.`in`.bluetooth_demo.room.entity.Message
import kotlinx.coroutines.experimental.CoroutineScope
import kotlinx.coroutines.experimental.Dispatchers
import kotlinx.coroutines.experimental.IO
import kotlinx.coroutines.experimental.launch

@Database(entities = arrayOf(Message::class), version = 1)
public abstract class BleChatDatabase : RoomDatabase() {

    abstract fun messageDao(): MessageDao

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
        fun populateDatabase(messageDao: MessageDao) {
//            messageDao.deleteAll()
//            var word = Word("Hello")
//            wordDao.insert(word)
//            word = Word("World!")
//            wordDao.insert(word)
        }
    }

}
