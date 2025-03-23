package com.example.shailavibes.ui.utils

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.example.shailavibes.ui.data.Song

fun playSong(context: Context, exoPlayer: ExoPlayer, song: Song) {
    // إنشاء MediaItem من المصدر (resource ID) الموجود في المجلد raw
    val mediaItem = MediaItem.fromUri("android.resource://${context.packageName}/raw/${song.resourceId}")
    
    // إعداد ExoPlayer لتشغيل الملف
    exoPlayer.setMediaItem(mediaItem)
    exoPlayer.prepare()
    exoPlayer.play()
}