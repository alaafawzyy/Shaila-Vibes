package com.example.shailavibes.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.exoplayer.ExoPlayer
import com.example.shailavibes.ui.data.Song
import com.example.shailavibes.ui.utils.getSongsFromRaw
import com.example.shailavibes.ui.utils.playSong
import kotlinx.coroutines.launch
import com.example.shailavibes.R
import kotlinx.coroutines.delay
import kotlin.math.max

@Composable
fun MusicPlayerScreen() {
    val context = LocalContext.current
    val drawerState = androidx.compose.material3.rememberDrawerState(initialValue = DrawerValue.Closed)
    val songs = remember { getSongsFromRaw(context) }
    var selectedSong by remember { mutableStateOf<Song?>(null) }
    var currentSongIndex by remember { mutableStateOf(-1) } // لتتبع الأغنية الحالية
    val exoPlayer = remember { ExoPlayer.Builder(context).build() }
    val scope = rememberCoroutineScope()

    // إيقاف ExoPlayer عند الخروج من الشاشة
    DisposableEffect(exoPlayer) {
        onDispose {
            exoPlayer.stop()
            exoPlayer.release()
        }
    }

    // وظيفة لتشغيل الأغنية التالية
    fun playNextSong() {
        if (songs.isNotEmpty()) {
            currentSongIndex = if (currentSongIndex < songs.size - 1) {
                currentSongIndex + 1
            } else {
                0 // العودة إلى الأولى إذا كانت الأخيرة
            }
            selectedSong = songs[currentSongIndex]
            playSong(context, exoPlayer, selectedSong!!)
        }
    }

    // وظيفة لتشغيل الأغنية السابقة
    fun playPreviousSong() {
        if (songs.isNotEmpty()) {
            currentSongIndex = if (currentSongIndex > 0) {
                currentSongIndex - 1
            } else {
                songs.size - 1 // الانتقال إلى الأخيرة إذا كانت الأولى
            }
            selectedSong = songs[currentSongIndex]
            playSong(context, exoPlayer, selectedSong!!)
        }
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    DrawerContent(
                        onItemClick = {
                            scope.launch { drawerState.close() }
                        }
                    )
                }
            },
            content = {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    Scaffold(
                        backgroundColor = Color(0xFF192233),
                        topBar = {
                            TopAppBar(
                                navigationIcon = {
                                    IconButton(onClick = { /* TODO: Handle more options click */ }) {
                                        Icon(
                                            imageVector = Icons.Default.MoreVert,
                                            contentDescription = "More Options",
                                            tint = Color.White
                                        )
                                    }
                                },
                                title = {
                                    Row {
                                        Icon(
                                            imageVector = Icons.Default.Search,
                                            contentDescription = "Search",
                                            tint = Color.White,
                                            modifier = Modifier
                                                .size(30.dp)
                                                .clickable { /* TODO: Handle search click */ }
                                        )
                                        Spacer(
                                            modifier = Modifier
                                                .width(30.dp)
                                                .padding(end = 60.dp)
                                        )
                                        Text(
                                            text = "شيلات الطريق بدون نت",
                                            color = Color.White,
                                            fontWeight = FontWeight.Medium,
                                            modifier = Modifier.padding(start = 10.dp)
                                        )
                                        Spacer(modifier = Modifier.width(15.dp))
                                    }
                                },
                                actions = {
                                    IconButton(onClick = {
                                        scope.launch { drawerState.open() }
                                    }) {
                                        Icon(
                                            imageVector = Icons.Default.Menu,
                                            contentDescription = "Menu",
                                            tint = Color.White
                                        )
                                    }
                                },
                                backgroundColor = Color(0xFF353d48),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp, topStart = 16.dp, topEnd = 16.dp))
                            )
                        },
                        content = { paddingValues ->
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color(0xFF2c343f))
                                    .padding(paddingValues)
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    SongList(
                                        songs = songs,
                                        onSongClick = { song ->
                                            selectedSong = song
                                            currentSongIndex = songs.indexOf(song)
                                            playSong(context, exoPlayer, song)
                                        }
                                    )
                                }
                            }
                        },
                        bottomBar = {
                            BottomAppBar(
                                backgroundColor = Color(0xFF404a56),
                                modifier = Modifier
                                    .height(230.dp)
                                    .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp))
                            ) {
                                Column(
                                    modifier = Modifier
                                        .align(Alignment.Top)
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Top
                                ) {
                                    Text(
                                        text = if (selectedSong != null) selectedSong!!.title else "قم باختيار شيلة",
                                        color = Color(0xFFf1c16b),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(top = 5.dp)
                                    )
                                    Spacer(Modifier.height(12.dp))
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 10.dp)
                                            .height(48.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Button(
                                            modifier = Modifier
                                                .size(width = 320.dp, height = 48.dp)
                                                .padding(end = 12.dp, start = 40.dp)
                                                .clip(RoundedCornerShape(16.dp)),
                                            colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFFfd9001)),
                                            onClick = {}
                                        ) {
                                            Text(
                                                text = "كلمات الشيلات",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 22.sp,
                                                color = Color.Black
                                            )
                                        }
                                        Image(
                                            painter = painterResource(id = R.drawable.like),
                                            contentDescription = "Like Icon",
                                            modifier = Modifier
                                                .size(100.dp)
                                                .padding(bottom = 7.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))

                                    // متغيرات لتتبع الوقت الحالي ومدة الأغنية
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
                                                .height(8.dp),
                                            color = Color(0xFFFFA500),
                                            backgroundColor = Color.Gray
                                        )
                                        Text(
                                            text = formatTime(duration),
                                            color = Color.White,
                                            fontSize = 12.sp
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        IconButton(onClick = { /* TODO: Expand functionality */ }) {
                                            Icon(
                                                painter = painterResource(id = R.drawable.ic_repeat),
                                                contentDescription = "Expand",
                                                tint = Color.White,
                                                modifier = Modifier.size(30.dp)
                                            )
                                        }

                                        IconButton(onClick = { playPreviousSong() }) {
                                            Icon(
                                                painter = painterResource(id = R.drawable.ic_rewind),
                                                contentDescription = "Rewind",
                                                tint = Color.White,
                                                modifier = Modifier.size(50.dp)
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
                                                painter = painterResource(id = if (exoPlayer.isPlaying) R.drawable.ic_pause else R.drawable.ic_play),
                                                contentDescription = "Play/Pause",
                                                tint = Color.Black,
                                                modifier = Modifier.size(50.dp)
                                            )
                                        }
                                        IconButton(onClick = { playNextSong() }) {
                                            Icon(
                                                painter = painterResource(id = R.drawable.ic_forward),
                                                contentDescription = "Forward",
                                                tint = Color.White,
                                                modifier = Modifier.size(50.dp)
                                            )
                                        }
                                        IconButton(onClick = { /* TODO: Shuffle functionality */ }) {
                                            Icon(
                                                painter = painterResource(id = R.drawable.ic_shuffle),
                                                contentDescription = "Shuffle",
                                                tint = Color.White,
                                                modifier = Modifier.size(50.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    )
                }
            }
        )
    }
}