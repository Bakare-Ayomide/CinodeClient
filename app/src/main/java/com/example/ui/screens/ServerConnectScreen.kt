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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.JellyfinServer
import com.example.ui.components.TvFocusableCard
import com.example.ui.theme.JellyfinBackground
import com.example.ui.theme.JellyfinCardBackground
import com.example.ui.theme.JellyfinCyan
import com.example.ui.theme.JellyfinPurple
import com.example.ui.theme.JellyfinSurfaceVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary

@Composable
fun ServerConnectScreen(
    savedServers: List<JellyfinServer>,
    activeServer: JellyfinServer?,
    onConnectServer: (url: String, user: String, pass: String) -> Unit,
    onSelectServer: (Int) -> Unit,
    onDeleteServer: (Int) -> Unit,
    onClose: () -> Unit,
    isLoading: Boolean = false,
    errorMessage: String? = null,
    modifier: Modifier = Modifier
) {
    var serverUrl by remember { mutableStateOf("https://cinode.zerolord.com") }
    var useApiKey by remember { mutableStateOf(false) }
    var username by remember { mutableStateOf("admin") }
    var password by remember { mutableStateOf("") }
    var apiKey by remember { mutableStateOf("") }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(JellyfinBackground, Color(0xFF131024))
                )
            )
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                Brush.linearGradient(listOf(JellyfinCyan, JellyfinPurple)),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Dns,
                            contentDescription = "Server",
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Connect to Jellyfin Server",
                            style = MaterialTheme.typography.headlineSmall,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Enter server host URL, admin credentials or API key",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
                        )
                    }
                }

                if (activeServer != null) {
                    TvFocusableCard(
                        onClick = onClose,
                        testTag = "btn_close_server_modal"
                    ) {
                        Surface(
                            color = JellyfinSurfaceVariant,
                            shape = CircleShape,
                            modifier = Modifier.padding(4.dp)
                        ) {
                            Text(
                                text = "✕",
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Main Content Layout
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Connection Form (Left)
                Card(
                    modifier = Modifier
                        .weight(1.3f)
                        .fillMaxSize(),
                    colors = CardDefaults.cardColors(containerColor = JellyfinCardBackground),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Text(
                                text = "Server Host & Authentication",
                                style = MaterialTheme.typography.titleMedium,
                                color = JellyfinCyan,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Preset Server Quick Options
                        item {
                            Text(
                                text = "Configured Servers:",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                FilterChip(
                                    selected = serverUrl == "https://cinode.zerolord.com",
                                    onClick = { serverUrl = "https://cinode.zerolord.com" },
                                    label = { Text("cinode.zerolord.com", fontSize = 11.sp) },
                                    leadingIcon = { Icon(Icons.Default.Dns, contentDescription = null, modifier = Modifier.size(14.dp)) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = JellyfinCyan.copy(alpha = 0.3f),
                                        selectedLabelColor = JellyfinCyan
                                    )
                                )
                                FilterChip(
                                    selected = serverUrl == "http://163.245.193.7:8096",
                                    onClick = { serverUrl = "http://163.245.193.7:8096" },
                                    label = { Text("163.245.193.7:8096", fontSize = 11.sp) },
                                    leadingIcon = { Icon(Icons.Default.Dns, contentDescription = null, modifier = Modifier.size(14.dp)) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = JellyfinCyan.copy(alpha = 0.3f),
                                        selectedLabelColor = JellyfinCyan
                                    )
                                )
                            }
                        }

                        item {
                            OutlinedTextField(
                                value = serverUrl,
                                onValueChange = { serverUrl = it },
                                label = { Text("Server Address / Host URL") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Dns,
                                        contentDescription = null,
                                        tint = JellyfinCyan
                                    )
                                },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = JellyfinCyan,
                                    unfocusedBorderColor = JellyfinSurfaceVariant,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_server_url")
                            )
                        }

                        // Authentication Mode Toggle
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                FilterChip(
                                    selected = !useApiKey,
                                    onClick = { useApiKey = false },
                                    label = { Text("Username & Password") },
                                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(16.dp)) },
                                    modifier = Modifier.weight(1f),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = JellyfinCyan.copy(alpha = 0.25f),
                                        selectedLabelColor = JellyfinCyan
                                    )
                                )
                                FilterChip(
                                    selected = useApiKey,
                                    onClick = { useApiKey = true },
                                    label = { Text("Admin API Key") },
                                    leadingIcon = { Icon(Icons.Default.Key, contentDescription = null, modifier = Modifier.size(16.dp)) },
                                    modifier = Modifier.weight(1f),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = JellyfinPurple.copy(alpha = 0.25f),
                                        selectedLabelColor = JellyfinPurple
                                    )
                                )
                            }
                        }

                        if (!useApiKey) {
                            item {
                                OutlinedTextField(
                                    value = username,
                                    onValueChange = { username = it },
                                    label = { Text("Admin / User Name") },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.AdminPanelSettings,
                                            contentDescription = null,
                                            tint = JellyfinCyan
                                        )
                                    },
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = JellyfinCyan,
                                        unfocusedBorderColor = JellyfinSurfaceVariant,
                                        focusedTextColor = TextPrimary,
                                        unfocusedTextColor = TextPrimary
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("input_username")
                                )
                            }

                            item {
                                OutlinedTextField(
                                    value = password,
                                    onValueChange = { password = it },
                                    label = { Text("Password") },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Lock,
                                            contentDescription = null,
                                            tint = JellyfinCyan
                                        )
                                    },
                                    visualTransformation = PasswordVisualTransformation(),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = JellyfinCyan,
                                        unfocusedBorderColor = JellyfinSurfaceVariant,
                                        focusedTextColor = TextPrimary,
                                        unfocusedTextColor = TextPrimary
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("input_password")
                                )
                            }
                        } else {
                            item {
                                OutlinedTextField(
                                    value = apiKey,
                                    onValueChange = { apiKey = it },
                                    label = { Text("Jellyfin Admin API Key / Token") },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Key,
                                            contentDescription = null,
                                            tint = JellyfinPurple
                                        )
                                    },
                                    visualTransformation = PasswordVisualTransformation(),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = JellyfinPurple,
                                        unfocusedBorderColor = JellyfinSurfaceVariant,
                                        focusedTextColor = TextPrimary,
                                        unfocusedTextColor = TextPrimary
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("input_api_key")
                                )
                            }
                        }

                        if (errorMessage != null) {
                            item {
                                Text(
                                    text = errorMessage,
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    val finalUser = if (useApiKey) "ApiKey:$apiKey" else username
                                    onConnectServer(serverUrl, finalUser, password)
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (useApiKey) JellyfinPurple else JellyfinCyan
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("btn_connect_server")
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(
                                        color = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                } else {
                                    Text(
                                        text = if (useApiKey) "Connect with Admin API Key" else "Connect & Save Server",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }

                        item {
                            // Quick Demo Connect Button
                            TvFocusableCard(
                                onClick = {
                                    onConnectServer(
                                        "http://demo.jellyfin.org/stable",
                                        "demo",
                                        ""
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                                testTag = "btn_quick_demo"
                            ) {
                                Surface(
                                    color = JellyfinPurple.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PlayArrow,
                                            contentDescription = null,
                                            tint = JellyfinPurple
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Quick Demo Server (Instant Test)",
                                            color = JellyfinPurple,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Saved Servers List (Right)
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize(),
                    colors = CardDefaults.cardColors(containerColor = JellyfinCardBackground),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp)
                    ) {
                        Text(
                            text = "Saved Servers",
                            style = MaterialTheme.typography.titleMedium,
                            color = JellyfinCyan,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        if (savedServers.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No saved servers yet.\nConnect to a server on the left.",
                                    color = TextMuted,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(savedServers) { server ->
                                    val isCurrent = activeServer?.id == server.id
                                    TvFocusableCard(
                                        onClick = { onSelectServer(server.id) },
                                        modifier = Modifier.fillMaxWidth(),
                                        testTag = "saved_server_${server.id}"
                                    ) { isFocused ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(
                                                    if (isCurrent) JellyfinCyan.copy(alpha = 0.2f) else JellyfinSurfaceVariant,
                                                    RoundedCornerShape(12.dp)
                                                )
                                                .padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = if (server.isDemo) Icons.Default.Tv else Icons.Default.Dns,
                                                    contentDescription = null,
                                                    tint = if (isCurrent) JellyfinCyan else TextMuted
                                                )
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Column {
                                                    Text(
                                                        text = server.name,
                                                        color = TextPrimary,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 13.sp
                                                    )
                                                    Text(
                                                        text = server.url,
                                                        color = TextMuted,
                                                        fontSize = 11.sp
                                                    )
                                                }
                                            }

                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                if (isCurrent) {
                                                    Icon(
                                                        imageVector = Icons.Default.Check,
                                                        contentDescription = "Active",
                                                        tint = JellyfinCyan,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                }
                                                IconButton(
                                                    onClick = { onDeleteServer(server.id) },
                                                    modifier = Modifier.size(28.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Delete,
                                                        contentDescription = "Delete",
                                                        tint = TextMuted,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

