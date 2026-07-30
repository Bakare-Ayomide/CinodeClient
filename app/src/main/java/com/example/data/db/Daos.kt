package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ServerDao {
    @Query("SELECT * FROM servers ORDER BY createdAt DESC")
    fun getAllServers(): Flow<List<ServerEntity>>

    @Query("SELECT * FROM servers WHERE isLastUsed = 1 LIMIT 1")
    suspend fun getLastUsedServer(): ServerEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertServer(server: ServerEntity): Long

    @Query("UPDATE servers SET isLastUsed = 0")
    suspend fun clearLastUsed()

    @Query("UPDATE servers SET isLastUsed = 1 WHERE id = :id")
    suspend fun setLastUsed(id: Int)

    @Query("DELETE FROM servers WHERE id = :id")
    suspend fun deleteServer(id: Int)
}

@Dao
interface FavoriteDao {
    @Query("SELECT * FROM favorites ORDER BY addedAt DESC")
    fun getAllFavorites(): Flow<List<FavoriteEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE itemId = :itemId)")
    fun isFavorite(itemId: String): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE itemId = :itemId")
    suspend fun deleteFavorite(itemId: String)
}

@Dao
interface MediaProgressDao {
    @Query("SELECT * FROM media_progress WHERE isFinished = 0 ORDER BY lastWatchedAt DESC")
    fun getContinueWatching(): Flow<List<MediaProgressEntity>>

    @Query("SELECT * FROM media_progress WHERE itemId = :itemId LIMIT 1")
    suspend fun getProgress(itemId: String): MediaProgressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveProgress(progress: MediaProgressEntity)

    @Query("DELETE FROM media_progress WHERE itemId = :itemId")
    suspend fun deleteProgress(itemId: String)
}

@Dao
interface DownloadDao {
    @Query("SELECT * FROM downloads ORDER BY downloadedAt DESC")
    fun getAllDownloads(): Flow<List<DownloadEntity>>

    @Query("SELECT * FROM downloads WHERE itemId = :itemId LIMIT 1")
    fun getDownloadFlow(itemId: String): Flow<DownloadEntity?>

    @Query("SELECT * FROM downloads WHERE itemId = :itemId LIMIT 1")
    suspend fun getDownload(itemId: String): DownloadEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateDownload(download: DownloadEntity)

    @Query("UPDATE downloads SET progressPercent = :progress, downloadedSizeBytes = :downloadedBytes, downloadStatus = :status WHERE itemId = :itemId")
    suspend fun updateProgress(itemId: String, progress: Int, downloadedBytes: Long, status: String)

    @Query("DELETE FROM downloads WHERE itemId = :itemId")
    suspend fun deleteDownload(itemId: String)
}
