package com.example.auth

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

/**
 * Production Firebase Phone Authentication using official PhoneAuthProvider.
 * Strictly verifies credentials against Firebase servers without dummy codes.
 */
object PhoneAuthManager {
    private const val TAG = "PhoneAuthManager"
    private var verificationId: String? = null
    private var resendToken: PhoneAuthProvider.ForceResendingToken? = null

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
        isResend: Boolean = false,
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
                onFailed("Firebase Authentication is not available on this device")
                return
            }

            val builder = PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber(phoneNumber)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(activity)
                .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                        Log.d(TAG, "Firebase instant auto-retrieval verification completed.")
                        try {
                            auth.signInWithCredential(credential)
                                .addOnSuccessListener {
                                    Log.d(TAG, "Auto sign-in successful: ${auth.currentUser?.uid}")
                                    onVerified()
                                }
                                .addOnFailureListener {
                                    Log.w(TAG, "Auto sign-in failed: ${it.message}")
                                    onFailed(it.message ?: "Automatic verification failed")
                                }
                        } catch (t: Throwable) {
                            Log.w(TAG, "Sign-in exception: ${t.message}")
                            onFailed(t.message ?: "Sign-in with credential failed")
                        }
                    }

                    override fun onVerificationFailed(e: FirebaseException) {
                        Log.w(TAG, "Firebase verification failed: ${e.message}", e)
                        val message = when {
                            e.message?.contains("quota", ignoreCase = true) == true ->
                                "SMS quota limit exceeded. Please try again later."
                            e.message?.contains("invalid", ignoreCase = true) == true && e.message?.contains("phone", ignoreCase = true) == true ->
                                "The phone number format is invalid. Please check the country code and number."
                            e.message?.contains("app verification", ignoreCase = true) == true || e.message?.contains("recaptcha", ignoreCase = true) == true ->
                                "App verification failed: ${e.message}"
                            else -> e.message ?: "Phone verification failed"
                        }
                        onFailed(message)
                    }

                    override fun onCodeSent(
                        id: String,
                        token: PhoneAuthProvider.ForceResendingToken
                    ) {
                        Log.d(TAG, "Firebase SMS verification code sent. verificationId=$id")
                        verificationId = id
                        resendToken = token
                        onCodeSent()
                    }
                })

            if (isResend && resendToken != null) {
                builder.setForceResendingToken(resendToken!!)
            }

            PhoneAuthProvider.verifyPhoneNumber(builder.build())
        } catch (t: Throwable) {
            Log.w(TAG, "PhoneAuthManager.sendOtp exception: ${t.message}", t)
            onFailed(t.message ?: "Phone verification unavailable")
        }
    }

    fun verifyOtp(code: String, onSuccess: () -> Unit, onFailed: (String) -> Unit) {
        try {
            val id = verificationId
            if (id.isNullOrBlank()) {
                onFailed("No verification in progress. Please request an SMS code first.")
                return
            }
            val auth = getFirebaseAuth()
            if (auth == null) {
                onFailed("Firebase Authentication is not available")
                return
            }
            val credential = PhoneAuthProvider.getCredential(id, code.trim())
            auth.signInWithCredential(credential)
                .addOnSuccessListener {
                    Log.d(TAG, "Firebase phone sign-in successful: ${auth.currentUser?.uid}")
                    onSuccess()
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Firebase sign-in failed: ${e.message}")
                    val msg = when {
                        e is FirebaseAuthInvalidCredentialsException ||
                        e.message?.contains("invalid", ignoreCase = true) == true ||
                        e.message?.contains("code", ignoreCase = true) == true ->
                            "The verification code is incorrect. Please check your SMS."
                        e.message?.contains("expired", ignoreCase = true) == true ->
                            "The verification code has expired. Please request a new SMS."
                        else -> e.message ?: "Verification failed. Please try again."
                    }
                    onFailed(msg)
                }
        } catch (t: Throwable) {
            Log.w(TAG, "PhoneAuthManager.verifyOtp exception: ${t.message}", t)
            onFailed(t.message ?: "Verification failed")
        }
    }
}
