package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.JellyfinItem
import com.example.data.model.MediaType
import com.example.ui.components.MediaPosterCard
import com.example.ui.components.TvFocusableCard
import com.example.ui.theme.JellyfinBackground
import com.example.ui.theme.JellyfinCyan
import com.example.ui.theme.JellyfinPurple
import com.example.ui.theme.JellyfinSurfaceVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary

enum class LibraryFilter(val label: String, val icon: ImageVector, val mediaType: MediaType?) {
    ALL("All Library", Icons.Default.FilterList, null),
    MOVIES("Movies", Icons.Default.Movie, MediaType.MOVIE),
    SERIES("TV Series", Icons.Default.Tv, MediaType.SERIES),
    FAVORITES("Favorites", Icons.Default.Bookmark, null)
}

@Composable
fun LibraryScreen(
    itemsList: List<JellyfinItem>,
    selectedFilter: LibraryFilter,
    onFilterSelect: (LibraryFilter) -> Unit,
    onMediaClick: (JellyfinItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(JellyfinBackground)
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        // Filter Chips Row
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(LibraryFilter.entries) { filter ->
                val isSelected = selectedFilter == filter
                TvFocusableCard(
                    onClick = { onFilterSelect(filter) },
                    testTag = "filter_chip_${filter.name.lowercase()}"
                ) {
                    Surface(
                        color = if (isSelected) JellyfinCyan else JellyfinSurfaceVariant,
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = filter.icon,
                                contentDescription = filter.label,
                                tint = if (isSelected) Color.White else TextMuted,
                                modifier = Modifier.height(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = filter.label,
                                color = if (isSelected) Color.White else TextMuted,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Items Grid
        if (itemsList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Movie,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.height(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No media items found for '${selectedFilter.label}'",
                        color = TextMuted,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 130.dp),
                contentPadding = PaddingValues(bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(itemsList) { item ->
                    MediaPosterCard(
                        item = item,
                        onClick = { onMediaClick(item) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
