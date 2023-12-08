package com.example.revision_app.retrofit.task

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface TaskApi {
    @GET("task/today")
    fun getAllTasks(@Header("revision-token") token: String,): Call<TaskDtoResponse>

    @GET("revision/goalsWithRevision/{id}")
    fun getGoalsForTask(@Path("id") id: String?, @Header("revision-token") token: String): Call<GoalForTaskReponse>

    @POST("task/createByGoal")
    fun createTask(@Header("revision-token") token: String, @Body task: CreateTask): Call<TaskDtoResponse>

    @POST("task/update/{id}")
    fun updateTask(@Path("id") id: String?, @Header("revision-token") token: String, @Body task: TaskDto): Call<TaskDtoResponse>
}