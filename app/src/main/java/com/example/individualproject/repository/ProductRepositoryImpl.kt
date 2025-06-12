package com.example.individualproject.repository

import com.example.individualproject.model.ProductModel

class ProductRepositoryImpl: ProductRepository {
    override fun addProduct(
        model: ProductModel,
        callback: (Boolean, String) -> Unit
    ) {
        TODO("Not yet implemented")
    }

    override fun updateProduct(
        productId: String,
        data: MutableMap<String, Any?>,
        callback: (Boolean, String) -> Unit
    ) {
        TODO("Not yet implemented")
    }

    override fun deleteProduct(
        productId: String,
        callback: (Boolean, String) -> Unit
    ) {
        TODO("Not yet implemented")
    }

    override fun getProductById(
        productId: String,
        callback: (Boolean, String, ProductModel?) -> Unit
    ) {
        TODO("Not yet implemented")
    }

    override fun getAllProduct(callback: (Boolean, String, List<ProductModel?>) -> Unit) {
        TODO("Not yet implemented")
    }

}