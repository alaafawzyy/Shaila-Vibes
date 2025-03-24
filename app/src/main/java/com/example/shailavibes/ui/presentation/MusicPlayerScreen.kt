package com.example.shailavibes.ui.presentation

import android.annotation.SuppressLint
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.shailavibes.R
import com.example.shailavibes.ui.data.Song
import com.example.shailavibes.ui.theme.Green
import com.example.shailavibes.ui.utils.SongList
import com.example.shailavibes.ui.utils.getSongsFromRaw
import com.example.shailavibes.ui.utils.playSong
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

@SuppressLint("UnrememberedMutableState")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MusicPlayerScreen() {
    val context = LocalContext.current
    val initialSongs = getSongsFromRaw(context)
    val sharedPreferences = context.getSharedPreferences("favorites_prefs", Context.MODE_PRIVATE)
    val favoriteIds = sharedPreferences.getStringSet("favorite_songs", emptySet())?.map { it.toInt() } ?: emptyList()
    initialSongs.forEach { song ->
        song.isFavorite = favoriteIds.contains(song.resourceId)
    }
    val songs: SnapshotStateList<Song> = remember { mutableStateListOf<Song>().apply { addAll(initialSongs) } }
    var selectedSong by remember { mutableStateOf<Song?>(null) }
    var currentSongIndex by remember { mutableStateOf(-1) }
    val exoPlayer = remember { ExoPlayer.Builder(context).build() }
    val scope = rememberCoroutineScope()
    var showMessage by remember { mutableStateOf<String?>(null) }
    var isRepeatModeOn by remember { mutableStateOf(false) }
    var isShuffleModeOn by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }
    var showFavoritesOnly by remember { mutableStateOf(false) }
    var favoritesUpdateTrigger by remember { mutableStateOf(0) } // متغير جديد لإجبار تحديث filteredSongs

    val filteredSongs by derivedStateOf {
        // استخدام favoritesUpdateTrigger عشان نجبر الـ derivedStateOf على إعادة الحساب
        favoritesUpdateTrigger
        if (showFavoritesOnly) {
            songs.filter { it.isFavorite }
        } else if (searchQuery.isEmpty()) {
            songs
        } else {
            songs.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                        it.artist.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    // دالة لحفظ الأغاني المفضلة في SharedPreferences
    fun saveFavorites() {
        val favoriteIds = songs.filter { it.isFavorite }.map { it.resourceId.toString() }.toSet()
        sharedPreferences.edit().putStringSet("favorite_songs", favoriteIds).apply()
        // تغيير قيمة favoritesUpdateTrigger عشان يجبر الـ filteredSongs على التحديث
        favoritesUpdateTrigger++
    }

    fun playNextSong() {
        if (filteredSongs.isNotEmpty()) {
            currentSongIndex = if (currentSongIndex < filteredSongs.size - 1) {
                currentSongIndex + 1
            } else {
                0
            }
            selectedSong = filteredSongs[currentSongIndex]
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
        if (filteredSongs.isNotEmpty()) {
            currentSongIndex = if (currentSongIndex > 0) {
                currentSongIndex - 1
            } else {
                filteredSongs.size - 1
            }
            selectedSong = filteredSongs[currentSongIndex]
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
        if (filteredSongs.isNotEmpty()) {
            val shuffledIndices = filteredSongs.indices.shuffled()
            currentSongIndex = shuffledIndices[0]
            selectedSong = filteredSongs[currentSongIndex]
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

    // متابعة تغييرات الـ filteredSongs لتحديث currentSongIndex و selectedSong
    LaunchedEffect(filteredSongs) {
        if (selectedSong != null) {
            // ابحث عن الأغنية الحالية في الـ filteredSongs الجديدة
            val newIndex = filteredSongs.indexOfFirst { it == selectedSong }
            if (newIndex != -1) {
                // لو الأغنية لسه موجودة في الـ filteredSongs، حدث الـ currentSongIndex
                currentSongIndex = newIndex
            } else {
                // لو الأغنية مش موجودة (مثلًا لأنها اتشالت من المفضلة وإحنا في وضع المفضلة)، وقف التشغيل
                exoPlayer.pause()
                selectedSong = null
                currentSongIndex = -1
            }
        }
    }

    // إضافة Listener للـ ExoPlayer عشان يشغل السورة اللي بعدها لما الحالية تخلّص
    LaunchedEffect(exoPlayer) {
        exoPlayer.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_ENDED) {
                    if (isShuffleModeOn) {
                        // لو الـ Shuffle مفعّل، نختار سورة عشوائية
                        shuffleSongs()
                    } else {
                        // لو الـ Shuffle مش مفعّل، نشغل السورة اللي بعدها
                        playNextSong()
                    }
                }
            }
        })
    }

    // Drawer State
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

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

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        DrawerLayout(
            drawerState = drawerState,
            drawerContent = {
                DrawerContent(
                    onFilterChange = { showFavorites ->
                        showFavoritesOnly = showFavorites
                        scope.launch { drawerState.close() }
                    }
                )
            },
            content = { paddingValues ->
                Scaffold(
                    containerColor = Color(0xFF192233),
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        TopAppBar(
                            navigationIcon = {
                                IconButton(onClick = {
                                    scope.launch {
                                        println("Opening Drawer")
                                        drawerState.open()
                                    }
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.Menu,
                                        contentDescription = "Menu",
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
                                                            text = "Search...",
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
                                            searchQuery = ""
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
                                        Text(
                                            text = stringResource(R.string.appbar_title),
                                            color = Color.White,
                                            fontWeight = FontWeight.Medium,
                                            modifier = Modifier.padding(start = 10.dp)
                                        )
                                        Spacer(modifier = Modifier.width(150.dp))
                                        Icon(
                                            imageVector = Icons.Default.Search,
                                            contentDescription = "Search",
                                            tint = Color.White,
                                            modifier = Modifier
                                                .size(30.dp)
                                                .clickable {
                                                    isSearchActive = true
                                                }
                                        )
                                    }
                                }
                            },
                            actions = {
                                IconButton(onClick = { /* TODO: Handle more options click */ }) {
                                    Icon(
                                        imageVector = Icons.Default.MoreVert,
                                        contentDescription = "More Options",
                                        tint = Color.White
                                    )
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = Color(0xFF353d48)
                            ),
                            modifier = Modifier
                                .clip(RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp, topStart = 16.dp, topEnd = 16.dp))
                        )
                    },
                    bottomBar = {
                        Surface(
                            color = Color(0xFF404a56),
                            modifier = Modifier
                                .height(150.dp)
                                .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = if (selectedSong != null) "${selectedSong!!.title} - ${selectedSong!!.artist}" else stringResource(
                                        R.string.choose_soura
                                    ),
                                    color = Color.White,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(top = 5.dp)
                                )

                                Spacer(Modifier.height(8.dp))



                                var currentPosition by remember { mutableStateOf(0L) }
                                var duration by remember { mutableStateOf(0L) }
                                var isSeeking by remember { mutableStateOf(false) }

                                LaunchedEffect(exoPlayer, isSeeking) {
                                    if (!isSeeking) {
                                        while (true) {
                                            currentPosition = exoPlayer.currentPosition
                                            duration = exoPlayer.duration
                                            delay(1000L)
                                        }
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

                                    Slider(
                                        value = if (duration > 0) currentPosition.toFloat() / duration else 0f,
                                        onValueChange = { newValue ->
                                            isSeeking = true
                                            currentPosition = (newValue * duration).toLong()
                                        },
                                        onValueChangeFinished = {
                                            exoPlayer.seekTo(currentPosition)
                                            isSeeking = false
                                            if (exoPlayer.isPlaying) {
                                                exoPlayer.play()
                                            }
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(horizontal = 8.dp),
                                        colors = SliderDefaults.colors(
                                            thumbColor = Green,
                                            activeTrackColor = Green,
                                            inactiveTrackColor = Color.Gray
                                        )
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
                                        .padding(start = 10.dp, end = 10.dp , bottom = 16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    IconButton(onClick = {
                                        isShuffleModeOn = !isShuffleModeOn
                                        if (isShuffleModeOn) {
                                            shuffleSongs()
                                            Toast.makeText(context, "تم تفعيل الاختيار العشوائي", Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(context, "تم إلغاء الاختيار العشوائي", Toast.LENGTH_SHORT).show()
                                        }
                                    }) {
                                        Icon(
                                            imageVector = Icons.Filled.Shuffle,
                                            contentDescription = "Shuffle",
                                            tint = if (isShuffleModeOn) Green else Color.White,
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
                                        if (isRepeatModeOn) {
                                            Toast.makeText(context, "تم وضع التكرار", Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(context, "تم إلغاء وضع التكرار", Toast.LENGTH_SHORT).show()
                                        }
                                        scope.launch {
                                            delay(2000L)
                                            showMessage = null
                                        }
                                    }) {
                                        Icon(
                                            imageVector = Icons.Filled.Repeat,
                                            contentDescription = "Repeat",
                                            tint = if (isRepeatModeOn) Green else Color.White,
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
                            songs = filteredSongs,
                            onSongClick = { song ->
                                selectedSong = song
                                currentSongIndex = filteredSongs.indexOf(song)
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
                            onFavoriteToggle = { song ->
                                song.isFavorite = !song.isFavorite
                                saveFavorites()
                            },
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0xFF2c343f))
                                .padding(paddingValues)
                        )
                    }
                )
            }
        )
    }
}

@Composable
fun DrawerLayout(
    drawerState: DrawerState,
    drawerContent: @Composable () -> Unit,
    content: @Composable (PaddingValues) -> Unit
) {
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            println("DrawerContent is being rendered")
            BoxWithConstraints {
                val maxWidth = this.maxWidth
                val drawerWidth = minOf(240.dp, maxWidth * 0.7f)
                println("Drawer Width: $drawerWidth, Max Width: $maxWidth")
                Box(
                    modifier = Modifier
                        .width(drawerWidth)
                        .fillMaxHeight()
                        .background(Color(0xFF192233))
                ) {
                    drawerContent()
                }
            }
        },
        gesturesEnabled = true,
        content = {
            content(PaddingValues(0.dp))
        }
    )
}