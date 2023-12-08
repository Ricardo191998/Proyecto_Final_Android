package com.example.revision_app.edit_revisions

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.revision_app.goals.GoalsInteractor
import com.example.revision_app.retrofit.goal.Goal
import com.example.revision_app.retrofit.login.User
import com.example.revision_app.retrofit.revision.RevisionCreate
import com.example.revision_app.retrofit.revision.RevisionDto
import com.example.revision_app.retrofit.revision.RevisionRepository
import com.example.revision_app.retrofit.revision.RevisionResponse
import com.example.revision_app.retrofit.revision.RevisionUpdate
import com.example.revision_app.room.goal.GoalRepository
import com.example.revision_app.room.goal.data.model.GoalEntity
import com.example.revision_app.room.revision.RevisionRespositoryLocal
import com.example.revision_app.room.revision.data.model.RevisionEntity
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Response

class EditRevisionsViewModel: ViewModel() {
    private val revisionSelected = MutableLiveData<RevisionDto>()
    private val revisionLocalSelected = MutableLiveData<RevisionEntity>()
    private val showFab = MutableLiveData<Boolean>()
    private val user = MutableLiveData<User>()
    private val interactor: GoalsInteractor
    private val isOffline = MutableLiveData<Boolean>()
    private val result = MutableLiveData<Any>()

    private lateinit var repository: RevisionRepository
    private lateinit var repositoryLocal: RevisionRespositoryLocal
    private lateinit var repositoryGoalsLocal: GoalRepository

    private val _goals = MutableLiveData<List<Goal>>()
    val goals: LiveData<List<Goal>> = _goals

    private val _goalsLocal = MutableLiveData<List<GoalEntity>>()
    val goalsLocal: LiveData<List<GoalEntity>> = _goalsLocal


    init {
        interactor = GoalsInteractor()
    }

    fun setIsOffline(isVisible: Boolean){
        isOffline.value = isVisible
    }

    fun getIsOffline(): Boolean?{
        return isOffline.value
    }

    fun setRevisionSelected(revision: RevisionDto){
        revisionSelected.value = revision
    }

    fun setLocalRevisionSelected(revision: RevisionEntity){
        revisionLocalSelected.value = revision
    }

    fun setUser(userSelected: User){
        user.value = userSelected
    }

    fun getRevisionSelected(): MutableLiveData<RevisionDto>{
        return revisionSelected
    }

    fun getLocalRevisionSelected(): MutableLiveData<RevisionEntity>{
        return revisionLocalSelected
    }

    fun getUser(): MutableLiveData<User>{
        return user
    }

    fun setShowFab(isVisible: Boolean){
        showFab.value = isVisible
    }

    fun getShowFab(): LiveData<Boolean> {
        return showFab
    }

    fun setResult(value: Any){
        result.value = value
    }

    fun getResult(): LiveData<Any> {
        return result
    }

    fun setRepository(repository: RevisionRepository) {
        this.repository = repository
    }

    fun setRepositoryLocal(repository: RevisionRespositoryLocal) {
        this.repositoryLocal = repository
    }

    fun setRepositoryGoalLocal(repository: GoalRepository) {
        this.repositoryGoalsLocal = repository
    }

    fun loadGoalsForUser(userId: String, token: String) {

        viewModelScope.launch {
            try {
                val goalsReponse = interactor.goalService.getGoals(userId, token)
                _goals.value = goalsReponse.goals
            } catch (e: Exception) {
                // Manejo de errores
            }
        }
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

    fun loadGoalsForUserLocal(userId: String){
        viewModelScope.launch {
            val goals = repositoryGoalsLocal.getGoals(userId)

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

    fun createRevision(revision: RevisionCreate, token: String) {
        viewModelScope.launch {
            val call : Call<RevisionResponse> = repository.createRevision(revision, token)

            call.enqueue(object: retrofit2.Callback<RevisionResponse>{
                override fun onResponse(
                    call: Call<RevisionResponse>,
                    response: Response<RevisionResponse>
                ) {
                    Log.d("RESPONSE", "Respuesta del servidor: ${response.body()}")

                    response.body()?.let{ revisions ->
                        Log.d("RESPONSE", revisions.toString())
                        Log.d("RESPONSE", revisions.ok!!.toString())
                        if(revisions.ok!!){
                            result.value = "create-success"
                            return
                        }
                        result.value = "create-error"
                    }

                }

                override fun onFailure(call: Call<RevisionResponse>, t: Throwable) {
                    Log.d("RESPONSE", "Error: ${t.message}")
                    result.value = "create-error"
                }

            })
        }
    }


    fun updateRevision(revision: RevisionUpdate, token: String) {

        viewModelScope.launch {
            val call : Call<RevisionResponse> = repository.updateRevision(revision._id!!, token, revision)

            call.enqueue(object: retrofit2.Callback<RevisionResponse>{
                override fun onResponse(
                    call: Call<RevisionResponse>,
                    response: Response<RevisionResponse>
                ) {
                    Log.d("RESPONSE", "Respuesta del servidor: ${response.body()}")

                    response.body()?.let{ revisions ->
                        Log.d("RESPONSE", revisions.toString())
                        if(revisions.ok!!){
                            result.value = "update-success"
                            return
                        }
                        result.value = "update-error"
                    }

                }

                override fun onFailure(call: Call<RevisionResponse>, t: Throwable) {
                    Log.d("RESPONSE", "Error: ${t.message}")
                    result.value = "update-error"
                }

            })
        }
    }

    fun deleteRevision(token: String) {

        viewModelScope.launch {
            val call : Call<RevisionResponse> = repository.deleteRevision(revisionSelected.value?._id!!, token)

            call.enqueue(object: retrofit2.Callback<RevisionResponse>{
                override fun onResponse(
                    call: Call<RevisionResponse>,
                    response: Response<RevisionResponse>
                ) {
                    Log.d("RESPONSE", "Respuesta del servidor: ${response.body()}")

                    response.body()?.let{ revisions ->
                        Log.d("RESPONSE", revisions.toString())
                        if(revisions.success!!){
                            result.value = "delete-success"
                            return
                        }
                        result.value = "delete-error"
                    }

                }

                override fun onFailure(call: Call<RevisionResponse>, t: Throwable) {
                    Log.d("RESPONSE", "Error: ${t.message}")
                    result.value = "delete-error"
                }

            })
        }
    }

    fun createRevisionLocal(user_id: String, revision: RevisionCreate){
        viewModelScope.launch {
            try {
                val goal : GoalEntity = getGoalLocalById(revision.goal_id!!)!!

                repositoryLocal.insertRevision(
                    RevisionEntity(
                        _id = "null",
                        date_evaluation = revision.date_evaluation!!,
                        color = "",
                        kpi = revision.kpi!!.toString(),
                        real_progress = "0",
                        closed = "false",
                        feedback = "",
                        kpi_desc = revision.kpi_desc!!,
                        goal_id = goal._id,
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

    fun deleteRevisionLocal(revision: RevisionEntity){
        viewModelScope.launch {
            try {
                repositoryLocal.deleteRevision(revision.id)
                result.value = "delete-success"
            } catch (e: Exception) {
                result.value = "delete-error"
            }
        }
    }

    fun updateRevisionLocal(revision: RevisionEntity){
        viewModelScope.launch {
            try {
                repositoryLocal.updateRevision(revision)
                result.value = "update-success"
            } catch (e: Exception) {
                result.value = "update-error"
            }
        }
    }
}