package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.config.AppConfig
import com.example.data.model.AyatDto
import com.example.data.model.SurahDetailDto
import com.example.ui.QuranViewModel
import com.example.ui.ScreenDestination
import com.example.ui.UiState
import com.example.ui.components.AppHeader
import com.example.ui.components.AutoScrollControlBar
import com.example.ui.components.EditNoteDialog
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@Composable
fun ReaderScreen(
    surahNumber: Int,
    targetAyah: Int? = null,
    viewModel: QuranViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val detailState by viewModel.surahDetailState.collectAsState()
    val userSettings by viewModel.userSettings.collectAsState()
    val isAutoScrolling by viewModel.isAutoScrolling.collectAsState()
    val autoScrollSpeed by viewModel.autoScrollSpeed.collectAsState()
    val bookmarks by viewModel.bookmarks.collectAsState()

    val arabicFontSize = userSettings?.arabicFontSize ?: 28f
    val showLatin = userSettings?.showLatin ?: true
    val showTranslation = userSettings?.showTranslation ?: true
    val selectedQariId = userSettings?.selectedQariId ?: "05"

    val listState = rememberLazyListState()

    // Note editing dialog state
    var editingNoteAyah by remember { mutableStateOf<AyatDto?>(null) }

    // Load surah on enter or when surahNumber changes
    LaunchedEffect(surahNumber) {
        viewModel.loadSurahDetail(surahNumber)
    }

    // Scroll to target ayah if requested
    LaunchedEffect(detailState, targetAyah) {
        if (detailState is UiState.Success && targetAyah != null && targetAyah > 0) {
            val idx = (targetAyah - 1).coerceAtLeast(0)
            listState.animateScrollToItem(idx + 1) // +1 for the header
        }
    }

    // Auto-scroll loop
    LaunchedEffect(isAutoScrolling, autoScrollSpeed) {
        if (isAutoScrolling) {
            val stepPixels = 12f * autoScrollSpeed
            while (isActive && isAutoScrolling) {
                listState.scrollBy(stepPixels)
                delay(80)
            }
        }
    }

    // Back handler cleans up auto-scroll and navigates back
    BackHandler {
        viewModel.stopAutoScroll()
        viewModel.navigateBack()
    }

    Scaffold(
        topBar = {
            val title = when (val s = detailState) {
                is UiState.Success -> "Surat ${s.data.namaLatin}"
                else -> "Membaca Al-Qur'an"
            }
            val subtitle = when (val s = detailState) {
                is UiState.Success -> "${s.data.arti} • ${s.data.jumlahAyat} ayat"
                else -> null
            }
            AppHeader(
                title = title,
                subtitle = subtitle,
                onBackClick = {
                    viewModel.stopAutoScroll()
                    viewModel.navigateBack()
                },
                actions = {
                    // Auto-scroll toggle action button
                    IconButton(
                        onClick = { viewModel.toggleAutoScroll() },
                        modifier = Modifier
                            .testTag("toggle_auto_scroll_action")
                            .minimumInteractiveComponentSize()
                    ) {
                        Icon(
                            imageVector = if (isAutoScrolling) Icons.Default.PauseCircle else Icons.Default.PlayCircleOutline,
                            contentDescription = "Auto Scroll",
                            tint = SoftGold
                        )
                    }

                    // Play full Surah audio
                    if (detailState is UiState.Success) {
                        val surah = (detailState as UiState.Success<SurahDetailDto>).data
                        IconButton(
                            onClick = { viewModel.playSurahAudio(surah, selectedQariId) },
                            modifier = Modifier
                                .testTag("play_surah_audio_action")
                                .minimumInteractiveComponentSize()
                        ) {
                            Icon(
                                imageVector = Icons.Default.VolumeUp,
                                contentDescription = "Putar Audio Surat",
                                tint = SoftGold
                            )
                        }
                    }
                }
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
            when (val state = detailState) {
                is UiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = Emerald)
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "Memuat ayat Al-Qur'an...",
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
                                onClick = { viewModel.loadSurahDetail(surahNumber) },
                                colors = ButtonDefaults.buttonColors(containerColor = Emerald)
                            ) {
                                Text("Coba Lagi", color = Color.White)
                            }
                        }
                    }
                }

                is UiState.Success -> {
                    val surah = state.data
                    val bookmarkedIds = remember(bookmarks) {
                        bookmarks.map { it.id }.toSet()
                    }

                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 120.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Header Bismillah (except Surah 9 At-Taubah, and Al-Fatihah where Bismillah is Ayah 1)
                        if (surah.nomor != 9) {
                            item {
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    color = Ivory,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, MetallicGold.copy(alpha = 0.5f))
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(18.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "بِسْمِ اللّٰهِ الرَّحْمٰنِ الرَّحِيْمِ",
                                            style = MaterialTheme.typography.headlineMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = ForestGreen
                                            ),
                                            textAlign = TextAlign.Center
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Dengan nama Allah Yang Maha Pengasih lagi Maha Penyayang",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = MutedTextLight
                                            ),
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }

                        // Ayat List
                        itemsIndexed(surah.ayat, key = { _, ayah -> ayah.nomorAyat }) { _, ayah ->
                            val isBookmarked = bookmarkedIds.contains("${surah.nomor}_${ayah.nomorAyat}")

                            AyahCard(
                                surah = surah,
                                ayah = ayah,
                                isBookmarked = isBookmarked,
                                arabicFontSize = arabicFontSize,
                                showLatin = showLatin,
                                showTranslation = showTranslation,
                                onPlayAudio = {
                                    viewModel.playAyahAudio(surah, ayah, selectedQariId)
                                },
                                onToggleBookmark = {
                                    viewModel.toggleBookmark(
                                        surahNumber = surah.nomor,
                                        surahName = surah.namaLatin,
                                        surahArabic = surah.nama,
                                        ayahNumber = ayah.nomorAyat,
                                        arabicText = ayah.teksArab,
                                        translation = ayah.teksIndonesia
                                    )
                                    // Update last read on bookmark
                                    viewModel.updateLastRead(
                                        surah.nomor, surah.namaLatin, surah.nama, ayah.nomorAyat, surah.jumlahAyat
                                    )
                                },
                                onOpenNote = {
                                    editingNoteAyah = ayah
                                },
                                onCopy = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val textToCopy = "${ayah.teksArab}\n\n${ayah.teksIndonesia}\n(QS. ${surah.namaLatin}: ${ayah.nomorAyat})"
                                    clipboard.setPrimaryClip(ClipData.newPlainText("Ayat Quran", textToCopy))
                                    Toast.makeText(context, "Ayat disalin ke clipboard", Toast.LENGTH_SHORT).show()
                                },
                                onShare = {
                                    val shareText = "${ayah.teksArab}\n\n\"${ayah.teksIndonesia}\"\n\n(QS. ${surah.namaLatin} [${surah.nomor}] : ${ayah.nomorAyat})\n\nDibagikan dari Muslim Quran"
                                    val intent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_TEXT, shareText)
                                    }
                                    context.startActivity(Intent.createChooser(intent, "Bagikan Ayat"))
                                },
                                onOpenTafsir = {
                                    viewModel.navigateTo(
                                        ScreenDestination.Tafsir(
                                            surahNumber = surah.nomor,
                                            targetAyah = ayah.nomorAyat
                                        )
                                    )
                                }
                            )
                        }

                        // Bottom Navigation Between Surahs
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                if (surah.nomor > 1) {
                                    OutlinedButton(
                                        onClick = {
                                            viewModel.navigateTo(ScreenDestination.Reader(surahNumber = surah.nomor - 1))
                                        },
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = DeepEmerald),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Surat Sebelumnya")
                                    }
                                } else {
                                    Spacer(modifier = Modifier.width(1.dp))
                                }

                                if (surah.nomor < 114) {
                                    Button(
                                        onClick = {
                                            viewModel.navigateTo(ScreenDestination.Reader(surahNumber = surah.nomor + 1))
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Emerald),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text("Surat Selanjutnya", color = Color.White)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Icon(imageVector = Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }

                UiState.Idle -> {}
            }

            // Floating Auto-Scroll Control Bar (When active)
            if (isAutoScrolling) {
                AutoScrollControlBar(
                    isScrolling = isAutoScrolling,
                    currentSpeed = autoScrollSpeed,
                    onToggle = { viewModel.toggleAutoScroll() },
                    onSpeedChange = { viewModel.setAutoScrollSpeed(it) },
                    onStop = { viewModel.stopAutoScroll() },
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        }
    }

    // Note editing dialog
    editingNoteAyah?.let { ayah ->
        val existingNote = bookmarks.find { it.id == "${surahNumber}_${ayah.nomorAyat}" }?.note ?: ""
        EditNoteDialog(
            initialNote = existingNote,
            surahName = (detailState as? UiState.Success)?.data?.namaLatin ?: "Surat",
            ayahNumber = ayah.nomorAyat,
            onDismiss = { editingNoteAyah = null },
            onSave = { newNote ->
                viewModel.updateBookmarkNote("${surahNumber}_${ayah.nomorAyat}", newNote)
                editingNoteAyah = null
                Toast.makeText(context, "Catatan berhasil disimpan", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@Composable
private fun AyahCard(
    surah: SurahDetailDto,
    ayah: AyatDto,
    isBookmarked: Boolean,
    arabicFontSize: Float,
    showLatin: Boolean,
    showTranslation: Boolean,
    onPlayAudio: () -> Unit,
    onToggleBookmark: () -> Unit,
    onOpenNote: () -> Unit,
    onCopy: () -> Unit,
    onShare: () -> Unit,
    onOpenTafsir: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("ayah_card_${ayah.nomorAyat}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Action Row: Ayah Number Badge + Action Icons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Ayah Badge
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(DeepEmerald),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = ayah.nomorAyat.toString(),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = SoftGold
                        )
                    )
                }

                // Action Buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onPlayAudio,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = "Putar Audio Ayat",
                            tint = Emerald,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = onToggleBookmark,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Bookmark Ayat",
                            tint = if (isBookmarked) MetallicGold else MutedTextLight,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = onOpenNote,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.EditNote,
                            contentDescription = "Catatan Ayat",
                            tint = Emerald,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    IconButton(
                        onClick = onOpenTafsir,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MenuBook,
                            contentDescription = "Tafsir Ayat",
                            tint = DeepEmerald,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = onCopy,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Salin Ayat",
                            tint = MutedTextLight,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = onShare,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Bagikan Ayat",
                            tint = MutedTextLight,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Arabic Text with complete harakat and RTL alignment
            Text(
                text = ayah.teksArab,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontSize = arabicFontSize.sp,
                    lineHeight = (arabicFontSize * 1.8f).sp,
                    fontWeight = FontWeight.Normal,
                    color = Charcoal
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                textAlign = TextAlign.End
            )

            // Transliteration Latin
            if (showLatin && ayah.teksLatin.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = ayah.teksLatin.trim(),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium,
                        color = Emerald,
                        lineHeight = 22.sp
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Indonesian Translation
            if (showTranslation && ayah.teksIndonesia.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = ayah.teksIndonesia,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MutedTextLight,
                        lineHeight = 22.sp
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
