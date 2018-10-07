package com.rahul.`in`.bluetooth_demo.util

import timber.log.Timber


class MyDebugTree : Timber.DebugTree() {
    override fun createStackElementTag(element: StackTraceElement): String? {
        return String.format("[L:%s] [M:%s] [C:%s]",
                element.lineNumber,
                element.methodName,
                super.createStackElementTag(element))
    }
}