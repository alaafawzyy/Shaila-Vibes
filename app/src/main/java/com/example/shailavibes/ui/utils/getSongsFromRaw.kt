package com.example.shailavibes.ui.utils

import android.content.Context
import com.example.shailavibes.R
import com.example.shailavibes.ui.data.Song

fun getSongsFromRaw(context: Context): List<Song> {
    val rawFiles = mutableListOf<Song>()


    val arabicNames = mapOf(
    "aa" to "عاديات",
      "aj" to "بلد"
    )

    try {
        val fields = R.raw::class.java.fields

        for (field in fields) {
            val fileName = field.name
            val resourceId = field.getInt(null)

            val cleanFileName = fileName.replace(Regex("^\\d{2}_"), "")


            val formattedTitle = arabicNames[cleanFileName] ?: cleanFileName

            val song = Song(
                title = formattedTitle,
                artist = "",
                resourceId = resourceId,
                isFavorite = false
            )
            rawFiles.add(song)
        }
    } catch (e: Exception) {
        println("Error loading raw files: ${e.message}")
    }
    return rawFiles
}