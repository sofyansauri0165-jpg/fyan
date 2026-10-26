package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ErrorOutline
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
import com.example.ui.QuranViewModel
import com.example.ui.UiState
import com.example.ui.components.AppHeader
import com.example.ui.theme.*

@Composable
fun TafsirScreen(
    surahNumber: Int,
    targetAyah: Int? = null,
    viewModel: QuranViewModel,
    modifier: Modifier = Modifier
) {
    val tafsirState by viewModel.tafsirState.collectAsState()
    val listState = rememberLazyListState()

    LaunchedEffect(surahNumber) {
        viewModel.loadTafsir(surahNumber)
    }

    LaunchedEffect(tafsirState, targetAyah) {
        if (tafsirState is UiState.Success && targetAyah != null && targetAyah > 0) {
            val idx = (targetAyah - 1).coerceAtLeast(0)
            listState.animateScrollToItem(idx)
        }
    }

    BackHandler {
        viewModel.navigateBack()
    }

    Scaffold(
        topBar = {
            val title = when (val s = tafsirState) {
                is UiState.Success -> "Tafsir ${s.data.namaLatin}"
                else -> "Tafsir Kemenag"
            }
            AppHeader(
                title = title,
                subtitle = "Kementerian Agama Republik Indonesia",
                onBackClick = { viewModel.navigateBack() }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (val state = tafsirState) {
                is UiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = Emerald)
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "Memuat tafsir surat...",
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
                                imageVector = Icons.Default.ErrorOutline,
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
                                onClick = { viewModel.loadTafsir(surahNumber) },
                                colors = ButtonDefaults.buttonColors(containerColor = Emerald)
                            ) {
                                Text("Coba Lagi", color = Color.White)
                            }
                        }
                    }
                }

                is UiState.Success -> {
                    val tafsirData = state.data
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(tafsirData.tafsir, key = { it.ayat }) { item ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("tafsir_item_${item.ayat}"),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(bottom = 10.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(34.dp)
                                                .clip(CircleShape)
                                                .background(DeepEmerald),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = item.ayat.toString(),
                                                style = MaterialTheme.typography.labelMedium.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = SoftGold
                                                )
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(10.dp))

                                        Text(
                                            text = "Ayat ${item.ayat}",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = DeepEmerald
                                            )
                                        )
                                    }

                                    Text(
                                        text = item.teks,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = Charcoal,
                                            lineHeight = 24.sp
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                UiState.Idle -> {}
            }
        }
    }
}
