package com.example.crypto

import android.content.Context
import android.util.Base64
import java.security.KeyFactory
import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.security.PublicKey
import java.security.spec.ECGenParameterSpec
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import javax.crypto.KeyAgreement

/**
 * Per-user P-256 identity keys. Private key never leaves the device.
 * Shared secret is derived with ECDH so both sides can encrypt without a hardcoded app password.
 */
object IdentityKeyManager {
    private const val PREFS = "olinam_identity"
    private const val PRIV = "ec_private"
    private const val PUB = "ec_public"

    fun ensureKeyPair(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val existing = prefs.getString(PUB, null)
        if (!existing.isNullOrBlank() && !prefs.getString(PRIV, null).isNullOrBlank()) {
            return existing
        }
        val generator = KeyPairGenerator.getInstance("EC")
        generator.initialize(ECGenParameterSpec("secp256r1"))
        val pair = generator.generateKeyPair()
        val pub = Base64.encodeToString(pair.public.encoded, Base64.NO_WRAP)
        val priv = Base64.encodeToString(pair.private.encoded, Base64.NO_WRAP)
        prefs.edit().putString(PUB, pub).putString(PRIV, priv).apply()
        return pub
    }

    fun publicKey(context: Context): String = ensureKeyPair(context)

    fun sharedSecret(context: Context, remotePublicKeyB64: String): ByteArray? {
        return try {
            val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            ensureKeyPair(context)
            val privBytes = Base64.decode(prefs.getString(PRIV, "") ?: "", Base64.DEFAULT)
            val remoteBytes = Base64.decode(remotePublicKeyB64, Base64.DEFAULT)
            val kf = KeyFactory.getInstance("EC")
            val privateKey: PrivateKey = kf.generatePrivate(PKCS8EncodedKeySpec(privBytes))
            val publicKey: PublicKey = kf.generatePublic(X509EncodedKeySpec(remoteBytes))
            val agreement = KeyAgreement.getInstance("ECDH")
            agreement.init(privateKey)
            agreement.doPhase(publicKey, true)
            agreement.generateSecret()
        } catch (_: Exception) {
            null
        }
    }
}
