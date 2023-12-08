package com.example.revision_app.retrofit.task

import com.google.gson.annotations.SerializedName

data class CreateTask(
    @SerializedName("desc")
    var desc: String = "",
    @SerializedName("goal_id")
    var goal_id: String = "",
    @SerializedName("value")
    var value: String = ""
)