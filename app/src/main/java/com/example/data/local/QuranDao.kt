package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface BookmarkDao {
    @Query("SELECT * FROM bookmarks ORDER BY createdAt DESC")
    fun getAllBookmarks(): Flow<List<BookmarkEntity>>

    @Query("SELECT * FROM bookmarks WHERE id = :id LIMIT 1")
    suspend fun getBookmarkById(id: String): BookmarkEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: BookmarkEntity)

    @Query("DELETE FROM bookmarks WHERE id = :id")
    suspend fun deleteBookmarkById(id: String)

    @Query("UPDATE bookmarks SET note = :note WHERE id = :id")
    suspend fun updateNote(id: String, note: String)
}

@Dao
interface LastReadDao {
    @Query("SELECT * FROM last_read WHERE id = 1 LIMIT 1")
    fun getLastReadFlow(): Flow<LastReadEntity?>

    @Query("SELECT * FROM last_read WHERE id = 1 LIMIT 1")
    suspend fun getLastRead(): LastReadEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setLastRead(lastRead: LastReadEntity)
}

@Dao
interface UserSettingsDao {
    @Query("SELECT * FROM user_settings WHERE id = 1 LIMIT 1")
    fun getSettingsFlow(): Flow<UserSettingsEntity?>

    @Query("SELECT * FROM user_settings WHERE id = 1 LIMIT 1")
    suspend fun getSettings(): UserSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSettings(settings: UserSettingsEntity)
}

@Dao
interface ReadingHistoryDao {
    @Query("SELECT * FROM reading_history ORDER BY timestamp DESC LIMIT 20")
    fun getRecentHistory(): Flow<List<ReadingHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun recordHistory(item: ReadingHistoryEntity)
}
