package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SettingsRemote
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.TvOff
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DeviceMode
import com.example.ui.theme.IconAmber
import com.example.ui.theme.IconBlue
import com.example.ui.theme.IconCyan
import com.example.ui.theme.IconGreen
import com.example.ui.theme.IconLime
import com.example.ui.theme.IconOrange
import com.example.ui.theme.IconPink
import com.example.ui.theme.IconPurple
import com.example.ui.theme.IconRed
import com.example.ui.theme.IconTeal
import com.example.ui.theme.IconYellow
import com.example.ui.theme.JellyfinBackground
import com.example.ui.theme.JellyfinCardBackground
import com.example.ui.theme.JellyfinRed
import com.example.ui.theme.JellyfinSurface
import com.example.ui.theme.JellyfinSurfaceVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary

fun NavDestination.getIconColor(): Color {
    return when (this) {
        NavDestination.HOME -> IconBlue
        NavDestination.MOVIES -> IconYellow
        NavDestination.TV_SHOWS -> IconOrange
        NavDestination.SEARCH -> IconCyan
        NavDestination.FAVORITES -> IconPink
        NavDestination.DOWNLOADS -> IconGreen
        NavDestination.PROFILE -> IconTeal
        NavDestination.SECURITY -> IconRed
        NavDestination.SETTINGS -> IconPurple
        NavDestination.AUTH -> IconAmber
        NavDestination.ONBOARDING -> IconLime
    }
}

enum class NavDestination(val title: String, val icon: ImageVector) {
    HOME("Home", Icons.Default.Home),
    MOVIES("TV & Movies", Icons.Default.Tv),
    TV_SHOWS("TV Series", Icons.Default.Movie),
    SEARCH("Search", Icons.Default.Search),
    FAVORITES("Favorites", Icons.Default.BookmarkBorder),
    DOWNLOADS("Downloads", Icons.Default.FileDownload),
    PROFILE("Profile", Icons.Default.PersonOutline),
    SECURITY("Security", Icons.Default.Shield),
    SETTINGS("Settings", Icons.AutoMirrored.Filled.ExitToApp),
    AUTH("Sign In", Icons.Default.Lock),
    ONBOARDING("Tour", Icons.Default.Star)
}

