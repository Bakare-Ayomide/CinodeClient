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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.JellyfinAccentGlow
import com.example.ui.theme.JellyfinBackground
import com.example.ui.theme.JellyfinCyan
import com.example.ui.theme.JellyfinSurfaceVariant

@Composable
fun TvRemoteOverlay(
    onNavigateUp: () -> Unit,
    onNavigateDown: () -> Unit,
    onNavigateLeft: () -> Unit,
    onNavigateRight: () -> Unit,
    onSelect: () -> Unit,
    onBack: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .testTag("tv_remote_overlay")
            .width(180.dp),
        color = JellyfinBackground.copy(alpha = 0.95f),
        shape = RoundedCornerShape(24.dp),
        tonalElevation = 16.dp,
        shadowElevation = 12.dp
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TV REMOTE",
                    style = MaterialTheme.typography.labelMedium,
                    color = JellyfinCyan,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    modifier = Modifier.padding(start = 6.dp)
                )
                IconButton(
                    onClick = onClose,
                    modifier = Modifier.size(24.dp)
                ) {
                    Text("✕", color = Color.White, fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // D-Pad Cross
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // UP
                IconButton(
                    onClick = onNavigateUp,
                    modifier = Modifier
                        .testTag("remote_up")
                        .size(38.dp)
                        .background(JellyfinSurfaceVariant, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowUp,
                        contentDescription = "Up",
                        tint = Color.White
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    // LEFT
                    IconButton(
                        onClick = onNavigateLeft,
                        modifier = Modifier
                            .testTag("remote_left")
                            .size(38.dp)
                            .background(JellyfinSurfaceVariant, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowLeft,
                            contentDescription = "Left",
                            tint = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // OK / SELECT
                    Box(
                        modifier = Modifier
                            .testTag("remote_select")
                            .size(44.dp)
                            .background(JellyfinCyan, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        IconButton(onClick = onSelect) {
                            Text(
                                text = "OK",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // RIGHT
                    IconButton(
                        onClick = onNavigateRight,
                        modifier = Modifier
                            .testTag("remote_right")
                            .size(38.dp)
                            .background(JellyfinSurfaceVariant, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowRight,
                            contentDescription = "Right",
                            tint = Color.White
                        )
                    }
                }

                // DOWN
                IconButton(
                    onClick = onNavigateDown,
                    modifier = Modifier
                        .testTag("remote_down")
                        .size(38.dp)
                        .background(JellyfinSurfaceVariant, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Down",
                        tint = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Bottom Buttons (BACK / HOME)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .testTag("remote_back")
                        .size(34.dp)
                        .background(JellyfinSurfaceVariant, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
