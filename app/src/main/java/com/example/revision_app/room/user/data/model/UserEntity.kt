package com.example.revision_app.room.user.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.revision_app.retrofit.Constants


@Entity(tableName = Constants.DATABASE_USER_TABLE)
class UserEntity (
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,

    @ColumnInfo(name = "uid")
    var uid: String,

    @ColumnInfo(name = "name")
    var name: String,

    @ColumnInfo(name = "user_name")
    var user_name: String,

    @ColumnInfo(name = "email")
    var email: String,

    @ColumnInfo(name = "image_url")
    var image_url: String,

)