package com.example.revision_app.goals

import com.example.revision_app.retrofit.Constants;
import com.example.revision_app.retrofit.goal.GoalService;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

class GoalsInteractor {

    var goalService: GoalService

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build();

        goalService = retrofit.create(GoalService::class.java)
    }
}
