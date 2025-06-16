package com.example.baseproject.base.viewmodel

import com.example.baseproject.base.entity.User
import com.example.baseproject.base.repositories.UserRepository
import com.example.baseproject.base.retrofit.ApiService
import com.example.baseproject.base.retrofit.RetrofitClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

class UserViewModel : BaseViewModel() {
    private val apiClient by lazy {
        RetrofitClient.getInstance().create(ApiService::class.java)
    }

    /**
     * TODO: if you want to use repository, you must declare variable repository and demo use in function getUsers
     * if not, please remove repository and using flowOnIO
     */
    private val userRepository by lazy {
        UserRepository()
    }

    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: Flow<List<User>> = _users

    fun getUsers(
        onSuccess: ((data: List<User>) -> Unit)? = null,
        onError: ((errMsg: String) -> Unit)? = null,
    ) {
        /**
         * create a coroutine scope to execute some task, success and errors run in main thread
         * */
        launchHandler(onError = {
            onError?.invoke(it ?: "error")
        }, {
            isLoading.value = true
            flowOnIO(apiClient.getUsers()).execute(success = {
                onSuccess?.invoke(it)
                isLoading.value = false
            }, errors = {
                onError?.invoke(it)
                isLoading.value = false
            })
        })

        /**demo using repository*/
//        launchHandler {
//            userRepository.getAllUser().execute(success = {
//
//            }, errors = {
//
//            })
//        }
    }

    fun getUser() {
        setStateIsLoading()
        launchSafe {
            _users.update { apiClient.getUsers() }
            setStateIsSuccess()
        }
    }

    fun updateUser(
        id: String,
        onSuccess: (() -> Unit)? = null,
        onError: ((errMsg: String) -> Unit)? = null,
    ) {
        launchHandler {
            flowOnIO(apiClient.updateUser(id)).execute(success = {
                onSuccess?.invoke()
            }, errors = {
                onError?.invoke(it)
            })
        }
    }

}