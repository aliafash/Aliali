package com.example.ui

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.data.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun YemenMainView(viewModel: YemenViewModel) {
    val context = LocalContext.current
    val currentScreen by viewModel.currentScreen.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val language by viewModel.language.collectAsState()
    val adminLoggedIn by viewModel.adminLoggedIn.collectAsState()

    // Parse Settings and create Colors depending on Active Theme
    val primaryColor = when (settings.themeName) {
        "COSMIC_SLATE" -> Color(0xFFE2E8F0) // Silver
        "CHARCOAL_GOLD" -> Color(0xFFD4AF37) // Gold
        "ROYAL_EMERALD" -> Color(0xFF097969) // Emerald Green
        else -> Color(0xFFE2E8F0)
    }

    val secondaryColor = when (settings.themeName) {
        "COSMIC_SLATE" -> Color(0xFF64748B) // Slate Grey
        "CHARCOAL_GOLD" -> Color(0xFFF3E5AB) // Pale Gold
        "ROYAL_EMERALD" -> Color(0xFF5F9EA0) // Mint Leaf
        else -> Color(0xFF64748B)
    }

    val backgroundColor = when (settings.themeName) {
        "COSMIC_SLATE" -> Color(0xFF0F172A) // Deep Navy Charcoal
        "CHARCOAL_GOLD" -> Color(0xFF121212) // Carbon Black
        "ROYAL_EMERALD" -> Color(0xFF062319) // Forest Dark Green
        else -> Color(0xFF0F172A)
    }

    val surfaceColor = when (settings.themeName) {
        "COSMIC_SLATE" -> Color(0xFF1E293B) // Slate Navy Card
        "CHARCOAL_GOLD" -> Color(0xFF1C1C1C) // Warm Coal
        "ROYAL_EMERALD" -> Color(0xFF0B3F2E) // Deep Emerald Card
        else -> Color(0xFF1E293B)
    }

    val appFontColor = when (settings.fontColorName) {
        "BRIGHT_WHITE" -> Color(0xFFFFFFFF)
        "LIGHT_GOLD" -> Color(0xFFFFFDD0)
        "VIBRANT_SILVER" -> Color(0xFFE2E8F0)
        else -> Color(0xFFFFFFFF)
    }

    // 5-TAP Backdoor Logic
    var tapCount by remember { mutableStateOf(0) }
    var lastTapTime by remember { mutableStateOf(0L) }
    val showBackdoorDialog by viewModel.showBackdoorDialog.collectAsState()
    var backdoorPasswordInput by remember { mutableStateOf("") }

    val handleHomeTap = {
        val now = System.currentTimeMillis()
        if (now - lastTapTime < 1000) {
            tapCount++
            if (tapCount >= 5) {
                tapCount = 0
                viewModel.showBackdoorDialog.value = true
            }
        } else {
            tapCount = 1
        }
        lastTapTime = now
        viewModel.navigateToHomeDirectly()
    }

    // Floating chatbot state
    var showChatBotOverlay by remember { mutableStateOf(false) }

    // Support Modal dialog state
    var showSupportModal by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = backgroundColor,
        topBar = {
            // High custom Top Bar to meet the Arabic RTL ordered icons request
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(surfaceColor)
                    .statusBarsPadding()
                    .padding(horizontal = 4.dp, vertical = 6.dp)
            ) {
                // Top App Bar row with explicit item sorting order
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Right or Left elements (RTL Arabic Layout)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Title & Logo Tap trigger
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clickable { handleHomeTap() }
                                .padding(paddingValues = PaddingValues(horizontal = 4.dp))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Home,
                                contentDescription = "Home Logo",
                                tint = primaryColor,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = settings.appName,
                                color = appFontColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Ordered utility buttons
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // 🎒 Refresh 🔄
                        IconButton(
                            onClick = { viewModel.refreshData() },
                            modifier = Modifier.testTag("refresh_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh",
                                tint = primaryColor
                            )
                        }

                        // 🌐 Language Switcher
                        IconButton(
                            onClick = { viewModel.toggleLanguage() },
                            modifier = Modifier.testTag("language_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Language,
                                contentDescription = "Language Switcher",
                                tint = primaryColor
                            )
                        }

                        // 👤 Join / Register Professional Page
                        IconButton(
                            onClick = { viewModel.navigateTo(Screen.ProviderRegistration) },
                            modifier = Modifier.testTag("register_professional_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.PersonAdd,
                                contentDescription = "Join Us",
                                tint = primaryColor
                            )
                        }

                        // 🔐 Login Controls (Admin control toggle)
                        IconButton(
                            onClick = {
                                if (adminLoggedIn) {
                                    viewModel.adminLoggedIn.value = false
                                    Toast.makeText(context, "تم تسجيل الخروج من لوحة الإدارة", Toast.LENGTH_SHORT).show()
                                    viewModel.navigateToHomeDirectly()
                                } else {
                                    viewModel.navigateTo(Screen.AdminLogin)
                                }
                            },
                            modifier = Modifier.testTag("login_button")
                        ) {
                            Icon(
                                imageVector = if (adminLoggedIn) Icons.Default.LockOpen else Icons.Default.Lock,
                                contentDescription = "Login Control",
                                tint = if (adminLoggedIn) Color.Green else primaryColor
                            )
                        }
                    }
                }
            }
        },
        bottomBar = {
            // Elegant footer layout supporting customizable elements
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(surfaceColor)
                    .navigationBarsPadding(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Divider(color = secondaryColor.copy(alpha = 0.3f), thickness = 1.dp)
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left informational button
                    IconButton(
                        onClick = { viewModel.navigateTo(Screen.AboutApp) },
                        modifier = Modifier.testTag("info_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "About App Info",
                            tint = primaryColor
                        )
                    }

                    // Middle promotional or branding text
                    if (settings.footerText.isNotEmpty()) {
                        Text(
                            text = settings.footerText,
                            color = primaryColor.copy(alpha = settings.footerOpacity),
                            fontWeight = FontWeight.Bold,
                            fontSize = settings.footerFontSize.sp,
                            modifier = Modifier.clickable {
                                // Call intent trigger
                                try {
                                    val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${settings.supportPhone}"))
                                    context.startActivity(dialIntent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "لا يمكن تشغيل الهاتف", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    } else {
                        Text(
                            text = "خدمات اليمن 🇾🇪",
                            color = primaryColor.copy(alpha = 0.5f),
                            fontSize = 12.sp
                        )
                    }

                    // Right float bubble triggers AI Chatbox
                    Box(
                        modifier = Modifier
                            .size(settings.smartAssistantSize.dp)
                            .clip(CircleShape)
                            .background(
                                color = if (settings.smartAssistantColor == "DEFAULT") primaryColor else Color(
                                    android.graphics.Color.parseColor(settings.smartAssistantColor)
                                )
                            )
                            .clickable { showChatBotOverlay = true }
                            .testTag("ai_assistant_bubble"),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.SupportAgent,
                                contentDescription = "AI helper",
                                tint = backgroundColor,
                                modifier = Modifier.size((settings.smartAssistantSize / 2.2).dp)
                            )
                            Text(
                                text = if (language == "AR") "خدمات" else "AI",
                                color = backgroundColor,
                                fontSize = (settings.smartAssistantSize / 5).sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Maintenance Mode Check
            if (settings.isMaintenanceMode && !adminLoggedIn) {
                MaintenanceView(settings, appFontColor, primaryColor)
            } else {
                // Main Navigation Routing
                AnimatedContent(
                    targetState = currentScreen,
                    transitionSpec = {
                        slideInHorizontally { width -> width } + fadeIn() togetherWith
                                slideOutHorizontally { width -> -width } + fadeOut()
                    },
                    label = "ScreenTransition"
                ) { screen ->
                    when (screen) {
                        is Screen.Home -> HomeScreen(viewModel, primaryColor, secondaryColor, surfaceColor, appFontColor, language)
                        is Screen.CategoryDetail -> CategoryDetailScreen(viewModel, screen.mainCategory, screen.subCategory, primaryColor, secondaryColor, surfaceColor, appFontColor, language)
                        is Screen.ProviderDetail -> ProviderDetailScreen(viewModel, screen.provider, primaryColor, secondaryColor, surfaceColor, appFontColor, language)
                        is Screen.ProviderRegistration -> ProviderRegistrationScreen(viewModel, primaryColor, secondaryColor, surfaceColor, appFontColor, language)
                        is Screen.AdminLogin -> AdminLoginScreen(viewModel, primaryColor, secondaryColor, surfaceColor, appFontColor, language)
                        is Screen.AdminPanel -> AdminPanelScreen(viewModel, primaryColor, secondaryColor, surfaceColor, appFontColor, language)
                        is Screen.SecretSettings -> SecretSettingsScreen(viewModel, primaryColor, secondaryColor, surfaceColor, appFontColor, language)
                        is Screen.AboutApp -> AboutAppScreen(viewModel, primaryColor, secondaryColor, surfaceColor, appFontColor, language)
                    }
                }
            }

            // Refresh progress loader overlay
            if (isRefreshing) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = primaryColor)
                }
            }

            // 5-TAP Password dialogues
            if (showBackdoorDialog) {
                Dialog(onDismissRequest = { viewModel.showBackdoorDialog.value = false }) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = surfaceColor),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .border(1.dp, primaryColor.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "البوابة السرية الخلفية 🔒",
                                color = appFontColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            OutlinedTextField(
                                value = backdoorPasswordInput,
                                onValueChange = { backdoorPasswordInput = it },
                                label = { Text("أدخل كلمة المرور السرية للمالك", color = primaryColor.copy(alpha = 0.7f)) },
                                visualTransformation = PasswordVisualTransformation(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("backdoor_password_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = primaryColor,
                                    unfocusedBorderColor = secondaryColor,
                                    focusedLabelColor = primaryColor,
                                    focusedTextColor = Color.White
                                )
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                TextButton(
                                    onClick = {
                                        viewModel.showBackdoorDialog.value = false
                                        backdoorPasswordInput = ""
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("إلغاء", color = Color.Red)
                                }
                                Button(
                                    onClick = {
                                        if (backdoorPasswordInput == settings.backdoorPassword) {
                                            viewModel.showBackdoorDialog.value = false
                                            backdoorPasswordInput = ""
                                            viewModel.navigateTo(Screen.SecretSettings)
                                        } else {
                                            Toast.makeText(context, "الكلمة السرية غير صحيحة!", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("submit_backdoor_password"),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("دخول", color = backgroundColor)
                                }
                            }
                        }
                    }
                }
            }

            // About Application Info Dialog
            if (showSupportModal) {
                Dialog(onDismissRequest = { showSupportModal = false }) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = surfaceColor),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, primaryColor.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                    ) {
                        LazyColumn(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            item {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = "Info",
                                    tint = primaryColor,
                                    modifier = Modifier.size(54.dp)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = settings.appName,
                                    color = appFontColor,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = settings.welcomeMessage,
                                    color = appFontColor.copy(alpha = 0.8f),
                                    fontSize = 14.sp,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(18.dp))
                                Divider(color = secondaryColor.copy(alpha = 0.3f))
                                Spacer(modifier = Modifier.height(14.dp))
                                Text(
                                    text = "📞 قنوات الدعم والمساعدة المباشرة:",
                                    color = primaryColor,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Right
                                )
                                Spacer(modifier = Modifier.height(10.dp))

                                // Contact Phone Click Trigger
                                Text(
                                    text = "رقم الدعم الساخن: ${settings.supportPhone}",
                                    color = appFontColor,
                                    fontSize = 14.sp,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            try {
                                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${settings.supportPhone}"))
                                                context.startActivity(intent)
                                            } catch (e: Exception) {
                                                Toast.makeText(context, "لا يمكن تشغيل الهاتف", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                        .padding(vertical = 4.dp),
                                    textAlign = TextAlign.Right
                                )

                                // Support Email
                                Text(
                                    text = "البريد الإلكتروني للإدارة: ${settings.supportEmail}",
                                    color = appFontColor,
                                    fontSize = 14.sp,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            try {
                                                val intent = Intent(Intent.ACTION_SENDTO).apply {
                                                    data = Uri.parse("mailto:${settings.supportEmail}")
                                                }
                                                context.startActivity(intent)
                                            } catch (e: Exception) {
                                                Toast.makeText(context, "لا يتم دعم بريد إلكتروني", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                        .padding(vertical = 4.dp),
                                    textAlign = TextAlign.Right
                                )

                                // WhatsApp Trigger
                                Text(
                                    text = "واتساب الدعم السريع: ${settings.supportWhatsapp}",
                                    color = appFontColor,
                                    fontSize = 14.sp,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            try {
                                                val url = "https://api.whatsapp.com/send?phone=${settings.supportWhatsapp}"
                                                val i = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                                context.startActivity(i)
                                            } catch (e: Exception) {
                                                Toast.makeText(context, "لا يمكن فتح واتساب", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                        .padding(vertical = 4.dp),
                                    textAlign = TextAlign.Right
                                )

                                Spacer(modifier = Modifier.height(18.dp))
                                Divider(color = secondaryColor.copy(alpha = 0.3f))
                                Spacer(modifier = Modifier.height(10.dp))

                                Text(
                                    text = "حقوق البرمجة والنشر محفوظة © 2026",
                                    color = appFontColor.copy(alpha = 0.5f),
                                    fontSize = 11.sp
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                Button(
                                    onClick = { showSupportModal = false },
                                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("أغلق النافذة", color = backgroundColor)
                                }
                            }
                        }
                    }
                }
            }

            // Floating chatbot conversation assistant overlay
            if (showChatBotOverlay) {
                Dialog(onDismissRequest = { showChatBotOverlay = false }) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = surfaceColor),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(0.85f)
                            .border(1.dp, primaryColor.copy(alpha = 0.4f), RoundedCornerShape(24.dp))
                    ) {
                        val chatLogs by viewModel.chatMessages.collectAsState()
                        val chatInput by viewModel.chatBuffer.collectAsState()
                        val chatSending by viewModel.isChatLoading.collectAsState()

                        Column(modifier = Modifier.fillMaxSize()) {
                            // Header banner
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(primaryColor)
                                    .padding(vertical = 14.dp, horizontal = 16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.SupportAgent,
                                            contentDescription = "Helper Bot",
                                            tint = backgroundColor,
                                            modifier = Modifier.size(34.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = "المساعد الذكي لخدمات اليمن 🤖",
                                                color = backgroundColor,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp
                                            )
                                            Text(
                                                text = "يعمل بتقنية Gemini 3.5 AI",
                                                color = backgroundColor.copy(alpha = 0.7f),
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                    IconButton(onClick = { showChatBotOverlay = false }) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Close",
                                            tint = backgroundColor
                                        )
                                    }
                                }
                            }

                            // Conversation Area
                            LazyColumn(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                reverseLayout = false
                            ) {
                                items(chatLogs) { item ->
                                    val isMe = item.second
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        contentAlignment = if (isMe) Alignment.CenterStart else Alignment.CenterEnd
                                    ) {
                                        Card(
                                            colors = CardDefaults.cardColors(
                                                containerColor = if (isMe) primaryColor.copy(alpha = 0.2f) else surfaceColor.copy(alpha = 0.9f)
                                            ),
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier
                                                .fillMaxWidth(0.85f)
                                                .border(
                                                    width = 1.dp,
                                                    color = if (isMe) primaryColor.copy(alpha = 0.6f) else secondaryColor.copy(alpha = 0.3f),
                                                    shape = RoundedCornerShape(12.dp)
                                                )
                                        ) {
                                            Column(modifier = Modifier.padding(10.dp)) {
                                                Text(
                                                    text = item.first,
                                                    color = appFontColor,
                                                    fontSize = 13.sp,
                                                    textAlign = if (isMe) TextAlign.Left else TextAlign.Right,
                                                    modifier = Modifier.fillMaxWidth()
                                                )
                                            }
                                        }
                                    }
                                }

                                if (chatSending) {
                                    item {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 8.dp),
                                            contentAlignment = Alignment.CenterEnd
                                        ) {
                                            CircularProgressIndicator(
                                                color = primaryColor,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            // Message Composer input bar
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(backgroundColor.copy(alpha = 0.8f))
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = chatInput,
                                    onValueChange = { viewModel.chatBuffer.value = it },
                                    placeholder = { Text("اطرح استفسارك هنا يا طيب...", color = primaryColor.copy(alpha = 0.5f)) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("chat_input_text_field"),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = primaryColor,
                                        unfocusedBorderColor = secondaryColor,
                                        focusedTextColor = Color.White
                                    )
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                IconButton(
                                    onClick = { viewModel.sendMessageToAssistant() },
                                    modifier = Modifier
                                        .background(primaryColor, CircleShape)
                                        .size(46.dp)
                                        .testTag("send_chat_message_button"),
                                    enabled = !chatSending
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Send,
                                        contentDescription = "Send Message",
                                        tint = backgroundColor
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

@Composable
fun MaintenanceView(settings: SystemSettingsEntity, fontColor: Color, primaryColor: Color) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Build,
                    contentDescription = "Maintenance Mode",
                    tint = primaryColor,
                    modifier = Modifier.size(72.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "وضع الصيانة والتحديثات 🛠️",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = fontColor
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "عذراً يا طيب! التطبيق في وضع الصيانة الاستثنائية حالياً لجدولة وتطوير الخدمات لراحتكم وتجويد الأداء.",
                    fontSize = 14.sp,
                    color = fontColor.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(18.dp))
                Text(
                    text = "لمتابعة الدعم السريع، يمكنك التواصل واتساب عبر: ${settings.supportWhatsapp}",
                    fontSize = 12.sp,
                    color = primaryColor,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
