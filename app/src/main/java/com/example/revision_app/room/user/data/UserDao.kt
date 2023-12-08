package com.example.revision_app.room.user.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.revision_app.retrofit.Constants
import com.example.revision_app.room.user.data.model.UserEntity

@Dao
interface UserDao {

    @Query("SELECT * FROM ${Constants.DATABASE_USER_TABLE}")
    fun getUsers():  List<UserEntity>

    @Insert
    suspend fun insertUser(user: UserEntity) : Long

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("DELETE FROM ${Constants.DATABASE_USER_TABLE} WHERE id = :id")
    suspend fun deleteUser(id: Long)

}


