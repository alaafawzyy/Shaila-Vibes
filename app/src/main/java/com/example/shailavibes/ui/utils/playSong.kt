package com.example.shailavibes.ui.utils

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.shailavibes.ui.data.Song

fun playSong(context: Context, exoPlayer: ExoPlayer, song: Song): Boolean {
    try {
        // إيقاف التشغيل الحالي لو فيه
        exoPlayer.stop()
        exoPlayer.clearMediaItems()

        // تحديد الملف الصوتي من مجلد raw بناءً على اسم الملف
        val resId = context.resources.getIdentifier(song.fileName, "raw", context.packageName)
        if (resId == 0) {
            println("Error: Audio file ${song.fileName} not found in raw resources")
            return false
        }

        println("Found audio file: ${song.fileName}, resId: $resId")

        // إنشاء MediaItem من الملف الصوتي
        val mediaItem = MediaItem.fromUri("android.resource://${context.packageName}/$resId")
        println("MediaItem created: $mediaItem")

        // إضافة MediaItem للـ ExoPlayer
        exoPlayer.setMediaItem(mediaItem)
        println("MediaItem set to ExoPlayer")

        // تهيئة وتشغيل الـ ExoPlayer
        exoPlayer.prepare()
        exoPlayer.playWhenReady = true
        println("ExoPlayer prepared and playing: ${song.title}")

        // إضافة listener للتحقق من حالة التشغيل
        exoPlayer.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                when (state) {
                    Player.STATE_READY -> println("ExoPlayer is ready and playing")
                    Player.STATE_ENDED -> println("Playback ended for ${song.title}")
                    Player.STATE_BUFFERING -> println("ExoPlayer is buffering")
                    Player.STATE_IDLE -> println("ExoPlayer is idle")
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                println("ExoPlayer error: ${error.message}")
            }
        })

        return true
    } catch (e: Exception) {
        println("Error playing song: ${e.message}")
        return false
    }
}
