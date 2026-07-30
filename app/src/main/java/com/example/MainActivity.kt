package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.DeviceMode
import com.example.ui.JellyfinUiState
import com.example.ui.JellyfinViewModel
import com.example.ui.components.JellyfinDrawerContent
import com.example.ui.components.JellyfinTopBar
import com.example.ui.components.JellyfinTvNavRail
import com.example.ui.components.NavDestination
import com.example.ui.components.TvRemoteOverlay
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.DetailScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LibraryFilter
import com.example.ui.screens.LibraryScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.PlayerScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.ServerConnectScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.JellyfinBackground
import com.example.ui.theme.JellyfinTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: JellyfinViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            JellyfinTheme {
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                val savedServers by viewModel.savedServers.collectAsStateWithLifecycle()
                val continueWatching by viewModel.continueWatching.collectAsStateWithLifecycle()
                val filteredItems by viewModel.filteredLibraryItems.collectAsStateWithLifecycle()
                val allItems by viewModel.allItems.collectAsStateWithLifecycle()
                val heroItem by viewModel.heroItem.collectAsStateWithLifecycle()
                val movies by viewModel.movies.collectAsStateWithLifecycle()
                val series by viewModel.series.collectAsStateWithLifecycle()
                val allDownloads by viewModel.allDownloads.collectAsStateWithLifecycle()

                JellyfinAppContent(
                    uiState = uiState,
                    savedServers = savedServers,
                    continueWatching = continueWatching,
                    filteredItems = filteredItems,
                    allItems = allItems,
                    heroItem = heroItem,
                    movies = movies,
                    series = series,
                    allDownloads = allDownloads,
                    viewModel = viewModel
                )
            }
        }
    }
}

