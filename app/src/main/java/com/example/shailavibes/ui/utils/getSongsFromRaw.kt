package com.example.shailavibes.ui.utils

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.example.shailavibes.ui.data.Song

fun getSongsFromRaw(context: Context): List<Song> {
    return listOf(
       // Song("شعبي الغربي", "محمد عزت", com.example.musicplayer.R.raw.song1),
        //Song("كل الشعبي", "فاطمة الزهراء", com.example.musicplayer.R.raw.song2)

    )
}

fun playSong(context: Context, exoPlayer: ExoPlayer, song: Song) {
    exoPlayer.stop()
    val mediaItem = MediaItem.fromUri("android.resource://${context.packageName}/${song.resourceId}")
    exoPlayer.setMediaItem(mediaItem)
    exoPlayer.prepare()
    exoPlayer.play()
}