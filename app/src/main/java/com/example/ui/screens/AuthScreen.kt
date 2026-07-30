package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DownloadForOffline
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.TvFocusableCard
import com.example.ui.theme.JellyfinBackground
import com.example.ui.theme.JellyfinCardBackground
import com.example.ui.theme.JellyfinCyan
import com.example.ui.theme.JellyfinPurple
import com.example.ui.theme.JellyfinRed
import com.example.ui.theme.JellyfinSurfaceVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary

enum class AuthTab {
    LOGIN,
    SIGNUP
}

data class OnboardingPage(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val badge: String,
    val accentColor: Color
)

@Composable
fun OnboardingScreen(
    onFinishOnboarding: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentPage by remember { mutableIntStateOf(0) }

    val pages = listOf(
        OnboardingPage(
            title = "Welcome to Jellyfin Cinema",
            subtitle = "Stream your entire personal movie and TV collection in pristine 4K HDR with spatial surround audio across all your devices.",
            icon = Icons.Default.Movie,
            badge = "PERSONAL MEDIA HUB",
            accentColor = JellyfinCyan
        ),
        OnboardingPage(
            title = "Offline Downloads & Smart Sync",
            subtitle = "Download full movies and TV episodes directly to in-app storage. Enjoy uninterrupted viewing when traveling without internet.",
            icon = Icons.Default.DownloadForOffline,
            badge = "OFFLINE PLAYBACK",
            accentColor = JellyfinPurple
        ),
        OnboardingPage(
            title = "Smart TV & Remote Control",
            subtitle = "Switch seamlessly between Mobile Touch and Android TV D-Pad navigation, or cast directly to smart displays with one tap.",
            icon = Icons.Default.Tv,
            badge = "CROSS-DEVICE CINEMA",
            accentColor = JellyfinRed
        )
    )

    val activePage = pages[currentPage]

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(JellyfinBackground)
    ) {
        // Top Skip Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(
                onClick = onFinishOnboarding,
                modifier = Modifier.testTag("btn_onboarding_skip")
            ) {
                Text(
                    text = "Skip Intro",
                    color = TextMuted,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }

        // Main Slide Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp, vertical = 60.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Icon Badge Graphic
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(activePage.accentColor.copy(alpha = 0.15f))
                    .border(2.dp, activePage.accentColor.copy(alpha = 0.4f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = activePage.icon,
                    contentDescription = null,
                    tint = activePage.accentColor,
                    modifier = Modifier.size(56.dp)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            Surface(
                color = activePage.accentColor.copy(alpha = 0.2f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = activePage.badge,
                    color = activePage.accentColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = activePage.title,
                color = TextPrimary,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 26.sp,
                textAlign = TextAlign.Center,
                lineHeight = 32.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = activePage.subtitle,
                color = TextMuted,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp,
                modifier = Modifier.width(340.dp)
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Indicator Dots
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                pages.indices.forEach { index ->
                    val isSelected = index == currentPage
                    Box(
                        modifier = Modifier
                            .height(8.dp)
                            .width(if (isSelected) 24.dp else 8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isSelected) activePage.accentColor else Color.White.copy(alpha = 0.2f))
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Action Button
            TvFocusableCard(
                onClick = {
                    if (currentPage < pages.size - 1) {
                        currentPage++
                    } else {
                        onFinishOnboarding()
                    }
                },
                testTag = "btn_onboarding_next"
            ) {
                Surface(
                    color = activePage.accentColor,
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 32.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (currentPage < pages.size - 1) "Continue" else "Get Started",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AuthScreen(
    onAuthSuccess: (email: String, name: String) -> Unit,
    onGuestLogin: () -> Unit,
    onOpenOnboarding: () -> Unit,
    modifier: Modifier = Modifier
) {
    var activeTab by remember { mutableStateOf(AuthTab.LOGIN) }

    // Form states
    var loginUsername by remember { mutableStateOf("") }
    var loginPassword by remember { mutableStateOf("") }
    var serverUrl by remember { mutableStateOf("https://demo.jellyfin.org/stable") }
    var rememberMe by remember { mutableStateOf(true) }

    var signupName by remember { mutableStateOf("") }
    var signupEmail by remember { mutableStateOf("") }
    var signupUsername by remember { mutableStateOf("") }
    var signupPassword by remember { mutableStateOf("") }
    var signupConfirmPassword by remember { mutableStateOf("") }
    var acceptTerms by remember { mutableStateOf(true) }

    var isPasswordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(JellyfinBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // Header Brand Logo
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(JellyfinCyan, JellyfinPurple)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Jellyfin Logo",
                    tint = Color.White,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "JELLYFIN MEDIA SERVER",
                color = JellyfinCyan,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 12.sp,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = if (activeTab == AuthTab.LOGIN) "Sign In to Your Server" else "Create New Jellyfin Account",
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Connect to your media library or try demo mode",
                color = TextMuted,
                fontSize = 13.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Segmented Tab Switcher
            Surface(
                color = JellyfinSurfaceVariant,
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .width(300.dp)
                    .height(46.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(4.dp)
                ) {
                    // Login Tab
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize()
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (activeTab == AuthTab.LOGIN) JellyfinCyan else Color.Transparent)
                            .clickable {
                                activeTab = AuthTab.LOGIN
                                errorMessage = null
                            }
                            .testTag("tab_auth_login"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Sign In",
                            color = if (activeTab == AuthTab.LOGIN) Color.White else TextMuted,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    // Signup Tab
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize()
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (activeTab == AuthTab.SIGNUP) JellyfinCyan else Color.Transparent)
                            .clickable {
                                activeTab = AuthTab.SIGNUP
                                errorMessage = null
                            }
                            .testTag("tab_auth_signup"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Sign Up",
                            color = if (activeTab == AuthTab.SIGNUP) Color.White else TextMuted,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Error / Success Banners
            if (errorMessage != null) {
                Surface(
                    color = JellyfinRed.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Text(
                        text = errorMessage!!,
                        color = JellyfinRed,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(12.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }

            if (successMessage != null) {
                Surface(
                    color = Color(0xFF2E7D32).copy(alpha = 0.2f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Text(
                        text = successMessage!!,
                        color = Color(0xFF81C784),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(12.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Form Container
            Surface(
                color = JellyfinCardBackground,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .width(420.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    if (activeTab == AuthTab.LOGIN) {
                        // --- LOGIN FORM ---
                        Text(
                            text = "Jellyfin Server URL",
                            color = TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = serverUrl,
                            onValueChange = { serverUrl = it },
                            placeholder = { Text("https://your-server.com", color = TextMuted) },
                            leadingIcon = { Icon(Icons.Default.Router, contentDescription = null, tint = JellyfinCyan) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = JellyfinCyan,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_server_url")
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Username or Email",
                            color = TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = loginUsername,
                            onValueChange = { loginUsername = it },
                            placeholder = { Text("e.g. demo_user", color = TextMuted) },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = JellyfinCyan) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = JellyfinCyan,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_login_username")
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Password",
                            color = TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = loginPassword,
                            onValueChange = { loginPassword = it },
                            placeholder = { Text("••••••••", color = TextMuted) },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = JellyfinCyan) },
                            trailingIcon = {
                                IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                    Icon(
                                        imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = "Toggle password visibility",
                                        tint = TextMuted
                                    )
                                }
                            },
                            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = JellyfinCyan,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_login_password")
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Checkbox(
                                checked = rememberMe,
                                onCheckedChange = { rememberMe = it },
                                colors = CheckboxDefaults.colors(checkedColor = JellyfinCyan)
                            )
                            Text(
                                text = "Keep me signed in on this device",
                                color = TextMuted,
                                fontSize = 12.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Submit Login Button
                        TvFocusableCard(
                            onClick = {
                                if (loginUsername.isBlank()) {
                                    errorMessage = "Please enter your username or email address."
                                    return@TvFocusableCard
                                }
                                isLoading = true
                                errorMessage = null
                                onAuthSuccess(loginUsername, loginUsername.takeWhile { it != '@' }.capitalize())
                            },
                            modifier = Modifier.fillMaxWidth(),
                            testTag = "btn_submit_login"
                        ) {
                            Surface(
                                color = JellyfinCyan,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Box(
                                    modifier = Modifier.padding(vertical = 14.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isLoading) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(20.dp),
                                            color = Color.White,
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        Text(
                                            text = "Sign In",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp
                                        )
                                    }
                                }
                            }
                        }

                    } else {
                        // --- SIGN UP FORM ---
                        Text(
                            text = "Full Name",
                            color = TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = signupName,
                            onValueChange = { signupName = it },
                            placeholder = { Text("Alex Morgan", color = TextMuted) },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = JellyfinCyan) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = JellyfinCyan,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_signup_name")
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Email Address",
                            color = TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = signupEmail,
                            onValueChange = { signupEmail = it },
                            placeholder = { Text("alex@example.com", color = TextMuted) },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = JellyfinCyan) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = JellyfinCyan,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_signup_email")
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Password",
                            color = TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(
                            value = signupPassword,
                            onValueChange = { signupPassword = it },
                            placeholder = { Text("Minimum 6 characters", color = TextMuted) },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = JellyfinCyan) },
                            visualTransformation = PasswordVisualTransformation(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = JellyfinCyan,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_signup_password")
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Checkbox(
                                checked = acceptTerms,
                                onCheckedChange = { acceptTerms = it },
                                colors = CheckboxDefaults.colors(checkedColor = JellyfinCyan)
                            )
                            Text(
                                text = "I accept terms of service & privacy policy",
                                color = TextMuted,
                                fontSize = 12.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Submit Signup Button
                        TvFocusableCard(
                            onClick = {
                                if (signupEmail.isBlank() || signupName.isBlank()) {
                                    errorMessage = "Please complete all required fields."
                                    return@TvFocusableCard
                                }
                                isLoading = true
                                errorMessage = null
                                onAuthSuccess(signupEmail, signupName)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            testTag = "btn_submit_signup"
                        ) {
                            Surface(
                                color = JellyfinPurple,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Box(
                                    modifier = Modifier.padding(vertical = 14.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Create Account & Sign In",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Quick Guest Demo Button
                    TvFocusableCard(
                        onClick = onGuestLogin,
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "btn_guest_demo_login"
                    ) {
                        Surface(
                            color = JellyfinSurfaceVariant,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = JellyfinCyan,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Continue with Guest Demo Mode",
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Re-watch Onboarding feature link
            TextButton(
                onClick = onOpenOnboarding,
                modifier = Modifier.testTag("btn_view_onboarding_intro")
            ) {
                Text(
                    text = "View Product Feature Tour",
                    color = JellyfinCyan,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
