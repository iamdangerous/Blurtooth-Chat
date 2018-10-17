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
        fakeUsersList.add(FakeUserModel(1, "SpiderMan", "spiderman@gmail.com", "1111111110", "Spidey", "https://i.ytimg.com/vi/K4zm30yeHHE/maxresdefault.jpg"))
        fakeUsersList.add(FakeUserModel(2, "RockWwe", "rock@gmail.com", "1111111111", "Rock", "https://givemesport.azureedge.net/images/18/04/21/370d9ad3b4d9882bca95aa2f13bfafc9/960.jpg"))
        fakeUsersList.add(FakeUserModel(3, "TripleHWwe", "tripleh@gmail.com", "1111111112", "TripleH", "https://givemesport.azureedge.net/images/18/04/21/370d9ad3b4d9882bca95aa2f13bfafc9/960.jpg"))
        fakeUsersList.add(FakeUserModel(4, "BrockLesnarWwe", "brocklesnar@gmail.com", "1111111113", "Brock Lesnar", "https://cdn.bleacherreport.net/images/team_logos/328x328/brock_lesnar.png"))
    }
}