package com.example.baseproject.base.entity

import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("id")
    val id: String? = null,
    @SerializedName("name")
    val name: String? = null,
    @SerializedName("age")
    val age: Int? = null
)
