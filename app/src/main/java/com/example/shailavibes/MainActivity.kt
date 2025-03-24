package com.example.shailavibes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.shailavibes.ui.presentation.MusicPlayerScreen
import com.example.shailavibes.ui.theme.ShailaVibesTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ShailaVibesTheme {
                MusicPlayerScreen()
            }
        }
    }
}
