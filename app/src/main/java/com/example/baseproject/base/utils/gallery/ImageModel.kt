package com.example.baseproject.base.utils.gallery

data class ImageModel(
    val title: String? = null,
    val albumName: String? = null,
    val photoUri: String? = null,
    var duration: Long? = null,
    var isSelected: Boolean = false
)
