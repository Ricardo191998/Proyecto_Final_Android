package com.example.revision_app.workers.goals

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.revision_app.application.RevisionApp
import com.example.revision_app.goals.GoalsInteractor
import com.example.revision_app.retrofit.goal.Goal
import com.example.revision_app.retrofit.goal.GoalInfo
import com.example.revision_app.room.goal.GoalRepository
import com.example.revision_app.room.goal.data.model.GoalEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class GoalsWorker(
    private val context: Context,
    private val workerParameters: WorkerParameters
): CoroutineWorker(context, workerParameters) {

    private lateinit var repository: GoalRepository
    private lateinit var interactor: GoalsInteractor

    var token : String = ""

    override suspend fun doWork(): Result {
        return suspendCoroutine { continuation ->
            try {
                val data = inputData
                interactor = GoalsInteractor()
                repository = (RevisionApp.appContext as RevisionApp).repositoryGoalLocal
                var user_id = ""
                var syncWithLocal = false

                data.getString("user_id")?.let { user_id = it }
                data.getString("token")?.let { token = it }
                data.getBoolean("syncWithLocal", false)?.let { syncWithLocal = it }

                CoroutineScope(Dispatchers.IO).launch {
                    var goalsLocal = getLocalGoals(user_id)
                    var goals = getGoals(user_id, token)

                    if (syncWithLocal) {
                        Log.d("G-W", "Sincronizamos con local")
                        syncWithLocal(goalsLocal, goals)
                    } else {
                        Log.d("G-W", "Sincronizamos con remoto")
                        syncWithRemote(goalsLocal, goals)
                    }

                    // Llama a continuation.resume() para indicar que la coroutine ha terminado
                    continuation.resume(Result.success(
                        workDataOf(
                            "result" to true
                        )
                    ))
                }
            } catch (e: Exception) {
                Log.d("W-E", "Failed -> ${e.message}")

                // Llama a continuation.resume() incluso en caso de error
                continuation.resume(Result.success(
                    workDataOf(
                        "result" to false
                    )
                ))
            }
        }
    }

    private suspend fun syncWithLocal(goalsLocal: List<GoalEntity>, goals: List<Goal>){
        // Para crear o actualizar
        if(!goalsLocal.isEmpty()){
            for (goalLocal in goalsLocal) {
                var isInRemote = false

                for (goal in goals) {
                    if(goal._id == goalLocal._id){
                        isInRemote = true
                        updateGoal(goalLocal)
                    }
                }

                if(!isInRemote){
                    createGoal(goalLocal)
                }

            }
        }

        // Para borrar
        for (goal in goals) {

            var isInLocal = false

            for (goalLocal in goalsLocal) {
                if(goal._id == goalLocal._id){
                    isInLocal = true
                }
            }

            if(!isInLocal){
                deleteGoal(goal)
            }
        }
    }

    private suspend fun syncWithRemote(goalsLocal: List<GoalEntity>, goals: List<Goal>){
        // Para crear o actualizar

        if(!goals.isEmpty()){
            for (goal in goals) {

                var isInLocal = false

                for (goalLocal in goalsLocal) {
                    if(goal._id == goalLocal._id){
                        isInLocal = true
                        updateLocalGoal(goalLocal.id, goal._id, goal.name, goal.description, goal.year, goal.aim, goal.user_id, "", goal.image_url)
                    }
                }

                if(!isInLocal){
                    createLocalGoal(goal._id, goal.name, goal.description, goal.year, goal.aim, goal.user_id, "", goal.image_url)
                }

            }
        }

        // Para borrar
        for (goalLocal in goalsLocal) {

            var isInBackend = false

            for (goal in goals) {
                if(goal._id == goalLocal._id){
                    isInBackend = true
                }
            }

            if(!isInBackend){
                deleteLocalGoal(goalLocal.id)
            }
        }
    }

    private suspend fun getGoals(user_id: String, token: String): List<Goal> {
        val goalsReponse = interactor.goalService.getGoals(user_id, token)
        return goalsReponse.goals
    }

    private suspend fun createGoal(goal: GoalEntity){
         Log.d("G-W", "Sincronizamos con la red ${goal.name}")
         interactor.goalService.createGoal(
             token,
             GoalInfo(
                 name = goal.name,
                 description = goal.description,
                 file = "",
                 aim= goal.aim
             )
         )
    }

    private suspend fun updateGoal( goal: GoalEntity){
        Log.d("G-W", "Sincronizamos con la red ${goal.name}")

        try {
            var response = interactor.goalService.updateGoal(
                token,
                goal._id,
                Goal(
                    name = goal.name,
                    description = goal.description,
                    file = goal.image_url,
                    aim= goal.aim,
                    year = goal.year,
                    user_id = goal.user_id,
                    email_address = "",
                    _id = goal._id,
                    image_url = goal.image_url
                )
            )
            Log.d("G-W", response.toString())
        }catch (e: Exception){
            Log.d("G-W", e.toString())
        }


    }

    private suspend fun deleteGoal(goal: Goal){
        interactor.goalService.deleteGoal(token, goal._id)
    }

    private suspend fun getLocalGoals(user_id: String): List<GoalEntity> {
        return repository.getGoals(user_id)
    }

    private suspend fun createLocalGoal(_id : String, name: String, description: String, year: String, aim: String, user_id: String, image_public_id: String, image_url: String ) {

        repository.insertGoal(
            GoalEntity(
                _id = _id,
                name = name,
                description = description,
                image_url = image_url,
                year = year,
                aim = aim,
                user_id = user_id,
                image_public_id = image_public_id
            )
        )
    }

    private suspend fun updateLocalGoal(id: Long, _id : String, name: String, description: String, year: String, aim: String, user_id: String, image_public_id: String, image_url: String ) {

        repository.updateGoal(
            GoalEntity(
                id = id,
                _id = _id,
                name = name,
                description = description,
                image_url = image_url,
                year = year,
                aim = aim,
                user_id = user_id,
                image_public_id = image_public_id
            )
        )
    }

    private suspend fun deleteLocalGoal(goal_id: Long){
        repository.deleteGoal(goal_id)
    }

}