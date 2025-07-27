package com.example.individualproject.repository

import android.provider.ContactsContract
import com.example.individualproject.model.UserModel
import com.google.firebase.auth.FirebaseUser
import javax.security.auth.callback.PasswordCallback

interface UserRepository {
    // ... other functions remain the same ...
    fun login(email:String,password: String,callback: (Boolean, String) -> Unit)
    fun register(email:String,password: String,callback: (Boolean, String, String) -> Unit)
    fun addUserToDatabase(userId: String, model: UserModel, callback: (Boolean, String) -> Unit)
    fun updateProfile(userId: String,data: MutableMap<String, Any?>, callback: (Boolean, String) -> Unit)
    fun forgetPassword(email:String,callback: (Boolean, String) -> Unit)
    fun getCurrentUser() : FirebaseUser?

    suspend fun getUserByID(UserID: String): UserModel?

    fun logout(callback: (Boolean, String) -> Unit )
}