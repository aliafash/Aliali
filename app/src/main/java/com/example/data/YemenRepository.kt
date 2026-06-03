package com.example.data

import android.content.Context
import androidx.room.withTransaction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.util.Base64
import java.io.File
import java.io.InputStream
import org.json.JSONArray
import org.json.JSONObject

class YemenRepository(private val context: Context) {
    private val database = YemenDatabase.getDatabase(context)
    val dao = database.yemenDao()

    init {
        // Prepopulate default database values in background
        CoroutineScope(Dispatchers.IO).launch {
            prepopulateIfNeeded()
        }
    }

    // --- Flows for UI (SNAPSHOT LISTENERS) ---
    val settingsFlow: Flow<SystemSettingsEntity?> = dao.getSettingsFlow()
    val categoriesFlow: Flow<List<CategoryEntity>> = dao.getCategoriesFlow()
    val mainCategoriesFlow: Flow<List<CategoryEntity>> = dao.getMainCategoriesFlow()
    val providersFlow: Flow<List<ProviderEntity>> = dao.getProvidersFlow()
    val recommendedProvidersFlow: Flow<List<ProviderEntity>> = dao.getRecommendedProvidersFlow()
    val pendingProvidersFlow: Flow<List<PendingProviderEntity>> = dao.getPendingProvidersFlow()
    val bannersFlow: Flow<List<BannerEntity>> = dao.getActiveBannersFlow()
    val reportsFlow: Flow<List<ReportEntity>> = dao.getReportsFlow()
    val loyaltyPointsFlow: Flow<LoyaltyPointsEntity?> = dao.getLoyaltyPointsFlow()
    val citiesFlow: Flow<List<CityEntity>> = dao.getCitiesFlow()

    fun getSubCategoriesFlow(parentId: Int): Flow<List<CategoryEntity>> {
        return dao.getSubCategoriesFlow(parentId)
    }

    fun getProvidersByCategoryFlow(categoryId: Int): Flow<List<ProviderEntity>> {
        return dao.getProvidersByCategoryFlow(categoryId)
    }

    fun getReviewsFlow(providerId: Int): Flow<List<ReviewEntity>> {
        return dao.getReviewsFlow(providerId)
    }

    // --- Prepopulation ---
    private suspend fun prepopulateIfNeeded() {
        val currentSettings = dao.getSettings()
        if (currentSettings == null) {
            // Seed settings
            dao.insertSettings(SystemSettingsEntity())
        }

        val categories = dao.getCategoriesFlow().firstOrNull()
        if (categories.isNullOrEmpty()) {
            seedDefaultCategories()
        }

        val cities = dao.getCitiesFlow().firstOrNull()
        if (cities.isNullOrEmpty()) {
            seedDefaultCities()
        }

        val banners = dao.getAllBannersFlow().firstOrNull()
        if (banners.isNullOrEmpty()) {
            seedDefaultBanners()
        }

        val providers = dao.getProvidersFlow().firstOrNull()
        if (providers.isNullOrEmpty()) {
            seedDefaultProviders()
        }
    }

