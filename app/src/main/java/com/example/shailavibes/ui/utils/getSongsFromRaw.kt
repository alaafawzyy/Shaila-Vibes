package com.example.shailavibes.ui.utils

import android.content.Context
import com.example.shailavibes.R
import com.example.shailavibes.ui.data.Song

fun getSongsFromRaw(context: Context): List<Song> {
    val songs = mutableListOf<Song>()

    // اسم القارئ (لأن الملفات ما فيهاش اسم القارئ)
    val artist = "مشاري راشد العفاسي" // عدلي الاسم حسب القارئ اللي نزلتِ منه

    // قراءة جميع الملفات من المجلد raw
    val fields = R.raw::class.java.fields
    fields.forEach { field ->
        val fileName = field.name // اسم الملف بدون الامتداد، مثل quran أو 001
        val resourceId = context.resources.getIdentifier(fileName, "raw", context.packageName)

        if (resourceId != 0) {
            // استخدام اسم الملف كاسم السورة مباشرة
            val surahName = fileName.replace("_", " ")

            // إضافة السورة إلى القائمة
            songs.add(Song(surahName, artist, resourceId))
        }
    }

    return songs.sortedBy { it.title } // ترتيب السور حسب الاسم
}