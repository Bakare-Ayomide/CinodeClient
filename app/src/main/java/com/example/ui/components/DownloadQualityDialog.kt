package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
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
import com.example.data.model.JellyfinItem
import com.example.ui.theme.JellyfinCardBackground
import com.example.ui.theme.JellyfinCyan
import com.example.ui.theme.JellyfinPurple
import com.example.ui.theme.JellyfinSurface
import com.example.ui.theme.JellyfinSurfaceVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary

data class DownloadQualityOption(
    val qualityLabel: String, // e.g. "1080p Full HD"
    val resolutionTag: String, // e.g. "1080p"
    val estimatedSizeText: String, // e.g. "1.2 GB"
    val description: String // e.g. "Recommended for phones & tablets"
)

@Composable
fun DownloadQualityDialog(
    itemTitle: String,
    mediaType: String,
    onDismiss: () -> Unit,
    onStartDownload: (quality: String) -> Unit
) {
    val qualityOptions = remember(mediaType) {
        val isMovie = mediaType == "MOVIE"
        listOf(
            DownloadQualityOption(
                qualityLabel = "Original / 4K Ultra HD",
                resolutionTag = "4K",
                estimatedSizeText = if (isMovie) "3.8 GB" else "1.4 GB",
                description = "Maximum quality • Dolby Atmos audio"
            ),
            DownloadQualityOption(
                qualityLabel = "1080p Full HD",
                resolutionTag = "1080p",
                estimatedSizeText = if (isMovie) "1.6 GB" else "650 MB",
                description = "Recommended • Best balance of detail & space"
            ),
            DownloadQualityOption(
                qualityLabel = "720p HD",
                resolutionTag = "720p",
                estimatedSizeText = if (isMovie) "850 MB" else "320 MB",
                description = "Standard HD • Saves download time"
            ),
            DownloadQualityOption(
                qualityLabel = "480p SD",
                resolutionTag = "480p",
                estimatedSizeText = if (isMovie) "420 MB" else "180 MB",
                description = "Compact • Fast download for slow networks"
            )
        )
    }

    var selectedOption by remember { mutableStateOf(qualityOptions[1]) } // Default 1080p

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = JellyfinCardBackground)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
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
                                imageVector = Icons.Default.HighQuality,
                                contentDescription = null,
                                tint = JellyfinCyan,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Select Download Quality",
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                text = itemTitle,
                                color = TextMuted,
                                fontSize = 12.sp,
                                maxLines = 1
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    qualityOptions.forEach { option ->
                        val isSelected = option.resolutionTag == selectedOption.resolutionTag
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) JellyfinCyan.copy(alpha = 0.15f) else JellyfinSurfaceVariant,
                            border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, JellyfinCyan) else null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedOption = option }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { selectedOption = option },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = JellyfinCyan,
                                        unselectedColor = TextMuted
                                    )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = option.qualityLabel,
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = option.description,
                                        color = TextMuted,
                                        fontSize = 11.sp
                                    )
                                }
                                Surface(
                                    color = if (isSelected) JellyfinCyan else JellyfinSurface,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = option.estimatedSizeText,
                                        color = if (isSelected) Color.Black else TextPrimary,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = TextMuted)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = {
                            onStartDownload(selectedOption.resolutionTag)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = JellyfinCyan),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Download (${selectedOption.estimatedSizeText})",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
