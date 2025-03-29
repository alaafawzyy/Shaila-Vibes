package com.example.shailavibes.ui.presentation

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.shailavibes.R
import com.example.shailavibes.ui.data.Song
import com.example.shailavibes.ui.theme.Green
import com.example.shailavibes.ui.utils.SongList
import com.example.shailavibes.ui.utils.getSongsFromRaw
import com.example.shailavibes.ui.utils.playSong
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("UnrememberedMutableState")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MusicPlayerScreen(activity: Activity, initialize: Unit) {
    val context = activity
    val initialSongs = getSongsFromRaw(context)
    val sharedPreferences = context.getSharedPreferences("favorites_prefs", Context.MODE_PRIVATE)
    val favoriteIds = sharedPreferences.getStringSet("favorite_songs", emptySet())?.map { it.toInt() } ?: emptyList()
    initialSongs.forEach { song -> song.isFavorite = favoriteIds.contains(song.resourceId) }
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
    var favoritesUpdateTrigger by remember { mutableStateOf(0) }

    // متغيرات الإعلان البيني
    var interstitialAd by remember { mutableStateOf<InterstitialAd?>(null) }
    var clickCount by remember { mutableStateOf(0) }
    var isFirstClick by remember { mutableStateOf(true) }
    val mycontext = LocalContext.current
    val isConnected = checkInternetConnection(context)

    LaunchedEffect(Unit) {
        MobileAds.initialize(context) {
            Log.d("InterstitialAd", "MobileAds initialized")
            loadInterstitialAd(context) { ad ->
                interstitialAd = ad
                if (ad != null) {
                    Log.d("InterstitialAd", "Initial ad loaded successfully")
                } else {
                    Log.w("InterstitialAd", "Initial ad loading failed")
                }
            }
        }
    }

    val filteredSongs by derivedStateOf {
        favoritesUpdateTrigger
        if (showFavoritesOnly) songs.filter { it.isFavorite }
        else if (searchQuery.isEmpty()) songs
        else songs.filter { it.title.contains(searchQuery, ignoreCase = true) || it.artist.contains(searchQuery, ignoreCase = true) }
    }

    fun saveFavorites(song: Song) {
        val favoriteIds = songs.filter { it.isFavorite }.map { it.resourceId.toString() }.toSet()
        sharedPreferences.edit().putStringSet("favorite_songs", favoriteIds).apply()
        favoritesUpdateTrigger++
        Toast.makeText(context, if (song.isFavorite) "تم إضافة ${song.title} إلى المفضلة" else "تم إزالة ${song.title} من المفضلة", Toast.LENGTH_SHORT).show()
    }

    fun playNextSong() {
        if (filteredSongs.isNotEmpty()) {
            currentSongIndex = if (currentSongIndex < filteredSongs.size - 1) currentSongIndex + 1 else 0
            selectedSong = filteredSongs[currentSongIndex]
            playSong(context, exoPlayer, selectedSong!!)
        }
    }

    fun playPreviousSong() {
        if (filteredSongs.isNotEmpty()) {
            currentSongIndex = if (currentSongIndex > 0) currentSongIndex - 1 else filteredSongs.size - 1
            selectedSong = filteredSongs[currentSongIndex]
            playSong(context, exoPlayer, selectedSong!!)
        }
    }

    LaunchedEffect(exoPlayer) {
        exoPlayer.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_ENDED) {
                    if (isShuffleModeOn) {
                        val shuffledIndices = filteredSongs.indices.shuffled()
                        currentSongIndex = shuffledIndices[0]
                        selectedSong = filteredSongs[currentSongIndex]
                        playSong(context, exoPlayer, selectedSong!!)
                    } else {
                        playNextSong()
                    }
                }
            }
        })
    }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    LaunchedEffect(isRepeatModeOn) {
        exoPlayer.repeatMode = if (isRepeatModeOn) Player.REPEAT_MODE_ONE else Player.REPEAT_MODE_OFF
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
            modifier = Modifier,
            content = { paddingValues ->
                Scaffold(
                    containerColor = Color(0xFF192233),
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        TopAppBar(
                            navigationIcon = {
                                IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                    Icon(Icons.Default.Menu, "Menu", tint = Color.White)
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
                                            textStyle = LocalTextStyle.current.copy(color = Color.Black, fontSize = 16.sp),
                                            decorationBox = { innerTextField ->
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(Icons.Default.Search, "Search", tint = Color.Gray, modifier = Modifier.size(20.dp))
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    if (searchQuery.isEmpty()) Text("Search...", color = Color.Gray, fontSize = 16.sp)
                                                    innerTextField()
                                                }
                                            }
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        IconButton(onClick = { isSearchActive = false; searchQuery = "" }) {
                                            Icon(Icons.Default.Close, "Close Search", tint = Color.White)
                                        }
                                    }
                                } else {
                                    Row {
                                        Text(stringResource(R.string.appbar_title), color = Color.White, fontWeight = FontWeight.Medium, modifier = Modifier.padding(start = 10.dp))
                                        Spacer(modifier = Modifier.width(150.dp))
                                        Icon(Icons.Default.Search, "Search", tint = Color.White, modifier = Modifier
                                            .size(30.dp)
                                            .clickable { isSearchActive = true })
                                    }
                                }
                            },
                            actions = {
                                IconButton(onClick = { /* TODO: Handle more options click */ }) {
                                    Icon(Icons.Default.MoreVert, "More Options", tint = Color.White)
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF353d48)),
                            modifier = Modifier.clip(RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp, topStart = 16.dp, topEnd = 16.dp))
                        )
                    },
                    bottomBar = {
                        Surface(
                            color = Color(0xFF404a56),
                            modifier = Modifier
                                .height(if (isConnected) 250.dp else 150.dp)
                                .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = selectedSong?.let { "${it.title} - ${it.artist}" } ?: stringResource(R.string.choose_soura),
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
                                    Text(formatTime(currentPosition), color = Color.White, fontSize = 12.sp)
                                    Slider(
                                        value = if (duration > 0) currentPosition.toFloat() / duration else 0f,
                                        onValueChange = { newValue ->
                                            isSeeking = true
                                            currentPosition = (newValue * duration).toLong()
                                        },
                                        onValueChangeFinished = {
                                            exoPlayer.seekTo(currentPosition)
                                            isSeeking = false
                                            if (exoPlayer.isPlaying) exoPlayer.play()
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(horizontal = 8.dp),
                                        colors = SliderDefaults.colors(thumbColor = Green, activeTrackColor = Green, inactiveTrackColor = Color.Gray)
                                    )
                                    Text(formatTime(duration), color = Color.White, fontSize = 12.sp)
                                }

                                Spacer(Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 10.dp, end = 10.dp, bottom = 16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    IconButton(
                                        onClick = {
                                            clickCount++
                                            Log.d("InterstitialAd", "Shuffle clicked, clickCount: $clickCount")
                                            if (isFirstClick || clickCount % 4 == 0) {
                                                showInterstitialAd(context, activity, interstitialAd) { ad ->
                                                    interstitialAd = ad
                                                    if (!isFirstClick) clickCount = 0
                                                }
                                            }
                                            isFirstClick = false
                                            isShuffleModeOn = !isShuffleModeOn
                                            Toast.makeText(context, if (isShuffleModeOn) "تم تفعيل الاختيار العشوائي" else "تم إلغاء الاختيار العشوائي", Toast.LENGTH_SHORT).show()
                                        }
                                    ) {
                                        Icon(Icons.Filled.Shuffle, "Shuffle", tint = if (isShuffleModeOn) Green else Color.White, modifier = Modifier.size(24.dp))
                                    }
                                    IconButton(
                                        onClick = {
                                            clickCount++
                                            Log.d("InterstitialAd", "Next clicked, clickCount: $clickCount")
                                            if (isFirstClick || clickCount % 4 == 0) {
                                                showInterstitialAd(context, activity, interstitialAd) { ad ->
                                                    interstitialAd = ad
                                                    if (!isFirstClick) clickCount = 0
                                                }
                                            }
                                            isFirstClick = false
                                            playNextSong()
                                        }
                                    ) {
                                        Icon(Icons.Filled.SkipNext, "Next", tint = Color.White, modifier = Modifier.size(24.dp))
                                    }

                                    IconButton(
                                        onClick = {
                                            clickCount++
                                            Log.d("InterstitialAd", "Play/Pause clicked, clickCount: $clickCount")
                                            if (isFirstClick || clickCount % 4 == 0) {
                                                showInterstitialAd(context, activity, interstitialAd) { ad ->
                                                    interstitialAd = ad
                                                    if (!isFirstClick) clickCount = 0
                                                }
                                            }
                                            isFirstClick = false
                                            if (selectedSong != null) {
                                                if (exoPlayer.isPlaying) exoPlayer.pause() else exoPlayer.play()
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
                                    IconButton(
                                        onClick = {
                                            clickCount++
                                            Log.d("InterstitialAd", "Previous clicked, clickCount: $clickCount")
                                            if (isFirstClick || clickCount % 4 == 0) {
                                                showInterstitialAd(context, activity, interstitialAd) { ad ->
                                                    interstitialAd = ad
                                                    if (!isFirstClick) clickCount = 0
                                                }
                                            }
                                            isFirstClick = false
                                            playPreviousSong()
                                        }
                                    ) {
                                        Icon(Icons.Filled.SkipPrevious, "Previous", tint = Color.White, modifier = Modifier.size(24.dp))
                                    }
                                    IconButton(
                                        onClick = {
                                            clickCount++
                                            Log.d("InterstitialAd", "Repeat clicked, clickCount: $clickCount")
                                            if (isFirstClick || clickCount % 4 == 0) {
                                                showInterstitialAd(context, activity, interstitialAd) { ad ->
                                                    interstitialAd = ad
                                                    if (!isFirstClick) clickCount = 0
                                                }
                                            }
                                            isFirstClick = false
                                            isRepeatModeOn = !isRepeatModeOn
                                            Toast.makeText(context, if (isRepeatModeOn) "تم وضع التكرار" else "تم إلغاء وضع التكرار", Toast.LENGTH_SHORT).show()
                                        }
                                    ) {
                                        Icon(Icons.Filled.Repeat, "Repeat", tint = if (isRepeatModeOn) Green else Color.White, modifier = Modifier.size(24.dp))
                                    }
                                }

                                showMessage?.let { message ->
                                    Text(message, color = Color.White, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
                                }


                                AndroidView(
                                    factory = { ctx ->
                                        AdView(ctx).apply {
                                            setAdSize(AdSize.BANNER)
                                            adUnitId = context.getString(R.string.banner_id)
                                            loadAd(AdRequest.Builder().build())
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .wrapContentHeight()
                                        .padding(top = 8.dp)
                                )
                            }
                        }
                    },
                    content = { paddingValues ->
                        SongList(
                            songs = filteredSongs,
                            onSongClick = { song ->
                                clickCount++
                                Log.d("InterstitialAd", "Song clicked, clickCount: $clickCount")
                                if (isFirstClick || clickCount % 4 == 0) {
                                    showInterstitialAd(context, activity, interstitialAd) { ad ->
                                        interstitialAd = ad
                                        if (!isFirstClick) clickCount = 0
                                    }
                                }
                                isFirstClick = false
                                selectedSong = song
                                currentSongIndex = filteredSongs.indexOf(song)
                                val success = playSong(context, exoPlayer, song)
                                showMessage = if (success) "تم اختيار ${song.title}" else "فشل تشغيل ${song.title}"
                                scope.launch { delay(2000L); showMessage = null }
                            },
                            onFavoriteToggle = { song ->
                                clickCount++
                                Log.d("InterstitialAd", "Favorite toggled, clickCount: $clickCount")
                                if (isFirstClick || clickCount % 4 == 0) {
                                    showInterstitialAd(context, activity, interstitialAd) { ad ->
                                        interstitialAd = ad
                                        if (!isFirstClick) clickCount = 0
                                    }
                                }
                                isFirstClick = false
                                song.isFavorite = !song.isFavorite
                                saveFavorites(song)
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

fun loadInterstitialAd(context: Context, onAdLoaded: (InterstitialAd?) -> Unit) {
    val adRequest = AdRequest.Builder().build()
    InterstitialAd.load(
        context,
        context.getString(R.string.Interstitial_id),
        adRequest,
        object : InterstitialAdLoadCallback() {
            override fun onAdLoaded(ad: InterstitialAd) {
                Log.d("InterstitialAd", "Ad loaded successfully: $ad")
                onAdLoaded(ad)
            }

            override fun onAdFailedToLoad(error: LoadAdError) {
                Log.e("InterstitialAd", "Ad failed to load: ${error.message}")
                onAdLoaded(null)

            }
        }
    )
}

fun checkInternetConnection(context: Context): Boolean {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = connectivityManager.activeNetwork ?: return false
    val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
    return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
}

fun showInterstitialAd(context: Context, activity: Activity, ad: InterstitialAd?, onAdClosed: (InterstitialAd?) -> Unit) {
    ad?.let { interstitial ->
        Log.d("InterstitialAd", "Showing interstitial ad, ad object: $interstitial")
        interstitial.show(activity)
        Log.d("InterstitialAd", "Ad shown, resetting to null")
        loadInterstitialAd(context) { newAd ->
            onAdClosed(newAd)
            Log.d("InterstitialAd", "New ad loaded after show: $newAd")
        }
    } ?: run {
        Log.w("InterstitialAd", "Ad not ready yet")
        loadInterstitialAd(context) { newAd ->
            onAdClosed(newAd)
            Log.d("InterstitialAd", "Ad reloaded due to null: $newAd")
        }
    }
}

@Composable
fun DrawerLayout(
    drawerState: DrawerState,
    drawerContent: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (PaddingValues) -> Unit
) {
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            BoxWithConstraints {
                val maxWidth = this.maxWidth
                val drawerWidth = minOf(240.dp, maxWidth * 0.7f)
                Box(modifier = Modifier
                    .width(drawerWidth)
                    .fillMaxHeight()
                    .background(Color(0xFF192233))) {
                    drawerContent()
                }
            }
        },
        gesturesEnabled = true,
        modifier = modifier,
        content = { content(PaddingValues(0.dp)) }
    )
}