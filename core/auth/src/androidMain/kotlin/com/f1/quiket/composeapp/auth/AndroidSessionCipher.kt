package com.f1.quiket.composeapp.auth

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

internal object AndroidSessionCipher {
    private const val KeyStoreType = "AndroidKeyStore"
    private const val KeyAlias = "quiket_cmp_session_aes_key"
    private const val Transformation = "AES/GCM/NoPadding"
    private const val TagLengthBits = 128

    fun encrypt(plaintext: String): String {
        val cipher = Cipher.getInstance(Transformation).apply {
            init(Cipher.ENCRYPT_MODE, getOrCreateSecretKey())
        }
        val ciphertext = cipher.doFinal(plaintext.encodeToByteArray())
        return encode(cipher.iv) + ":" + encode(ciphertext)
    }

    fun decrypt(encodedPayload: String): String {
        val separatorIndex = encodedPayload.indexOf(':')
        require(separatorIndex > 0 && separatorIndex < encodedPayload.lastIndex) {
            "Malformed encrypted auth payload."
        }
        val iv = decode(encodedPayload.substring(0, separatorIndex))
        val ciphertext = decode(encodedPayload.substring(separatorIndex + 1))
        val cipher = Cipher.getInstance(Transformation).apply {
            init(
                Cipher.DECRYPT_MODE,
                getOrCreateSecretKey(),
                GCMParameterSpec(TagLengthBits, iv),
            )
        }
        return cipher.doFinal(ciphertext).decodeToString()
    }

    fun resetKey() {
        val keyStore = KeyStore.getInstance(KeyStoreType).apply { load(null) }
        if (keyStore.containsAlias(KeyAlias)) {
            keyStore.deleteEntry(KeyAlias)
        }
    }

    private fun getOrCreateSecretKey(): SecretKey {
        val keyStore = KeyStore.getInstance(KeyStoreType).apply { load(null) }
        val existingKey = (keyStore.getEntry(KeyAlias, null) as? KeyStore.SecretKeyEntry)?.secretKey
        if (existingKey != null) {
            return existingKey
        }
        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KeyStoreType)
        val spec = KeyGenParameterSpec.Builder(
            KeyAlias,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
        ).setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .build()
        keyGenerator.init(spec)
        return keyGenerator.generateKey()
    }

    private fun encode(bytes: ByteArray): String =
        Base64.encodeToString(bytes, Base64.NO_WRAP)

    private fun decode(value: String): ByteArray =
        Base64.decode(value, Base64.NO_WRAP)
}
