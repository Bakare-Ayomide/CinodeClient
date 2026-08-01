package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.DownloadForOffline
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
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
import java.io.File

enum class DownloadFilter {
    ALL,
    MOVIES,
    SERIES,
    DOWNLOADING
}

fun formatFileSize(bytes: Long): String {
    if (bytes <= 0) return "0 MB"
    val mb = bytes / (1024.0 * 1024.0)
    return if (mb >= 1000) {
        String.format("%.2f GB", mb / 1024.0)
    } else {
        String.format("%.1f MB", mb)
    }
}

fun formatSpeed(bytesPerSec: Long): String {
    if (bytesPerSec <= 0) return "0 KB/s"
    val kb = bytesPerSec / 1024.0
    val mb = kb / 1024.0
    return if (mb >= 1.0) {
        String.format("%.1f MB/s", mb)
    } else {
        String.format("%.0f KB/s", kb)
    }
}

fun formatEta(seconds: Long): String {
    if (seconds <= 0) return "Calculating..."
    val mins = seconds / 60
    val secs = seconds % 60
    return if (mins > 0) "${mins}m ${secs}s left" else "${secs}s left"
}

@Composable
fun DownloadsScreen(
    downloads: List<DownloadEntity>,
    onPlayMedia: (JellyfinItem) -> Unit,
    onDeleteDownload: (String) -> Unit,
    onPauseDownload: (String) -> Unit = {},
    onResumeDownload: (String) -> Unit = {},
    onCancelDownload: (String) -> Unit = {},
    onRetryDownload: (String) -> Unit = {},
    onDeleteSeasonDownloads: (String, Int) -> Unit = { _, _ -> },
    onDeleteSeriesDownloads: (String) -> Unit = {},
    onDeleteAllDownloads: () -> Unit = {},
    onExploreLibrary: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf(DownloadFilter.ALL) }
    var showClearAllDialog by remember { mutableStateOf(false) }

    val expandedSeriesMap = remember { mutableStateMapOf<String, Boolean>() }
    val expandedSeasonsMap = remember { mutableStateMapOf<String, Boolean>() }

    val activeCount = downloads.count { it.downloadStatus == "DOWNLOADING" || it.downloadStatus == "PAUSED" || it.downloadStatus == "WAITING" }
    val moviesDownloads = remember(downloads) { downloads.filter { it.mediaType == "MOVIE" && it.downloadStatus == "COMPLETED" } }
    val seriesDownloads = remember(downloads) { downloads.filter { (it.mediaType == "SERIES" || it.mediaType == "EPISODE") && it.downloadStatus == "COMPLETED" } }
    val activeDownloads = remember(downloads) { downloads.filter { it.downloadStatus != "COMPLETED" } }

    val totalStorageBytes = downloads.sumOf { it.downloadedSizeBytes }

    if (showClearAllDialog) {
        AlertDialog(
            onDismissRequest = { showClearAllDialog = false },
            title = { Text("Delete All Downloads", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete all downloaded movies and episodes? This will free up ${formatFileSize(totalStorageBytes)} of storage.", color = TextMuted) },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteAllDownloads()
                        showClearAllDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = JellyfinRed)
                ) {
                    Text("Delete All", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearAllDialog = false }) {
                    Text("Cancel", color = TextMuted)
                }
            },
            containerColor = JellyfinCardBackground
        )
    }

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
                                text = "In-App local storage • Watch offline anytime",
                                color = TextMuted,
                                fontSize = 12.sp
                            )
                        }
                    }

                    if (downloads.isNotEmpty()) {
                        Button(
                            onClick = { showClearAllDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = JellyfinRed.copy(alpha = 0.2f)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.DeleteSweep, contentDescription = null, tint = JellyfinRed, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Clear All", color = JellyfinRed, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
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
                                    text = "Device Storage",
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }

                            Text(
                                text = "${formatFileSize(totalStorageBytes)} Used",
                                color = JellyfinCyan,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        LinearProgressIndicator(
                            progress = { (totalStorageBytes.toFloat() / (50L * 1024 * 1024 * 1024)).coerceIn(0.02f, 1.0f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(CircleShape),
                            color = JellyfinCyan,
                            trackColor = JellyfinSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "${downloads.size} items downloaded • Fast local ExoPlayer playback",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Filter Chips
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    item {
                        FilterChip(
                            selected = selectedFilter == DownloadFilter.ALL,
                            onClick = { selectedFilter = DownloadFilter.ALL },
                            label = { Text("All Downloads (${downloads.size})") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = JellyfinCyan,
                                selectedLabelColor = Color.Black,
                                containerColor = JellyfinSurfaceVariant,
                                labelColor = TextPrimary
                            )
                        )
                    }

                    item {
                        FilterChip(
                            selected = selectedFilter == DownloadFilter.MOVIES,
                            onClick = { selectedFilter = DownloadFilter.MOVIES },
                            label = { Text("Movies (${moviesDownloads.size})") },
                            leadingIcon = { Icon(Icons.Default.Movie, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = JellyfinCyan,
                                selectedLabelColor = Color.Black,
                                containerColor = JellyfinSurfaceVariant,
                                labelColor = TextPrimary
                            )
                        )
                    }

                    item {
                        FilterChip(
                            selected = selectedFilter == DownloadFilter.SERIES,
                            onClick = { selectedFilter = DownloadFilter.SERIES },
                            label = { Text("TV Shows (${seriesDownloads.size})") },
                            leadingIcon = { Icon(Icons.Default.Tv, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = JellyfinCyan,
                                selectedLabelColor = Color.Black,
                                containerColor = JellyfinSurfaceVariant,
                                labelColor = TextPrimary
                            )
                        )
                    }

                    item {
                        FilterChip(
                            selected = selectedFilter == DownloadFilter.DOWNLOADING,
                            onClick = { selectedFilter = DownloadFilter.DOWNLOADING },
                            label = { Text("Downloading ($activeCount)") },
                            leadingIcon = { Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = JellyfinCyan,
                                selectedLabelColor = Color.Black,
                                containerColor = JellyfinSurfaceVariant,
                                labelColor = TextPrimary
                            )
                        )
                    }
                }
            }
        }

        // Empty Downloads View
        if (downloads.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = JellyfinCardBackground),
                    shape = RoundedCornerShape(20.dp),
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
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(JellyfinSurfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.FileDownload,
                                contentDescription = null,
                                tint = TextMuted,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "No Downloads Yet",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Download movies and TV episodes directly to in-app storage to enjoy watching offline anywhere.",
                            color = TextMuted,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = onExploreLibrary,
                            colors = ButtonDefaults.buttonColors(containerColor = JellyfinCyan),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Explore Library", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            // Section 1: Active Downloads Manager
            if ((selectedFilter == DownloadFilter.ALL || selectedFilter == DownloadFilter.DOWNLOADING) && activeDownloads.isNotEmpty()) {
                item {
                    Text(
                        text = "ACTIVE DOWNLOAD MANAGER",
                        color = JellyfinCyan,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                    )
                }

                items(activeDownloads, key = { "active_${it.itemId}" }) { activeItem ->
                    ActiveDownloadCard(
                        download = activeItem,
                        onPause = { onPauseDownload(activeItem.itemId) },
                        onResume = { onResumeDownload(activeItem.itemId) },
                        onCancel = { onCancelDownload(activeItem.itemId) },
                        onRetry = { onRetryDownload(activeItem.itemId) }
                    )
                }
            }

            // Section 2: Movies Downloads
            if ((selectedFilter == DownloadFilter.ALL || selectedFilter == DownloadFilter.MOVIES) && moviesDownloads.isNotEmpty()) {
                item {
                    Text(
                        text = "MOVIES (${moviesDownloads.size})",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                    )
                }

                items(moviesDownloads, key = { "movie_${it.itemId}" }) { movie ->
                    DownloadedMovieCard(
                        download = movie,
                        onPlay = {
                            val mediaItem = JellyfinItem(
                                id = movie.itemId,
                                title = movie.title,
                                overview = movie.overview,
                                mediaType = MediaType.MOVIE,
                                posterUrl = movie.posterUrl,
                                backdropUrl = movie.backdropUrl,
                                videoUrl = movie.localFilePath,
                                year = movie.year,
                                rating = movie.rating,
                                resolution = movie.resolution
                            )
                            onPlayMedia(mediaItem)
                        },
                        onDelete = { onDeleteDownload(movie.itemId) }
                    )
                }
            }

            // Section 3: TV Shows Grouped Hierarchy (Show -> Season -> Episodes)
            if ((selectedFilter == DownloadFilter.ALL || selectedFilter == DownloadFilter.SERIES) && seriesDownloads.isNotEmpty()) {
                val seriesGrouped = seriesDownloads.groupBy { it.seriesName ?: it.title }

                item {
                    Text(
                        text = "TV SHOWS (${seriesGrouped.keys.size})",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                    )
                }

                seriesGrouped.forEach { (seriesName, episodes) ->
                    item(key = "series_$seriesName") {
                        val isExpanded = expandedSeriesMap[seriesName] ?: false
                        val totalSeriesBytes = episodes.sumOf { it.downloadedSizeBytes }
                        val seasonsCount = episodes.mapNotNull { it.seasonNumber }.distinct().size.coerceAtLeast(1)

                        Card(
                            colors = CardDefaults.cardColors(containerColor = JellyfinCardBackground),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { expandedSeriesMap[seriesName] = !isExpanded }
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val firstPoster = episodes.firstOrNull()?.localPosterPath?.ifBlank { episodes.firstOrNull()?.posterUrl } ?: ""
                                    AsyncImage(
                                        model = if (firstPoster.startsWith("/")) File(firstPoster) else firstPoster,
                                        contentDescription = seriesName,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(54.dp, 76.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(JellyfinSurfaceVariant)
                                    )

                                    Spacer(modifier = Modifier.width(14.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = seriesName,
                                            color = TextPrimary,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "$seasonsCount Season(s) • ${episodes.size} Episode(s) • ${formatFileSize(totalSeriesBytes)}",
                                            color = TextMuted,
                                            fontSize = 12.sp
                                        )
                                    }

                                    IconButton(onClick = { onDeleteSeriesDownloads(seriesName) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete Show", tint = TextMuted)
                                    }

                                    Icon(
                                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                        contentDescription = null,
                                        tint = JellyfinCyan
                                    )
                                }

                                // Expanded Seasons & Episodes list
                                AnimatedVisibility(visible = isExpanded) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(JellyfinSurface.copy(alpha = 0.5f))
                                            .padding(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        val seasonGrouped = episodes.groupBy { it.seasonNumber ?: 1 }
                                        seasonGrouped.forEach { (seasonNum, seasonEpisodes) ->
                                            val seasonKey = "${seriesName}_S${seasonNum}"
                                            val isSeasonExpanded = expandedSeasonsMap[seasonKey] ?: true

                                            Card(
                                                colors = CardDefaults.cardColors(containerColor = JellyfinSurfaceVariant),
                                                shape = RoundedCornerShape(12.dp)
                                            ) {
                                                Column(modifier = Modifier.padding(10.dp)) {
                                                    Row(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .clickable { expandedSeasonsMap[seasonKey] = !isSeasonExpanded },
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.SpaceBetween
                                                    ) {
                                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                                            Icon(Icons.Default.FolderZip, contentDescription = null, tint = JellyfinPurple, modifier = Modifier.size(16.dp))
                                                            Spacer(modifier = Modifier.width(8.dp))
                                                            Text(
                                                                text = "Season $seasonNum (${seasonEpisodes.size} episodes)",
                                                                color = TextPrimary,
                                                                fontWeight = FontWeight.Bold,
                                                                fontSize = 13.sp
                                                            )
                                                        }

                                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                                            IconButton(
                                                                onClick = { onDeleteSeasonDownloads(seriesName, seasonNum) },
                                                                modifier = Modifier.size(28.dp)
                                                            ) {
                                                                Icon(Icons.Default.Delete, contentDescription = "Delete Season", tint = TextMuted, modifier = Modifier.size(16.dp))
                                                            }
                                                        }
                                                    }

                                                    if (isSeasonExpanded) {
                                                        Spacer(modifier = Modifier.height(8.dp))
                                                        seasonEpisodes.forEach { ep ->
                                                            DownloadedEpisodeRow(
                                                                episode = ep,
                                                                onPlay = {
                                                                    val mediaItem = JellyfinItem(
                                                                        id = ep.itemId,
                                                                        title = ep.title,
                                                                        overview = ep.overview,
                                                                        mediaType = MediaType.SERIES,
                                                                        posterUrl = ep.posterUrl,
                                                                        backdropUrl = ep.backdropUrl,
                                                                        videoUrl = ep.localFilePath,
                                                                        year = ep.year,
                                                                        rating = ep.rating,
                                                                        resolution = ep.resolution
                                                                    )
                                                                    onPlayMedia(mediaItem)
                                                                },
                                                                onDelete = { onDeleteDownload(ep.itemId) }
                                                            )
                                                            Spacer(modifier = Modifier.height(6.dp))
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
    }
}

@Composable
fun ActiveDownloadCard(
    download: DownloadEntity,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onCancel: () -> Unit,
    onRetry: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = JellyfinCardBackground),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                val imagePath = download.localPosterPath.ifBlank { download.posterUrl }
                AsyncImage(
                    model = if (imagePath.startsWith("/")) File(imagePath) else imagePath,
                    contentDescription = download.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(48.dp, 68.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(JellyfinSurfaceVariant)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = download.title,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = when (download.downloadStatus) {
                                "DOWNLOADING" -> JellyfinCyan.copy(alpha = 0.2f)
                                "PAUSED" -> Color(0xFFFFB74D).copy(alpha = 0.2f)
                                else -> JellyfinRed.copy(alpha = 0.2f)
                            },
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = download.downloadStatus,
                                color = when (download.downloadStatus) {
                                    "DOWNLOADING" -> JellyfinCyan
                                    "PAUSED" -> Color(0xFFFFB74D)
                                    else -> JellyfinRed
                                },
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = "${formatFileSize(download.downloadedSizeBytes)} / ${formatFileSize(download.totalSizeBytes)}",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    if (download.downloadStatus == "DOWNLOADING") {
                        Text(
                            text = "${formatSpeed(download.downloadSpeedBytesPerSec)} • ${formatEta(download.etaSeconds)}",
                            color = JellyfinCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Row {
                    when (download.downloadStatus) {
                        "DOWNLOADING" -> {
                            IconButton(onClick = onPause) {
                                Icon(Icons.Default.Pause, contentDescription = "Pause", tint = Color.White)
                            }
                        }
                        "PAUSED" -> {
                            IconButton(onClick = onResume) {
                                Icon(Icons.Default.PlayArrow, contentDescription = "Resume", tint = JellyfinCyan)
                            }
                        }
                        "FAILED" -> {
                            IconButton(onClick = onRetry) {
                                Icon(Icons.Default.Refresh, contentDescription = "Retry", tint = JellyfinCyan)
                            }
                        }
                    }

                    IconButton(onClick = onCancel) {
                        Icon(Icons.Default.Delete, contentDescription = "Cancel", tint = TextMuted)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            LinearProgressIndicator(
                progress = { (download.progressPercent / 100f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape),
                color = if (download.downloadStatus == "PAUSED") Color(0xFFFFB74D) else JellyfinCyan,
                trackColor = JellyfinSurfaceVariant
            )
        }
    }
}

@Composable
fun DownloadedMovieCard(
    download: DownloadEntity,
    onPlay: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = JellyfinCardBackground),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val posterPath = download.localPosterPath.ifBlank { download.posterUrl }
            AsyncImage(
                model = if (posterPath.startsWith("/")) File(posterPath) else posterPath,
                contentDescription = download.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(54.dp, 78.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(JellyfinSurfaceVariant)
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = download.title,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = Color(0xFF2E7D32).copy(alpha = 0.3f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF81C784), modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Downloaded", color = Color(0xFF81C784), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "${download.quality} • ${formatFileSize(download.downloadedSizeBytes)}",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                TvFocusableCard(onClick = onPlay, testTag = "btn_play_download_${download.itemId}") {
                    Surface(
                        color = JellyfinCyan,
                        shape = CircleShape,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = Color.Black, modifier = Modifier.size(20.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = TextMuted)
                }
            }
        }
    }
}

@Composable
fun DownloadedEpisodeRow(
    episode: DownloadEntity,
    onPlay: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        color = JellyfinCardBackground,
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val posterPath = episode.localPosterPath.ifBlank { episode.posterUrl }
            AsyncImage(
                model = if (posterPath.startsWith("/")) File(posterPath) else posterPath,
                contentDescription = episode.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(48.dp, 32.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(JellyfinSurfaceVariant)
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (episode.episodeNumber != null) "E${episode.episodeNumber} • ${episode.title}" else episode.title,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${episode.quality} • ${formatFileSize(episode.downloadedSizeBytes)}",
                    color = TextMuted,
                    fontSize = 10.sp
                )
            }

            IconButton(onClick = onPlay, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = JellyfinCyan, modifier = Modifier.size(20.dp))
            }

            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = TextMuted, modifier = Modifier.size(16.dp))
            }
        }
    }
}
