package com.example.revision_app.retrofit.goal

import com.example.revision_app.retrofit.Constants
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path

interface GoalService {
    @Headers("Content-Type: application/json")
    @GET(Constants.API_PATH + Constants.GOALS_PATH +"/{user_id}")
    suspend fun getGoals(@Path("user_id") goal_id: String, @Header("Revision-token") token: String): GoalResponse


    @Headers("Content-Type: application/json")
    @POST(Constants.API_PATH + Constants.GOALS_PATH + Constants.GOALS_CREATE_PATH)
    suspend fun createGoal(@Header("revision-token") token: String, @Body goal: GoalInfo): GoalCreateReponse


    @Headers("Content-Type: application/json")
    @POST(Constants.API_PATH + Constants.GOALS_PATH + Constants.GOALS_UPDATE_PATH + "/{goal_id}")
    suspend fun updateGoal(@Header("revision-token") token: String, @Path("goal_id") goal_id: String, @Body goal: Goal): GoalCreateReponse



    @Headers("Content-Type: application/json")
    @DELETE(Constants.API_PATH + Constants.GOALS_PATH + "/{goal_id}")
    suspend fun deleteGoal(@Header("revision-token") token: String, @Path("goal_id") goal_id: String): GoalCreateReponse
}