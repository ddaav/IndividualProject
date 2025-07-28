package com.example.individualproject.repository

import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.OpenableColumns
import android.util.Log
import com.cloudinary.Cloudinary
import com.cloudinary.utils.ObjectUtils
import com.example.individualproject.model.ProductModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.io.InputStream
import java.util.concurrent.Executors

class ProductRepositoryImpl : ProductRepository{
    val database=FirebaseDatabase.getInstance()
    val ref =database.reference.child("products")


    private val cloudinary = Cloudinary(
        mapOf(
            "cloud_name" to "ddqy3foaa",
            "api_key" to "885454126157828",
            "api_secret" to "t0pCRs5mwEKkdPlYzI-yWyGtg_U"
        )
    )

    override fun uploadImage(context: Context, imageUri: Uri, callback: (String?) -> Unit) {
        val executor = Executors.newSingleThreadExecutor()
        executor.execute {
            try {
                val inputStream: InputStream? = context.contentResolver.openInputStream(imageUri)
                var fileName = getFileNameFromUri(context, imageUri)


                fileName = fileName?.substringBeforeLast(".") ?: "uploaded_image"

                val response = cloudinary.uploader().upload(
                    inputStream, ObjectUtils.asMap(
                        "public_id", fileName,
                        "resource_type", "image"
                    )
                )

                var imageUrl = response["url"] as String?

                imageUrl = imageUrl?.replace("http://", "https://")


                Handler(Looper.getMainLooper()).post {
                    callback(imageUrl)
                }

            } catch (e: Exception) {
                e.printStackTrace()
                Handler(Looper.getMainLooper()).post {
                    callback(null)
                }
            }
        }
    }

    override fun getFileNameFromUri(context: Context, uri: Uri): String? {
        var fileName: String? = null
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1) {
                    fileName = it.getString(nameIndex)
                }
            }
        }
        return fileName
    }
    override fun addProduct(model: ProductModel, callback: (Boolean, String) -> Unit) {
        val id=ref.push().key.toString()
        model.productId=id
        ref.child(model.productId).setValue(model).addOnCompleteListener{
            if (it.isSuccessful){
                callback(true,"product added successfully")
            }else{callback(false,"${it.exception?.message}")}

        }
    }

    override fun updateProduct(
        product: ProductModel,
        callback: (Boolean, String) -> Unit
    ) {
        ref.child(product.productId).setValue(product).addOnCompleteListener{
            if (it.isSuccessful){
                callback(true,"Product updated successfully")
            }else{
                callback(false,"${it.exception?.message}")
            }
        }
    }

    override fun deleteProduct(productId: String, callback: (Boolean, String) -> Unit) {

        ref.child(productId).removeValue().addOnCompleteListener{
            if (it.isSuccessful){
                callback(true,"product deleted successfully")
            }else{
                callback(false,"${it.exception?.message}")
            }
        }
    }

    override fun getProductById(
        productId: String,
        callback: (Boolean, String, ProductModel?) -> Unit
    ) {ref.addValueEventListener(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            if (snapshot.exists()){
                val product = snapshot.getValue(ProductModel::class.java)
                if (product != null){
                    callback(true,"product fetched",product)
                }
            }

        }

        override fun onCancelled(error: DatabaseError) {
            callback(false,error.message,null)
        }
    })

    }


    override fun getAllProduct(callback: (Boolean, String, List<ProductModel?>) -> Unit) {
        ref.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val allProducts = mutableListOf<ProductModel>()
                if (snapshot.exists()){
                    for (eachProduct in snapshot.children){
                        try {
                            val product = eachProduct.getValue(ProductModel::class.java)
                            if (product != null){
                                allProducts.add(product)
                            }
                        } catch (e: Exception) {
                            Log.e("ProductRepository", "Failed to parse product: ${eachProduct.key}", e)
                        }
                    }
                    callback(true, "Products fetched successfully.", allProducts)
                } else {
                    callback(true, "No products found.", emptyList())
                }
            }

            override fun onCancelled(error: DatabaseError) {
                callback(false, error.message, emptyList())
            }
        })
    }

}