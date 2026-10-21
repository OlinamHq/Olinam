package com.example.chat.olinam

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine

/**
 * Directory synchronizer that indexes registered Olinam users
 * and determines in real time whether a contact number is an Olinam app user.
 */
class OlinamDirectorySync(
    private val firestore: FirebaseFirestore?
) {
    private val tag = "OlinamDirectorySync"
    private val scope = CoroutineScope(Dispatchers.IO)

    private val _registeredNumbers = MutableStateFlow<Set<String>>(emptySet())
    val registeredNumbers: StateFlow<Set<String>> = _registeredNumbers.asStateFlow()

    private val _userPublicKeys = MutableStateFlow<Map<String, String>>(emptyMap())
    val userPublicKeys: StateFlow<Map<String, String>> = _userPublicKeys.asStateFlow()

    private var directoryListener: ListenerRegistration? = null

    init {
        startObservingDirectory()
    }

    fun startObservingDirectory() {
        val db = firestore ?: return
        try {
            directoryListener?.remove()
            directoryListener = db.collection("users")
                .addSnapshotListener { snapshot, error ->
                    if (error != null || snapshot == null) {
                        Log.w(tag, "Directory listener error: ${error?.message}")
                        return@addSnapshotListener
                    }

                    val numberSet = mutableSetOf<String>()
                    val keyMap = mutableMapOf<String, String>()

                    for (doc in snapshot.documents) {
                        val phone = doc.getString("phoneNumber") ?: ""
                        val cleanPhone = doc.getString("cleanPhoneNumber") ?: ""
                        val pubKey = doc.getString("publicKey")

                        if (phone.isNotBlank()) {
                            val digits = PhoneNormalizer.extractDigits(phone)
                            val last10 = PhoneNormalizer.extractNational10Digits(phone)
                            val e164 = PhoneNormalizer.toE164(phone)

                            numberSet.add(phone.trim())
                            if (digits.isNotBlank()) numberSet.add(digits)
                            if (last10.isNotBlank()) numberSet.add(last10)
                            if (e164.isNotBlank()) numberSet.add(e164)

                            if (!pubKey.isNullOrBlank()) {
                                keyMap[last10] = pubKey
                                keyMap[digits] = pubKey
                            }
                        }

                        if (cleanPhone.isNotBlank()) {
                            numberSet.add(cleanPhone.trim())
                            val last10 = PhoneNormalizer.extractNational10Digits(cleanPhone)
                            if (last10.isNotBlank()) numberSet.add(last10)
                            if (!pubKey.isNullOrBlank()) {
                                keyMap[last10] = pubKey
                            }
                        }
                    }

                    _registeredNumbers.value = numberSet
                    _userPublicKeys.value = keyMap
                }
        } catch (e: Exception) {
            Log.w(tag, "Directory listen exception: ${e.message}")
        }
    }

    /**
     * Checks instantly against local cached set if phone is on Olinam.
     */
    fun isRegistered(phoneNumber: String?): Boolean {
        if (phoneNumber.isNullOrBlank()) return false
        val digits = PhoneNormalizer.extractDigits(phoneNumber)
        val last10 = PhoneNormalizer.extractNational10Digits(phoneNumber)
        val e164 = PhoneNormalizer.toE164(phoneNumber)
        val currentSet = _registeredNumbers.value

        return currentSet.contains(phoneNumber.trim()) ||
                currentSet.contains(digits) ||
                (last10.length >= 7 && currentSet.contains(last10)) ||
                (e164.isNotBlank() && currentSet.contains(e164))
    }

    /**
     * Returns the ECDH public key for a phone number if available.
     */
    fun getPublicKeyForPhone(phoneNumber: String?): String? {
        if (phoneNumber.isNullOrBlank()) return null
        val last10 = PhoneNormalizer.extractNational10Digits(phoneNumber)
        val digits = PhoneNormalizer.extractDigits(phoneNumber)
        return _userPublicKeys.value[last10] ?: _userPublicKeys.value[digits]
    }

    /**
     * Asynchronously queries Firestore to verify a phone number in case of race condition.
     */
    suspend fun verifyRemoteRegistration(phoneNumber: String): Boolean {
        if (isRegistered(phoneNumber)) return true
        val db = firestore ?: return false
        val digits = PhoneNormalizer.extractDigits(phoneNumber)
        val last10 = PhoneNormalizer.extractNational10Digits(phoneNumber)
        val e164 = PhoneNormalizer.toE164(phoneNumber)

        return suspendCancellableCoroutine { cont ->
            db.collection("users")
                .whereEqualTo("phoneNumber", phoneNumber.trim())
                .limit(1)
                .get()
                .addOnSuccessListener { snapshot ->
                    if (!snapshot.isEmpty) {
                        updateCachedNumber(phoneNumber, digits, last10, e164)
                        if (cont.isActive) cont.resume(true) {}
                    } else if (last10.length >= 7) {
                        db.collection("users")
                            .whereEqualTo("cleanPhoneNumber", last10)
                            .limit(1)
                            .get()
                            .addOnSuccessListener { subSnap ->
                                val found = !subSnap.isEmpty
                                if (found) {
                                    updateCachedNumber(phoneNumber, digits, last10, e164)
                                }
                                if (cont.isActive) cont.resume(found) {}
                            }
                            .addOnFailureListener {
                                if (cont.isActive) cont.resume(false) {}
                            }
                    } else {
                        if (cont.isActive) cont.resume(false) {}
                    }
                }
                .addOnFailureListener {
                    if (cont.isActive) cont.resume(false) {}
                }
        }
    }

    private fun updateCachedNumber(raw: String, digits: String, last10: String, e164: String) {
        val updated = _registeredNumbers.value.toMutableSet()
        if (raw.isNotBlank()) updated.add(raw.trim())
        if (digits.isNotBlank()) updated.add(digits)
        if (last10.isNotBlank()) updated.add(last10)
        if (e164.isNotBlank()) updated.add(e164)
        _registeredNumbers.value = updated
    }

    fun destroy() {
        directoryListener?.remove()
        directoryListener = null
    }
}
