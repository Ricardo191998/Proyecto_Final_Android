package com.example.revision_app.retrofit.task

import com.example.revision_app.retrofit.revision.RevisionDto
import com.google.gson.annotations.SerializedName

data class TaskDtoResponse(
    @SerializedName("ok")
    var ok: Boolean? = false,
    @SerializedName("message")
    var message: String? = "",
    @SerializedName("tasks")
    var tasks: List<TaskDto>? = null,
    @SerializedName("task")
    var task: TaskDto? = null
)

data class TaskDto(
    @SerializedName("_id")
    var _id: String? = "false",
    @SerializedName("revision_id")
    var revision_id: String? = "",
    @SerializedName("user_id")
    var user_id: String? = "null",
    @SerializedName("desc")
    var desc: String? = "",
    @SerializedName("value")
    var value: Int? = 0,
    @SerializedName("status")
    var status: String? = "",
    @SerializedName("created_date")
    var created_date: String? = ""
)