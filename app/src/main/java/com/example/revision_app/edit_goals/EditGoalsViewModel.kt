package com.example.revision_app.edit_goals

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.revision_app.goals.GoalsInteractor
import com.example.revision_app.retrofit.goal.Goal
import com.example.revision_app.retrofit.goal.GoalInfo
import com.example.revision_app.room.goal.GoalRepository
import com.example.revision_app.room.goal.data.model.GoalEntity
import kotlinx.coroutines.launch
import java.util.Calendar

class EditGoalsViewModel :ViewModel() {
    private val goalSelected = MutableLiveData<Goal>()
    private val goalLocalSelected = MutableLiveData<GoalEntity>()
    private val showFab = MutableLiveData<Boolean>()
    private val interactor: GoalsInteractor
    private val result = MutableLiveData<Any>()
    private val isOffline = MutableLiveData<Boolean>()
    private val user_id = MutableLiveData<String>()

    private lateinit var repositoryLocal: GoalRepository

    init {
        interactor = GoalsInteractor()
    }

    fun setRepository(repository: GoalRepository) {
        this.repositoryLocal = repository
    }

    fun setGoalSelected(goal: Goal){
        goalSelected.value = goal
    }

    fun getGoalSelected(): MutableLiveData<Goal>{
        return goalSelected
    }

    fun setLocalGoalSelected(goal: GoalEntity){
        goalLocalSelected.value = goal
    }

    fun getLocalGoalSelected(): MutableLiveData<GoalEntity>{
        return goalLocalSelected
    }

    fun setShowFab(isVisible: Boolean){
        showFab.value = isVisible
    }

    fun getShowFab(): LiveData<Boolean>{
        return showFab
    }

    fun setUserId(_id: String){
        user_id.value = _id
    }

    fun getUserId(): LiveData<String>{
        return user_id
    }

    fun setIsOffline(isVisible: Boolean){
        isOffline.value = isVisible
    }

    fun getIsOffline(): Boolean?{
        return isOffline.value
    }

    fun setResult(value: Any){
        result.value = value
    }

    fun getResult(): LiveData<Any>{
        return result
    }

    fun createGoal(token: String, goal: GoalInfo){
        viewModelScope.launch {
            try {
                val goalsReponse = interactor.goalService.createGoal(token, goal)
                result.value = goalsReponse.success
                if(goalsReponse.success){
                    result.value = "create-success"
                } else {
                    result.value = "create-error"
                }
            } catch (e: Exception) {
                result.value = "create-error"
            }
        }
    }

    fun createGoalLocal(user_id: String, goal: GoalInfo){
        viewModelScope.launch {
            try {
                val calendar = Calendar.getInstance()
                repositoryLocal.insertGoal(
                    GoalEntity(
                        _id = "null",
                        name = goal.name,
                        description = goal.description,
                        year = calendar.get(Calendar.YEAR).toString(),
                        aim = goal.aim,
                        image_public_id = "",
                        image_url = "https://res.cloudinary.com/ricardo191998/image/upload/v1621296439/no_image_kukdun.png",
                        user_id = user_id
                    )
                )
                result.value = "create-success"
            } catch (e: Exception) {
                Log.d("Erro", e.toString())
                result.value = "create-error"
            }
        }
    }

    fun deleteGoalLocal(goal: GoalEntity){
        viewModelScope.launch {
            try {
                repositoryLocal.deleteGoal(goal.id)
                result.value = "delete-success"
            } catch (e: Exception) {
                result.value = "delete-error"
            }
        }
    }

    fun updateGoalLocal(goal: GoalEntity){
        viewModelScope.launch {
            try {
                repositoryLocal.updateGoal(goal)
                result.value = "update-success"
            } catch (e: Exception) {
                result.value = "update-error"
            }
        }
    }

    fun deleteGoal(token: String, goal: Goal){
        viewModelScope.launch {
            try {
                val goalsReponse = interactor.goalService.deleteGoal(token, goal._id)

                if(goalsReponse.success){
                    result.value = "delete-success"
                } else {
                    result.value = "delete-error"
                }
            } catch (e: Exception) {
                result.value = "delete-error"
            }
        }
    }

    fun updateGoal(token: String, goal: Goal){
        viewModelScope.launch {
            try {
                val goalsReponse = interactor.goalService.updateGoal(token, goal._id, goal)
                if(goalsReponse.success){
                    result.value = "update-success"
                } else {
                    result.value = "update-error"
                }
            } catch (e: Exception) {
                result.value = "update-error"
            }
        }
    }


}