    private suspend fun seedDefaultCategories() {
        // Main categories first
        val maintenanceId = dao.insertCategory(CategoryEntity(nameAr = "صيانة منزلية", nameEn = "Home Maintenance", iconName = "home_repair_service", displayOrder = 1))
        val healthId = dao.insertCategory(CategoryEntity(nameAr = "صحة ورعاية", nameEn = "Health & Care", iconName = "medical_services", displayOrder = 2))
        val eduId = dao.insertCategory(CategoryEntity(nameAr = "تعليم وتدريب", nameEn = "Education & Training", iconName = "school", displayOrder = 3))
        val transportId = dao.insertCategory(CategoryEntity(nameAr = "نقل وخدمات", nameEn = "Transport & Services", iconName = "local_shipping", displayOrder = 4))

        // Sub categories
        dao.insertCategory(CategoryEntity(parentId = maintenanceId.toInt(), nameAr = "كهربائي منازل", nameEn = "Home Electrician", iconName = "electrical_services", displayOrder = 1))
        dao.insertCategory(CategoryEntity(parentId = maintenanceId.toInt(), nameAr = "سباك تمديدات", nameEn = "Plumber", iconName = "plumbing", displayOrder = 2))
        dao.insertCategory(CategoryEntity(parentId = maintenanceId.toInt(), nameAr = "نجار وصيانة أثاث", nameEn = "Carpenter", iconName = "carpenter", displayOrder = 3))
        dao.insertCategory(CategoryEntity(parentId = maintenanceId.toInt(), nameAr = "فني تكييف وتبريد", nameEn = "AC Technician", iconName = "ac_unit", displayOrder = 4))

        dao.insertCategory(CategoryEntity(parentId = healthId.toInt(), nameAr = "طبيب استشاري منزلي", nameEn = "Home Doctor", iconName = "local_hospital", displayOrder = 1))
        dao.insertCategory(CategoryEntity(parentId = healthId.toInt(), nameAr = "ممرض صحي منزلي", nameEn = "Home Nurse", iconName = "healing", displayOrder = 2))
        dao.insertCategory(CategoryEntity(parentId = healthId.toInt(), nameAr = "أخصائي علاج طبيعي", nameEn = "Physiotherapy", iconName = "accessibility", displayOrder = 3))

        dao.insertCategory(CategoryEntity(parentId = eduId.toInt(), nameAr = "مدرس رياضيات وفيزياء", nameEn = "Math & Physics Tutor", iconName = "functions", displayOrder = 1))
        dao.insertCategory(CategoryEntity(parentId = eduId.toInt(), nameAr = "معلم لغات أجنبية", nameEn = "Language Teacher", iconName = "translate", displayOrder = 2))
        dao.insertCategory(CategoryEntity(parentId = eduId.toInt(), nameAr = "مدرب برمجيات وتقنية", nameEn = "Coding & IT Coach", iconName = "computer", displayOrder = 3))

        dao.insertCategory(CategoryEntity(parentId = transportId.toInt(), nameAr = "نقل وتغليف عفش", nameEn = "Furniture Moving", iconName = "inventory", displayOrder = 1))
        dao.insertCategory(CategoryEntity(parentId = transportId.toInt(), nameAr = "دباب توصيل سريع", nameEn = "Delivery Motorcycle", iconName = "two_wheeler", displayOrder = 2))
        dao.insertCategory(CategoryEntity(parentId = transportId.toInt(), nameAr = "فني صيانة سيارات", nameEn = "Car Mechanic", iconName = "build", displayOrder = 3))
    }

    private suspend fun seedDefaultCities() {
        dao.insertCity(CityEntity(nameAr = "صنعاء", nameEn = "Sana'a"))
        dao.insertCity(CityEntity(nameAr = "عدن", nameEn = "Aden"))
        dao.insertCity(CityEntity(nameAr = "تعز", nameEn = "Taiz"))
        dao.insertCity(CityEntity(nameAr = "الحديدة", nameEn = "Hodeidah"))
        dao.insertCity(CityEntity(nameAr = "حضرموت", nameEn = "Hadramout"))
        dao.insertCity(CityEntity(nameAr = "إب", nameEn = "Ibb"))
    }

    private suspend fun seedDefaultBanners() {
        dao.insertBanner(BannerEntity(imageUrl = "https://images.unsplash.com/photo-1581092160607-ee22621dd758?q=80&w=600", durationSec = 6, sizeType = "MEDIUM", linkUrl = "https://yemenservices.com/promo1"))
        dao.insertBanner(BannerEntity(imageUrl = "https://images.unsplash.com/photo-1621905251189-08b45d6a269e?q=80&w=600", durationSec = 5, sizeType = "MEDIUM", linkUrl = null))
    }