@Composable
fun JellyfinAppContent(
    uiState: JellyfinUiState,
    savedServers: List<com.example.data.model.JellyfinServer>,
    continueWatching: List<com.example.data.db.MediaProgressEntity>,
    filteredItems: List<com.example.data.model.JellyfinItem>,
    allItems: List<com.example.data.model.JellyfinItem>,
    heroItem: com.example.data.model.JellyfinItem,
    movies: List<com.example.data.model.JellyfinItem>,
    series: List<com.example.data.model.JellyfinItem>,
    allDownloads: List<com.example.data.db.DownloadEntity>,
    viewModel: JellyfinViewModel
) {
    // 1. Fullscreen Video Player Mode
    if (uiState.playingItem != null) {
        PlayerScreen(
            item = uiState.playingItem,
            onBack = { viewModel.stopPlayback() },
            onSaveProgress = { pos, dur ->
                viewModel.saveMediaProgress(uiState.playingItem, pos, dur)
            }
        )
        return
    }

    // 2. Fullscreen Onboarding Flow Mode
    if (uiState.showOnboardingFlow || uiState.currentDestination == NavDestination.ONBOARDING) {
        OnboardingScreen(
            onFinishOnboarding = { viewModel.finishOnboarding() }
        )
        return
    }

    // 3. Fullscreen Auth Screen Flow Mode (Login & Signup)
    if (uiState.showAuthFlow || uiState.currentDestination == NavDestination.AUTH || !uiState.isAuthenticated) {
        AuthScreen(
            onLoginSubmit = { user, pass, url, callback ->
                viewModel.loginWithServer(user, pass, url, callback)
            },
            onSignupSubmit = { name, email, pass, url, callback ->
                viewModel.signupWithServer(name, email, pass, url, callback)
            },
            onGuestLogin = { viewModel.loginGuest() },
            onOpenOnboarding = { viewModel.openOnboarding() }
        )
        return
    }

    // 4. Fullscreen Server Connection Modal
    if (uiState.isServerConnectModalOpen) {
        ServerConnectScreen(
            savedServers = savedServers,
            activeServer = uiState.activeServer,
            onConnectServer = { url, user, pass ->
                viewModel.connectServer(url, user, pass)
            },
            onSelectServer = { serverId ->
                viewModel.selectSavedServer(serverId)
            },
            onDeleteServer = { serverId ->
                viewModel.deleteSavedServer(serverId)
            },
            onClose = { viewModel.closeServerConnectModal() },
            isLoading = uiState.isLoading,
            errorMessage = uiState.serverErrorMessage
        )
        return
    }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val adminEnvUser = com.example.BuildConfig.JELLYFIN_ADMIN_USER.ifEmpty { "duwit" }
    val isAdminUser = uiState.currentUserName.equals(adminEnvUser, ignoreCase = true) ||
            uiState.currentUserEmail.startsWith(adminEnvUser, ignoreCase = true) ||
            uiState.currentUserName.contains("duwit", ignoreCase = true) ||
            uiState.currentUserEmail.contains("duwit", ignoreCase = true)

    // 3. Main Jellyfin Client Application with Sideways Drawer Navigation
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            JellyfinDrawerContent(
                currentDestination = uiState.currentDestination,
                isAdmin = isAdminUser,
                onNavigate = { dest ->
                    viewModel.navigateTo(dest)
                    scope.launch { drawerState.close() }
                },
                onCloseDrawer = { scope.launch { drawerState.close() } }
            )
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(JellyfinBackground)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Top Bar
                JellyfinTopBar(
                    activeServerName = uiState.activeServer?.name ?: "Demo Server",
                    deviceMode = uiState.deviceMode,
                    isTvRemoteVisible = uiState.isTvRemoteVisible,
                    onToggleDeviceMode = { viewModel.toggleDeviceMode() },
                    onToggleTvRemote = { viewModel.toggleTvRemote() },
                    onOpenServerConnect = { viewModel.openServerConnectModal() },
                    onOpenDrawer = { scope.launch { drawerState.open() } },
                    searchQuery = uiState.searchQuery,
                    onSearchQueryChange = { query -> viewModel.setSearchQuery(query) }
                )

                // Layout based on Device Mode & Screen Size (Adaptive for Mobile, Laptop, Desktop, and Smart TV)
                val configuration = androidx.compose.ui.platform.LocalConfiguration.current
                val isWideScreen = configuration.screenWidthDp >= 600 || uiState.deviceMode == DeviceMode.SMART_TV_DPAD

                if (isWideScreen) {
                    Row(modifier = Modifier.weight(1f)) {
                        JellyfinTvNavRail(
                            currentDestination = uiState.currentDestination,
                            isAdmin = isAdminUser,
                            onNavigate = { dest -> viewModel.navigateTo(dest) }
                        )

                        Box(modifier = Modifier.weight(1f)) {
                            MainScreenContent(
                                uiState = uiState,
                                continueWatching = continueWatching,
                                filteredItems = filteredItems,
                                allItems = allItems,
                                heroItem = heroItem,
                                movies = movies,
                                series = series,
                                allDownloads = allDownloads,
                                viewModel = viewModel,
                                onOpenDrawer = { scope.launch { drawerState.open() } }
                            )
                        }
                    }
                } else {
                    Box(modifier = Modifier.weight(1f)) {
                        MainScreenContent(
                            uiState = uiState,
                            continueWatching = continueWatching,
                            filteredItems = filteredItems,
                            allItems = allItems,
                            heroItem = heroItem,
                            movies = movies,
                            series = series,
                            allDownloads = allDownloads,
                            viewModel = viewModel,
                            onOpenDrawer = { scope.launch { drawerState.open() } }
                        )
                    }
                }
            }

            // Floating TV D-Pad Remote Controller Overlay
            if (uiState.isTvRemoteVisible) {
                TvRemoteOverlay(
                    onNavigateUp = {},
                    onNavigateDown = {},
                    onNavigateLeft = {},
                    onNavigateRight = {},
                    onSelect = {},
                    onBack = {
                        if (uiState.selectedItem != null) {
                            viewModel.clearSelectedItem()
                        }
                    },
                    onClose = { viewModel.toggleTvRemote() },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 16.dp, bottom = 24.dp)
                )
            }
        }
    }
}

