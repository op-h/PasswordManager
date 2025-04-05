package opsecurity.oph.passwordmanager.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import opsecurity.oph.passwordmanager.data.entities.Password
import opsecurity.oph.passwordmanager.data.entities.User
import opsecurity.oph.passwordmanager.utils.SecurityUtils

// Create a property extension for the DataStore
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class PasswordRepository(private val context: Context) {
    private var database: PasswordDatabase? = null
    private var currentUsername: String? = null
    private val masterKey = SecurityUtils.getOrCreateMasterKey(context)

    // Key for storing the logged-in user
    private val loggedInUserKey = stringPreferencesKey("logged_in_user")

    private suspend fun initializeDatabase() {
        if (database == null || currentUsername == null) {
            val username = getLoggedInUser().first()
            if (username != null) {
                database = PasswordDatabase.getDatabase(context, username)
                currentUsername = username
            }
        }
    }

    private fun getUserDatabase(username: String): PasswordDatabase {
        return PasswordDatabase.getDatabase(context, username)
    }

    suspend fun registerUser(username: String, password: String): Boolean {
        val userDb = getUserDatabase(username)
        val existingUser = userDb.userDao().getUser(username)
        if (existingUser != null) return false

        val salt = SecurityUtils.generateSalt()
        val hashedPassword = SecurityUtils.hashPassword(password, salt)
        val user = User(username, hashedPassword, salt)
        userDb.userDao().insertUser(user)
        return true
    }

    suspend fun loginUser(username: String, password: String): Boolean {
        val userDb = getUserDatabase(username)
        val user = userDb.userDao().getUser(username) ?: return false
        val hashedPassword = SecurityUtils.hashPassword(password, user.salt)
        return hashedPassword == user.password
    }

    suspend fun saveLoggedInUser(username: String) {
        context.dataStore.edit { preferences ->
            preferences[loggedInUserKey] = username
        }
        // Close existing database if any
        database?.let {
            PasswordDatabase.closeDatabase()
        }
        // Initialize new database for the logged-in user
        database = PasswordDatabase.getDatabase(context, username)
        currentUsername = username
    }

    fun getLoggedInUser(): Flow<String?> {
        return context.dataStore.data.map { preferences ->
            preferences[loggedInUserKey]
        }
    }

    suspend fun clearLoggedInUser() {
        context.dataStore.edit { preferences ->
            preferences.remove(loggedInUserKey)
        }
        // Close the database when logging out
        database?.let {
            PasswordDatabase.closeDatabase()
        }
        database = null
        currentUsername = null
    }

    suspend fun addPassword(title: String, username: String, password: String) {
        try {
            initializeDatabase()
            val userId = currentUsername ?: throw IllegalStateException("No user logged in")
            val (encryptedPassword, iv) = SecurityUtils.encryptPassword(password, masterKey)
            val passwordEntity = Password(
                userId = userId,
                title = title,
                username = username,
                encryptedPassword = encryptedPassword,
                iv = iv
            )
            database?.passwordDao()?.insertPassword(passwordEntity)
                ?: throw IllegalStateException("Database not initialized")
        } catch (e: Exception) {
            throw RuntimeException("Failed to add password: ${e.message}", e)
        }
    }

    suspend fun getAllPasswords(): List<Password> {
        return try {
            initializeDatabase()
            val userId = currentUsername ?: throw IllegalStateException("No user logged in")
            database?.passwordDao()?.getAllPasswords(userId)
                ?: throw IllegalStateException("Database not initialized")
        } catch (e: Exception) {
            throw RuntimeException("Failed to retrieve passwords: ${e.message}", e)
        }
    }

    fun decryptPassword(password: Password): String {
        return try {
            SecurityUtils.decryptPassword(password.encryptedPassword, password.iv, masterKey)
        } catch (e: Exception) {
            throw RuntimeException("Failed to decrypt password: ${e.message}", e)
        }
    }

    suspend fun updatePassword(passwordId: Long, title: String, username: String, password: String) {
        try {
            initializeDatabase()
            val userId = currentUsername ?: throw IllegalStateException("No user logged in")
            val (encryptedPassword, iv) = SecurityUtils.encryptPassword(password, masterKey)
            val passwordEntity = Password(
                id = passwordId,
                userId = userId,
                title = title,
                username = username,
                encryptedPassword = encryptedPassword,
                iv = iv
            )
            database?.passwordDao()?.updatePassword(passwordEntity)
                ?: throw IllegalStateException("Database not initialized")
        } catch (e: Exception) {
            throw RuntimeException("Failed to update password: ${e.message}", e)
        }
    }

    suspend fun deletePassword(password: Password) {
        try {
            initializeDatabase()
            database?.passwordDao()?.deletePassword(password)
                ?: throw IllegalStateException("Database not initialized")
        } catch (e: Exception) {
            throw RuntimeException("Failed to delete password: ${e.message}", e)
        }
    }

    suspend fun clearAllPasswords() {
        try {
            initializeDatabase()
            val userId = currentUsername ?: throw IllegalStateException("No user logged in")
            database?.passwordDao()?.deleteAllPasswords(userId)
                ?: throw IllegalStateException("Database not initialized")
            android.util.Log.d("PasswordRepository", "Successfully cleared all passwords")
        } catch (e: Exception) {
            android.util.Log.e("PasswordRepository", "Error clearing passwords: ${e.message}", e)
            throw RuntimeException("Failed to clear passwords: ${e.message}", e)
        }
    }
} 