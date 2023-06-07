package com.example.baseproject.base.utils.gallery

data class AlbumModel(
    val coverUri: String?,
    val name: String,
    var albumPhotos: MutableList<ImageModel>? = mutableListOf()
)
