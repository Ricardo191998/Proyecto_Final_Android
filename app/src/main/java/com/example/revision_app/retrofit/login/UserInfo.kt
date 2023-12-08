package com.example.revision_app.retrofit.login
import com.example.revision_app.retrofit.Constants
import com.google.gson.annotations.SerializedName

class UserInfo (
    @SerializedName(Constants.NAME_PARAM) val name: String,
    @SerializedName(Constants.EMAIL_PARAM) val email: String,
    @SerializedName(Constants.PASSWORD_PARAM) val pass: String
)