package com.the.drawingapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UserViewModel : ViewModel() {
    private val firebaseAuth = FirebaseAuth.getInstance()

    private val _user = MutableStateFlow(firebaseAuth.currentUser)
    val user: StateFlow<FirebaseUser?> = _user.asStateFlow()

    private val _authMessage = MutableStateFlow<String?>(null)
    val authMessage: StateFlow<String?> = _authMessage.asStateFlow()

    private val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        _user.value = firebaseAuth.currentUser
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
                    } else {
                        _user.value = null
                        _authMessage.value = task.exception?.message ?: "Failed to sign up"
                    }
                }
        } else _authMessage.value = "Email or password cannot be blank."
    }

    fun logout() {
        firebaseAuth.signOut()
    }

    override fun onCleared() {
        super.onCleared()
        firebaseAuth.signOut()
        firebaseAuth.removeAuthStateListener(authStateListener)
    }

}