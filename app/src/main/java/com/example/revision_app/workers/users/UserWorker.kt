package com.example.revision_app.workers.users

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.revision_app.application.RevisionApp
import com.example.revision_app.room.user.UserRepository
import com.example.revision_app.room.user.data.model.UserEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class UserWorker(
    private val context: Context,
    private val workerParameters: WorkerParameters
): CoroutineWorker(context, workerParameters) {

    private lateinit var repository: UserRepository

    override suspend fun doWork(): Result {
        val data = inputData
        repository = (RevisionApp.appContext  as RevisionApp).repositoryUserLocal

        var uid = ""
        var name = ""
        var user_name = ""
        var image_url = ""
        var email_address = ""
        var isUpdated = false

        data.getString("uid")?.let { uid = it }
        data.getString("name")?.let { name = it }
        data.getString("user_name")?.let { user_name = it }
        data.getString("image_url")?.let { image_url = it }
        data.getString("email_address")?.let { email_address = it }

        CoroutineScope(Dispatchers.IO).launch {

            val tempUsers = async { syncUserWithLocal() }
            var localUsers = tempUsers.await()

            if(localUsers.isEmpty()) {
                createUser(uid, name, user_name, image_url, email_address)
            } else {
                for (user in localUsers) {
                    if(user.uid == uid) {
                        // Actualizar usuario
                        updateUser(user.id,uid, name, user_name, image_url, email_address)
                        isUpdated = true
                        break
                    }
                }

                if(!isUpdated){
                    createUser(uid, name, user_name, image_url, email_address)
                }
            }

        }

        return Result.success(
            workDataOf(
                "result" to true
            )
        )

    }

    private suspend fun syncUserWithLocal(): List<UserEntity> {
        return repository.getUsers()
    }

    private suspend fun createUser(uid : String, name: String, user_name: String, image_url: String, email: String ) {
        repository.insertUser(UserEntity(uid = uid, name = name, user_name = user_name, image_url = image_url, email = email))
    }

    private suspend fun updateUser(id: Long,uid : String, name: String, user_name: String, image_url: String, email: String ) {
        repository.updateUser(UserEntity(id = id, uid = uid, name = name, user_name = user_name, image_url = image_url, email = email))
    }

}