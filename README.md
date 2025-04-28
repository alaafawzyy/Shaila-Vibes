# Shaila Vibes - Music Player App

A modern Android music player built with Jetpack Compose and ExoPlayer, featuring Arabic language support, favorites management, and intuitive controls.

## Features

🎵 **Music Playback**
- Play/pause functionality
- Next/previous track navigation
- Progress slider with time indicators
- Repeat (one/all) and shuffle modes

❤️ **Favorites System**
- Mark songs as favorites
- Filter to show only favorite songs
- Persisted using SharedPreferences

🔍 **Search & Filter**
- Search by song title or artist
- Filter favorites only from drawer menu

📱 **Modern UI**
- Jetpack Compose interface
- RTL support for Arabic
- Dark theme with custom colors
- Responsive layout for all screens

📢 **Ad Integration**
- Interstitial ads (shown periodically)
- Banner ads at the bottom

## Technical Implementation

### Architecture
- Single Activity with multiple Composables
- State management using Jetpack Compose
- Clean separation of UI and business logic

### Libraries Used
- **Jetpack Compose** - Modern UI toolkit
- **ExoPlayer** - Audio playback engine
- **AdMob** - For ad integration (banner & interstitial)
- **Material 3** - UI components and theming

### Key Components
1. `SongList` - Displays scrollable list of songs
2. `SongItem` - Individual song row with play/favorite controls
3. `MusicPlayerScreen` - Main screen with player controls
4. `DrawerContent` - Navigation drawer with filters
5. `playSong()` - Audio playback utility function

## Screenshots

| Song List | Player Controls | Favorites |
|-----------|-----------------|-----------|
| <img src="screenshots/list.png" width="200"> | <img src="screenshots/player.png" width="200"> | <img src="screenshots/favorites.png" width="200"> |

## Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/yourusername/shaila-vibes.git
