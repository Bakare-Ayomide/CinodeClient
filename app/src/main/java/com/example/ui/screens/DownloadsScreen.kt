package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.DownloadForOffline
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.db.DownloadEntity
import com.example.data.model.JellyfinItem
import com.example.data.model.MediaType
import com.example.ui.components.TvFocusableCard
import com.example.ui.theme.JellyfinBackground
import com.example.ui.theme.JellyfinCardBackground
import com.example.ui.theme.JellyfinCyan
import com.example.ui.theme.JellyfinPurple
import com.example.ui.theme.JellyfinRed
import com.example.ui.theme.JellyfinSurface
import com.example.ui.theme.JellyfinSurfaceVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary

enum class DownloadFilter {
    ALL,
    MOVIES,
    SERIES,
    DOWNLOADING
}

@Composable
fun DownloadsScreen(
    downloads: List<DownloadEntity>,
    onPlayMedia: (JellyfinItem) -> Unit,
    onDeleteDownload: (String) -> Unit,
    onExploreLibrary: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf(DownloadFilter.ALL) }

    val filteredDownloads = remember(downloads, selectedFilter) {
        when (selectedFilter) {
            DownloadFilter.ALL -> downloads
            DownloadFilter.MOVIES -> downloads.filter { it.mediaType == "MOVIE" }
            DownloadFilter.SERIES -> downloads.filter { it.mediaType == "SERIES" || it.mediaType == "EPISODE" }
            DownloadFilter.DOWNLOADING -> downloads.filter { it.downloadStatus == "DOWNLOADING" }
        }
    }

    val totalStorageBytes = downloads.sumOf { it.downloadedSizeBytes }
    val totalStorageMb = totalStorageBytes / (1024 * 1024)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(JellyfinBackground),
        contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 48.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Top Header
        item {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(JellyfinCyan.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.DownloadForOffline,
                                contentDescription = null,
                                tint = JellyfinCyan,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = "Offline Downloads",
                                color = TextPrimary,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 22.sp
                            )
                            Text(
                                text = "In-App storage • Available without internet",
                                color = TextMuted,
                                fontSize = 12.sp
                            )
                        }
                    }

                    // Total Count Badge
                    Surface(
                        color = JellyfinSurfaceVariant,
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = "${downloads.size} items",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Storage Usage Header Banner
                Surface(
                    color = JellyfinCardBackground,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Storage,
                                    contentDescription = null,
                                    tint = JellyfinCyan,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "In-App Local Storage",
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                            Text(
                                text = if (totalStorageMb > 1024) String.format("%.2f GB", totalStorageMb / 1024f) else "$totalStorageMb MB used",
                                color = JellyfinCyan,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        val storageProgress = (totalStorageMb / 32000f).coerceIn(0.02f, 1f)
                        LinearProgressIndicator(
                            progress = { storageProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = JellyfinCyan,
                            trackColor = Color.White.copy(alpha = 0.12f)
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Stored securely inside app private folder. Accessible offline.",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Filter Category Chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        FilterChip(
                            selected = selectedFilter == DownloadFilter.ALL,
                            onClick = { selectedFilter = DownloadFilter.ALL },
                            label = { Text("All (${downloads.size})") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = JellyfinCyan,
                                selectedLabelColor = Color.White,
                                containerColor = JellyfinSurfaceVariant,
                                labelColor = TextMuted
                            )
                        )
                    }
                    item {
                        val moviesCount = downloads.count { it.mediaType == "MOVIE" }
                        FilterChip(
                            selected = selectedFilter == DownloadFilter.MOVIES,
                            onClick = { selectedFilter = DownloadFilter.MOVIES },
                            label = { Text("Movies ($moviesCount)") },
                            leadingIcon = { Icon(Icons.Default.Movie, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = JellyfinCyan,
                                selectedLabelColor = Color.White,
                                containerColor = JellyfinSurfaceVariant,
                                labelColor = TextMuted
                            )
                        )
                    }
                    item {
                        val seriesCount = downloads.count { it.mediaType == "SERIES" || it.mediaType == "EPISODE" }
                        FilterChip(
                            selected = selectedFilter == DownloadFilter.SERIES,
                            onClick = { selectedFilter = DownloadFilter.SERIES },
                            label = { Text("TV Series ($seriesCount)") },
                            leadingIcon = { Icon(Icons.Default.Tv, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = JellyfinCyan,
                                selectedLabelColor = Color.White,
                                containerColor = JellyfinSurfaceVariant,
                                labelColor = TextMuted
                            )
                        )
                    }
                    item {
                        val activeCount = downloads.count { it.downloadStatus == "DOWNLOADING" }
                        FilterChip(
                            selected = selectedFilter == DownloadFilter.DOWNLOADING,
                            onClick = { selectedFilter = DownloadFilter.DOWNLOADING },
                            label = { Text("Downloading ($activeCount)") },
                            leadingIcon = { Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = JellyfinPurple,
                                selectedLabelColor = Color.White,
                                containerColor = JellyfinSurfaceVariant,
                                labelColor = TextMuted
                            )
                        )
                    }
                }
            }
        }

        // Empty State Banner
        if (filteredDownloads.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(JellyfinSurfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.FileDownload,
                                contentDescription = null,
                                tint = TextMuted,
                                modifier = Modifier.size(40.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = if (downloads.isEmpty()) "No Downloads Yet" else "No matching downloads",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = if (downloads.isEmpty())
                                "Download movies and TV shows from the detail page to watch offline anytime without network."
                            else "No items match your selected filter.",
                            color = TextMuted,
                            fontSize = 13.sp,
                            modifier = Modifier.width(280.dp),
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        TvFocusableCard(
                            onClick = onExploreLibrary,
                            testTag = "btn_explore_library_empty"
                        ) {
                            Surface(
                                color = JellyfinRed,
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Movie,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Explore Movies & Shows",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Downloads List
            items(filteredDownloads, key = { it.itemId }) { download ->
                val mediaType = when (download.mediaType) {
                    "MOVIE" -> MediaType.MOVIE
                    "SERIES" -> MediaType.SERIES
                    "EPISODE" -> MediaType.EPISODE
                    else -> MediaType.MOVIE
                }

                val item = JellyfinItem(
                    id = download.itemId,
                    title = download.title,
                    overview = download.overview,
                    mediaType = mediaType,
                    posterUrl = download.posterUrl,
                    backdropUrl = download.backdropUrl,
                    year = download.year,
                    rating = download.rating,
                    resolution = download.resolution,
                    videoUrl = download.localFilePath
                )

                val isCompleted = download.downloadStatus == "COMPLETED"
                val sizeMb = download.downloadedSizeBytes / (1024 * 1024)

                TvFocusableCard(
                    onClick = {
                        if (isCompleted) {
                            onPlayMedia(item)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "download_item_${download.itemId}"
                ) { isFocused ->
                    Surface(
                        color = if (isFocused) JellyfinSurfaceVariant else JellyfinCardBackground,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Poster Thumbnail
                            Box(
                                modifier = Modifier
                                    .width(76.dp)
                                    .aspectRatio(2f / 3f)
                                    .clip(RoundedCornerShape(8.dp))
                            ) {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(download.posterUrl)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = download.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )

                                if (isCompleted) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(Color.Black.copy(alpha = 0.35f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PlayArrow,
                                            contentDescription = "Play",
                                            tint = Color.White,
                                            modifier = Modifier
                                                .size(28.dp)
                                                .background(JellyfinCyan, CircleShape)
                                                .padding(4.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            // Details
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        color = if (download.mediaType == "MOVIE") JellyfinRed.copy(alpha = 0.2f) else JellyfinPurple.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = download.mediaType,
                                            color = if (download.mediaType == "MOVIE") JellyfinRed else JellyfinPurple,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(6.dp))

                                    Text(
                                        text = "${download.year} • ${download.resolution}",
                                        color = TextMuted,
                                        fontSize = 11.sp
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = download.title,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                if (isCompleted) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Downloaded",
                                            tint = Color(0xFF4CAF50),
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Downloaded • $sizeMb MB",
                                            color = Color(0xFF81C784),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                } else {
                                    Column {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "Downloading ${download.progressPercent}%",
                                                color = JellyfinCyan,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "$sizeMb MB",
                                                color = TextMuted,
                                                fontSize = 11.sp
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        LinearProgressIndicator(
                                            progress = { download.progressPercent / 100f },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(4.dp)
                                                .clip(RoundedCornerShape(2.dp)),
                                            color = JellyfinCyan,
                                            trackColor = Color.White.copy(alpha = 0.1f)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            // Delete Action Button
                            IconButton(
                                onClick = { onDeleteDownload(download.itemId) },
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(Color.White.copy(alpha = 0.08f), CircleShape)
                                    .testTag("btn_delete_download_${download.itemId}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete Download",
                                    tint = Color.White.copy(alpha = 0.7f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
