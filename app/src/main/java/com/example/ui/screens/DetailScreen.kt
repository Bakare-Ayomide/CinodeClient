package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.Downloading
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.example.data.model.Episode
import com.example.data.model.JellyfinItem
import com.example.data.model.MediaType
import com.example.ui.components.MediaPosterCard
import com.example.ui.components.TvFocusableCard
import com.example.ui.theme.JellyfinBackground
import com.example.ui.theme.JellyfinCardBackground
import com.example.ui.theme.JellyfinCyan
import com.example.ui.theme.JellyfinPurple
import com.example.ui.theme.JellyfinSurfaceVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary

import com.example.ui.components.DownloadQualityDialog

@Composable
fun DetailScreen(
    item: JellyfinItem,
    episodes: List<Episode>,
    isFavorite: Boolean,
    downloadState: DownloadEntity? = null,
    relatedMovies: List<JellyfinItem> = emptyList(),
    relatedShows: List<JellyfinItem> = emptyList(),
    suggestedForYou: List<JellyfinItem> = emptyList(),
    moreLikeThis: List<JellyfinItem> = emptyList(),
    onBack: () -> Unit,
    onPlay: (JellyfinItem) -> Unit,
    onPlayEpisode: (Episode) -> Unit,
    onToggleFavorite: () -> Unit,
    onStartDownload: (JellyfinItem) -> Unit = {},
    onStartDownloadWithQuality: (JellyfinItem, String, Episode?) -> Unit = { item, quality, episode -> onStartDownload(item) },
    onDeleteDownload: (String) -> Unit = {},
    onItemClick: (JellyfinItem) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedSeason by remember { mutableStateOf(1) }
    var showQualityDialog by remember { mutableStateOf(false) }
    var itemToDownload by remember { mutableStateOf<Pair<JellyfinItem, Episode?>?>(null) }

    if (showQualityDialog && itemToDownload != null) {
        val (targetItem, targetEpisode) = itemToDownload!!
        DownloadQualityDialog(
            itemTitle = targetEpisode?.title ?: targetItem.title,
            mediaType = targetItem.mediaType.name,
            onDismiss = { showQualityDialog = false },
            onStartDownload = { quality ->
                onStartDownloadWithQuality(targetItem, quality, targetEpisode)
                showQualityDialog = false
            }
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(JellyfinBackground),
        contentPadding = PaddingValues(bottom = 40.dp)
    ) {
        // Hero Backdrop Header
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 220.dp, max = 340.dp)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(item.backdropUrl.ifEmpty { item.posterUrl })
                        .crossfade(true)
                        .build(),
                    contentDescription = item.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Dark Gradients
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.5f),
                                    Color.Transparent,
                                    JellyfinBackground.copy(alpha = 0.95f),
                                    JellyfinBackground
                                )
                            )
                        )
                )

                // Top Back Button
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .padding(16.dp)
                        .align(Alignment.TopStart)
                        .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                        .testTag("btn_detail_back")
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
            }
        }

        // Details Pane Content
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    // Poster Thumbnail (Floating overlay)
                    Box(
                        modifier = Modifier
                            .width(100.dp)
                            .aspectRatio(2f / 3f)
                            .clip(RoundedCornerShape(12.dp))
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(item.posterUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = item.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.titleLarge,
                            color = TextPrimary,
                            fontWeight = FontWeight.Black,
                            fontSize = 19.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = Color(0xFFFFC107),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = item.rating,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "${item.year} • ${item.resolution}",
                                color = JellyfinCyan,
                                fontWeight = FontWeight.Medium,
                                fontSize = 12.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = item.audioCodec,
                            color = TextMuted,
                            fontSize = 11.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Genre badges
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(item.genres) { genre ->
                                Surface(
                                    color = JellyfinSurfaceVariant,
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        text = genre,
                                        color = TextMuted,
                                        fontSize = 10.sp,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Actions Row
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            TvFocusableCard(
                                onClick = { onPlay(item) },
                                testTag = "btn_play_detail"
                            ) {
                                Surface(
                                    color = JellyfinCyan,
                                    shape = RoundedCornerShape(20.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PlayArrow,
                                            contentDescription = "Play",
                                            tint = Color.White
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Play",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                    }
                                }
                            }

                            TvFocusableCard(
                                onClick = onToggleFavorite,
                                testTag = "btn_favorite_detail"
                            ) {
                                Surface(
                                    color = if (isFavorite) JellyfinPurple.copy(alpha = 0.3f) else JellyfinSurfaceVariant,
                                    shape = CircleShape
                                ) {
                                    Box(
                                        modifier = Modifier.padding(10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = if (isFavorite) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                            contentDescription = "Favorite",
                                            tint = if (isFavorite) JellyfinPurple else Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }

                            // Download Action Button
                            TvFocusableCard(
                                onClick = {
                                    if (downloadState?.downloadStatus == "COMPLETED") {
                                        onDeleteDownload(item.id)
                                    } else if (downloadState == null) {
                                        itemToDownload = Pair(item, null)
                                        showQualityDialog = true
                                    }
                                },
                                testTag = "btn_download_detail"
                            ) {
                                Surface(
                                    color = when (downloadState?.downloadStatus) {
                                        "COMPLETED" -> Color(0xFF2E7D32).copy(alpha = 0.3f)
                                        "DOWNLOADING" -> JellyfinCyan.copy(alpha = 0.2f)
                                        else -> JellyfinSurfaceVariant
                                    },
                                    shape = CircleShape
                                ) {
                                    Box(
                                        modifier = Modifier.padding(10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        when (downloadState?.downloadStatus) {
                                            "COMPLETED" -> {
                                                Icon(
                                                    imageVector = Icons.Default.CheckCircle,
                                                    contentDescription = "Downloaded",
                                                    tint = Color(0xFF81C784),
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                            "DOWNLOADING" -> {
                                                CircularProgressIndicator(
                                                    progress = { (downloadState.progressPercent / 100f) },
                                                    modifier = Modifier.size(20.dp),
                                                    color = JellyfinCyan,
                                                    trackColor = Color.White.copy(alpha = 0.2f),
                                                    strokeWidth = 2.dp
                                                )
                                            }
                                            else -> {
                                                Icon(
                                                    imageVector = Icons.Default.FileDownload,
                                                    contentDescription = "Download",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Overview Synopsis
                Text(
                    text = "Overview",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = item.overview,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMuted,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )

                // Cast List Section
                if (item.cast.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "Cast & Crew",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        items(item.cast) { member ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .background(JellyfinCardBackground, RoundedCornerShape(12.dp))
                                    .padding(8.dp)
                            ) {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(member.imageUrl)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = member.name,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(JellyfinSurfaceVariant)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = member.name,
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        text = member.role,
                                        color = TextMuted,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // Episodes Section (If TV Series)
                if (item.mediaType == MediaType.SERIES) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Episodes (Season 1)",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "${episodes.size} Episodes",
                            color = JellyfinCyan,
                            fontSize = 12.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        episodes.forEach { episode ->
                            TvFocusableCard(
                                onClick = { onPlayEpisode(episode) },
                                modifier = Modifier.fillMaxWidth(),
                                testTag = "episode_item_${episode.id}"
                            ) { isFocused ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(JellyfinCardBackground, RoundedCornerShape(12.dp))
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .width(100.dp)
                                            .height(60.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                    ) {
                                        AsyncImage(
                                            model = ImageRequest.Builder(LocalContext.current)
                                                .data(episode.thumbnailUrl)
                                                .crossfade(true)
                                                .build(),
                                            contentDescription = episode.title,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                        Icon(
                                            imageVector = Icons.Default.PlayArrow,
                                            contentDescription = "Play",
                                            tint = Color.White,
                                            modifier = Modifier
                                                .align(Alignment.Center)
                                                .size(24.dp)
                                                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = episode.title,
                                            color = TextPrimary,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                        Text(
                                            text = episode.overview,
                                            color = TextMuted,
                                            fontSize = 11.sp,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Related Movies Section
                if (relatedMovies.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "Related Movies",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(relatedMovies) { related ->
                            MediaPosterCard(
                                item = related,
                                onClick = { onItemClick(related) },
                                modifier = Modifier.width(110.dp)
                            )
                        }
                    }
                }

                // Related Shows Section
                if (relatedShows.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "Related Shows",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(relatedShows) { related ->
                            MediaPosterCard(
                                item = related,
                                onClick = { onItemClick(related) },
                                modifier = Modifier.width(110.dp)
                            )
                        }
                    }
                }

                // Suggested For You Section
                if (suggestedForYou.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "Suggested For You",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(suggestedForYou) { suggested ->
                            MediaPosterCard(
                                item = suggested,
                                onClick = { onItemClick(suggested) },
                                modifier = Modifier.width(110.dp)
                            )
                        }
                    }
                }

                // More Like This Section
                if (moreLikeThis.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "More Like This",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(moreLikeThis) { itemLike ->
                            MediaPosterCard(
                                item = itemLike,
                                onClick = { onItemClick(itemLike) },
                                modifier = Modifier.width(110.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
