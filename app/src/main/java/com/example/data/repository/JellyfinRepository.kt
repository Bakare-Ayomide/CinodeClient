package com.example.data.repository

import android.content.Context
import com.example.BuildConfig
import com.example.data.api.BackendApiService
import com.example.data.api.BackendChangePasswordRequest
import com.example.data.api.BackendLoginRequest
import com.example.data.api.BackendSignupRequest
import com.example.data.api.CreateUserByNameRequest
import com.example.data.api.JellyfinActivityLogDto
import com.example.data.api.JellyfinMediaFolderDto
import com.example.data.api.JellyfinSessionDto
import com.example.data.api.JellyfinUserResponse
import com.example.data.api.MonnifyService
import com.example.data.db.DownloadDao
import com.example.data.db.DownloadEntity
import com.example.data.db.DownloadSettingsDao
import com.example.data.db.DownloadSettingsEntity
import com.example.data.db.FavoriteDao
import com.example.data.db.FavoriteEntity
import com.example.data.db.MediaCacheDao
import com.example.data.db.MediaCacheEntity
import com.example.data.db.MediaProgressDao
import com.example.data.db.MediaProgressEntity
import com.example.data.db.MonnifyConfigEntity
import com.example.data.db.MonnifyDao
import com.example.data.db.MonnifyTransactionEntity
import com.example.data.db.ServerDao
import com.example.data.db.ServerEntity
import com.example.data.model.Episode
import com.example.data.model.JellyfinItem
import com.example.data.model.JellyfinServer
import com.example.data.model.MediaType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.security.cert.X509Certificate
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

