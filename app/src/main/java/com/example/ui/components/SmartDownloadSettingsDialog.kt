package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.AutoMode
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.db.DownloadSettingsEntity
import com.example.ui.theme.JellyfinCardBackground
import com.example.ui.theme.JellyfinCyan
import com.example.ui.theme.JellyfinSurfaceVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary

@Composable
fun SmartDownloadSettingsDialog(
    currentSettings: DownloadSettingsEntity,
    onDismiss: () -> Unit,
    onSaveSettings: (DownloadSettingsEntity) -> Unit
) {
    var wifiOnly by remember { mutableStateOf(currentSettings.wifiOnly) }
    var autoRetryFailed by remember { mutableStateOf(currentSettings.autoRetryFailed) }
    var autoDownloadNextEpisode by remember { mutableStateOf(currentSettings.autoDownloadNextEpisode) }
    var autoDeleteWatched by remember { mutableStateOf(currentSettings.autoDeleteWatched) }
    var maxSimultaneous by remember { mutableStateOf(currentSettings.maxSimultaneousDownloads) }
    var storageLimitGb by remember { mutableStateOf(currentSettings.maxStorageLimitGb) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = JellyfinCardBackground)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(JellyfinCyan.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null,
                            tint = JellyfinCyan,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Smart Download Settings",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "Automate offline media preferences",
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Toggle 1: Wi-Fi Only
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Download via Wi-Fi only", color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Text("Avoid background cellular data usage", color = TextMuted, fontSize = 11.sp)
                    }
                    Switch(
                        checked = wifiOnly,
                        onCheckedChange = { wifiOnly = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.Black, checkedTrackColor = JellyfinCyan)
                    )
                }

                Divider(color = JellyfinSurfaceVariant, modifier = Modifier.padding(vertical = 12.dp))

                // Toggle 2: Auto Retry Failed
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Auto-retry failed downloads", color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Text("Automatically resume when network recovers", color = TextMuted, fontSize = 11.sp)
                    }
                    Switch(
                        checked = autoRetryFailed,
                        onCheckedChange = { autoRetryFailed = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.Black, checkedTrackColor = JellyfinCyan)
                    )
                }

                Divider(color = JellyfinSurfaceVariant, modifier = Modifier.padding(vertical = 12.dp))

                // Toggle 3: Auto-Download Next Episode
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Smart Download Next Episode", color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Text("Auto-fetch next episode when current is 80% watched", color = TextMuted, fontSize = 11.sp)
                    }
                    Switch(
                        checked = autoDownloadNextEpisode,
                        onCheckedChange = { autoDownloadNextEpisode = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.Black, checkedTrackColor = JellyfinCyan)
                    )
                }

                Divider(color = JellyfinSurfaceVariant, modifier = Modifier.padding(vertical = 12.dp))

                // Toggle 4: Auto-Delete Watched
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Auto-delete watched media", color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Text("Remove downloaded episode after completion to save space", color = TextMuted, fontSize = 11.sp)
                    }
                    Switch(
                        checked = autoDeleteWatched,
                        onCheckedChange = { autoDeleteWatched = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.Black, checkedTrackColor = JellyfinCyan)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = TextMuted)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = {
                            val updated = currentSettings.copy(
                                wifiOnly = wifiOnly,
                                autoRetryFailed = autoRetryFailed,
                                autoDownloadNextEpisode = autoDownloadNextEpisode,
                                autoDeleteWatched = autoDeleteWatched,
                                maxSimultaneousDownloads = maxSimultaneous,
                                maxStorageLimitGb = storageLimitGb
                            )
                            onSaveSettings(updated)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = JellyfinCyan),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Save Preferences", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
