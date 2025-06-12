package com.example.individualproject.repository

import android.provider.ContactsContract
import com.example.individualproject.model.UserModel
import com.google.firebase.auth.FirebaseUser
import javax.security.auth.callback.PasswordCallback

interface UserRepository {
    //login
    // register
    //forget password
    // update profile
    //getCurrentUser
    //add user to database
    //{
    // "success" : true,
    // "message" : "login successful"
    //}
    //logout
    fun login(email:String,password: String,callback: (Boolean, String) -> Unit)


    //authentication function
    fun register(email:String,password: String,callback: (Boolean, String, String) -> Unit)

    //database function
    fun addUserToDatabase(userId: String, model: UserModel, callback: (Boolean, String) -> Unit)

    fun updateProfile(userId: String,data: MutableMap<String, Any?>, callback: (Boolean, String) -> Unit)

    fun forgetPassword(email:String,callback: (Boolean, String) -> Unit)


    fun getCurrentUser() : FirebaseUser?

    fun getUserByID(UserID: String, callback: (UserModel?, Boolean, String) -> Unit )


    fun logout(callback: (Boolean, String) -> Unit )

}