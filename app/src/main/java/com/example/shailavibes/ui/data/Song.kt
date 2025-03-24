package com.example.shailavibes.ui.data


data class Song(
    val title: String,
    val artist: String,
    val resourceId: Int,
    var isFavorite: Boolean = false
)