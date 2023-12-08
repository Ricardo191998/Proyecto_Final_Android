package com.example.revision_app.revisions

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.revision_app.goals.GoalsInteractor
import com.example.revision_app.retrofit.revision.Goal
import com.example.revision_app.retrofit.revision.RevisionDto
import com.example.revision_app.retrofit.revision.RevisionRepository
import com.example.revision_app.retrofit.revision.RevisionResponse
import com.example.revision_app.room.goal.GoalRepository
import com.example.revision_app.room.goal.data.model.GoalEntity
import com.example.revision_app.room.revision.RevisionRespositoryLocal
import com.example.revision_app.room.revision.data.model.RevisionEntity
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Response

class RevisionViewModel : ViewModel()  {

    private var interactor: GoalsInteractor
    private lateinit var repository: RevisionRepository
    private lateinit var repositoryLocal: RevisionRespositoryLocal
    private lateinit var repositoryGoalsLocal: GoalRepository

    init {
        interactor = GoalsInteractor()
    }

    val toastMessage = MutableLiveData<String>()

    fun showToastMessage(message: String) {
        toastMessage.value = message
    }

    private val _goals = MutableLiveData<List<Goal>>()
    val goals: LiveData<List<Goal>> = _goals

    private val _revision = MutableLiveData<List<RevisionDto>>()
    val revision: LiveData<List<RevisionDto>> = _revision

    private val _revisionLocal = MutableLiveData<List<RevisionEntity>>()
    val revisionLocal: LiveData<List<RevisionEntity>> = _revisionLocal

    private lateinit var revisionsCurrent : List<RevisionDto>

    fun getRevisionLocalById(_id: String) : RevisionEntity?{
        try {
            for (revision in revisionLocal.value!!) {
                if(revision._id == _id){
                    return revision
                }
            }
            return null
        }catch (e : Exception){
            return null
        }
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

    fun searchRevision(textSearch: String){

        if(textSearch == ""){
            _revision.value = revisionsCurrent
            return
        }

        val filtro = "[${textSearch}].*".toRegex()  // Expresión regular para nombres que comienzan con "J" o "C"

        val revisionFiltrada = revisionsCurrent.filter { revision ->
            filtro.matches(revision?.goal_id?.name!!)
        }

        _revision.value = revisionFiltrada
    }

    fun loadRevision(isActual: Boolean, userId: String, token: String) {

        viewModelScope.launch {
            val call : Call<RevisionResponse>
            if(isActual){
                 call = repository.getRevisions(userId, token)
            } else {
                 call = repository.getAllRevisions(userId, token)
            }

            call.enqueue(object: retrofit2.Callback<RevisionResponse>{
                override fun onResponse(
                    call: Call<RevisionResponse>,
                    response: Response<RevisionResponse>
                ) {

                    response.body()?.let{ revisions ->
                        if(revisions.ok!!){
                            _revision.value = revisions.revisions!!
                            revisionsCurrent = revisions.revisions!!
                        }
                    }

                }

                override fun onFailure(call: Call<RevisionResponse>, t: Throwable) {
                    Log.d("RESPONSE", "Error: ${t.message}")
                    showToastMessage("No hay conexión")
                }

            })
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
                            description = goal.description,
                            year = goal.year,
                            image_url = goal.image_url,
                            aim = goal.aim
                        )
                    )
                }
                _goals.value = tempGoals
            } else {
                // TODO: Manejo de metas vacio
            }
        }
    }

    fun getGoallById(_id: String) : Goal?{
        try {
            for (goal in goals.value!!) {
                if(goal._id == _id){
                    return goal
                }
            }
            return null
        }catch (e : Exception){
            return null
        }
    }

    fun loadRevisionsForUserLocal(isActual: Boolean, userId: String){
        viewModelScope.launch {
            var revisions: List<RevisionEntity>
            if(isActual){
                revisions = repositoryLocal.getActualRevisions(userId)
            } else {
                revisions = repositoryLocal.getRevisions(userId)
            }

            val tempRevisions: MutableList<RevisionDto> = mutableListOf()

            if(revisions != null) {
                for (revision in revisions) {
                    val goal = getGoallById(revision.goal_id)
                    tempRevisions.add(
                        RevisionDto(
                            _id = revision._id,
                            date_evaluation = revision.date_evaluation,
                            color = revision.color,
                            kpi = revision.kpi.toInt(),
                            real_progress = revision.real_progress.toInt(),
                            closed = revision.closed.toBoolean(),
                            feedback = revision.feedback,
                            kpi_desc = revision.kpi_desc,
                            goal_id = goal
                        )
                    )
                }
                _revisionLocal.value = revisions
                _revision.value = tempRevisions
            } else {
                // TODO: Manejo de metas vacio
            }
        }
    }
}