package com.example.individualproject.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.individualproject.model.CartModel
import com.example.individualproject.repository.cart.CartRepository
import com.google.firebase.auth.FirebaseAuth

class CartViewModel(private val repository: CartRepository) : ViewModel() {

    private val _cartItems = MutableLiveData<List<CartModel>>()
    val cartItems: LiveData<List<CartModel>> get() = _cartItems

    private val _totalPrice = MutableLiveData<Double>()
    val totalPrice: LiveData<Double> get() = _totalPrice

    private val currentUserId: String?
        get() = FirebaseAuth.getInstance().currentUser?.uid

    init {
        loadCartItems()
    }

    private fun loadCartItems() {
        currentUserId?.let { userId ->
            repository.getCartItems(userId) { items, _ ->
                _cartItems.postValue(items)
                calculateTotalPrice(items)
            }
        }
    }

    private fun calculateTotalPrice(items: List<CartModel>) {
        val total = items.sumOf { it.productPrice * it.quantity }
        _totalPrice.postValue(total)
    }

    fun addToCart(userId: String, product: CartModel, callback: (Boolean, String) -> Unit) {
        repository.addToCart(userId, product, callback)
    }

    fun removeFromCart(cartId: String, callback: (Boolean, String) -> Unit) {
        currentUserId?.let { userId ->
            repository.removeFromCart(userId, cartId, callback)
        }
    }

    fun clearCart(callback: (Boolean, String) -> Unit) {
        currentUserId?.let { userId ->
            repository.clearCart(userId, callback)
        }
    }
}