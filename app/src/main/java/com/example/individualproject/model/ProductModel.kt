package com.example.individualproject.model

import android.adservices.adid.AdId

data class ProductModel(
    var productId: String = "",
    var productName: String = "",
    var productPrice: Double = 0.0 ,
    var  productDesc : String = "",
    var  image : String = "",
)
