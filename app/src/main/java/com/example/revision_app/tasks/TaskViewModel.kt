package com.example.revision_app.tasks

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.revision_app.retrofit.login.User
import com.example.revision_app.retrofit.revision.Goal
import com.example.revision_app.retrofit.task.GoalForTaskReponse
import com.example.revision_app.retrofit.task.TaskDto
import com.example.revision_app.retrofit.task.TaskDtoResponse
import com.example.revision_app.retrofit.task.TaskRepository
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Response
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class TaskViewModel : ViewModel()  {

    private lateinit var repository: TaskRepository
    val toastMessage = MutableLiveData<String>()
    private val _task = MutableLiveData<List<TaskDto>>()
    val tasks: LiveData<List<TaskDto>> = _task
    private val result = MutableLiveData<Any>()

    private val _goals = MutableLiveData<List<Goal>>()
    val goals: LiveData<List<Goal>> = _goals

    fun setRepository(repository: TaskRepository) {
        this.repository = repository
    }

    fun showToastMessage(message: String) {
        toastMessage.value = message
    }

    fun setResult(value: Any){
        result.value = value
    }

    fun getResult(): LiveData<Any> {
        return result
    }

    fun loadTask(token: String) {

        viewModelScope.launch {
            val call : Call<TaskDtoResponse> = repository.getAllTask(token)

            call.enqueue(object: retrofit2.Callback<TaskDtoResponse>{
                override fun onResponse(
                    call: Call<TaskDtoResponse>,
                    response: Response<TaskDtoResponse>
                ) {

                    response.body()?.let{ taskResponse ->
                        _task.value = taskResponse.tasks!!
                    }

                }

                override fun onFailure(call: Call<TaskDtoResponse>, t: Throwable) {
                    Log.d("RESPONSE", "Error: ${t.message}")
                    showToastMessage("No hay conexión")
                }

            })
        }
    }

    fun loadGoals(user: User, token: String) {

        viewModelScope.launch {
            val call : Call<GoalForTaskReponse> = repository.getGoalsForTask(user.uid,token)

            call.enqueue(object: retrofit2.Callback<GoalForTaskReponse>{
                override fun onResponse(
                    call: Call<GoalForTaskReponse>,
                    response: Response<GoalForTaskReponse>
                ) {

                    response.body()?.let{ goalResponse ->
                        _goals.value = goalResponse.goals!!
                    }

                }

                override fun onFailure(call: Call<GoalForTaskReponse>, t: Throwable) {
                    Log.d("RESPONSE", "Error: ${t.message}")
                    showToastMessage("No hay conexión")
                }

            })
        }
    }

    suspend fun updateTask(task: TaskDto, token: String): Boolean {
        return suspendCoroutine { continuation ->
            viewModelScope.launch {
                val call: Call<TaskDtoResponse> = repository.updateTask(task._id!!, token, task)

                call.enqueue(object : retrofit2.Callback<TaskDtoResponse> {
                    override fun onResponse(
                        call: Call<TaskDtoResponse>,
                        response: Response<TaskDtoResponse>
                    ) {
                        response.body()?.let { rest ->
                            if (rest.ok!!) {
                                continuation.resume(true)
                                return
                            }
                        }

                        continuation.resume(false)
                    }

                    override fun onFailure(call: Call<TaskDtoResponse>, t: Throwable) {
                        continuation.resume(false)
                    }
                })
            }
        }
    }
}