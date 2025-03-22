package com.example.shailavibes.ui


import com.example.shailavibes.R
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.exoplayer.ExoPlayer

import com.example.shailavibes.ui.data.Song

@Composable
fun PlaybackControls(song: Song, exoPlayer: ExoPlayer) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF2A3B5A)),
            //.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = song.title,
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = if (song.artist.isNotEmpty()) song.artist else "يرجى اختيار أغنية",
            color = Color(0xFFFFA500),
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(onClick = { /* Rewind */ }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_rewind),
                    contentDescription = "Rewind",
                    tint = Color.White
                )
            }
            IconButton(
                onClick = {
                    if (song.resourceId != 0) { // التأكد من أن هناك أغنية مختارة
                        if (exoPlayer.isPlaying) {
                            exoPlayer.pause()
                        } else {
                            exoPlayer.play()
                        }
                    }
                },
                enabled = song.resourceId != 0 // تعطيل الزر إذا لم يتم اختيار أغنية
            ) {
                Icon(
                    painter = painterResource(id = if (exoPlayer.isPlaying) R.drawable.ic_pause else R.drawable.ic_play),
                    contentDescription = "Play/Pause",
                    tint = Color.White
                )
            }
            IconButton(onClick = { /* Forward */ }) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_forward),
                    contentDescription = "Forward",
                    tint = Color.White
                )
            }
        }
    }
}