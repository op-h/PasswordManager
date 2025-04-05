package opsecurity.oph.passwordmanager.data

import androidx.room.*
import opsecurity.oph.passwordmanager.data.entities.Password
import opsecurity.oph.passwordmanager.data.entities.User

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE username = :username")
    suspend fun getUser(username: String): User?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)
}

@Dao
interface PasswordDao {
    @Query("SELECT * FROM passwords WHERE userId = :userId")
    suspend fun getAllPasswords(userId: String): List<Password>

    @Insert
    suspend fun insertPassword(password: Password)

    @Update
    suspend fun updatePassword(password: Password)

    @Delete
    suspend fun deletePassword(password: Password)
    
    @Query("DELETE FROM passwords WHERE userId = :userId")
    suspend fun deleteAllPasswords(userId: String)
}

@Database(
    entities = [User::class, Password::class],
    version = 2,
    exportSchema = false
)
abstract class PasswordDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun passwordDao(): PasswordDao

    companion object {
        @Volatile
        private var INSTANCE: PasswordDatabase? = null

        fun getDatabase(context: android.content.Context, username: String): PasswordDatabase {
            val databaseName = "password_database_${username.hashCode()}"
            
            return INSTANCE ?: synchronized(this) {
                val instance = androidx.room.Room.databaseBuilder(
                    context.applicationContext,
                    PasswordDatabase::class.java,
                    databaseName
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }

        fun closeDatabase() {
            INSTANCE?.close()
            INSTANCE = null
        }
    }
} 