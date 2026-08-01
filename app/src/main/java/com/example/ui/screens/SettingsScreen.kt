package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.SettingsRemote
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DeviceMode
import com.example.data.model.JellyfinServer
import com.example.ui.components.TvFocusableCard
import com.example.ui.theme.JellyfinBackground
import com.example.ui.theme.JellyfinCardBackground
import com.example.ui.theme.JellyfinCyan
import com.example.ui.theme.JellyfinPurple
import com.example.ui.theme.JellyfinSurfaceVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary

import com.example.data.db.DownloadSettingsEntity
import com.example.ui.components.SmartDownloadSettingsDialog
import androidx.compose.material.icons.filled.Download

@Composable
fun SettingsScreen(
    activeServer: JellyfinServer?,
    deviceMode: DeviceMode,
    onToggleDeviceMode: () -> Unit,
    onOpenServerConnect: () -> Unit,
    downloadSettings: DownloadSettingsEntity = DownloadSettingsEntity(),
    onUpdateDownloadSettings: (DownloadSettingsEntity) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var defaultQuality by remember { mutableStateOf("4K Ultra HD") }
    var showSmartSettingsDialog by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    if (showSmartSettingsDialog) {
        SmartDownloadSettingsDialog(
            currentSettings = downloadSettings,
            onDismiss = { showSmartSettingsDialog = false },
            onSaveSettings = { updated ->
                onUpdateDownloadSettings(updated)
                showSmartSettingsDialog = false
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(JellyfinBackground)
            .padding(24.dp)
            .verticalScroll(scrollState)
    ) {
        Text(
            text = "Settings & Device Preferences",
            style = MaterialTheme.typography.headlineSmall,
            color = TextPrimary,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Configure client layout, media playback, and server connection",
            style = MaterialTheme.typography.bodySmall,
            color = TextMuted
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Device Mode Settings Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = JellyfinCardBackground),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "DISPLAY & INPUT MODE",
                    color = JellyfinCyan,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(JellyfinSurfaceVariant, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (deviceMode == DeviceMode.SMART_TV_DPAD) Icons.Default.Tv else Icons.Default.PhoneAndroid,
                                contentDescription = null,
                                tint = JellyfinCyan
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (deviceMode == DeviceMode.SMART_TV_DPAD) "Android Smart TV Mode (D-Pad Focus)" else "Android Phone Mode (Touch UI)",
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = if (deviceMode == DeviceMode.SMART_TV_DPAD) "Optimized for TV Remote D-Pad directional navigation and Leanback rail" else "Optimized for handheld touch controls and bottom navigation bar",
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Switch(
                        checked = deviceMode == DeviceMode.SMART_TV_DPAD,
                        onCheckedChange = { onToggleDeviceMode() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = JellyfinCyan,
                            uncheckedThumbColor = TextMuted,
                            uncheckedTrackColor = JellyfinSurfaceVariant
                        ),
                        modifier = Modifier.testTag("switch_device_mode")
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Server Connection Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = JellyfinCardBackground),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "ACTIVE SERVER CONNECTION",
                    color = JellyfinCyan,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(JellyfinSurfaceVariant, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Dns,
                                contentDescription = null,
                                tint = JellyfinPurple
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = activeServer?.name ?: "Demo Server",
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = activeServer?.url ?: "http://demo.jellyfin.org/stable/",
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        }
                    }

                    TvFocusableCard(
                        onClick = onOpenServerConnect,
                        testTag = "btn_change_server_settings"
                    ) {
                        Surface(
                            color = JellyfinPurple.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "Manage Servers",
                                color = JellyfinPurple,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Playback Quality Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = JellyfinCardBackground),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "PLAYBACK & TRANSCODING PREFERENCE",
                    color = JellyfinCyan,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.height(14.dp))

                val qualityOptions = listOf("4K Ultra HD", "1080p Full HD", "720p HD", "Auto Transcode")
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    qualityOptions.forEach { quality ->
                        val isSelected = defaultQuality == quality
                        TvFocusableCard(
                            onClick = { defaultQuality = quality },
                            testTag = "quality_option_${quality.lowercase().replace(" ", "_")}"
                        ) {
                            Surface(
                                color = if (isSelected) JellyfinCyan else JellyfinSurfaceVariant,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = quality,
                                    color = if (isSelected) Color.White else TextMuted,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Smart Downloads Preferences Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = JellyfinCardBackground),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "OFFLINE SMART DOWNLOADS",
                    color = JellyfinCyan,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(JellyfinSurfaceVariant, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = null,
                                tint = JellyfinCyan
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Wi-Fi Only & Smart Features",
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = if (downloadSettings.wifiOnly) "Wi-Fi Only • Auto-Retry • Smart Next Episode" else "Cellular & Wi-Fi Enabled • Auto-Retry",
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        }
                    }

                    TvFocusableCard(
                        onClick = { showSmartSettingsDialog = true },
                        testTag = "btn_open_smart_downloads_settings"
                    ) {
                        Surface(
                            color = JellyfinCyan.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "Configure",
                                color = JellyfinCyan,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // About App Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = JellyfinCardBackground),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = JellyfinCyan
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Jellyfin Android & TV Client v1.0.0",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Built with Jetpack Compose, Room Database, and Leanback TV focus support.",
                    color = TextMuted,
                    fontSize = 12.sp
                )
            }
        }
    }
}
