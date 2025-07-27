package com.example.individualproject.viewmodel

import android.provider.ContactsContract
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.individualproject.model.UserModel
import com.example.individualproject.repository.UserRepository
import com.google.firebase.auth.FirebaseUser
import javax.security.auth.callback.Callback

class UserViewModel (val repo : UserRepository) : ViewModel(){

    fun login(email: String, password: String, callback: (Boolean, String) -> Unit) {
        repo.login(email, password, callback)
    }
    fun register(email: String, password: String, callback: (Boolean, String, String) -> Unit) {
        repo.register(email, password, callback)
    }
    fun addUserToDatabase(userId: String, model: UserModel, callback: (Boolean, String) -> Unit) {
        repo.addUserToDatabase(userId, model, callback)
    }
    fun updateProfile(userId: String, data: MutableMap<String, Any?>, callback: (Boolean, String) -> Unit) {
        repo.updateProfile(userId, data, callback)
    }
    fun forgetPassword(email: String, callback: (Boolean, String) -> Unit) {
        repo.forgetPassword(email, callback)
    }
    fun getCurrentUser(): FirebaseUser? {
        return repo.getCurrentUser()
    }
    fun logout(callback: (Boolean, String) -> Unit) {
        repo.logout(callback)
    }


    private val _users = MutableLiveData<UserModel?>()
    val users: LiveData<UserModel?> get() = _users


    suspend fun getUserById(userId: String): UserModel? {
        return repo.getUserByID(userId)
    }


    fun fetchUserToLiveData(userId: String) {

    }
}