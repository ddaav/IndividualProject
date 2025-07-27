package com.example.individualproject.viewmodel

import androidx.lifecycle.ViewModel
import com.example.individualproject.model.CartModel
import com.example.individualproject.repository.cart.CartRepository

class CartViewModel(private val repository: CartRepository) : ViewModel() {

    fun addToCart(userId: String, product: CartModel, callback: (Boolean, String) -> Unit) {
        repository.addToCart(userId, product, callback)
    }
}