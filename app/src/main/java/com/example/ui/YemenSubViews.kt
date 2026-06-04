package com.example.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.speech.RecognizerIntent
import android.widget.Toast
import android.content.ClipData
import android.content.ClipboardManager
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.WebSettings
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.viewinterop.AndroidView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.Color
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
import java.io.File
import java.util.*

// Helper function to map material symbols
fun getIconForName(name: String): androidx.compose.ui.graphics.vector.ImageVector {
    return when (name) {
        "home_repair_service" -> Icons.Default.Construction
        "medical_services" -> Icons.Default.LocalHospital
        "school" -> Icons.Default.School
        "local_shipping" -> Icons.Default.LocalShipping
        "electrical_services" -> Icons.Default.ElectricBolt
        "plumbing" -> Icons.Default.Plumbing
        "carpenter" -> Icons.Default.Carpenter
        "ac_unit" -> Icons.Default.AcUnit
        "local_hospital" -> Icons.Default.Emergency
        "healing" -> Icons.Default.Healing
        "accessibility" -> Icons.Default.Accessibility
        "functions" -> Icons.Default.Calculate
        "translate" -> Icons.Default.Translate
        "computer" -> Icons.Default.Computer
        "inventory" -> Icons.Default.Inventory
        "two_wheeler" -> Icons.Default.TwoWheeler
        "build" -> Icons.Default.Build
        else -> Icons.Default.Star
    }
}

fun uriToBase64(context: Context, uri: Uri): String? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri)
        val bytes = inputStream?.readBytes()
        inputStream?.close()
        if (bytes != null) {
            android.util.Base64.encodeToString(bytes, android.util.Base64.DEFAULT)
        } else null
    } catch (e: Exception) {
        null
    }
}

