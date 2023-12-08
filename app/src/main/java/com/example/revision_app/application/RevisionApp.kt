package com.example.revision_app.application

import android.app.Application
import android.content.Context
import com.example.revision_app.retrofit.remote.RetrofitHelper
import com.example.revision_app.retrofit.revision.RevisionRepository
import com.example.revision_app.retrofit.task.TaskRepository
import com.example.revision_app.room.db.RevisionDatabase
import com.example.revision_app.room.goal.GoalRepository
import com.example.revision_app.room.revision.RevisionRespositoryLocal
import com.example.revision_app.room.user.UserRepository

class RevisionApp: Application() {

    companion object {
        lateinit var appContext: Context
            private set
    }

    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext
    }

    private val database by lazy {
        RevisionDatabase.getDatabase(this@RevisionApp)
    }

    private val retrofit by lazy{
        RetrofitHelper().getRetrofit()
    }

    val repositoryGoalLocal by lazy {
        GoalRepository(database.goalDao())
    }

    val repositoryUserLocal by lazy {
        UserRepository(database.userDao())
    }

    val repositoryRevisionLocal by lazy {
        RevisionRespositoryLocal(database.revisionDao())
    }

    val repository by lazy{
        RevisionRepository(retrofit)
    }

    val repositoryTask by lazy{
        TaskRepository(retrofit)
    }

}