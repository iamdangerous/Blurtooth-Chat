package com.rahul.`in`.bluetooth_demo.fake

class FakeDataProvider() {

    val fakeUsersList = arrayListOf<FakeUserModel>()

    init {
        prepareFakeUsers()
    }

    fun provideFakeUser(id: Int): FakeUserModel {
        return fakeUsersList.find { it-> it.id == id }!!
    }

    fun prepareFakeUsers() {
        fakeUsersList.add(FakeUserModel(1, "RahulLohra", "rahul@gmail.com", "9999676446", "Rahul"))
        fakeUsersList.add(FakeUserModel(2, "RockWwe", "rock@gmail.com", "1111111111", "Rock"))
        fakeUsersList.add(FakeUserModel(3, "TripleHWwe", "tripleh@gmail.com", "1111111112", "TripleH"))
        fakeUsersList.add(FakeUserModel(4, "BrockLesnarWwe", "brocklesnar@gmail.com", "1111111113", "Brock Lesnar"))
    }
}