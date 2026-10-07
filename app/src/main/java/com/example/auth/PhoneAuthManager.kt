package com.example.auth

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

/**
 * Real Firebase Phone Authentication. Requires Phone provider enabled in Firebase console.
 */
object PhoneAuthManager {
    private var verificationId: String? = null

    private fun Context.findActivity(): Activity? {
        var current: Context = this
        while (current is ContextWrapper) {
            if (current is Activity) return current
            current = current.baseContext
        }
        return null
    }

    fun sendOtp(
        context: Context,
        phoneNumber: String,
        onCodeSent: () -> Unit,
        onVerified: () -> Unit,
        onFailed: (String) -> Unit
    ) {
        val activity = context.findActivity()
        if (activity == null) {
            onFailed("Activity not found for phone verification")
            return
        }
        val options = PhoneAuthOptions.newBuilder(FirebaseAuth.getInstance())
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    FirebaseAuth.getInstance().signInWithCredential(credential)
                        .addOnSuccessListener { onVerified() }
                        .addOnFailureListener { onFailed(it.message ?: "Auto verify failed") }
                }

                override fun onVerificationFailed(e: FirebaseException) {
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
    }

    fun verifyOtp(code: String, onSuccess: () -> Unit, onFailed: (String) -> Unit) {
        val id = verificationId
        if (id.isNullOrBlank()) {
            onFailed("No verification in progress")
            return
        }
        val credential = PhoneAuthProvider.getCredential(id, code.trim())
        FirebaseAuth.getInstance().signInWithCredential(credential)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onFailed(it.message ?: "Invalid code") }
    }
}
