package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.db.MediaProgressEntity
import com.example.data.model.JellyfinItem
import com.example.data.model.MediaType
import com.example.ui.components.ContinueWatchingCard
import com.example.ui.components.LibraryTileCard
import com.example.ui.components.MediaPosterCard
import com.example.ui.components.TvFocusableCard
import com.example.ui.theme.JellyfinBackground
import com.example.ui.theme.JellyfinRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@Composable
fun HomeScreen(
    heroItem: JellyfinItem,
    continueWatchingList: List<MediaProgressEntity>,
    movies: List<JellyfinItem>,
    series: List<JellyfinItem>,
    allItems: List<JellyfinItem>,
    onMediaClick: (JellyfinItem) -> Unit,
    onPlayMedia: (JellyfinItem) -> Unit,
    onProgressClick: (MediaProgressEntity) -> Unit,
    onNavigateToFilter: (LibraryFilter) -> Unit = {},
    onOpenDrawer: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    // Spotlight carousel combining top movies & TV series posters
    val spotlightItems = remember(heroItem, movies, series) {
        (listOf(heroItem) + movies + series).distinctBy { it.id }.take(7)
    }
    var activeHeroIndex by remember { mutableIntStateOf(0) }

    // Auto-slide hero poster every 5 seconds
    LaunchedEffect(spotlightItems) {
        while (isActive) {
            delay(5000L)
            if (spotlightItems.isNotEmpty()) {
                activeHeroIndex = (activeHeroIndex + 1) % spotlightItems.size
            }
        }
    }

    val currentHero = spotlightItems.getOrElse(activeHeroIndex) { heroItem }
    // Dynamic Jellyfin Section Categories
    val topRatedItems = (allItems.ifEmpty { movies + series }).sortedByDescending { it.rating.toDoubleOrNull() ?: 0.0 }.take(12)

    val actionSciFi = (allItems.ifEmpty { movies + series }).filter { item ->
        item.genres.any { g -> g.contains("Action", true) || g.contains("Sci-Fi", true) || g.contains("Cyberpunk", true) }
    }.ifEmpty { (movies + series).take(8) }

    val dramaThriller = (allItems.ifEmpty { movies + series }).filter { item ->
        item.genres.any { g -> g.contains("Drama", true) || g.contains("Thriller", true) || g.contains("Mystery", true) }
    }.ifEmpty { (movies + series).drop(2).take(8) }

    val adventureFantasy = (allItems.ifEmpty { movies + series }).filter { item ->
        item.genres.any { g -> g.contains("Adventure", true) || g.contains("Fantasy", true) }
    }.ifEmpty { (movies + series).reversed().take(8) }

    val comedyAnimation = (allItems.ifEmpty { movies + series }).filter { item ->
        item.genres.any { g -> g.contains("Comedy", true) || g.contains("Animation", true) }
    }.ifEmpty { (movies + series).drop(1).take(8) }

    val favoriteItems = allItems.filter { it.isFavorite }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(JellyfinBackground)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
        // 1. Full-bleed Auto-Sliding Hero Carousel (3 Seconds Transition)
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(560.dp)
            ) {
                AnimatedContent(
                    targetState = currentHero,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "hero_carousel"
                ) { activeItem ->
                    Box(modifier = Modifier.fillMaxSize()) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(activeItem.backdropUrl.ifEmpty { activeItem.posterUrl })
                                .crossfade(true)
                                .build(),
                            contentDescription = activeItem.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )

                        // Top & Bottom Gradient Shadows
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Black.copy(alpha = 0.5f),
                                            Color.Transparent,
                                            JellyfinBackground.copy(alpha = 0.7f),
                                            JellyfinBackground
                                        )
                                    )
                                )
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(
                                            JellyfinBackground.copy(alpha = 0.85f),
                                            Color.Transparent
                                        )
                                    )
                                )
                        )

                        // Hero Details Pane
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(horizontal = 24.dp, vertical = 28.dp)
                                .fillMaxWidth(0.92f)
                        ) {
                            // FEATURED pill tag + Metadata line
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    color = JellyfinRed,
                                    shape = RoundedCornerShape(3.dp)
                                ) {
                                    Text(
                                        text = "SPOTLIGHT",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 9.sp,
                                        letterSpacing = 1.2.sp,
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Text(
                                    text = "${activeItem.year} • ${if (activeItem.mediaType == MediaType.SERIES) "SERIES" else "MOVIE"} • ${activeItem.rating} ★",
                                    color = Color.White.copy(alpha = 0.9f),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    letterSpacing = 0.8.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Hero Title
                            Text(
                                text = activeItem.title,
                                fontStyle = FontStyle.Normal,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = if (activeItem.title.length > 20) 28.sp else 36.sp,
                                lineHeight = if (activeItem.title.length > 20) 34.sp else 42.sp,
                                color = Color.White,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                letterSpacing = (-0.5).sp
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Overview / Synopsis
                            Text(
                                text = activeItem.overview,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFFDDDDDD),
                                fontSize = 12.sp,
                                lineHeight = 17.sp,
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            // Hero Buttons: Solid PLAY TRAILER + Translucent ADD WATCHLIST
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                TvFocusableCard(
                                    onClick = { onPlayMedia(activeItem) },
                                    testTag = "hero_btn_play"
                                ) {
                                    Surface(
                                        color = JellyfinRed,
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = "PLAY NOW",
                                            color = Color.White,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 11.sp,
                                            letterSpacing = 1.sp,
                                            modifier = Modifier.padding(horizontal = 22.dp, vertical = 11.dp)
                                        )
                                    }
                                }

                                TvFocusableCard(
                                    onClick = { onMediaClick(activeItem) },
                                    testTag = "hero_btn_details"
                                ) {
                                    Surface(
                                        color = Color.Black.copy(alpha = 0.5f),
                                        shape = RoundedCornerShape(4.dp),
                                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f))
                                    ) {
                                        Text(
                                            text = "+ DETAILS",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            letterSpacing = 1.sp,
                                            modifier = Modifier.padding(horizontal = 18.dp, vertical = 11.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Top Right Action Icons
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 28.dp, end = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(18.dp))
                    Icon(
                        imageVector = Icons.Outlined.Notifications,
                        contentDescription = "Notifications",
                        tint = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Auto-slide 3-Second Poster Indicator Dots at bottom center of hero
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    spotlightItems.indices.forEach { index ->
                        val isSelected = index == activeHeroIndex
                        Box(
                            modifier = Modifier
                                .height(4.dp)
                                .width(if (isSelected) 22.dp else 8.dp)
                                .background(
                                    color = if (isSelected) JellyfinRed else Color.White.copy(alpha = 0.4f),
                                    shape = CircleShape
                                )
                                .clickable { activeHeroIndex = index }
                        )
                    }
                }
            }
        }

        // 2. My Media Libraries
        item {
            MediaSectionHeader(title = "My Media")
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    LibraryTileCard(
                        title = "Movies",
                        countText = "${movies.size} Titles",
                        icon = Icons.Default.Movie,
                        accentColor = JellyfinRed,
                        onClick = { onNavigateToFilter(LibraryFilter.MOVIES) }
                    )
                }
                item {
                    LibraryTileCard(
                        title = "TV Shows",
                        countText = "${series.size} Series",
                        icon = Icons.Default.Tv,
                        accentColor = Color.White,
                        onClick = { onNavigateToFilter(LibraryFilter.SERIES) }
                    )
                }
                item {
                    LibraryTileCard(
                        title = "Favorites",
                        countText = "${favoriteItems.size} Saved",
                        icon = Icons.Default.Bookmark,
                        accentColor = JellyfinRed,
                        onClick = { onNavigateToFilter(LibraryFilter.FAVORITES) }
                    )
                }
            }
        }

        // 3. Recently Played Section
        if (continueWatchingList.isNotEmpty()) {
            item {
                MediaSectionHeader(title = "Recently Played")
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(continueWatchingList) { progress ->
                        ContinueWatchingCard(
                            progress = progress,
                            onClick = { onProgressClick(progress) }
                        )
                    }
                }
            }
        }

        // 4. Recently Added Movies
        if (movies.isNotEmpty()) {
            item {
                MediaSectionHeader(title = "Recently Added Movies")
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(movies) { item ->
                        MediaPosterCard(
                            item = item,
                            onClick = { onMediaClick(item) }
                        )
                    }
                }
            }
        }

        // 5. Recently Added TV Shows
        if (series.isNotEmpty()) {
            item {
                MediaSectionHeader(title = "Recently Added TV Shows")
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(series) { item ->
                        MediaPosterCard(
                            item = item,
                            onClick = { onMediaClick(item) }
                        )
                    }
                }
            }
        }

        // 6. Top Rated & Trending
        if (topRatedItems.isNotEmpty()) {
            item {
                MediaSectionHeader(title = "Top Rated & Trending")
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(topRatedItems) { item ->
                        MediaPosterCard(
                            item = item,
                            onClick = { onMediaClick(item) }
                        )
                    }
                }
            }
        }

        // 7. Action & Sci-Fi
        if (actionSciFi.isNotEmpty()) {
            item {
                MediaSectionHeader(title = "Action & Sci-Fi")
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(actionSciFi) { item ->
                        MediaPosterCard(
                            item = item,
                            onClick = { onMediaClick(item) }
                        )
                    }
                }
            }
        }

        // 8. Drama & Mystery
        if (dramaThriller.isNotEmpty()) {
            item {
                MediaSectionHeader(title = "Drama & Thriller")
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(dramaThriller) { item ->
                        MediaPosterCard(
                            item = item,
                            onClick = { onMediaClick(item) }
                        )
                    }
                }
            }
        }

        // 9. Adventure & Fantasy
        if (adventureFantasy.isNotEmpty()) {
            item {
                MediaSectionHeader(title = "Adventure & Fantasy")
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(adventureFantasy) { item ->
                        MediaPosterCard(
                            item = item,
                            onClick = { onMediaClick(item) }
                        )
                    }
                }
            }
        }

        // 10. Comedies & Animation
        if (comedyAnimation.isNotEmpty()) {
            item {
                MediaSectionHeader(title = "Comedies & Animation")
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(comedyAnimation) { item ->
                        MediaPosterCard(
                            item = item,
                            onClick = { onMediaClick(item) }
                        )
                    }
                }
            }
        }

        // 11. My Favorites Section
        if (favoriteItems.isNotEmpty()) {
            item {
                MediaSectionHeader(title = "My Favorites")
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(favoriteItems) { item ->
                        MediaPosterCard(
                            item = item,
                            onClick = { onMediaClick(item) }
                        )
                    }
                }
            }
        }
    }

        // Floating Red Action / Menu Button (3-lines menu icon revealing drawer)
        FloatingActionButton(
            onClick = onOpenDrawer,
            shape = CircleShape,
            containerColor = JellyfinRed,
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 24.dp, end = 20.dp)
                .size(56.dp)
                .testTag("floating_menu_btn")
        ) {
            Icon(
                imageVector = Icons.Default.Menu,
                contentDescription = "Menu",
                modifier = Modifier.size(26.dp)
            )
        }
    }
}

@Composable
fun MediaSectionHeader(title: String, onSeeAll: (() -> Unit)? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, top = 26.dp, bottom = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontStyle = FontStyle.Italic,
            fontWeight = FontWeight.SemiBold,
            fontSize = 18.sp,
            color = Color.White
        )
        Text(
            text = "VIEW ALL",
            fontSize = 10.sp,
            color = TextMuted,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
    }
}
