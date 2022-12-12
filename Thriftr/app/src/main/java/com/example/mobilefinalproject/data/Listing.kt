package com.example.mobilefinalproject.data

data class Listing(
    var uid: String = "",
    var email: String = "",
    var seller: String = "",
    var itemTitle: String = "",
    var itemDesc: String = "",
    var itemPrice: Float = 0.0f,
    var imgUrl: String = ""
)