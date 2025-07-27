package com.example.individualproject.repository.cart

import com.example.individualproject.model.CartModel

interface CartRepository {
    fun addToCart(userId: String, product: CartModel, callback: (Boolean, String) -> Unit)
}