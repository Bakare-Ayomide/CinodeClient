package com.example.data.repository

import com.example.R
import com.example.data.model.CastMember
import com.example.data.model.Episode
import com.example.data.model.JellyfinItem
import com.example.data.model.MediaType

object DemoMediaProvider {

    // Public sample video streams for media playback verification
    private const val STREAM_TEARS_OF_STEEL = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4"
    private const val STREAM_BIG_BUCK_BUNNY = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
    private const val STREAM_SINTEL = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4"
    private const val STREAM_ELEPHANTS_DREAM = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4"
    private const val STREAM_FOR_BIGGER_BLAZES = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4"

    // Drawable references for generated high-res posters
    private val poster1 = "android.resource://com.aistudio.jellyfinclient.app/" + R.drawable.img_movie_poster_1
    private val poster2 = "android.resource://com.aistudio.jellyfinclient.app/" + R.drawable.img_movie_poster_2
    private val heroBanner = "android.resource://com.aistudio.jellyfinclient.app/" + R.drawable.img_hero_banner

    val heroItem = JellyfinItem(
        id = "hero_supergirl",
        title = "Supergirl",
        overview = "When an unexpected and ruthless adversary strikes too close to home, Kara Zor-El, aka Supergirl, reluctantly joins forces with an unlikely companion on an epic, interstellar journey of vengeance and justice.",
        mediaType = MediaType.MOVIE,
        posterUrl = "https://images.unsplash.com/photo-1534447677768-be436bb09401?w=800&auto=format&fit=crop",
        backdropUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=1200&auto=format&fit=crop",
        year = "2026",
        rating = "6.7",
        durationMs = 7800000L, // 2h 10m
        resolution = "4K HDR • IMAX",
        audioCodec = "Dolby Atmos 7.1",
        genres = listOf("Action", "Adventure", "Sci-Fi"),
        cast = listOf(
            CastMember("Milly Alcock", "Kara Zor-El / Supergirl", "https://picsum.photos/seed/cast1/200/300"),
            CastMember("Matthias Schoenaerts", "Krem of the Yellow Hills", "https://picsum.photos/seed/cast2/200/300")
        ),
        videoUrl = STREAM_TEARS_OF_STEEL
    )

    val sampleMovies = listOf(
        heroItem,
        JellyfinItem(
            id = "movie_2",
            title = "Neon City 2099",
            overview = "In a rain-drenched cyberpunk metropolis, a rogue cybernetic detective uncovers a conspiracy threatening to wipe out human memories.",
            mediaType = MediaType.MOVIE,
            posterUrl = poster2,
            backdropUrl = heroBanner,
            year = "2025",
            rating = "9.1",
            durationMs = 7320000L,
            resolution = "4K Ultra HD",
            audioCodec = "DTS-X 7.1",
            genres = listOf("Cyberpunk", "Action", "Mystery"),
            cast = listOf(
                CastMember("Kaelen Cross", "Detective", "https://picsum.photos/seed/cast4/200/300"),
                CastMember("Nyx Vane", "Hacker", "https://picsum.photos/seed/cast5/200/300")
            ),
            videoUrl = STREAM_BIG_BUCK_BUNNY
        ),
        JellyfinItem(
            id = "movie_3",
            title = "Tears of Steel",
            overview = "In a dystopian future, a team of soldiers and scientists gather in Amsterdam to stop a colossal artificial intelligence onslaught.",
            mediaType = MediaType.MOVIE,
            posterUrl = "https://images.unsplash.com/photo-1534447677768-be436bb09401?w=600&auto=format&fit=crop",
            backdropUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=1200&auto=format&fit=crop",
            year = "2024",
            rating = "8.7",
            durationMs = 5400000L,
            resolution = "4K HDR",
            audioCodec = "5.1 Surround",
            genres = listOf("Action", "Sci-Fi"),
            videoUrl = STREAM_TEARS_OF_STEEL
        ),
        JellyfinItem(
            id = "movie_4",
            title = "Sintel: Dragon's Legacy",
            overview = "A lonely wanderer embarks on a treacherous quest across icy mountain peaks to reclaim her stolen companion dragon.",
            mediaType = MediaType.MOVIE,
            posterUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=600&auto=format&fit=crop",
            backdropUrl = "https://images.unsplash.com/photo-1534447677768-be436bb09401?w=1200&auto=format&fit=crop",
            year = "2024",
            rating = "8.9",
            durationMs = 4800000L,
            resolution = "1080p HD",
            audioCodec = "Dolby Digital",
            genres = listOf("Fantasy", "Adventure"),
            videoUrl = STREAM_SINTEL
        ),
        JellyfinItem(
            id = "movie_5",
            title = "Cosmic Awakening",
            overview = "Deep space researchers make first contact with a sentient nebula that communicates through harmonic stellar frequencies.",
            mediaType = MediaType.MOVIE,
            posterUrl = poster1,
            backdropUrl = heroBanner,
            year = "2026",
            rating = "8.6",
            durationMs = 6900000L,
            resolution = "4K HDR",
            audioCodec = "Dolby Atmos",
            genres = listOf("Sci-Fi", "Drama"),
            videoUrl = STREAM_ELEPHANTS_DREAM
        )
    )

