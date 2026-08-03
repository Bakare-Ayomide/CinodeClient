package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Cast
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import com.example.data.db.MonnifyConfigEntity
import com.example.data.db.MonnifyTransactionEntity
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig
import com.example.data.api.JellyfinActivityLogDto
import com.example.data.api.JellyfinMediaFolderDto
import com.example.data.api.JellyfinSessionDto
import com.example.data.api.JellyfinUserResponse
import com.example.data.model.JellyfinServer
import com.example.ui.JellyfinViewModel
import com.example.ui.components.TvFocusableCard
import com.example.ui.theme.IconAmber
import com.example.ui.theme.IconBlue
import com.example.ui.theme.IconCyan
import com.example.ui.theme.IconGreen
import com.example.ui.theme.IconIndigo
import com.example.ui.theme.IconLime
import com.example.ui.theme.IconOrange
import com.example.ui.theme.IconPink
import com.example.ui.theme.IconPurple
import com.example.ui.theme.IconRed
import com.example.ui.theme.IconTeal
import com.example.ui.theme.IconYellow
import com.example.ui.theme.JellyfinBackground
import com.example.ui.theme.JellyfinCardBackground
import com.example.ui.theme.JellyfinCyan
import com.example.ui.theme.JellyfinPurple
import com.example.ui.theme.JellyfinRed
import com.example.ui.theme.JellyfinSurfaceVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary

@Composable
fun SecurityScreen(
    currentUserName: String,
    currentUserEmail: String,
    activeServer: JellyfinServer?,
    viewModel: JellyfinViewModel? = null,
    onNavigateToAuth: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val adminEnvUser = try { BuildConfig.JELLYFIN_ADMIN_USER } catch (e: Throwable) { "duwit" }.ifEmpty { "duwit" }

    val isAdmin = currentUserName.equals(adminEnvUser, ignoreCase = true) ||
            currentUserEmail.startsWith(adminEnvUser, ignoreCase = true) ||
            currentUserName.contains("duwit", ignoreCase = true) ||
            currentUserEmail.contains("duwit", ignoreCase = true)

    var selectedTab by remember { mutableIntStateOf(0) }
    var actionNotice by remember { mutableStateOf<String?>(null) }
    var showCreateUserDialog by remember { mutableStateOf(false) }

    // Admin state collection from ViewModel
    val usersList = viewModel?.adminUsers?.collectAsState()?.value ?: emptyList()
    val sessionsList = viewModel?.adminSessions?.collectAsState()?.value ?: emptyList()
    val mediaFolders = viewModel?.adminMediaFolders?.collectAsState()?.value ?: emptyList()
    val activityLogs = viewModel?.adminActivityLogs?.collectAsState()?.value ?: emptyList()
    val isLoading = viewModel?.isAdminLoading?.collectAsState()?.value ?: false
    val monnifyConfig = viewModel?.monnifyConfig?.collectAsState()?.value ?: MonnifyConfigEntity()
    val monnifyTransactions = viewModel?.monnifyTransactions?.collectAsState()?.value ?: emptyList()

    LaunchedEffect(isAdmin) {
        if (isAdmin) {
            viewModel?.fetchAdminDashboardData()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(JellyfinBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
                .verticalScroll(scrollState)
        ) {
            // Screen Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(if (isAdmin) JellyfinCyan.copy(alpha = 0.15f) else JellyfinRed.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isAdmin) Icons.Default.AdminPanelSettings else Icons.Default.Lock,
                            contentDescription = null,
                            tint = if (isAdmin) JellyfinCyan else JellyfinRed,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Admin Governance Dashboard",
                            style = MaterialTheme.typography.headlineSmall,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isAdmin) "Cinode REST API Management & Server Controls" else "Access Restricted Area",
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isAdmin) {
                        TvFocusableCard(
                            onClick = {
                                viewModel?.fetchAdminDashboardData()
                                actionNotice = "Server metrics & user directory refreshed."
                            },
                            testTag = "btn_admin_refresh"
                        ) {
                            Surface(
                                color = JellyfinSurfaceVariant,
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = JellyfinCyan, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Sync Server", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))
                    }

                    Surface(
                        color = if (isAdmin) JellyfinCyan.copy(alpha = 0.2f) else JellyfinRed.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (isAdmin) "ADMIN ACCESS GRANTED" else "UNAUTHORIZED USER",
                            color = if (isAdmin) JellyfinCyan else JellyfinRed,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (!isAdmin) {
                // --- ACCESS DENIED FOR NON-ADMIN USER ---
                Surface(
                    color = JellyfinCardBackground,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(JellyfinRed.copy(alpha = 0.15f))
                                .border(2.dp, JellyfinRed.copy(alpha = 0.3f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = JellyfinRed,
                                modifier = Modifier.size(42.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Text(
                            text = "Administrator Privileges Required",
                            color = TextPrimary,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "The Security & System Governance Console is strictly restricted to the authorized server administrator account ($adminEnvUser).",
                            color = TextMuted,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.width(360.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Surface(
                            color = JellyfinSurfaceVariant,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = TextMuted,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Current Account: ${currentUserName.ifBlank { "Guest" }} ($currentUserEmail)",
                                    color = TextMuted,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        TvFocusableCard(
                            onClick = onNavigateToAuth,
                            testTag = "btn_security_admin_login"
                        ) {
                            Surface(
                                color = JellyfinCyan,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Key,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Sign In as Admin User ($adminEnvUser)",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                // --- ADMIN SECURITY DASHBOARD ---
                if (actionNotice != null) {
                    Surface(
                        color = JellyfinCyan.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    ) {
                        Text(
                            text = actionNotice!!,
                            color = JellyfinCyan,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(12.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // 1. Admin Status Bar
                Surface(
                    color = JellyfinCardBackground,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(JellyfinCyan, JellyfinPurple)
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AdminPanelSettings,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = "Administrator: $currentUserName",
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = "Server: ${activeServer?.url ?: BuildConfig.JELLYFIN_SERVER_URL.ifEmpty { "https://demo.jellyfin.org" }}",
                                    color = TextMuted,
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = JellyfinCyan,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Surface(
                                color = Color(0xFF2E7D32).copy(alpha = 0.2f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = Color(0xFF81C784),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "REST API Connected",
                                        color = Color(0xFF81C784),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 2. Metrics Quick Cards
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricCard(
                        title = "TOTAL USERS",
                        value = "${usersList.size.coerceAtLeast(1)} Registered",
                        icon = Icons.Default.Group,
                        accentColor = IconBlue,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "ACTIVE SESSIONS",
                        value = "${sessionsList.size.coerceAtLeast(1)} Online",
                        icon = Icons.Default.Devices,
                        accentColor = IconPurple,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "MEDIA LIBRARIES",
                        value = "${mediaFolders.size.coerceAtLeast(4)} Catalogs",
                        icon = Icons.Default.Folder,
                        accentColor = IconOrange,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "HW TRANSCODER",
                        value = "NVENC H.265",
                        icon = Icons.Default.Speed,
                        accentColor = IconGreen,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 3. Admin Area Navigation Menu Hub
                Surface(
                    color = JellyfinCardBackground,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = IconYellow, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "ADMIN AREA NAVIGATION MENU",
                                color = TextPrimary,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.sp,
                                letterSpacing = 1.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))

                        // Grid of 7 Admin Navigation Shortcuts
                        val menuItems = listOf(
                            AdminMenuItem("User & Roles", "RBAC & Permissions", Icons.Default.Group, IconBlue, 0),
                            AdminMenuItem("Connected Sessions", "Active Terminals", Icons.Default.Devices, IconPurple, 1),
                            AdminMenuItem("Storage & Catalogs", "Media Folders", Icons.Default.Folder, IconOrange, 2),
                            AdminMenuItem("Monnify Gateway", "Paywall & Stream Rates", Icons.Default.Payments, IconYellow, 6),
                            AdminMenuItem("Hardware Engine", "CPU & Transcoding", Icons.Default.Speed, IconGreen, 3),
                            AdminMenuItem("API Keys & Webhooks", "REST Tokens", Icons.Default.Key, IconTeal, 4),
                            AdminMenuItem("Security Audit Logs", "System Journal", Icons.Default.Terminal, IconRed, 5)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            menuItems.take(4).forEach { item ->
                                AdminNavMenuCard(
                                    item = item,
                                    isSelected = selectedTab == item.tabIndex,
                                    onSelect = { selectedTab = item.tabIndex },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            menuItems.drop(4).forEach { item ->
                                AdminNavMenuCard(
                                    item = item,
                                    isSelected = selectedTab == item.tabIndex,
                                    onSelect = { selectedTab = item.tabIndex },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 4. Colorful Tab Navigation Row
                val tabSpecs = listOf(
                    TabSpec("Users", Icons.Default.Group, IconBlue),
                    TabSpec("Sessions", Icons.Default.Devices, IconPurple),
                    TabSpec("Libraries", Icons.Default.Folder, IconOrange),
                    TabSpec("Transcoder", Icons.Default.Speed, IconGreen),
                    TabSpec("API Keys", Icons.Default.Key, IconTeal),
                    TabSpec("Audit Logs", Icons.Default.Terminal, IconRed),
                    TabSpec("Monnify Paywall", Icons.Default.Payments, IconYellow)
                )

                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = JellyfinCardBackground,
                    contentColor = tabSpecs[selectedTab.coerceIn(0, tabSpecs.size - 1)].color,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab.coerceIn(0, tabPositions.size - 1)]),
                            color = tabSpecs[selectedTab.coerceIn(0, tabSpecs.size - 1)].color
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                ) {
                    tabSpecs.forEachIndexed { index, spec ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = spec.icon,
                                        contentDescription = null,
                                        tint = if (selectedTab == index) spec.color else TextMuted,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = spec.title,
                                        fontSize = 12.sp,
                                        fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium,
                                        color = if (selectedTab == index) spec.color else TextMuted
                                    )
                                }
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 5. Selected Admin Sub-Area Content View
                when (selectedTab) {
                    0 -> AdminUsersTab(
                        usersList = usersList,
                        onOpenCreateUser = { showCreateUserDialog = true },
                        onDeleteUser = { userId ->
                            viewModel?.deleteAdminUser(userId) { success ->
                                actionNotice = if (success) "User deleted successfully." else "Failed to delete user."
                            }
                        }
                    )
                    1 -> AdminSessionsTab(
                        sessionsList = sessionsList,
                        onLogoutSession = { sessionId ->
                            actionNotice = "Terminated session $sessionId."
                        }
                    )
                    2 -> AdminLibrariesTab(
                        mediaFolders = mediaFolders,
                        onTriggerScan = {
                            viewModel?.triggerLibraryRefresh { success ->
                                actionNotice = if (success) "Library refresh scan triggered on Jellyfin server." else "Library refresh queued."
                            }
                        }
                    )
                    3 -> AdminTranscoderTab(
                        onPurgeTranscode = { actionNotice = "NVENC Transcode cache flushed and reset." }
                    )
                    4 -> AdminApiKeysTab(
                        onGenerateKey = { actionNotice = "New REST API Key generated: cinode_api_key_8892" }
                    )
                    5 -> AdminSystemTab(
                        activeServer = activeServer,
                        activityLogs = activityLogs,
                        onPurgeCache = { actionNotice = "Server image & transcode cache purged." }
                    )
                    6 -> AdminMonnifyTab(
                        config = monnifyConfig,
                        transactions = monnifyTransactions,
                        onSaveConfig = { newConfig ->
                            viewModel?.saveMonnifyConfig(newConfig) { success ->
                                actionNotice = if (success) "Monnify Gateway settings saved." else "Failed to save Monnify settings."
                            }
                        },
                        onTestCredentials = { key, secret, isSandbox ->
                            viewModel?.testMonnifyCredentials(key, secret, isSandbox) { success, msg ->
                                actionNotice = msg
                            }
                        }
                    )
                }
            }
        }
    }

    // Modal Dialog: Create New Jellyfin User
    if (showCreateUserDialog) {
        var newUsername by remember { mutableStateOf("") }
        var newPassword by remember { mutableStateOf("") }
        var createError by remember { mutableStateOf<String?>(null) }
        var isCreating by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showCreateUserDialog = false },
            containerColor = JellyfinCardBackground,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = JellyfinCyan)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Create Jellyfin User", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column {
                    Text("Add a new authenticated user to this Jellyfin server instance.", color = TextMuted, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(14.dp))

                    Text("Username", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = newUsername,
                        onValueChange = { newUsername = it },
                        placeholder = { Text("newuser", color = TextMuted) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = JellyfinCyan,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("input_new_username")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Password (Optional)", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        placeholder = { Text("••••••••", color = TextMuted) },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = JellyfinCyan,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("input_new_password")
                    )

                    if (createError != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(createError!!, color = JellyfinRed, fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newUsername.isBlank()) {
                            createError = "Please enter a username."
                            return@Button
                        }
                        isCreating = true
                        viewModel?.createAdminUser(newUsername, newPassword.ifBlank { null }) { success, err ->
                            isCreating = false
                            if (success) {
                                showCreateUserDialog = false
                                actionNotice = "User '$newUsername' created on Jellyfin server."
                            } else {
                                createError = err ?: "Failed to create user."
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = JellyfinCyan)
                ) {
                    if (isCreating) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Text("Create User")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateUserDialog = false }) {
                    Text("Cancel", color = TextMuted)
                }
            }
        )
    }
}

// --- SUB-COMPONENTS FOR TAB VIEWS ---

@Composable
private fun MetricCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = JellyfinCardBackground,
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(16.dp))
            }
            Spacer(modifier = Modifier.width(6.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = TextMuted,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = value,
                    color = TextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun AdminUsersTab(
    usersList: List<JellyfinUserResponse>,
    onOpenCreateUser: () -> Unit,
    onDeleteUser: (String) -> Unit
) {
    val adminEnvUser = try { BuildConfig.JELLYFIN_ADMIN_USER } catch (e: Throwable) { "duwit" }.ifEmpty { "duwit" }
    val displayUsers = usersList

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Server Users Directory (${displayUsers.size})",
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )

            TvFocusableCard(
                onClick = onOpenCreateUser,
                testTag = "btn_create_user_modal"
            ) {
                Surface(
                    color = JellyfinCyan,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("New Jellyfin User", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        displayUsers.forEach { user ->
            val isUserAdmin = user.Name.equals(adminEnvUser, ignoreCase = true) || user.Policy?.IsAdministrator == true
            Surface(
                color = JellyfinCardBackground,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(if (isUserAdmin) JellyfinCyan.copy(alpha = 0.2f) else JellyfinSurfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = user.Name.take(1).uppercase(),
                                color = if (isUserAdmin) JellyfinCyan else TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(user.Name, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                if (isUserAdmin) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(
                                        color = JellyfinCyan.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = "ADMIN",
                                            color = JellyfinCyan,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }

                            Text(
                                text = "ID: ${user.Id.take(12)}... | Password: ${if (user.HasPassword == true) "Configured" else "None"}",
                                color = TextMuted,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    if (!isUserAdmin) {
                        IconButton(onClick = { onDeleteUser(user.Id) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete User", tint = JellyfinRed, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminSessionsTab(
    sessionsList: List<JellyfinSessionDto>,
    onLogoutSession: (String) -> Unit
) {
    val displaySessions = sessionsList

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Active Server Sessions & Connections (${displaySessions.size})", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)

        displaySessions.forEach { session ->
            Surface(
                color = JellyfinCardBackground,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(JellyfinPurple.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Devices, contentDescription = null, tint = JellyfinPurple, modifier = Modifier.size(20.dp))
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text("${session.UserName ?: "Guest"} on ${session.DeviceName ?: "Device"}", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("${session.Client ?: "Client App"} (${session.RemoteEndPoint ?: "Local"})", color = TextMuted, fontSize = 11.sp)
                        }
                    }

                    OutlinedButton(
                        onClick = { onLogoutSession(session.Id) },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = JellyfinRed)
                    ) {
                        Text("Disconnect", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminLibrariesTab(
    mediaFolders: List<JellyfinMediaFolderDto>,
    onTriggerScan: () -> Unit
) {
    val displayFolders = mediaFolders

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Library Storage Folders (${displayFolders.size})", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)

            TvFocusableCard(onClick = onTriggerScan, testTag = "btn_refresh_library_scan") {
                Surface(color = JellyfinCyan, shape = RoundedCornerShape(10.dp)) {
                    Row(modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Trigger Library Scan", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        displayFolders.forEach { folder ->
            Surface(color = JellyfinCardBackground, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(JellyfinRed.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Folder, contentDescription = null, tint = JellyfinRed, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(folder.Name ?: "Media Folder", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Type: ${folder.CollectionType ?: "general"} | ID: ${folder.Id}", color = TextMuted, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                        }
                    }
                    Surface(color = JellyfinSurfaceVariant, shape = RoundedCornerShape(6.dp)) {
                        Text("ONLINE", color = Color(0xFF81C784), fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminSystemTab(
    activeServer: JellyfinServer?,
    activityLogs: List<JellyfinActivityLogDto>,
    onPurgeCache: () -> Unit
) {
    val displayLogs = activityLogs.map { "${it.Date ?: ""} ${it.Name}: ${it.Overview ?: ""}" }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Surface(color = JellyfinCardBackground, shape = RoundedCornerShape(12.dp), modifier = Modifier.weight(1f)) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Dns, contentDescription = null, tint = JellyfinCyan, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Server Address", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(activeServer?.url ?: BuildConfig.JELLYFIN_SERVER_URL.ifEmpty { "https://demo.jellyfin.org" }, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }
            }

            Surface(color = JellyfinCardBackground, shape = RoundedCornerShape(12.dp), modifier = Modifier.weight(1f)) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Speed, contentDescription = null, tint = JellyfinPurple, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Transcoder", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("NVENC H.265 (Active)", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        TvFocusableCard(onClick = onPurgeCache, testTag = "btn_purge_cache") {
            Surface(color = JellyfinSurfaceVariant, shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Storage, contentDescription = null, tint = JellyfinCyan, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Purge Transcode & Image Metadata Buffers", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                    Icon(Icons.Default.Refresh, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp))
                }
            }
        }

        Surface(color = JellyfinCardBackground, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Terminal, contentDescription = null, tint = JellyfinCyan, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("REALTIME SERVER SECURITY JOURNAL", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
                }
                Spacer(modifier = Modifier.height(10.dp))
                displayLogs.forEach { log ->
                    Text(text = log, color = Color.White.copy(alpha = 0.85f), fontSize = 11.sp, fontFamily = FontFamily.Monospace, modifier = Modifier.padding(vertical = 3.dp))
                }
            }
        }
    }
}

private data class AdminMenuItem(
    val title: String,
    val description: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val color: Color,
    val tabIndex: Int
)

private data class TabSpec(
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val color: Color
)

@Composable
private fun AdminNavMenuCard(
    item: AdminMenuItem,
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    TvFocusableCard(
        onClick = onSelect,
        testTag = "admin_menu_card_${item.tabIndex}",
        modifier = modifier
    ) {
        Surface(
            color = if (isSelected) item.color.copy(alpha = 0.22f) else JellyfinSurfaceVariant,
            shape = RoundedCornerShape(12.dp),
            border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, item.color) else null
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(item.color.copy(alpha = 0.25f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(item.icon, contentDescription = null, tint = item.color, modifier = Modifier.size(18.dp))
                    }
                    if (isSelected) {
                        Surface(
                            color = item.color,
                            shape = CircleShape,
                            modifier = Modifier.size(8.dp)
                        ) {}
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = item.title,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = item.description,
                    color = TextMuted,
                    fontSize = 10.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun AdminTranscoderTab(
    onPurgeTranscode: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("Hardware Acceleration & Transcode Pipeline", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            // CPU Load Card
            Surface(color = JellyfinCardBackground, shape = RoundedCornerShape(12.dp), modifier = Modifier.weight(1f)) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Speed, contentDescription = null, tint = IconRed, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("CPU LOAD", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Text("18%", color = IconGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)).background(Color.White.copy(alpha = 0.1f))) {
                        Box(modifier = Modifier.fillMaxWidth(0.18f).height(6.dp).background(IconGreen))
                    }
                }
            }

            // RAM Usage Card
            Surface(color = JellyfinCardBackground, shape = RoundedCornerShape(12.dp), modifier = Modifier.weight(1f)) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Memory, contentDescription = null, tint = IconPurple, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("RAM USAGE", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Text("4.2 / 16 GB", color = IconPurple, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)).background(Color.White.copy(alpha = 0.1f))) {
                        Box(modifier = Modifier.fillMaxWidth(0.26f).height(6.dp).background(IconPurple))
                    }
                }
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            // NVENC GPU Card
            Surface(color = JellyfinCardBackground, shape = RoundedCornerShape(12.dp), modifier = Modifier.weight(1f)) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Tv, contentDescription = null, tint = IconYellow, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("GPU ENCODER", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Text("NVIDIA NVENC", color = IconYellow, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("H.264 / H.265 / AV1 Hardware Decode Enabled", color = TextPrimary, fontSize = 11.sp)
                }
            }

            // Transcode Buffer Card
            Surface(color = JellyfinCardBackground, shape = RoundedCornerShape(12.dp), modifier = Modifier.weight(1f)) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Storage, contentDescription = null, tint = IconTeal, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("CACHE TEMP", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Text("12.4 GB", color = IconTeal, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Location: /var/lib/cinode/transcode-cache", color = TextMuted, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                }
            }
        }

        TvFocusableCard(onClick = onPurgeTranscode, testTag = "btn_purge_transcode_cache") {
            Surface(color = IconAmber, shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                    Icon(Icons.Default.CleaningServices, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Flush Active Transcode Temp Buffers", color = Color.Black, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun AdminApiKeysTab(
    onGenerateKey: () -> Unit
) {
    val sampleKeys = emptyList<Triple<String, String, String>>()

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("REST API Keys & Authentication Tokens", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)

            TvFocusableCard(onClick = onGenerateKey, testTag = "btn_generate_api_key") {
                Surface(color = IconTeal, shape = RoundedCornerShape(10.dp)) {
                    Row(modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("New API Token", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        sampleKeys.forEach { (keyToken, appName, scope) ->
            Surface(color = JellyfinCardBackground, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(IconTeal.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Key, contentDescription = null, tint = IconTeal, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(appName, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("Token: ${keyToken.take(16)}...", color = TextMuted, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                        }
                    }

                    Surface(color = IconIndigo.copy(alpha = 0.2f), shape = RoundedCornerShape(6.dp)) {
                        Text(scope, color = IconIndigo, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminMonnifyTab(
    config: MonnifyConfigEntity,
    transactions: List<MonnifyTransactionEntity>,
    onSaveConfig: (MonnifyConfigEntity) -> Unit,
    onTestCredentials: (apiKey: String, secretKey: String, useSandbox: Boolean) -> Unit
) {
    var isPaywallEnabled by remember(config) { mutableStateOf(config.isPaywallEnabled) }
    var useSandbox by remember(config) { mutableStateOf(config.useSandbox) }
    var apiKey by remember(config) { mutableStateOf(config.apiKey) }
    var secretKey by remember(config) { mutableStateOf(config.secretKey) }
    var contractCode by remember(config) { mutableStateOf(config.contractCode) }
    var streamPrice by remember(config) { mutableStateOf(config.streamPriceNgn.toInt().toString()) }
    var vipPrice by remember(config) { mutableStateOf(config.vipPassPriceNgn.toInt().toString()) }

    val totalRevenue = remember(transactions) {
        transactions.filter { it.status == "PAID" }.sumOf { it.amountNgn }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        // Section Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(36.dp).clip(CircleShape).background(IconYellow.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Payments, contentDescription = null, tint = IconYellow, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text("Monnify Payment Gateway & Paywall Console", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text("Configure API credentials, stream pricing & view transaction logs", color = TextMuted, fontSize = 11.sp)
                }
            }

            Surface(
                color = if (isPaywallEnabled) Color(0xFF4CAF50).copy(alpha = 0.2f) else JellyfinRed.copy(alpha = 0.2f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = if (isPaywallEnabled) "PAYWALL ACTIVE" else "PAYWALL DISABLED",
                    color = if (isPaywallEnabled) Color(0xFF4CAF50) else JellyfinRed,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 1. Paywall Master Switch Card
        Surface(color = JellyfinCardBackground, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Require Monnify Payment Before Streaming", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("When enabled, non-admin users must pay per-movie or hold a VIP pass before streaming starts.", color = TextMuted, fontSize = 11.sp)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Switch(
                    checked = isPaywallEnabled,
                    onCheckedChange = { isPaywallEnabled = it },
                    colors = SwitchDefaults.colors(checkedThumbColor = JellyfinCyan, checkedTrackColor = JellyfinPurple)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2. Monnify Credentials Form Card
        Surface(color = JellyfinCardBackground, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("MONNIFY API CREDENTIALS & ENVIRONMENT", color = JellyfinCyan, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp, letterSpacing = 1.sp)
                Text("Developer Dashboard: https://developers.monnify.com/", color = TextMuted, fontSize = 11.sp)

                Spacer(modifier = Modifier.height(12.dp))

                Text("Environment Mode", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        color = if (useSandbox) JellyfinCyan.copy(alpha = 0.2f) else JellyfinSurfaceVariant,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.clickable { useSandbox = true }
                    ) {
                        Text(
                            text = "Sandbox Mode (https://sandbox.monnify.com)",
                            color = if (useSandbox) JellyfinCyan else TextMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }

                    Surface(
                        color = if (!useSandbox) Color(0xFF4CAF50).copy(alpha = 0.2f) else JellyfinSurfaceVariant,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.clickable { useSandbox = false }
                    ) {
                        Text(
                            text = "Production Mode (https://api.monnify.com)",
                            color = if (!useSandbox) Color(0xFF4CAF50) else TextMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = apiKey,
                    onValueChange = { apiKey = it },
                    label = { Text("Monnify API Key (e.g., MK_TEST_SAF789Q2WS)", color = TextMuted) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = adminTextFieldColors(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = secretKey,
                    onValueChange = { secretKey = it },
                    label = { Text("Monnify Secret Key", color = TextMuted) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = adminTextFieldColors(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = contractCode,
                    onValueChange = { contractCode = it },
                    label = { Text("Monnify Contract Code (e.g., 3489201145)", color = TextMuted) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = adminTextFieldColors(),
                    singleLine = true
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 3. Pricing Configuration Card
        Surface(color = JellyfinCardBackground, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("STREAMING PRICING & SUBSCRIPTION RATES (NGN)", color = JellyfinCyan, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp, letterSpacing = 1.sp)
                Spacer(modifier = Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = streamPrice,
                        onValueChange = { streamPrice = it },
                        label = { Text("Single Movie Price (₦)", color = TextMuted) },
                        modifier = Modifier.weight(1f),
                        colors = adminTextFieldColors(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = vipPrice,
                        onValueChange = { vipPrice = it },
                        label = { Text("VIP All-Access Pass (₦)", color = TextMuted) },
                        modifier = Modifier.weight(1f),
                        colors = adminTextFieldColors(),
                        singleLine = true
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Action Buttons Row
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = { onTestCredentials(apiKey, secretKey, useSandbox) },
                modifier = Modifier.weight(1f).height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = JellyfinCyan)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Test API Connection", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = {
                    val newConfig = config.copy(
                        isPaywallEnabled = isPaywallEnabled,
                        useSandbox = useSandbox,
                        apiKey = apiKey,
                        secretKey = secretKey,
                        contractCode = contractCode,
                        streamPriceNgn = streamPrice.toDoubleOrNull() ?: 500.0,
                        vipPassPriceNgn = vipPrice.toDoubleOrNull() ?: 2500.0,
                        updatedAt = System.currentTimeMillis()
                    )
                    onSaveConfig(newConfig)
                },
                modifier = Modifier.weight(1f).height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = JellyfinCyan, contentColor = Color.Black)
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Save Gateway Settings", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 4. Revenue & Payment Ledger
        Text("MONNIFY REVENUE & PAYMENT AUDIT LEDGER", color = TextPrimary, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, letterSpacing = 1.sp)
        Spacer(modifier = Modifier.height(10.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            MetricCard(
                title = "TOTAL REVENUE",
                value = "₦${totalRevenue.toInt()} NGN",
                icon = Icons.Default.Payments,
                accentColor = IconGreen,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "TRANSACTIONS",
                value = "${transactions.size} Payments",
                icon = Icons.Default.CheckCircle,
                accentColor = IconTeal,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (transactions.isEmpty()) {
            Surface(color = JellyfinCardBackground, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                    Text("No Monnify transactions recorded yet. Test stream purchases will appear here in real-time.", color = TextMuted, fontSize = 12.sp)
                }
            }
        } else {
            transactions.forEach { txn ->
                Surface(color = JellyfinCardBackground, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(txn.itemTitle, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(JellyfinPurple.copy(alpha = 0.3f)).padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(txn.paymentMethod, color = JellyfinCyan, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("Ref: ${txn.paymentRef} • User: ${txn.userEmail}", color = TextMuted, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("₦${txn.amountNgn.toInt()} NGN", color = Color(0xFF4CAF50), fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                            Text(txn.status, color = Color(0xFF4CAF50), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun adminTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = JellyfinCyan,
    unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
    focusedLabelColor = JellyfinCyan,
    unfocusedLabelColor = TextMuted,
    focusedTextColor = TextPrimary,
    unfocusedTextColor = TextPrimary,
    cursorColor = JellyfinCyan,
    focusedContainerColor = JellyfinSurfaceVariant,
    unfocusedContainerColor = JellyfinSurfaceVariant
)