// ==========================================
// 1. HOME SCREEN
// ==========================================
@Composable
fun HomeScreen(
    viewModel: YemenViewModel,
    primaryColor: Color,
    secondaryColor: Color,
    surfaceColor: Color,
    fontColor: Color,
    language: String
) {
    val context = LocalContext.current
    val settings by viewModel.settings.collectAsState()
    val banners by viewModel.activeBanners.collectAsState()
    val mainCats by viewModel.mainCategories.collectAsState()
    val recommendedprovs by viewModel.recommendedProviders.collectAsState()
    val points by viewModel.loyaltyPoints.collectAsState()
    
    // Search UI states
    val query by viewModel.searchQuery.collectAsState()
    val city by viewModel.searchCity.collectAsState()
    val district by viewModel.searchDistrict.collectAsState()
    val phone by viewModel.searchPhone.collectAsState()
    val filteredProvs by viewModel.filteredProviders.collectAsState()

    var showAdvancedFilters by remember { mutableStateOf(false) }

    // Speech voice recognizer simulation trigger
    val speechRecognizerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = { result ->
            val data = result.data
            val results = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val text = results?.getOrNull(0)
            if (!text.isNullOrEmpty()) {
                viewModel.searchQuery.value = text
                Toast.makeText(context, "البحث الصوتي: $text", Toast.LENGTH_SHORT).show()
            }
        }
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome Banner and loyalty point tracker
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = surfaceColor),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
                    .border(1.dp, primaryColor.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        val welcomeBmp = remember(settings.welcomeImageBase64) {
                            if (settings.welcomeImageBase64.isNotEmpty()) {
                                try {
                                    val decodedBytes = android.util.Base64.decode(settings.welcomeImageBase64, android.util.Base64.DEFAULT)
                                    val bmp2 = android.graphics.BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                                    bmp2?.asImageBitmap()
                                } catch (e: Exception) {
                                    null
                                }
                            } else null
                        }
                        if (welcomeBmp != null) {
                            Image(
                                bitmap = welcomeBmp,
                                contentDescription = "Yemen Services Welcome Image",
                                contentScale = ContentScale.FillWidth,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(110.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            )
                        } else {
                            val align = when (settings.welcomeGravity) {
                                "right" -> TextAlign.Right
                                "left" -> TextAlign.Left
                                else -> TextAlign.Center
                            }
                            Text(
                                text = if (language == "AR") settings.welcomeMessage else "Welcome to Yemen Services",
                                color = fontColor,
                                fontSize = settings.welcomeFontSize.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = align,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (language == "AR") "رصيد نقاط الولاء الخاص بك: $points نقطة 🎁" else "Your Loyalty Points Balance: $points Points",
                            color = primaryColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Right,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.CardGiftcard,
                        contentDescription = "Points tracker",
                        tint = primaryColor,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        }

        // Banners Carousel
        if (banners.isNotEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .clip(RoundedCornerShape(14.dp))
                ) {
                    val currentBanner = banners[0]
                    AsyncImage(
                        model = currentBanner.imageUrl,
                        contentDescription = "Public Banner",
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable {
                                if (currentBanner.linkUrl != null) {
                                    Toast
                                        .makeText(
                                            context,
                                            "تحويل لمحتوى الرابط: ${currentBanner.linkUrl}",
                                            Toast.LENGTH_SHORT
                                        )
                                        .show()
                                }
                            },
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.3f))
                    )
                    Text(
                        text = if (language == "AR") "إعلان ممول للخدمات ⭐️" else "Featured Ad",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .background(Color.Black.copy(alpha = 0.7f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }
        }

        // Advanced Search Panel
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = surfaceColor),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, secondaryColor.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = if (language == "AR") "🔍 محرك البحث الذكي والمتقدم" else "Smart Advanced Search",
                        color = fontColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Right
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = query,
                            onValueChange = { viewModel.searchQuery.value = it },
                            placeholder = { Text(if (language == "AR") "بحث باسم المهني أو عنوان العمل..." else "Search by name...", color = fontColor.copy(alpha = 0.4f), fontSize = 12.sp) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("search_query_text_field"),
                            trailingIcon = {
                                IconButton(onClick = {
                                    try {
                                        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ar-YE")
                                        }
                                        speechRecognizerLauncher.launch(intent)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "البحث الصوتي غير مدعوم على هذا الجهاز", Toast.LENGTH_SHORT).show()
                                    }
                                }) {
                                    Icon(imageVector = Icons.Default.Mic, contentDescription = "Voice Search", tint = primaryColor)
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = primaryColor,
                                unfocusedBorderColor = secondaryColor,
                                focusedTextColor = Color.White
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { showAdvancedFilters = !showAdvancedFilters }) {
                            Text(
                                text = if (showAdvancedFilters) "إغلاق الفلاتر المتقدمة 🔼" else "خيارات الفلترة والتوزيع الجغرافي 🔽",
                                color = primaryColor,
                                fontSize = 12.sp
                            )
                        }
                    }

                    if (showAdvancedFilters) {
                        Spacer(modifier = Modifier.height(8.dp))
                        // Filters fields
                        OutlinedTextField(
                            value = city,
                            onValueChange = { viewModel.searchCity.value = it },
                            label = { Text("المدينة (مثال: صنعاء، عدن)", color = primaryColor.copy(alpha = 0.7f), fontSize = 11.sp) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = primaryColor,
                                unfocusedBorderColor = secondaryColor,
                                focusedTextColor = Color.White
                            )
                        )

                        OutlinedTextField(
                            value = district,
                            onValueChange = { viewModel.searchDistrict.value = it },
                            label = { Text("الحي أو السكن (مثال: السبعين)", color = primaryColor.copy(alpha = 0.7f), fontSize = 11.sp) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = primaryColor,
                                unfocusedBorderColor = secondaryColor,
                                focusedTextColor = Color.White
                            )
                        )

                        OutlinedTextField(
                            value = phone,
                            onValueChange = { viewModel.searchPhone.value = it },
                            label = { Text("رقم الهاتف للتواصل المباشر", color = primaryColor.copy(alpha = 0.7f), fontSize = 11.sp) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = primaryColor,
                                unfocusedBorderColor = secondaryColor,
                                focusedTextColor = Color.White
                            )
                        )

                        // Data Saver Mode Info Check with switch
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "تفعيل وضع توفير باقة البيانات 📶",
                                color = fontColor,
                                fontSize = 12.sp
                            )
                            Switch(
                                checked = settings.isDataSaverMode,
                                onCheckedChange = {
                                    viewModel.updateSettings(settings.copy(isDataSaverMode = it))
                                    Toast.makeText(context, if (it) "تفعيل توفير البيانات" else "إيقاف توفير البيانات", Toast.LENGTH_SHORT).show()
                                },
                                colors = SwitchDefaults.colors(checkedThumbColor = primaryColor)
                            )
                        }
                    }
                }
            }
        }

        // Search Results List (if search query or filters are active)
        if (query.isNotEmpty() || city.isNotEmpty() || district.isNotEmpty() || phone.isNotEmpty()) {
            item {
                Text(
                    text = "🔍 نتائج البحث المكتشفة (${filteredProvs.size} مقدم خدمة):",
                    color = primaryColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Right
                )
            }

            if (filteredProvs.isEmpty()) {
                item {
                    Text(
                        text = "عذراً يا طيب، لم نجد أي متطابق لعمليات البحث هذه. حاول مجدداً مع تصفية أوسع.",
                        color = fontColor.copy(alpha = 0.6f),
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                    )
                }
            } else {
                items(filteredProvs) { provider ->
                    ProviderCard(
                        provider = provider,
                        primaryColor = primaryColor,
                        secondaryColor = secondaryColor,
                        surfaceColor = surfaceColor,
                        fontColor = fontColor,
                        language = language,
                        onClick = { viewModel.navigateTo(Screen.ProviderDetail(provider)) }
                    )
                }
            }
        }

        // Recommended / Recommended section
        if (recommendedprovs.isNotEmpty() && query.isEmpty() && city.isEmpty() && district.isEmpty() && phone.isEmpty()) {
            item {
                Text(
                    text = if (language == "AR") "⭐ المهنيون الموصى بهم من قبل الإدارة" else "Recommended Professionals",
                    color = fontColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Right
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(recommendedprovs) { provider ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = surfaceColor),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .width(170.dp)
                                .clickable { viewModel.navigateTo(Screen.ProviderDetail(provider)) }
                                .border(1.dp, primaryColor.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(100.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                ) {
                                    if (settings.isDataSaverMode) {
                                        // Load extremely low quality placeholders
                                        Box(modifier = Modifier.fillMaxSize().background(secondaryColor.copy(alpha = 0.3f)), contentAlignment = Alignment.Center) {
                                            Icon(imageVector = Icons.Default.Person, contentDescription = "Low Res", tint = primaryColor)
                                        }
                                    } else {
                                        AsyncImage(
                                            model = provider.imageUrl,
                                            contentDescription = provider.name,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .padding(4.dp)
                                            .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(4.dp))
                                            .align(Alignment.TopEnd)
                                    ) {
                                        Text(
                                            text = "⭐ ${provider.rating}",
                                            color = primaryColor,
                                            fontSize = 10.sp,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = provider.name,
                                    color = fontColor,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    textAlign = TextAlign.Right,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Text(
                                    text = provider.address,
                                    color = fontColor.copy(alpha = 0.6f),
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    textAlign = TextAlign.Right,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }
        }

        // Full grid of Main Categories list (الأقسام الرئيسية)
        if (query.isEmpty() && city.isEmpty() && district.isEmpty() && phone.isEmpty()) {
            item {
                Text(
                    text = if (language == "AR") "📂 تصفح أقسام الخدمات الرئيسية المتوفرة:" else "Browse Main Services:",
                    color = fontColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Right
                )
            }

            item {
                val subCategories by viewModel.categories.collectAsState()
                
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    mainCats.forEach { mainCat ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = surfaceColor),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, secondaryColor.copy(alpha = 0.2f), RoundedCornerShape(14.dp))
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = getIconForName(mainCat.iconName),
                                        contentDescription = "Icon",
                                        tint = primaryColor,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (language == "AR") mainCat.nameAr else mainCat.nameEn,
                                        color = fontColor,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                
                                // Display horizontal flow of sub categories click loops
                                val filteredSubs = subCategories.filter { it.parentId == mainCat.id }
                                if (filteredSubs.isEmpty()) {
                                    Text("قريباً.. جاري إضافة كوادر العمل فرعية لهذا القسم", color = fontColor.copy(alpha = 0.5f), fontSize = 11.sp)
                                } else {
                                    Row(
                                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        filteredSubs.forEach { sub ->
                                            Box(
                                                modifier = Modifier
                                                    .background(primaryColor.copy(alpha = 0.12f), RoundedCornerShape(20.dp))
                                                    .border(1.dp, primaryColor.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                                                    .clickable { viewModel.navigateTo(Screen.CategoryDetail(mainCat, sub)) }
                                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                                            ) {
                                                Text(
                                                    text = if (language == "AR") sub.nameAr else sub.nameEn,
                                                    color = fontColor,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.SemiBold
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
        }

        // Padding at bottom
        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

// Custom Provider Cards Layout render
@Composable
fun ProviderCard(
    provider: ProviderEntity,
    primaryColor: Color,
    secondaryColor: Color,
    surfaceColor: Color,
    fontColor: Color,
    language: String,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = surfaceColor),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .border(
                width = if (provider.isSupportedPremium()) 2.dp else 1.dp,
                color = if (provider.isSupportedPremium()) primaryColor else secondaryColor.copy(alpha = 0.2f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profile Photo
            Box(
                modifier = Modifier
                    .size(65.dp)
                    .clip(RoundedCornerShape(10.dp))
            ) {
                AsyncImage(
                    model = provider.imageUrl,
                    contentDescription = "Photo",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = provider.name,
                        color = fontColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    if (provider.isSubscribed) {
                        Box(
                            modifier = Modifier
                                .background(primaryColor, RoundedCornerShape(4.dp))
                        ) {
                            Text(
                                text = "مميز 👑",
                                color = Color.Black,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "📍 ${provider.address} (${provider.district})",
                    color = fontColor.copy(alpha = 0.7f),
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Star, contentDescription = "Rating", tint = primaryColor, modifier = Modifier.size(14.dp))
                    Text(
                        text = " ${provider.rating} (${provider.reviewsCount} تقييم)",
                        color = fontColor.copy(alpha = 0.6f),
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

// Subscription helper indicator checks
fun ProviderEntity.isSupportedPremium(): Boolean {
    return isPinned || isRecommended || isSubscribed
}

// ==========================================
// 2. CATEGORY DETAIL SCREEN
// ==========================================
@Composable
fun CategoryDetailScreen(
    viewModel: YemenViewModel,
    mainCategory: CategoryEntity,
    subCategory: CategoryEntity,
    primaryColor: Color,
    secondaryColor: Color,
    surfaceColor: Color,
    fontColor: Color,
    language: String
) {
    val providersFlow = viewModel.getProvidersByCategoryFlow(subCategory.id).collectAsState(initial = emptyList<ProviderEntity>())
    val providers = providersFlow.value

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = { viewModel.navigateBack() }) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = primaryColor)
                }
                Text(
                    text = "${mainCategory.nameAr} / ${subCategory.nameAr}",
                    color = fontColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    textAlign = TextAlign.Right
                )
            }
            Divider(color = secondaryColor.copy(alpha = 0.3f), modifier = Modifier.padding(vertical = 8.dp))
        }

        if (providers.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 50.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "لا توجد كوادر عمل حالية في هذا القسم الفرعي. كن أول من يسجل ويثبت وجوده!",
                        color = fontColor.copy(alpha = 0.6f),
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            items(providers) { provider ->
                ProviderCard(
                    provider = provider,
                    primaryColor = primaryColor,
                    secondaryColor = secondaryColor,
                    surfaceColor = surfaceColor,
                    fontColor = fontColor,
                    language = language,
                    onClick = { viewModel.navigateTo(Screen.ProviderDetail(provider)) }
                )
            }
        }
    }
}

// ==========================================
// 3. PROVIDER DETAIL SCREEN
// ==========================================
@Composable
fun ProviderDetailScreen(
    viewModel: YemenViewModel,
    provider: ProviderEntity,
    primaryColor: Color,
    secondaryColor: Color,
    surfaceColor: Color,
    fontColor: Color,
    language: String
) {
    val context = LocalContext.current
    val settings by viewModel.settings.collectAsState()
    val reviewsFlow = viewModel.getReviewsFlow(provider.id).collectAsState(initial = emptyList<ReviewEntity>())
    val reviews = reviewsFlow.value

    var showReviewDialog by remember { mutableStateOf(false) }
    var reviewName by remember { mutableStateOf("") }
    var reviewRating by remember { mutableStateOf(5) }
    var reviewComment by remember { mutableStateOf("") }

    var showReportDialog by remember { mutableStateOf(false) }
    var reportReason by remember { mutableStateOf("") }
    var reportDesc by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = { viewModel.navigateBack() }) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = primaryColor)
                }
                Text(
                    text = "تفاصيل ملف مقدم الخدمة",
                    color = fontColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }

        // Profile details Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = surfaceColor),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.dp, primaryColor.copy(alpha = 0.3f), RoundedCornerShape(18.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(85.dp)
                                .clip(RoundedCornerShape(12.dp))
                        ) {
                            AsyncImage(
                                model = provider.imageUrl,
                                contentDescription = provider.name,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = provider.name,
                                color = fontColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                            if (provider.isSubscribed) {
                                Box(
                                    modifier = Modifier
                                        .background(primaryColor, RoundedCornerShape(4.dp))
                                        .padding(top = 4.dp)
                                ) {
                                    Text(
                                        text = "مميز 👑 - مشترك شهري نشط",
                                        color = Color.Black,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Star, contentDescription = "Rating", tint = primaryColor, modifier = Modifier.size(16.dp))
                                Text(
                                    text = " ${provider.rating} (${provider.reviewsCount} تقييم مكتمل)",
                                    color = fontColor.copy(alpha = 0.8f),
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Divider(color = secondaryColor.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "📍 العنوان ومكان العمل الحالي: ${provider.address}",
                        color = fontColor,
                        fontSize = 13.sp,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Right
                    )
                    Text(
                        text = "🏘️ الدائرة السكنية / الحي: ${provider.district}",
                        color = fontColor,
                        fontSize = 13.sp,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Right
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Buttons of quick phone dial, whatsapp link, or view location map
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                try {
                                    val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${provider.phone}"))
                                    context.startActivity(dialIntent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "الخدمة الهاتفية معطلة في هذا الإصدار", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("call_provider_button")
                        ) {
                            Icon(imageVector = Icons.Default.Call, contentDescription = "Call", tint = Color.Black, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("اتصال هاتف", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                try {
                                    val url = "https://api.whatsapp.com/send?phone=${provider.phone}"
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "لم يكتشف وجود تطبيق واتساب على الجهاز", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("whatsapp_provider_button")
                        ) {
                            Icon(imageVector = Icons.Default.Chat, contentDescription = "WhatsApp", tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("واتساب مباشر", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    
                    // Maps routing GPS button
                    OutlinedButton(
                        onClick = {
                            try {
                                val coordinates = provider.gps ?: "15.3694,44.1910"
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("geo:$coordinates?q=$coordinates(${provider.name})"))
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "فشل تشغيل خرائط الخريطة", Toast.LENGTH_SHORT).show()
                            }
                        },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("gps_map_button"),
                        border = BorderStroke(1.dp, primaryColor)
                    ) {
                        Icon(imageVector = Icons.Default.Map, contentDescription = "GPS Map location", tint = primaryColor)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("خرائط قوقل GPS وموقع المكتب", color = primaryColor, fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Share Provider Details and Download Link button
                    Button(
                        onClick = {
                            try {
                                val downloadUrl = settings.socialShareLink.ifEmpty { "https://yemen-services.aistudio.com" }
                                val shareText = """
                                    خدمات اليمن الكوادر والمهنيين 🇾🇪
                                    مقدم الخدمة: ${provider.name}
                                    المديرية والحي: ${provider.district}
                                    الهاتف: ${provider.phone}
                                    العنوان: ${provider.address}
                                    حمل التطبيق من هَنُا ومشاركتنا: $downloadUrl
                                """.trimIndent()

                                val sendIntent: Intent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, shareText)
                                    type = "text/plain"
                                }
                                val shareIntent = Intent.createChooser(sendIntent, "مشاركة مقدم الخدمة عبر:")
                                context.startActivity(shareIntent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "فشل بدء مشاركة البيانات عبر النظام", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("share_provider_button")
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = "Share", tint = Color.Black)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("مشاركة ملف مقدم الخدمة والتطبيق 🔗", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Ratings & User Reviews history
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { showReviewDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("تقييم المهني +10 نقاط 🍿", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Text(
                    text = "🗒️ تقييمات وتعليقات العملاء الأخرين:",
                    color = fontColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }

        if (reviews.isEmpty()) {
            item {
                Text(
                    text = "لا توجد مراجعات مكتوبة حالية لهذا المهني. قيّمه واحصل على 10 نقاط فوراً!",
                    color = fontColor.copy(alpha = 0.5f),
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        } else {
            items(reviews) { r ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = surfaceColor),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, secondaryColor.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "⭐ ".repeat(r.rating), color = primaryColor, fontSize = 11.sp)
                            Text(text = r.userName, color = fontColor, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = r.comment, color = fontColor.copy(alpha = 0.9f), fontSize = 12.sp, textAlign = TextAlign.Right, modifier = Modifier.fillMaxWidth())
                    }
                }
            }
        }

        // Report Provider and warning Button Section
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = surfaceColor.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showReportDialog = true }
                    .border(1.dp, Color.Red.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Icon(imageVector = Icons.Default.Warning, contentDescription = "Warning Report", tint = Color.Red)
                    Text(
                        text = "البلاغ عن هذا العضو / انتهاكات تقديم الخدمة ⚠️",
                        color = Color.Red,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Interactive custom rating dialog
    if (showReviewDialog) {
        Dialog(onDismissRequest = { showReviewDialog = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = surfaceColor),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("تقييم مقدم الخدمة وإرساله", color = fontColor, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = reviewName,
                        onValueChange = { reviewName = it },
                        label = { Text("اسمك الثلاثي الكريم", color = primaryColor.copy(alpha = 0.7f)) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primaryColor, unfocusedBorderColor = secondaryColor, focusedTextColor = Color.White)
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("اختر عدد النجوم للاستشهاد بالتجربة:", color = fontColor, fontSize = 12.sp)
                    Row {
                        (1..5).forEach { rate ->
                            IconButton(onClick = { reviewRating = rate }) {
                                Icon(
                                    imageVector = if (rate <= reviewRating) Icons.Default.Star else Icons.Default.StarBorder,
                                    contentDescription = "Rate",
                                    tint = primaryColor
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = reviewComment,
                        onValueChange = { reviewComment = it },
                        label = { Text("ملاحظاتك/تجربتك (عربية فصحى أو عامية)", color = primaryColor.copy(alpha = 0.7f)) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primaryColor, unfocusedBorderColor = secondaryColor, focusedTextColor = Color.White)
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TextButton(onClick = { showReviewDialog = false }) {
                            Text("رجوع", color = Color.Red)
                        }
                        Button(
                            onClick = {
                                if (reviewName.isNotEmpty() && reviewComment.isNotEmpty()) {
                                    viewModel.addReview(provider.id, reviewName, reviewRating, reviewComment)
                                    Toast.makeText(context, "تم إرسال تقييمك وحصدت 10 نقاط ولاء! شكرًا لك.", Toast.LENGTH_LONG).show()
                                    showReviewDialog = false
                                } else {
                                    Toast.makeText(context, "الرجاء املأ جميع خانات المراجعة للقبول والتثبيت", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                        ) {
                            Text("إرسال التقييم", color = Color.Black)
                        }
                    }
                }
            }
        }
    }

    // Reports Submission dialogue
    if (showReportDialog) {
        Dialog(onDismissRequest = { showReportDialog = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = surfaceColor),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("رفع بلاغ للمراجعة الإدارية ⚠️", color = Color.Red, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = reportReason,
                        onValueChange = { reportReason = it },
                        label = { Text("سبب البلاغ الرئيسي (مثال: عدم توافر مهارة، سعر مبالغ)", color = primaryColor.copy(alpha = 0.8f)) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primaryColor, unfocusedBorderColor = secondaryColor, focusedTextColor = Color.White)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = reportDesc,
                        onValueChange = { reportDesc = it },
                        label = { Text("تفاصيل المشكلة والوقائع بالتحديد", color = primaryColor.copy(alpha = 0.8f)) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primaryColor, unfocusedBorderColor = secondaryColor, focusedTextColor = Color.White)
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TextButton(onClick = { showReportDialog = false }) {
                            Text("إلغاء", color = fontColor)
                        }
                        Button(
                            onClick = {
                                if (reportReason.isNotEmpty() && reportDesc.isNotEmpty()) {
                                    viewModel.submitReport(provider.id, provider.name, reportReason, reportDesc)
                                    Toast.makeText(context, "تم تسجيل البلاغ وسنطارده من قبل مشرفي التقارير الفورية.", Toast.LENGTH_LONG).show()
                                    showReportDialog = false
                                } else {
                                    Toast.makeText(context, "يرجى الإفصاح عن التفاصيل والمشكلة", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                        ) {
                            Text("إرسال البلاغ", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 4. PROVIDER REGISTRATION SCREEN
// ==========================================
@Composable
fun ProviderRegistrationScreen(
    viewModel: YemenViewModel,
    primaryColor: Color,
    secondaryColor: Color,
    surfaceColor: Color,
    fontColor: Color,
    language: String
) {
    val context = LocalContext.current
    val subCategories by viewModel.categories.collectAsState()
    val activeSubCategories = subCategories.filter { it.parentId != null }

    var regName by remember { mutableStateOf("") }
    var regPhone by remember { mutableStateOf("") }
    var regAddress by remember { mutableStateOf("") }
    var regDistrict by remember { mutableStateOf("") }
    var regSelectedCategoryId by remember { mutableStateOf(0) }
    LaunchedEffect(activeSubCategories) {
        if (regSelectedCategoryId == 0 && activeSubCategories.isNotEmpty()) {
            regSelectedCategoryId = activeSubCategories.first().id
        }
    }
    var regGps by remember { mutableStateOf("15.3694,44.1910") } // default صنعاء coordinates
    var regImageUrl by remember { mutableStateOf("https://images.unsplash.com/photo-1534528741775-53994a69daeb?q=80&w=200") } // Sample base portrait image
    var regIdCardUrl by remember { mutableStateOf<String?>("https://images.unsplash.com/photo-1554774853-aae0a22c8aa4?q=80&w=200") } // Sample card fallback

    var dropdownExpanded by remember { mutableStateOf(false) }

    var showSelfieMediaPicker by remember { mutableStateOf(false) }
    var showIdCardMediaPicker by remember { mutableStateOf(false) }

    // Launchers for Selfie image
    val selfieGalleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val base64 = uriToBase64(context, uri)
            if (base64 != null) {
                regImageUrl = base64
                Toast.makeText(context, "تم حفظ الصورة الشخصية بنجاح 📸", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val selfieCameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            try {
                val stream = java.io.ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream)
                val bytes = stream.toByteArray()
                regImageUrl = android.util.Base64.encodeToString(bytes, android.util.Base64.DEFAULT)
                Toast.makeText(context, "تم التقاط الصورة الشخصية بنجاح 📸", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "تعذر معالجة الصورة", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Launchers for National ID card image
    val idCardGalleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val base64 = uriToBase64(context, uri)
            if (base64 != null) {
                regIdCardUrl = base64
                Toast.makeText(context, "تم حفظ صورة بطاقة الهوية بنجاح 🪪", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val idCardCameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            try {
                val stream = java.io.ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream)
                val bytes = stream.toByteArray()
                regIdCardUrl = android.util.Base64.encodeToString(bytes, android.util.Base64.DEFAULT)
                Toast.makeText(context, "تم التقاط صورة بطاقة الهوية بنجاح 🪪", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "تعذر معالجة الصورة", Toast.LENGTH_SHORT).show()
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = { viewModel.navigateToHomeDirectly() }) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = primaryColor)
                }
                Text(
                    text = "💼 استمارة تسجيل الكوادر والمهنيين",
                    color = fontColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
            Divider(color = secondaryColor.copy(alpha = 0.3f), modifier = Modifier.padding(vertical = 5.dp))
        }

        item {
            // Explanatory guidelines
            Card(
                colors = CardDefaults.cardColors(containerColor = surfaceColor.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "انضم إلى أسرة مقدمي خدمات اليمن! املأ الحقول اللازمة والبيانات الإجبارية لتنضم إلى دليلنا وتصل لزبائنك على الفور. أي تسجيل يمنحك +15 نقطة ولاء مجانية!",
                    color = fontColor.copy(alpha = 0.8f),
                    fontSize = 12.sp,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Name Full Triplet
                OutlinedTextField(
                    value = regName,
                    onValueChange = { regName = it },
                    label = { Text("الاسم الثلاثي الكامل (إجباري)", color = primaryColor.copy(alpha = 0.8f)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("reg_name_input"),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primaryColor, unfocusedBorderColor = secondaryColor, focusedTextColor = Color.White)
                )

                // Phone / WhatsApp
                OutlinedTextField(
                    value = regPhone,
                    onValueChange = { regPhone = it },
                    label = { Text("رقم الهاتف الفعال / واتساب (إجباري)", color = primaryColor.copy(alpha = 0.8f)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("reg_phone_input"),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primaryColor, unfocusedBorderColor = secondaryColor, focusedTextColor = Color.White)
                )

                // Category dropdown filter selection
                val selectedCategory = activeSubCategories.find { it.id == regSelectedCategoryId }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { dropdownExpanded = true }
                ) {
                    OutlinedTextField(
                        value = selectedCategory?.nameAr ?: "اختر القسم الفني المعتمد",
                        onValueChange = {},
                        readOnly = true,
                        enabled = false,
                        label = { Text("القسم والخدمة الرئيسية (إجباري)", color = primaryColor.copy(alpha = 0.8f)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reg_category_dropdown"),
                        trailingIcon = { Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "dropdown", tint = primaryColor) },
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = Color.White,
                            disabledBorderColor = secondaryColor,
                            disabledLabelColor = primaryColor,
                            disabledTrailingIconColor = primaryColor
                        )
                    )
                    DropdownMenu(
                        expanded = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false },
                        modifier = Modifier.fillMaxWidth(0.9f)
                    ) {
                        activeSubCategories.forEach { subCat ->
                            DropdownMenuItem(
                                text = { Text(subCat.nameAr, color = Color.White) },
                                onClick = {
                                    regSelectedCategoryId = subCat.id
                                    dropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // Current Office/Address Room
                OutlinedTextField(
                    value = regAddress,
                    onValueChange = { regAddress = it },
                    label = { Text("مكان وعنوان مكتب العمل الحالي (مثال: شارع حدة - صنعاء)", color = primaryColor.copy(alpha = 0.8f)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("reg_address_input"),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primaryColor, unfocusedBorderColor = secondaryColor, focusedTextColor = Color.White)
                )

                // District Neighborhood Name
                OutlinedTextField(
                    value = regDistrict,
                    onValueChange = { regDistrict = it },
                    label = { Text("منطقة الدائرة السكنية الحالية (مثال: مديرية السبعين)", color = primaryColor.copy(alpha = 0.8f)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("reg_district_input"),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primaryColor, unfocusedBorderColor = secondaryColor, focusedTextColor = Color.White)
                )

                // GPS input text
                OutlinedTextField(
                    value = regGps,
                    onValueChange = { regGps = it },
                    label = { Text("إحداثيات وموقع الخريطة GPS (اختياري)", color = primaryColor.copy(alpha = 0.8f)) },
                    placeholder = { Text("مثال: 15.3694,44.1910") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("reg_gps_input"),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primaryColor, unfocusedBorderColor = secondaryColor, focusedTextColor = Color.White)
                )

                // Pictures selector simulators
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ElevatedButton(
                        onClick = {
                            showSelfieMediaPicker = true
                        },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.elevatedButtonColors(containerColor = surfaceColor)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(imageVector = Icons.Default.Camera, contentDescription = "profile pic", tint = primaryColor)
                            Text(
                                text = if (regImageUrl.startsWith("http")) "رفع الصورة الشخصية (إجباري)" else "تم اختيار الصورة الشخصية ✅",
                                color = if (regImageUrl.startsWith("http")) fontColor else primaryColor,
                                fontSize = 9.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    ElevatedButton(
                        onClick = {
                            showIdCardMediaPicker = true
                        },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.elevatedButtonColors(containerColor = surfaceColor)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(imageVector = Icons.Default.CreditCard, contentDescription = "ID Card pic", tint = primaryColor)
                            Text(
                                text = if (regIdCardUrl == null || regIdCardUrl!!.startsWith("http")) "رفع بطاقة الهوية (اختياري)" else "تم اختيار بطاقة الهوية ✅",
                                color = if (regIdCardUrl == null || regIdCardUrl!!.startsWith("http")) fontColor else primaryColor,
                                fontSize = 9.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Submit Form
                Button(
                    onClick = {
                        if (regName.isNotEmpty() && regPhone.isNotEmpty() && regAddress.isNotEmpty() && regDistrict.isNotEmpty()) {
                            viewModel.submitPendingProvider(
                                name = regName,
                                phone = regPhone,
                                categoryId = regSelectedCategoryId,
                                address = regAddress,
                                district = regDistrict,
                                imageUrl = regImageUrl,
                                idCardUrl = regIdCardUrl,
                                gps = regGps
                            )
                            Toast.makeText(context, "تم رفع طلب الانضمام بنجاح! هو بانتظار الموافقة الفورية من الإدارة.", Toast.LENGTH_LONG).show()
                            viewModel.navigateToHomeDirectly()
                        } else {
                            Toast.makeText(context, "يرجى ملء كافة الخانات الإجبارية أولاً للتقديم", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("submit_registration_button")
                ) {
                    Text("تقديم طلب الانضمام للمراجعة الفورية 📨", color = Color.Black, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showSelfieMediaPicker) {
        Dialog(onDismissRequest = { showSelfieMediaPicker = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = surfaceColor),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("رفع الصورة الشخصية - سيلفي 📸", color = fontColor, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = {
                            showSelfieMediaPicker = false
                            selfieCameraLauncher.launch(null)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("التقاط صورة بالكاميرا مباشر 📷", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            showSelfieMediaPicker = false
                            selfieGalleryLauncher.launch("image/*")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = secondaryColor),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("اختيار صورة من معرض الهاتف 📁", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(onClick = { showSelfieMediaPicker = false }) {
                        Text("إلغاء ❌", color = Color.Red)
                    }
                }
            }
        }
    }

    if (showIdCardMediaPicker) {
        Dialog(onDismissRequest = { showIdCardMediaPicker = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = surfaceColor),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("رفع صورة بطاقة الهوية الوطنية 🪪", color = fontColor, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = {
                            showIdCardMediaPicker = false
                            idCardCameraLauncher.launch(null)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("التقاط صورة بالكاميرا مباشر 📷", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            showIdCardMediaPicker = false
                            idCardGalleryLauncher.launch("image/*")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = secondaryColor),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("اختيار صورة من معرض الهاتف 📁", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(onClick = { showIdCardMediaPicker = false }) {
                        Text("إلغاء ❌", color = Color.Red)
                    }
                }
            }
        }
    }
}

// ==========================================
// 5. ADMIN LOGIN SCREEN
// ==========================================
@Composable
fun AdminLoginScreen(
    viewModel: YemenViewModel,
    primaryColor: Color,
    secondaryColor: Color,
    surfaceColor: Color,
    fontColor: Color,
    language: String
) {
    val context = LocalContext.current
    val settings by viewModel.settings.collectAsState()

    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = surfaceColor),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, primaryColor.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "تسجيل دخول مشرف الإدارة 🔐",
                    color = fontColor,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(18.dp))

                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("اسم المستخدم (المدير الإفتراضي: WAM2026)", color = primaryColor.copy(alpha = 0.8f)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_username_input"),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primaryColor, unfocusedBorderColor = secondaryColor, focusedTextColor = Color.White)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("كلمة المرور للإدارة", color = primaryColor.copy(alpha = 0.8f)) },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_password_input"),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primaryColor, unfocusedBorderColor = secondaryColor, focusedTextColor = Color.White)
                )

                var rememberMe by remember { mutableStateOf(false) }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { rememberMe = !rememberMe },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    Text("تذكّر كلمة المرور وبقائي متصلاً تلقائياً 💾", color = fontColor, fontSize = 11.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Checkbox(
                        checked = rememberMe,
                        onCheckedChange = { rememberMe = it },
                        colors = CheckboxDefaults.colors(checkedColor = primaryColor, uncheckedColor = secondaryColor)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(
                        onClick = { viewModel.navigateToHomeDirectly() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("إلغاء", color = Color.Red)
                    }

                    Button(
                        onClick = {
                            if (username.isEmpty() || password.isEmpty()) {
                                Toast.makeText(context, "الرجاء كتابة اسم المستخدم ومفتاح المرور كخطوة أولية", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            
                            if (username == "owner" && password == settings.backdoorPassword) {
                                viewModel.loggedInUserRole.value = "OWNER"
                                viewModel.loggedInUsername.value = "المالك الرئيسي 👑"
                                viewModel.adminLoggedIn.value = true
                                if (rememberMe) {
                                    viewModel.saveRememberLogin("OWNER", "owner", password)
                                } else {
                                    viewModel.clearRememberLogin()
                                }
                                Toast.makeText(context, "مرحباً يا مالك التطبيق! تم فتح كامل الصلاحيات 👑", Toast.LENGTH_LONG).show()
                                viewModel.navigateTo(Screen.AdminPanel)
                            } else if (username == "WAM2026" && password == settings.adminPassword) {
                                viewModel.loggedInUserRole.value = "ADMIN"
                                viewModel.loggedInUsername.value = "المدير العام 👑"
                                viewModel.adminLoggedIn.value = true
                                if (rememberMe) {
                                    viewModel.saveRememberLogin("ADMIN", "WAM2026", password)
                                } else {
                                    viewModel.clearRememberLogin()
                                }
                                Toast.makeText(context, "أهلاً بك يا مدير! تم فتح لوحة التحكم بنجاح.", Toast.LENGTH_LONG).show()
                                viewModel.navigateTo(Screen.AdminPanel)
                            } else {
                                val mods = viewModel.moderators.value
                                val matchedMod = mods.find { it.username == username && it.password == password }
                                if (matchedMod != null) {
                                    viewModel.loggedInUserRole.value = "MODERATOR"
                                    viewModel.loggedInUsername.value = matchedMod.username
                                    viewModel.adminLoggedIn.value = true
                                    if (rememberMe) {
                                        viewModel.saveRememberLogin("MODERATOR", matchedMod.username, password)
                                    } else {
                                        viewModel.clearRememberLogin()
                                        viewModel.loggedInUserRole.value = "MODERATOR"
                                        viewModel.loggedInUsername.value = matchedMod.username
                                        viewModel.adminLoggedIn.value = true
                                    }
                                    Toast.makeText(context, "مرحباً بالمشرف المحترم: ${matchedMod.username} 🔐", Toast.LENGTH_LONG).show()
                                    viewModel.navigateTo(Screen.AdminPanel)
                                } else {
                                    Toast.makeText(context, "الاسم أو رمز المرور غير معتمد! تحقق مجدداً.", Toast.LENGTH_SHORT).show()
                                }
                             }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("admin_login_submit_button")
                    ) {
                        Text("تسجيل الدخول", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ==========================================
// 6. SECRET SETTINGS SCREEN (Backdoor Settings)
// ==========================================
@Composable
fun SecretSettingsScreen(
    viewModel: YemenViewModel,
    primaryColor: Color,
    secondaryColor: Color,
    surfaceColor: Color,
    fontColor: Color,
    language: String
) {
    val context = LocalContext.current
    val settings by viewModel.settings.collectAsState()

    var nameInput by remember { mutableStateOf(settings.appName) }
    var welcomeInput by remember { mutableStateOf(settings.welcomeMessage) }
    var footerInput by remember { mutableStateOf(settings.footerText) }
    var phoneInput by remember { mutableStateOf(settings.supportPhone) }
    var emailInput by remember { mutableStateOf(settings.supportEmail) }
    var whatsappInput by remember { mutableStateOf(settings.supportWhatsapp) }
    var passInput by remember { mutableStateOf(settings.adminPassword) }

    // About Page Customize settings
    var downloadUrlInput by remember { mutableStateOf(settings.downloadUrl) }
    var aboutWelcomeInput by remember { mutableStateOf(settings.aboutWelcome) }
    var fabIconClassInput by remember { mutableStateOf(settings.fabIconClass) }
    var fabPositionInput by remember { mutableStateOf(settings.fabPosition) }
    var fabSizeInput by remember { mutableStateOf(settings.fabSize.toString()) }
    var fabColorInput by remember { mutableStateOf(settings.fabColor) }

    var selectedTheme by remember { mutableStateOf(settings.themeName) }
    var selectedFontColor by remember { mutableStateOf(settings.fontColorName) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = { viewModel.navigateToHomeDirectly() }) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = primaryColor)
                }
                Text(
                    text = "⚙️ الإعدادات السرية والخلفية للمالك",
                    color = fontColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
            Divider(color = secondaryColor.copy(alpha = 0.3f), modifier = Modifier.padding(vertical = 5.dp))
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Change App Title
                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    label = { Text("تغيير اسم التطبيق الكلي", color = primaryColor.copy(alpha = 0.8f)) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primaryColor, unfocusedBorderColor = secondaryColor, focusedTextColor = Color.White)
                )

                // Change Welcome Message
                OutlinedTextField(
                    value = welcomeInput,
                    onValueChange = { welcomeInput = it },
                    label = { Text("رسالة ترحيب الشريط العام", color = primaryColor.copy(alpha = 0.8f)) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primaryColor, unfocusedBorderColor = secondaryColor, focusedTextColor = Color.White)
                )

                // Change Banner Footer Ad Promo
                OutlinedTextField(
                    value = footerInput,
                    onValueChange = { footerInput = it },
                    label = { Text("تذييل الإمداد والصيانة الدورية (MAW 777644670)", color = primaryColor.copy(alpha = 0.8f)) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primaryColor, unfocusedBorderColor = secondaryColor, focusedTextColor = Color.White)
                )

                // Support phone and numbers
                OutlinedTextField(
                    value = phoneInput,
                    onValueChange = { phoneInput = it },
                    label = { Text("أرقام وإيميلات الدعم المباشر", color = primaryColor.copy(alpha = 0.8f)) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primaryColor, unfocusedBorderColor = secondaryColor, focusedTextColor = Color.White)
                )

                OutlinedTextField(
                    value = emailInput,
                    onValueChange = { emailInput = it },
                    label = { Text("عنوان البريد الإلكتروني للمراسلة", color = primaryColor.copy(alpha = 0.8f)) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primaryColor, unfocusedBorderColor = secondaryColor, focusedTextColor = Color.White)
                )

                OutlinedTextField(
                    value = whatsappInput,
                    onValueChange = { whatsappInput = it },
                    label = { Text("قناة دعم الواتساب المباشرة", color = primaryColor.copy(alpha = 0.8f)) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primaryColor, unfocusedBorderColor = secondaryColor, focusedTextColor = Color.White)
                )

                // Change Admin Password
                OutlinedTextField(
                    value = passInput,
                    onValueChange = { passInput = it },
                    label = { Text("كلمة مرور المشرف والمدير (WAM2026)", color = primaryColor.copy(alpha = 0.8f)) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primaryColor, unfocusedBorderColor = secondaryColor, focusedTextColor = Color.White)
                )
            }
        }

        // About the App Panel Toggles
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = surfaceColor),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, primaryColor.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "⚙️ تخصيص صفحة (حول التطبيق) والزر العائم:",
                        color = fontColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )

                    OutlinedTextField(
                        value = downloadUrlInput,
                        onValueChange = { downloadUrlInput = it },
                        label = { Text("رابط تحميل التطبيق للمشاركة", color = primaryColor.copy(alpha = 0.8f)) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primaryColor, unfocusedBorderColor = secondaryColor, focusedTextColor = Color.White)
                    )

                    OutlinedTextField(
                        value = aboutWelcomeInput,
                        onValueChange = { aboutWelcomeInput = it },
                        label = { Text("نص ترحيبي ووصف حول التطبيق", color = primaryColor.copy(alpha = 0.8f)) },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 4,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primaryColor, unfocusedBorderColor = secondaryColor, focusedTextColor = Color.White)
                    )

                    OutlinedTextField(
                        value = fabIconClassInput,
                        onValueChange = { fabIconClassInput = it },
                        label = { Text("أيقونة الزر العائم (FontAwesome Class مثلاً: fas fa-headset)", color = primaryColor.copy(alpha = 0.8f)) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primaryColor, unfocusedBorderColor = secondaryColor, focusedTextColor = Color.White)
                    )

                    OutlinedTextField(
                        value = fabSizeInput,
                        onValueChange = { fabSizeInput = it },
                        label = { Text("حجم أيقونة الزر العائم (بكسل، افتراضي: 60)", color = primaryColor.copy(alpha = 0.8f)) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primaryColor, unfocusedBorderColor = secondaryColor, focusedTextColor = Color.White)
                    )

                    OutlinedTextField(
                        value = fabColorInput,
                        onValueChange = { fabColorInput = it },
                        label = { Text("لون الزر العائم (كود HEX مثل #FF9800)", color = primaryColor.copy(alpha = 0.8f)) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primaryColor, unfocusedBorderColor = secondaryColor, focusedTextColor = Color.White)
                    )

                    Text(
                        text = "موقع الزر العائم في الصفحة:",
                        color = fontColor.copy(alpha = 0.8f),
                        fontSize = 11.sp
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("bottom-left" to "⬅️ يسار أسفل الشاشة", "bottom-right" to "➡️ يمين أسفل الشاشة").forEach { pos ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(if (fabPositionInput == pos.first) primaryColor else surfaceColor.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                    .border(1.dp, primaryColor)
                                    .clickable { fabPositionInput = pos.first }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = pos.second,
                                    color = if (fabPositionInput == pos.first) Color.Black else fontColor,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Color selector theme parameters
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = surfaceColor),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, primaryColor.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "🎨 لوحة تخصيص ألوان وتوزيع البصريات:",
                        color = fontColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Theme selectors
                    Text("اختر المظهر والخلفية كوزميك أو ذهبي أو زمردي:", color = fontColor.copy(alpha = 0.8f), fontSize = 11.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf("COSMIC_SLATE" to "🌌 كوزميك سيلفر", "CHARCOAL_GOLD" to "✨ الذهبي الفاخر", "ROYAL_EMERALD" to "🟢 الزمردي الراقي").forEach { theme ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(if (selectedTheme == theme.first) primaryColor else surfaceColor.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                    .border(1.dp, primaryColor)
                                    .clickable { selectedTheme = theme.first }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = theme.second,
                                    color = if (selectedTheme == theme.first) Color.Black else fontColor,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Font Color Selectors
                    Text("اختر لون الخطوط والكتابة في الحقول:", color = fontColor.copy(alpha = 0.8f), fontSize = 11.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf("BRIGHT_WHITE" to "◽ أبيض ناصع", "LIGHT_GOLD" to "🟡 ذهبي فاتح", "VIBRANT_SILVER" to "◽ فضي متوهج").forEach { fontCol ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(if (selectedFontColor == fontCol.first) primaryColor else surfaceColor.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                    .border(1.dp, primaryColor)
                                    .clickable { selectedFontColor = fontCol.first }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = fontCol.second,
                                    color = if (selectedFontColor == fontCol.first) Color.Black else fontColor,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }

        // Save modification changes
        item {
            Button(
                onClick = {
                    val updated = settings.copy(
                        appName = nameInput,
                        welcomeMessage = welcomeInput,
                        footerText = footerInput,
                        supportPhone = phoneInput,
                        supportEmail = emailInput,
                        supportWhatsapp = whatsappInput,
                        adminPassword = passInput,
                        themeName = selectedTheme,
                        fontColorName = selectedFontColor,
                        downloadUrl = downloadUrlInput,
                        aboutWelcome = aboutWelcomeInput,
                        fabIconClass = fabIconClassInput,
                        fabPosition = fabPositionInput,
                        fabSize = fabSizeInput.toIntOrNull() ?: 60,
                        fabColor = fabColorInput
                    )
                    viewModel.updateSettings(updated)
                    Toast.makeText(context, "تم حفظ ومزامنة الألوان والمصفوفات لجميع الأجهزة النشطة بنجاح! 🎉", Toast.LENGTH_LONG).show()
                    viewModel.navigateToHomeDirectly()
                },
                colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text("حفظ التحديثات ومزامنتها فوراً 🔁", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// ==========================================
// 7. ADMIN PANEL SCREEN
// ==========================================
@Composable
fun AdminPanelScreen(
    viewModel: YemenViewModel,
    primaryColor: Color,
    secondaryColor: Color,
    surfaceColor: Color,
    fontColor: Color,
    language: String
) {
    val context = LocalContext.current
    val pendingList by viewModel.pendingProviders.collectAsState()
    val fullList by viewModel.allProviders.collectAsState()
    val mainCats by viewModel.mainCategories.collectAsState()
    val reportsList by viewModel.reports.collectAsState()
    val backupMessage by viewModel.backupStatusMessage.collectAsState()

    var activeTabIndex by remember { mutableStateOf(0) }
    
    val allCategories by viewModel.categories.collectAsState()
    val moderatorsList by viewModel.moderators.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val loggedInUserRole by viewModel.loggedInUserRole.collectAsState()
    val loggedInUsername by viewModel.loggedInUsername.collectAsState()

    // Category form and editing states
    var catNameAr by remember { mutableStateOf("") }
    var catNameEn by remember { mutableStateOf("") }
    var catIcon by remember { mutableStateOf("star") }
    var catParentId by remember { mutableStateOf<Int?>(null) }
    var editingCategory by remember { mutableStateOf<com.example.data.CategoryEntity?>(null) }

    // Moderator form states
    var modUsername by remember { mutableStateOf("") }
    var modPassword by remember { mutableStateOf("") }

    // Welcome image launcher
    val welcomeImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val base64 = uriToBase64(context, uri)
            if (base64 != null) {
                viewModel.updateSettings(settings.copy(welcomeImageBase64 = base64))
                Toast.makeText(context, "تم تطبيق صورة الترحيب بنجاح 🖼️", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Manual insert form states
    var manualName by remember { mutableStateOf("") }
    var manualPhone by remember { mutableStateOf("") }
    var manualAddress by remember { mutableStateOf("") }
    var manualCategoryId by remember { mutableStateOf(mainCats.firstOrNull()?.id ?: 0) }

    // Backup report states
    var markdownReport by remember { mutableStateOf("جاري تحميل تقرير الإحصاء الأسبوعي للتطبيق...") }

    // Load MD report
    LaunchedEffect(Unit) {
        markdownReport = viewModel.dao.getReviewsFlow(0).let {
            viewModel.allProviders.value.let {
                // compute from repo
                val reportPath = YemenRepository(context)
                reportPath.getWeeklyReportMarkdown()
            }
        }
    }

    if (backupMessage != null) {
        Toast.makeText(context, backupMessage, Toast.LENGTH_LONG).show()
        viewModel.clearBackupStatus()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Card(
            colors = CardDefaults.cardColors(containerColor = surfaceColor),
            shape = RoundedCornerShape(0.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { viewModel.navigateToHomeDirectly() }) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close Panel", tint = primaryColor)
                    }
                    Text(text = "لوحة التحكم الرئيسية للأدمن 👑", color = fontColor, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }

                // Dynamic, role-filtered and secure tabs mapper
                val adminTabs = remember(loggedInUserRole, pendingList.size) {
                    val tabsList = mutableListOf<Pair<Int, String>>()
                    tabsList.add(0 to "⏳ طلبات التسجيل (${pendingList.size})")
                    tabsList.add(1 to "✍️ إضافة يدوية")
                    if (loggedInUserRole == "OWNER") {
                        tabsList.add(2 to "⭐ إدارة وتثبيت")
                        tabsList.add(3 to "📌 البلاغات والتقارير")
                        tabsList.add(4 to "💾 نسخ وقاعدة إحصاء")
                    }
                    tabsList.add(5 to "📁 إدارة الأقسام")
                    tabsList.add(6 to "⚙️ المشرفون والخصائص")
                    tabsList.add(7 to "💬 مراقبة الدعم")
                    tabsList
                }

                ScrollableTabRow(
                    selectedTabIndex = adminTabs.indexOfFirst { it.first == activeTabIndex }.coerceAtLeast(0),
                    containerColor = surfaceColor,
                    contentColor = primaryColor
                ) {
                    adminTabs.forEach { tab ->
                        Tab(
                            selected = activeTabIndex == tab.first,
                            onClick = { activeTabIndex = tab.first }
                        ) {
                            Text(tab.second, color = fontColor, fontSize = 11.sp, modifier = Modifier.padding(8.dp))
                        }
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            when (activeTabIndex) {
                0 -> {
                    // Pending Registrations Queue
                    if (pendingList.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("لا توجد طلبات معلقة حالياً بفولدر الانتظار 🎉", color = fontColor.copy(alpha = 0.5f), fontSize = 12.sp)
                        }
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(pendingList) { pending ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = surfaceColor),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(text = "الاسم: ${pending.name}", color = fontColor, fontWeight = FontWeight.Bold)
                                        Text(text = "الهاتف: ${pending.phone}", color = fontColor)
                                        Text(text = "العنوان: ${pending.address} / ${pending.district}", color = fontColor)
                                        Spacer(modifier = Modifier.height(10.dp))
                                        
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Button(
                                                onClick = {
                                                    viewModel.approvePendingProvider(pending.id)
                                                    Toast.makeText(context, "تم قبول العضو ونقل بياناته بنجاح!", Toast.LENGTH_SHORT).show()
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color.Green),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Text("قبول الطلب", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }

                                            Button(
                                                onClick = {
                                                    viewModel.rejectPendingProvider(pending.id, "مخالف للشروط")
                                                    Toast.makeText(context, "تم رفض وحذف الطلب من الانتظار", Toast.LENGTH_SHORT).show()
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Text("رفض وبلاغ", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                1 -> {
                    // Manual Direct insertion provider form
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        item {
                            Text("إدخال مقدم خدمة يدوياً وتوثيقه فوراً ✍️", color = fontColor, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        item {
                            OutlinedTextField(
                                value = manualName,
                                onValueChange = { manualName = it },
                                label = { Text("الاسم الكامل", color = primaryColor.copy(alpha = 0.8f)) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primaryColor, unfocusedBorderColor = secondaryColor, focusedTextColor = Color.White)
                            )
                        }
                        item {
                            OutlinedTextField(
                                value = manualPhone,
                                onValueChange = { manualPhone = it },
                                label = { Text("رقم هاتف المهني", color = primaryColor.copy(alpha = 0.8f)) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primaryColor, unfocusedBorderColor = secondaryColor, focusedTextColor = Color.White)
                            )
                        }
                        item {
                            OutlinedTextField(
                                value = manualAddress,
                                onValueChange = { manualAddress = it },
                                label = { Text("مكان العمل الحالي والعنوان بالتحديد", color = primaryColor.copy(alpha = 0.8f)) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primaryColor, unfocusedBorderColor = secondaryColor, focusedTextColor = Color.White)
                            )
                        }
                        item {
                            Button(
                                onClick = {
                                    if (manualName.isNotEmpty() && manualPhone.isNotEmpty() && manualAddress.isNotEmpty()) {
                                        viewModel.addProviderDirectly(
                                            name = manualName,
                                            phone = manualPhone,
                                            categoryId = 5, // electric target default subcat
                                            address = manualAddress,
                                            imageUrl = "https://images.unsplash.com/photo-1540569014015-19a7be504e3a?q=80&w=300"
                                        )
                                        Toast.makeText(context, "تم حفظ المهني في قاعدة البيانات مباشرة ونشره حالاً!", Toast.LENGTH_LONG).show()
                                        manualName = ""
                                        manualPhone = ""
                                        manualAddress = ""
                                        activeTabIndex = 2
                                    } else {
                                        Toast.makeText(context, "يرجى تعبئة كافة العناصر", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("إضافة عضو مباشر 🚀", color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                2 -> {
                    // Pinned / Recommended Members Manager
                    if (fullList.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("لا توجد كوادر عمل مسجلة بعد بالتطبيق", color = fontColor.copy(alpha = 0.5f))
                        }
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(fullList) { p ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = surfaceColor)
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text(text = p.name, color = fontColor, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            FilterChip(
                                                selected = p.isPinned,
                                                onClick = { viewModel.togglePinProvider(p) },
                                                label = { Text("تثبيت بالقائمة", fontSize = 10.sp) },
                                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = primaryColor, labelColor = fontColor)
                                            )

                                            FilterChip(
                                                selected = p.isRecommended,
                                                onClick = { viewModel.toggleRecommendProvider(p) },
                                                label = { Text("توصية بالرئيسية", fontSize = 10.sp) },
                                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = primaryColor, labelColor = fontColor)
                                            )

                                            FilterChip(
                                                selected = p.isSubscribed,
                                                onClick = { viewModel.toggleSubscribeProvider(p) },
                                                label = { Text("مذكرة مميز 👑", fontSize = 10.sp) },
                                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = primaryColor, labelColor = fontColor)
                                            )

                                            IconButton(
                                                onClick = {
                                                    viewModel.deleteProvider(p.id)
                                                    Toast.makeText(context, "تم حذف المهني من القوائم", Toast.LENGTH_SHORT).show()
                                                }
                                            ) {
                                                Icon(imageVector = Icons.Default.Delete, contentDescription = "delete", tint = Color.Red)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                3 -> {
                    // Users Reports listings
                    if (reportsList.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("لا توجد بلاغات مسجلة من العملاء حالياً 🎉", color = fontColor.copy(alpha = 0.5f))
                        }
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(reportsList) { report ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = surfaceColor),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(text = "عضو مستهدف: ${report.providerName} (ID: ${report.providerId})", color = Color.Red, fontWeight = FontWeight.Bold)
                                        Text(text = "السبب: ${report.reason}", color = fontColor, fontWeight = FontWeight.SemiBold)
                                        Text(text = "رواية المشكلة: ${report.text}", color = fontColor.copy(alpha = 0.8f))
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Button(
                                            onClick = {
                                                viewModel.deleteReport(report.id)
                                                Toast.makeText(context, "تم أرشفة ومعالجة ملف البلاغ بنجاح!", Toast.LENGTH_SHORT).show()
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text("أرشفة ومعالجة البلاغ ☑️", color = surfaceColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                4 -> {
                    // Database Backup, JSON restoring and Weekly reports stats
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Text("إدارة استيراد ونفث النسخ الاحتياطية 💾", color = fontColor, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { viewModel.triggerLocalBackup() },
                                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(imageVector = Icons.Default.Backup, contentDescription = "backup", tint = surfaceColor, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("أخذ نسخة json", color = surfaceColor, fontSize = 10.sp)
                                }

                                Button(
                                    onClick = { viewModel.triggerLocalRestore() },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.Yellow),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(imageVector = Icons.Default.Restore, contentDescription = "restore", tint = Color.Black, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("استرجاع قاعدة البيانات", color = Color.Black, fontSize = 10.sp)
                                }
                            }
                        }

                        // Weekly computed reports log
                        item {
                            Divider(color = secondaryColor.copy(alpha = 0.3f), modifier = Modifier.padding(vertical = 10.dp))
                            Text("📄 تقرير لوحة الأداء الأسبوعي المصدر:", color = fontColor, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(surfaceColor)
                                    .padding(12.dp)
                                    .border(1.dp, primaryColor.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                            ) {
                                Text(
                                    text = markdownReport,
                                    color = fontColor,
                                    fontSize = 11.sp,
                                    textAlign = TextAlign.Right,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }

                5 -> {
                    // Category Management Tab
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        item {
                            Text(
                                text = if (editingCategory != null) "تعديل القسم المحدد ✍️" else "إضافة قسم أساسي أو فرعي جديد 📁",
                                color = fontColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }

                        item {
                            OutlinedTextField(
                                value = catNameAr,
                                onValueChange = { catNameAr = it },
                                label = { Text("الاسم باللغة العربية", color = primaryColor) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primaryColor, unfocusedBorderColor = secondaryColor, focusedTextColor = Color.White)
                            )
                        }

                        item {
                            OutlinedTextField(
                                value = catNameEn,
                                onValueChange = { catNameEn = it },
                                label = { Text("الاسم باللغة الإنجليزية", color = primaryColor) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primaryColor, unfocusedBorderColor = secondaryColor, focusedTextColor = Color.White)
                            )
                        }

                        item {
                            OutlinedTextField(
                                value = catIcon,
                                onValueChange = { catIcon = it },
                                label = { Text("رمز الأيقونة (مثل: work, star, build, home)", color = primaryColor) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primaryColor, unfocusedBorderColor = secondaryColor, focusedTextColor = Color.White)
                            )
                        }

                        item {
                            Text("التبعية والفرع:", color = fontColor, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                FilterChip(
                                    selected = catParentId == null,
                                    onClick = { catParentId = null },
                                    label = { Text("قسم رئيسي مستند", fontSize = 10.sp) },
                                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = primaryColor, labelColor = fontColor)
                                )

                                val mainCatsOnly = allCategories.filter { it.parentId == null }
                                mainCatsOnly.forEach { mainC ->
                                    FilterChip(
                                        selected = catParentId == mainC.id,
                                        onClick = { catParentId = mainC.id },
                                        label = { Text("فرع لـ: ${mainC.nameAr}", fontSize = 10.sp) },
                                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = primaryColor, labelColor = fontColor)
                                    )
                                }
                            }
                        }

                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                if (editingCategory != null) {
                                    Button(
                                        onClick = {
                                            editingCategory = null
                                            catNameAr = ""
                                            catNameEn = ""
                                            catIcon = "star"
                                            catParentId = null
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("إلغاء التعديل", color = Color.White, fontSize = 11.sp)
                                    }
                                }

                                Button(
                                    onClick = {
                                        if (catNameAr.isNotEmpty() && catNameEn.isNotEmpty()) {
                                            if (editingCategory != null) {
                                                viewModel.updateCategory(
                                                    id = editingCategory!!.id,
                                                    nameAr = catNameAr,
                                                    nameEn = catNameEn,
                                                    iconName = catIcon,
                                                    parentId = catParentId
                                                )
                                                editingCategory = null
                                                Toast.makeText(context, "تم تطبيق تعديل القسم بنجاح ☑️", Toast.LENGTH_SHORT).show()
                                            } else {
                                                viewModel.addCategory(
                                                    nameAr = catNameAr,
                                                    nameEn = catNameEn,
                                                    iconName = catIcon,
                                                    parentId = catParentId
                                                )
                                                Toast.makeText(context, "تم إنشاء وإطلاق القسم بنجاح 🎉", Toast.LENGTH_SHORT).show()
                                            }
                                            catNameAr = ""
                                            catNameEn = ""
                                            catIcon = "star"
                                            catParentId = null
                                        } else {
                                            Toast.makeText(context, "يرجى تعبئة الأسماء العربية والإنجليزية", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = if (editingCategory != null) "حفظ التغييرات" else "إنشاء القسم",
                                        color = Color.Black,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        item {
                            Divider(color = secondaryColor.copy(alpha = 0.3f), modifier = Modifier.padding(vertical = 10.dp))
                            Text("قائمة الأقسام الحالية بالفئات والمحاور:", color = fontColor, fontWeight = FontWeight.Bold)
                        }

                        items(allCategories) { cat ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = surfaceColor),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = "${cat.nameAr} (${cat.nameEn})", color = fontColor, fontWeight = FontWeight.Bold)
                                        Text(
                                            text = if (cat.parentId == null) "قسم رئيسي مستقر 👑" else "قسم فرعي يتبع للمعرف: ${cat.parentId}",
                                            color = primaryColor,
                                            fontSize = 10.sp
                                        )
                                        Text(text = "رمز الأيقونة: ${cat.iconName}", color = fontColor.copy(alpha = 0.5f), fontSize = 10.sp)
                                    }

                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        IconButton(onClick = {
                                            editingCategory = cat
                                            catNameAr = cat.nameAr
                                            catNameEn = cat.nameEn
                                            catIcon = cat.iconName
                                            catParentId = cat.parentId
                                        }) {
                                            Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit Cat", tint = primaryColor)
                                        }

                                        IconButton(onClick = {
                                            viewModel.deleteCategory(cat.id)
                                            Toast.makeText(context, "تم حذف القسم بنجاح 🗑️", Toast.LENGTH_SHORT).show()
                                        }) {
                                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete Cat", tint = Color.Red)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                6 -> {
                    // Moderators, Footer transparency & greeting controls
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        // Role identifier header
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(primaryColor.copy(alpha = 0.1f))
                                    .padding(10.dp)
                                    .border(1.dp, primaryColor, RoundedCornerShape(8.dp)),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("المستخدم المتصل حالياً: $loggedInUsername", color = fontColor, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Text("الصلاحية: $loggedInUserRole", color = primaryColor, fontSize = 10.sp)
                                }
                                Button(
                                    onClick = {
                                        viewModel.clearRememberLogin()
                                        Toast.makeText(context, "تم تسجيل خروج المشرف وإلغاء كلمة المرور المحفوظة 🔓", Toast.LENGTH_LONG).show()
                                        viewModel.navigateToHomeDirectly()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("تسجيل الخروج 🔓", color = Color.White, fontSize = 9.sp)
                                }
                            }
                        }

                        // Welcome Customization section (ONLY available if role is ADMIN or OWNER)
                        if (loggedInUserRole == "ADMIN" || loggedInUserRole == "OWNER") {
                            item {
                                Text("إعدادات رسالة الترحيب وصورتها الرئيسية: 🖼️", color = fontColor, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }

                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = { welcomeImageLauncher.launch("image/*") },
                                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("اختر صورة للترحيب 📷", color = Color.Black, fontSize = 10.sp)
                                    }

                                    Button(
                                        onClick = {
                                            viewModel.updateSettings(settings.copy(welcomeImageBase64 = ""))
                                            Toast.makeText(context, "تم حذف صورة الترحيب والعودة للنص 🗑️", Toast.LENGTH_SHORT).show()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("إلغاء الصورة 🗑️", color = Color.White, fontSize = 10.sp)
                                    }
                                }
                            }

                            item {
                                Text("حجم خط الترحيب الحالي: ${settings.welcomeFontSize}sp", color = fontColor, fontSize = 11.sp)
                                Slider(
                                    value = settings.welcomeFontSize.toFloat(),
                                    onValueChange = {
                                        viewModel.updateSettings(settings.copy(welcomeFontSize = it.toInt()))
                                    },
                                    valueRange = 10f..32f,
                                    colors = SliderDefaults.colors(thumbColor = primaryColor, activeTrackColor = primaryColor)
                                )
                            }

                            item {
                                Text("موضع ومكان رسالة الترحيب:", color = fontColor, fontSize = 11.sp)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    listOf("center", "right", "left").forEach { g ->
                                        FilterChip(
                                            selected = settings.welcomeGravity == g,
                                            onClick = {
                                                viewModel.updateSettings(settings.copy(welcomeGravity = g))
                                            },
                                            label = { Text(if (g == "center") "وسط 🎯" else if (g == "right") "يمين ➡️" else "يسار ⬅️", fontSize = 10.sp) },
                                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = primaryColor, labelColor = fontColor)
                                        )
                                    }
                                }
                            }

                            // Footer settings
                            item {
                                Divider(color = secondaryColor.copy(alpha = 0.3f), modifier = Modifier.padding(vertical = 8.dp))
                                Text("تعديل شفافية وحجم تذييل مساعد الذكاء ومعلومات الصفحة: 🛡️", color = fontColor, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }

                            item {
                                Text("شفافية التذييل المخصصة: ${(settings.footerOpacity * 100).toInt()}%", color = fontColor, fontSize = 11.sp)
                                Slider(
                                    value = settings.footerOpacity,
                                    onValueChange = {
                                        viewModel.updateSettings(settings.copy(footerOpacity = it))
                                    },
                                    valueRange = 0.0f..1.0f,
                                    colors = SliderDefaults.colors(thumbColor = primaryColor, activeTrackColor = primaryColor)
                                )
                            }

                            item {
                                Text("حجم خط التذييل المخصص: ${settings.footerFontSize}sp", color = fontColor, fontSize = 11.sp)
                                Slider(
                                    value = settings.footerFontSize.toFloat(),
                                    onValueChange = {
                                        viewModel.updateSettings(settings.copy(footerFontSize = it.toInt()))
                                    },
                                    valueRange = 8f..24f,
                                    colors = SliderDefaults.colors(thumbColor = primaryColor, activeTrackColor = primaryColor)
                                )
                            }

                            // Moderator Management Section
                            item {
                                Divider(color = secondaryColor.copy(alpha = 0.3f), modifier = Modifier.padding(vertical = 10.dp))
                                Text("إدارة حسابات المشرفين المساعدين 👥", color = fontColor, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                            }

                            item {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = surfaceColor),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text("إنشاء حساب مشرف جديد 👥", color = fontColor, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        OutlinedTextField(
                                            value = modUsername,
                                            onValueChange = { modUsername = it },
                                            label = { Text("اسم المشرف", color = primaryColor) },
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primaryColor, unfocusedBorderColor = secondaryColor, focusedTextColor = Color.White)
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        OutlinedTextField(
                                            value = modPassword,
                                            onValueChange = { modPassword = it },
                                            label = { Text("كلمة المرور الحساسة للمشرف", color = primaryColor) },
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = primaryColor, unfocusedBorderColor = secondaryColor, focusedTextColor = Color.White)
                                        )
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Button(
                                            onClick = {
                                                if (modUsername.isNotEmpty() && modPassword.isNotEmpty()) {
                                                    viewModel.addModerator(modUsername, modPassword)
                                                    modUsername = ""
                                                    modPassword = ""
                                                    Toast.makeText(context, "تم إعداد حساب المشرف المعتمد بنجاح 👥", Toast.LENGTH_SHORT).show()
                                                } else {
                                                    Toast.makeText(context, "يرجى كتابة اسم وكلمة مرور للمشرف", Toast.LENGTH_SHORT).show()
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text("تسجيل المشرف والاعتماد ➕", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                        }
                                    }
                                }
                            }

                            item {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("قائمة المشرفين المسجلين في التطبيق:", color = fontColor, fontWeight = FontWeight.Bold)
                            }

                            if (moderatorsList.isEmpty()) {
                                item {
                                    Text("لا يوجد مشرفين إضافيين حالياً", color = fontColor.copy(alpha = 0.5f), fontSize = 11.sp)
                                }
                            } else {
                                items(moderatorsList) { m ->
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = surfaceColor),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(10.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text("رمز المشرف: ${m.username}", color = fontColor, fontWeight = FontWeight.SemiBold)
                                                Text("المرور: ${m.password}", color = primaryColor, fontSize = 11.sp)
                                            }
                                            IconButton(onClick = {
                                                viewModel.deleteModerator(m.id)
                                                Toast.makeText(context, "تم حذف المشرف بنجاح 🗑️", Toast.LENGTH_SHORT).show()
                                            }) {
                                                Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete Mod", tint = Color.Red)
                                            }
                                        }
                                    }
                                }
                            }

                            // --- 🎨 Visual Themes & Primary Colors Customizer ---
                            item {
                                Divider(color = secondaryColor.copy(alpha = 0.3f), modifier = Modifier.padding(vertical = 8.dp))
                                Text("🎨 محدد الهوية البصرية وألوان التطبيق (Theme):", color = fontColor, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }

                            item {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = surfaceColor.copy(alpha = 0.5f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        Text("أدخل كود الـ Hex للألوان مباشرة لتغيير هوية التطبيق فورياً:", color = fontColor, fontSize = 11.sp)
                                        
                                        var primaryHex by remember { mutableStateOf(settings.primaryColorHex) }
                                        var secondaryHex by remember { mutableStateOf(settings.secondaryColorHex) }
                                        var bgHex by remember { mutableStateOf(settings.backgroundColorHex) }
                                        var surfaceHex by remember { mutableStateOf(settings.surfaceColorHex) }
                                        var inputFontHex by remember { mutableStateOf(settings.inputFontColorHex) }

                                        OutlinedTextField(
                                            value = primaryHex,
                                            onValueChange = { primaryHex = it; viewModel.updateSettings(settings.copy(primaryColorHex = it)) },
                                            label = { Text("اللون الأساسي للأزرار والرموز (Primary Color Hex)", color = primaryColor) },
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White)
                                        )

                                        OutlinedTextField(
                                            value = secondaryHex,
                                            onValueChange = { secondaryHex = it; viewModel.updateSettings(settings.copy(secondaryColorHex = it)) },
                                            label = { Text("اللون الثانوي والحدود الفرعية (Secondary Color Hex)", color = primaryColor) },
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White)
                                        )

                                        OutlinedTextField(
                                            value = bgHex,
                                            onValueChange = { bgHex = it; viewModel.updateSettings(settings.copy(backgroundColorHex = it)) },
                                            label = { Text("لون خلفية التطبيق (Background Color Hex)", color = primaryColor) },
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White)
                                        )

                                        OutlinedTextField(
                                            value = surfaceHex,
                                            onValueChange = { surfaceHex = it; viewModel.updateSettings(settings.copy(surfaceColorHex = it)) },
                                            label = { Text("لون البطاقات والقوائم الفرعية (Surface Color Hex)", color = primaryColor) },
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White)
                                        )

                                        Divider(color = secondaryColor.copy(alpha = 0.2f))

                                        Text("🔤 تخصيص خط وطباعة حقول الكتابة والمدخلات:", color = fontColor, fontWeight = FontWeight.Bold, fontSize = 12.sp)

                                        OutlinedTextField(
                                            value = inputFontHex,
                                            onValueChange = { inputFontHex = it; viewModel.updateSettings(settings.copy(inputFontColorHex = it)) },
                                            label = { Text("لون خط الكتابة (Input Font Color Hex)", color = primaryColor) },
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White)
                                        )

                                        Text("عائلة الخط المعتمدة لحقول الإدخال:", color = fontColor, fontSize = 11.sp)
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            listOf("DEFAULT", "MONOSPACE", "SERIF", "SANS_SERIF").forEach { family ->
                                                FilterChip(
                                                    selected = settings.inputFontFamily == family,
                                                    onClick = { viewModel.updateSettings(settings.copy(inputFontFamily = family)) },
                                                    label = { Text(family, fontSize = 9.sp) },
                                                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = primaryColor, labelColor = fontColor)
                                                )
                                            }
                                        }

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Checkbox(
                                                checked = settings.inputFontWeight == "BOLD",
                                                onCheckedChange = { viewModel.updateSettings(settings.copy(inputFontWeight = if (it) "BOLD" else "NORMAL")) }
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("جعل مدخلات حقول الكتابة عريضة (Bold) دائماً", color = fontColor, fontSize = 11.sp)
                                        }
                                    }
                                }
                            }

                            // --- 📱 Customizable App Icons & Floating Bubbles Sizes ---
                            item {
                                Divider(color = secondaryColor.copy(alpha = 0.3f), modifier = Modifier.padding(vertical = 8.dp))
                                Text("📱 التحكم بحجم وألوان العناصر العائمة والأيقونات:", color = fontColor, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }

                            item {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = surfaceColor.copy(alpha = 0.5f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        // App Info Icon size control
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Checkbox(
                                                checked = settings.isAppInfoVisible,
                                                onCheckedChange = { viewModel.updateSettings(settings.copy(isAppInfoVisible = it)) }
                                            )
                                            Text("إظهار أيقونة معلومات التطبيق والشركة", color = fontColor, fontSize = 11.sp)
                                        }

                                        Text("حجم أيقونة معلومات التطبيق: ${settings.appInfoSize} dp", color = fontColor, fontSize = 11.sp)
                                        Slider(
                                            value = settings.appInfoSize.toFloat(),
                                            onValueChange = { viewModel.updateSettings(settings.copy(appInfoSize = it.toInt())) },
                                            valueRange = 16f..72f,
                                            colors = SliderDefaults.colors(thumbColor = primaryColor)
                                        )

                                        Divider(color = secondaryColor.copy(alpha = 0.2f))

                                        // Assistant bubble size and color control
                                        Text("حجم زر المساعد الذكي العائم: ${settings.smartAssistantSize} dp", color = fontColor, fontSize = 11.sp)
                                        Slider(
                                            value = settings.smartAssistantSize.toFloat(),
                                            onValueChange = { viewModel.updateSettings(settings.copy(smartAssistantSize = it.toInt())) },
                                            valueRange = 16f..90f,
                                            colors = SliderDefaults.colors(thumbColor = primaryColor)
                                        )

                                        var assistantColorStr by remember { mutableStateOf(settings.smartAssistantColor) }
                                        OutlinedTextField(
                                            value = assistantColorStr,
                                            onValueChange = { assistantColorStr = it; viewModel.updateSettings(settings.copy(smartAssistantColor = it)) },
                                            label = { Text("كود لون دائرة المساعد الذكي (Hex/DEFAULT)", color = primaryColor) },
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White)
                                        )

                                        Divider(color = secondaryColor.copy(alpha = 0.2f))

                                        // Chat bubble size and visibility
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Checkbox(
                                                checked = settings.isChatFloatingIconVisible,
                                                onCheckedChange = { viewModel.updateSettings(settings.copy(isChatFloatingIconVisible = it)) }
                                            )
                                            Text("عرض أيقونة الدعم الفني المباشر العائمة فوق زر المعلومات", color = fontColor, fontSize = 11.sp)
                                        }

                                        Text("حجم أيقونة الدردشة والدعم العائم: ${settings.chatFloatingIconSize} dp", color = fontColor, fontSize = 11.sp)
                                        Slider(
                                            value = settings.chatFloatingIconSize.toFloat(),
                                            onValueChange = { viewModel.updateSettings(settings.copy(chatFloatingIconSize = it.toInt())) },
                                            valueRange = 16f..90f,
                                            colors = SliderDefaults.colors(thumbColor = primaryColor)
                                        )

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Checkbox(
                                                checked = settings.isChatFeatureEnabled,
                                                onCheckedChange = { viewModel.updateSettings(settings.copy(isChatFeatureEnabled = it)) }
                                            )
                                            Text("تمكين ميزة المحادثة الفورية وتداولها عبر التطبيق", color = fontColor, fontSize = 11.sp)
                                        }
                                    }
                                }
                            }

                            // --- 🗺️ Map Radius Search & Maintenance Mode Configuration ---
                            item {
                                Divider(color = secondaryColor.copy(alpha = 0.3f), modifier = Modifier.padding(vertical = 8.dp))
                                Text("🗺️ حدود البحث الجغرافي ووضعية الصيانة العامة:", color = fontColor, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }

                            item {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = surfaceColor.copy(alpha = 0.5f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        Text("الحد الأقصى الافتراضي لنطاق البحث في الخريطة: ${settings.maxRadiusLimit} كم", color = fontColor, fontSize = 11.sp)
                                        Slider(
                                            value = settings.maxRadiusLimit.toFloat(),
                                            onValueChange = { viewModel.updateSettings(settings.copy(maxRadiusLimit = it.toInt())) },
                                            valueRange = 5f..200f,
                                            colors = SliderDefaults.colors(thumbColor = primaryColor)
                                        )

                                        Divider(color = secondaryColor.copy(alpha = 0.2f))

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Checkbox(
                                                checked = settings.isMaintenanceMode,
                                                onCheckedChange = { viewModel.updateSettings(settings.copy(isMaintenanceMode = it)) }
                                            )
                                            Text("⚠️ تفعيل وضع الصيانة الاستثنائية لجميع الزائرين", color = fontColor, fontSize = 11.sp)
                                        }

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Checkbox(
                                                checked = settings.isVoiceNotesEnabled,
                                                onCheckedChange = { viewModel.updateSettings(settings.copy(isVoiceNotesEnabled = it)) }
                                            )
                                            Text("🎙️ تمكين إضافة الأوصاف والطلبات عبر التسجيل الصوتي والـ Mic", color = fontColor, fontSize = 11.sp)
                                        }

                                        var shareLinkStr by remember { mutableStateOf(settings.socialShareLink) }
                                        OutlinedTextField(
                                            value = shareLinkStr,
                                            onValueChange = { shareLinkStr = it; viewModel.updateSettings(settings.copy(socialShareLink = it)) },
                                            label = { Text("رابط تحميل ومشاركة التطبيق عبر منصات التواصل", color = primaryColor) },
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White)
                                        )
                                    }
                                }
                            }

                            // --- 📢 Dynamic Banner Ads Slider Customizer ---
                            item {
                                Divider(color = secondaryColor.copy(alpha = 0.3f), modifier = Modifier.padding(vertical = 8.dp))
                                Text("📢 إدارة لافتات الإعلانات الممولة (Banner Ads):", color = fontColor, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }

                            item {
                                val activeBannersList by viewModel.activeBanners.collectAsState()
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = surfaceColor.copy(alpha = 0.5f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        Text("أضف لافتة إعلانية جديدة:", color = fontColor, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                        
                                        var newBannerImg by remember { mutableStateOf("") }
                                        var newBannerLink by remember { mutableStateOf("") }
                                        var newBannerSize by remember { mutableStateOf("medium") }
                                        var newBannerDur by remember { mutableStateOf(5) }

                                        OutlinedTextField(
                                            value = newBannerImg,
                                            onValueChange = { newBannerImg = it },
                                            label = { Text("رابط صورة اللافتة الإعلانية (Image URL)", color = primaryColor) },
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White)
                                        )

                                        OutlinedTextField(
                                            value = newBannerLink,
                                            onValueChange = { newBannerLink = it },
                                            label = { Text("رابط توجيه ومشاركة الإعلان (مقدم خدمة أو خارجي)", color = primaryColor) },
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White)
                                        )

                                        Text("حجم ومساحة اللافتة المعتمدة:", color = fontColor, fontSize = 11.sp)
                                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            listOf("small", "medium", "large").forEach { sz ->
                                                FilterChip(
                                                    selected = newBannerSize == sz,
                                                    onClick = { newBannerSize = sz },
                                                    label = { Text(sz, fontSize = 9.sp) }
                                                )
                                            }
                                        }

                                        Button(
                                            onClick = {
                                                if (newBannerImg.trim().isNotEmpty()) {
                                                    viewModel.addBanner(newBannerImg.trim(), newBannerLink.trim(), newBannerSize, newBannerDur)
                                                    newBannerImg = ""
                                                    newBannerLink = ""
                                                    Toast.makeText(context, "تمت إضافة لافتة الإعلان الممولة بنجاح!", Toast.LENGTH_SHORT).show()
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text("إضافة ونشر اللافتة الإعلانية 📢", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                        }

                                        Divider(color = secondaryColor.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 4.dp))
                                        
                                        Text("اللافتات النشطة حالياً بالتطبيق (${activeBannersList.size}):", color = fontColor, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                        activeBannersList.forEach { b ->
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(secondaryColor.copy(alpha = 0.15f))
                                                    .padding(6.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(text = "رابط: ${b.linkUrl ?: "مفتوح"}", color = fontColor, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                                    Text(text = "حجم: ${b.sizeType} | المدة: ${b.durationSec}ث", color = primaryColor, fontSize = 9.sp)
                                                }
                                                IconButton(onClick = { viewModel.deleteBanner(b.id) }) {
                                                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // --- 🔔 Customized Notification Text templates ---
                            item {
                                Divider(color = secondaryColor.copy(alpha = 0.3f), modifier = Modifier.padding(vertical = 8.dp))
                                Text("🔔 نماذج رسائل التنبيهات وجدولة الإشعارات التلقائية:", color = fontColor, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }

                            item {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = surfaceColor.copy(alpha = 0.5f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        Text("خصص نصوص الرسائل التي ترسل تلقائياً للمستخدمين والكوادر:", color = fontColor, fontSize = 11.sp)

                                        // Welcome notification setup
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Checkbox(
                                                checked = settings.isWelcomeNotifyEnabled,
                                                onCheckedChange = { viewModel.updateSettings(settings.copy(isWelcomeNotifyEnabled = it)) }
                                            )
                                            Text("تفعيل رسالة الترحيب والانضمام التلقائية", color = fontColor, fontSize = 11.sp)
                                        }
                                        var welcomeMsgStr by remember { mutableStateOf(settings.welcomeNotifyMsg) }
                                        OutlinedTextField(
                                            value = welcomeMsgStr,
                                            onValueChange = { welcomeMsgStr = it; viewModel.updateSettings(settings.copy(welcomeNotifyMsg = it)) },
                                            label = { Text("نص رسالة الترحيب والانضمام", color = primaryColor) },
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White)
                                        )

                                        Divider(color = secondaryColor.copy(alpha = 0.2f))

                                        // Appointments notification setup
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Checkbox(
                                                checked = settings.isAppointmentNotifyEnabled,
                                                onCheckedChange = { viewModel.updateSettings(settings.copy(isAppointmentNotifyEnabled = it)) }
                                            )
                                            Text("تفعيل تذكير المواعيد وحجوزات المهنيين", color = fontColor, fontSize = 11.sp)
                                        }
                                        var appointMsgStr by remember { mutableStateOf(settings.appointmentNotifyMsg) }
                                        OutlinedTextField(
                                            value = appointMsgStr,
                                            onValueChange = { appointMsgStr = it; viewModel.updateSettings(settings.copy(appointmentNotifyMsg = it)) },
                                            label = { Text("نص إشعار تذكير الموعد", color = primaryColor) },
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White)
                                        )

                                        Divider(color = secondaryColor.copy(alpha = 0.2f))

                                        // Billing billing alert setup
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Checkbox(
                                                checked = settings.isBillingNotifyEnabled,
                                                onCheckedChange = { viewModel.updateSettings(settings.copy(isBillingNotifyEnabled = it)) }
                                            )
                                            Text("تفعيل تنبيه فواتير الاشتراكات والتحصيل", color = fontColor, fontSize = 11.sp)
                                        }
                                        var billingMsgStr by remember { mutableStateOf(settings.billingNotifyMsg) }
                                        OutlinedTextField(
                                            value = billingMsgStr,
                                            onValueChange = { billingMsgStr = it; viewModel.updateSettings(settings.copy(billingNotifyMsg = it)) },
                                            label = { Text("نص إشعار فواتير وسندات السداد", color = primaryColor) },
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White)
                                        )

                                        Divider(color = secondaryColor.copy(alpha = 0.2f))

                                        Text("ساعة إرسال وتداول الإشعارات: ${settings.notificationSendHour}:00", color = fontColor, fontSize = 11.sp)
                                        Slider(
                                            value = settings.notificationSendHour.toFloat(),
                                            onValueChange = { viewModel.updateSettings(settings.copy(notificationSendHour = it.toInt())) },
                                            valueRange = 0f..23f,
                                            colors = SliderDefaults.colors(thumbColor = primaryColor)
                                        )
                                    }
                                }
                            }
                        } else {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(20.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "عذراً! المالك الرئيسي والمدير العام فقط يمتلكون صلاحية التحكم بالمشرفين وإعدادات التذييل المتقدمة 🔐",
                                        color = Color.Red,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }
                7 -> {
                    // Chat Support and Monitoring Room
                    val allMsgs by viewModel.allChatMessages.collectAsState()
                    var selectedReceiverId by remember { mutableStateOf<Int?>(null) }
                    var chatReplyText by remember { mutableStateOf("") }
                    val activeProvs by viewModel.allProviders.collectAsState()

                    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("💬 غرفة مراقبة المحادثات والدردشة الفورية", color = fontColor, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    activeProvs.forEach { p ->
                                        viewModel.toggleProviderChat(p.id, true)
                                    }
                                    Toast.makeText(context, "تم تفعيل الدردشة لجميع مقدمي الخدمات ✅", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("تفعيل للكل ✅", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = {
                                    activeProvs.forEach { p ->
                                        viewModel.toggleProviderChat(p.id, false)
                                    }
                                    Toast.makeText(context, "تم إيقاف وتعطيل الدردشة عن جميع مقدمي الخدمات ❌", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("إيقاف للكل ❌", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Divider(color = secondaryColor.copy(alpha = 0.3f))

                        if (selectedReceiverId == null) {
                            // Groups conversations
                            val conversations = allMsgs.groupBy { it.receiverId }
                            if (conversations.isEmpty()) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Text("لا توجد أي محادثات جارية حالياً داخل التطبيق 💬", color = fontColor.copy(alpha = 0.5f), fontSize = 12.sp)
                                }
                            } else {
                                LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    conversations.forEach { (recvId, msgs) ->
                                        val lastMsg = msgs.lastOrNull()
                                        val name = if (recvId == 0) "إدارة التطبيق" else "مقدم الخدمة #${recvId}"
                                        val matchesProv = activeProvs.find { it.id == recvId }
                                        val displayName = matchesProv?.name ?: name
                                        item {
                                            Card(
                                                colors = CardDefaults.cardColors(containerColor = surfaceColor),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable { selectedReceiverId = recvId }
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(12.dp),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Text(displayName, color = fontColor, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                        Text(lastMsg?.messageText ?: "", color = fontColor.copy(alpha = 0.7f), fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                                    }
                                                    Icon(imageVector = Icons.Default.ChevronLeft, contentDescription = "Open Chat", tint = primaryColor)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            // Detailed support window
                            val conversationMsgs = allMsgs.filter { it.receiverId == selectedReceiverId }
                            val activeProv = activeProvs.find { it.id == selectedReceiverId }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(onClick = { selectedReceiverId = null }) {
                                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = primaryColor)
                                }
                                Text(activeProv?.name ?: "دردشة مقدم الخدمة #${selectedReceiverId}", color = fontColor, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Spacer(modifier = Modifier.width(48.dp))
                            }

                            Card(
                                colors = CardDefaults.cardColors(containerColor = surfaceColor.copy(alpha = 0.5f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    LazyColumn(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        items(conversationMsgs) { msg ->
                                            val alignDir = if (msg.isFromUser) Alignment.End else Alignment.Start
                                            val bgBubble = if (msg.isFromUser) primaryColor else secondaryColor.copy(alpha = 0.4f)
                                            val textCol = if (msg.isFromUser) Color.Black else fontColor
                                            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = alignDir) {
                                                Card(
                                                    colors = CardDefaults.cardColors(containerColor = bgBubble),
                                                    shape = RoundedCornerShape(12.dp)
                                                ) {
                                                    Text(msg.messageText, color = textCol, modifier = Modifier.padding(10.dp), fontSize = 12.sp)
                                                }
                                                Text(msg.senderName, color = fontColor.copy(alpha = 0.5f), fontSize = 9.sp, modifier = Modifier.padding(horizontal = 4.dp))
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        OutlinedTextField(
                                            value = chatReplyText,
                                            onValueChange = { chatReplyText = it },
                                            modifier = Modifier.weight(1f),
                                            placeholder = { Text("اكتب رد الإدارة وسيرسل فوراً...", color = fontColor.copy(alpha = 0.5f), fontSize = 11.sp) },
                                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White)
                                        )
                                        IconButton(
                                            onClick = {
                                                if (chatReplyText.trim().isNotEmpty()) {
                                                    viewModel.sendChatMessage("إدارة التطبيق المعتمدة", selectedReceiverId ?: 0, chatReplyText.trim(), false)
                                                    chatReplyText = ""
                                                }
                                            }
                                        ) {
                                            Icon(imageVector = Icons.Default.Send, contentDescription = "Send", tint = primaryColor)
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

// ==========================================
// 8. ABOUT APP SCREEN (HIGH RE-USE DYNAMIC WEBVIEW DESIGN)
// ==========================================
@Composable
fun AboutAppScreen(
    viewModel: YemenViewModel,
    primaryColor: Color,
    secondaryColor: Color,
    surfaceColor: Color,
    fontColor: Color,
    language: String
) {
    val context = LocalContext.current
    val settings by viewModel.settings.collectAsState()

    // Parse App Colors into Hex Format safely
    val primaryHex = String.format("#%06X", (0xFFFFFF and primaryColor.value.toInt()))
    val secondaryHex = String.format("#%06X", (0xFFFFFF and secondaryColor.value.toInt()))
    val backgroundHex = "#0F172A" // Beautiful rich deep eye-comfort midnight navy
    val surfaceHex = String.format("#%06X", (0xFFFFFF and surfaceColor.value.toInt()))
    val fontHex = String.format("#%06X", (0xFFFFFF and fontColor.value.toInt()))
    
    val fabColorStr = if (settings.fabColor.startsWith("#")) settings.fabColor else "#${settings.fabColor}"
    val fabPosStyle = if (settings.fabPosition == "bottom-left") "bottom: 24px; left: 24px;" else "bottom: 24px; right: 24px;"
    val fabMenuPosStyle = if (settings.fabPosition == "bottom-left") "left: 0;" else "right: 0;"

    // HTML Content generated on-the-fly dynamically combining the database parameters
    val htmlData = """
        <!DOCTYPE html>
        <html lang="ar" dir="rtl">
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0, user-scalable=yes">
            <title>حول التطبيق</title>
            <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.2/css/all.min.css">
            <link rel="preconnect" href="https://fonts.googleapis.com">
            <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
            <link href="https://fonts.googleapis.com/css2?family=Cairo:wght@300;400;600;700;800&display=swap" rel="stylesheet">
            
            <style>
                :root {
                    --primary-color: $primaryHex;
                    --secondary-color: $secondaryHex;
                    --background-color: $backgroundHex;
                    --surface-color: $surfaceHex;
                    --font-color: $fontHex;
                    --fab-color: $fabColorStr;
                    --fab-size: ${settings.fabSize}px;
                }
                
                * {
                    box-sizing: border-box;
                    margin: 0;
                    padding: 0;
                    font-family: 'Cairo', sans-serif;
                }
                
                body {
                    background-color: var(--background-color);
                    color: var(--font-color);
                    padding: 16px;
                    direction: rtl;
                    font-size: 14px;
                    line-height: 1.6;
                    min-height: 100vh;
                    position: relative;
                }

                .header-card {
                    background: linear-gradient(135deg, var(--surface-color), rgba(15, 23, 42, 0.95));
                    border-radius: 16px;
                    padding: 24px 20px;
                    text-align: center;
                    box-shadow: 0 4px 20px rgba(0,0,0,0.3);
                    border: 1px solid rgba(255,255,255,0.08);
                    margin-bottom: 20px;
                }

                .app-logo {
                    font-size: 54px;
                    color: var(--primary-color);
                    margin-bottom: 12px;
                    animation: bounce 1.8s infinite alternate cubic-bezier(0.455, 0.03, 0.515, 0.955);
                }

                @keyframes bounce {
                    from { transform: translateY(0); }
                    to { transform: translateY(-8px); }
                }

                .welcome-title {
                    font-size: 21px;
                    font-weight: 800;
                    color: var(--primary-color);
                    margin-bottom: 8px;
                }

                .welcome-text {
                    font-size: 13.5px;
                    opacity: 0.9;
                    color: var(--font-color);
                    text-align: center;
                }

                .info-section {
                    background-color: var(--surface-color);
                    border-radius: 16px;
                    padding: 20px;
                    box-shadow: 0 4px 15px rgba(0,0,0,0.25);
                    border: 1px solid rgba(255,255,255,0.05);
                    margin-bottom: 20px;
                }

                .section-title {
                    font-size: 15px;
                    font-weight: 700;
                    border-bottom: 2px solid var(--primary-color);
                    padding-bottom: 8px;
                    margin-bottom: 16px;
                    color: var(--primary-color);
                    display: flex;
                    align-items: center;
                    gap: 8px;
                }

                .contact-item {
                    display: flex;
                    flex-direction: column;
                    gap: 10px;
                    padding: 14px 0;
                    border-bottom: 1px solid rgba(255,255,255,0.05);
                }
                
                .contact-item:last-child {
                    border-bottom: none;
                }

                .contact-label {
                    display: flex;
                    align-items: center;
                    gap: 10px;
                    font-weight: 600;
                    font-size: 14px;
                }

                .contact-label i {
                    color: var(--primary-color);
                    font-size: 18px;
                    width: 20px;
                    text-align: center;
                }

                .contact-val {
                    direction: ltr;
                    text-align: right;
                    font-weight: 700;
                    font-size: 15px;
                    word-break: break-all;
                    opacity: 0.95;
                    padding: 6px 10px;
                    background: rgba(255, 255, 255, 0.03);
                    border-radius: 6px;
                }

                .btn-group {
                    display: flex;
                    gap: 8px;
                    margin-top: 5px;
                }

                .action-btn {
                    flex: 1;
                    padding: 10px 12px;
                    font-size: 12px;
                    font-weight: 700;
                    border: none;
                    border-radius: 8px;
                    cursor: pointer;
                    display: flex;
                    align-items: center;
                    justify-content: center;
                    gap: 6px;
                    transition: all 0.2s ease;
                }

                .btn-call {
                    background-color: var(--primary-color);
                    color: #0b3f2e; /* dark text contrast */
                }
                
                .btn-copy {
                    background-color: rgba(255,255,255,0.1);
                    color: var(--font-color);
                    border: 1px solid rgba(255,255,255,0.2);
                }

                .action-btn:active {
                    transform: scale(0.95);
                }

                .share-banner {
                    text-align: center;
                    background-color: rgba(255, 255, 255, 0.03);
                    border: 2px dashed var(--primary-color);
                    border-radius: 14px;
                    padding: 18px;
                    margin-bottom: 60px;
                }

                .btn-share {
                    display: inline-flex;
                    align-items: center;
                    gap: 8px;
                    background-color: var(--primary-color);
                    color: #000;
                    padding: 12px 24px;
                    border: none;
                    border-radius: 10px;
                    font-weight: 700;
                    font-size: 13px;
                    width: 100%;
                    justify-content: center;
                    transition: transform 0.2s;
                    cursor: pointer;
                }

                .btn-share:active {
                    transform: scale(0.97);
                }

                /* Floating Action Button (FAB) Configurable Settings */
                .fab-container {
                    position: fixed;
                    $fabPosStyle
                    z-index: 1000;
                }

                .fab-btn {
                    width: var(--fab-size);
                    height: var(--fab-size);
                    border-radius: 50%;
                    background-color: var(--fab-color);
                    color: #121212;
                    display: flex;
                    align-items: center;
                    justify-content: center;
                    box-shadow: 0 4px 15px rgba(0,0,0,0.5);
                    border: 2px solid var(--primary-color);
                    cursor: pointer;
                    transition: all 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);
                    font-size: calc(var(--fab-size) * 0.4);
                }

                .fab-btn:active {
                    transform: scale(0.9);
                }

                .fab-menu {
                    position: absolute;
                    bottom: calc(var(--fab-size) + 12px);
                    $fabMenuPosStyle
                    background-color: var(--surface-color);
                    border-radius: 12px;
                    box-shadow: 0 5px 25px rgba(0,0,0,0.6);
                    border: 1px solid rgba(255,255,255,0.1);
                    padding: 8px;
                    display: none;
                    width: 180px;
                    flex-direction: column;
                    gap: 6px;
                    animation: slideUp 0.25s forwards;
                }

                @keyframes slideUp {
                    from { opacity: 0; transform: translateY(15px); }
                    to { opacity: 1; transform: translateY(0); }
                }

                .fab-menu-item {
                    display: flex;
                    align-items: center;
                    gap: 10px;
                    padding: 10px 12px;
                    border-radius: 8px;
                    color: var(--font-color);
                    font-weight: 600;
                    font-size: 13.5px;
                    cursor: pointer;
                    transition: background 0.2s;
                    text-align: right;
                }

                .fab-menu-item:hover, .fab-menu-item:active {
                    background-color: rgba(255,255,255,0.08);
                    color: var(--primary-color);
                }
                
                .fab-menu-item i {
                    color: var(--primary-color);
                    font-size: 14px;
                }
                
                .toast-popup {
                    position: fixed;
                    bottom: 40px;
                    left: 50%;
                    transform: translateX(-50%) translateY(100px);
                    background-color: #323232;
                    color: #fff;
                    padding: 12px 24px;
                    border-radius: 20px;
                    font-size: 12.5px;
                    font-weight: 600;
                    z-index: 10000;
                    opacity: 0;
                    transition: all 0.3s ease;
                    box-shadow: 0 4px 10px rgba(0,0,0,0.4);
                }
                
                .toast-popup.show {
                    transform: translateX(-50%) translateY(0);
                    opacity: 1;
                }
            </style>
        </head>
        <body>

            <div class="header-card">
                <div class="app-logo">
                    <i class="fas fa-handshake-angle"></i>
                </div>
                <div class="welcome-title">${settings.appName}</div>
                <div class="welcome-text">${settings.aboutWelcome}</div>
            </div>

            <div class="info-section">
                <div class="section-title">
                    <i class="fas fa-info-circle"></i>
                    <span>بيانات التواصل المباشرة</span>
                </div>

                <!-- Support Phone Number -->
                <div class="contact-item">
                    <div class="contact-label">
                        <i class="fas fa-phone-volume"></i>
                        <span>رقم الدعم الفني المباشر</span>
                    </div>
                    <div class="contact-val">${settings.supportPhone}</div>
                    <div class="btn-group">
                        <button class="action-btn btn-call" onclick="triggerCall('${settings.supportPhone}')">
                            <i class="fas fa-mobile-screen-button"></i> اتصل اﻵن
                        </button>
                        <button class="action-btn btn-copy" onclick="triggerCopy('${settings.supportPhone}', 'تم نسخ رقم الدعم الفني بنجاح')">
                            <i class="fas fa-paste"></i> نسخ الرقم
                        </button>
                    </div>
                </div>

                <!-- Support Email -->
                <div class="contact-item">
                    <div class="contact-label">
                        <i class="fas fa-envelope-open-text"></i>
                        <span>البريد الإلكتروني للإدارة</span>
                    </div>
                    <div class="contact-val">${settings.supportEmail}</div>
                    <div class="btn-group">
                        <button class="action-btn btn-call" onclick="triggerEmail('${settings.supportEmail}')">
                            <i class="fas fa-paper-plane"></i> مراسلة المالك
                        </button>
                        <button class="action-btn btn-copy" onclick="triggerCopy('${settings.supportEmail}', 'تم نسخ البريد الإلكتروني بنجاح')">
                            <i class="fas fa-copy"></i> نسخ الإيميل
                        </button>
                    </div>
                </div>
            </div>

            <!-- Share Application Link -->
            <div class="share-banner">
                <p style="margin-bottom: 12px; font-weight: 700; font-size: 13.5px; opacity: 0.9;">هل أعجبك التطبيق؟ شاركه مع الأصدقاء والعائلة لدعم ومساندة الأسر والعاملين في جميع المحافظات!</p>
                <button class="btn-share" onclick="triggerShare('${settings.downloadUrl}')">
                    <i class="fas fa-share-nodes"></i> مشاركة رابط تحميل التطبيق
                </button>
            </div>

            <!-- Floating Action Button (FAB) config view -->
            <div class="fab-container">
                <div class="fab-menu" id="fabMenu">
                    <div class="fab-menu-item" onclick="triggerCall('${settings.supportPhone}')">
                        <i class="fas fa-phone-volume"></i>
                        <span>اتصل بالدعم</span>
                    </div>
                    <div class="fab-menu-item" onclick="triggerShare('${settings.downloadUrl}')">
                        <i class="fas fa-share-nodes"></i>
                        <span>مشاركة التطبيق</span>
                    </div>
                </div>
                <!-- Custom Dynamic Icon and Size controlled directly by the Database settings -->
                <div class="fab-btn" onclick="toggleFabMenu(event)">
                    <i class="${settings.fabIconClass}"></i>
                </div>
            </div>
            
            <div class="toast-popup" id="toastPop">تم النسخ بنجاح</div>

            <script>
                function toggleFabMenu(event) {
                    event.stopPropagation();
                    var menu = document.getElementById('fabMenu');
                    if (menu.style.display === 'flex') {
                        menu.style.display = 'none';
                    } else {
                        menu.style.display = 'flex';
                    }
                }

                document.addEventListener('click', function() {
                    document.getElementById('fabMenu').style.display = 'none';
                });

                function triggerCall(num) {
                    window.location.href = "app://call?phone=" + encodeURIComponent(num);
                }

                function triggerShare(link) {
                    window.location.href = "app://share?link=" + encodeURIComponent(link);
                }
                
                function triggerEmail(email) {
                    window.location.href = "mailto:" + email;
                }

                function triggerCopy(text, toastMsg) {
                    window.location.href = "app://copy?text=" + encodeURIComponent(text) + "&toast=" + encodeURIComponent(toastMsg);
                    showToast(toastMsg);
                }
                
                function showToast(msg) {
                    var toast = document.getElementById('toastPop');
                    toast.innerText = msg;
                    toast.classList.add('show');
                    setTimeout(function() {
                        toast.classList.remove('show');
                    }, 2500);
                }
            </script>
        </body>
        </html>
    """.trimIndent()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(android.graphics.Color.parseColor(backgroundHex)))
    ) {
        // Native Premium Responsive Header Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(surfaceColor)
                .statusBarsPadding()
                .padding(horizontal = 8.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = { viewModel.navigateToHomeDirectly() },
                modifier = Modifier.testTag("about_back_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back icon",
                    tint = primaryColor
                )
            }
            Text(
                text = if (language == "AR") "حول تطبيق خدمات اليمن" else "About Yemen Services",
                color = fontColor,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.width(48.dp))
        }

        AndroidView(
            factory = { ctx ->
                WebView(ctx).apply {
                    webViewClient = object : WebViewClient() {
                        override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                            if (url != null) {
                                if (url.startsWith("app://call")) {
                                    val uri = Uri.parse(url)
                                    val rawPhone = uri.getQueryParameter("phone") ?: settings.supportPhone
                                    try {
                                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$rawPhone"))
                                        ctx.startActivity(intent)
                                    } catch (e: Exception) {
                                        Toast.makeText(ctx, "لا يمكن تشغيل الهاتف", Toast.LENGTH_SHORT).show()
                                    }
                                    return true
                                } else if (url.startsWith("app://share")) {
                                    val uri = Uri.parse(url)
                                    val link = uri.getQueryParameter("link") ?: settings.downloadUrl
                                    try {
                                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(Intent.EXTRA_SUBJECT, settings.appName)
                                            putExtra(Intent.EXTRA_TEXT, "بكل سهولة ويُسر، حمل تطبيق ${settings.appName} الآن للتواصل مع أفضل الكوادر المهنية والطبية في اليمن: $link")
                                        }
                                        ctx.startActivity(Intent.createChooser(shareIntent, "مشاركة رابط التطبيق"))
                                    } catch (e: Exception) {
                                        Toast.makeText(ctx, "تعذر تفعيل مشاركة الرابط للمشاركة", Toast.LENGTH_SHORT).show()
                                    }
                                    return true
                                } else if (url.startsWith("app://copy")) {
                                    val uri = Uri.parse(url)
                                    val textToCopy = uri.getQueryParameter("text") ?: ""
                                    val toastMsg = uri.getQueryParameter("toast") ?: "تم نسخ النص!"
                                    
                                    val clipboard = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("YemenSupport", textToCopy)
                                    clipboard.setPrimaryClip(clip)
                                    
                                    Toast.makeText(ctx, toastMsg, Toast.LENGTH_SHORT).show()
                                    return true
                                } else if (url.startsWith("mailto:")) {
                                    try {
                                        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse(url))
                                        ctx.startActivity(intent)
                                    } catch (e: Exception) {
                                        Toast.makeText(ctx, "لا يوجد تطبيق بريد تواصل نشط", Toast.LENGTH_SHORT).show()
                                    }
                                    return true
                                }
                            }
                            return false
                        }
                    }
                    this.settings.javaScriptEnabled = true
                    this.settings.domStorageEnabled = true
                    this.settings.defaultTextEncodingName = "UTF-8"
                }
            },
            update = { webView ->
                webView.loadDataWithBaseURL(null, htmlData, "text/html", "UTF-8", null)
            },
            modifier = Modifier
                .fillMaxSize()
                .testTag("about_app_webview")
        )
    }
}