@Composable
fun MainScreenContent(
    uiState: JellyfinUiState,
    continueWatching: List<com.example.data.db.MediaProgressEntity>,
    filteredItems: List<com.example.data.model.JellyfinItem>,
    allItems: List<com.example.data.model.JellyfinItem>,
    heroItem: com.example.data.model.JellyfinItem,
    movies: List<com.example.data.model.JellyfinItem>,
    series: List<com.example.data.model.JellyfinItem>,
    allDownloads: List<com.example.data.db.DownloadEntity>,
    viewModel: JellyfinViewModel,
    onOpenDrawer: () -> Unit = {}
) {
    // If an item is selected, show DetailScreen
    if (uiState.selectedItem != null) {
        val downloadState = allDownloads.find { it.itemId == uiState.selectedItem.id }
        DetailScreen(
            item = uiState.selectedItem,
            episodes = viewModel.episodes,
            isFavorite = uiState.selectedItem.isFavorite,
            downloadState = downloadState,
            onBack = { viewModel.clearSelectedItem() },
            onPlay = { item -> viewModel.playMedia(item) },
            onPlayEpisode = { episode -> viewModel.playEpisode(episode) },
            onToggleFavorite = { viewModel.toggleFavorite(uiState.selectedItem) },
            onStartDownload = { item -> viewModel.startDownload(item) },
            onDeleteDownload = { itemId -> viewModel.deleteDownload(itemId) }
        )
        return
    }

    // Screen Router based on Navigation Destination
    when (uiState.currentDestination) {
        NavDestination.HOME -> {
            HomeScreen(
                heroItem = heroItem,
                continueWatchingList = continueWatching,
                movies = movies,
                series = series,
                allItems = allItems,
                onMediaClick = { item -> viewModel.selectItem(item) },
                onPlayMedia = { item -> viewModel.playMedia(item) },
                onProgressClick = { progress ->
                    viewModel.getItemDetails(progress.itemId)?.let { item ->
                        viewModel.playMedia(item)
                    }
                },
                onNavigateToFilter = { filter ->
                    viewModel.setFilter(filter)
                    when (filter) {
                        LibraryFilter.MOVIES -> viewModel.navigateTo(NavDestination.MOVIES)
                        LibraryFilter.SERIES -> viewModel.navigateTo(NavDestination.TV_SHOWS)
                        else -> viewModel.navigateTo(NavDestination.FAVORITES)
                    }
                },
                onOpenDrawer = onOpenDrawer
            )
        }

        NavDestination.MOVIES -> {
            LibraryScreen(
                itemsList = if (movies.isNotEmpty()) movies else filteredItems,
                selectedFilter = LibraryFilter.MOVIES,
                onFilterSelect = { filter -> viewModel.setFilter(filter) },
                onMediaClick = { item -> viewModel.selectItem(item) }
            )
        }

        NavDestination.TV_SHOWS -> {
            LibraryScreen(
                itemsList = if (series.isNotEmpty()) series else filteredItems,
                selectedFilter = LibraryFilter.SERIES,
                onFilterSelect = { filter -> viewModel.setFilter(filter) },
                onMediaClick = { item -> viewModel.selectItem(item) }
            )
        }

        NavDestination.FAVORITES -> {
            LibraryScreen(
                itemsList = filteredItems,
                selectedFilter = LibraryFilter.FAVORITES,
                onFilterSelect = { filter -> viewModel.setFilter(filter) },
                onMediaClick = { item -> viewModel.selectItem(item) }
            )
        }

        NavDestination.DOWNLOADS -> {
            com.example.ui.screens.DownloadsScreen(
                downloads = allDownloads,
                onPlayMedia = { item -> viewModel.playMedia(item) },
                onDeleteDownload = { itemId -> viewModel.deleteDownload(itemId) },
                onExploreLibrary = { viewModel.navigateTo(NavDestination.MOVIES) }
            )
        }

        NavDestination.PROFILE -> {
            ProfileScreen(
                userName = uiState.currentUserName,
                userEmail = uiState.currentUserEmail,
                activeServer = uiState.activeServer,
                onOpenOnboarding = { viewModel.openOnboarding() },
                onSignOut = { viewModel.logout() }
            )
        }

        NavDestination.SETTINGS -> {
            SettingsScreen(
                activeServer = uiState.activeServer,
                deviceMode = uiState.deviceMode,
                onToggleDeviceMode = { viewModel.toggleDeviceMode() },
                onOpenServerConnect = { viewModel.openServerConnectModal() }
            )
        }

        NavDestination.SECURITY -> {
            com.example.ui.screens.SecurityScreen(
                currentUserName = uiState.currentUserName,
                currentUserEmail = uiState.currentUserEmail,
                activeServer = uiState.activeServer,
                viewModel = viewModel,
                onNavigateToAuth = { viewModel.openAuth() }
            )
        }

        else -> {
            LibraryScreen(
                itemsList = filteredItems,
                selectedFilter = uiState.selectedFilter,
                onFilterSelect = { filter -> viewModel.setFilter(filter) },
                onMediaClick = { item -> viewModel.selectItem(item) }
            )
        }
    }
}
