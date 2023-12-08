package com.example.revision_app.room.goal.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.revision_app.retrofit.Constants

@Entity(tableName = Constants.DATABASE_GOAL_TABLE)
class GoalEntity (
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,

    @ColumnInfo(name = "_id")
    var _id: String,

    @ColumnInfo(name = "name")
    var name: String,

    @ColumnInfo(name = "description")
    var description: String,

    @ColumnInfo(name = "year")
    var year: String,

    @ColumnInfo(name = "aim")
    var aim: String,

    @ColumnInfo(name = "image_url")
    var image_url: String,

    @ColumnInfo(name = "image_public_id")
    var image_public_id: String,

    @ColumnInfo(name = "user_id")
    var user_id: String
)