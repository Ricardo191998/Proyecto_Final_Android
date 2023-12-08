package com.example.revision_app.retrofit.revision

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface RevisionApi {

    @GET("revision/{id}")
    fun getAllRevisions(@Path("id") id: String?, @Header("revision-token") token: String,): Call<RevisionResponse>

    @GET("revision/actualWeek/{id}")
    fun getActualRevisions(@Path("id") id: String?, @Header("revision-token") token: String,): Call<RevisionResponse>

    @POST("revision/create")
    fun createRevision(@Header("revision-token") token: String, @Body revision: RevisionCreate): Call<RevisionResponse>

    @POST("revision/update/{id}")
    fun updateRevision(@Path("id") id: String?, @Header("revision-token") token: String, @Body revision: RevisionUpdate): Call<RevisionResponse>

    @DELETE("revision/{id}")
    fun deleteRevision(@Path("id") id: String?, @Header("revision-token") token: String): Call<RevisionResponse>

}