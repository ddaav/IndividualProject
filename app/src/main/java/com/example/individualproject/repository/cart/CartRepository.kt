package com.example.individualproject.repository.cart

import com.example.individualproject.model.CartModel

interface CartRepository {
    fun addToCart(userId: String, product: CartModel, callback: (Boolean, String) -> Unit)
    fun getCartItems(userId: String, callback: (List<CartModel>, String?) -> Unit)
    fun removeFromCart(userId: String, cartId: String, callback: (Boolean, String) -> Unit)
    fun clearCart(userId: String, callback: (Boolean, String) -> Unit)
}