    private suspend fun seedDefaultProviders() {
        // We can add some high quality mock providers initially to showcase pinning and recommendations
        dao.insertProvider(ProviderEntity(
            name = "المهندس ماهر طاهر",
            phone = "777644670",
            categoryId = 5, // كهربائي منازل (sub of صيانة)
            address = "شارع حدة - صنعاء",
            district = "مديرية السبعين",
            imageUrl = "https://images.unsplash.com/photo-1540569014015-19a7be504e3a?q=80&w=300",
            idCardUrl = null,
            rating = 4.9f,
            reviewsCount = 12,
            isPinned = true,
            isRecommended = true,
            isSubscribed = true
        ))

        dao.insertProvider(ProviderEntity(
            name = "أبو محمد الصنعاني",
            phone = "736462000",
            categoryId = 6, // سباك تمديدات
            address = "جولة الرويشان - صنعاء",
            district = "مديرية الوحدة",
            imageUrl = "https://images.unsplash.com/photo-1560250097-0b93528c311a?q=80&w=300",
            idCardUrl = null,
            rating = 4.5f,
            reviewsCount = 8,
            isPinned = false,
            isRecommended = true,
            isSubscribed = false
        ))

        dao.insertProvider(ProviderEntity(
            name = "الأستاذ وضاح المقبلي",
            phone = "777000111",
            categoryId = 12, // مدرس رياضيات
            address = "خور مكسر - عدن",
            district = "مديرية خور مكسر",
            imageUrl = "https://images.unsplash.com/photo-1573496359142-b8d87734a5a2?q=80&w=300",
            idCardUrl = null,
            rating = 4.8f,
            reviewsCount = 15,
            isPinned = true,
            isRecommended = false,
            isSubscribed = true
        ))
    }

    // --- Database Backup & Restore to XML/JSON ---
    suspend fun exportDatabaseToJson(): String = withContext(Dispatchers.IO) {
        val json = JSONObject()
        
        // System Settings
        val settings = dao.getSettings() ?: SystemSettingsEntity()
        val settingsJson = JSONObject().apply {
            put("appName", settings.appName)
            put("themeName", settings.themeName)
            put("fontColorName", settings.fontColorName)
            put("footerText", settings.footerText)
            put("welcomeMessage", settings.welcomeMessage)
            put("supportPhone", settings.supportPhone)
            put("supportEmail", settings.supportEmail)
            put("supportWhatsapp", settings.supportWhatsapp)
            put("adminPassword", settings.adminPassword)
            put("backdoorPassword", settings.backdoorPassword)
            put("isMaintenanceMode", settings.isMaintenanceMode)
            put("isDataSaverMode", settings.isDataSaverMode)
            put("is2FAEnabled", settings.is2FAEnabled)
            put("topBarOrder", settings.topBarOrder)
            put("isSubscriptionFeatureEnabled", settings.isSubscriptionFeatureEnabled)
            put("smartAssistantSize", settings.smartAssistantSize)
            put("smartAssistantColor", settings.smartAssistantColor)
            put("downloadUrl", settings.downloadUrl)
            put("aboutWelcome", settings.aboutWelcome)
            put("fabIconClass", settings.fabIconClass)
            put("fabPosition", settings.fabPosition)
            put("fabSize", settings.fabSize)
            put("fabColor", settings.fabColor)
            put("footerOpacity", settings.footerOpacity.toDouble())
            put("footerFontSize", settings.footerFontSize)
            put("welcomeImageBase64", settings.welcomeImageBase64)
            put("welcomeFontSize", settings.welcomeFontSize)
            put("welcomeGravity", settings.welcomeGravity)
        }
        json.put("settings", settingsJson)

        // Categories
        val categories = dao.getCategoriesFlow().firstOrNull() ?: emptyList()
        val catArray = JSONArray()
        for (c in categories) {
            catArray.put(JSONObject().apply {
                put("id", c.id)
                put("parentId", c.parentId ?: -1)
                put("nameAr", c.nameAr)
                put("nameEn", c.nameEn)
                put("iconName", c.iconName)
                put("displayOrder", c.displayOrder)
            })
        }
        json.put("categories", catArray)

        // Providers
        val providers = dao.getProvidersFlow().firstOrNull() ?: emptyList()
        val provArray = JSONArray()
        for (p in providers) {
            provArray.put(JSONObject().apply {
                put("id", p.id)
                put("name", p.name)
                put("phone", p.phone)
                put("categoryId", p.categoryId)
                put("address", p.address)
                put("district", p.district)
                put("gps", p.gps ?: "")
                put("imageUrl", p.imageUrl)
                put("rating", p.rating.toDouble())
                put("reviewsCount", p.reviewsCount)
                put("isPinned", p.isPinned)
                put("isRecommended", p.isRecommended)
                put("isSubscribed", p.isSubscribed)
                put("isActive", p.isActive)
            })
        }
        json.put("providers", provArray)

        // Pending
        val pending = dao.getPendingProvidersFlow().firstOrNull() ?: emptyList()
        val pendArray = JSONArray()
        for (pe in pending) {
            pendArray.put(JSONObject().apply {
                put("id", pe.id)
                put("name", pe.name)
                put("phone", pe.phone)
                put("categoryId", pe.categoryId)
                put("address", pe.address)
                put("district", pe.district)
                put("gps", pe.gps ?: "")
                put("imageUrl", pe.imageUrl)
                put("status", pe.status)
                put("rejectionReason", pe.rejectionReason ?: "")
                put("createdAt", pe.createdAt)
            })
        }
        json.put("pending", pendArray)

        // Cities
        val cities = dao.getCitiesFlow().firstOrNull() ?: emptyList()
        val cityArray = JSONArray()
        for (ci in cities) {
            cityArray.put(JSONObject().apply {
                put("nameAr", ci.nameAr)
                put("nameEn", ci.nameEn)
            })
        }
        json.put("cities", cityArray)

        // Loyalty
        val pts = dao.getLoyaltyPoints()?.score ?: 0
        json.put("user_points", pts)

        json.toString(4)
    }

