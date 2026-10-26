package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.AudioPlaybackState
import com.example.config.AppConfig
import com.example.ui.theme.*

@Composable
fun AppHeader(
    title: String,
    subtitle: String? = null,
    onBackClick: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    Surface(
        color = DeepEmerald,
        contentColor = Color.White,
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (onBackClick != null) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier
                            .testTag("header_back_button")
                            .minimumInteractiveComponentSize()
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Kembali",
                            tint = SoftGold
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (!subtitle.isNullOrEmpty()) {
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = SoftGold
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    content = actions
                )
            }
        }
    }
}

@Composable
fun MiniAudioPlayer(
    state: AudioPlaybackState,
    onPlayPause: () -> Unit,
    onSeek: (Float) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!state.isPlaying && !state.isLoading && state.currentPositionMs == 0) return

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        color = DeepEmerald,
        contentColor = Color.White,
        tonalElevation = 8.dp,
        shadowElevation = 8.dp
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MetallicGold.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.VolumeUp,
                        contentDescription = "Audio Quran",
                        tint = SoftGold,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = state.title.ifEmpty { "Audio Murottal" },
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = if (state.isLoading) "Memuat audio..." else state.subtitle.ifEmpty { "Qari" },
                        style = MaterialTheme.typography.bodySmall.copy(color = SoftGold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(28.dp),
                        color = SoftGold,
                        strokeWidth = 2.dp
                    )
                } else {
                    IconButton(
                        onClick = onPlayPause,
                        modifier = Modifier
                            .testTag("audio_play_pause_button")
                            .minimumInteractiveComponentSize()
                    ) {
                        Icon(
                            imageVector = if (state.isPlaying) Icons.Default.PauseCircle else Icons.Default.PlayCircle,
                            contentDescription = if (state.isPlaying) "Jeda" else "Putar",
                            tint = MetallicGold,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                IconButton(
                    onClick = onClose,
                    modifier = Modifier
                        .testTag("audio_close_button")
                        .minimumInteractiveComponentSize()
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Tutup Pemutar",
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Slider progress bar
            Slider(
                value = state.progress,
                onValueChange = onSeek,
                colors = SliderDefaults.colors(
                    thumbColor = MetallicGold,
                    activeTrackColor = MetallicGold,
                    inactiveTrackColor = Color.White.copy(alpha = 0.2f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp)
            )

            // Current time and duration
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = formatMs(state.currentPositionMs),
                    style = MaterialTheme.typography.labelSmall.copy(color = Color.White.copy(alpha = 0.7f))
                )
                Text(
                    text = formatMs(state.durationMs),
                    style = MaterialTheme.typography.labelSmall.copy(color = Color.White.copy(alpha = 0.7f))
                )
            }
        }
    }
}

private fun formatMs(ms: Int): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}

@Composable
fun AutoScrollControlBar(
    isScrolling: Boolean,
    currentSpeed: Float,
    onToggle: () -> Unit,
    onSpeedChange: (Float) -> Unit,
    onStop: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(20.dp),
        color = ForestGreen,
        contentColor = Color.White,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onToggle,
                    modifier = Modifier
                        .testTag("auto_scroll_toggle_button")
                        .size(40.dp)
                ) {
                    Icon(
                        imageVector = if (isScrolling) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isScrolling) "Jeda Auto-Scroll" else "Mulai Auto-Scroll",
                        tint = SoftGold
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                Text(
                    text = if (isScrolling) "Auto-Scroll Aktif" else "Auto-Scroll Jeda",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                )
            }

            // Speed options: 1x, 1.5x, 2x
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf(1.0f, 1.5f, 2.0f).forEach { speed ->
                    val isSelected = (currentSpeed == speed)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) MetallicGold else Color.White.copy(alpha = 0.15f))
                            .clickable { onSpeedChange(speed) }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${speed}x",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) ForestGreen else Color.White
                            )
                        )
                    }
                }

                IconButton(
                    onClick = onStop,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Hentikan Auto-Scroll",
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun EditNoteDialog(
    initialNote: String,
    surahName: String,
    ayahNumber: Int,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var noteText by remember { mutableStateOf(initialNote) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Catatan $surahName : Ayat $ayahNumber",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column {
                Text(
                    text = "Tulis refleksi, tadabbur, atau catatan pribadi untuk ayat ini:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MutedTextLight
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .testTag("note_input_field"),
                    placeholder = { Text("Tulis catatan Anda...") },
                    maxLines = 5
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(noteText) },
                colors = ButtonDefaults.buttonColors(containerColor = Emerald),
                modifier = Modifier.testTag("save_note_button")
            ) {
                Text("Simpan", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}
