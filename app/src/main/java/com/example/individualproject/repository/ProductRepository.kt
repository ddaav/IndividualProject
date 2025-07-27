package com.example.individualproject.repository

import android.content.Context
import android.net.Uri
import com.example.individualproject.model.ProductModel

interface ProductRepository {

    fun addProduct(model: ProductModel,
                   callback: (Boolean, String)-> Unit)

    fun updateProduct(
        product: ProductModel,
        callback: (Boolean, String) -> Unit
    )

    fun deleteProduct(
        productId: String,
        callback: (Boolean, String) -> Unit
    )

    fun getProductById(
        productId: String,
        callback: (Boolean, String, ProductModel?) -> Unit
    )

    fun getAllProduct(callback: (Boolean, String,
                                 List<ProductModel?>) -> Unit)


    fun uploadImage(context: Context,imageUri: Uri, callback: (String?) -> Unit)

    fun getFileNameFromUri(context: Context,uri: Uri): String?
}