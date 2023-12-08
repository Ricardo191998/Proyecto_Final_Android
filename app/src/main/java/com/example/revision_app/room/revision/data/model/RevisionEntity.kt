package com.example.revision_app.room.revision.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.revision_app.retrofit.Constants

@Entity(tableName = Constants.DATABASE_REVISION_TABLE)
class  RevisionEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,

    @ColumnInfo(name = "_id")
    var _id: String,

    @ColumnInfo(name = "date_evaluation")
    var date_evaluation: String,

    @ColumnInfo(name = "color")
    var color: String,

    @ColumnInfo(name = "kpi")
    var kpi: String,

    @ColumnInfo(name = "real_progress")
    var real_progress: String,

    @ColumnInfo(name = "closed")
    var closed: String,

    @ColumnInfo(name = "feedback")
    var feedback: String,

    @ColumnInfo(name = "kpi_desc")
    var kpi_desc: String,

    @ColumnInfo(name = "goal_id")
    var goal_id: String,

    @ColumnInfo(name = "user_id")
    var user_id: String
)