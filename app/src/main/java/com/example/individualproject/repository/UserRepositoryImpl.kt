package com.example.individualproject.repository

import com.example.individualproject.model.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

class UserRepositoryImpl : UserRepository {

    val auth : FirebaseAuth = FirebaseAuth.getInstance()
    val database : FirebaseDatabase= FirebaseDatabase.getInstance()
    val ref : DatabaseReference = database.reference.child("users")

    // ... login, register, etc. remain the same ...
    override fun login(email: String, password: String, callback: (Boolean, String) -> Unit) {
        auth.signInWithEmailAndPassword(email,password)
            .addOnCompleteListener {
                if (it.isSuccessful){
                    callback(true,"Login successfully")
                }else{
                    callback(false,"${it.exception?.message}")
                }
            }
    }
    override fun register(email: String, password: String, callback: (Boolean, String, String) -> Unit) {
        auth.createUserWithEmailAndPassword(email,password)
            .addOnCompleteListener {
                if (it.isSuccessful){
                    callback(true,"Register successfully",
                        "${auth.currentUser?.uid}")
                }else{
                    callback(false,"${it.exception?.message}","")
                }
            }
    }
    override fun addUserToDatabase(userId: String, model: UserModel, callback: (Boolean, String) -> Unit) {
        ref.child(userId).setValue(model).addOnCompleteListener {
            if (it.isSuccessful){
                callback(true,"user added")
            }else{
                callback(false,"${it.exception?.message}")
            }
        }
    }
    override fun updateProfile(userId: String, data: MutableMap<String, Any?>, callback: (Boolean, String) -> Unit) {
        ref.child(userId).setValue(data).addOnCompleteListener {
            if (it.isSuccessful){
                callback(true,"user updated")
            }else{
                callback(false,"${it.exception?.message}")
            }
        }
    }
    override fun forgetPassword(email: String, callback: (Boolean, String) -> Unit) {
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener {
                if (it.isSuccessful){
                    callback(true,"Password reset email sent."
                    )
                }else{
                    callback(false,"${it.exception?.message}")
                }
            }
    }
    override fun getCurrentUser(): FirebaseUser? {
        return  auth.currentUser
    }
    override fun logout(callback: (Boolean, String) -> Unit) {
        try {
            auth.signOut()
            callback(true,"logout successful")
        }catch (e: Exception){
            callback(false,"${e.message}")
        }
    }

    override suspend fun getUserByID(UserID: String): UserModel? {
        // This is a coroutine that will suspend until the Firebase callback is received.
        return suspendCancellableCoroutine { continuation ->
            val listener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val user = snapshot.getValue(UserModel::class.java)
                    // When data is received, resume the coroutine with the user object.
                    if (continuation.isActive) {
                        continuation.resume(user)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // If there's an error, resume with null.
                    if (continuation.isActive) {
                        continuation.resume(null)
                    }
                }
            }
            ref.child(UserID).addListenerForSingleValueEvent(listener)

            // If the coroutine is cancelled, remove the listener.
            continuation.invokeOnCancellation {
                ref.child(UserID).removeEventListener(listener)
            }
        }
    }
}