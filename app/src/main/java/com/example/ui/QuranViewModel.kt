package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.AudioPlaybackState
import com.example.audio.QuranAudioPlayer
import com.example.data.local.*
import com.example.data.model.AyatDto
import com.example.data.model.SurahDetailDto
import com.example.data.model.SurahItemDto
import com.example.data.model.TafsirDetailDto
import com.example.data.repository.QuranRepository
import com.example.data.repository.SearchResultItem
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

sealed interface ScreenDestination {
    object Home : ScreenDestination
    object SurahList : ScreenDestination
    object Search : ScreenDestination
    object Bookmarks : ScreenDestination
    object Settings : ScreenDestination
    data class Reader(val surahNumber: Int, val targetAyah: Int? = null) : ScreenDestination
    data class Tafsir(val surahNumber: Int, val targetAyah: Int? = null) : ScreenDestination
}

sealed interface UiState<out T> {
    object Idle : UiState<Nothing>
    object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

class QuranViewModel(application: Application) : AndroidViewModel(application) {
    private val database = QuranDatabase.getDatabase(application)
    val repository = QuranRepository(database)
    val audioPlayer = QuranAudioPlayer(application)

    // Navigation state
    private val _navigationStack = MutableStateFlow<List<ScreenDestination>>(listOf(ScreenDestination.Home))
    val currentScreen: StateFlow<ScreenDestination> = _navigationStack.map { it.lastOrNull() ?: ScreenDestination.Home }
        .stateIn(viewModelScope, SharingStarted.Eagerly, ScreenDestination.Home)

    fun navigateTo(destination: ScreenDestination) {
        val current = _navigationStack.value.toMutableList()
        // If navigating to a main tab, replace root
        when (destination) {
            is ScreenDestination.Home,
            is ScreenDestination.SurahList,
            is ScreenDestination.Search,
            is ScreenDestination.Bookmarks,
            is ScreenDestination.Settings -> {
                _navigationStack.value = listOf(destination)
            }
            else -> {
                current.add(destination)
                _navigationStack.value = current
            }
        }
    }

    fun navigateBack(): Boolean {
        val current = _navigationStack.value
        return if (current.size > 1) {
            _navigationStack.value = current.dropLast(1)
            true
        } else {
            false
        }
    }

    // Surah List state
    private val _surahListState = MutableStateFlow<UiState<List<SurahItemDto>>>(UiState.Loading)
    val surahListState: StateFlow<UiState<List<SurahItemDto>>> = _surahListState.asStateFlow()

    // Surah Detail state
    private val _surahDetailState = MutableStateFlow<UiState<SurahDetailDto>>(UiState.Idle)
    val surahDetailState: StateFlow<UiState<SurahDetailDto>> = _surahDetailState.asStateFlow()

    // Tafsir state
    private val _tafsirState = MutableStateFlow<UiState<TafsirDetailDto>>(UiState.Idle)
    val tafsirState: StateFlow<UiState<TafsirDetailDto>> = _tafsirState.asStateFlow()

    // Search state
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<SearchResultItem>>(emptyList())
    val searchResults: StateFlow<List<SearchResultItem>> = _searchResults.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    // Auto-scroll state
    private val _isAutoScrolling = MutableStateFlow(false)
    val isAutoScrolling: StateFlow<Boolean> = _isAutoScrolling.asStateFlow()

    private val _autoScrollSpeed = MutableStateFlow(1.0f) // 1.0f, 1.5f, 2.0f, 3.0f
    val autoScrollSpeed: StateFlow<Float> = _autoScrollSpeed.asStateFlow()

    // Audio Playback
    val audioState: StateFlow<AudioPlaybackState> = audioPlayer.playbackState

    // Persistent Room data flows
    val bookmarks: StateFlow<List<BookmarkEntity>> = repository.getBookmarksFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val lastRead: StateFlow<LastReadEntity?> = repository.getLastReadFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val readingHistory: StateFlow<List<ReadingHistoryEntity>> = repository.getReadingHistory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userSettings: StateFlow<UserSettingsEntity?> = repository.getUserSettingsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    init {
        loadSurahs()
        // Pre-initialize settings
        viewModelScope.launch {
            repository.getUserSettings()
        }
    }

    fun loadSurahs(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _surahListState.value = UiState.Loading
            repository.getSurahs(forceRefresh)
                .onSuccess { list ->
                    _surahListState.value = UiState.Success(list)
                }
                .onFailure { error ->
                    _surahListState.value = UiState.Error(error.message ?: "Gagal memuat daftar surat")
                }
        }
    }

