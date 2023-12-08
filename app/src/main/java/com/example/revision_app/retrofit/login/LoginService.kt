package com.example.revision_app.retrofit.login
import com.example.revision_app.retrofit.Constants
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

interface LoginService {
    @Headers("Content-Type: application/json")
    @POST(Constants.API_PATH + Constants.AUTH + Constants.LOGIN_PATH)
    suspend fun loginUser(@Body data: LoginInfo): LoginResponse


    @Headers("Content-Type: application/json")
    @GET(Constants.API_PATH + Constants.AUTH)
    suspend fun auth(@Header("revision-token") token: String): LoginResponse
}