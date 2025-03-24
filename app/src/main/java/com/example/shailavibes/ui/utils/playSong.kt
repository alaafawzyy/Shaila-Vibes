package com.example.shailavibes.ui.utils

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.example.shailavibes.ui.data.Song

fun playSong(context: Context, exoPlayer: ExoPlayer, song: Song): Boolean {
    return try {
        val mediaItem = MediaItem.fromUri("android.resource://${context.packageName}/${song.resourceId}")
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
        exoPlayer.playWhenReady = true
        true
    } catch (e: Exception) {
        println("Error playing song ${song.title}: ${e.message}")
        false
    }
}