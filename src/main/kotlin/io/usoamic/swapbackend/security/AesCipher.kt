package io.usoamic.swapbackend.security

import java.util.*

import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * AesCipher
 *
 * Encode/Decode text by password using AES-128-CBC algorithm
 * Based on https://gist.github.com/demisang/716250080d77a7f65e66f4e813e5a636
 */
class AesCipher constructor(
    private val method: String,
    private val key: String,
    private val iv: String
) {
    private val charset = charset("UTF-8")

    fun encrypt(plainText: String): String? {
        try {
            // Check secret length
            if (!isKeyLengthValid(key)) {
                throw Exception("Secret key's length must be 128, 192 or 256 bits")
            }

            val initVectorBytes = Base64.getDecoder().decode(iv)

            val ivParameterSpec = IvParameterSpec(initVectorBytes)
            val secretKeySpec = SecretKeySpec(key.toByteArray(charset), "AES")

            val cipher = Cipher.getInstance(method)
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec)

            // Encrypt input text
            val encrypted = cipher.doFinal(plainText.toByteArray(charset))

            // Result is base64-encoded string: initVector + encrypted result
            return Base64.getEncoder().encodeToString(encrypted)
        } catch (t: Throwable) { t.printStackTrace() }
        return null
    }

    fun decrypt(cipherText: String): String? {
        try {
            // Check secret length
            if (!isKeyLengthValid(key)) {
                throw Exception("Secret key's length must be 128, 192 or 256 bits")
            }

            // Get raw encoded data
            val encrypted = Base64.getDecoder().decode(cipherText)

            // Slice initialization vector
            val ivParameterSpec = IvParameterSpec(Base64.getDecoder().decode(iv))
            // Set secret password
            val secretKeySpec = SecretKeySpec(key.toByteArray(charset), "AES")

            val cipher = Cipher.getInstance(method)
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec)

            // Trying to get decrypted text
            return String(cipher.doFinal(encrypted))
        } catch (t: Throwable) { t.printStackTrace() }
        return null
    }

    /**
     * Check that secret password length is valid
     *
     * @param key 16/24/32 -characters secret password
     * @return TRUE if valid, FALSE otherwise
     */
    private fun isKeyLengthValid(key: String): Boolean {
        return key.length == 16 || key.length == 24 || key.length == 32
    }
}
