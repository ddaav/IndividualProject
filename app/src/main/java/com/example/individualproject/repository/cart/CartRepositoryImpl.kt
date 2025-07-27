package com.example.individualproject.repository.cart

import com.example.individualproject.model.CartModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class CartRepositoryImpl : CartRepository {
    private val database = FirebaseDatabase.getInstance()
    private val cartRef = database.reference.child("carts")

    override fun addToCart(userId: String, product: CartModel, callback: (Boolean, String) -> Unit) {
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

    override fun getCartItems(userId: String, callback: (List<CartModel>, String?) -> Unit) {
        cartRef.child(userId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val cartItems = mutableListOf<CartModel>()
                for (itemSnapshot in snapshot.children) {
                    val cartItem = itemSnapshot.getValue(CartModel::class.java)
                    cartItem?.let { cartItems.add(it) }
                }
                callback(cartItems, null)
            }

            override fun onCancelled(error: DatabaseError) {
                callback(emptyList(), error.message)
            }
        })
    }

    override fun removeFromCart(userId: String, cartId: String, callback: (Boolean, String) -> Unit) {
        cartRef.child(userId).child(cartId).removeValue()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true, "Item removed from cart.")
                } else {
                    callback(false, task.exception?.message ?: "Failed to remove item.")
                }
            }
    }

    override fun clearCart(userId: String, callback: (Boolean, String) -> Unit) {
        cartRef.child(userId).removeValue()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true, "Cart cleared.")
                } else {
                    callback(false, task.exception?.message ?: "Failed to clear cart.")
                }
            }
    }
}