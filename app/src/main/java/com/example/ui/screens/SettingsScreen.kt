package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.config.AppConfig
import com.example.ui.QuranViewModel
import com.example.ui.components.AppHeader
import com.example.ui.theme.*

@Composable
fun SettingsScreen(
    viewModel: QuranViewModel,
    modifier: Modifier = Modifier
) {
    val userSettings by viewModel.userSettings.collectAsState()

    val arabicFontSize = userSettings?.arabicFontSize ?: 28f
    val showLatin = userSettings?.showLatin ?: true
    val showTranslation = userSettings?.showTranslation ?: true
    val selectedQariId = userSettings?.selectedQariId ?: "05"
    val dailyTarget = userSettings?.dailyAyahTarget ?: 10
    val isDarkMode = userSettings?.isDarkMode ?: false

    Scaffold(
        topBar = {
            AppHeader(
                title = "Pengaturan",
                subtitle = "Kustomisasi tampilan & audio"
            )
        },
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 90.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Tampilan Teks Al-Qur'an (Font Size Preview)
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Ukuran Huruf Arab",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = DeepEmerald
                            )
                        )
                        Text(
                            text = "Atur ukuran teks agar nyaman saat membaca",
                            style = MaterialTheme.typography.bodySmall,
                            color = MutedTextLight
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Live Preview Box
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Ivory,
                            border = androidx.compose.foundation.BorderStroke(1.dp, SoftGold)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "بِسْمِ اللّٰهِ الرَّحْمٰنِ الرَّحِيْمِ",
                                    fontSize = arabicFontSize.sp,
                                    lineHeight = (arabicFontSize * 1.8f).sp,
                                    fontWeight = FontWeight.Normal,
                                    color = Charcoal,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Ukuran: ${arabicFontSize.toInt()} sp",
                                    style = MaterialTheme.typography.labelSmall.copy(color = DeepEmerald)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Slider(
                            value = arabicFontSize,
                            onValueChange = { newSize ->
                                viewModel.updateSettings { it.copy(arabicFontSize = newSize) }
                            },
                            valueRange = 20f..40f,
                            steps = 9,
                            colors = SliderDefaults.colors(
                                thumbColor = Emerald,
                                activeTrackColor = Emerald
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("arabic_font_slider")
                        )
                    }
                }
            }

            // 2. Preferensi Tampilan
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Preferensi Tampilan",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = DeepEmerald
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Transliterasi Latin Toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Transliterasi Latin",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                                )
                                Text(
                                    text = "Menampilkan bacaan latin di bawah ayat",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MutedTextLight
                                )
                            }
                            Switch(
                                checked = showLatin,
                                onCheckedChange = { checked ->
                                    viewModel.updateSettings { it.copy(showLatin = checked) }
                                },
                                colors = SwitchDefaults.colors(checkedThumbColor = Emerald, checkedTrackColor = Emerald.copy(alpha = 0.5f)),
                                modifier = Modifier.testTag("toggle_latin_switch")
                            )
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = BorderLight)

                        // Terjemahan Indonesia Toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Terjemahan Bahasa Indonesia",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                                )
                                Text(
                                    text = "Menampilkan arti ayat standar Kemenag RI",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MutedTextLight
                                )
                            }
                            Switch(
                                checked = showTranslation,
                                onCheckedChange = { checked ->
                                    viewModel.updateSettings { it.copy(showTranslation = checked) }
                                },
                                colors = SwitchDefaults.colors(checkedThumbColor = Emerald, checkedTrackColor = Emerald.copy(alpha = 0.5f)),
                                modifier = Modifier.testTag("toggle_translation_switch")
                            )
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = BorderLight)

                        // Dark Theme Toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Mode Gelap (Dark Mode)",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                                )
                                Text(
                                    text = "Warna forest green yang menenangkan mata saat malam",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MutedTextLight
                                )
                            }
                            Switch(
                                checked = isDarkMode,
                                onCheckedChange = { checked ->
                                    viewModel.updateSettings { it.copy(isDarkMode = checked) }
                                },
                                colors = SwitchDefaults.colors(checkedThumbColor = MetallicGold, checkedTrackColor = DeepEmerald),
                                modifier = Modifier.testTag("toggle_dark_mode_switch")
                            )
                        }
                    }
                }
            }

            // 3. Pilihan Qari Murottal
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Pilihan Qari Murottal",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = DeepEmerald
                            )
                        )
                        Text(
                            text = "Pilih qari favorit untuk lantunan audio Al-Qur'an",
                            style = MaterialTheme.typography.bodySmall,
                            color = MutedTextLight
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        AppConfig.QARI_LIST.forEach { qari ->
                            val isSelected = selectedQariId == qari.id
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) DeepEmerald.copy(alpha = 0.1f) else Color.Transparent)
                                    .clickable {
                                        viewModel.updateSettings { it.copy(selectedQariId = qari.id) }
                                    }
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { viewModel.updateSettings { it.copy(selectedQariId = qari.id) } },
                                    colors = RadioButtonDefaults.colors(selectedColor = Emerald)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = qari.name,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = Charcoal
                                        )
                                    )
                                    Text(
                                        text = qari.description,
                                        style = MaterialTheme.typography.labelSmall.copy(color = MutedTextLight)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 4. Target Tilawah Harian
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Target Tilawah Harian",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = DeepEmerald
                            )
                        )
                        Text(
                            text = "Pilih target jumlah ayat yang ingin dibaca setiap hari",
                            style = MaterialTheme.typography.bodySmall,
                            color = MutedTextLight
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(5, 10, 20, 50, 100).forEach { targetOption ->
                                val isSelected = dailyTarget == targetOption
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isSelected) DeepEmerald else BorderLight)
                                        .clickable {
                                            viewModel.updateSettings { it.copy(dailyAyahTarget = targetOption) }
                                        }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "$targetOption",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) SoftGold else Charcoal
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 5. Informasi Aplikasi & Branding
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = AppConfig.APP_NAME,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = DeepEmerald
                            )
                        )
                        Text(
                            text = AppConfig.TAGLINE,
                            style = MaterialTheme.typography.bodySmall,
                            color = MutedTextLight
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Versi 1.0 • Sumber Data: equran.id API v2\nKementerian Agama Republik Indonesia",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MutedTextLight,
                                textAlign = TextAlign.Center
                            )
                        )
                    }
                }
            }
        }
    }
}
