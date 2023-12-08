package com.example.revision_app.retrofit.login

import java.io.Serializable

data class User(val uid: String,
                val email_address: String,
                val name: String,
                val user_name: String,
                val image_url: String) : Serializable