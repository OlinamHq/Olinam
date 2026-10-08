package com.example.auth

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

/**
 * Real Firebase Phone Authentication with safe fallbacks and zero-crash exception handling.
 */
object PhoneAuthManager {
    private const val TAG = "PhoneAuthManager"
    private var verificationId: String? = null

    private fun Context.findActivity(): Activity? {
        var current: Context = this
        while (current is ContextWrapper) {
            if (current is Activity) return current
            current = current.baseContext
        }
        return null
    }

    private fun getFirebaseAuth(): FirebaseAuth? = try {
        FirebaseAuth.getInstance()
    } catch (t: Throwable) {
        Log.w(TAG, "FirebaseAuth not initialized: ${t.message}")
        null
    }

    fun isFirebaseAvailable(): Boolean = getFirebaseAuth() != null

    fun sendOtp(
        context: Context,
        phoneNumber: String,
        onCodeSent: () -> Unit,
        onVerified: () -> Unit,
        onFailed: (String) -> Unit
    ) {
        try {
            val activity = context.findActivity()
            if (activity == null) {
                onFailed("Activity not found for phone verification")
                return
            }

            val auth = getFirebaseAuth()
            if (auth == null) {
                onFailed("Firebase not initialized in this environment")
                return
            }

            val options = PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber(phoneNumber)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(activity)
                .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                        try {
                            auth.signInWithCredential(credential)
                                .addOnSuccessListener { onVerified() }
                                .addOnFailureListener { onFailed(it.message ?: "Auto verify failed") }
                        } catch (t: Throwable) {
                            onFailed(t.message ?: "Sign-in with credential failed")
                        }
                    }

                    override fun onVerificationFailed(e: FirebaseException) {
                        Log.w(TAG, "Firebase verification failed: ${e.message}")
                        onFailed(e.message ?: "Phone verification failed")
                    }

                    override fun onCodeSent(
                        id: String,
                        token: PhoneAuthProvider.ForceResendingToken
                    ) {
                        verificationId = id
                        onCodeSent()
                    }
                })
                .build()

            PhoneAuthProvider.verifyPhoneNumber(options)
        } catch (t: Throwable) {
            Log.w(TAG, "PhoneAuthManager.sendOtp caught exception: ${t.message}")
            onFailed(t.message ?: "Phone verification unavailable")
        }
    }

    fun verifyOtp(code: String, onSuccess: () -> Unit, onFailed: (String) -> Unit) {
        try {
            val id = verificationId
            if (id.isNullOrBlank()) {
                onFailed("No verification in progress")
                return
            }
            val auth = getFirebaseAuth()
            if (auth == null) {
                onFailed("Firebase not initialized")
                return
            }
            val credential = PhoneAuthProvider.getCredential(id, code.trim())
            auth.signInWithCredential(credential)
                .addOnSuccessListener { onSuccess() }
                .addOnFailureListener { onFailed(it.message ?: "Invalid code") }
        } catch (t: Throwable) {
            Log.w(TAG, "PhoneAuthManager.verifyOtp caught exception: ${t.message}")
            onFailed(t.message ?: "Verification failed")
        }
    }
}
