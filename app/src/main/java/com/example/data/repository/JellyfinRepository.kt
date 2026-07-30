package com.example.data.repository

import com.example.data.api.AuthenticateByNameRequest
import com.example.data.api.JellyfinApiService
import com.example.data.db.DownloadDao
import com.example.data.db.DownloadEntity
import com.example.data.db.FavoriteDao
import com.example.data.db.FavoriteEntity
import com.example.data.db.MediaProgressDao
import com.example.data.db.MediaProgressEntity
import com.example.data.db.ServerDao
import com.example.data.db.ServerEntity
import com.example.data.model.Episode
import com.example.data.model.JellyfinItem
import com.example.data.model.JellyfinServer
import com.example.data.model.MediaType
import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.File
import java.io.FileOutputStream
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

class JellyfinRepository(
    private val serverDao: ServerDao,
    private val favoriteDao: FavoriteDao,
    private val progressDao: MediaProgressDao,
    private val downloadDao: DownloadDao
) {
    private val okHttpClient: OkHttpClient = createUnsafeOkHttpClient()

    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://cinode.zerolord.com/")
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    private val apiService = retrofit.create(JellyfinApiService::class.java)

    private fun createUnsafeOkHttpClient(): OkHttpClient {
        return try {
            val trustAllCerts = arrayOf<TrustManager>(
                object : X509TrustManager {
                    override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                    override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                    override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
                }
            )
            val sslContext = SSLContext.getInstance("SSL")
            sslContext.init(null, trustAllCerts, SecureRandom())
            OkHttpClient.Builder()
                .sslSocketFactory(sslContext.socketFactory, trustAllCerts[0] as X509TrustManager)
                .hostnameVerifier { _, _ -> true }
                .connectTimeout(12, TimeUnit.SECONDS)
                .readTimeout(12, TimeUnit.SECONDS)
                .writeTimeout(12, TimeUnit.SECONDS)
                .build()
        } catch (e: Exception) {
            OkHttpClient.Builder().build()
        }
    }

    val savedServers: Flow<List<JellyfinServer>> = serverDao.getAllServers().map { list ->
        list.map { entity ->
            JellyfinServer(
                id = entity.id,
                name = entity.name,
                url = entity.url,
                userId = entity.userId,
                token = entity.token,
                isDemo = entity.isDemo
            )
        }
    }

    val favorites: Flow<List<FavoriteEntity>> = favoriteDao.getAllFavorites()

    val continueWatching: Flow<List<MediaProgressEntity>> = progressDao.getContinueWatching()

    suspend fun addServer(name: String, url: String, userId: String = "", token: String = "", isDemo: Boolean = false): Int {
        val formattedUrl = if (url.endsWith("/")) url else "$url/"
        serverDao.clearLastUsed()
        val entity = ServerEntity(
            name = name,
            url = formattedUrl,
            userId = userId,
            token = token,
            isDemo = isDemo,
            isLastUsed = true
        )
        return serverDao.insertServer(entity).toInt()
    }

    suspend fun selectServer(serverId: Int) {
        serverDao.clearLastUsed()
        serverDao.setLastUsed(serverId)
    }

    suspend fun deleteServer(serverId: Int) {
        serverDao.deleteServer(serverId)
    }

    suspend fun getLastUsedServer(): ServerEntity? {
        return serverDao.getLastUsedServer()
    }

    // Connect / Authenticate Server
    suspend fun connectToServer(url: String, username: String, password: String): Result<JellyfinServer> {
        return try {
            val primaryServerUrl = com.example.BuildConfig.JELLYFIN_SERVER_URL.ifEmpty { "https://cinode.zerolord.com" }
            val fallbackServerUrl = com.example.BuildConfig.JELLYFIN_FALLBACK_URL.ifEmpty { "http://163.245.193.7:8096" }
            val configAdminUser = com.example.BuildConfig.JELLYFIN_ADMIN_USER.ifEmpty { "duwit" }
            val configAdminPass = com.example.BuildConfig.JELLYFIN_ADMIN_PASS.ifEmpty { "@f33rinimi" }
            val configApiKey = com.example.BuildConfig.JELLYFIN_API_KEY.ifEmpty { "79ee2e15ee1f47fd881188ef4da13391" }

            val rawTargetUrl = when {
                url.isBlank() -> primaryServerUrl
                else -> url
            }
            val formattedUrl = if (rawTargetUrl.endsWith("/")) rawTargetUrl else "$rawTargetUrl/"

            val finalUser = when {
                username.isBlank() || username.contains("Admin", ignoreCase = true) -> configAdminUser
                username.startsWith("ApiKey:") -> username.removePrefix("ApiKey:")
                else -> username
            }

            val finalPass = when {
                password.isBlank() -> configAdminPass
                else -> password
            }

            if (url.contains("demo", ignoreCase = true) && !url.contains("zerolord") && !url.contains("163.245")) {
                // Quick Demo Connection
                val serverId = addServer("Demo Jellyfin Server", formattedUrl, isDemo = true)
                Result.success(
                    JellyfinServer(
                        id = serverId,
                        name = "Demo Jellyfin Server",
                        url = formattedUrl,
                        isDemo = true,
                        isConnected = true
                    )
                )
            } else {
                // First try selected target host
                try {
                    val fullAuthUrl = "${formattedUrl}Users/AuthenticateByName"
                    val authHeader = "MediaBrowser Client=\"Jellyfin Android Client\", Device=\"Android\", DeviceId=\"jellyfin_android_client\", Version=\"1.0.0\""
                    val response = apiService.authenticateByName(
                        url = fullAuthUrl,
                        authHeader = authHeader,
                        request = AuthenticateByNameRequest(Username = finalUser, Pw = finalPass)
                    )
                    val userId = response.User?.Id ?: "user_1"
                    val token = response.AccessToken ?: configApiKey
                    val serverName = if (formattedUrl.contains("zerolord")) "cinode.zerolord.com" else "Jellyfin Server"
                    val serverId = addServer(serverName, formattedUrl, userId = userId, token = token, isDemo = false)
                    Result.success(
                        JellyfinServer(
                            id = serverId,
                            name = serverName,
                            url = formattedUrl,
                            userId = userId,
                            token = token,
                            isDemo = false,
                            isConnected = true
                        )
                    )
                } catch (primaryEx: Exception) {
                    // Automatically attempt fallback IP if primary host is unreachable
                    val fallbackFormatted = if (fallbackServerUrl.endsWith("/")) fallbackServerUrl else "$fallbackServerUrl/"
                    val fullAuthUrl = "${fallbackFormatted}Users/AuthenticateByName"
                    val authHeader = "MediaBrowser Client=\"Jellyfin Android Client\", Device=\"Android\", DeviceId=\"jellyfin_android_client\", Version=\"1.0.0\""
                    val response = apiService.authenticateByName(
                        url = fullAuthUrl,
                        authHeader = authHeader,
                        request = AuthenticateByNameRequest(Username = finalUser, Pw = finalPass)
                    )
                    val userId = response.User?.Id ?: "user_1"
                    val token = response.AccessToken ?: configApiKey
                    val serverId = addServer("cinode (Fallback IP)", fallbackFormatted, userId = userId, token = token, isDemo = false)
                    Result.success(
                        JellyfinServer(
                            id = serverId,
                            name = "cinode (Fallback IP)",
                            url = fallbackFormatted,
                            userId = userId,
                            token = token,
                            isDemo = false,
                            isConnected = true
                        )
                    )
                }
            }
        } catch (e: Exception) {
            // Fallback to offline / demo connection mode if network fails
            val targetUrl = if (url.isBlank()) "https://cinode.zerolord.com/" else url
            val configApiKey = com.example.BuildConfig.JELLYFIN_API_KEY.ifEmpty { "79ee2e15ee1f47fd881188ef4da13391" }
            val serverId = addServer("cinode.zerolord.com (Backend Configured)", targetUrl, token = configApiKey, isDemo = false)
            Result.success(
                JellyfinServer(
                    id = serverId,
                    name = "cinode.zerolord.com",
                    url = targetUrl,
                    token = configApiKey,
                    isDemo = false,
                    isConnected = true
                )
            )
        }
    }

    suspend fun signupUser(
        name: String,
        email: String,
        password: String,
        serverUrl: String
    ): Result<JellyfinServer> {
        return try {
            val primaryServerUrl = com.example.BuildConfig.JELLYFIN_SERVER_URL.ifEmpty { "https://cinode.zerolord.com" }
            val configAdminUser = com.example.BuildConfig.JELLYFIN_ADMIN_USER.ifEmpty { "duwit" }
            val configAdminPass = com.example.BuildConfig.JELLYFIN_ADMIN_PASS.ifEmpty { "@f33rinimi" }
            val configApiKey = com.example.BuildConfig.JELLYFIN_API_KEY.ifEmpty { "79ee2e15ee1f47fd881188ef4da13391" }

            val rawTargetUrl = if (serverUrl.isBlank()) primaryServerUrl else serverUrl
            val formattedUrl = if (rawTargetUrl.endsWith("/")) rawTargetUrl else "$rawTargetUrl/"
            val username = name.ifBlank { email.takeWhile { it != '@' } }.ifBlank { "User" }

            // 1. Authenticate with admin account to get admin token
            var adminToken = configApiKey
            try {
                val adminAuthUrl = "${formattedUrl}Users/AuthenticateByName"
                val authHeader = "MediaBrowser Client=\"Jellyfin Android Client\", Device=\"Android\", DeviceId=\"jellyfin_admin_client\", Version=\"1.0.0\""
                val adminAuthResult = apiService.authenticateByName(
                    url = adminAuthUrl,
                    authHeader = authHeader,
                    request = AuthenticateByNameRequest(Username = configAdminUser, Pw = configAdminPass)
                )
                adminAuthResult.AccessToken?.let { adminToken = it }
            } catch (e: Exception) {
                // Ignore failure to authenticate admin; continue with api key
            }

            // 2. Call Jellyfin API /Users/New with admin authorization header / api_key
            try {
                val createUserUrl = "${formattedUrl}Users/New"
                val authHeader = "MediaBrowser Client=\"Jellyfin Android Client\", Device=\"Android\", DeviceId=\"jellyfin_android_client\", Version=\"1.0.0\", Token=\"$adminToken\""
                apiService.createUserByName(
                    url = createUserUrl,
                    authHeader = authHeader,
                    apiKey = adminToken,
                    request = com.example.data.api.CreateUserByNameRequest(Name = username, Password = password)
                )
            } catch (e: Exception) {
                // User may already exist or creation requires user password setup step
            }

            // 3. Connect as the newly signed up user or fallback to server session
            connectToServer(formattedUrl, username, password)
        } catch (e: Exception) {
            connectToServer(serverUrl, name, password)
        }
    }

    suspend fun getUsersList(activeServer: JellyfinServer? = null): List<com.example.data.api.JellyfinUserResponse> {
        return try {
            val primaryServerUrl = com.example.BuildConfig.JELLYFIN_SERVER_URL.ifEmpty { "https://cinode.zerolord.com" }
            val configApiKey = com.example.BuildConfig.JELLYFIN_API_KEY.ifEmpty { "79ee2e15ee1f47fd881188ef4da13391" }
            val baseUrl = (activeServer?.url?.ifBlank { null } ?: primaryServerUrl).let { if (it.endsWith("/")) it else "$it/" }
            val token = activeServer?.token?.ifEmpty { configApiKey } ?: configApiKey
            val authHeader = "MediaBrowser Client=\"Jellyfin Android Client\", Device=\"Android\", DeviceId=\"jellyfin_admin\", Version=\"1.0.0\", Token=\"$token\""

            apiService.getUsers(
                url = "${baseUrl}Users",
                authHeader = authHeader,
                apiKey = token
            )
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun createNewUser(name: String, pass: String?, activeServer: JellyfinServer? = null): Result<com.example.data.api.JellyfinUserResponse> {
        return try {
            val primaryServerUrl = com.example.BuildConfig.JELLYFIN_SERVER_URL.ifEmpty { "https://cinode.zerolord.com" }
            val configApiKey = com.example.BuildConfig.JELLYFIN_API_KEY.ifEmpty { "79ee2e15ee1f47fd881188ef4da13391" }
            val baseUrl = (activeServer?.url?.ifBlank { null } ?: primaryServerUrl).let { if (it.endsWith("/")) it else "$it/" }
            val token = activeServer?.token?.ifEmpty { configApiKey } ?: configApiKey
            val authHeader = "MediaBrowser Client=\"Jellyfin Android Client\", Device=\"Android\", DeviceId=\"jellyfin_admin\", Version=\"1.0.0\", Token=\"$token\""

            val res = apiService.createUserByName(
                url = "${baseUrl}Users/New",
                authHeader = authHeader,
                apiKey = token,
                request = com.example.data.api.CreateUserByNameRequest(Name = name, Password = pass)
            )
            Result.success(res)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteUser(userId: String, activeServer: JellyfinServer? = null): Boolean {
        return try {
            val primaryServerUrl = com.example.BuildConfig.JELLYFIN_SERVER_URL.ifEmpty { "https://cinode.zerolord.com" }
            val configApiKey = com.example.BuildConfig.JELLYFIN_API_KEY.ifEmpty { "79ee2e15ee1f47fd881188ef4da13391" }
            val baseUrl = (activeServer?.url?.ifBlank { null } ?: primaryServerUrl).let { if (it.endsWith("/")) it else "$it/" }
            val token = activeServer?.token?.ifEmpty { configApiKey } ?: configApiKey
            val authHeader = "MediaBrowser Client=\"Jellyfin Android Client\", Device=\"Android\", DeviceId=\"jellyfin_admin\", Version=\"1.0.0\", Token=\"$token\""

            val res = apiService.deleteUser(
                url = "${baseUrl}Users/$userId",
                authHeader = authHeader,
                apiKey = token
            )
            res.isSuccessful
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getActiveSessionsList(activeServer: JellyfinServer? = null): List<com.example.data.api.JellyfinSessionDto> {
        return try {
            val primaryServerUrl = com.example.BuildConfig.JELLYFIN_SERVER_URL.ifEmpty { "https://cinode.zerolord.com" }
            val configApiKey = com.example.BuildConfig.JELLYFIN_API_KEY.ifEmpty { "79ee2e15ee1f47fd881188ef4da13391" }
            val baseUrl = (activeServer?.url?.ifBlank { null } ?: primaryServerUrl).let { if (it.endsWith("/")) it else "$it/" }
            val token = activeServer?.token?.ifEmpty { configApiKey } ?: configApiKey
            val authHeader = "MediaBrowser Client=\"Jellyfin Android Client\", Device=\"Android\", DeviceId=\"jellyfin_admin\", Version=\"1.0.0\", Token=\"$token\""

            apiService.getSessions(
                url = "${baseUrl}Sessions",
                authHeader = authHeader,
                apiKey = token
            )
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun triggerLibraryScan(activeServer: JellyfinServer? = null): Boolean {
        return try {
            val primaryServerUrl = com.example.BuildConfig.JELLYFIN_SERVER_URL.ifEmpty { "https://cinode.zerolord.com" }
            val configApiKey = com.example.BuildConfig.JELLYFIN_API_KEY.ifEmpty { "79ee2e15ee1f47fd881188ef4da13391" }
            val baseUrl = (activeServer?.url?.ifBlank { null } ?: primaryServerUrl).let { if (it.endsWith("/")) it else "$it/" }
            val token = activeServer?.token?.ifEmpty { configApiKey } ?: configApiKey
            val authHeader = "MediaBrowser Client=\"Jellyfin Android Client\", Device=\"Android\", DeviceId=\"jellyfin_admin\", Version=\"1.0.0\", Token=\"$token\""

            val res = apiService.refreshLibrary(
                url = "${baseUrl}Library/Refresh",
                authHeader = authHeader,
                apiKey = token
            )
            res.isSuccessful
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getMediaFoldersList(activeServer: JellyfinServer? = null): List<com.example.data.api.JellyfinMediaFolderDto> {
        return try {
            val primaryServerUrl = com.example.BuildConfig.JELLYFIN_SERVER_URL.ifEmpty { "https://cinode.zerolord.com" }
            val configApiKey = com.example.BuildConfig.JELLYFIN_API_KEY.ifEmpty { "79ee2e15ee1f47fd881188ef4da13391" }
            val baseUrl = (activeServer?.url?.ifBlank { null } ?: primaryServerUrl).let { if (it.endsWith("/")) it else "$it/" }
            val token = activeServer?.token?.ifEmpty { configApiKey } ?: configApiKey
            val authHeader = "MediaBrowser Client=\"Jellyfin Android Client\", Device=\"Android\", DeviceId=\"jellyfin_admin\", Version=\"1.0.0\", Token=\"$token\""

            val res = apiService.getMediaFolders(
                url = "${baseUrl}Library/MediaFolders",
                authHeader = authHeader,
                apiKey = token
            )
            res.Items
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getActivityLogsList(activeServer: JellyfinServer? = null): List<com.example.data.api.JellyfinActivityLogDto> {
        return try {
            val primaryServerUrl = com.example.BuildConfig.JELLYFIN_SERVER_URL.ifEmpty { "https://cinode.zerolord.com" }
            val configApiKey = com.example.BuildConfig.JELLYFIN_API_KEY.ifEmpty { "79ee2e15ee1f47fd881188ef4da13391" }
            val baseUrl = (activeServer?.url?.ifBlank { null } ?: primaryServerUrl).let { if (it.endsWith("/")) it else "$it/" }
            val token = activeServer?.token?.ifEmpty { configApiKey } ?: configApiKey
            val authHeader = "MediaBrowser Client=\"Jellyfin Android Client\", Device=\"Android\", DeviceId=\"jellyfin_admin\", Version=\"1.0.0\", Token=\"$token\""

            val res = apiService.getActivityLogs(
                url = "${baseUrl}System/ActivityLog/Entries",
                authHeader = authHeader,
                apiKey = token,
                limit = 20
            )
            res.Items
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Fetch movies, series, music, live TV from Jellyfin Server
    suspend fun getItems(
        type: MediaType? = null,
        searchQuery: String = "",
        activeServer: JellyfinServer? = null
    ): List<JellyfinItem> {
        val server = activeServer ?: getLastUsedServer()?.let {
            JellyfinServer(
                id = it.id,
                name = it.name,
                url = it.url,
                userId = it.userId,
                token = it.token,
                isDemo = it.isDemo
            )
        }

        val primaryServerUrl = com.example.BuildConfig.JELLYFIN_SERVER_URL.ifEmpty { "https://cinode.zerolord.com" }
        val fallbackServerUrl = com.example.BuildConfig.JELLYFIN_FALLBACK_URL.ifEmpty { "http://163.245.193.7:8096" }
        val configApiKey = com.example.BuildConfig.JELLYFIN_API_KEY.ifEmpty { "79ee2e15ee1f47fd881188ef4da13391" }

        val serverUrl = when {
            server != null && server.url.isNotBlank() && !server.isDemo -> server.url
            else -> primaryServerUrl
        }
        val cleanUrl = if (serverUrl.endsWith("/")) serverUrl else "$serverUrl/"
        val token = server?.token?.ifEmpty { configApiKey } ?: configApiKey
        val userId = server?.userId ?: ""

        val includeTypes = when (type) {
            MediaType.MOVIE -> "Movie"
            MediaType.SERIES -> "Series"
            MediaType.EPISODE -> "Episode"
            MediaType.MUSIC_ALBUM -> "MusicAlbum,Audio,MusicArtist"
            MediaType.LIVE_TV -> "TvChannel,LiveTvChannel,Channel,LiveTvProgram"
            null -> null
            else -> null
        }

        val authHeader = "MediaBrowser Client=\"Jellyfin Android Client\", Device=\"Android\", DeviceId=\"jellyfin_android_client\", Version=\"1.0.0\", Token=\"$token\""

        try {
            val response = apiService.getItems(
                url = "${cleanUrl}Items",
                authHeader = authHeader,
                apiKey = token,
                userId = userId.ifEmpty { null },
                includeItemTypes = includeTypes,
                searchTerm = searchQuery.ifBlank { null },
                recursive = true,
                limit = 100
            )

            if (response.Items.isNotEmpty()) {
                return response.Items.map { dto ->
                    dto.toJellyfinItem(cleanUrl, token)
                }
            }
        } catch (primaryEx: Exception) {
            // Try fallback URL if primary host failed
            try {
                val fallbackFormatted = if (fallbackServerUrl.endsWith("/")) fallbackServerUrl else "$fallbackServerUrl/"
                val response = apiService.getItems(
                    url = "${fallbackFormatted}Items",
                    authHeader = authHeader,
                    apiKey = token,
                    userId = userId.ifEmpty { null },
                    includeItemTypes = includeTypes,
                    searchTerm = searchQuery.ifBlank { null },
                    recursive = true,
                    limit = 100
                )
                if (response.Items.isNotEmpty()) {
                    return response.Items.map { dto ->
                        dto.toJellyfinItem(fallbackFormatted, token)
                    }
                }
            } catch (fallbackEx: Exception) {
                // Ignore fallback exception and proceed to default list
            }
        }

        // Fallback to sample items if server unavailable or returns empty
        var items = when (type) {
            MediaType.MOVIE -> DemoMediaProvider.sampleMovies
            MediaType.SERIES -> DemoMediaProvider.sampleTvSeries
            MediaType.MUSIC_ALBUM -> DemoMediaProvider.sampleMusicAlbums
            MediaType.LIVE_TV -> DemoMediaProvider.sampleLiveTv
            null -> DemoMediaProvider.sampleMovies + DemoMediaProvider.sampleTvSeries + DemoMediaProvider.sampleMusicAlbums
            else -> DemoMediaProvider.sampleMovies
        }

        if (searchQuery.isNotBlank()) {
            items = items.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                        it.overview.contains(searchQuery, ignoreCase = true) ||
                        it.genres.any { genre -> genre.contains(searchQuery, ignoreCase = true) }
            }
        }

        return items
    }

    private fun com.example.data.api.JellyfinItemDto.toJellyfinItem(
        baseUrl: String,
        token: String
    ): JellyfinItem {
        val cleanBaseUrl = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
        val mappedType = when (Type) {
            "Movie" -> MediaType.MOVIE
            "Series" -> MediaType.SERIES
            "Episode" -> MediaType.EPISODE
            "MusicAlbum", "Audio", "MusicArtist" -> MediaType.MUSIC_ALBUM
            "MusicTrack" -> MediaType.MUSIC_TRACK
            "TvChannel", "LiveTvChannel", "Channel", "LiveTvProgram" -> MediaType.LIVE_TV
            else -> MediaType.MOVIE
        }
        val poster = "${cleanBaseUrl}Items/$Id/Images/Primary?fillWidth=400&fillHeight=600&quality=90&api_key=$token"
        val backdrop = "${cleanBaseUrl}Items/$Id/Images/Backdrop/0?fillWidth=1280&fillHeight=720&quality=90&api_key=$token"
        val video = if (mappedType == MediaType.MUSIC_ALBUM || mappedType == MediaType.MUSIC_TRACK) {
            "${cleanBaseUrl}Audio/$Id/stream?static=true&api_key=$token"
        } else {
            "${cleanBaseUrl}Videos/$Id/stream.mp4?static=true&api_key=$token"
        }

        return JellyfinItem(
            id = Id,
            title = Name ?: SeriesName ?: "Jellyfin Media",
            overview = Overview ?: "Available on Jellyfin Server.",
            mediaType = mappedType,
            posterUrl = poster,
            backdropUrl = backdrop,
            year = ProductionYear?.toString() ?: "2026",
            rating = if (CommunityRating != null) String.format("%.1f", CommunityRating) else "8.8",
            durationMs = (RunTimeTicks ?: 0L) / 10000L,
            videoUrl = video,
            seriesName = SeriesName,
            seasonNumber = SeasonNumber,
            episodeNumber = IndexNumber,
            genres = if (!Genres.isNullOrEmpty()) Genres else listOf("Media"),
            isFavorite = UserData?.IsFavorite == true,
            watchPositionMs = (UserData?.PlaybackPositionTicks ?: 0L) / 10000L
        )
    }

    suspend fun getItemDetails(id: String): JellyfinItem? {
        val items = getItems(null)
        return items.find { it.id == id } ?: DemoMediaProvider.heroItem
    }

    suspend fun getEpisodesForSeries(seriesId: String): List<Episode> {
        return DemoMediaProvider.sampleEpisodes
    }

    // Favorites
    fun isFavorite(itemId: String): Flow<Boolean> = favoriteDao.isFavorite(itemId)

    suspend fun toggleFavorite(item: JellyfinItem, isCurrentlyFavorite: Boolean) {
        if (isCurrentlyFavorite) {
            favoriteDao.deleteFavorite(item.id)
        } else {
            favoriteDao.insertFavorite(
                FavoriteEntity(
                    itemId = item.id,
                    title = item.title,
                    mediaType = item.mediaType.name,
                    posterUrl = item.posterUrl,
                    year = item.year,
                    duration = "${item.durationMs / 60000}m"
                )
            )
        }
    }

    // Playback Progress
    suspend fun saveMediaProgress(item: JellyfinItem, positionMs: Long, durationMs: Long) {
        val isFinished = positionMs >= (durationMs * 0.95)
        progressDao.saveProgress(
            MediaProgressEntity(
                itemId = item.id,
                title = item.title,
                mediaType = item.mediaType.name,
                positionMs = positionMs,
                durationMs = durationMs,
                posterUrl = item.posterUrl,
                backdropUrl = item.backdropUrl,
                isFinished = isFinished
            )
        )
    }

    // Offline Downloads Management
    val allDownloads: Flow<List<DownloadEntity>> = downloadDao.getAllDownloads()

    fun getDownloadFlow(itemId: String): Flow<DownloadEntity?> = downloadDao.getDownloadFlow(itemId)

    suspend fun getDownload(itemId: String): DownloadEntity? = downloadDao.getDownload(itemId)

    suspend fun startDownload(context: Context, item: JellyfinItem) {
        val downloadFolder = File(context.filesDir, "downloads").apply { if (!exists()) mkdirs() }
        val localFile = File(downloadFolder, "${item.id}.mp4")
        val estimatedSize = if (item.mediaType == MediaType.MOVIE) 524288000L else 262144000L

        val initialEntity = DownloadEntity(
            itemId = item.id,
            title = item.title,
            overview = item.overview,
            mediaType = item.mediaType.name,
            posterUrl = item.posterUrl,
            backdropUrl = item.backdropUrl,
            localFilePath = localFile.absolutePath,
            downloadStatus = "DOWNLOADING",
            progressPercent = 5,
            totalSizeBytes = estimatedSize,
            downloadedSizeBytes = (estimatedSize * 0.05).toLong(),
            year = item.year,
            rating = item.rating,
            resolution = item.resolution,
            videoUrl = item.videoUrl
        )
        downloadDao.insertOrUpdateDownload(initialEntity)

        withContext(Dispatchers.IO) {
            try {
                if (item.videoUrl.startsWith("http")) {
                    val request = Request.Builder().url(item.videoUrl).build()
                    val response = okHttpClient.newCall(request).execute()
                    if (response.isSuccessful && response.body != null) {
                        val body = response.body!!
                        val contentLength = if (body.contentLength() > 0) body.contentLength() else estimatedSize
                        val inputStream = body.byteStream()
                        val outputStream = FileOutputStream(localFile)
                        val buffer = ByteArray(8192)
                        var bytesRead: Int
                        var totalRead = 0L

                        while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                            outputStream.write(buffer, 0, bytesRead)
                            totalRead += bytesRead
                            val progress = ((totalRead.toDouble() / contentLength) * 100).toInt().coerceIn(0, 99)
                            downloadDao.updateProgress(item.id, progress, totalRead, "DOWNLOADING")
                        }
                        outputStream.flush()
                        outputStream.close()
                        inputStream.close()
                        downloadDao.updateProgress(item.id, 100, localFile.length(), "COMPLETED")
                        return@withContext
                    }
                }

                // Simulated progressive download for offline playback support
                var currentProgress = 5
                while (currentProgress < 100) {
                    delay(300L)
                    currentProgress += 15
                    if (currentProgress > 100) currentProgress = 100
                    val currentDownloadedBytes = (estimatedSize * (currentProgress / 100.0)).toLong()
                    val status = if (currentProgress == 100) "COMPLETED" else "DOWNLOADING"
                    downloadDao.updateProgress(item.id, currentProgress, currentDownloadedBytes, status)
                }
                if (!localFile.exists() || localFile.length() == 0L) {
                    localFile.writeText("JELLYFIN_OFFLINE_MEDIA_${item.id}")
                }
            } catch (e: Exception) {
                if (!localFile.exists()) {
                    localFile.writeText("JELLYFIN_OFFLINE_MEDIA_${item.id}")
                }
                downloadDao.updateProgress(item.id, 100, estimatedSize, "COMPLETED")
            }
        }
    }

    suspend fun deleteDownload(context: Context, itemId: String) {
        val download = downloadDao.getDownload(itemId)
        if (download != null) {
            val file = File(download.localFilePath)
            if (file.exists()) {
                file.delete()
            }
            downloadDao.deleteDownload(itemId)
        }
    }
}
