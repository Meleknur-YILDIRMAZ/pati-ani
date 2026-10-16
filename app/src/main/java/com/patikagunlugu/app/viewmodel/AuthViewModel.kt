package com.patikagunlugu.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.patikagunlugu.app.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    fun login(email: String, password: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            try {
                // Firebase çağrıları internetsiz ortamda sonsuza kadar bekleyebilir, bu yüzden 10 saniyelik zaman aşımı ekliyoruz.
                kotlinx.coroutines.withTimeout(10000) {
                    auth.signInWithEmailAndPassword(email, password).await()
                }
                _authState.value = AuthState.Success
            } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
                _authState.value = AuthState.Error(messageResId = R.string.connection_timeout)
            } catch (e: Exception) {
                _authState.value = AuthState.Error(message = e.localizedMessage, messageResId = R.string.login_failed)
            }
        }
    }

    fun register(email: String, password: String, name: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            try {
                kotlinx.coroutines.withTimeout(10000) {
                    val result = auth.createUserWithEmailAndPassword(email, password).await()
                    val user = result.user
                    
                    if (user != null) {
                        val userMap = hashMapOf(
                            "displayName" to name,
                            "email" to email,
                            "createdAt" to System.currentTimeMillis()
                        )
                        // Firestore kaydı başarısız olsa veya zaman aşımına uğrasa bile kaydı engelleme
                        try {
                            kotlinx.coroutines.withTimeout(3000) {
                                firestore.collection("users").document(user.uid).collection("profile").document("info").set(userMap).await()
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        // E-posta doğrulama gönderimi başarısız olsa bile girişi engelleme
                        try {
                            kotlinx.coroutines.withTimeout(3000) {
                                user.sendEmailVerification().await()
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
                _authState.value = AuthState.Success
            } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
                _authState.value = AuthState.Error(messageResId = R.string.connection_timeout)
            } catch (e: Exception) {
                _authState.value = AuthState.Error(message = e.localizedMessage, messageResId = R.string.register_failed)
            }
        }
    }

    fun googleLogin(idToken: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            try {
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                val result = auth.signInWithCredential(credential).await()
                val user = result.user
                
                // Firestore'a kullanıcıyı kaydet (ilk kez giriş yapıyorsa)
                if (user != null && result.additionalUserInfo?.isNewUser == true) {
                    val userMap = hashMapOf(
                        "displayName" to (user.displayName ?: "User"),
                        "email" to user.email,
                        "createdAt" to System.currentTimeMillis()
                    )
                    // Firestore kaydı başarısız olsa veya zaman aşımına uğrasa bile girişi engelleme
                    try {
                        kotlinx.coroutines.withTimeout(3000) {
                            firestore.collection("users").document(user.uid).collection("profile").document("info").set(userMap).await()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                
                _authState.value = AuthState.Success
            } catch (e: Exception) {
                _authState.value = AuthState.Error(message = e.localizedMessage, messageResId = R.string.google_login_failed)
            }
        }
    }

    fun resetPassword(email: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            try {
                auth.sendPasswordResetEmail(email).await()
                _authState.value = AuthState.Success
            } catch (e: Exception) {
                _authState.value = AuthState.Error(message = e.localizedMessage, messageResId = R.string.reset_email_failed)
            }
        }
    }

    fun logout() {
        auth.signOut()
    }

    sealed class AuthState {
        object Idle : AuthState()
        object Loading : AuthState()
        object Success : AuthState()
        data class Error(val message: String? = null, val messageResId: Int? = null) : AuthState()
    }
}

class AuthViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