    fun loadSurahDetail(nomor: Int) {
        viewModelScope.launch {
            _surahDetailState.value = UiState.Loading
            repository.getSurahDetail(nomor)
                .onSuccess { detail ->
                    _surahDetailState.value = UiState.Success(detail)
                    // Record last read
                    repository.saveLastRead(
                        surahNumber = detail.nomor,
                        surahName = detail.namaLatin,
                        surahArabic = detail.nama,
                        ayahNumber = 1,
                        totalAyah = detail.jumlahAyat
                    )
                }
                .onFailure { error ->
                    _surahDetailState.value = UiState.Error(error.message ?: "Gagal memuat detail surat")
                }
        }
    }

    fun loadTafsir(nomor: Int) {
        viewModelScope.launch {
            _tafsirState.value = UiState.Loading
            repository.getTafsir(nomor)
                .onSuccess { tafsir ->
                    _tafsirState.value = UiState.Success(tafsir)
                }
                .onFailure { error ->
                    _tafsirState.value = UiState.Error(error.message ?: "Gagal memuat tafsir surat")
                }
        }
    }

    // Auto-scroll controls
    fun toggleAutoScroll() {
        _isAutoScrolling.value = !_isAutoScrolling.value
    }

    fun pauseAutoScroll() {
        _isAutoScrolling.value = false
    }

    fun resumeAutoScroll() {
        _isAutoScrolling.value = true
    }

    fun stopAutoScroll() {
        _isAutoScrolling.value = false
    }

    fun setAutoScrollSpeed(speed: Float) {
        _autoScrollSpeed.value = speed
    }

    // Search query update
    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        if (query.trim().isEmpty()) {
            _searchResults.value = emptyList()
            _isSearching.value = false
            return
        }
        viewModelScope.launch {
            _isSearching.value = true
            val results = repository.searchQuran(query)
            _searchResults.value = results
            _isSearching.value = false
        }
    }

    // Audio controls
    fun playSurahAudio(surah: SurahDetailDto, qariId: String = "05") {
        val audioMap = surah.audioFull
        val url = audioMap?.get(qariId) ?: audioMap?.values?.firstOrNull()
        if (url != null) {
            val qariName = com.example.config.AppConfig.QARI_LIST.find { it.id == qariId }?.name ?: "Qari"
            audioPlayer.playAudio(
                url = url,
                title = "Surat ${surah.namaLatin}",
                subtitle = qariName,
                surahNumber = surah.nomor
            )
        }
    }

    fun playAyahAudio(surah: SurahDetailDto, ayah: AyatDto, qariId: String = "05") {
        val audioMap = ayah.audio
        val url = audioMap?.get(qariId) ?: audioMap?.values?.firstOrNull()
        if (url != null) {
            val qariName = com.example.config.AppConfig.QARI_LIST.find { it.id == qariId }?.name ?: "Qari"
            audioPlayer.playAudio(
                url = url,
                title = "${surah.namaLatin} : Ayat ${ayah.nomorAyat}",
                subtitle = qariName,
                surahNumber = surah.nomor,
                ayahNumber = ayah.nomorAyat
            )
        }
    }

    fun pauseAudio() = audioPlayer.pause()
    fun resumeAudio() = audioPlayer.resume()
    fun seekAudio(progress: Float) = audioPlayer.seekTo(progress)
    fun stopAudio() = audioPlayer.stop()

    // Bookmark & Note actions
    fun toggleBookmark(
        surahNumber: Int,
        surahName: String,
        surahArabic: String,
        ayahNumber: Int,
        arabicText: String,
        translation: String
    ) {
        viewModelScope.launch {
            if (repository.isBookmarked(surahNumber, ayahNumber)) {
                repository.removeBookmark(surahNumber, ayahNumber)
            } else {
                repository.addBookmark(
                    surahNumber = surahNumber,
                    surahName = surahName,
                    surahArabic = surahArabic,
                    ayahNumber = ayahNumber,
                    arabicText = arabicText,
                    translation = translation
                )
            }
        }
    }

    fun updateBookmarkNote(id: String, note: String) {
        viewModelScope.launch {
            repository.updateBookmarkNote(id, note)
        }
    }

    fun deleteBookmark(surahNumber: Int, ayahNumber: Int) {
        viewModelScope.launch {
            repository.removeBookmark(surahNumber, ayahNumber)
        }
    }

    fun updateLastRead(
        surahNumber: Int,
        surahName: String,
        surahArabic: String,
        ayahNumber: Int,
        totalAyah: Int
    ) {
        viewModelScope.launch {
            repository.saveLastRead(surahNumber, surahName, surahArabic, ayahNumber, totalAyah)
            repository.incrementAyahRead(1)
        }
    }

    fun updateSettings(modifier: (UserSettingsEntity) -> UserSettingsEntity) {
        viewModelScope.launch {
            val current = repository.getUserSettings()
            repository.updateSettings(modifier(current))
        }
    }

    override fun onCleared() {
        super.onCleared()
        audioPlayer.release()
    }
}
