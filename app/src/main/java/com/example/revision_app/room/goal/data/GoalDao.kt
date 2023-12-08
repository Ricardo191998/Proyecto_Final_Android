package com.example.revision_app.room.goal.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.revision_app.retrofit.Constants
import com.example.revision_app.room.goal.data.model.GoalEntity

@Dao
interface GoalDao {

    @Query("SELECT * FROM ${Constants.DATABASE_GOAL_TABLE} WHERE user_id = :user_id")
    fun getGoalByUser(user_id: String):  List<GoalEntity>

    @Insert
    suspend fun insertGoal(goal: GoalEntity) : Long

    @Update
    suspend fun updateGoal(goal: GoalEntity)

    @Query("DELETE FROM ${Constants.DATABASE_GOAL_TABLE} WHERE id = :id")
    suspend fun delete(id: Long)

}
