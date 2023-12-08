package com.example.revision_app.retrofit.revision

import retrofit2.Call
import retrofit2.Retrofit

class RevisionRepository(private val retrofit: Retrofit) {

    private val revisionApi: RevisionApi = retrofit.create(RevisionApi::class.java)

    fun getAllRevisions(user_id: String, token: String): Call<RevisionResponse> =
        revisionApi.getAllRevisions(user_id, token)

    fun getRevisions(id: String, token: String): Call<RevisionResponse> =
        revisionApi.getActualRevisions(id, token)

    fun createRevision(revision: RevisionCreate, token: String): Call<RevisionResponse> =
        revisionApi.createRevision(token, revision)

    fun updateRevision(id: String, token: String, revision: RevisionUpdate): Call<RevisionResponse> =
        revisionApi.updateRevision(id, token, revision)

    fun deleteRevision(id: String, token: String): Call<RevisionResponse> =
        revisionApi.deleteRevision(id, token)

}