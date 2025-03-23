package com.example.shailavibes.ui

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.shailavibes.ui.data.Song
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun PlaybackControls(
    context: Context,
    exoPlayer: ExoPlayer,
    selectedSong: Song?,
    songs: List<Song>,
    currentSongIndex: Int,
    onSongChange: (Int) -> Unit,
    onShowMessage: (String) -> Unit
) {
    val scope = rememberCoroutineScope()

    var isRepeatModeOn by remember { mutableStateOf(false) }

    LaunchedEffect(isRepeatModeOn) {
        exoPlayer.repeatMode = if (isRepeatModeOn) {
            Player.REPEAT_MODE_ONE
        } else {
            Player.REPEAT_MODE_OFF
        }
    }

    fun playNextSong() {
        if (songs.isNotEmpty()) {
            val newIndex = if (currentSongIndex < songs.size - 1) {
                currentSongIndex + 1
            } else {
                0
            }
            onSongChange(newIndex)
            val success = playSong(context, exoPlayer, songs[newIndex])
            if (!success) {
                onShowMessage("فشل تشغيل ${songs[newIndex].title}")
            }
        }
    }


    fun playPreviousSong() {
        if (songs.isNotEmpty()) {
            val newIndex = if (currentSongIndex > 0) {
                currentSongIndex - 1
            } else {
                songs.size - 1 // الانتقال إلى الأخيرة إذا كانت الأولى
            }
            onSongChange(newIndex)
            val success = playSong(context, exoPlayer, songs[newIndex])
            if (!success) {
                onShowMessage("فشل تشغيل ${songs[newIndex].title}")
            }
        }
    }

    // وظيفة لخلط السور
    fun shuffleSongs() {
        if (songs.isNotEmpty()) {
            val shuffledSongs = songs.shuffled()
            onSongChange(0)
            val success = playSong(context, exoPlayer, shuffledSongs[0])
            if (!success) {
                onShowMessage("فشل تشغيل ${shuffledSongs[0].title}")
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // النص الديناميكي (اسم السورة واسم القارئ)
        Text(
            text = if (selectedSong != null) "${selectedSong.title} - ${selectedSong.artist}" else "قم باختيار سورة",
            color = Color(0xFFf1c16b),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 5.dp)
        )

        Spacer(Modifier.height(8.dp))

        // الزر الكبير "كلمات السور"
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFFfd9001))
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = "Like Icon",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = "كلمات السور",
                color = Color.Black,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.height(8.dp))

        // متغيرات لتتبع الوقت الحالي ومدة السورة
        var currentPosition by remember { mutableStateOf(0L) }
        var duration by remember { mutableStateOf(0L) }

        // تحديث الوقت الحالي باستخدام LaunchedEffect
        LaunchedEffect(exoPlayer) {
            while (true) {
                currentPosition = exoPlayer.currentPosition
                duration = exoPlayer.duration
                delay(1000L) // تحديث كل ثانية
            }
        }

        // تحويل الوقت إلى صيغة mm:ss
        fun formatTime(millis: Long): String {
            if (millis < 0) return "00:00" // للتعامل مع القيم السلبية
            val minutes = (millis / 1000) / 60
            val seconds = (millis / 1000) % 60
            return String.format("%02d:%02d", minutes, seconds)
        }

        // شريط التقدم مع التوقيت
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = formatTime(currentPosition),
                color = Color.White,
                fontSize = 12.sp
            )
            LinearProgressIndicator(
                progress = if (duration > 0) currentPosition.toFloat() / duration else 0f,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp)
                    .height(2.dp),
                color = Color(0xFFFFA500),
                backgroundColor = Color.Gray
            )
            Text(
                text = formatTime(duration),
                color = Color.White,
                fontSize = 12.sp
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // أزرار التحكم (باستخدام الأيقونات الموسعة)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = { shuffleSongs() }) {
                Icon(
                    imageVector = Icons.Filled.Shuffle,
                    contentDescription = "Shuffle",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
            IconButton(onClick = { playPreviousSong() }) {
                Icon(
                    imageVector = Icons.Filled.SkipPrevious,
                    contentDescription = "Previous",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
            IconButton(
                onClick = {
                    if (selectedSong != null) {
                        if (exoPlayer.isPlaying) {
                            exoPlayer.pause()
                        } else {
                            exoPlayer.play()
                        }
                    }
                },
                enabled = selectedSong != null,
                modifier = Modifier
                    .background(Color.White, shape = CircleShape)
                    .size(48.dp)
            ) {
                Icon(
                    imageVector = if (exoPlayer.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = "Play/Pause",
                    tint = Color.Black,
                    modifier = Modifier.size(24.dp)
                )
            }
            IconButton(onClick = { playNextSong() }) {
                Icon(
                    imageVector = Icons.Filled.SkipNext,
                    contentDescription = "Next",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
            IconButton(onClick = {
                // تفعيل/إلغاء تفعيل وضع التكرار
                isRepeatModeOn = !isRepeatModeOn
                onShowMessage(if (isRepeatModeOn) "تم تفعيل وضع التكرار" else "تم إلغاء وضع التكرار")
                scope.launch {
                    delay(2000L)
                    onShowMessage("")
                }
            }) {
                Icon(
                    imageVector = Icons.Filled.Repeat,
                    contentDescription = "Repeat",
                    tint = if (isRepeatModeOn) Color(0xFFFFA500) else Color.White, // تغيير اللون لو وضع التكرار مفعّل
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

// دالة تشغيل السورة
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

        return true
    } catch (e: Exception) {
        println("Error playing song: ${e.message}")
        return false
    }
}