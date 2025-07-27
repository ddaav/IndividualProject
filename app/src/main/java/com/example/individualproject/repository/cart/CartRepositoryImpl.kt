package com.example.individualproject.repository.cart

import com.example.individualproject.model.CartModel
import com.google.firebase.database.FirebaseDatabase

class CartRepositoryImpl : CartRepository {
    private val database = FirebaseDatabase.getInstance()
    // Each user will have their own cart node, identified by their UID
    private val cartRef = database.reference.child("carts")

    override fun addToCart(userId: String, product: CartModel, callback: (Boolean, String) -> Unit) {
        // Generate a new unique ID for the cart item
        val cartId = cartRef.child(userId).push().key ?: ""
        val newCartItem = product.copy(cartId = cartId)

        cartRef.child(userId).child(cartId).setValue(newCartItem)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true, "Product added to cart.")
                } else {
                    callback(false, task.exception?.message ?: "Failed to add to cart.")
                }
            }
    }
}