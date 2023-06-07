package com.example.baseproject.base.repositories

import com.example.baseproject.base.entity.User
import com.example.baseproject.base.retrofit.ApiService
import com.example.baseproject.base.retrofit.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class UserRepository {
    private val apiClient by lazy {
        RetrofitClient.getInstance().create(ApiService::class.java)
    }

    suspend fun getAllUser(): Flow<List<User>> {
        return flow {
            emit(apiClient.getUsers())
        }.flowOn(Dispatchers.IO)
    }
}