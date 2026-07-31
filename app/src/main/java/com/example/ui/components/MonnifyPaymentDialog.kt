package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.db.MonnifyConfigEntity
import com.example.data.model.JellyfinItem
import com.example.ui.theme.JellyfinBackground
import com.example.ui.theme.JellyfinCardBackground
import com.example.ui.theme.JellyfinCyan
import com.example.ui.theme.JellyfinPurple
import com.example.ui.theme.JellyfinSurfaceVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary

enum class MonnifyPlanType {
    SINGLE_STREAM,
    VIP_PASS
}

enum class MonnifyPaymentChannel {
    CARD,
    BANK_TRANSFER,
    USSD,
    WEB_PORTAL
}

@Composable
fun MonnifyPaymentDialog(
    item: JellyfinItem,
    config: MonnifyConfigEntity,
    userEmail: String,
    userName: String,
    onDismiss: () -> Unit,
    onPaymentSuccess: (item: JellyfinItem, planType: MonnifyPlanType, paymentRef: String, method: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedPlan by remember { mutableStateOf(MonnifyPlanType.VIP_PASS) }
    var selectedChannel by remember { mutableStateOf(MonnifyPaymentChannel.CARD) }

    val activeAmount = 600.0

    // Card Form state
    var cardNumber by remember { mutableStateOf("5399 4100 8821 9902") }
    var cardExpiry by remember { mutableStateOf("12/28") }
    var cardCvv by remember { mutableStateOf("882") }
    var cardHolderName by remember { mutableStateOf(userName.ifBlank { "Stream Subscriber" }) }

    // Status / Processing
    var isProcessing by remember { mutableStateOf(false) }
    var processMessage by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var paymentSuccessNotice by remember { mutableStateOf(false) }

    // Virtual Account state
    val virtualAccountNo = remember { "603" + (10000000..99999999).random() }
    val bankName = remember { "Wema Bank / Monnify Gateway" }
    val accountName = remember { "Cinode Media - Monnify" }

    // USSD state
    var selectedBankUssd by remember { mutableStateOf("GTBank (*737*)") }
    val ussdCode = remember(selectedBankUssd, activeAmount) {
        val prefix = when {
            selectedBankUssd.contains("GTBank") -> "*737*33*"
            selectedBankUssd.contains("Zenith") -> "*966*33*"
            selectedBankUssd.contains("Access") -> "*901*33*"
            else -> "*894*33*"
        }
        "$prefix${activeAmount.toInt()}*${config.contractCode.takeLast(6)}#"
    }

    val clipboardManager = LocalClipboardManager.current
    var copiedNotice by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = { if (!isProcessing) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = modifier
                .fillMaxWidth(0.92f)
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(24.dp),
            color = JellyfinBackground,
            border = borderGradient()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                // Header Bar with Monnify Branding
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Brush.linearGradient(listOf(JellyfinCyan, JellyfinPurple))),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Payments,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Monnify",
                                    color = JellyfinCyan,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(if (config.useSandbox) Color(0xFFFF9800) else Color(0xFF4CAF50))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = if (config.useSandbox) "TEST MODE" else "LIVE GATEWAY",
                                        color = Color.Black,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }
                            Text(
                                text = "Paywall Protected Stream",
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        enabled = !isProcessing
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Media Banner Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(JellyfinCardBackground)
                        .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                        .padding(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(item.posterUrl.ifEmpty { item.backdropUrl })
                                .crossfade(true)
                                .build(),
                            contentDescription = item.title,
                            modifier = Modifier
                                .size(width = 60.dp, height = 80.dp)
                                .clip(RoundedCornerShape(10.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(JellyfinPurple.copy(alpha = 0.3f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = item.mediaType.name,
                                    color = JellyfinCyan,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = item.title,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Unlock full high-definition stream & offline access",
                                color = TextMuted,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Step 1: Access Plan
                Text(
                    text = "1. ACCESS PLAN",
                    color = TextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Single All-Access Plan Card
                PlanCard(
                    title = "All Library Access",
                    priceText = "₦600 NGN",
                    subtitle = "Unlimited streaming & downloads for all movies and series",
                    isSelected = true,
                    badge = "ALL ACCESS",
                    onClick = { selectedPlan = MonnifyPlanType.VIP_PASS },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Step 2: Choose Payment Method Channel
                Text(
                    text = "2. MONNIFY PAYMENT METHOD",
                    color = TextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ChannelChip(
                        label = "Card",
                        icon = Icons.Default.CreditCard,
                        isSelected = selectedChannel == MonnifyPaymentChannel.CARD,
                        onClick = { selectedChannel = MonnifyPaymentChannel.CARD },
                        modifier = Modifier.weight(1f)
                    )
                    ChannelChip(
                        label = "Transfer",
                        icon = Icons.Default.AccountBalance,
                        isSelected = selectedChannel == MonnifyPaymentChannel.BANK_TRANSFER,
                        onClick = { selectedChannel = MonnifyPaymentChannel.BANK_TRANSFER },
                        modifier = Modifier.weight(1f)
                    )
                    ChannelChip(
                        label = "USSD",
                        icon = Icons.Default.PhoneAndroid,
                        isSelected = selectedChannel == MonnifyPaymentChannel.USSD,
                        onClick = { selectedChannel = MonnifyPaymentChannel.USSD },
                        modifier = Modifier.weight(1f)
                    )
                    ChannelChip(
                        label = "Portal",
                        icon = Icons.Default.OpenInNew,
                        isSelected = selectedChannel == MonnifyPaymentChannel.WEB_PORTAL,
                        onClick = { selectedChannel = MonnifyPaymentChannel.WEB_PORTAL },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Content Panel according to Channel
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(JellyfinCardBackground)
                        .border(1.dp, JellyfinCyan.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                        .padding(14.dp)
                ) {
                    when (selectedChannel) {
                        MonnifyPaymentChannel.CARD -> {
                            Column {
                                Text(
                                    text = "Pay via Debit / Credit Card (Monnify Direct)",
                                    color = JellyfinCyan,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(10.dp))

                                OutlinedTextField(
                                    value = cardNumber,
                                    onValueChange = { cardNumber = it },
                                    label = { Text("Card Number", color = TextMuted) },
                                    leadingIcon = { Icon(Icons.Default.CreditCard, contentDescription = null, tint = JellyfinCyan) },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = monnifyTextFieldColors(),
                                    singleLine = true
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = cardExpiry,
                                        onValueChange = { cardExpiry = it },
                                        label = { Text("Expiry (MM/YY)", color = TextMuted) },
                                        modifier = Modifier.weight(1f),
                                        colors = monnifyTextFieldColors(),
                                        singleLine = true
                                    )
                                    OutlinedTextField(
                                        value = cardCvv,
                                        onValueChange = { cardCvv = it },
                                        label = { Text("CVV", color = TextMuted) },
                                        visualTransformation = PasswordVisualTransformation(),
                                        modifier = Modifier.weight(1f),
                                        colors = monnifyTextFieldColors(),
                                        singleLine = true
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedTextField(
                                    value = cardHolderName,
                                    onValueChange = { cardHolderName = it },
                                    label = { Text("Cardholder Name", color = TextMuted) },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = monnifyTextFieldColors(),
                                    singleLine = true
                                )
                            }
                        }

                        MonnifyPaymentChannel.BANK_TRANSFER -> {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.AccountBalance, contentDescription = null, tint = JellyfinCyan, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Monnify Dedicated Virtual Bank Account",
                                        color = JellyfinCyan,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(
                                    text = "Transfer exactly ₦${activeAmount.toInt()} NGN from your bank app to complete payment instantly.",
                                    color = TextMuted,
                                    fontSize = 11.sp
                                )
                                Spacer(modifier = Modifier.height(12.dp))

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(JellyfinSurfaceVariant)
                                        .padding(12.dp)
                                ) {
                                    Column {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text("BANK NAME", color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                                Text(bankName, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(JellyfinCyan.copy(alpha = 0.2f))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text("AUTO-VERIFY", color = JellyfinCyan, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text("ACCOUNT NUMBER", color = TextMuted, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                                Text(virtualAccountNo, color = JellyfinCyan, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                                            }
                                            IconButton(
                                                onClick = {
                                                    clipboardManager.setText(AnnotatedString(virtualAccountNo))
                                                    copiedNotice = "Account number copied!"
                                                }
                                            ) {
                                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = JellyfinCyan)
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("ACCOUNT NAME: $accountName", color = TextMuted, fontSize = 11.sp)
                                    }
                                }
                            }
                        }

                        MonnifyPaymentChannel.USSD -> {
                            Column {
                                Text(
                                    text = "Dial Monnify USSD Shortcode",
                                    color = JellyfinCyan,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    listOf("GTBank (*737*)", "Zenith (*966*)", "Access (*901*)", "FirstBank (*894*)").forEach { bank ->
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (selectedBankUssd == bank) JellyfinPurple else JellyfinSurfaceVariant)
                                                .clickable { selectedBankUssd = bank }
                                                .padding(horizontal = 8.dp, vertical = 6.dp)
                                        ) {
                                            Text(
                                                text = bank.takeWhile { it != ' ' },
                                                color = if (selectedBankUssd == bank) Color.White else TextMuted,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(JellyfinSurfaceVariant)
                                        .padding(12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = ussdCode,
                                            color = JellyfinCyan,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        IconButton(
                                            onClick = {
                                                clipboardManager.setText(AnnotatedString(ussdCode))
                                                copiedNotice = "USSD code copied!"
                                            }
                                        ) {
                                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy USSD", tint = JellyfinCyan)
                                        }
                                    }
                                }
                            }
                        }

                        MonnifyPaymentChannel.WEB_PORTAL -> {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "Monnify Hosted Web Checkout",
                                    color = JellyfinCyan,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Pay via Monnify standard checkout URL. You will be redirected back upon completion.",
                                    color = TextMuted,
                                    fontSize = 11.sp,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(JellyfinSurfaceVariant)
                                        .padding(10.dp)
                                ) {
                                    Text(
                                        text = "${if (config.useSandbox) "https://sandbox.monnify.com" else "https://api.monnify.com"}/checkout/MNFY_REF_${System.currentTimeMillis()}",
                                        color = JellyfinCyan,
                                        fontSize = 11.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }

                if (copiedNotice != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(copiedNotice!!, color = Color(0xFF4CAF50), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(errorMessage!!, color = Color(0xFFFF5252), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Payment Action Button
                if (isProcessing) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(color = JellyfinCyan, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = processMessage.ifBlank { "Communicating with Monnify Gateway..." },
                            color = TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                } else if (paymentSuccessNotice) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(28.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Payment Approved! Unlocking Stream...",
                            color = Color(0xFF4CAF50),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Button(
                        onClick = {
                            isProcessing = true
                            processMessage = "Initializing Monnify Reference..."
                            errorMessage = null

                            val paymentRef = "MNFY_REF_" + System.currentTimeMillis()
                            val methodStr = selectedChannel.name

                            // Trigger complete Monnify flow
                            onPaymentSuccess(item, selectedPlan, paymentRef, methodStr)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = JellyfinCyan,
                            contentColor = Color.Black
                        )
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "PAY ₦${activeAmount.toInt()} NGN VIA MONNIFY",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Footer Security Badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Shield, contentDescription = null, tint = TextMuted, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Secured end-to-end by Monnify Payment Infrastructure",
                        color = TextMuted,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun PlanCard(
    title: String,
    priceText: String,
    subtitle: String,
    isSelected: Boolean,
    badge: String? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(if (isSelected) JellyfinPurple.copy(alpha = 0.35f) else JellyfinCardBackground)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) JellyfinCyan else Color.White.copy(alpha = 0.1f),
                shape = RoundedCornerShape(14.dp)
            )
            .clickable { onClick() }
            .padding(12.dp)
    ) {
        Column {
            if (badge != null) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(JellyfinCyan)
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text(badge, color = Color.Black, fontSize = 8.sp, fontWeight = FontWeight.ExtraBold)
                }
                Spacer(modifier = Modifier.height(4.dp))
            }
            Text(title, color = if (isSelected) JellyfinCyan else TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(2.dp))
            Text(priceText, color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)
            Spacer(modifier = Modifier.height(2.dp))
            Text(subtitle, color = TextMuted, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun ChannelChip(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) JellyfinCyan else JellyfinCardBackground)
            .border(
                width = 1.dp,
                color = if (isSelected) JellyfinCyan else Color.White.copy(alpha = 0.1f),
                shape = RoundedCornerShape(10.dp)
            )
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) Color.Black else TextMuted,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                color = if (isSelected) Color.Black else TextMuted,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun monnifyTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = JellyfinCyan,
    unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
    focusedLabelColor = JellyfinCyan,
    unfocusedLabelColor = TextMuted,
    focusedTextColor = TextPrimary,
    unfocusedTextColor = TextPrimary,
    cursorColor = JellyfinCyan,
    focusedContainerColor = JellyfinSurfaceVariant,
    unfocusedContainerColor = JellyfinSurfaceVariant
)

private fun borderGradient() = androidx.compose.foundation.BorderStroke(
    width = 1.5.dp,
    brush = Brush.linearGradient(listOf(JellyfinCyan.copy(alpha = 0.6f), JellyfinPurple.copy(alpha = 0.6f)))
)
