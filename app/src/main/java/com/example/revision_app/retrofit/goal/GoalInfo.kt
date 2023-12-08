package com.example.revision_app.retrofit.goal

import java.io.Serializable

data class GoalInfo(
                val name: String,
                val description: String,
                val file: String,
                val aim: String) : Serializable