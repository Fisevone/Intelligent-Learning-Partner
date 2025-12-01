package com.example.educationapp.data.dao

import androidx.room.*
import com.example.educationapp.data.User
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE username = :username AND password = :password")
    suspend fun login(username: String, password: String): User?

    @Query("SELECT * FROM users WHERE username = :username")
    suspend fun getUserByUsername(username: String): User?

    @Query("SELECT * FROM users WHERE email = :email")
    suspend fun getUserByEmail(email: String): User?

    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserById(userId: Long): User?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User): Long

    @Update
    suspend fun updateUser(user: User)

    @Delete
    suspend fun deleteUser(user: User)

    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<User>>
    
    @Query("SELECT * FROM users WHERE userType = :userType")
    suspend fun getUsersByType(userType: com.example.educationapp.data.UserType): List<User>
    
    @Query("SELECT COUNT(*) FROM users WHERE userType = :userType")
    suspend fun getUserCountByType(userType: com.example.educationapp.data.UserType): Int
    
    @Query("SELECT * FROM users WHERE username LIKE :pattern")
    suspend fun getUsersByUsernamePattern(pattern: String): List<User>
}
