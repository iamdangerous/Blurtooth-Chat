package com.rahul.`in`.bluetooth_demo.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.rahul.`in`.bluetooth_demo.R
import com.rahul.`in`.bluetooth_demo.fragment.MessageListFragment

class MessageListActivity : AppCompatActivity() {

    lateinit var fragment:MessageListFragment
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message_list)
        fragment = MessageListFragment()

        setupFragment()
    }

    fun setupFragment(){
        supportFragmentManager.beginTransaction()
                .replace(R.id.frame_container,fragment)
                .commit()
    }
}
