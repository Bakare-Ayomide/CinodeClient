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
    val mediaType: String, // "MOVIE", "SERIES", "EPISODE"
    val posterUrl: String,
    val backdropUrl: String,
    val localFilePath: String,
    val localPosterPath: String = "",
    val downloadStatus: String = "DOWNLOADING", // "WAITING", "DOWNLOADING", "PAUSED", "COMPLETED", "FAILED"
    val progressPercent: Int = 0,
    val totalSizeBytes: Long = 0L,
    val downloadedSizeBytes: Long = 0L,
    val downloadSpeedBytesPerSec: Long = 0L,
    val etaSeconds: Long = 0L,
    val year: String = "",
    val rating: String = "",
    val resolution: String = "",
    val quality: String = "1080p", // "Original", "4K", "1080p", "720p", "480p", "360p"
    val seriesName: String? = null,
    val seasonNumber: Int? = null,
    val episodeNumber: Int? = null,
    val durationMs: Long = 0L,
    val videoUrl: String = "",
    val downloadedAt: Long = System.currentTimeMillis(),
    val lastSyncedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "download_settings")
data class DownloadSettingsEntity(
    @PrimaryKey val id: Int = 1,
    val wifiOnly: Boolean = true,
    val allowMobileData: Boolean = false,
    val autoRetryFailed: Boolean = true,
    val autoDownloadNextEpisode: Boolean = false,
    val autoDeleteWatched: Boolean = false,
    val maxSimultaneousDownloads: Int = 2,
    val maxStorageLimitGb: Int = 50,
    val defaultQuality: String = "1080p"
)

@Entity(tableName = "monnify_config")
data class MonnifyConfigEntity(
    @PrimaryKey val id: Int = 1,
    val isPaywallEnabled: Boolean = true,
    val useSandbox: Boolean = true,
    val apiKey: String = "MK_TEST_SAF789Q2WS",
    val secretKey: String = "SK_TEST_9988112233",
    val contractCode: String = "3489201145",
    val streamPriceNgn: Double = 600.0,
    val vipPassPriceNgn: Double = 600.0,
    val paymentMethods: String = "CARD,ACCOUNT_TRANSFER,USSD",
    val currency: String = "NGN",
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "monnify_transactions")
data class MonnifyTransactionEntity(
    @PrimaryKey val paymentRef: String,
    val itemId: String,
    val itemTitle: String,
    val userEmail: String,
    val amountNgn: Double,
    val status: String = "PAID", // "PAID", "PENDING", "FAILED"
    val paymentMethod: String = "CARD", // "CARD", "ACCOUNT_TRANSFER", "USSD"
    val virtualAccountNo: String = "6034182991",
    val bankName: String = "Wema Bank / Monnify",
    val transactionReference: String = "",
    val paidAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "media_cache")
data class MediaCacheEntity(
    @PrimaryKey val id: String,
    val title: String,
    val overview: String,
    val mediaType: String,
    val posterUrl: String,
    val backdropUrl: String,
    val year: String = "2026",
    val rating: String = "8.8",
    val durationMs: Long = 0L,
    val seriesName: String? = null,
    val seasonNumber: Int? = null,
    val episodeNumber: Int? = null,
    val videoUrl: String = "",
    val genresJson: String = "",
    val isFavorite: Boolean = false,
    val watchPositionMs: Long = 0L,
    val cachedAt: Long = System.currentTimeMillis()
)

