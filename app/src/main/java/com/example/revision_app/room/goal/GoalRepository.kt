package com.example.revision_app.room.goal

import com.example.revision_app.room.goal.data.GoalDao
import com.example.revision_app.room.goal.data.model.GoalEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GoalRepository(private val goalDao: GoalDao) {
    suspend fun getGoals(userId: String) : List<GoalEntity> {
        return withContext(Dispatchers.IO) {
            goalDao.getGoalByUser(userId)
        }
    }

    suspend fun insertGoal(goal: GoalEntity){
        goalDao.insertGoal(goal)
    }

    suspend fun updateGoal(goal: GoalEntity){
        goalDao.updateGoal(goal)
    }

    suspend fun deleteGoal(id: Long){
        return withContext(Dispatchers.IO) {
            goalDao.delete(id)
        }
    }
}