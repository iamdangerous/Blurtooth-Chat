package com.rahul.`in`.bluetooth_demo.util

import java.text.SimpleDateFormat
import java.util.*

class TimeUtil{
    companion object {
        private val SECOND = 1000
        private val MINUTE = 60 * SECOND
        private val HOUR = 60 * MINUTE
        private val DAY = 24 * HOUR
        val ISO_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
        val locale = Locale.getDefault()
        val TIMEZONE = TimeZone.getTimeZone("GMT")

        fun getCurrentUTCTimestamp(): String {
            val dateFormatGmt = SimpleDateFormat(ISO_FORMAT, locale)
            dateFormatGmt.timeZone = TIMEZONE
            return dateFormatGmt.format(Date())
        }

        fun getUTCTimestamp(date: Date): String {
            val dateFormatGmt = SimpleDateFormat(ISO_FORMAT, locale)
            dateFormatGmt.timeZone = TIMEZONE
            return dateFormatGmt.format(date)
        }
    }
}