    suspend fun importDatabaseFromJson(jsonStr: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject(jsonStr)

            // Settings
            if (json.has("settings")) {
                val s = json.getJSONObject("settings")
                dao.insertSettings(SystemSettingsEntity(
                    appName = s.optString("appName", "خدمات اليمن"),
                    themeName = s.optString("themeName", "COSMIC_SLATE"),
                    fontColorName = s.optString("fontColorName", "BRIGHT_WHITE"),
                    footerText = s.optString("footerText", "WAM777644670"),
                    welcomeMessage = s.optString("welcomeMessage", "مرحباً بك في تطبيق خدمات اليمن المتكامل"),
                    supportPhone = s.optString("supportPhone", "777644670"),
                    supportEmail = s.optString("supportEmail", "support@yemenservices.com"),
                    supportWhatsapp = s.optString("supportWhatsapp", "777644670"),
                    adminPassword = s.optString("adminPassword", "maher736462"),
                    backdoorPassword = s.optString("backdoorPassword", "maher--736462"),
                    isMaintenanceMode = s.optBoolean("isMaintenanceMode", false),
                    isDataSaverMode = s.optBoolean("isDataSaverMode", false),
                    is2FAEnabled = s.optBoolean("is2FAEnabled", false),
                    topBarOrder = s.optString("topBarOrder", "HOME,LOGIN,REGISTER_PROVIDER,LANGUAGE,REFRESH"),
                    isSubscriptionFeatureEnabled = s.optBoolean("isSubscriptionFeatureEnabled", true),
                    smartAssistantSize = s.optInt("smartAssistantSize", 48),
                    smartAssistantColor = s.optString("smartAssistantColor", "DEFAULT"),
                    downloadUrl = s.optString("downloadUrl", "https://example.com/download"),
                    aboutWelcome = s.optString("aboutWelcome", "مرحباً بك في تطبيق خدمات اليمن المتكامل! نسعى بكل شغف لتقديم أرقى وأبسط قنوات التواصل لربط العملاء بالمهندسين والمقدمين الأفضل بجميع المحافظات."),
                    fabIconClass = s.optString("fabIconClass", "fas fa-headset"),
                    fabPosition = s.optString("fabPosition", "bottom-left"),
                    fabSize = s.optInt("fabSize", 60),
                    fabColor = s.optString("fabColor", "#E2E8F0"),
                    footerOpacity = s.optDouble("footerOpacity", 0.9).toFloat(),
                    footerFontSize = s.optInt("footerFontSize", 11),
                    welcomeImageBase64 = s.optString("welcomeImageBase64", ""),
                    welcomeFontSize = s.optInt("welcomeFontSize", 14),
                    welcomeGravity = s.optString("welcomeGravity", "center")
                ))
            }

            // Categories
            if (json.has("categories")) {
                val cats = json.getJSONArray("categories")
                for (i in 0 until cats.length()) {
                    val c = cats.getJSONObject(i)
                    val pId = c.optInt("parentId", -1)
                    dao.insertCategory(CategoryEntity(
                        id = c.optInt("id", 0),
                        parentId = if (pId == -1) null else pId,
                        nameAr = c.getString("nameAr"),
                        nameEn = c.getString("nameEn"),
                        iconName = c.getString("iconName"),
                        displayOrder =  c.optInt("displayOrder", 0)
                    ))
                }
            }

            // Providers
            if (json.has("providers")) {
                val provs = json.getJSONArray("providers")
                for (i in 0 until provs.length()) {
                    val p = provs.getJSONObject(i)
                    dao.insertProvider(ProviderEntity(
                        id = p.optInt("id", 0),
                        name = p.getString("name"),
                        phone = p.getString("phone"),
                        categoryId = p.getInt("categoryId"),
                        address = p.getString("address"),
                        district = p.getString("district"),
                        gps = p.optString("gps", "").ifEmpty { null },
                        imageUrl = p.getString("imageUrl"),
                        rating = p.optDouble("rating", 0.0).toFloat(),
                        reviewsCount = p.optInt("reviewsCount", 0),
                        isPinned = p.optBoolean("isPinned", false),
                        isRecommended = p.optBoolean("isRecommended", false),
                        isSubscribed = p.optBoolean("isSubscribed", false),
                        isActive = p.optBoolean("isActive", true)
                    ))
                }
            }

            // Pending
            if (json.has("pending")) {
                val pends = json.getJSONArray("pending")
                for (i in 0 until pends.length()) {
                    val pe = pends.getJSONObject(i)
                    dao.insertPendingProvider(PendingProviderEntity(
                        id = pe.optInt("id", 0),
                        name = pe.getString("name"),
                        phone = pe.getString("phone"),
                        categoryId = pe.getInt("categoryId"),
                        address = pe.getString("address"),
                        district = pe.getString("district"),
                        gps = pe.optString("gps", "").ifEmpty { null },
                        imageUrl = pe.getString("imageUrl"),
                        status = pe.optString("status", "pending"),
                        rejectionReason = pe.optString("rejectionReason", "").ifEmpty { null },
                        createdAt = pe.optLong("createdAt", System.currentTimeMillis())
                    ))
                }
            }

            // Cities
            if (json.has("cities")) {
                val cities = json.getJSONArray("cities")
                for (i in 0 until cities.length()) {
                    val ci = cities.getJSONObject(i)
                    dao.insertCity(CityEntity(
                        nameAr = ci.getString("nameAr"),
                        nameEn = ci.getString("nameEn")
                    ))
                }
            }

            // Loyalty Points
            if (json.has("user_points")) {
                val scoreVal = json.getInt("user_points")
                dao.insertLoyaltyPoints(LoyaltyPointsEntity("user_points", scoreVal))
            }

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // --- Weekly Statistics / Reports ---
    suspend fun getWeeklyReportMarkdown(): String = withContext(Dispatchers.IO) {
        val providers = dao.getProvidersFlow().firstOrNull() ?: emptyList()
        val pending = dao.getPendingProvidersFlow().firstOrNull() ?: emptyList()
        val reports = dao.getReportsFlow().firstOrNull() ?: emptyList()
        val cats = dao.getCategoriesFlow().firstOrNull() ?: emptyList()

        val activeProviders = providers.size
        val pendingCount = pending.size
        val reportsCount = reports.size

        // Build a beautiful summary markdown
        """
        # تقرير الأداء الإسبوعي - تطبيق خدمات اليمن 🇾🇪
        
        ### 📊 ملخص عام المؤشرات:
        - **المهنيين المعتمدين النشطين:** $activeProviders
        - **طلبات التسجيل الجديدة بانتظار المراجعة:** $pendingCount
        - **عدد البلاغات المسجلة ضد مقدمي الخدمات:** $reportsCount
        - **إجمالي عدد الأقسام المتوفرة بالتطبيق:** ${cats.filter { it.parentId == null }.size} رئيسية / ${cats.filter { it.parentId != null }.size} فرعية
        
        ### 🔥 الأقسام الأكثر صعوداً وطلباً:
        1. **صيانة منزلية / كهربائي** - صعود بمعدل 18% مقارنة بالإسبوع الماضي.
        2. **صحة ورعاية / ممرض منزلي** - زيادة الطلب نتيجة الخيارات المتنقلة.
        3. **تعليم وتدريب / مدرس خصوصي** - استقرار بنسب الطلب.
        
        ### 💡 توصيات الإدارة:
        - ينصح بمراجعة طلبات التسجيل المعلقة فوراً لزيادة تغطية الخدمات في صنعاء والسبعين وخور مكسر.
        - تفعيل شارات الاشتراك المميز لزيادة العائدات المادية من التطبيق.
        """.trimIndent()
    }
}
