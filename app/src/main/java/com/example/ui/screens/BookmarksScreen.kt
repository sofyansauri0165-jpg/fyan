package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.local.BookmarkEntity
import com.example.data.local.ReadingHistoryEntity
import com.example.ui.QuranViewModel
import com.example.ui.ScreenDestination
import com.example.ui.components.AppHeader
import com.example.ui.components.EditNoteDialog
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun BookmarksScreen(
    viewModel: QuranViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val bookmarks by viewModel.bookmarks.collectAsState()
    val history by viewModel.readingHistory.collectAsState()

    var selectedTabIndex by remember { mutableStateOf(0) }
    var editingBookmark by remember { mutableStateOf<BookmarkEntity?>(null) }

    Scaffold(
        topBar = {
            AppHeader(
                title = "Bookmark & Riwayat",
                subtitle = "Catatan tadabbur dan riwayat tilawah"
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
            // Tab Row
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = DeepEmerald
            ) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = { Text("Bookmark (${bookmarks.size})") },
                    icon = { Icon(Icons.Default.Bookmark, contentDescription = null) }
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = { Text("Riwayat (${history.size})") },
                    icon = { Icon(Icons.Default.History, contentDescription = null) }
                )
            }

            when (selectedTabIndex) {
                0 -> {
                    // Bookmarks List
                    if (bookmarks.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.BookmarkBorder,
                                    contentDescription = null,
                                    tint = MutedTextLight,
                                    modifier = Modifier.size(56.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Belum ada bookmark tersimpan",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Charcoal
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Tandai ayat favorit dan tambahkan catatan saat membaca Al-Qur'an.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MutedTextLight
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 90.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(bookmarks, key = { it.id }) { item ->
                                BookmarkCard(
                                    bookmark = item,
                                    onClick = {
                                        viewModel.navigateTo(
                                            ScreenDestination.Reader(
                                                surahNumber = item.surahNumber,
                                                targetAyah = item.ayahNumber
                                            )
                                        )
                                    },
                                    onEditNote = { editingBookmark = item },
                                    onDelete = {
                                        viewModel.deleteBookmark(item.surahNumber, item.ayahNumber)
                                        Toast.makeText(context, "Bookmark dihapus", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                        }
                    }
                }

                1 -> {
                    // History List
                    if (history.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.History,
                                    contentDescription = null,
                                    tint = MutedTextLight,
                                    modifier = Modifier.size(56.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Belum ada riwayat bacaan",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Charcoal
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 90.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(history, key = { it.surahNumber }) { item ->
                                HistoryCard(
                                    history = item,
                                    onClick = {
                                        viewModel.navigateTo(
                                            ScreenDestination.Reader(
                                                surahNumber = item.surahNumber,
                                                targetAyah = item.lastAyah
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
    }

    // Edit Note Dialog
    editingBookmark?.let { b ->
        EditNoteDialog(
            initialNote = b.note,
            surahName = b.surahName,
            ayahNumber = b.ayahNumber,
            onDismiss = { editingBookmark = null },
            onSave = { newNote ->
                viewModel.updateBookmarkNote(b.id, newNote)
                editingBookmark = null
                Toast.makeText(context, "Catatan berhasil diperbarui", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@Composable
private fun BookmarkCard(
    bookmark: BookmarkEntity,
    onClick: () -> Unit,
    onEditNote: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("bookmark_card_${bookmark.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(DeepEmerald),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = bookmark.ayahNumber.toString(),
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = SoftGold
                            )
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = "QS. ${bookmark.surahName} : Ayat ${bookmark.ayahNumber}",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = DeepEmerald
                            )
                        )
                        Text(
                            text = formatTimestamp(bookmark.createdAt),
                            style = MaterialTheme.typography.labelSmall.copy(color = MutedTextLight)
                        )
                    }
                }

                Row {
                    IconButton(onClick = onEditNote, modifier = Modifier.size(36.dp)) {
                        Icon(
                            imageVector = Icons.Default.EditNote,
                            contentDescription = "Edit Catatan",
                            tint = Emerald,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Hapus Bookmark",
                            tint = ErrorRed,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Arabic text snippet
            Text(
                text = bookmark.arabicText,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Normal,
                    color = Charcoal
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Translation snippet
            Text(
                text = bookmark.translation,
                style = MaterialTheme.typography.bodySmall.copy(color = MutedTextLight),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // Personal note if exists
            if (bookmark.note.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Ivory,
                    border = androidx.compose.foundation.BorderStroke(1.dp, SoftGold)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.StickyNote2,
                            contentDescription = null,
                            tint = MetallicGold,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = bookmark.note,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Charcoal,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryCard(
    history: ReadingHistoryEntity,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("history_card_${history.surahNumber}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(DeepEmerald.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = history.surahNumber.toString(),
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = DeepEmerald
                    )
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = history.surahName,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Charcoal
                    )
                )
                Text(
                    text = "Terakhir dibaca: Ayat ${history.lastAyah} dari ${history.totalAyah}",
                    style = MaterialTheme.typography.bodySmall.copy(color = MutedTextLight)
                )
            }

            Text(
                text = history.surahArabic,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = Emerald
                )
            )
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))
    return sdf.format(Date(timestamp))
}
