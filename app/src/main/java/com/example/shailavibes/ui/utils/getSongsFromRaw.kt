package com.example.shailavibes.ui.utils

import android.content.Context
import com.example.shailavibes.R
import com.example.shailavibes.ui.data.Song

fun getSongsFromRaw(context: Context): List<Song> {
    val rawFiles = mutableListOf<Song>()
    try {
        val resources = context.resources
        val fields = R.raw::class.java.fields

        for (field in fields) {

            val fileName = field.name
            val song = Song(
                title = fileName.replace("_", " ").capitalize(),
                artist = "مستريح الدغاميني",
                fileName = fileName
            )
            rawFiles.add(song)
        }
    } catch (e: Exception) {
        println("Error loading raw files: ${e.message}")
    }
    return rawFiles.sortedBy { it.title }
}