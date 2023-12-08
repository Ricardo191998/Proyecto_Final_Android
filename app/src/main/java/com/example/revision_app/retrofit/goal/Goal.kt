package com.example.revision_app.retrofit.goal

import java.io.Serializable

data class Goal(val _id: String,
                val email_address: String,
                var name: String,
                var description: String,
                val year: String,
                val user_id: String,
                val image_url: String,
                var aim: String,
                var file: String) : Serializable