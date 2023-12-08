package com.example.revision_app.room.revision

import com.example.revision_app.room.revision.data.RevisionDao
import com.example.revision_app.room.revision.data.model.RevisionEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RevisionRespositoryLocal(private val revisionDao: RevisionDao) {
    suspend fun getRevisions(userId: String) : List<RevisionEntity> {
        return withContext(Dispatchers.IO) {
            revisionDao.getRevisionByUser(userId)
        }
    }

    suspend fun getActualRevisions(userId: String) : List<RevisionEntity> {
        return withContext(Dispatchers.IO) {
            revisionDao.getActualRevision(userId)
        }
    }

    suspend fun insertRevision(revision: RevisionEntity){
        revisionDao.insertRevision(revision)
    }

    suspend fun updateRevision(revision: RevisionEntity){
        revisionDao.updateRevision(revision)
    }

    suspend fun deleteRevision(id: Long){
        return withContext(Dispatchers.IO) {
            revisionDao.deleteRevision(id)
        }
    }
}