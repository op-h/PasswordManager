package opsecurity.oph.passwordmanager.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey val username: String,
    val password: String, // This will store the hashed password
    val salt: String // For password hashing
) 