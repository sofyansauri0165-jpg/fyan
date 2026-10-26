package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.QuranViewModel
import com.example.ui.ScreenDestination
import com.example.ui.components.MiniAudioPlayer
import com.example.ui.screens.*
import com.example.ui.theme.*

class MainActivity : ComponentActivity() {
    private val viewModel: QuranViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val userSettings by viewModel.userSettings.collectAsState()
            val isDarkMode = userSettings?.isDarkMode ?: false

            MuslimQuranTheme(darkTheme = isDarkMode) {
                MainAppContent(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainAppContent(viewModel: QuranViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val audioState by viewModel.audioState.collectAsState()

    val isMainTab = when (currentScreen) {
        is ScreenDestination.Home,
        is ScreenDestination.SurahList,
        is ScreenDestination.Search,
        is ScreenDestination.Bookmarks,
        is ScreenDestination.Settings -> true
        else -> false
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            Column {
                // Mini audio player banner (shown when audio is active)
                MiniAudioPlayer(
                    state = audioState,
                    onPlayPause = {
                        if (audioState.isPlaying) viewModel.pauseAudio() else viewModel.resumeAudio()
                    },
                    onSeek = { progress -> viewModel.seekAudio(progress) },
                    onClose = { viewModel.stopAudio() }
                )

                // Bottom Navigation Bar for the 5 main tabs
                if (isMainTab) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = DeepEmerald,
                        tonalElevation = 6.dp
                    ) {
                        BottomNavItem(
                            icon = Icons.Default.Home,
                            label = "Beranda",
                            testTag = "bottom_nav_home",
                            selected = currentScreen is ScreenDestination.Home,
                            onClick = { viewModel.navigateTo(ScreenDestination.Home) }
                        )

                        BottomNavItem(
                            icon = Icons.Default.FormatListNumbered,
                            label = "Surat",
                            testTag = "bottom_nav_surahs",
                            selected = currentScreen is ScreenDestination.SurahList,
                            onClick = { viewModel.navigateTo(ScreenDestination.SurahList) }
                        )

                        BottomNavItem(
                            icon = Icons.Default.Search,
                            label = "Cari",
                            testTag = "bottom_nav_search",
                            selected = currentScreen is ScreenDestination.Search,
                            onClick = { viewModel.navigateTo(ScreenDestination.Search) }
                        )

                        BottomNavItem(
                            icon = Icons.Default.Bookmark,
                            label = "Simpan",
                            testTag = "bottom_nav_bookmarks",
                            selected = currentScreen is ScreenDestination.Bookmarks,
                            onClick = { viewModel.navigateTo(ScreenDestination.Bookmarks) }
                        )

                        BottomNavItem(
                            icon = Icons.Default.Settings,
                            label = "Pengaturan",
                            testTag = "bottom_nav_settings",
                            selected = currentScreen is ScreenDestination.Settings,
                            onClick = { viewModel.navigateTo(ScreenDestination.Settings) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (val dest = currentScreen) {
                is ScreenDestination.Home -> HomeScreen(viewModel = viewModel)
                is ScreenDestination.SurahList -> SurahListScreen(viewModel = viewModel)
                is ScreenDestination.Search -> SearchScreen(viewModel = viewModel)
                is ScreenDestination.Bookmarks -> BookmarksScreen(viewModel = viewModel)
                is ScreenDestination.Settings -> SettingsScreen(viewModel = viewModel)
                is ScreenDestination.Reader -> ReaderScreen(
                    surahNumber = dest.surahNumber,
                    targetAyah = dest.targetAyah,
                    viewModel = viewModel
                )
                is ScreenDestination.Tafsir -> TafsirScreen(
                    surahNumber = dest.surahNumber,
                    targetAyah = dest.targetAyah,
                    viewModel = viewModel
                )
            }
        }
    }
}

@Composable
private fun RowScope.BottomNavItem(
    icon: ImageVector,
    label: String,
    testTag: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    NavigationBarItem(
        icon = { Icon(imageVector = icon, contentDescription = label) },
        label = { Text(text = label) },
        selected = selected,
        onClick = onClick,
        modifier = Modifier.testTag(testTag),
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = ForestGreen,
            selectedTextColor = DeepEmerald,
            indicatorColor = MetallicGold,
            unselectedIconColor = MutedTextLight,
            unselectedTextColor = MutedTextLight
        )
    )
}
