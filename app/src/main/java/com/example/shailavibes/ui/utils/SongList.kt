package com.example.shailavibes.ui.utils

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions.Companion.Default
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.motionEventSpy
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shailavibes.ui.data.Song
import com.example.shailavibes.ui.theme.Green

@Composable
fun SongList(
    modifier: Modifier = Modifier,
    songs: List<Song>,
    onSongClick: (Song) -> Unit,
    onFavoriteToggle: (Song) -> Unit,
) {
    if (songs.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "لا توجد عناصر",
                color = Color.White,
                fontSize = 16.sp
            )
        }
    } else {
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(8.dp)

        ) {
            itemsIndexed(
                items = songs,
                key = { index, song -> "${song.title}-${song.isFavorite}" }
            ) { _, song ->
                SongItem(
                    song = song,
                    onSongClick = { onSongClick(song) },
                    onFavoriteToggle = { onFavoriteToggle(song) }
                )
            }
        }
    }
}

@Composable
fun SongItem(
    song: Song,
    onSongClick: () -> Unit,
    onFavoriteToggle: () -> Unit
) {
    var isFavorite by remember(song) { mutableStateOf(song.isFavorite) }
    var isPlay by remember{ mutableStateOf(false) }

    LaunchedEffect(song.isFavorite) {
        isFavorite = song.isFavorite
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF22292f))
            .clickable { onSongClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = {
            isPlay=!isPlay
            if (isPlay) {
                onSongClick()
            }
        }) {
            Icon(
                imageVector =if (isPlay) Icons.Default.Pause else Icons.Default.PlayCircle,
                contentDescription = "Play",
                tint = Color(0xFF3B3C3D),
                modifier = Modifier.size(24.dp)
            )
        }
        Column() {
            Text(
                text = song.title,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = song.artist,
                color = Color.Gray,
                fontSize = 14.sp
            )
        }
        IconButton(onClick = {
            onFavoriteToggle()
            isFavorite = song.isFavorite
        }) {
            Icon(
                imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                contentDescription = "Favorite",
                tint = if (isFavorite) Green else Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
    }
    Spacer(modifier = Modifier.height(8.dp))
}