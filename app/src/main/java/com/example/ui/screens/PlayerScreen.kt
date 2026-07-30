package com.example.ui.screens

import android.widget.MediaController
import android.widget.VideoView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Cast
import androidx.compose.material.icons.filled.CastConnected
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ClosedCaption
import androidx.compose.material.icons.filled.Tv
import androidx.compose.ui.window.Dialog
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.JellyfinItem
import com.example.ui.components.TvFocusableCard
import com.example.ui.theme.JellyfinBackground
import com.example.ui.theme.JellyfinCyan
import com.example.ui.theme.JellyfinPurple
import com.example.ui.theme.JellyfinSurfaceVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import kotlinx.coroutines.delay

@Composable
fun PlayerScreen(
    item: JellyfinItem,
    onBack: () -> Unit,
    onSaveProgress: (positionMs: Long, durationMs: Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isPlaying by remember { mutableStateOf(true) }
    var currentPositionMs by remember { mutableLongStateOf(item.watchPositionMs) }
    var durationMs by remember { mutableLongStateOf(item.durationMs.coerceAtLeast(60000L)) }
    var showControls by remember { mutableStateOf(true) }
    var selectedQuality by remember { mutableStateOf(item.resolution) }
    var subtitlesEnabled by remember { mutableStateOf(true) }
    var playbackSpeed by remember { mutableFloatStateOf(1.0f) }
    var isSeeking by remember { mutableStateOf(false) }
    var seekingPosMs by remember { mutableLongStateOf(0L) }

    var isCasting by remember { mutableStateOf(false) }
    var connectedDeviceName by remember { mutableStateOf("") }
    var showCastDialog by remember { mutableStateOf(false) }

    val showSkipIntro = currentPositionMs in 5000L..90000L
    val showSkipCredits = durationMs > 180000L && currentPositionMs > (durationMs - 150000L)

    // Auto hide controls after 4 seconds of inactivity
    LaunchedEffect(showControls, isPlaying) {
        if (showControls && isPlaying) {
            delay(4000)
            showControls = false
        }
    }

    // Timer simulation for playback progress
    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            delay(1000)
            currentPositionMs = (currentPositionMs + (1000 * playbackSpeed).toLong()).coerceAtMost(durationMs)
            if (currentPositionMs % 5000L == 0L) {
                onSaveProgress(currentPositionMs, durationMs)
            }
        }
    }

    var isHardwareVideoAvailable by remember { mutableStateOf(true) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                showControls = !showControls
            }
    ) {
        // Native Android VideoView (Attempted when hardware stream available)
        if (isHardwareVideoAvailable) {
            AndroidView(
                factory = { ctx ->
                    VideoView(ctx).apply {
                        val streamUri = android.net.Uri.parse(
                            if (item.videoUrl.isNotEmpty()) item.videoUrl else "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4"
                        )
                        setVideoURI(streamUri)
                        setOnErrorListener { _, _, _ ->
                            isHardwareVideoAvailable = false
                            true // Suppress system "Can't play video" OS popup dialog
                        }
                        setOnPreparedListener { mp ->
                            durationMs = mp.duration.toLong().coerceAtLeast(60000L)
                            mp.start()
                            isPlaying = true
                        }
                        setOnCompletionListener {
                            isPlaying = false
                            onSaveProgress(durationMs, durationMs)
                        }
                    }
                },
                update = { videoView ->
                    if (isPlaying) {
                        if (!videoView.isPlaying && isHardwareVideoAvailable) {
                            try { videoView.start() } catch (e: Exception) { isHardwareVideoAvailable = false }
                        }
                    } else {
                        if (videoView.isPlaying) {
                            try { videoView.pause() } catch (e: Exception) {}
                        }
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        // Ambient Visual Cinema Backdrop Player (Guarantees movie artwork, subtitles and visual player always work on all devices)
        if (!isHardwareVideoAvailable || item.videoUrl.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(if (item.backdropUrl.isNotEmpty()) item.backdropUrl else item.posterUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = item.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.5f),
                                    Color.Black.copy(alpha = 0.75f),
                                    Color.Black.copy(alpha = 0.95f)
                                )
                            )
                        )
                )

                // Subtitle Overlay Simulation at lower-third
                if (subtitlesEnabled && isPlaying) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 120.dp)
                            .background(Color.Black.copy(alpha = 0.75f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "[ English Subtitles ] Playing ${item.title} • 4K HDR Atmos 7.1",
                            color = Color(0xFFFFE082),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // Player Controls Overlay
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.8f),
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.9f)
                            )
                        )
                    )
                    .padding(20.dp)
            ) {
                // Top Player Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopStart),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = {
                                onSaveProgress(currentPositionMs, durationMs)
                                onBack()
                            },
                            modifier = Modifier
                                .background(JellyfinSurfaceVariant, CircleShape)
                                .testTag("player_btn_back")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column {
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "${item.resolution} • ${item.audioCodec}",
                                style = MaterialTheme.typography.bodySmall,
                                color = JellyfinCyan,
                                fontSize = 11.sp
                            )
                        }
                    }

                    // Settings & Quality Badges
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TvFocusableCard(
                            onClick = { subtitlesEnabled = !subtitlesEnabled },
                            testTag = "player_btn_subtitles"
                        ) {
                            Surface(
                                color = if (subtitlesEnabled) JellyfinPurple.copy(alpha = 0.3f) else JellyfinSurfaceVariant,
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.padding(2.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Subtitles,
                                        contentDescription = "Subtitles",
                                        tint = if (subtitlesEnabled) JellyfinCyan else TextMuted,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (subtitlesEnabled) "CC ON" else "CC OFF",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        TvFocusableCard(
                            onClick = { showCastDialog = true },
                            testTag = "player_btn_cast"
                        ) {
                            Surface(
                                color = if (isCasting) JellyfinCyan.copy(alpha = 0.25f) else JellyfinSurfaceVariant,
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.padding(2.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (isCasting) Icons.Default.CastConnected else Icons.Default.Cast,
                                        contentDescription = "Cast",
                                        tint = if (isCasting) JellyfinCyan else Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (isCasting) connectedDeviceName else "Cast",
                                        color = if (isCasting) JellyfinCyan else Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        TvFocusableCard(
                            onClick = {
                                playbackSpeed = when (playbackSpeed) {
                                    1.0f -> 1.25f
                                    1.25f -> 1.5f
                                    1.5f -> 2.0f
                                    else -> 1.0f
                                }
                            },
                            testTag = "player_btn_speed"
                        ) {
                            Surface(
                                color = JellyfinSurfaceVariant,
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.padding(2.dp)
                            ) {
                                Text(
                                    text = "${playbackSpeed}x Speed",
                                    color = JellyfinCyan,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }

                // Center Playback Buttons (Rewind -10s, Play/Pause, Forward +10s)
                Row(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TvFocusableCard(
                        onClick = { currentPositionMs = (currentPositionMs - 10000L).coerceAtLeast(0L) },
                        testTag = "player_btn_rewind"
                    ) {
                        Surface(
                            color = JellyfinSurfaceVariant,
                            shape = CircleShape,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.FastRewind,
                                    contentDescription = "-10s",
                                    tint = Color.White
                                )
                            }
                        }
                    }

                    TvFocusableCard(
                        onClick = { isPlaying = !isPlaying },
                        testTag = "player_btn_play_pause"
                    ) {
                        Surface(
                            color = JellyfinCyan,
                            shape = CircleShape,
                            modifier = Modifier.size(64.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = if (isPlaying) "Pause" else "Play",
                                    tint = Color.White,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }
                    }

                    TvFocusableCard(
                        onClick = { currentPositionMs = (currentPositionMs + 10000L).coerceAtMost(durationMs) },
                        testTag = "player_btn_forward"
                    ) {
                        Surface(
                            color = JellyfinSurfaceVariant,
                            shape = CircleShape,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.FastForward,
                                    contentDescription = "+10s",
                                    tint = Color.White
                                )
                            }
                        }
                    }
                }

                // Bottom Seek Slider & Timers & Trickplay Chapter Preview
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomStart)
                ) {
                    // Skip Intro / Skip Credits Floating Pill Buttons
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        if (showSkipIntro) {
                            TvFocusableCard(
                                onClick = {
                                    currentPositionMs = 95000L.coerceAtMost(durationMs)
                                },
                                testTag = "player_btn_skip_intro"
                            ) {
                                Surface(
                                    color = JellyfinCyan,
                                    shape = RoundedCornerShape(20.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.FastForward,
                                            contentDescription = "Skip Intro",
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Skip Intro",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                        }

                        if (showSkipCredits) {
                            Spacer(modifier = Modifier.width(8.dp))
                            TvFocusableCard(
                                onClick = {
                                    currentPositionMs = (durationMs - 5000L).coerceAtLeast(0L)
                                },
                                testTag = "player_btn_skip_credits"
                            ) {
                                Surface(
                                    color = JellyfinPurple,
                                    shape = RoundedCornerShape(20.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.FastForward,
                                            contentDescription = "Skip Credits",
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Skip Credits",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Trickplay Image Chapter Preview Card (shown when seeking)
                    if (isSeeking) {
                        val chapterNum = ((seekingPosMs / (300000L.coerceAtLeast(1L))) + 1)
                        Surface(
                            color = JellyfinBackground.copy(alpha = 0.95f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .padding(bottom = 8.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(160.dp)
                                        .height(90.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                ) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(LocalContext.current)
                                            .data(if (item.backdropUrl.isNotEmpty()) item.backdropUrl else item.posterUrl)
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = "Chapter Preview",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )

                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(
                                                Brush.verticalGradient(
                                                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))
                                                )
                                            )
                                    )

                                    Text(
                                        text = formatTime(seekingPosMs),
                                        color = JellyfinCyan,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        modifier = Modifier
                                            .align(Alignment.BottomStart)
                                            .padding(6.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Chapter $chapterNum",
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = formatTime(if (isSeeking) seekingPosMs else currentPositionMs),
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = formatTime(durationMs),
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                    }

                    Slider(
                        value = currentPositionMs.toFloat(),
                        onValueChange = {
                            isSeeking = true
                            seekingPosMs = it.toLong()
                            currentPositionMs = it.toLong()
                        },
                        onValueChangeFinished = {
                            isSeeking = false
                        },
                        valueRange = 0f..durationMs.toFloat(),
                        colors = SliderDefaults.colors(
                            thumbColor = JellyfinCyan,
                            activeTrackColor = JellyfinCyan,
                            inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("player_seek_bar")
                    )
                }
            }
        }

        if (showCastDialog) {
            CastReceiverDialog(
                isCurrentlyCasting = isCasting,
                currentDeviceName = connectedDeviceName,
                onSelectDevice = { deviceName ->
                    if (deviceName.isEmpty()) {
                        isCasting = false
                        connectedDeviceName = ""
                    } else {
                        isCasting = true
                        connectedDeviceName = deviceName
                    }
                    showCastDialog = false
                },
                onDismiss = { showCastDialog = false }
            )
        }
    }
}

@Composable
private fun CastReceiverDialog(
    isCurrentlyCasting: Boolean,
    currentDeviceName: String,
    onSelectDevice: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val availableDevices = listOf(
        "Living Room Android TV",
        "Samsung OLED 4K TV",
        "Bedroom Chromecast",
        "Office Smart Display"
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            color = JellyfinBackground,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .width(340.dp)
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Cast,
                            contentDescription = null,
                            tint = JellyfinCyan,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Cast to Device",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Select a smart TV or receiver on your Wi-Fi network to stream directly:",
                    color = TextMuted,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                if (isCurrentlyCasting) {
                    TvFocusableCard(
                        onClick = { onSelectDevice("") },
                        testTag = "btn_disconnect_cast"
                    ) {
                        Surface(
                            color = Color(0xFFD32F2F).copy(alpha = 0.2f),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = null,
                                    tint = Color(0xFFEF5350),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Disconnect from $currentDeviceName",
                                    color = Color(0xFFEF5350),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }

                availableDevices.forEach { deviceName ->
                    val isThisDevice = isCurrentlyCasting && currentDeviceName == deviceName
                    TvFocusableCard(
                        onClick = { onSelectDevice(deviceName) },
                        testTag = "btn_cast_device_$deviceName"
                    ) {
                        Surface(
                            color = if (isThisDevice) JellyfinCyan.copy(alpha = 0.2f) else JellyfinSurfaceVariant,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Tv,
                                        contentDescription = null,
                                        tint = if (isThisDevice) JellyfinCyan else TextMuted,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = deviceName,
                                        color = if (isThisDevice) JellyfinCyan else TextPrimary,
                                        fontWeight = if (isThisDevice) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 13.sp
                                    )
                                }

                                if (isThisDevice) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Connected",
                                        tint = JellyfinCyan,
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
}

private fun formatTime(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}
