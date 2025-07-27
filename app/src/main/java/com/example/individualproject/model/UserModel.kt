package com.example.individualproject.model

data class UserModel(
    var userID : String = "",
    var email : String = "",
    var firstName : String = "",
    var lastName : String = "",
    var gender : String = "",
    var address : String = "",
    var role: String = "Normal"
)