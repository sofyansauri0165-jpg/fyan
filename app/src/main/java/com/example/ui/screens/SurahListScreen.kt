package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.config.AppConfig
import com.example.data.model.SurahItemDto
import com.example.ui.QuranViewModel
import com.example.ui.ScreenDestination
import com.example.ui.UiState
import com.example.ui.components.AppHeader
import com.example.ui.theme.*
import java.util.Locale

@Composable
fun SurahListScreen(
    viewModel: QuranViewModel,
    modifier: Modifier = Modifier
) {
    val surahState by viewModel.surahListState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("Semua") } // "Semua", "Mekah", "Madinah"

    Scaffold(
        topBar = {
            AppHeader(
                title = "Daftar Surat",
                subtitle = "114 Surat Al-Qur'anul Karim"
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
                    .testTag("surah_search_input"),
                placeholder = { Text("Cari surat atau arti...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Cari",
                        tint = DeepEmerald
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Hapus")
                        }
                    }
                },
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Emerald,
                    unfocusedBorderColor = BorderLight,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                ),
                singleLine = true
            )

            // Filter Chips (Semua, Makkiyah, Madaniyah)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Semua", "Mekah", "Madinah").forEach { filter ->
                    val isSelected = selectedFilter == filter
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedFilter = filter },
                        label = {
                            Text(
                                text = when (filter) {
                                    "Mekah" -> "Makkiyah"
                                    "Madinah" -> "Madaniyah"
                                    else -> "Semua (114)"
                                }
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = DeepEmerald,
                            selectedLabelColor = Color.White,
                            containerColor = MaterialTheme.colorScheme.surface,
                            labelColor = Charcoal
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Surah List or Loading/Error State
            when (val state = surahState) {
                is UiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = Emerald)
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "Memuat 114 Surat...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MutedTextLight
                            )
                        }
                    }
                }

                is UiState.Error -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.CloudOff,
                                contentDescription = null,
                                tint = ErrorRed,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = state.message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Charcoal
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { viewModel.loadSurahs(forceRefresh = true) },
                                colors = ButtonDefaults.buttonColors(containerColor = Emerald)
                            ) {
                                Text("Coba Lagi", color = Color.White)
                            }
                        }
                    }
                }

                is UiState.Success -> {
                    val filteredList = state.data.filter { surah ->
                        val matchesQuery = searchQuery.isBlank() ||
                                surah.namaLatin.lowercase(Locale.ROOT).contains(searchQuery.lowercase(Locale.ROOT)) ||
                                surah.arti.lowercase(Locale.ROOT).contains(searchQuery.lowercase(Locale.ROOT)) ||
                                surah.nomor.toString() == searchQuery.trim()

                        val matchesFilter = when (selectedFilter) {
                            "Mekah" -> surah.tempatTurun.equals("Mekah", ignoreCase = true)
                            "Madinah" -> surah.tempatTurun.equals("Madinah", ignoreCase = true)
                            else -> true
                        }

                        matchesQuery && matchesFilter
                    }

                    if (filteredList.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Tidak ditemukan surat yang cocok dengan \"$searchQuery\"",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MutedTextLight
                            )
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 90.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(filteredList, key = { it.nomor }) { surah ->
                                SurahListItem(
                                    surah = surah,
                                    onClick = {
                                        viewModel.navigateTo(ScreenDestination.Reader(surahNumber = surah.nomor))
                                    }
                                )
                            }
                        }
                    }
                }

                UiState.Idle -> {}
            }
        }
    }
}

@Composable
private fun SurahListItem(
    surah: SurahItemDto,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("surah_item_${surah.nomor}"),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Surah Number Badge
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(DeepEmerald.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = surah.nomor.toString(),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = DeepEmerald
                    )
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Surah Latin Name, Meaning, Verse Count
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = surah.namaLatin,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Charcoal
                    )
                )
                Text(
                    text = "${surah.arti} • ${surah.tempatTurun} • ${surah.jumlahAyat} ayat",
                    style = MaterialTheme.typography.bodySmall.copy(color = MutedTextLight)
                )
            }

            // Arabic Name
            Text(
                text = surah.nama,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Emerald
                )
            )
        }
    }
}
