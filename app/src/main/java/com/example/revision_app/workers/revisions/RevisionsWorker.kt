package com.example.revision_app.workers.revisions

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.revision_app.application.RevisionApp
import com.example.revision_app.retrofit.revision.RevisionCreate
import com.example.revision_app.retrofit.revision.RevisionDto
import com.example.revision_app.retrofit.revision.RevisionRepository
import com.example.revision_app.retrofit.revision.RevisionResponse
import com.example.revision_app.retrofit.revision.RevisionUpdate
import com.example.revision_app.room.revision.RevisionRespositoryLocal
import com.example.revision_app.room.revision.data.model.RevisionEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import retrofit2.Call
import retrofit2.Response
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class RevisionsWorker(
    private val context: Context,
    private val workerParameters: WorkerParameters
): CoroutineWorker(context, workerParameters) {

    private lateinit var repository: RevisionRepository
    private lateinit var repositoryLocal: RevisionRespositoryLocal

    var token : String = ""
    var user_id: String = ""
    override suspend fun doWork(): Result {
        return suspendCoroutine { continuation ->
            try {
                val data = inputData
                repository = (RevisionApp.appContext as RevisionApp).repository
                repositoryLocal = (RevisionApp.appContext as RevisionApp).repositoryRevisionLocal
                var syncWithLocal = false

                data.getString("user_id")?.let { user_id = it }
                data.getString("token")?.let { token = it }
                data.getBoolean("syncWithLocal", false)?.let { syncWithLocal = it }

                CoroutineScope(Dispatchers.IO).launch {
                    var revisionsLocal = getLocalRevisions(user_id)
                    var revisions = getRevisions(user_id, token)

                    if (syncWithLocal) {
                        Log.d("R-W", "Sincronizamos con local")
                        syncWithLocal(revisionsLocal, revisions)
                    } else {
                        Log.d("R-W", "Sincronizamos con remoto")
                        syncWithRemote(revisionsLocal, revisions)
                    }

                    // Llama a continuation.resume() para indicar que la coroutine ha terminado
                    continuation.resume(
                        Result.success(
                        workDataOf(
                            "result" to true
                        )
                    ))
                }
            } catch (e: Exception) {
                Log.d("RW-E", "Failed -> ${e.message}")

                // Llama a continuation.resume() incluso en caso de error
                continuation.resume(
                    Result.success(
                    workDataOf(
                        "result" to false
                    )
                ))
            }
        }
    }

    private suspend fun syncWithLocal(revisiosLocal: List<RevisionEntity>, revisions: List<RevisionDto>){
        // Para crear o actualizar
        if(!revisiosLocal.isEmpty()){
            for (revisionLocal in revisiosLocal) {
                var isInRemote = false

                for (revision in revisions) {
                    if(revision._id == revisionLocal._id){
                        isInRemote = true
                        updateRevision(revisionLocal)
                    }
                }

                if(!isInRemote){
                    createRevision(revisionLocal)
                }

            }
        }

        // Para borrar
        for (revision in revisions) {

            var isInLocal = false

            for (revisionLocal in revisiosLocal) {
                if(revision._id == revisionLocal._id){
                    isInLocal = true
                }
            }

            if(!isInLocal){
                deleteRevision(revision)
            }
        }
    }

    private suspend fun syncWithRemote(revisionsLocal: List<RevisionEntity>, revisions: List<RevisionDto>){
        // Para crear o actualizar

        if(!revisions.isEmpty()){
            for (revision in revisions) {

                var isInLocal = false

                for (revisionLocal in revisionsLocal) {
                    if(revision._id == revisionLocal._id){
                        isInLocal = true
                        updateLocalRevision(revisionLocal, revision)
                    }
                }

                if(!isInLocal){
                    createLocalRevision(revision, user_id)
                }

            }
        }

        // Para borrar
        for (revisionLocal in revisionsLocal) {

            var isInBackend = false

            for (revision in revisions) {
                if(revision._id == revisionLocal._id){
                    isInBackend = true
                }
            }

            if(!isInBackend){
                deleteLocalRevision(revisionLocal.id)
            }
        }
    }

    private suspend fun getRevisions(user_id: String, token: String): List<RevisionDto> {
        return suspendCancellableCoroutine { continuation ->

            val call : Call<RevisionResponse> = repository.getAllRevisions(user_id, token)

            call.enqueue(object: retrofit2.Callback<RevisionResponse>{
                override fun onResponse(
                    call: Call<RevisionResponse>,
                    response: Response<RevisionResponse>
                ) {

                    response.body()?.let{ revisions ->
                        if(revisions.ok!!){
                            continuation.resume(revisions.revisions!!)
                        } else {
                            continuation.resume(emptyList())
                        }
                    }

                }

                override fun onFailure(call: Call<RevisionResponse>, t: Throwable) {
                    Log.d("RESPONSE", "Error: ${t.message}")
                    continuation.resume(emptyList())
                }

            })
        }


    }

    private suspend fun createRevision(revision: RevisionEntity){
        Log.d("G-W", "Sincronizamos con la red ${revision._id}")
        CoroutineScope(Dispatchers.IO).launch {
            try {
                var newRevision = RevisionCreate(revision.date_evaluation, revision.kpi.toInt(), revision.kpi_desc, revision._id)
                val call : Call<RevisionResponse> = repository.createRevision(newRevision, token)


                call.enqueue(object: retrofit2.Callback<RevisionResponse>{
                    override fun onResponse(
                        call: Call<RevisionResponse>,
                        response: Response<RevisionResponse>
                    ) {
                        Log.d("RESPONSE", "Respuesta del servidor: ${response.body()}")

                        response.body()?.let{ revisions ->
                            Log.d("R-W RESPONSE", revisions.toString())
                            Log.d("R-W RESPONSE", revisions.ok!!.toString())
                        }

                    }

                    override fun onFailure(call: Call<RevisionResponse>, t: Throwable) {
                        Log.d("R-W RESPONSE", "Error: ${t.message}")
                    }

                })

            }catch (e: Exception) {
                Log.d("R-W", e.toString())
            }
        }
    }

    private suspend fun updateRevision( revision: RevisionEntity){
        Log.d("G-W", "Sincronizamos con la red ${revision._id}")
        CoroutineScope(Dispatchers.IO).launch {
            try {
                var newRevision = RevisionUpdate(
                    revision._id,
                    revision.date_evaluation,
                    revision.color,
                    revision.kpi.toInt(),
                    revision.real_progress.toInt(),
                    revision.closed.toBoolean(),
                    revision.feedback,
                    revision.kpi_desc,
                    revision.goal_id
                )
                val call : Call<RevisionResponse> = repository.updateRevision(revision._id!!, token, newRevision)


                call.enqueue(object: retrofit2.Callback<RevisionResponse>{
                    override fun onResponse(
                        call: Call<RevisionResponse>,
                        response: Response<RevisionResponse>
                    ) {
                        Log.d("R-W RESPONSE", "Respuesta del servidor: ${response.body()}")

                        response.body()?.let{ revisions ->
                            Log.d("R-W RESPONSE", revisions.toString())
                        }

                    }

                    override fun onFailure(call: Call<RevisionResponse>, t: Throwable) {
                        Log.d("R-W RESPONSE", "Error: ${t.message}")

                    }

                })
            }catch (e: Exception){
                Log.d("R-W", e.toString())
            }
        }

    }

    private suspend fun deleteRevision(revision: RevisionDto){
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val call : Call<RevisionResponse> = repository.deleteRevision(revision._id!!, token)

                call.enqueue(object: retrofit2.Callback<RevisionResponse>{
                    override fun onResponse(
                        call: Call<RevisionResponse>,
                        response: Response<RevisionResponse>
                    ) {
                        Log.d("RESPONSE", "Respuesta del servidor: ${response.body()}")

                        response.body()?.let{ revisions ->
                            Log.d("R-W RESPONSE", revisions.toString())
                        }

                    }

                    override fun onFailure(call: Call<RevisionResponse>, t: Throwable) {
                        Log.d("R-W RESPONSE", "Error: ${t.message}")
                    }

                })

            } catch (e: Exception){
                Log.d("R-W", e.toString())
            }
        }

    }

    private suspend fun getLocalRevisions(user_id: String): List<RevisionEntity> {
        return repositoryLocal.getRevisions(user_id)
    }

    private suspend fun createLocalRevision(revision: RevisionDto, user_id: String) {

        CoroutineScope(Dispatchers.IO).launch  {
            try {
                repositoryLocal.insertRevision(
                    RevisionEntity(
                        _id = revision._id!!,
                        date_evaluation = revision.date_evaluation!!,
                        color = revision.color!!.toString(),
                        kpi = revision.kpi!!.toString(),
                        real_progress = revision.real_progress!!.toString(),
                        closed = revision.closed!!.toString(),
                        feedback = revision.feedback!!,
                        kpi_desc = revision.kpi_desc!!,
                        goal_id = revision.goal_id!!._id!!,
                        user_id = user_id
                    )
                )
            } catch (e: Exception) {
                Log.d("R-W Error", e.toString())
            }
        }
    }

    private suspend fun updateLocalRevision(revisionLocal: RevisionEntity,revision: RevisionDto) {

        var updateRevision = revisionLocal

        updateRevision.date_evaluation = revision.date_evaluation!!
        updateRevision.color = revision.color!!
        updateRevision.kpi = revision.kpi!!.toString()
        updateRevision.closed = revision.closed!!.toString()
        updateRevision.feedback = revision.feedback!!.toString()
        updateRevision.kpi_desc = revision.kpi_desc!!
        updateRevision.goal_id = revision.goal_id!!._id!!

        repositoryLocal.updateRevision(
            updateRevision
        )
    }

    private suspend fun deleteLocalRevision(revision_id: Long){
        repositoryLocal.deleteRevision(revision_id)
    }

}