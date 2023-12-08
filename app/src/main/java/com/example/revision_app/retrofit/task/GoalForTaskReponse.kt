package com.example.revision_app.retrofit.task

import com.example.revision_app.retrofit.revision.Goal
import com.google.gson.annotations.SerializedName

data class GoalForTaskReponse(
    @SerializedName("ok")
    var ok: Boolean? = false,
    @SerializedName("goals")
    var goals: List<Goal>? = null
)
