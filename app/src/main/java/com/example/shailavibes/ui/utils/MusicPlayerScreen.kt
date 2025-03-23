package com.example.shailavibes.ui.utils

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
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
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.shailavibes.ui.data.Song
import com.example.shailavibes.ui.DrawerContent
import com.example.shailavibes.ui.SongList
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

@Composable
fun MusicPlayerScreen() {
    val context = LocalContext.current
    val drawerState = androidx.compose.material3.rememberDrawerState(initialValue = DrawerValue.Closed)
    val songs by remember { mutableStateOf(getSongsFromRaw(context)) }
    var selectedSong by remember { mutableStateOf<Song?>(null) }
    var currentSongIndex by remember { mutableStateOf(-1) }
    val exoPlayer = remember { ExoPlayer.Builder(context).build() }
    val scope = rememberCoroutineScope()
    var showMessage by remember { mutableStateOf<String?>(null) }
    var isRepeatModeOn by remember { mutableStateOf(false) }

    // متغيرات السيرش
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }

    // فلترة السور بناءً على نص البحث
    val filteredSongs = if (searchQuery.isEmpty()) {
        songs
    } else {
        songs.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
                    it.artist.contains(searchQuery, ignoreCase = true)
        }
    }


    // ربط isRepeatModeOn بالـ ExoPlayer
    LaunchedEffect(isRepeatModeOn) {
        exoPlayer.repeatMode = if (isRepeatModeOn) {
            Player.REPEAT_MODE_ONE
        } else {
            Player.REPEAT_MODE_OFF
        }
    }

    DisposableEffect(exoPlayer) {
        onDispose {
            exoPlayer.stop()
            exoPlayer.release()
        }
    }

    fun playNextSong() {
        if (songs.isNotEmpty()) {
            currentSongIndex = if (currentSongIndex < songs.size - 1) {
                currentSongIndex + 1
            } else {
                0
            }
            selectedSong = songs[currentSongIndex]
            val success = playSong(context, exoPlayer, selectedSong!!)
            if (!success) {
                showMessage = "فشل تشغيل ${selectedSong!!.title}"
                scope.launch {
                    delay(2000L)
                    showMessage = null
                }
            }
        }
    }

    fun playPreviousSong() {
        if (songs.isNotEmpty()) {
            currentSongIndex = if (currentSongIndex > 0) {
                currentSongIndex - 1
            } else {
                songs.size - 1
            }
            selectedSong = songs[currentSongIndex]
            val success = playSong(context, exoPlayer, selectedSong!!)
            if (!success) {
                showMessage = "فشل تشغيل ${selectedSong!!.title}"
                scope.launch {
                    delay(2000L)
                    showMessage = null
                }
            }
        }
    }

    fun shuffleSongs() {
        if (songs.isNotEmpty()) {
            val shuffledSongs = songs.shuffled()
            currentSongIndex = 0
            selectedSong = shuffledSongs[currentSongIndex]
            val success = playSong(context, exoPlayer, selectedSong!!)
            if (!success) {
                showMessage = "فشل تشغيل ${selectedSong!!.title}"
                scope.launch {
                    delay(2000L)
                    showMessage = null
                }
            }
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
                                    if (isSearchActive) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            BasicTextField(
                                                value = searchQuery,
                                                onValueChange = { searchQuery = it },
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .background(Color.White, RoundedCornerShape(8.dp))
                                                    .padding(8.dp),
                                                singleLine = true,
                                                textStyle = LocalTextStyle.current.copy(
                                                    color = Color.Black,
                                                    fontSize = 16.sp
                                                ),
                                                decorationBox = { innerTextField ->
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Search,
                                                            contentDescription = "Search",
                                                            tint = Color.Gray,
                                                            modifier = Modifier.size(20.dp)
                                                        )
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        if (searchQuery.isEmpty()) {
                                                            Text(
                                                                text = "search..",
                                                                color = Color.Gray,
                                                                fontSize = 16.sp
                                                            )
                                                        }
                                                        innerTextField()
                                                    }
                                                }
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            IconButton(onClick = {
                                                isSearchActive = false
                                                searchQuery = "" // إعادة تعيين نص البحث
                                            }) {
                                                Icon(
                                                    imageVector = Icons.Default.Close,
                                                    contentDescription = "Close Search",
                                                    tint = Color.White
                                                )
                                            }
                                        }
                                    } else {
                                        Row {
                                            Icon(
                                                imageVector = Icons.Default.Search,
                                                contentDescription = "Search",
                                                tint = Color.White,
                                                modifier = Modifier
                                                    .size(30.dp)
                                                    .clickable {
                                                        isSearchActive = true // تفعيل السيرش
                                                    }
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
                        bottomBar = {
                            BottomAppBar(
                                backgroundColor = Color(0xFF404a56),
                                modifier = Modifier
                                    .height(200.dp)
                                    .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp))
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp, vertical = 8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = if (selectedSong != null) "${selectedSong!!.title} - ${selectedSong!!.artist}" else "قم باختيار سورة",
                                        color = Color(0xFFf1c16b),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(top = 5.dp)
                                    )

                                    Spacer(Modifier.height(8.dp))

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

                                    var currentPosition by remember { mutableStateOf(0L) }
                                    var duration by remember { mutableStateOf(0L) }

                                    LaunchedEffect(exoPlayer) {
                                        while (true) {
                                            currentPosition = exoPlayer.currentPosition
                                            duration = exoPlayer.duration
                                            delay(1000L)
                                        }
                                    }

                                    fun formatTime(millis: Long): String {
                                        if (millis < 0) return "00:00"
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
                                            isRepeatModeOn = !isRepeatModeOn
                                            showMessage = if (isRepeatModeOn) "تم تفعيل وضع التكرار" else "تم إلغاء وضع التكرار"
                                            scope.launch {
                                                delay(2000L)
                                                showMessage = null
                                            }
                                        }) {
                                            Icon(
                                                imageVector = Icons.Filled.Repeat,
                                                contentDescription = "Repeat",
                                                tint = if (isRepeatModeOn) Color(0xFFFFA500) else Color.White,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                    }

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
                        },
                        content = { paddingValues ->
                            SongList(
                                songs = filteredSongs, // استخدام القايمة المفلترة
                                onSongClick = { song ->
                                    selectedSong = song
                                    currentSongIndex = songs.indexOf(song)
                                    val success = playSong(context, exoPlayer, song)
                                    showMessage = if (success) {
                                        "تم اختيار ${song.title}"
                                    } else {
                                        "فشل تشغيل ${song.title}"
                                    }
                                    scope.launch {
                                        delay(2000L)
                                        showMessage = null
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color(0xFF2c343f))
                                    .padding(paddingValues)
                            )
                        }
                    )
                }
            }
        )
    }
}