@Composable
fun CinodeTopBar(
    activeServerName: String,
    deviceMode: DeviceMode,
    isTvRemoteVisible: Boolean,
    onToggleDeviceMode: () -> Unit,
    onToggleTvRemote: () -> Unit,
    onOpenServerConnect: () -> Unit,
    onOpenDrawer: () -> Unit = {},
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = JellyfinSurface.copy(alpha = 0.95f),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Menu Icon + Brand Logo & Server Badge
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onOpenDrawer,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("topbar_menu_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Menu",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(2.dp))

                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .background(JellyfinRed, CircleShape)
                        .clickable { onOpenDrawer() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "C",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                Column(
                    modifier = Modifier.clickable { onOpenDrawer() }
                ) {
                    Text(
                        text = "CINODE",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp,
                        letterSpacing = 1.sp
                    )
                    // Server status badge
                    TvFocusableCard(
                        onClick = onOpenServerConnect,
                        testTag = "server_badge"
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .background(JellyfinSurfaceVariant, RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(Color(0xFF4CAF50), CircleShape)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = activeServerName,
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted,
                                fontSize = 9.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Search Bar (Responsive Width)
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = { Text("Search...", color = TextMuted, fontSize = 11.sp) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(16.dp)
                    )
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = JellyfinRed,
                    unfocusedBorderColor = JellyfinSurfaceVariant,
                    focusedContainerColor = JellyfinCardBackground,
                    unfocusedContainerColor = JellyfinCardBackground,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .weight(1f, fill = false)
                    .widthIn(min = 80.dp, max = 160.dp)
                    .height(40.dp)
                    .testTag("topbar_search_input")
            )

            Spacer(modifier = Modifier.width(6.dp))

            // Mode & Remote controls
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // TV / Phone Mode Switcher
                TvFocusableCard(
                    onClick = onToggleDeviceMode,
                    testTag = "toggle_device_mode"
                ) {
                    Surface(
                        color = if (deviceMode == DeviceMode.SMART_TV_DPAD) JellyfinRed.copy(alpha = 0.3f) else JellyfinSurfaceVariant,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.padding(2.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = if (deviceMode == DeviceMode.SMART_TV_DPAD) Icons.Default.Tv else Icons.Default.TvOff,
                                contentDescription = "Mode",
                                tint = if (deviceMode == DeviceMode.SMART_TV_DPAD) JellyfinRed else TextMuted,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(4.dp))

                // On-Screen Remote Toggle
                TvFocusableCard(
                    onClick = onToggleTvRemote,
                    testTag = "toggle_tv_remote"
                ) {
                    Surface(
                        color = if (isTvRemoteVisible) JellyfinRed.copy(alpha = 0.3f) else JellyfinSurfaceVariant,
                        shape = CircleShape,
                        modifier = Modifier.size(34.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.SettingsRemote,
                                contentDescription = "TV Remote",
                                tint = if (isTvRemoteVisible) JellyfinRed else Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun JellyfinTopBar(
    activeServerName: String,
    deviceMode: DeviceMode,
    isTvRemoteVisible: Boolean,
    onToggleDeviceMode: () -> Unit,
    onToggleTvRemote: () -> Unit,
    onOpenServerConnect: () -> Unit,
    onOpenDrawer: () -> Unit = {},
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    CinodeTopBar(
        activeServerName = activeServerName,
        deviceMode = deviceMode,
        isTvRemoteVisible = isTvRemoteVisible,
        onToggleDeviceMode = onToggleDeviceMode,
        onToggleTvRemote = onToggleTvRemote,
        onOpenServerConnect = onOpenServerConnect,
        onOpenDrawer = onOpenDrawer,
        searchQuery = searchQuery,
        onSearchQueryChange = onSearchQueryChange,
        modifier = modifier
    )
}

/**
 * Sidebar Navigation Drawer matching screenshot design
 */
@Composable
fun CinodeDrawerContent(
    currentDestination: NavDestination,
    activeServerName: String = "Demo Server",
    isAdmin: Boolean = false,
    onNavigate: (NavDestination) -> Unit = {},
    onOpenServerConnect: () -> Unit = {},
    onCloseDrawer: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        // Narrow Dark Black Sidebar Rail on the left
        Surface(
            color = Color(0xFF0D0D0D),
            modifier = Modifier
                .fillMaxHeight()
                .width(76.dp)
        ) {
            Row(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(1f)
                        .padding(vertical = 24.dp, horizontal = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    NavDestination.entries.forEach { destination ->
                        if (destination == NavDestination.SETTINGS) return@forEach
                        if (destination == NavDestination.SECURITY && !isAdmin) return@forEach
                        val isSelected = currentDestination == destination ||
                            (destination == NavDestination.MOVIES && currentDestination == NavDestination.TV_SHOWS)

                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isSelected) destination.getIconColor() else destination.getIconColor().copy(alpha = 0.15f))
                                .clickable {
                                    onNavigate(destination)
                                    onCloseDrawer()
                                }
                                .testTag("drawer_icon_${destination.name.lowercase()}"),
                            contentAlignment = Alignment.Center
                        ) {
                            val iconColor = if (isSelected) Color.White else destination.getIconColor()
                            Icon(
                                imageVector = destination.icon,
                                contentDescription = destination.title,
                                tint = iconColor,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // Exit / Logout Icon at bottom
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .clickable {
                                onOpenServerConnect()
                                onCloseDrawer()
                            }
                            .testTag("drawer_exit_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Exit",
                            tint = Color.White.copy(alpha = 0.65f),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // Vertical Divider Line matching attached screenshot
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(1.dp)
                        .background(Color.White.copy(alpha = 0.12f))
                )
            }
        }

        // Red Floating Close Action Button (FAB) at Bottom Right of Screen (Matches attached screenshot)
        FloatingActionButton(
            onClick = onCloseDrawer,
            shape = CircleShape,
            containerColor = JellyfinRed,
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .size(60.dp)
                .testTag("drawer_close_fab")
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close Drawer",
                modifier = Modifier.size(30.dp)
            )
        }
    }
}

@Composable
fun JellyfinDrawerContent(
    currentDestination: NavDestination,
    activeServerName: String = "Demo Server",
    isAdmin: Boolean = false,
    onNavigate: (NavDestination) -> Unit = {},
    onOpenServerConnect: () -> Unit = {},
    onCloseDrawer: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    CinodeDrawerContent(
        currentDestination = currentDestination,
        activeServerName = activeServerName,
        isAdmin = isAdmin,
        onNavigate = onNavigate,
        onOpenServerConnect = onOpenServerConnect,
        onCloseDrawer = onCloseDrawer,
        modifier = modifier
    )
}

@Composable
fun CinodeTvNavRail(
    currentDestination: NavDestination,
    isAdmin: Boolean = false,
    onNavigate: (NavDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color(0xFF0D0D0D),
        modifier = modifier
            .fillMaxHeight()
            .width(76.dp)
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f)
                    .padding(vertical = 24.dp, horizontal = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                NavDestination.entries.forEach { destination ->
                    if (destination == NavDestination.SETTINGS) return@forEach
                    if (destination == NavDestination.SECURITY && !isAdmin) return@forEach
                    val isSelected = currentDestination == destination ||
                        (destination == NavDestination.MOVIES && currentDestination == NavDestination.TV_SHOWS)

                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) destination.getIconColor() else destination.getIconColor().copy(alpha = 0.15f))
                            .clickable { onNavigate(destination) }
                            .testTag("rail_icon_${destination.name.lowercase()}"),
                        contentAlignment = Alignment.Center
                    ) {
                        val iconColor = if (isSelected) Color.White else destination.getIconColor()
                        Icon(
                            imageVector = destination.icon,
                            contentDescription = destination.title,
                            tint = iconColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onNavigate(NavDestination.SETTINGS) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                        contentDescription = "Exit",
                        tint = Color.White.copy(alpha = 0.65f),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(1.dp)
                    .background(Color.White.copy(alpha = 0.12f))
            )
        }
    }
}

@Composable
fun JellyfinTvNavRail(
    currentDestination: NavDestination,
    isAdmin: Boolean = false,
    onNavigate: (NavDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    CinodeTvNavRail(
        currentDestination = currentDestination,
        isAdmin = isAdmin,
        onNavigate = onNavigate,
        modifier = modifier
    )
}

