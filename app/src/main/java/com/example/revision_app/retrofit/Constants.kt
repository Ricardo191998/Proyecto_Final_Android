package com.example.revision_app.retrofit

object Constants {
    const val BASE_URL = "https://revisionback-suburbia-3lykdwzvva-uc.a.run.app"

    const val API_PATH = "/api"
    const val AUTH = "/auth"
    const val LOGIN_PATH = "/login"
    const val USERS_PATH = "/user"
    const val GOALS_PATH = "/goal"
    const val USERS_CREATE_PATH = "/create"
    const val UPDATE_PATH = "/update"
    const val GOALS_CREATE_PATH = "/create"
    const val GOALS_UPDATE_PATH = "/update"

    const val EMAIL_PARAM = "user_name"
    const val PASSWORD_PARAM = "password"
    const val NAME_PARAM = "name"

    const val ID_PROPERTY = "id"
    const val TOKEN_PROPERTY = "token"

    const val DATABASE_NAME = "RevisionDB"

    const val DATABASE_GOAL_TABLE = "GoalsTable"
    const val DATABASE_USER_TABLE = "UsersTable"
    const val DATABASE_REVISION_TABLE = "RevisionTable"
}