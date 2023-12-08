package com.example.revision_app.retrofit.task

import retrofit2.Call
import retrofit2.Retrofit

class TaskRepository(private val retrofit: Retrofit) {

    private val taskApi: TaskApi = retrofit.create(TaskApi::class.java)

    fun getAllTask(token: String): Call<TaskDtoResponse> =
        taskApi.getAllTasks(token)

    fun getGoalsForTask(id: String, token: String): Call<GoalForTaskReponse> =
        taskApi.getGoalsForTask(id, token)

    fun createTask(task: CreateTask, token: String): Call<TaskDtoResponse> =
        taskApi.createTask(token, task)

    fun updateTask(id: String, token: String, task: TaskDto): Call<TaskDtoResponse> =
        taskApi.updateTask(id, token, task)

}