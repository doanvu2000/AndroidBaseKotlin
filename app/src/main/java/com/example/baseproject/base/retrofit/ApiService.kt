package com.example.baseproject.base.retrofit

import com.example.baseproject.base.entity.User
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET("/users")
    suspend fun getUsers(): List<User>

    suspend fun updateUser(@Query("id") id: String)
}