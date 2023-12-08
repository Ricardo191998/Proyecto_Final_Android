package com.example.revision_app.retrofit.login

import com.example.revision_app.retrofit.Constants
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface UserService {
    @Headers("Content-Type: application/json")
    @POST(Constants.API_PATH + Constants.USERS_PATH + Constants.USERS_CREATE_PATH)
    suspend fun createUser(@Body data: UserInfo): UserResponse
}