package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "servers")
data class ServerEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val url: String,
    val userId: String = "",
    val token: String = "",
    val isDemo: Boolean = false,
    val isLastUsed: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey val itemId: String,
    val title: String,
    val mediaType: String,
    val posterUrl: String,
    val year: String = "",
    val duration: String = "",
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "media_progress")
data class MediaProgressEntity(
    @PrimaryKey val itemId: String,
    val title: String,
    val mediaType: String,
    val positionMs: Long,
    val durationMs: Long,
    val posterUrl: String,
    val backdropUrl: String,
    val isFinished: Boolean = false,
    val lastWatchedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "downloads")
data class DownloadEntity(
    @PrimaryKey val itemId: String,
    val title: String,
    val overview: String,
    val mediaType: String,
    val posterUrl: String,
    val backdropUrl: String,
    val localFilePath: String,
    val downloadStatus: String = "DOWNLOADING", // "DOWNLOADING", "COMPLETED", "FAILED"
    val progressPercent: Int = 0,
    val totalSizeBytes: Long = 0L,
    val downloadedSizeBytes: Long = 0L,
    val year: String = "",
    val rating: String = "",
    val resolution: String = "",
    val videoUrl: String = "",
    val downloadedAt: Long = System.currentTimeMillis()
)
