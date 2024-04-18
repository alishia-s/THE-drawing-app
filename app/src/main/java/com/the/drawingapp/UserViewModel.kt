package com.the.drawingapp

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class UserViewModel : ViewModel() {
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val _user = MutableStateFlow(firebaseAuth.currentUser)
    val user: StateFlow<FirebaseUser?> = _user.asStateFlow()

    private val _authMessage = MutableStateFlow<String?>(null)
    val authMessage: StateFlow<String?> = _authMessage.asStateFlow()

    private val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        _user.value = firebaseAuth.currentUser
        if(firebaseAuth.currentUser != null) {
            _authMessage.value = "Logged in as ${firebaseAuth.currentUser?.email}"
        }
    }

    init {
        firebaseAuth.addAuthStateListener(authStateListener)

    }

    fun login(email: String, password: String) {
        if (email.isNotBlank() && password.isNotBlank()) {
            firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        _user.value = firebaseAuth.currentUser
                        _authMessage.value = "Logged in as ${firebaseAuth.currentUser?.email}"
                    } else {
                        _user.value = null
                        _authMessage.value = task.exception?.message ?: "Failed to log in"
                    }
                }
        } else _authMessage.value = "Email or password cannot be blank."

    }

    fun signUp(email: String, password: String) {
        if(email.isNotBlank() && password.isNotBlank()) {
            firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        _user.value = firebaseAuth.currentUser
                        _authMessage.value = "Signed up as ${firebaseAuth.currentUser?.email}"
                        updateUserProfileInFirestore(firebaseAuth.currentUser!!.uid, firebaseAuth.currentUser!!.email!!)
                    } else {
                        _user.value = null
                        _authMessage.value = task.exception?.message ?: "Failed to sign up"
                    }
                }
        } else _authMessage.value = "Email or password cannot be blank."
    }

    private fun updateUserProfileInFirestore(userId: String, email: String) {
        val userDoc = firestore.collection("users").document(email)
        val userData = mapOf(
            "uid" to userId
        )

        userDoc.set(userData)
            .addOnSuccessListener {
                Log.d("UserVM", "Successfully updated user profile for $userId")
            }
            .addOnFailureListener { e ->
                Log.e("UserVM", "Error updating user profile for $userId", e)
            }
    }

    fun getUserID(): String? {
        return firebaseAuth.currentUser?.uid
    }

    fun getUserIDByEmail(email: String, onSuccess: (String?) -> Unit, onError: (Exception) -> Unit) {
        Log.d("UserVM", "Getting id for user $email")
        firestore.collection("users").document(email)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                val uid = documentSnapshot.getString("uid")
                if (uid != null) {
                    Log.d("UserVM", "Found $uid for user $email")
                    onSuccess(uid)
                } else {
                    onError(Exception("Email not found"))
                }
            }
            .addOnFailureListener { exception ->
                onError(exception)
            }
    }

    fun logout() {
        firebaseAuth.signOut()
    }

    override fun onCleared() {
        super.onCleared()
    }


}