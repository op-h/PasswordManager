package opsecurity.oph.passwordmanager.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "passwords")
data class Password(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: String,  // Added to associate passwords with specific users
    val title: String,
    val username: String,
    val encryptedPassword: String,
    val iv: String // Initialization vector for encryption
) 