package com.example.revision_app.room.revision.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.revision_app.retrofit.Constants
import com.example.revision_app.room.revision.data.model.RevisionEntity

@Dao
interface RevisionDao {

    @Query("SELECT * FROM ${Constants.DATABASE_REVISION_TABLE} WHERE user_id = :user_id")
    fun getRevisionByUser(user_id: String):  List<RevisionEntity>


    @Query("SELECT * FROM ${Constants.DATABASE_REVISION_TABLE} WHERE strftime('%Y-%m-%dT%H:%M:%fZ', date_evaluation) > strftime('%Y-%m-%dT%H:%M:%fZ', 'now') AND strftime('%Y-%m-%dT%H:%M:%fZ', date_evaluation) <= strftime('%Y-%m-%dT%H:%M:%fZ', 'now', '+6 days') AND user_id = :user_id")
    suspend fun getActualRevision(user_id: String): List<RevisionEntity>

    @Insert
    suspend fun insertRevision(goal: RevisionEntity) : Long

    @Update
    suspend fun updateRevision(goal: RevisionEntity)

    @Query("DELETE FROM ${Constants.DATABASE_REVISION_TABLE} WHERE id = :id")
    suspend fun deleteRevision(id: Long)

}