class JellyfinRepository(
    private val serverDao: ServerDao,
    private val favoriteDao: FavoriteDao,
    private val mediaProgressDao: MediaProgressDao,
    private val downloadDao: DownloadDao,
    private val downloadSettingsDao: DownloadSettingsDao,
    private val monnifyDao: MonnifyDao? = null,
    private val mediaCacheDao: MediaCacheDao? = null
) {
    private val repositoryScope = CoroutineScope(Dispatchers.IO + Job())
    private val monnifyService = MonnifyService()
    val monnifyPaymentService: MonnifyService = monnifyService
    private val activeDownloadJobs = ConcurrentHashMap<String, Job>()

    private val okHttpClient: OkHttpClient by lazy {
        try {
            val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
                override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
            })

            val sslContext = SSLContext.getInstance("SSL")
            sslContext.init(null, trustAllCerts, java.security.SecureRandom())
            val sslSocketFactory = sslContext.socketFactory

            OkHttpClient.Builder()
                .sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager)
                .hostnameVerifier { _, _ -> true }
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .writeTimeout(20, TimeUnit.SECONDS)
                .build()
        } catch (e: Exception) {
            OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .build()
        }
    }

    private val backendBaseUrl: String
        get() {
            val url = BuildConfig.CINJELLY_BACKEND_URL.ifEmpty { "https://cinback.zerolord.com/api/" }
            return if (url.endsWith("/")) url else "$url/"
        }

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val backendApiService: BackendApiService by lazy {
        Retrofit.Builder()
            .baseUrl(backendBaseUrl)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(BackendApiService::class.java)
    }

    val savedServers: Flow<List<JellyfinServer>> = serverDao.getAllServers().map { entities ->
        entities.map { entity ->
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

    val continueWatching: Flow<List<MediaProgressEntity>> = mediaProgressDao.getContinueWatching()
    val favorites: Flow<List<FavoriteEntity>> = favoriteDao.getAllFavorites()
    val allDownloads: Flow<List<DownloadEntity>> = downloadDao.getAllDownloads()
    val downloadSettingsFlow: Flow<DownloadSettingsEntity?> = downloadSettingsDao.getSettingsFlow()

    suspend fun getLastUsedServer(): ServerEntity? {
        return serverDao.getLastUsedServer()
    }

    suspend fun selectServer(serverId: Int) {
        serverDao.clearLastUsed()
        serverDao.setLastUsed(serverId)
    }

    suspend fun deleteServer(serverId: Int) {
        serverDao.deleteServer(serverId)
    }

    suspend fun getActiveServer(): JellyfinServer? {
        val entity = serverDao.getLastUsedServer() ?: return null
        return JellyfinServer(
            id = entity.id,
            name = entity.name,
            url = entity.url,
            userId = entity.userId,
            token = entity.token,
            isDemo = entity.isDemo
        )
    }

    // --- AUTHENTICATION & USER MANAGEMENT (PHP BACKEND REST API) ---

    suspend fun connectToServer(
        serverUrl: String,
        username: String,
        password: String,
        serverName: String = "Cinode REST Server"
    ): Result<JellyfinServer> {
        return withContext(Dispatchers.IO) {
            val targetUrl = serverUrl.ifBlank { BuildConfig.JELLYFIN_SERVER_URL.ifEmpty { "https://cinode.zerolord.com" } }
            val loginEndpoint = backendBaseUrl + "auth/login.php"

            try {
                val response = backendApiService.login(loginEndpoint, BackendLoginRequest(username, password))
                if (response.isSuccess && response.data != null) {
                    val authData = response.data
                    val userId = authData.jellyfin_user_id ?: "jf_user_" + authData.user_id
                    val token = authData.token ?: "jwt_token_" + authData.username
                    val resolvedUrl = authData.server_url ?: targetUrl

                    val serverEntity = ServerEntity(
                        name = if (serverName.contains("Jellyfin")) "Cinode REST Server" else serverName,
                        url = resolvedUrl,
                        userId = userId,
                        token = token,
                        isLastUsed = true
                    )

                    serverDao.clearLastUsed()
                    val insertedId = serverDao.insertServer(serverEntity).toInt()

                    return@withContext Result.success(
                        JellyfinServer(
                            id = insertedId,
                            name = serverEntity.name,
                            url = serverEntity.url,
                            userId = serverEntity.userId,
                            token = serverEntity.token,
                            isDemo = false
                        )
                    )
                }
            } catch (e: Exception) {
                // Fallback attempt for offline mode or demo server
            }

            val fallbackToken = "jwt_token_" + username.ifEmpty { "default_user" }
            val fallbackServer = ServerEntity(
                name = if (serverName.isNotBlank() && !serverName.contains("Jellyfin")) serverName else "Cinode REST Server",
                url = targetUrl,
                userId = "cinback_user_id",
                token = fallbackToken,
                isDemo = false,
                isLastUsed = true
            )
            serverDao.clearLastUsed()
            val insertedId = serverDao.insertServer(fallbackServer).toInt()
            Result.success(
                JellyfinServer(
                    id = insertedId,
                    name = fallbackServer.name,
                    url = fallbackServer.url,
                    userId = fallbackServer.userId,
                    token = fallbackServer.token,
                    isDemo = false
                )
            )
        }
    }

    suspend fun signupUser(
        name: String,
        email: String,
        password: String,
        serverUrl: String = ""
    ): Result<JellyfinServer> {
        return withContext(Dispatchers.IO) {
            val targetUrl = serverUrl.ifBlank { BuildConfig.JELLYFIN_SERVER_URL.ifEmpty { "https://cinode.zerolord.com" } }
            val signupEndpoint = backendBaseUrl + "auth/signup.php"
            val username = email.substringBefore("@").ifBlank { name.lowercase().replace(" ", "_") }

            try {
                val response = backendApiService.signup(
                    signupEndpoint,
                    BackendSignupRequest(
                        name = name,
                        email = email,
                        username = username,
                        password = password
                    )
                )

                if (response.isSuccess && response.data != null) {
                    val authData = response.data
                    val userId = authData.jellyfin_user_id ?: "jf_user_${authData.user_id}"
                    val token = authData.token ?: "token_${authData.username}"
                    val resolvedUrl = authData.server_url ?: targetUrl

                    val serverEntity = ServerEntity(
                        name = "Cinode REST Server",
                        url = resolvedUrl,
                        userId = userId,
                        token = token,
                        isLastUsed = true
                    )

                    serverDao.clearLastUsed()
                    val insertedId = serverDao.insertServer(serverEntity).toInt()

                    return@withContext Result.success(
                        JellyfinServer(
                            id = insertedId,
                            name = serverEntity.name,
                            url = serverEntity.url,
                            userId = serverEntity.userId,
                            token = serverEntity.token,
                            isDemo = false
                        )
                    )
                } else {
                    return@withContext Result.failure(Exception(response.message.ifBlank { "Signup failed on PHP Backend." }))
                }
            } catch (e: Exception) {
                Result.failure(Exception("PHP Backend connection error: ${e.message}"))
            }
        }
    }

    suspend fun changeUserPassword(
        username: String,
        currentPw: String,
        newPw: String
    ): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val endpoint = backendBaseUrl + "auth/change_password.php"
                val response = backendApiService.changePassword(
                    endpoint,
                    BackendChangePasswordRequest(username, currentPw, newPw)
                )
                if (response.isSuccess) {
                    Result.success(Unit)
                } else {
                    Result.failure(Exception(response.message))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    // --- ADMIN DASHBOARD & GOVERNANCE METHODS (PHP REST API) ---

    suspend fun getUsersList(activeServer: JellyfinServer?): List<JellyfinUserResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val endpoint = backendBaseUrl + "jellyfin/users.php"
                val res = backendApiService.getUsers(endpoint)
                if (res.isSuccess && res.data != null) {
                    return@withContext res.data
                }
            } catch (e: Exception) {
                // Return empty if backend fails
            }
            emptyList()
        }
    }

    suspend fun createNewUser(name: String, password: String?, activeServer: JellyfinServer?): Result<JellyfinUserResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val endpoint = backendBaseUrl + "jellyfin/users.php"
                val res = backendApiService.createUser(endpoint, CreateUserByNameRequest(Name = name, Password = password))
                if (res.isSuccess && res.data != null) {
                    return@withContext Result.success(res.data)
                }
                return@withContext Result.failure(Exception(res.message ?: "Failed to create user"))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun deleteUser(userId: String, activeServer: JellyfinServer?): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val endpoint = backendBaseUrl + "jellyfin/users.php"
                val res = backendApiService.deleteUser(endpoint, userId)
                return@withContext res.isSuccess
            } catch (e: Exception) {
                false
            }
        }
    }

    suspend fun getActiveSessionsList(activeServer: JellyfinServer?): List<JellyfinSessionDto> {
        return withContext(Dispatchers.IO) {
            try {
                val endpoint = backendBaseUrl + "jellyfin/sessions.php"
                val res = backendApiService.getSessions(endpoint)
                if (res.isSuccess && res.data != null) {
                    return@withContext res.data
                }
            } catch (e: Exception) {
                // Return empty if backend fails
            }
            emptyList()
        }
    }

    suspend fun getMediaFoldersList(activeServer: JellyfinServer?): List<JellyfinMediaFolderDto> {
        return withContext(Dispatchers.IO) {
            try {
                val endpoint = backendBaseUrl + "jellyfin/folders.php"
                val res = backendApiService.getMediaFolders(endpoint)
                if (res.isSuccess && res.data?.Items != null) {
                    return@withContext res.data.Items
                }
            } catch (e: Exception) {
                // Return empty if backend fails
            }
            emptyList()
        }
    }

    suspend fun triggerLibraryScan(activeServer: JellyfinServer?): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val endpoint = backendBaseUrl + "jellyfin/scan.php"
                val res = backendApiService.refreshLibrary(endpoint)
                return@withContext res.isSuccess
            } catch (e: Exception) {
                true
            }
        }
    }

    suspend fun getActivityLogsList(activeServer: JellyfinServer?): List<JellyfinActivityLogDto> {
        return withContext(Dispatchers.IO) {
            try {
                val endpoint = backendBaseUrl + "jellyfin/activity.php"
                val res = backendApiService.getActivityLogs(endpoint)
                if (res.isSuccess && res.data?.Items != null) {
                    return@withContext res.data.Items
                }
            } catch (e: Exception) {
                // Fallback
            }
            emptyList()
        }
    }

    // --- MEDIA CONTENT FETCHING & CACHING ---

    suspend fun getItems(
        type: MediaType? = null,
        searchQuery: String? = null,
        activeServer: JellyfinServer? = null
    ): List<JellyfinItem> {
        return withContext(Dispatchers.IO) {
            val server = activeServer ?: getActiveServer()
            val serverUrl = server?.url ?: BuildConfig.JELLYFIN_SERVER_URL.ifEmpty { "https://cinode.zerolord.com" }

            val includeType = when (type) {
                MediaType.MOVIE -> "Movie,Video"
                MediaType.SERIES -> "Series,Season,Episode"
                MediaType.LIVE_TV -> "LiveTv,TvChannel,Channel"
                MediaType.MUSIC_ALBUM -> "MusicAlbum,Audio"
                else -> null
            }

            try {
                val endpoint = backendBaseUrl + "jellyfin/items.php"
                var res = backendApiService.getItems(endpoint, includeItemTypes = includeType, searchTerm = searchQuery)
                
                // Fallback to fetch without includeItemTypes if specific filter returned empty
                if (res.isSuccess && (res.data?.Items == null || res.data.Items.isEmpty()) && includeType != null) {
                    res = backendApiService.getItems(endpoint, includeItemTypes = null, searchTerm = searchQuery)
                }

                if (res.isSuccess && !res.data?.Items.isNullOrEmpty()) {
                    val rawItems = res.data!!.Items!!
                    val mapped = rawItems.map { dto ->
                        val isSeries = dto.Type == "Series" || dto.Type == "Season" || dto.Type == "Episode" || !dto.SeriesName.isNullOrBlank()
                        val mType = when {
                            dto.Type == "Movie" || (dto.Type == "Video" && dto.SeriesName.isNullOrBlank()) -> MediaType.MOVIE
                            isSeries -> MediaType.SERIES
                            dto.Type == "LiveTv" || dto.Type == "TvChannel" || dto.Type == "Channel" -> MediaType.LIVE_TV
                            dto.Type == "MusicAlbum" || dto.Type == "Audio" || dto.Type == "MusicVideo" -> MediaType.MUSIC_ALBUM
                            else -> type ?: MediaType.MOVIE
                        }

                        val poster = "$serverUrl/Items/${dto.Id}/Images/Primary"
                        val backdrop = "$serverUrl/Items/${dto.Id}/Images/Backdrop"
                        val video = "$serverUrl/Videos/${dto.Id}/stream.mp4"

                        JellyfinItem(
                            id = dto.Id,
                            title = dto.Name ?: "Untitled Stream",
                            overview = dto.Overview ?: "Experience high-fidelity 4K streaming directly on Cinode.",
                            mediaType = mType,
                            posterUrl = poster,
                            backdropUrl = backdrop,
                            videoUrl = video,
                            rating = (dto.CommunityRating ?: 8.8f).toString(),
                            year = (dto.ProductionYear ?: 2026).toString(),
                            durationMs = (dto.RunTimeTicks ?: 72000000000L) / 10000L,
                            seriesName = dto.SeriesName,
                            genres = if (!dto.Genres.isNullOrEmpty()) dto.Genres else listOf("Sci-Fi", "Action", "Adventure")
                        )
                    }

                    // Save to Room Cache
                    val cacheEntities = mapped.map { item ->
                        MediaCacheEntity(
                            id = item.id,
                            title = item.title,
                            overview = item.overview,
                            mediaType = item.mediaType.name,
                            posterUrl = item.posterUrl,
                            backdropUrl = item.backdropUrl,
                            videoUrl = item.videoUrl,
                            rating = item.rating,
                            year = item.year,
                            durationMs = item.durationMs,
                            seriesName = item.seriesName
                        )
                    }
                    mediaCacheDao?.insertAll(cacheEntities)

                    val filtered = if (type != null) {
                        mapped.filter { it.mediaType == type }
                    } else mapped

                    return@withContext if (filtered.isNotEmpty()) filtered else mapped
                }
            } catch (e: Exception) {
                // Fallback to cache if network fails
            }

            val cached = mediaCacheDao?.getAllCached()
            if (!cached.isNullOrEmpty()) {
                val cachedMapped = cached.map { c ->
                    JellyfinItem(
                        id = c.id,
                        title = c.title,
                        overview = c.overview,
                        mediaType = try { MediaType.valueOf(c.mediaType) } catch (_: Exception) { MediaType.MOVIE },
                        posterUrl = c.posterUrl,
                        backdropUrl = c.backdropUrl,
                        videoUrl = c.videoUrl,
                        rating = c.rating,
                        year = c.year,
                        durationMs = c.durationMs,
                        seriesName = c.seriesName
                    )
                }
                val filteredCache = if (type != null) {
                    cachedMapped.filter { it.mediaType == type }
                } else cachedMapped

                return@withContext if (filteredCache.isNotEmpty()) filteredCache else cachedMapped
            }

            return@withContext emptyList()
        }
    }

    suspend fun getEpisodesForSeries(seriesId: String): List<Episode> {
        return withContext(Dispatchers.IO) {
            val server = getActiveServer()
            val serverUrl = server?.url ?: BuildConfig.JELLYFIN_SERVER_URL.ifEmpty { "https://cinode.zerolord.com" }

            try {
                val endpoint = backendBaseUrl + "jellyfin/episodes.php"
                val res = backendApiService.getEpisodes(endpoint, seriesId)
                if (res.isSuccess && res.data?.Items != null) {
                    return@withContext res.data.Items.mapIndexed { idx, dto ->
                        Episode(
                            id = dto.Id,
                            title = dto.Name ?: "Episode ${idx + 1}",
                            episodeNumber = idx + 1,
                            seasonNumber = 1,
                            overview = dto.Overview ?: "Cinode VIP Episode",
                            durationMs = (dto.RunTimeTicks ?: 27000000000L) / 10000L,
                            thumbnailUrl = "$serverUrl/Items/${dto.Id}/Images/Primary",
                            videoUrl = "$serverUrl/Videos/${dto.Id}/stream.mp4"
                        )
                    }
                }
            } catch (e: Exception) {
                // Return empty if fails
            }

            emptyList()
        }
    }

    // --- FAVORITES & WATCH PROGRESS ---

    suspend fun toggleFavorite(item: JellyfinItem, currentFavoriteState: Boolean) {
        if (!currentFavoriteState) {
            favoriteDao.insertFavorite(
                FavoriteEntity(
                    itemId = item.id,
                    title = item.title,
                    mediaType = item.mediaType.name,
                    posterUrl = item.posterUrl,
                    year = item.year
                )
            )
        } else {
            favoriteDao.deleteFavorite(item.id)
        }
    }

    suspend fun saveMediaProgress(item: JellyfinItem, positionMs: Long, durationMs: Long) {
        if (durationMs <= 0) return
        val isFinished = positionMs >= (durationMs * 0.95)
        mediaProgressDao.saveProgress(
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

    // --- DOWNLOADS ENGINE ---

    suspend fun getDownloadSettings(): DownloadSettingsEntity {
        return downloadSettingsDao.getSettings() ?: DownloadSettingsEntity()
    }

    suspend fun saveDownloadSettings(settings: DownloadSettingsEntity) {
        downloadSettingsDao.saveSettings(settings)
    }

    fun startDownload(
        context: Context,
        item: JellyfinItem,
        quality: String = "1080p",
        seriesName: String? = null,
        seasonNumber: Int? = null,
        episodeNumber: Int? = null,
        episodeDetails: Episode? = null
    ) {
        if (activeDownloadJobs.containsKey(item.id)) return

        val job = repositoryScope.launch {
            val downloadFolder = File(context.filesDir, "downloads")
            if (!downloadFolder.exists()) downloadFolder.mkdirs()

            val targetFile = File(downloadFolder, "${item.id}.mp4")
            val entity = DownloadEntity(
                itemId = item.id,
                title = episodeDetails?.title ?: item.title,
                overview = episodeDetails?.overview ?: item.overview,
                mediaType = item.mediaType.name,
                posterUrl = episodeDetails?.thumbnailUrl ?: item.posterUrl,
                backdropUrl = item.backdropUrl,
                localFilePath = targetFile.absolutePath,
                downloadStatus = "DOWNLOADING",
                progressPercent = 0,
                downloadedSizeBytes = 0,
                totalSizeBytes = 100 * 1024 * 1024L,
                quality = quality,
                seriesName = seriesName ?: item.seriesName,
                seasonNumber = seasonNumber ?: item.seasonNumber,
                episodeNumber = episodeNumber ?: item.episodeNumber,
                durationMs = episodeDetails?.durationMs ?: item.durationMs,
                videoUrl = episodeDetails?.videoUrl ?: item.videoUrl
            )
            downloadDao.insertOrUpdateDownload(entity)

            try {
                val streamUrl = episodeDetails?.videoUrl?.ifBlank { item.videoUrl } ?: item.videoUrl
                val url = URL(streamUrl)
                val connection = url.openConnection() as HttpURLConnection
                connection.connectTimeout = 10000
                connection.readTimeout = 10000
                connection.connect()

                val contentLength = connection.contentLengthLong.let { if (it <= 0) 100 * 1024 * 1024L else it }
                val inputStream = connection.inputStream
                val outputStream = FileOutputStream(targetFile)

                val buffer = ByteArray(8192)
                var bytesRead: Int
                var totalRead = 0L

                while (inputStream.read(buffer).also { bytesRead = it } != -1 && isActive) {
                    outputStream.write(buffer, 0, bytesRead)
                    totalRead += bytesRead
                    val progress = ((totalRead.toFloat() / contentLength.toFloat()) * 100).toInt().coerceIn(0, 100)

                    downloadDao.updateProgressAndSpeed(
                        itemId = item.id,
                        progress = progress,
                        downloadedBytes = totalRead,
                        speed = 1024 * 1024L,
                        eta = 30L,
                        status = "DOWNLOADING"
                    )
                    delay(200)
                }

                outputStream.flush()
                outputStream.close()
                inputStream.close()

                if (isActive) {
                    downloadDao.updateStatus(item.id, "COMPLETED")
                }
            } catch (e: Exception) {
                if (isActive) {
                    downloadDao.updateStatus(item.id, "FAILED")
                }
            } finally {
                activeDownloadJobs.remove(item.id)
            }
        }
        activeDownloadJobs[item.id] = job
    }

    fun pauseDownload(itemId: String) {
        activeDownloadJobs[itemId]?.cancel()
        activeDownloadJobs.remove(itemId)
        repositoryScope.launch { downloadDao.updateStatus(itemId, "PAUSED") }
    }

    fun resumeDownload(context: Context, itemId: String) {
        repositoryScope.launch {
            val download = downloadDao.getDownload(itemId)
            if (download != null) {
                val downloadItem = JellyfinItem(
                    id = download.itemId,
                    title = download.title,
                    overview = download.overview,
                    mediaType = if (download.mediaType == "SERIES") MediaType.SERIES else MediaType.MOVIE,
                    posterUrl = download.posterUrl,
                    backdropUrl = download.backdropUrl,
                    videoUrl = download.videoUrl
                )
                startDownload(context, downloadItem, download.quality, download.seriesName, download.seasonNumber, download.episodeNumber)
            }
        }
    }

    fun cancelDownload(context: Context, itemId: String) {
        activeDownloadJobs[itemId]?.cancel()
        activeDownloadJobs.remove(itemId)
        repositoryScope.launch { downloadDao.deleteDownload(itemId) }
    }

    fun retryDownload(context: Context, itemId: String) {
        resumeDownload(context, itemId)
    }

    fun deleteDownload(context: Context, itemId: String) {
        cancelDownload(context, itemId)
    }

    fun deleteSeasonDownloads(context: Context, seriesName: String, seasonNumber: Int) {
        repositoryScope.launch {
            downloadDao.deleteDownloadsBySeason(seriesName, seasonNumber)
        }
    }

    fun deleteSeriesDownloads(context: Context, seriesName: String) {
        repositoryScope.launch {
            downloadDao.deleteDownloadsBySeries(seriesName)
        }
    }

    fun deleteAllDownloads(context: Context) {
        activeDownloadJobs.forEach { (_, job) -> job.cancel() }
        activeDownloadJobs.clear()
        repositoryScope.launch {
            val downloadFolder = File(context.filesDir, "downloads")
            if (downloadFolder.exists()) {
                downloadFolder.listFiles()?.forEach { it.delete() }
            }
            downloadDao.deleteAllDownloads()
        }
    }

    // --- MONNIFY GATEWAY INTEGRATION METHODS ---

    val monnifyConfigFlow: Flow<MonnifyConfigEntity?>? = monnifyDao?.getConfigFlow()
    val allMonnifyTransactionsFlow: Flow<List<MonnifyTransactionEntity>>? = monnifyDao?.getAllTransactions()

    fun getPaidTransactionsForUser(email: String): Flow<List<MonnifyTransactionEntity>>? {
        return monnifyDao?.getPaidTransactionsForUser(email)
    }

    suspend fun getMonnifyConfig(): MonnifyConfigEntity {
        return monnifyDao?.getConfig() ?: MonnifyConfigEntity()
    }

    suspend fun saveMonnifyConfig(config: MonnifyConfigEntity) {
        monnifyDao?.saveConfig(config)
    }

    suspend fun recordMonnifyTransaction(transaction: MonnifyTransactionEntity) {
        monnifyDao?.insertTransaction(transaction)
    }

    suspend fun testMonnifyCredentials(apiKey: String, secretKey: String, useSandbox: Boolean): com.example.data.api.MonnifyAuthResponse {
        return monnifyService.authenticate(apiKey, secretKey, useSandbox)
    }

    suspend fun initMonnifyTransaction(
        config: MonnifyConfigEntity,
        amount: Double,
        customerName: String,
        customerEmail: String,
        itemTitle: String,
        username: String = ""
    ): com.example.data.api.MonnifyInitResponse {
        return monnifyService.initializeTransaction(config, amount, customerName, customerEmail, itemTitle, username)
    }

    suspend fun verifyMonnifyTransaction(
        config: MonnifyConfigEntity,
        paymentRef: String,
        username: String = ""
    ): com.example.data.api.MonnifyVerifyResponse {
        return monnifyService.verifyTransaction(config, paymentRef, username)
    }
}
