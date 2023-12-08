package com.example.revision_app.retrofit.revision
import com.google.gson.annotations.SerializedName

data class RevisionResponse(
    @SerializedName("ok")
    var ok: Boolean? = false,
    @SerializedName("success")
    var success: Boolean? = false,
    @SerializedName("revisions")
    var revisions: List<RevisionDto>? = null,
    @SerializedName("message")
    var message: String? = ""
)

data class RevisionDto(
    @SerializedName("_id")
    var _id: String? = null,
    @SerializedName("date_evaluation")
    var date_evaluation: String? = null,
    @SerializedName("color")
    var color: String? = null,
    @SerializedName("kpi")
    var kpi: Number? = null,
    @SerializedName("real_progress")
    var real_progress: Number? = null,
    @SerializedName("closed")
    var closed: Boolean? = null,
    @SerializedName("feedback")
    var feedback: String? = null,
    @SerializedName("kpi_desc")
    var kpi_desc: String? = null,
    @SerializedName("goal_id")
    var goal_id: Goal? = null
)

data class Goal (
    @SerializedName("_id")
    var _id: String? = null,
    @SerializedName("name")
    var name: String? = null,
    @SerializedName("description")
    var description: String? = null,
    @SerializedName("year")
    var year: String? = null,
    @SerializedName("aim")
    var aim: String? = null,
    @SerializedName("image_url")
    var image_url: String? = null
)

data class RevisionCreate(
    @SerializedName("date_evaluation")
    var date_evaluation: String? = null,
    @SerializedName("kpi")
    var kpi: Number? = null,
    @SerializedName("kpi_desc")
    var kpi_desc: String? = null,
    @SerializedName("goal_id")
    var goal_id: String? = null
)

data class RevisionUpdate(
    @SerializedName("_id")
    var _id: String? = null,
    @SerializedName("date_evaluation")
    var date_evaluation: String? = null,
    @SerializedName("color")
    var color: String? = null,
    @SerializedName("kpi")
    var kpi: Number? = null,
    @SerializedName("real_progress")
    var real_progress: Number? = null,
    @SerializedName("closed")
    var closed: Boolean? = null,
    @SerializedName("feedback")
    var feedback: String? = null,
    @SerializedName("kpi_desc")
    var kpi_desc: String? = null,
    @SerializedName("goal_id")
    var goal_id: String? = ""
)