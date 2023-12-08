package com.example.revision_app.goals

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.revision_app.retrofit.goal.Goal
import com.example.revision_app.room.goal.GoalRepository
import com.example.revision_app.room.goal.data.model.GoalEntity
import kotlinx.coroutines.launch

class GoalsViewModel : ViewModel() {

    private var interactor: GoalsInteractor

    private lateinit var repositoryLocal: GoalRepository

    init {
        interactor = GoalsInteractor()
    }

    private val _goals = MutableLiveData<List<Goal>>()
    val goals: LiveData<List<Goal>> = _goals

    private val _goalsLocal = MutableLiveData<List<GoalEntity>>()
    val goalsLocal: LiveData<List<GoalEntity>> = _goalsLocal

    fun setRepository(repository: GoalRepository) {
        this.repositoryLocal = repository
    }

    fun getGoalLocalById(_id: String) : GoalEntity?{
        try {
            for (goal in goalsLocal.value!!) {
                if(goal._id == _id){
                    return goal
                }
            }
            return null
        }catch (e : Exception){
            return null
        }
    }

    fun loadGoalsForUser(userId: String, token: String) {

        viewModelScope.launch {
            try {
                val goalsReponse = interactor.goalService.getGoals(userId, token)
                _goals.value = goalsReponse.goals
            } catch (e: Exception) {
                // TODO: Manejo de errores
            }
        }

    }

    fun loadGoalsForUserLocal(userId: String){
        viewModelScope.launch {
            val goals = repositoryLocal.getGoals(userId)

            val tempGoals: MutableList<Goal> = mutableListOf()

            if(goals != null) {
                for (goal in goals) {
                    tempGoals.add(
                        Goal(
                            _id = goal._id,
                            name = goal.name,
                            user_id = goal.user_id,
                            description = goal.description,
                            year = goal.year,
                            image_url = goal.image_url,
                            aim = goal.aim,
                            file = "",
                            email_address = ""
                        )
                    )
                }
                _goalsLocal.value = goals
                _goals.value = tempGoals
            } else {
                // TODO: Manejo de metas vacio
            }
        }
    }
}