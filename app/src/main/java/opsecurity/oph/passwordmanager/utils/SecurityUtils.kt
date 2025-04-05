package opsecurity.oph.passwordmanager.utils

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object SecurityUtils {
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val MASTER_KEY_ALIAS = "PasswordManagerMasterKey"
    private const val GCM_IV_LENGTH = 12
    private const val GCM_TAG_LENGTH = 128
    
    // Simple static key for testing - in a real app this should be securely stored
    private const val TEMP_KEY = "0123456789ABCDEF0123456789ABCDEF" // 32 bytes for AES-256

    fun hashPassword(password: String, salt: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val input = (password + salt).toByteArray()
        val hashBytes = digest.digest(input)
        return Base64.encodeToString(hashBytes, Base64.DEFAULT)
    }

    fun generateSalt(): String {
        val random = SecureRandom()
        val salt = ByteArray(16)
        random.nextBytes(salt)
        return Base64.encodeToString(salt, Base64.DEFAULT)
    }

    fun encryptPassword(password: String, masterKey: SecretKey): Pair<String, String> {
        try {
            // Use a simple AES cipher with a static key for testing
            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            
            // Create a static key from the temp key
            val key = SecretKeySpec(TEMP_KEY.toByteArray(), "AES")
            
            // Generate a random IV
            val iv = ByteArray(16)
            SecureRandom().nextBytes(iv)
            
            // Initialize the cipher
            val ivSpec = IvParameterSpec(iv)
            cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec)
            
            // Encrypt
            val encryptedBytes = cipher.doFinal(password.toByteArray())
            
            return Pair(
                Base64.encodeToString(encryptedBytes, Base64.DEFAULT),
                Base64.encodeToString(iv, Base64.DEFAULT)
            )
        } catch (e: Exception) {
            android.util.Log.e("SecurityUtils", "Encryption error: ${e.message}", e)
            throw RuntimeException("Encryption failed: ${e.message}", e)
        }
    }

    fun decryptPassword(encryptedPassword: String, iv: String, masterKey: SecretKey): String {
        try {
            // Use a simple AES cipher with a static key for testing
            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            
            // Create a static key from the temp key
            val key = SecretKeySpec(TEMP_KEY.toByteArray(), "AES")
            
            // Decode the IV
            val decodedIv = Base64.decode(iv, Base64.DEFAULT)
            val ivSpec = IvParameterSpec(decodedIv)
            
            // Initialize the cipher
            cipher.init(Cipher.DECRYPT_MODE, key, ivSpec)
            
            // Decrypt
            val encryptedBytes = Base64.decode(encryptedPassword, Base64.DEFAULT)
            val decryptedBytes = cipher.doFinal(encryptedBytes)
            
            return String(decryptedBytes)
        } catch (e: Exception) {
            android.util.Log.e("SecurityUtils", "Decryption error: ${e.message}", e)
            return "Unable to retrieve password"
        }
    }

    fun getOrCreateMasterKey(context: android.content.Context): SecretKey {
        // Just return a dummy key since we're not actually using it
        return SecretKeySpec(TEMP_KEY.toByteArray(), "AES")
    }
} 