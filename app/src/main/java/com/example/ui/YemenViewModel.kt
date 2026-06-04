package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File

sealed class Screen {
    object Home : Screen()
    data class CategoryDetail(val mainCategory: CategoryEntity, val subCategory: CategoryEntity) : Screen()
    data class ProviderDetail(val provider: ProviderEntity) : Screen()
    object ProviderRegistration : Screen()
    object AdminLogin : Screen()
    object AdminPanel : Screen()
    object SecretSettings : Screen()
    object AboutApp : Screen()
}

class YemenViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = YemenRepository(application)
    val dao = repository.dao

    fun getProvidersByCategoryFlow(categoryId: Int): Flow<List<ProviderEntity>> {
        return repository.getProvidersByCategoryFlow(categoryId)
    }

    fun getReviewsFlow(providerId: Int): Flow<List<ReviewEntity>> {
        return repository.getReviewsFlow(providerId)
    }

    // Navigation Backstack (Simple & extremely robust state-driven navigation)
    private val _navigationStack = MutableStateFlow<List<Screen>>(listOf(Screen.Home))
    val navigationStack: StateFlow<List<Screen>> = _navigationStack.asStateFlow()

    val currentScreen: StateFlow<Screen> = _navigationStack.map { it.lastOrNull() ?: Screen.Home }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Screen.Home)

    // Language State: "AR" (Arabic) or "EN" (English)
    private val _language = MutableStateFlow("AR")
    val language: StateFlow<String> = _language.asStateFlow()

    // Refresh State
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    // Search advanced flows
    val searchQuery = MutableStateFlow("")
    val searchCity = MutableStateFlow("")
    val searchDistrict = MutableStateFlow("")
    val searchPhone = MutableStateFlow("")
    val searchRadius = MutableStateFlow(10f) // default radius search 10km
    val searchMinRating = MutableStateFlow(0f) // min rating stars
    val searchCategoryId = MutableStateFlow(0) // 0 means any category

    // Active Banner Index
    private val _currentBannerIndex = MutableStateFlow(0)
    val currentBannerIndex: StateFlow<Int> = _currentBannerIndex.asStateFlow()

    // Dialog state for Backdoor password
    var showBackdoorDialog = MutableStateFlow(false)

    // Loyalty Points (earned dynamically)
    val loyaltyPoints: StateFlow<Int> = repository.loyaltyPointsFlow
        .map { it?.score ?: 0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Active Theme Settings (Loaded dynamically from SQLite to adjust visual styling in real-time!)
    val settings: StateFlow<SystemSettingsEntity> = repository.settingsFlow
        .map { it ?: SystemSettingsEntity() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SystemSettingsEntity())

    // Base Lists
    val mainCategories = repository.mainCategoriesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categories = repository.categoriesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allProviders = repository.providersFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recommendedProviders = repository.recommendedProvidersFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingProviders = repository.pendingProvidersFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeBanners = repository.bannersFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val reports = repository.reportsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val cities = repository.citiesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Smart assistant chat logs
    private val _chatMessages = MutableStateFlow<List<Pair<String, Boolean>>>(listOf(
        "أهلاً وسهلاً بك يا طيب في تطبيق خدمات اليمن! كيف أستطيع خدمتك اليوم ومساعدتك في اختيار أفضل الكوادر المهنية والطبية؟" to false
    ))
    val chatMessages: StateFlow<List<Pair<String, Boolean>>> = _chatMessages.asStateFlow()
    val chatBuffer = MutableStateFlow("")
    val isChatLoading = MutableStateFlow(false)

    // Admin state
    val adminLoggedIn = MutableStateFlow(false)
    val loggedInUserRole = MutableStateFlow<String>("GUEST") // GUEST, ADMIN, OWNER, MODERATOR
    val loggedInUsername = MutableStateFlow<String>("")

    val moderators: StateFlow<List<ModeratorEntity>> = repository.dao.getModeratorsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Load remember login state if exists
        val prefs = application.getSharedPreferences("yemen_prefs", android.content.Context.MODE_PRIVATE)
        val savedRole = prefs.getString("saved_role", "GUEST") ?: "GUEST"
        val savedUser = prefs.getString("saved_username", "") ?: ""
        val savedPass = prefs.getString("saved_password", "") ?: ""
        
        if (savedRole != "GUEST" && savedUser.isNotEmpty() && savedPass.isNotEmpty()) {
            loggedInUserRole.value = savedRole
            loggedInUsername.value = if (savedRole == "OWNER") "المالك الرئيسي 👑" else if (savedRole == "ADMIN") "المدير العام 👑" else savedUser
            adminLoggedIn.value = true
        }
    }

    fun saveRememberLogin(role: String, user: String, pass: String) {
        val prefs = getApplication<Application>().getSharedPreferences("yemen_prefs", android.content.Context.MODE_PRIVATE)
        prefs.edit().apply {
            putString("saved_role", role)
            putString("saved_username", user)
            putString("saved_password", pass)
            apply()
        }
    }

    fun clearRememberLogin() {
        val prefs = getApplication<Application>().getSharedPreferences("yemen_prefs", android.content.Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
        loggedInUserRole.value = "GUEST"
        loggedInUsername.value = ""
        adminLoggedIn.value = false
    }

    fun addModerator(username: String, pass: String, permissions: String = "ALL") {
        viewModelScope.launch {
            repository.dao.insertModerator(ModeratorEntity(
                username = username,
                password = pass,
                permissions = permissions
            ))
        }
    }

    fun updateModerator(id: Int, username: String, pass: String, permissions: String = "ALL") {
        viewModelScope.launch {
            repository.dao.insertModerator(ModeratorEntity(
                id = id,
                username = username,
                password = pass,
                permissions = permissions
            ))
        }
    }

    fun deleteModerator(id: Int) {
        viewModelScope.launch {
            repository.dao.deleteModeratorById(id)
        }
    }

    fun updateCategory(id: Int, nameAr: String, nameEn: String, iconName: String, parentId: Int?, displayOrder: Int = 0) {
        viewModelScope.launch {
            repository.dao.insertCategory(CategoryEntity(
                id = id,
                parentId = parentId,
                nameAr = nameAr,
                nameEn = nameEn,
                iconName = iconName,
                displayOrder = displayOrder
            ))
        }
    }

    fun navigateTo(screen: Screen) {
        val current = _navigationStack.value.toMutableList()
        current.add(screen)
        _navigationStack.value = current
    }

    fun navigateBack() {
        val current = _navigationStack.value.toMutableList()
        if (current.size > 1) {
            current.removeAt(current.size - 1)
            _navigationStack.value = current
        }
    }

    fun navigateToHomeDirectly() {
        _navigationStack.value = listOf(Screen.Home)
    }

    fun toggleLanguage() {
        _language.value = if (_language.value == "AR") "EN" else "AR"
    }

    fun refreshData() {
        viewModelScope.launch {
            _isRefreshing.value = true
            // Simulate refresh and clear temp inputs
            kotlinx.coroutines.delay(1000)
            _isRefreshing.value = false
        }
    }

    // --- Search Logic ---
    val filteredProviders: StateFlow<List<ProviderEntity>> = combine(
        allProviders,
        searchQuery,
        searchCity,
        searchDistrict,
        searchPhone,
        searchRadius,
        searchMinRating,
        searchCategoryId
    ) { flowsArray ->
        val providers = flowsArray[0] as List<ProviderEntity>
        val query = flowsArray[1] as String
        val city = flowsArray[2] as String
        val district = flowsArray[3] as String
        val phone = flowsArray[4] as String
        val radius = flowsArray[5] as Float
        val minRating = flowsArray[6] as Float
        val catId = flowsArray[7] as Int

        providers.filter { p ->
            val matchesQuery = query.isEmpty() || p.name.contains(query, ignoreCase = true) || p.address.contains(query, ignoreCase = true)
            val matchesCity = city.isEmpty() || p.address.contains(city, ignoreCase = true)
            val matchesDistrict = district.isEmpty() || p.district.contains(district, ignoreCase = true)
            val matchesPhone = phone.isEmpty() || p.phone.contains(phone)
            val matchesRating = p.rating >= minRating
            val matchesCategory = catId == 0 || p.categoryId == catId

            // GPS Distance Calculation (Haversine Formula)
            var matchesRadius = true
            if (radius > 0f) {
                p.gps?.let { gpsStr ->
                    try {
                        val parts = gpsStr.split(",")
                        if (parts.size == 2) {
                            val pLat = parts[0].trim().toDouble()
                            val pLon = parts[1].trim().toDouble()
                            // Center of Sana'a as baseline coordinates (default user position)
                            val uLat = 15.3694
                            val uLon = 44.1910
                            
                            val rEarth = 6371.0 // Earth radius in KM
                            val latDistance = Math.toRadians(pLat - uLat)
                            val lonDistance = Math.toRadians(pLon - uLon)
                            val a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2) +
                                    Math.cos(Math.toRadians(uLat)) * Math.cos(Math.toRadians(pLat)) *
                                    Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2)
                            val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
                            val distance = rEarth * c
                            matchesRadius = distance <= radius
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            matchesQuery && matchesCity && matchesDistrict && matchesPhone && matchesRating && matchesCategory && matchesRadius
        }.sortedWith(compareByDescending<ProviderEntity> { it.isPinned }
            .thenByDescending { it.isSubscribed }
            .thenByDescending { it.rating })
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Actions ---

    fun updateSettings(newSettings: SystemSettingsEntity) {
        viewModelScope.launch {
            repository.dao.insertSettings(newSettings)
        }
    }

    fun addLoyaltyPoints(scoreToAdd: Int) {
        viewModelScope.launch {
            val current = repository.dao.getLoyaltyPoints() ?: LoyaltyPointsEntity()
            repository.dao.insertLoyaltyPoints(current.copy(score = current.score + scoreToAdd))
        }
    }

    fun submitPendingProvider(
        name: String,
        phone: String,
        categoryId: Int,
        address: String,
        district: String,
        imageUrl: String,
        idCardUrl: String?,
        gps: String?
    ) {
        viewModelScope.launch {
            val pending = PendingProviderEntity(
                name = name,
                phone = phone,
                categoryId = categoryId,
                address = address,
                district = district,
                imageUrl = imageUrl,
                idCardUrl = idCardUrl,
                gps = gps,
                status = "pending"
            )
            repository.dao.insertPendingProvider(pending)
            addLoyaltyPoints(15) // +15 points for submissions!
        }
    }

    fun approvePendingProvider(pendingId: Int) {
        viewModelScope.launch {
            val pending = repository.dao.getPendingProviderById(pendingId)
            if (pending != null) {
                val provider = ProviderEntity(
                    name = pending.name,
                    phone = pending.phone,
                    categoryId = pending.categoryId,
                    address = pending.address,
                    district = pending.district,
                    gps = pending.gps,
                    imageUrl = pending.imageUrl,
                    idCardUrl = pending.idCardUrl,
                    rating = 5.0f,
                    reviewsCount = 1,
                    isPinned = false,
                    isRecommended = false,
                    isSubscribed = false
                )
                repository.dao.insertProvider(provider)
                repository.dao.deletePendingProviderById(pendingId)
            }
        }
    }

    fun rejectPendingProvider(pendingId: Int, reason: String) {
        viewModelScope.launch {
            repository.dao.deletePendingProviderById(pendingId)
        }
    }

    fun addProviderDirectly(
        name: String,
        phone: String,
        categoryId: Int,
        address: String,
        imageUrl: String
    ) {
        viewModelScope.launch {
            val provider = ProviderEntity(
                name = name,
                phone = phone,
                categoryId = categoryId,
                address = address,
                district = "يدوي",
                imageUrl = imageUrl,
                rating = 5.0f,
                reviewsCount = 0
            )
            repository.dao.insertProvider(provider)
        }
    }

    fun deleteProvider(providerId: Int) {
        viewModelScope.launch {
            repository.dao.deleteProviderById(providerId)
        }
    }

    fun togglePinProvider(provider: ProviderEntity) {
        viewModelScope.launch {
            repository.dao.insertProvider(provider.copy(isPinned = !provider.isPinned))
        }
    }

    fun toggleRecommendProvider(provider: ProviderEntity) {
        viewModelScope.launch {
            repository.dao.insertProvider(provider.copy(isRecommended = !provider.isRecommended))
        }
    }

    fun toggleSubscribeProvider(provider: ProviderEntity) {
        viewModelScope.launch {
            repository.dao.insertProvider(provider.copy(isSubscribed = !provider.isSubscribed))
        }
    }

    fun submitReport(providerId: Int, providerName: String, reason: String, text: String) {
        viewModelScope.launch {
            repository.dao.insertReport(ReportEntity(
                providerId = providerId,
                providerName = providerName,
                reason = reason,
                text = text
            ))
        }
    }

    fun deleteReport(reportId: Int) {
        viewModelScope.launch {
            repository.dao.deleteReportById(reportId)
        }
    }

    fun addReview(providerId: Int, userName: String, rating: Int, comment: String) {
        viewModelScope.launch {
            repository.dao.insertReview(ReviewEntity(
                providerId = providerId,
                userName = userName,
                rating = rating,
                comment = comment
            ))
            
            // Adjust average rating
            val current = repository.dao.getProviderById(providerId)
            if (current != null) {
                val newCount = current.reviewsCount + 1
                val newRating = ((current.rating * current.reviewsCount) + rating) / newCount
                repository.dao.insertProvider(current.copy(
                    rating = String.format("%.1f", newRating).toFloat(),
                    reviewsCount = newCount
                ))
            }

            addLoyaltyPoints(10) // Earning points on reviews!
        }
    }

    fun addCategory(nameAr: String, nameEn: String, iconName: String, parentId: Int?) {
        viewModelScope.launch {
            repository.dao.insertCategory(CategoryEntity(
                nameAr = nameAr,
                nameEn = nameEn,
                iconName = iconName,
                parentId = parentId
            ))
        }
    }

    fun deleteCategory(id: Int) {
        viewModelScope.launch {
            repository.dao.deleteCategoryById(id)
        }
    }

    fun addCity(nameAr: String, nameEn: String) {
        viewModelScope.launch {
            repository.dao.insertCity(CityEntity(nameAr, nameEn))
        }
    }

    fun deleteCity(nameAr: String) {
        viewModelScope.launch {
            repository.dao.deleteCityByName(nameAr)
        }
    }

    fun addBanner(imageUrl: String, link: String?, size: String, duration: Int) {
        viewModelScope.launch {
            repository.dao.insertBanner(BannerEntity(
                imageUrl = imageUrl,
                linkUrl = link,
                sizeType = size,
                durationSec = duration
            ))
        }
    }

    fun deleteBanner(id: Int) {
        viewModelScope.launch {
            repository.dao.deleteBannerById(id)
        }
    }

    // --- Gemini Assistant Chat ---
    fun sendMessageToAssistant() {
        val query = chatBuffer.value.trim()
        if (query.isEmpty()) return
        chatBuffer.value = ""

        _chatMessages.value = _chatMessages.value + (query to true)
        isChatLoading.value = true

        viewModelScope.launch {
            val appName = settings.value.appName
            val aiResponse = YemenGeminiService.chatWithAssistant(query, appName)
            _chatMessages.value = _chatMessages.value + (aiResponse to false)
            isChatLoading.value = false
        }
    }

    // --- Realtime Chat Messages Flow ---
    val allChatMessages: StateFlow<List<ChatMessageEntity>> = repository.dao.getAllChatMessagesFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun getChatMessagesForReceiver(id: Int): Flow<List<ChatMessageEntity>> {
        return repository.dao.getChatMessagesByReceiverFlow(id)
    }

    fun sendChatMessage(senderName: String, receiverId: Int, messageText: String, isFromUser: Boolean) {
        viewModelScope.launch {
            repository.dao.insertChatMessage(ChatMessageEntity(
                senderName = senderName,
                receiverId = receiverId,
                messageText = messageText,
                isFromUser = isFromUser
            ))
        }
    }

    fun cleanOldChats(days: Int) {
        viewModelScope.launch {
            val cutoff = System.currentTimeMillis() - (days * 24 * 60 * 60 * 1000L)
            repository.dao.deleteOldChatMessages(cutoff)
        }
    }

    fun toggleProviderChat(providerId: Int, isEnabled: Boolean) {
        viewModelScope.launch {
            val p = repository.dao.getProviderById(providerId)
            if (p != null) {
                repository.dao.insertProvider(p.copy(isChatActive = isEnabled))
            }
        }
    }

    // --- DB Backup & Restore ---
    private val _backupStatusMessage = MutableStateFlow<String?>(null)
    val backupStatusMessage: StateFlow<String?> = _backupStatusMessage.asStateFlow()

    fun triggerLocalBackup() {
        viewModelScope.launch {
            try {
                val json = repository.exportDatabaseToJson()
                val file = File(getApplication<Application>().filesDir, "yemen_services_backup.json")
                file.writeText(json)
                _backupStatusMessage.value = "تم أخذ نسخة احتياطية بنجاح إلى ملف محلي داخل التطبيق!"
            } catch (e: Exception) {
                _backupStatusMessage.value = "فشل أخذ النسخة الاحتياطية: ${e.message}"
            }
        }
    }

    fun triggerLocalRestore() {
        viewModelScope.launch {
            try {
                val file = File(getApplication<Application>().filesDir, "yemen_services_backup.json")
                if (file.exists()) {
                    val json = file.readText()
                    val success = repository.importDatabaseFromJson(json)
                    if (success) {
                        _backupStatusMessage.value = "تم استعادة قاعدة البيانات بنجاح وبشكل متكامل!"
                    } else {
                        _backupStatusMessage.value = "فشل البناء فالتنسيق غير متوافق."
                    }
                } else {
                    _backupStatusMessage.value = "لم يتم العثور على أي ملف نسخة احتياطية سابق."
                }
            } catch (e: Exception) {
                _backupStatusMessage.value = "فشل الاستعادة: ${e.message}"
            }
        }
    }

    fun performFolderBackup(folderUri: android.net.Uri) {
        viewModelScope.launch {
            try {
                val json = repository.exportDatabaseToJson()
                val msg = repository.saveBackupToFolder(json, folderUri)
                _backupStatusMessage.value = msg
            } catch (p: Exception) {
                _backupStatusMessage.value = "عذراً، حدث خطأ أثناء النسخ الاحتياطي: ${p.message}"
            }
        }
    }

    fun clearBackupStatus() {
        _backupStatusMessage.value = null
    }
}
