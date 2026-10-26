package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bookmarks")
data class BookmarkEntity(
    @PrimaryKey val id: String, // format: "surah_ayah", e.g. "1_1"
    val surahNumber: Int,
    val surahName: String,
    val surahArabic: String,
    val ayahNumber: Int,
    val arabicText: String,
    val translation: String,
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "last_read")
data class LastReadEntity(
    @PrimaryKey val id: Int = 1,
    val surahNumber: Int,
    val surahName: String,
    val surahArabic: String,
    val ayahNumber: Int,
    val totalAyah: Int,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_settings")
data class UserSettingsEntity(
    @PrimaryKey val id: Int = 1,
    val arabicFontSize: Float = 28f,
    val showLatin: Boolean = true,
    val showTranslation: Boolean = true,
    val selectedQariId: String = "05",
    val dailyAyahTarget: Int = 10,
    val ayahsReadToday: Int = 0,
    val lastActiveDate: String = "",
    val isDarkMode: Boolean = false
)

@Entity(tableName = "reading_history")
data class ReadingHistoryEntity(
    @PrimaryKey val surahNumber: Int,
    val surahName: String,
    val surahArabic: String,
    val lastAyah: Int,
    val totalAyah: Int,
    val timestamp: Long = System.currentTimeMillis()
)
