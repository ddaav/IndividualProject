package com.example.individualproject.model

data class CartModel(
    val cartId: String = "",
    val productId: String = "",
    val productName: String = "",
    val productPrice: Double = 0.0,
    val productImage: String = "",
    var quantity: Int = 1
)