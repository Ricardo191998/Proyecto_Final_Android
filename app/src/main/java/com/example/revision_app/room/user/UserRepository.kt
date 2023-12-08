package com.example.revision_app.room.user

import com.example.revision_app.room.user.data.UserDao
import com.example.revision_app.room.user.data.model.UserEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserRepository(private val userDao: UserDao) {
    suspend fun getUsers() : List<UserEntity> {
        return withContext(Dispatchers.IO) {
            userDao.getUsers()
        }
    }

    suspend fun insertUser(user: UserEntity){
        userDao.insertUser(user)
    }

    suspend fun updateUser(user: UserEntity){
        userDao.updateUser(user)
    }

    suspend fun deleteUser(id: Long){
        return withContext(Dispatchers.IO) {
            userDao.deleteUser(id)
        }
    }
}