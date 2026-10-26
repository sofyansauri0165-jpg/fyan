package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.config.AppConfig
import com.example.ui.QuranViewModel
import com.example.ui.ScreenDestination
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    viewModel: QuranViewModel,
    modifier: Modifier = Modifier
) {
    val lastRead by viewModel.lastRead.collectAsState()
    val userSettings by viewModel.userSettings.collectAsState()
    val todayDate = remember {
        val sdf = SimpleDateFormat("EEEE, d MMMM yyyy", Locale("id", "ID"))
        sdf.format(Date())
    }

    val dailyTarget = userSettings?.dailyAyahTarget ?: 10
    val readToday = userSettings?.ayahsReadToday ?: 0
    val targetProgress = if (dailyTarget > 0) (readToday.toFloat() / dailyTarget.toFloat()).coerceIn(0f, 1f) else 0f

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        // 1. Top Brand Header
        item {
            Surface(
                color = DeepEmerald,
                contentColor = Color.White
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Assalamu'alaikum",
                                style = MaterialTheme.typography.bodyMedium.copy(color = SoftGold)
                            )
                            Text(
                                text = AppConfig.APP_NAME,
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                            Text(
                                text = AppConfig.TAGLINE,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = WarmWhite.copy(alpha = 0.85f)
                                )
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(MetallicGold.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_quran_hero),
                                contentDescription = "Logo Muslim Quran",
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(ForestGreen.copy(alpha = 0.6f))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = SoftGold,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = todayDate,
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = WarmWhite.copy(alpha = 0.9f)
                            )
                        )
                    }
                }
            }
        }

        // 2. Last Read Card (Lanjut Membaca)
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("home_last_read_card"),
                    shape = RoundedCornerShape(18.dp),
                    color = ForestGreen,
                    contentColor = Color.White,
                    shadowElevation = 4.dp
                ) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.MenuBook,
                                        contentDescription = null,
                                        tint = MetallicGold,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Terakhir Dibaca",
                                        style = MaterialTheme.typography.labelLarge.copy(
                                            color = SoftGold,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    )
                                }

                                if (lastRead != null) {
                                    Text(
                                        text = "Surat #${lastRead!!.surahNumber}",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = WarmWhite.copy(alpha = 0.7f)
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            if (lastRead != null) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Bottom
                                ) {
                                    Column {
                                        Text(
                                            text = lastRead!!.surahName,
                                            style = MaterialTheme.typography.titleLarge.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        )
                                        Text(
                                            text = "Ayat ${lastRead!!.ayahNumber} dari ${lastRead!!.totalAyah} ayat",
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                color = WarmWhite.copy(alpha = 0.8f)
                                            )
                                        )
                                    }

                                    Text(
                                        text = lastRead!!.surahArabic,
                                        style = MaterialTheme.typography.headlineSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = MetallicGold
                                        )
                                    )
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                Button(
                                    onClick = {
                                        viewModel.navigateTo(
                                            ScreenDestination.Reader(
                                                surahNumber = lastRead!!.surahNumber,
                                                targetAyah = lastRead!!.ayahNumber
                                            )
                                        )
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("continue_reading_button"),
                                    colors = ButtonDefaults.buttonColors(containerColor = Emerald),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Lanjutkan Membaca",
                                        color = Color.White,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            } else {
                                Text(
                                    text = "Belum ada riwayat bacaan. Mulai tilawah sekarang!",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = WarmWhite.copy(alpha = 0.8f)
                                    )
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = { viewModel.navigateTo(ScreenDestination.Reader(surahNumber = 1)) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Emerald),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Buka Al-Fatihah", color = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. Tilawah Progress Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Target Tilawah Harian",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = DeepEmerald
                                )
                            )
                            Text(
                                text = "Konsisten membaca setiap hari",
                                style = MaterialTheme.typography.bodySmall,
                                color = MutedTextLight
                            )
                        }

                        Text(
                            text = "$readToday / $dailyTarget Ayat",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Emerald
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    LinearProgressIndicator(
                        progress = { targetProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp)),
                        color = MetallicGold,
                        trackColor = BorderLight
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (readToday >= dailyTarget) "Alhamdulillah, target tercapai! 🌟" else "Tersisa ${dailyTarget - readToday} ayat lagi",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = if (readToday >= dailyTarget) Emerald else MutedTextLight,
                                fontWeight = if (readToday >= dailyTarget) FontWeight.Bold else FontWeight.Normal
                            )
                        )

                        TextButton(
                            onClick = { viewModel.navigateTo(ScreenDestination.Settings) }
                        ) {
                            Text("Ubah Target", color = DeepEmerald, style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
        }

        // 4. Quick Actions Grid
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
                Text(
                    text = "Akses Cepat",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = DeepEmerald
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    QuickActionTile(
                        icon = Icons.Default.FormatListNumbered,
                        title = "114 Surat",
                        subtitle = "Daftar Lengkap",
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.navigateTo(ScreenDestination.SurahList) }
                    )

                    QuickActionTile(
                        icon = Icons.Default.Search,
                        title = "Cari Ayat",
                        subtitle = "Global Search",
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.navigateTo(ScreenDestination.Search) }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    QuickActionTile(
                        icon = Icons.Default.Bookmark,
                        title = "Bookmark",
                        subtitle = "Catatan & Simpanan",
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.navigateTo(ScreenDestination.Bookmarks) }
                    )

                    QuickActionTile(
                        icon = Icons.Default.Settings,
                        title = "Pengaturan",
                        subtitle = "Font & Audio Qari",
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.navigateTo(ScreenDestination.Settings) }
                    )
                }
            }
        }

        // 5. Surat Populer & Pilihan
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Text(
                    text = "Surat Populer",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = DeepEmerald
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                val popularSurahs = listOf(
                    PopularSurahItem(1, "Al-Fatihah", "الفاتحة", "Pembukaan", 7),
                    PopularSurahItem(18, "Al-Kahf", "الكهف", "Gua", 110),
                    PopularSurahItem(36, "Yasin", "يس", "Yasin", 83),
                    PopularSurahItem(55, "Ar-Rahman", "الرحمن", "Maha Pengasih", 78),
                    PopularSurahItem(56, "Al-Waqi'ah", "الواقعة", "Hari Kiamat", 96),
                    PopularSurahItem(67, "Al-Mulk", "الملك", "Kerajaan", 30)
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp)
                ) {
                    items(popularSurahs) { item ->
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surface,
                            tonalElevation = 2.dp,
                            shadowElevation = 2.dp,
                            modifier = Modifier
                                .width(140.dp)
                                .clickable {
                                    viewModel.navigateTo(ScreenDestination.Reader(surahNumber = item.number))
                                }
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(DeepEmerald.copy(alpha = 0.12f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = item.number.toString(),
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = DeepEmerald
                                            )
                                        )
                                    }

                                    Text(
                                        text = item.arabic,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = MetallicGold
                                        )
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Text(
                                    text = item.latin,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = Charcoal
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Text(
                                    text = "${item.ayahCount} ayat",
                                    style = MaterialTheme.typography.labelSmall.copy(color = MutedTextLight)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private data class PopularSurahItem(
    val number: Int,
    val latin: String,
    val arabic: String,
    val meaning: String,
    val ayahCount: Int
)

@Composable
private fun QuickActionTile(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(DeepEmerald.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = DeepEmerald,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Charcoal
                    )
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall.copy(color = MutedTextLight)
                )
            }
        }
    }
}
