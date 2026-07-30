package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.db.DownloadEntity
import com.example.data.db.MediaProgressEntity
import com.example.data.model.DeviceMode
import com.example.data.model.Episode
import com.example.data.model.JellyfinItem
import com.example.data.model.JellyfinServer
import com.example.data.model.MediaType
import com.example.data.repository.DemoMediaProvider
import com.example.data.repository.JellyfinRepository
import com.example.ui.components.NavDestination
import com.example.ui.screens.LibraryFilter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class JellyfinUiState(
    val currentDestination: NavDestination = NavDestination.HOME,
    val selectedFilter: LibraryFilter = LibraryFilter.ALL,
    val searchQuery: String = "",
    val activeServer: JellyfinServer? = null,
    val deviceMode: DeviceMode = DeviceMode.PHONE_TOUCH,
    val isTvRemoteVisible: Boolean = false,
    val isServerConnectModalOpen: Boolean = false,
    val selectedItem: JellyfinItem? = null,
    val playingItem: JellyfinItem? = null,
    val isLoading: Boolean = false,
    val serverErrorMessage: String? = null,
    val isAuthenticated: Boolean = false,
    val currentUserEmail: String = "",
    val currentUserName: String = "",
    val isOnboardingCompleted: Boolean = false,
    val showOnboardingFlow: Boolean = false,
    val showAuthFlow: Boolean = true
)

class JellyfinViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = JellyfinRepository(
        db.serverDao(),
        db.favoriteDao(),
        db.mediaProgressDao(),
        db.downloadDao()
    )

    private val _uiState = MutableStateFlow(JellyfinUiState())
    val uiState: StateFlow<JellyfinUiState> = _uiState.asStateFlow()

    val savedServers: StateFlow<List<JellyfinServer>> = repository.savedServers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val continueWatching: StateFlow<List<MediaProgressEntity>> = repository.continueWatching
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allDownloads: StateFlow<List<DownloadEntity>> = repository.allDownloads
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _allItems = MutableStateFlow<List<JellyfinItem>>(emptyList())
    val allItems: StateFlow<List<JellyfinItem>> = _allItems.asStateFlow()

    private val _movies = MutableStateFlow<List<JellyfinItem>>(emptyList())
    val movies: StateFlow<List<JellyfinItem>> = _movies.asStateFlow()

    private val _series = MutableStateFlow<List<JellyfinItem>>(emptyList())
    val series: StateFlow<List<JellyfinItem>> = _series.asStateFlow()

    private val _liveTv = MutableStateFlow<List<JellyfinItem>>(emptyList())
    val liveTv: StateFlow<List<JellyfinItem>> = _liveTv.asStateFlow()

    private val _albums = MutableStateFlow<List<JellyfinItem>>(emptyList())
    val albums: StateFlow<List<JellyfinItem>> = _albums.asStateFlow()

    private val _heroItem = MutableStateFlow<JellyfinItem>(DemoMediaProvider.heroItem)
    val heroItem: StateFlow<JellyfinItem> = _heroItem.asStateFlow()

    val episodes = DemoMediaProvider.sampleEpisodes

    private val _filteredLibraryItems = MutableStateFlow<List<JellyfinItem>>(emptyList())
    val filteredLibraryItems: StateFlow<List<JellyfinItem>> = _filteredLibraryItems.asStateFlow()

    private val prefs = application.getSharedPreferences("cinode_user_session_prefs", Context.MODE_PRIVATE)

    init {
        restoreUserSession()
        loadInitialServer()
    }

    private fun restoreUserSession() {
        val isAuth = prefs.getBoolean("is_authenticated", false)
        val email = prefs.getString("user_email", "") ?: ""
        val name = prefs.getString("user_name", "") ?: ""
        val onboardingDone = prefs.getBoolean("is_onboarding_completed", false)

        if (isAuth) {
            _uiState.value = _uiState.value.copy(
                isAuthenticated = true,
                currentUserEmail = email,
                currentUserName = name.ifBlank { "Jellyfin User" },
                showAuthFlow = false,
                showOnboardingFlow = false,
                isOnboardingCompleted = onboardingDone,
                currentDestination = NavDestination.HOME
            )
        } else if (onboardingDone) {
            _uiState.value = _uiState.value.copy(
                isOnboardingCompleted = true,
                showOnboardingFlow = false,
                showAuthFlow = true
            )
        }
    }

    private fun saveUserSession(email: String, name: String) {
        prefs.edit()
            .putBoolean("is_authenticated", true)
            .putString("user_email", email)
            .putString("user_name", name)
            .apply()
    }

    private fun clearUserSession() {
        prefs.edit()
            .putBoolean("is_authenticated", false)
            .putString("user_email", "")
            .putString("user_name", "")
            .apply()
    }

    private fun loadInitialServer() {
        viewModelScope.launch {
            val lastServer = repository.getLastUsedServer()
            if (lastServer != null && lastServer.token.isNotBlank()) {
                val server = JellyfinServer(
                    id = lastServer.id,
                    name = lastServer.name,
                    url = lastServer.url,
                    userId = lastServer.userId,
                    token = lastServer.token,
                    isDemo = lastServer.isDemo
                )
                _uiState.value = _uiState.value.copy(activeServer = server)
                loadAllServerItems(server)
            } else {
                _uiState.value = _uiState.value.copy(isLoading = true)
                val result = repository.connectToServer("", "", "")
                result.onSuccess { connectedServer ->
                    _uiState.value = _uiState.value.copy(
                        activeServer = connectedServer,
                        isLoading = false
                    )
                    loadAllServerItems(connectedServer)
                }.onFailure {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    loadAllServerItems(null)
                }
            }
        }
    }

    fun loadAllServerItems(server: JellyfinServer? = _uiState.value.activeServer) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val fetchedAll = repository.getItems(type = null, searchQuery = "", activeServer = server)
            val fetchedMovies = repository.getItems(type = MediaType.MOVIE, searchQuery = "", activeServer = server)
            val fetchedSeries = repository.getItems(type = MediaType.SERIES, searchQuery = "", activeServer = server)
            val fetchedLiveTv = repository.getItems(type = MediaType.LIVE_TV, searchQuery = "", activeServer = server)
            val fetchedAlbums = repository.getItems(type = MediaType.MUSIC_ALBUM, searchQuery = "", activeServer = server)

            _allItems.value = fetchedAll
            _movies.value = if (fetchedMovies.isNotEmpty()) fetchedMovies else fetchedAll.filter { it.mediaType == MediaType.MOVIE }
            _series.value = if (fetchedSeries.isNotEmpty()) fetchedSeries else fetchedAll.filter { it.mediaType == MediaType.SERIES }
            _liveTv.value = if (fetchedLiveTv.isNotEmpty()) fetchedLiveTv else fetchedAll.filter { it.mediaType == MediaType.LIVE_TV }
            _albums.value = if (fetchedAlbums.isNotEmpty()) fetchedAlbums else fetchedAll.filter { it.mediaType == MediaType.MUSIC_ALBUM || it.mediaType == MediaType.MUSIC_TRACK }

            _heroItem.value = _movies.value.firstOrNull() ?: _allItems.value.firstOrNull() ?: DemoMediaProvider.heroItem

            _uiState.value = _uiState.value.copy(isLoading = false)
            updateLibraryFilter()
        }
    }

    fun navigateTo(destination: NavDestination) {
        _uiState.value = _uiState.value.copy(
            currentDestination = destination,
            selectedItem = null
        )
    }

    fun setFilter(filter: LibraryFilter) {
        _uiState.value = _uiState.value.copy(selectedFilter = filter)
        updateLibraryFilter()
    }

    fun setSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        updateLibraryFilter()
    }

    private fun updateLibraryFilter() {
        viewModelScope.launch {
            val filter = _uiState.value.selectedFilter
            val query = _uiState.value.searchQuery
            var items = repository.getItems(filter.mediaType, query, _uiState.value.activeServer)

            if (filter == LibraryFilter.FAVORITES) {
                items = _allItems.value.filter { it.isFavorite }
            }

            _filteredLibraryItems.value = items
        }
    }

    fun toggleDeviceMode() {
        val newMode = if (_uiState.value.deviceMode == DeviceMode.PHONE_TOUCH) {
            DeviceMode.SMART_TV_DPAD
        } else {
            DeviceMode.PHONE_TOUCH
        }
        _uiState.value = _uiState.value.copy(deviceMode = newMode)
    }

    fun toggleTvRemote() {
        _uiState.value = _uiState.value.copy(isTvRemoteVisible = !_uiState.value.isTvRemoteVisible)
    }

    fun openServerConnectModal() {
        _uiState.value = _uiState.value.copy(isServerConnectModalOpen = true)
    }

    fun closeServerConnectModal() {
        _uiState.value = _uiState.value.copy(isServerConnectModalOpen = false)
    }

    fun selectItem(item: JellyfinItem) {
        _uiState.value = _uiState.value.copy(selectedItem = item)
    }

    fun getItemDetails(id: String): JellyfinItem? {
        return _allItems.value.find { it.id == id } ?: _movies.value.find { it.id == id } ?: _heroItem.value
    }

    fun clearSelectedItem() {
        _uiState.value = _uiState.value.copy(selectedItem = null)
    }

    fun playMedia(item: JellyfinItem) {
        _uiState.value = _uiState.value.copy(playingItem = item)
    }

    fun playEpisode(episode: Episode) {
        val epItem = JellyfinItem(
            id = episode.id,
            title = episode.title,
            overview = episode.overview,
            mediaType = MediaType.EPISODE,
            posterUrl = episode.thumbnailUrl,
            backdropUrl = episode.thumbnailUrl,
            videoUrl = episode.videoUrl,
            durationMs = episode.durationMs
        )
        _uiState.value = _uiState.value.copy(playingItem = epItem)
    }

    fun stopPlayback() {
        _uiState.value = _uiState.value.copy(playingItem = null)
    }

    fun connectServer(url: String, user: String, pass: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, serverErrorMessage = null)
            val result = repository.connectToServer(url, user, pass)
            result.onSuccess { server ->
                _uiState.value = _uiState.value.copy(
                    activeServer = server,
                    isLoading = false,
                    isServerConnectModalOpen = false
                )
                loadAllServerItems(server)
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    serverErrorMessage = err.message ?: "Failed to connect to Jellyfin server"
                )
            }
        }
    }

    fun selectSavedServer(serverId: Int) {
        viewModelScope.launch {
            repository.selectServer(serverId)
            val serverEntity = repository.getLastUsedServer()
            if (serverEntity != null) {
                val server = JellyfinServer(
                    id = serverEntity.id,
                    name = serverEntity.name,
                    url = serverEntity.url,
                    userId = serverEntity.userId,
                    token = serverEntity.token,
                    isDemo = serverEntity.isDemo
                )
                _uiState.value = _uiState.value.copy(
                    activeServer = server,
                    isServerConnectModalOpen = false
                )
                loadAllServerItems(server)
            }
        }
    }

    fun deleteSavedServer(serverId: Int) {
        viewModelScope.launch {
            repository.deleteServer(serverId)
        }
    }

    fun toggleFavorite(item: JellyfinItem) {
        viewModelScope.launch {
            repository.toggleFavorite(item, item.isFavorite)
        }
    }

    fun saveMediaProgress(item: JellyfinItem, positionMs: Long, durationMs: Long) {
        viewModelScope.launch {
            repository.saveMediaProgress(item, positionMs, durationMs)
        }
    }

    fun startDownload(item: JellyfinItem) {
        viewModelScope.launch {
            repository.startDownload(getApplication(), item)
        }
    }

    fun deleteDownload(itemId: String) {
        viewModelScope.launch {
            repository.deleteDownload(getApplication(), itemId)
        }
    }

    fun login(email: String, name: String) {
        val finalName = name.ifBlank { "Jellyfin User" }
        saveUserSession(email, finalName)
        _uiState.value = _uiState.value.copy(
            isAuthenticated = true,
            currentUserEmail = email,
            currentUserName = finalName,
            showAuthFlow = false,
            showOnboardingFlow = false,
            currentDestination = NavDestination.HOME
        )
    }

    fun loginWithServer(username: String, pass: String, serverUrl: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, serverErrorMessage = null)
            val res = repository.connectToServer(serverUrl, username, pass)
            res.fold(
                onSuccess = { server ->
                    val email = if (username.contains("@")) username else "$username@jellyfin.local"
                    val name = username.takeWhile { it != '@' }.ifBlank { "Jellyfin User" }
                    saveUserSession(email, name)
                    _uiState.value = _uiState.value.copy(
                        activeServer = server,
                        isAuthenticated = true,
                        currentUserEmail = email,
                        currentUserName = name,
                        showAuthFlow = false,
                        showOnboardingFlow = false,
                        isLoading = false,
                        currentDestination = NavDestination.HOME
                    )
                    loadAllServerItems(server)
                    onResult(true, null)
                },
                onFailure = { err ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        serverErrorMessage = err.localizedMessage ?: "Failed to authenticate with server"
                    )
                    onResult(false, err.localizedMessage ?: "Failed to authenticate")
                }
            )
        }
    }

    fun signupWithServer(name: String, email: String, pass: String, serverUrl: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, serverErrorMessage = null)
            val res = repository.signupUser(name, email, pass, serverUrl)
            res.fold(
                onSuccess = { server ->
                    val finalName = name.ifBlank { "Jellyfin User" }
                    saveUserSession(email, finalName)
                    _uiState.value = _uiState.value.copy(
                        activeServer = server,
                        isAuthenticated = true,
                        currentUserEmail = email,
                        currentUserName = finalName,
                        showAuthFlow = false,
                        showOnboardingFlow = false,
                        isLoading = false,
                        currentDestination = NavDestination.HOME
                    )
                    loadAllServerItems(server)
                    onResult(true, null)
                },
                onFailure = { err ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        serverErrorMessage = err.localizedMessage ?: "Failed to register user"
                    )
                    onResult(false, err.localizedMessage ?: "Failed to register user")
                }
            )
        }
    }

    fun loginGuest() {
        login("guest@jellyfin.demo", "Guest User")
    }

    private val _adminUsers = MutableStateFlow<List<com.example.data.api.JellyfinUserResponse>>(emptyList())
    val adminUsers: StateFlow<List<com.example.data.api.JellyfinUserResponse>> = _adminUsers.asStateFlow()

    private val _adminSessions = MutableStateFlow<List<com.example.data.api.JellyfinSessionDto>>(emptyList())
    val adminSessions: StateFlow<List<com.example.data.api.JellyfinSessionDto>> = _adminSessions.asStateFlow()

    private val _adminMediaFolders = MutableStateFlow<List<com.example.data.api.JellyfinMediaFolderDto>>(emptyList())
    val adminMediaFolders: StateFlow<List<com.example.data.api.JellyfinMediaFolderDto>> = _adminMediaFolders.asStateFlow()

    private val _adminActivityLogs = MutableStateFlow<List<com.example.data.api.JellyfinActivityLogDto>>(emptyList())
    val adminActivityLogs: StateFlow<List<com.example.data.api.JellyfinActivityLogDto>> = _adminActivityLogs.asStateFlow()

    private val _isAdminLoading = MutableStateFlow(false)
    val isAdminLoading: StateFlow<Boolean> = _isAdminLoading.asStateFlow()

    fun fetchAdminDashboardData() {
        viewModelScope.launch {
            _isAdminLoading.value = true
            val activeServer = _uiState.value.activeServer
            val users = repository.getUsersList(activeServer)
            val sessions = repository.getActiveSessionsList(activeServer)
            val folders = repository.getMediaFoldersList(activeServer)
            val logs = repository.getActivityLogsList(activeServer)

            _adminUsers.value = users
            _adminSessions.value = sessions
            _adminMediaFolders.value = folders
            _adminActivityLogs.value = logs
            _isAdminLoading.value = false
        }
    }

    fun createAdminUser(name: String, pass: String?, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            _isAdminLoading.value = true
            val result = repository.createNewUser(name, pass, _uiState.value.activeServer)
            _isAdminLoading.value = false
            if (result.isSuccess) {
                fetchAdminDashboardData()
                onResult(true, null)
            } else {
                onResult(false, result.exceptionOrNull()?.message ?: "Failed to create user on Jellyfin server")
            }
        }
    }

    fun deleteAdminUser(userId: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            _isAdminLoading.value = true
            val success = repository.deleteUser(userId, _uiState.value.activeServer)
            _isAdminLoading.value = false
            if (success) {
                fetchAdminDashboardData()
            }
            onResult(success)
        }
    }

    fun triggerLibraryRefresh(onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            _isAdminLoading.value = true
            val success = repository.triggerLibraryScan(_uiState.value.activeServer)
            _isAdminLoading.value = false
            onResult(success)
        }
    }

    fun logout() {
        clearUserSession()
        _uiState.value = _uiState.value.copy(
            isAuthenticated = false,
            showAuthFlow = true,
            showOnboardingFlow = false
        )
    }

    fun finishOnboarding() {
        prefs.edit().putBoolean("is_onboarding_completed", true).apply()
        _uiState.value = _uiState.value.copy(
            isOnboardingCompleted = true,
            showOnboardingFlow = false,
            showAuthFlow = !_uiState.value.isAuthenticated
        )
    }

    fun openOnboarding() {
        _uiState.value = _uiState.value.copy(
            showOnboardingFlow = true,
            showAuthFlow = false
        )
    }

    fun openAuth() {
        _uiState.value = _uiState.value.copy(
            showAuthFlow = true,
            showOnboardingFlow = false
        )
    }
}

typealias CinodeViewModel = JellyfinViewModel
typealias CinodeUiState = JellyfinUiState
