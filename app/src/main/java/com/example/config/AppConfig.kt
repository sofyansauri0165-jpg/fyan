package com.example.config

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.*

/**
 * Muslim Quran Central Configuration - Single source of truth for
 * brand identity, theme tokens, and API endpoints.
 */
object AppConfig {
    const val APP_NAME = "Muslim Quran"
    const val TAGLINE = "Your Daily Connection with the Quran"
    const val API_BASE_URL = "https://equran.id/api/v2/"

    val deepEmerald = DeepEmerald
    val forestGreen = ForestGreen
    val emerald = Emerald
    val metallicGold = MetallicGold
    val softGold = SoftGold
    val ivory = Ivory
    val warmWhite = WarmWhite
    val charcoal = Charcoal

    // Qari options available in equran.id API v2
    val QARI_LIST = listOf(
        QariInfo("05", "Misyari Rasyid Al-Afasi", "Riyadh, Arab Saudi"),
        QariInfo("01", "Abdullah Al-Juhany", "Imam Masjidil Haram"),
        QariInfo("02", "Abdul Muhsin Al-Qasim", "Imam Masjid Nabawi"),
        QariInfo("03", "Abdurrahman as-Sudais", "Imam Besar Masjidil Haram"),
        QariInfo("04", "Ibrahim Al-Dossari", "Qari Internasional"),
        QariInfo("06", "Yasser Al-Dosari", "Imam Masjidil Haram")
    )
}

data class QariInfo(
    val id: String,
    val name: String,
    val description: String
)
