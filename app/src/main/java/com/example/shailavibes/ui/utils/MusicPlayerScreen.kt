package com.example.shailavibes.ui.utils

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.exoplayer.ExoPlayer
import com.example.shailavibes.ui.data.Song
import com.example.shailavibes.ui.DrawerContent
import com.example.shailavibes.ui.SongList
import kotlinx.coroutines.launch
import com.example.shailavibes.R
import kotlinx.coroutines.delay

@Composable
fun MusicPlayerScreen() {
    val context = LocalContext.current
    val drawerState = androidx.compose.material3.rememberDrawerState(initialValue = DrawerValue.Closed)
    val songs = remember { getSongsFromRaw(context) }
    var selectedSong by remember { mutableStateOf<Song?>(null) }
    var currentSongIndex by remember { mutableStateOf(-1) } // لتتبع الأغنية الحالية
    val exoPlayer = remember { ExoPlayer.Builder(context).build() }
    val scope = rememberCoroutineScope()
    var showMessage by remember { mutableStateOf<String?>(null) } // لعرض رسائل مؤقتة

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

    // وظيفة لخلط السور
    fun shuffleSongs() {
        if (songs.isNotEmpty()) {
            val shuffledSongs = songs.shuffled()
            currentSongIndex = 0
            selectedSong = shuffledSongs[currentSongIndex]
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
                                            text = "القرآن الكريم بدون نت",
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
                                // الـ RecyclerView (SongList) يتمدد لملء المساحة
                                SongList(
                                    songs = songs,
                                    onSongClick = { song ->
                                        selectedSong = song
                                        currentSongIndex = songs.indexOf(song)
                                        playSong(context, exoPlayer, song)
                                    },
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(bottom = 120.dp) // إضافة padding لتجنب التداخل مع الـ BottomAppBar
                                )

                                // الـ BottomAppBar ثابت في الأسفل
                                BottomAppBar(
                                    backgroundColor = Color(0xFF404a56),
                                    modifier = Modifier
                                        .height(220.dp)
                                        .align(Alignment.BottomCenter) // تثبيت الـ BottomAppBar في الأسفل
                                        .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp))
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 8.dp, vertical = 8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        // النص الديناميكي (اسم السورة واسم القارئ)
                                        Text(
                                            text = if (selectedSong != null) "${selectedSong!!.title} - ${selectedSong!!.artist}" else "قم باختيار سورة",
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
                                                showMessage = "تم فتح شاشة التوسيع (Expand)"
                                                scope.launch {
                                                    delay(2000L)
                                                    showMessage = null
                                                }
                                            }) {
                                                Icon(
                                                    imageVector = Icons.Filled.Repeat,
                                                    contentDescription = "Expand",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            }
                                        }

                                        // عرض رسالة مؤقتة إذا وجدت
                                        showMessage?.let { message ->
                                            Text(
                                                text = message,
                                                color = Color.White,
                                                fontSize = 12.sp,
                                                modifier = Modifier.padding(top = 8.dp)
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