    val sampleTvSeries = listOf(
        JellyfinItem(
            id = "series_1",
            title = "Quantum Chronicles",
            overview = "A team of time-traveling agents leap between timeline branches to prevent reality paradoxes.",
            mediaType = MediaType.SERIES,
            posterUrl = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=600&auto=format&fit=crop",
            backdropUrl = heroBanner,
            year = "2026",
            rating = "9.5",
            durationMs = 3600000L,
            resolution = "4K HDR",
            seriesName = "Quantum Chronicles",
            genres = listOf("Sci-Fi", "Drama", "Mystery"),
            videoUrl = STREAM_FOR_BIGGER_BLAZES
        ),
        JellyfinItem(
            id = "series_2",
            title = "Mythic Realm: Shadows",
            overview = "High fantasy series tracking five noble houses clashing for control over ancient elemental runes.",
            mediaType = MediaType.SERIES,
            posterUrl = poster1,
            backdropUrl = heroBanner,
            year = "2025",
            rating = "9.2",
            durationMs = 3300000L,
            resolution = "4K HDR",
            seriesName = "Mythic Realm",
            genres = listOf("Fantasy", "Drama"),
            videoUrl = STREAM_TEARS_OF_STEEL
        ),
        JellyfinItem(
            id = "series_3",
            title = "Sub-Zero Station",
            overview = "Scientists in Antarctic Station 9 discover an unearthly entity buried beneath three miles of solid ice.",
            mediaType = MediaType.SERIES,
            posterUrl = poster2,
            backdropUrl = heroBanner,
            year = "2026",
            rating = "8.8",
            durationMs = 2700000L,
            resolution = "1080p HD",
            seriesName = "Sub-Zero Station",
            genres = listOf("Horror", "Mystery", "Sci-Fi"),
            videoUrl = STREAM_BIG_BUCK_BUNNY
        )
    )

    val sampleEpisodes = listOf(
        Episode(
            id = "ep_101",
            title = "S1:E1 - The Temporal Anomaly",
            episodeNumber = 1,
            seasonNumber = 1,
            overview = "Agent Vance discovers a rogue divergence in 1984 Berlin that threatens to erase the modern orbital grid.",
            durationMs = 3120000L,
            thumbnailUrl = heroBanner,
            videoUrl = STREAM_FOR_BIGGER_BLAZES
        ),
        Episode(
            id = "ep_102",
            title = "S1:E2 - Entropy Zero",
            episodeNumber = 2,
            seasonNumber = 1,
            overview = "Trapped in a decaying pocket dimension, the squad must recalibrate their quantum jump engine before thermal collapse.",
            durationMs = 2940000L,
            thumbnailUrl = poster1,
            videoUrl = STREAM_TEARS_OF_STEEL
        ),
        Episode(
            id = "ep_103",
            title = "S1:E3 - Echoes in the Static",
            episodeNumber = 3,
            seasonNumber = 1,
            overview = "A distress signal from an abandoned lunar station exposes secret military experiments with dark matter.",
            durationMs = 3240000L,
            thumbnailUrl = poster2,
            videoUrl = STREAM_SINTEL
        )
    )

    val sampleMusicAlbums = listOf(
        JellyfinItem(
            id = "music_1",
            title = "Synthwave Odyssey 2099",
            overview = "Album by Neon Horizon • 12 Tracks",
            mediaType = MediaType.MUSIC_ALBUM,
            posterUrl = poster2,
            backdropUrl = heroBanner,
            year = "2026",
            rating = "9.6",
            durationMs = 2800000L,
            resolution = "FLAC 24-bit / 96kHz",
            audioCodec = "Hi-Res Audio",
            genres = listOf("Synthwave", "Electronic", "Ambient")
        ),
        JellyfinItem(
            id = "music_2",
            title = "Starlight Symphonies",
            overview = "Album by Galactic Philharmonic • 8 Tracks",
            mediaType = MediaType.MUSIC_ALBUM,
            posterUrl = poster1,
            backdropUrl = heroBanner,
            year = "2025",
            rating = "9.3",
            durationMs = 3400000L,
            resolution = "FLAC 24-bit / 192kHz",
            audioCodec = "Spatial Audio",
            genres = listOf("Orchestral", "Cinematic")
        )
    )

    val sampleLiveTv = listOf(
        JellyfinItem(
            id = "livetv_1",
            title = "Cinema 4K Ultra",
            overview = "Now Playing: Interstellar Directors Cut • Next: Cyberpunk Docu-Series",
            mediaType = MediaType.LIVE_TV,
            posterUrl = heroBanner,
            backdropUrl = heroBanner,
            channelNumber = "CH 101",
            resolution = "4K 60fps",
            videoUrl = STREAM_TEARS_OF_STEEL
        ),
        JellyfinItem(
            id = "livetv_2",
            title = "Sci-Fi Channel HD",
            overview = "Now Playing: Quantum Chronicles Marathon",
            mediaType = MediaType.LIVE_TV,
            posterUrl = poster1,
            backdropUrl = heroBanner,
            channelNumber = "CH 102",
            resolution = "1080p 60fps",
            videoUrl = STREAM_BIG_BUCK_BUNNY
        ),
        JellyfinItem(
            id = "livetv_3",
            title = "Nature & Planet Earth 4K",
            overview = "Now Playing: Wonders of Deep Oceans",
            mediaType = MediaType.LIVE_TV,
            posterUrl = poster2,
            backdropUrl = heroBanner,
            channelNumber = "CH 105",
            resolution = "4K HDR",
            videoUrl = STREAM_SINTEL
        )
    )
}
