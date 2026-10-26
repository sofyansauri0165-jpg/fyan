package com.example.data.repository

import com.example.data.api.ApiClient
import com.example.data.local.*
import com.example.data.model.AyatDto
import com.example.data.model.SurahDetailDto
import com.example.data.model.SurahItemDto
import com.example.data.model.TafsirDetailDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class SearchResultItem(
    val surahNumber: Int,
    val surahName: String,
    val surahArabic: String,
    val ayahNumber: Int,
    val arabicText: String,
    val translation: String,
    val matchSnippet: String
)

class QuranRepository(private val database: QuranDatabase) {
    private val api = ApiClient.apiService

    // In-memory cache for surahs
    private var cachedSurahs: List<SurahItemDto>? = null
    // Cache for detail verses by surah number
    private val surahDetailCache = mutableMapOf<Int, SurahDetailDto>()

    suspend fun getSurahs(forceRefresh: Boolean = false): Result<List<SurahItemDto>> = withContext(Dispatchers.IO) {
        if (!forceRefresh && !cachedSurahs.isNullOrEmpty()) {
            return@withContext Result.success(cachedSurahs!!)
        }
        try {
            val response = api.getSurahs()
            if (response.isSuccessful && response.body() != null) {
                val list = response.body()!!.data
                cachedSurahs = list
                Result.success(list)
            } else {
                Result.failure(Exception("Gagal memuat daftar surat (Kode: ${response.code()})"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getSurahDetail(nomor: Int): Result<SurahDetailDto> = withContext(Dispatchers.IO) {
        surahDetailCache[nomor]?.let { return@withContext Result.success(it) }
        try {
            val response = api.getSurahDetail(nomor)
            if (response.isSuccessful && response.body() != null) {
                val detail = response.body()!!.data
                surahDetailCache[nomor] = detail
                Result.success(detail)
            } else {
                Result.failure(Exception("Gagal memuat surat #$nomor (Kode: ${response.code()})"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getTafsir(nomor: Int): Result<TafsirDetailDto> = withContext(Dispatchers.IO) {
        try {
            val response = api.getTafsir(nomor)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.data)
            } else {
                Result.failure(Exception("Gagal memuat tafsir surat #$nomor (Kode: ${response.code()})"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Global Search across loaded surahs and cached verses
    suspend fun searchQuran(query: String): List<SearchResultItem> = withContext(Dispatchers.IO) {
        val q = query.trim().lowercase(Locale.ROOT)
        if (q.isEmpty()) return@withContext emptyList()

        val results = mutableListOf<SearchResultItem>()

        // 1. Check if user typed a surah number or "surat X" or name
        cachedSurahs?.forEach { surah ->
            if (surah.nomor.toString() == q ||
                surah.namaLatin.lowercase(Locale.ROOT).contains(q) ||
                surah.arti.lowercase(Locale.ROOT).contains(q)
            ) {
                results.add(
                    SearchResultItem(
                        surahNumber = surah.nomor,
                        surahName = surah.namaLatin,
                        surahArabic = surah.nama,
                        ayahNumber = 1,
                        arabicText = surah.nama,
                        translation = "${surah.arti} (${surah.jumlahAyat} ayat - ${surah.tempatTurun})",
                        matchSnippet = "Surat ke-${surah.nomor}: ${surah.namaLatin} - ${surah.arti}"
                    )
                )
            }
        }

        // 2. Search within loaded surah details verses
        for ((surahNum, detail) in surahDetailCache) {
            for (ayah in detail.ayat) {
                val trans = ayah.teksIndonesia.lowercase(Locale.ROOT)
                val latin = ayah.teksLatin.lowercase(Locale.ROOT)
                if (trans.contains(q) || latin.contains(q) || ayah.nomorAyat.toString() == q) {
                    val snippet = if (trans.contains(q)) {
                        val idx = trans.indexOf(q)
                        val start = (idx - 25).coerceAtLeast(0)
                        val end = (idx + q.length + 35).coerceAtMost(ayah.teksIndonesia.length)
                        "..." + ayah.teksIndonesia.substring(start, end) + "..."
                    } else {
                        ayah.teksIndonesia
                    }
                    results.add(
                        SearchResultItem(
                            surahNumber = surahNum,
                            surahName = detail.namaLatin,
                            surahArabic = detail.nama,
                            ayahNumber = ayah.nomorAyat,
                            arabicText = ayah.teksArab,
                            translation = ayah.teksIndonesia,
                            matchSnippet = snippet
                        )
                    )
                }
            }
        }

        results
    }

    // Local DB Operations
    fun getBookmarksFlow(): Flow<List<BookmarkEntity>> = database.bookmarkDao().getAllBookmarks()

    suspend fun isBookmarked(surahNumber: Int, ayahNumber: Int): Boolean {
        val id = "${surahNumber}_${ayahNumber}"
        return database.bookmarkDao().getBookmarkById(id) != null
    }

    suspend fun addBookmark(
        surahNumber: Int,
        surahName: String,
        surahArabic: String,
        ayahNumber: Int,
        arabicText: String,
        translation: String,
        note: String = ""
    ) {
        val id = "${surahNumber}_${ayahNumber}"
        database.bookmarkDao().insertBookmark(
            BookmarkEntity(
                id = id,
                surahNumber = surahNumber,
                surahName = surahName,
                surahArabic = surahArabic,
                ayahNumber = ayahNumber,
                arabicText = arabicText,
                translation = translation,
                note = note
            )
        )
    }

    suspend fun removeBookmark(surahNumber: Int, ayahNumber: Int) {
        val id = "${surahNumber}_${ayahNumber}"
        database.bookmarkDao().deleteBookmarkById(id)
    }

    suspend fun updateBookmarkNote(id: String, note: String) {
        database.bookmarkDao().updateNote(id, note)
    }

    fun getLastReadFlow(): Flow<LastReadEntity?> = database.lastReadDao().getLastReadFlow()

    suspend fun saveLastRead(
        surahNumber: Int,
        surahName: String,
        surahArabic: String,
        ayahNumber: Int,
        totalAyah: Int
    ) {
        database.lastReadDao().setLastRead(
            LastReadEntity(
                surahNumber = surahNumber,
                surahName = surahName,
                surahArabic = surahArabic,
                ayahNumber = ayahNumber,
                totalAyah = totalAyah
            )
        )
        database.readingHistoryDao().recordHistory(
            ReadingHistoryEntity(
                surahNumber = surahNumber,
                surahName = surahName,
                surahArabic = surahArabic,
                lastAyah = ayahNumber,
                totalAyah = totalAyah
            )
        )
    }

    fun getReadingHistory(): Flow<List<ReadingHistoryEntity>> = database.readingHistoryDao().getRecentHistory()

    fun getUserSettingsFlow(): Flow<UserSettingsEntity?> = database.userSettingsDao().getSettingsFlow()

    suspend fun getUserSettings(): UserSettingsEntity {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val settings = database.userSettingsDao().getSettings()
        return if (settings == null) {
            val initial = UserSettingsEntity(lastActiveDate = today)
            database.userSettingsDao().saveSettings(initial)
            initial
        } else {
            // Reset daily progress if date changed
            if (settings.lastActiveDate != today) {
                val updated = settings.copy(ayahsReadToday = 0, lastActiveDate = today)
                database.userSettingsDao().saveSettings(updated)
                updated
            } else {
                settings
            }
        }
    }

    suspend fun updateSettings(settings: UserSettingsEntity) {
        database.userSettingsDao().saveSettings(settings)
    }

    suspend fun incrementAyahRead(count: Int = 1) {
        val current = getUserSettings()
        val updated = current.copy(ayahsReadToday = current.ayahsReadToday + count)
        database.userSettingsDao().saveSettings(updated)
    }
}
