package com.example.data.model

enum class MediaType {
    MOVIE,
    SERIES,
    EPISODE,
    MUSIC_ALBUM,
    MUSIC_TRACK,
    LIVE_TV
}

data class JellyfinItem(
    val id: String,
    val title: String,
    val overview: String,
    val mediaType: MediaType,
    val posterUrl: String,
    val backdropUrl: String,
    val year: String = "2026",
    val rating: String = "8.8",
    val durationMs: Long = 7200000L, // default 120 mins
    val resolution: String = "4K HDR",
    val audioCodec: String = "Dolby Atmos 7.1",
    val seriesName: String? = null,
    val seasonNumber: Int? = null,
    val episodeNumber: Int? = null,
    val videoUrl: String = "",
    val genres: List<String> = listOf("Sci-Fi", "Action", "Adventure"),
    val cast: List<CastMember> = emptyList(),
    val channelNumber: String? = null,
    val isFavorite: Boolean = false,
    val watchPositionMs: Long = 0L
)

data class CastMember(
    val name: String,
    val role: String,
    val imageUrl: String = ""
)

data class Episode(
    val id: String,
    val title: String,
    val episodeNumber: Int,
    val seasonNumber: Int,
    val overview: String,
    val durationMs: Long,
    val thumbnailUrl: String,
    val videoUrl: String
)

data class JellyfinServer(
    val id: Int,
    val name: String,
    val url: String,
    val userId: String = "",
    val token: String = "",
    val isDemo: Boolean = false,
    val isConnected: Boolean = true
)

// D-Pad navigation mode
enum class DeviceMode {
    PHONE_TOUCH,
    SMART_TV_DPAD
}
