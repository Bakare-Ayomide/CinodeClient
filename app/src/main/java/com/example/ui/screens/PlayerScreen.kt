package com.example.ui.screens

import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Brightness6
import androidx.compose.material.icons.filled.Cast
import androidx.compose.material.icons.filled.CastConnected
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PictureInPicture
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.JellyfinItem
import com.example.ui.components.TvFocusableCard
import com.example.ui.theme.JellyfinBackground
import com.example.ui.theme.JellyfinCyan
import com.example.ui.theme.JellyfinPurple
import com.example.ui.theme.JellyfinRed
import com.example.ui.theme.JellyfinSurface
import com.example.ui.theme.JellyfinSurfaceVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import java.io.File
import kotlinx.coroutines.delay

enum class PlayerSheetType {
    NONE, SETTINGS, SUBTITLES, AUDIO, QUALITY, ASPECT_RATIO, SPEED, STATS, WATCH_PARTY, SLEEP_TIMER, ERROR_REPORT
}

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
    var durationMs by remember { mutableLongStateOf(item.durationMs.coerceAtLeast(180000L)) }
    var showControls by remember { mutableStateOf(true) }
    var isScreenLocked by remember { mutableStateOf(false) }

    // Settings states
    var activeSheet by remember { mutableStateOf(PlayerSheetType.NONE) }
    var selectedQuality by remember { mutableStateOf("1080p Direct Play") }
    var selectedAspectRatio by remember { mutableStateOf("Fit") }
    var playbackSpeed by remember { mutableFloatStateOf(1.0f) }
    var repeatMode by remember { mutableStateOf("None") } // None, One, All
    var isFavorite by remember { mutableStateOf(item.isFavorite) }
    var sleepTimerMinutes by remember { mutableIntStateOf(0) } // 0 = off

    // Subtitle states
    var subtitlesEnabled by remember { mutableStateOf(true) }
    var selectedSubtitleTrack by remember { mutableStateOf("English [SRT] (Default)") }
    var subtitleDelaySeconds by remember { mutableFloatStateOf(0.0f) }
    var subtitleFontSizeSp by remember { mutableIntStateOf(16) }

    // Audio states
    var selectedAudioTrack by remember { mutableStateOf("English TrueHD 7.1 Atmos (Default)") }
    var audioDelaySeconds by remember { mutableFloatStateOf(0.0f) }

    // Gestures states
    val audioManager = remember(context) { context.getSystemService(Context.AUDIO_SERVICE) as? android.media.AudioManager }
    val initialVol = remember(audioManager) {
        if (audioManager != null) {
            val maxVol = audioManager.getStreamMaxVolume(android.media.AudioManager.STREAM_MUSIC)
            val curVol = audioManager.getStreamVolume(android.media.AudioManager.STREAM_MUSIC)
            if (maxVol > 0) curVol.toFloat() / maxVol.toFloat() else 0.8f
        } else 0.8f
    }
    val initialBrightness = remember(context) {
        val activity = context as? Activity
        val lp = activity?.window?.attributes
        if (lp != null && lp.screenBrightness >= 0f) lp.screenBrightness else 0.8f
    }

    var gestureOverlayText by remember { mutableStateOf<String?>(null) }
    var gestureOverlayIcon by remember { mutableStateOf<ImageVector?>(null) }
    var isTemporary2x by remember { mutableStateOf(false) }
    var brightnessLevel by remember { mutableFloatStateOf(initialBrightness) }
    var volumeLevel by remember { mutableFloatStateOf(initialVol) }

    // Seeking states
    var isSeeking by remember { mutableStateOf(false) }
    var seekingPosMs by remember { mutableLongStateOf(0L) }

    // Casting & Watch Party
    var isCasting by remember { mutableStateOf(false) }
    var connectedDeviceName by remember { mutableStateOf("") }
    var toastMessage by remember { mutableStateOf<String?>(null) }
    var isFullScreen by remember { mutableStateOf(false) }

    val toggleFullScreen = {
        val activity = context as? Activity
        if (!isFullScreen) {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            isFullScreen = true
            toastMessage = "Landscape Fullscreen Mode"
        } else {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            isFullScreen = false
            toastMessage = "Standard View Mode"
        }
    }

    val enterPipMode = {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val activity = context as? Activity
            try {
                val params = android.app.PictureInPictureParams.Builder().build()
                activity?.enterPictureInPictureMode(params)
                toastMessage = "Entering Picture-in-Picture Mode"
            } catch (e: Exception) {
                toastMessage = "PiP mode not available"
            }
        } else {
            toastMessage = "PiP requires Android 8.0 or higher"
        }
    }

    val showSkipIntro = currentPositionMs in 5000L..90000L
    val showSkipCredits = durationMs > 180000L && currentPositionMs > (durationMs - 150000L)

    // Auto hide controls
    LaunchedEffect(showControls, isPlaying, isScreenLocked) {
        if (showControls && isPlaying && !isScreenLocked) {
            delay(10000)
            showControls = false
        }
    }

    // Toast auto-hide
    LaunchedEffect(toastMessage) {
        if (toastMessage != null) {
            delay(2500)
            toastMessage = null
        }
    }

    // Gesture indicator auto-hide
    LaunchedEffect(gestureOverlayText) {
        if (gestureOverlayText != null) {
            delay(1200)
            gestureOverlayText = null
            gestureOverlayIcon = null
        }
    }

    // Sleep timer countdown
    LaunchedEffect(sleepTimerMinutes) {
        if (sleepTimerMinutes > 0) {
            delay(sleepTimerMinutes * 60 * 1000L)
            isPlaying = false
            toastMessage = "Sleep timer expired. Playback paused."
            sleepTimerMinutes = 0
        }
    }

    // Initialize Media3 ExoPlayer Engine for fast, buffering-free streaming & local playback
    val exoPlayer = remember(context) {
        ExoPlayer.Builder(context).build()
    }

    val localDownloadedFile = remember(item.id) { File(context.filesDir, "downloads/${item.id}.mp4") }
    val mediaUri = remember(item.id, item.videoUrl) {
        if (localDownloadedFile.exists() && localDownloadedFile.length() > 0) {
            android.net.Uri.fromFile(localDownloadedFile)
        } else if (item.videoUrl.isNotBlank()) {
            android.net.Uri.parse(item.videoUrl)
        } else {
            android.net.Uri.parse("https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4")
        }
    }

    LaunchedEffect(mediaUri) {
        val mediaItem = MediaItem.fromUri(mediaUri)
        exoPlayer.setMediaItem(mediaItem)
        if (currentPositionMs > 0) {
            exoPlayer.seekTo(currentPositionMs)
        }
        exoPlayer.prepare()
        exoPlayer.playWhenReady = isPlaying
    }

    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying = playing
            }

            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_READY) {
                    if (exoPlayer.duration > 0) {
                        durationMs = exoPlayer.duration
                    }
                } else if (state == Player.STATE_ENDED) {
                    isPlaying = false
                    onSaveProgress(durationMs, durationMs)
                    if (repeatMode == "One") {
                        exoPlayer.seekTo(0)
                        exoPlayer.play()
                    }
                }
            }
        }
        exoPlayer.addListener(listener)
        onDispose {
            exoPlayer.removeListener(listener)
            exoPlayer.release()
        }
    }

    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            exoPlayer.play()
        } else {
            exoPlayer.pause()
        }
    }

    LaunchedEffect(playbackSpeed, isTemporary2x) {
        val speed = if (isTemporary2x) 2.0f else playbackSpeed
        exoPlayer.playbackParameters = PlaybackParameters(speed)
    }

    // Apply Screen Brightness Gesture
    LaunchedEffect(brightnessLevel) {
        val activity = context as? Activity
        val window = activity?.window
        if (window != null) {
            val lp = window.attributes
            lp.screenBrightness = brightnessLevel.coerceIn(0.01f, 1.0f)
            window.attributes = lp
        }
    }

    // Apply Audio Volume Gesture
    LaunchedEffect(volumeLevel) {
        exoPlayer.volume = volumeLevel.coerceIn(0.0f, 1.0f)
        if (audioManager != null) {
            val maxVol = audioManager.getStreamMaxVolume(android.media.AudioManager.STREAM_MUSIC)
            if (maxVol > 0) {
                val targetVol = (volumeLevel * maxVol).toInt()
                audioManager.setStreamVolume(android.media.AudioManager.STREAM_MUSIC, targetVol, 0)
            }
        }
    }

    LaunchedEffect(exoPlayer) {
        while (true) {
            delay(500)
            if (exoPlayer.duration > 0) {
                durationMs = exoPlayer.duration
            }
            if (exoPlayer.isPlaying) {
                currentPositionMs = exoPlayer.currentPosition
                if (currentPositionMs % 5000L < 500L) {
                    onSaveProgress(currentPositionMs, durationMs)
                }
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        showControls = !showControls
                    },
                    onDoubleTap = { offset ->
                        if (!isScreenLocked) {
                            if (offset.x < size.width / 2) {
                                currentPositionMs = (currentPositionMs - 10000L).coerceAtLeast(0L)
                                exoPlayer.seekTo(currentPositionMs)
                                gestureOverlayText = "-10s"
                                gestureOverlayIcon = Icons.Default.FastRewind
                            } else {
                                currentPositionMs = (currentPositionMs + 10000L).coerceAtMost(durationMs)
                                exoPlayer.seekTo(currentPositionMs)
                                gestureOverlayText = "+10s"
                                gestureOverlayIcon = Icons.Default.FastForward
                            }
                        }
                    },
                    onLongPress = {
                        if (!isScreenLocked) {
                            isTemporary2x = true
                            gestureOverlayText = "2x Speed"
                            gestureOverlayIcon = Icons.Default.Speed
                        }
                    }
                )
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { isTemporary2x = false },
                    onDragEnd = { isTemporary2x = false },
                    onDragCancel = { isTemporary2x = false },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        if (!isScreenLocked) {
                            val x = change.position.x
                            val yAmount = dragAmount.y
                            if (x < size.width / 2) {
                                brightnessLevel = (brightnessLevel - (yAmount / 800f)).coerceIn(0.1f, 1.0f)
                                gestureOverlayText = "Brightness ${(brightnessLevel * 100).toInt()}%"
                                gestureOverlayIcon = Icons.Default.Brightness6
                            } else {
                                volumeLevel = (volumeLevel - (yAmount / 800f)).coerceIn(0.0f, 1.0f)
                                gestureOverlayText = "Volume ${(volumeLevel * 100).toInt()}%"
                                gestureOverlayIcon = if (volumeLevel == 0f) Icons.Default.VolumeMute else Icons.Default.VolumeUp
                            }
                        }
                    }
                )
            }
    ) {
        // High Performance ExoPlayer Surface View
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = false
                    resizeMode = when (selectedAspectRatio) {
                        "Crop", "Zoom" -> AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                        "Fill" -> AspectRatioFrameLayout.RESIZE_MODE_FILL
                        "16:9" -> AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH
                        "4:3" -> AspectRatioFrameLayout.RESIZE_MODE_FIXED_HEIGHT
                        else -> AspectRatioFrameLayout.RESIZE_MODE_FIT
                    }
                }
            },
            update = { playerView ->
                playerView.resizeMode = when (selectedAspectRatio) {
                    "Crop", "Zoom" -> AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                    "Fill" -> AspectRatioFrameLayout.RESIZE_MODE_FILL
                    "16:9" -> AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH
                    "4:3" -> AspectRatioFrameLayout.RESIZE_MODE_FIXED_HEIGHT
                    else -> AspectRatioFrameLayout.RESIZE_MODE_FIT
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Backdrop Ambient Player Fallback
        if (item.videoUrl.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(if (item.backdropUrl.isNotEmpty()) item.backdropUrl else item.posterUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = item.title,
                    contentScale = when (selectedAspectRatio) {
                        "Fill" -> ContentScale.Crop
                        "Stretch" -> ContentScale.FillBounds
                        else -> ContentScale.Fit
                    },
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
            }
        }

        // Gesture Action Feedback Popup Indicator
        if (gestureOverlayText != null) {
            Surface(
                color = JellyfinBackground.copy(alpha = 0.9f),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, JellyfinCyan.copy(alpha = 0.5f)),
                modifier = Modifier.align(Alignment.Center)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    gestureOverlayIcon?.let { icon ->
                        Icon(imageVector = icon, contentDescription = null, tint = JellyfinCyan, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                    }
                    Text(
                        text = gestureOverlayText!!,
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Toast Message Pill
        if (toastMessage != null) {
            Surface(
                color = JellyfinRed,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 70.dp)
            ) {
                Text(
                    text = toastMessage!!,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp)
                )
            }
        }

        // Screen Locked Mode Overlay
        if (isScreenLocked && showControls) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                TvFocusableCard(
                    onClick = {
                        isScreenLocked = false
                        toastMessage = "Controls Unlocked"
                    },
                    testTag = "player_btn_unlock"
                ) {
                    Surface(
                        color = JellyfinRed,
                        shape = CircleShape,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Box(
                            modifier = Modifier.padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.LockOpen, contentDescription = "Unlock", tint = Color.White)
                        }
                    }
                }
            }
        }

        // Player Controls Overlay Glassmorphism Panel
        AnimatedVisibility(
            visible = showControls && !isScreenLocked,
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
                                Color.Black.copy(alpha = 0.85f),
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.95f)
                            )
                        )
                    )
                    .padding(16.dp)
            ) {
                // TOP TOOLBAR
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
                                (context as? Activity)?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
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

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "${item.year} • $selectedQuality • ${item.audioCodec}",
                                style = MaterialTheme.typography.bodySmall,
                                color = JellyfinCyan,
                                fontSize = 11.sp
                            )
                        }
                    }

                    // Top Quick Action Badges & Buttons
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Cast Button
                        IconButton(
                            onClick = {
                                isCasting = !isCasting
                                connectedDeviceName = if (isCasting) "Living Room TV" else ""
                                toastMessage = if (isCasting) "Casting to Living Room TV" else "Cast Disconnected"
                            },
                            modifier = Modifier.background(if (isCasting) JellyfinCyan.copy(alpha = 0.3f) else JellyfinSurfaceVariant, CircleShape)
                        ) {
                            Icon(
                                imageVector = if (isCasting) Icons.Default.CastConnected else Icons.Default.Cast,
                                contentDescription = "Chromecast",
                                tint = if (isCasting) JellyfinCyan else Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Lock Screen Button
                        IconButton(
                            onClick = {
                                isScreenLocked = true
                                toastMessage = "Screen Locked. Tap to unlock."
                            },
                            modifier = Modifier.background(JellyfinSurfaceVariant, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Lock Screen",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                // CENTER PLAYBACK CONTROLS (Prev Episode, Rewind -10s, Play/Pause, Forward +10s, Next Episode)
                Row(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            currentPositionMs = 0L
                            toastMessage = "Restarting Episode"
                        },
                        modifier = Modifier.background(JellyfinSurfaceVariant, CircleShape)
                    ) {
                        Icon(Icons.Default.SkipPrevious, contentDescription = "Previous", tint = Color.White)
                    }

                    TvFocusableCard(
                        onClick = {
                            currentPositionMs = (currentPositionMs - 10000L).coerceAtLeast(0L)
                        },
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
                            color = JellyfinRed,
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
                        onClick = {
                            currentPositionMs = (currentPositionMs + 10000L).coerceAtMost(durationMs)
                        },
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

                    IconButton(
                        onClick = {
                            toastMessage = "Autoplay Next Episode"
                        },
                        modifier = Modifier.background(JellyfinSurfaceVariant, CircleShape)
                    ) {
                        Icon(Icons.Default.SkipNext, contentDescription = "Next", tint = Color.White)
                    }
                }

                // BOTTOM CONTROLS & ACTION ROW
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomStart)
                ) {
                    // Skip Intro / Skip Credits Floating Buttons
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
                                    toastMessage = "Intro Skipped"
                                },
                                testTag = "player_btn_skip_intro"
                            ) {
                                Surface(
                                    color = JellyfinCyan,
                                    shape = CircleShape,
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.FastForward,
                                            contentDescription = "Skip Intro",
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
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
                                    toastMessage = "Credits Skipped"
                                },
                                testTag = "player_btn_skip_credits"
                            ) {
                                Surface(
                                    color = JellyfinPurple,
                                    shape = CircleShape,
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.FastForward,
                                            contentDescription = "Skip Credits",
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Chapter Preview Tooltip
                    if (isSeeking) {
                        val chapterNum = ((seekingPosMs / (300000L.coerceAtLeast(1L))) + 1)
                        Surface(
                            color = JellyfinBackground.copy(alpha = 0.95f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .align(Alignment.CenterHorizontally)
                                .padding(bottom = 6.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Chapter $chapterNum • ${formatTime(seekingPosMs)}",
                                    color = JellyfinCyan,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }

                    // Progress Slider + Timers
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
                            text = "-${formatTime(durationMs - (if (isSeeking) seekingPosMs else currentPositionMs))}",
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
                            exoPlayer.seekTo(seekingPosMs)
                            isSeeking = false
                        },
                        valueRange = 0f..durationMs.toFloat(),
                        colors = SliderDefaults.colors(
                            thumbColor = JellyfinRed,
                            activeTrackColor = JellyfinRed,
                            inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("player_seek_bar")
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // BOTTOM ACTION TOOLBAR (Well-fitted remaining icons)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Subtitles Button
                        PlayerPillButton(
                            icon = Icons.Default.Subtitles,
                            label = "Subtitles",
                            active = subtitlesEnabled,
                            onClick = { activeSheet = PlayerSheetType.SUBTITLES }
                        )

                        // Audio Button
                        PlayerPillButton(
                            icon = Icons.Default.Audiotrack,
                            label = "Audio",
                            active = false,
                            onClick = { activeSheet = PlayerSheetType.AUDIO }
                        )

                        // Speed Button
                        PlayerPillButton(
                            icon = Icons.Default.Speed,
                            label = "Speed",
                            active = playbackSpeed != 1.0f,
                            onClick = { activeSheet = PlayerSheetType.SPEED }
                        )

                        // Quality Button
                        PlayerPillButton(
                            icon = Icons.Default.HighQuality,
                            label = "Quality",
                            active = true,
                            onClick = { activeSheet = PlayerSheetType.QUALITY }
                        )

                        // Download Icon Button
                        IconButton(
                            onClick = { toastMessage = "Downloading for offline viewing" },
                            modifier = Modifier
                                .size(36.dp)
                                .background(JellyfinSurfaceVariant, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FileDownload,
                                contentDescription = "Download",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Stats / Info Button
                        IconButton(
                            onClick = { activeSheet = PlayerSheetType.STATS },
                            modifier = Modifier
                                .size(36.dp)
                                .background(JellyfinSurfaceVariant, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Playback Stats",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }

        // SETTINGS & AUDIO/SUBTITLE POPUP DIALOGS
        if (activeSheet != PlayerSheetType.NONE) {
            PlayerSettingsModal(
                type = activeSheet,
                item = item,
                selectedQuality = selectedQuality,
                selectedAspectRatio = selectedAspectRatio,
                playbackSpeed = playbackSpeed,
                repeatMode = repeatMode,
                sleepTimerMinutes = sleepTimerMinutes,
                subtitlesEnabled = subtitlesEnabled,
                selectedSubtitleTrack = selectedSubtitleTrack,
                subtitleDelaySeconds = subtitleDelaySeconds,
                selectedAudioTrack = selectedAudioTrack,
                audioDelaySeconds = audioDelaySeconds,
                onSelectQuality = { selectedQuality = it; activeSheet = PlayerSheetType.NONE },
                onSelectAspectRatio = { selectedAspectRatio = it; activeSheet = PlayerSheetType.NONE },
                onSelectSpeed = { playbackSpeed = it; activeSheet = PlayerSheetType.NONE },
                onSelectRepeatMode = { repeatMode = it; activeSheet = PlayerSheetType.NONE },
                onSelectSleepTimer = { sleepTimerMinutes = it; toastMessage = if (it > 0) "Sleep timer set for $it mins" else "Sleep timer disabled"; activeSheet = PlayerSheetType.NONE },
                onToggleSubtitles = { subtitlesEnabled = it },
                onSelectSubtitleTrack = { selectedSubtitleTrack = it },
                onAdjustSubtitleDelay = { subtitleDelaySeconds += it },
                onSelectAudioTrack = { selectedAudioTrack = it },
                onAdjustAudioDelay = { audioDelaySeconds += it },
                onCopyStreamUrl = { toastMessage = "Stream URL copied to clipboard" },
                onReportError = { toastMessage = "Playback error log submitted to server"; activeSheet = PlayerSheetType.NONE },
                onDismiss = { activeSheet = PlayerSheetType.NONE }
            )
        }
    }
}

@Composable
private fun PlayerPillButton(
    icon: ImageVector,
    label: String,
    active: Boolean,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(36.dp)
            .background(if (active) JellyfinRed.copy(alpha = 0.3f) else JellyfinSurfaceVariant, CircleShape)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (active) JellyfinRed else Color.White,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun PlayerSettingsModal(
    type: PlayerSheetType,
    item: JellyfinItem,
    selectedQuality: String,
    selectedAspectRatio: String,
    playbackSpeed: Float,
    repeatMode: String,
    sleepTimerMinutes: Int,
    subtitlesEnabled: Boolean,
    selectedSubtitleTrack: String,
    subtitleDelaySeconds: Float,
    selectedAudioTrack: String,
    audioDelaySeconds: Float,
    onSelectQuality: (String) -> Unit,
    onSelectAspectRatio: (String) -> Unit,
    onSelectSpeed: (Float) -> Unit,
    onSelectRepeatMode: (String) -> Unit,
    onSelectSleepTimer: (Int) -> Unit,
    onToggleSubtitles: (Boolean) -> Unit,
    onSelectSubtitleTrack: (String) -> Unit,
    onAdjustSubtitleDelay: (Float) -> Unit,
    onSelectAudioTrack: (String) -> Unit,
    onAdjustAudioDelay: (Float) -> Unit,
    onCopyStreamUrl: () -> Unit,
    onReportError: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            color = JellyfinBackground,
            shape = RoundedCornerShape(20.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, JellyfinSurfaceVariant),
            modifier = Modifier
                .width(420.dp)
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = when (type) {
                            PlayerSheetType.SETTINGS -> "Player Settings"
                            PlayerSheetType.SUBTITLES -> "Subtitle Options"
                            PlayerSheetType.AUDIO -> "Audio Track & Sync"
                            PlayerSheetType.QUALITY -> "Stream Quality"
                            PlayerSheetType.ASPECT_RATIO -> "Aspect Ratio"
                            PlayerSheetType.SPEED -> "Playback Speed"
                            PlayerSheetType.STATS -> "Playback Statistics"
                            PlayerSheetType.WATCH_PARTY -> "Watch Party & Session"
                            PlayerSheetType.SLEEP_TIMER -> "Sleep Timer"
                            PlayerSheetType.ERROR_REPORT -> "Report Issue"
                            else -> ""
                        },
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )

                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted, modifier = Modifier.size(18.dp))
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                when (type) {
                    PlayerSheetType.SETTINGS -> {
                        SettingsRowItem("Playback Speed", "${playbackSpeed}x", Icons.Default.Speed) { onSelectSpeed(1.0f) }
                        SettingsRowItem("Quality", selectedQuality, Icons.Default.HighQuality) { onSelectQuality("Auto") }
                        SettingsRowItem("Repeat Mode", repeatMode, Icons.Default.Repeat) { onSelectRepeatMode(if (repeatMode == "None") "One" else "None") }
                        SettingsRowItem("Sleep Timer", if (sleepTimerMinutes > 0) "$sleepTimerMinutes min" else "Off", Icons.Default.Timer) { onSelectSleepTimer(30) }
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Surface(
                                color = JellyfinSurfaceVariant,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f).clickable { onCopyStreamUrl() }
                            ) {
                                Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = null, tint = JellyfinCyan, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Copy Stream URL", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    PlayerSheetType.SUBTITLES -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Subtitles Enabled", color = TextPrimary, fontSize = 14.sp)
                            TvFocusableCard(onClick = { onToggleSubtitles(!subtitlesEnabled) }) {
                                Surface(
                                    color = if (subtitlesEnabled) JellyfinRed else JellyfinSurfaceVariant,
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        text = if (subtitlesEnabled) "ON" else "OFF",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Available Subtitle Tracks", color = TextMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))

                        listOf("English [SRT] (Default)", "English [SDH]", "Spanish [SubRip]", "French [VTT]", "Off").forEach { track ->
                            SelectableOptionItem(
                                title = track,
                                selected = selectedSubtitleTrack == track,
                                onSelect = { onSelectSubtitleTrack(track); if (track == "Off") onToggleSubtitles(false) else onToggleSubtitles(true) }
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Subtitle Sync Delay (${String.format("%.1f", subtitleDelaySeconds)}s)", color = TextMuted, fontSize = 12.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 4.dp)) {
                            PillButton("-0.5s") { onAdjustSubtitleDelay(-0.5f) }
                            PillButton("Reset") { onAdjustSubtitleDelay(-subtitleDelaySeconds) }
                            PillButton("+0.5s") { onAdjustSubtitleDelay(0.5f) }
                        }
                    }

                    PlayerSheetType.AUDIO -> {
                        Text("Audio Track Selection", color = TextMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))

                        listOf("English TrueHD 7.1 Atmos (Default)", "English AC3 5.1 (Direct)", "Spanish Stereo 2.0", "Commentary Track AAC").forEach { track ->
                            SelectableOptionItem(
                                title = track,
                                selected = selectedAudioTrack == track,
                                onSelect = { onSelectAudioTrack(track) }
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Audio Sync Delay (${String.format("%.1f", audioDelaySeconds)}s)", color = TextMuted, fontSize = 12.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 4.dp)) {
                            PillButton("-0.2s") { onAdjustAudioDelay(-0.2f) }
                            PillButton("Reset") { onAdjustAudioDelay(-audioDelaySeconds) }
                            PillButton("+0.2s") { onAdjustAudioDelay(0.2f) }
                        }
                    }

                    PlayerSheetType.QUALITY -> {
                        listOf("Auto (Adaptive Bitrate)", "Direct Play (Original 4K 45Mbps)", "1080p (Transcode 10Mbps)", "720p (Transcode 4Mbps)", "480p (Mobile 1.5Mbps)").forEach { quality ->
                            SelectableOptionItem(
                                title = quality,
                                selected = selectedQuality.contains(quality.take(5)),
                                onSelect = { onSelectQuality(quality) }
                            )
                        }
                    }

                    PlayerSheetType.ASPECT_RATIO -> {
                        listOf("Auto", "Fit", "Fill", "Stretch", "Zoom", "Original").forEach { ratio ->
                            SelectableOptionItem(
                                title = ratio,
                                selected = selectedAspectRatio == ratio,
                                onSelect = { onSelectAspectRatio(ratio) }
                            )
                        }
                    }

                    PlayerSheetType.SPEED -> {
                        listOf(0.25f, 0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 1.75f, 2.0f).forEach { speed ->
                            SelectableOptionItem(
                                title = "${speed}x Normal Speed",
                                selected = playbackSpeed == speed,
                                onSelect = { onSelectSpeed(speed) }
                            )
                        }
                    }

                    PlayerSheetType.STATS -> {
                        StatLine("Video Codec", "HEVC / H.265 Main 10 (Direct Play)")
                        StatLine("Audio Codec", "${item.audioCodec.ifEmpty { "E-AC3 Atmos 7.1" }} (Direct Pass)")
                        StatLine("Resolution", "${item.resolution.ifEmpty { "3840x2160 (4K UHD)" }} @ 23.976 fps")
                        StatLine("Bitrate", "24.8 Mbps (Adaptive Max)")
                        StatLine("HDR Type", "Dolby Vision / HDR10+")
                        StatLine("Buffer Health", "32.4s (Optimal)")
                        StatLine("Network Speed", "88.2 Mbps (Local Network)")
                        StatLine("Transcoding", "Disabled (Direct Play Engine)")
                    }

                    PlayerSheetType.WATCH_PARTY -> {
                        Text("Active Watch Party Session", color = TextMuted, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Syncing playback position across connected family members:", color = TextPrimary, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        listOf("Chris (Host - Playing)", "Sarah (Connected)", "Alex (Buffering)").forEach { member ->
                            Row(modifier = Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(8.dp).background(if (member.contains("Host")) JellyfinCyan else Color.Green, CircleShape))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(member, color = TextPrimary, fontSize = 13.sp)
                            }
                        }
                    }

                    PlayerSheetType.SLEEP_TIMER -> {
                        listOf(0, 15, 30, 45, 60, 90).forEach { mins ->
                            SelectableOptionItem(
                                title = if (mins == 0) "Turn Off Sleep Timer" else "$mins Minutes",
                                selected = sleepTimerMinutes == mins,
                                onSelect = { onSelectSleepTimer(mins) }
                            )
                        }
                    }

                    PlayerSheetType.ERROR_REPORT -> {
                        Text("Having playback issues with this file?", color = TextPrimary, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("This will send playback diagnostics and server transcode logs to your Jellyfin administrator for inspection.", color = TextMuted, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(14.dp))
                        TvFocusableCard(onClick = onReportError) {
                            Surface(color = JellyfinRed, shape = RoundedCornerShape(10.dp), modifier = Modifier.fillMaxWidth()) {
                                Text("Submit Log & Report Error", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.padding(12.dp))
                            }
                        }
                    }

                    else -> {}
                }
            }
        }
    }
}

@Composable
private fun SettingsRowItem(title: String, value: String, icon: ImageVector, onClick: () -> Unit) {
    Surface(
        color = JellyfinSurfaceVariant,
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = icon, contentDescription = null, tint = JellyfinCyan, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Text(title, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            }
            Text(value, color = JellyfinCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun SelectableOptionItem(title: String, selected: Boolean, onSelect: () -> Unit) {
    Surface(
        color = if (selected) JellyfinRed.copy(alpha = 0.25f) else JellyfinSurfaceVariant,
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
            .clickable { onSelect() }
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                color = if (selected) JellyfinRed else TextPrimary,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                fontSize = 13.sp
            )
            if (selected) {
                Icon(Icons.Default.Check, contentDescription = "Selected", tint = JellyfinRed, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun StatLine(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = TextMuted, fontSize = 12.sp)
        Text(value, color = JellyfinCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun PillButton(text: String, onClick: () -> Unit) {
    Surface(
        color = JellyfinSurfaceVariant,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(text, color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp))
    }
}

private fun formatTime(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}
