package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.repository.SearchResultItem
import com.example.ui.QuranViewModel
import com.example.ui.ScreenDestination
import com.example.ui.components.AppHeader
import com.example.ui.theme.*

@Composable
fun SearchScreen(
    viewModel: QuranViewModel,
    modifier: Modifier = Modifier
) {
    val query by viewModel.searchQuery.collectAsState()
    val results by viewModel.searchResults.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()

    Scaffold(
        topBar = {
            AppHeader(
                title = "Pencarian Global",
                subtitle = "Cari surat, ayat, atau arti terjemahan"
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
            // Search Input Field
            OutlinedTextField(
                value = query,
                onValueChange = { viewModel.onSearchQueryChanged(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .testTag("global_search_input"),
                placeholder = { Text("Contoh: Al-Baqarah, sabar, surga, rezeki...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Cari",
                        tint = DeepEmerald
                    )
                },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
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

            // Result count or searching indicator
            if (isSearching) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = Emerald,
                    trackColor = BorderLight
                )
            }

            if (query.isNotBlank()) {
                Text(
                    text = if (isSearching) "Mencari dalam Al-Qur'an..." else "Ditemukan ${results.size} hasil",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                    color = MutedTextLight,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }

            // Results List
            if (results.isEmpty() && query.isNotBlank() && !isSearching) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Tidak ada hasil untuk \"$query\". Coba kata kunci lain atau buka surat terlebih dahulu untuk mengindeks seluruh ayat.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MutedTextLight,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 90.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(results) { item ->
                        SearchResultCard(
                            item = item,
                            onClick = {
                                viewModel.navigateTo(
                                    ScreenDestination.Reader(
                                        surahNumber = item.surahNumber,
                                        targetAyah = item.ayahNumber
                                    )
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchResultCard(
    item: SearchResultItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("search_result_${item.surahNumber}_${item.ayahNumber}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(DeepEmerald.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = item.surahNumber.toString(),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = DeepEmerald
                            )
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Text(
                        text = "QS. ${item.surahName} : Ayat ${item.ayahNumber}",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = DeepEmerald
                        )
                    )
                }

                Text(
                    text = item.surahArabic,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MetallicGold
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = item.matchSnippet,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Charcoal,
                    fontWeight = FontWeight.Medium
                ),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
