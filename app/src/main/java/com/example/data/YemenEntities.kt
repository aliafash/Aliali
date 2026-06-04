package com.example.data

import androidx.room.*

@Entity(tableName = "system_settings")
data class SystemSettingsEntity(
    @PrimaryKey val id: String = "global",
    val appName: String = "خدمات اليمن",
    val themeName: String = "COSMIC_SLATE", // COSMIC_SLATE, CHARCOAL_GOLD, ROYAL_EMERALD, BLUE_RED, CUSTOM
    val fontColorName: String = "BRIGHT_WHITE", // BRIGHT_WHITE, LIGHT_GOLD, VIBRANT_SILVER
    val footerText: String = "WAM777644670",
    val welcomeMessage: String = "مرحباً بك في تطبيق خدمات اليمن المتكامل",
    val supportPhone: String = "777644670",
    val supportEmail: String = "support@yemenservices.com",
    val supportWhatsapp: String = "777644670",
    val adminPassword: String = "maher736462",
    val backdoorPassword: String = "maher--736462",
    val isMaintenanceMode: Boolean = false,
    val isDataSaverMode: Boolean = false,
    val is2FAEnabled: Boolean = false,
    val topBarOrder: String = "HOME,LOGIN,REGISTER_PROVIDER,LANGUAGE,REFRESH",
    val isSubscriptionFeatureEnabled: Boolean = true,
    val smartAssistantSize: Int = 48, // dp
    val smartAssistantColor: String = "DEFAULT",
    
    // Configurable About Page parameters
    val downloadUrl: String = "https://example.com/download",
    val socialShareLink: String = "https://t.me/yemenservices",
    val aboutWelcome: String = "مرحباً بك في تطبيق خدمات اليمن المتكامل! نسعى بكل شغف لتقديم أرقى وأبسط قنوات التواصل لربط العملاء بالمهندسين والمقدمين الأفضل بجميع المحافظات.",
    val fabIconClass: String = "fas fa-headset", // e.g., "fas fa-headset", "fas fa-phone-alt", "fas fa-envelope", "fas fa-share-alt"
    val fabPosition: String = "bottom-left", // "bottom-left", "bottom-right"
    val fabSize: Int = 60, // size in pixels (e.g. 50, 60, 70)
    val fabColor: String = "#E2E8F0", // HEX color code e.g. #D4AF37

    // Footer & Welcome panel customizations
    val footerOpacity: Float = 0.9f,
    val footerFontSize: Int = 11,
    val welcomeImageBase64: String = "", // Base64 of custom greeting image
    val welcomeFontSize: Int = 14,
    val welcomeGravity: String = "center", // "center", "right", "left"

    // Newly added options for Floating actions and voice notes
    val isVoiceNotesEnabled: Boolean = true,
    val isAppInfoVisible: Boolean = true,
    val appInfoSize: Int = 48,
    val appInfoIcon: String = "info",
    val isSmartAssistantVisible: Boolean = true,
    val smartAssistantIcon: String = "assistant",

    // Sponsored Dynamic Ad configuration
    val sponsoredAdText: String = "إعلان ممول متميز: تواصل الآن مع النخبة من مهندسي صيانة المنازل والخدمات في اليمن!",
    val sponsoredAdImage: String = "",
    val sponsoredAdVideoUrl: String = "",
    val sponsoredAdType: String = "TEXT", // TEXT, IMAGE, VIDEO
    val sponsoredAdDurationSec: Int = 10,
    val isSponsoredAdVisible: Boolean = true,

    // Dynamic Customizable Aesthetic Values
    val primaryColorHex: String = "#2563EB", // Blue Accent
    val secondaryColorHex: String = "#DC2626", // Red Accent
    val backgroundColorHex: String = "#0B0F19", // Deep Charcoal
    val surfaceColorHex: String = "#1E293B", // Navy Blue Card
    val inputFontColorHex: String = "#FFFFFF", // Clear bright white
    val inputFontWeight: String = "BOLD", // BOLD, NORMAL
    val inputFontFamily: String = "DEFAULT", // DEFAULT, MONOSPACE, SERIF, SANS_SERIF

    // Search and Area filter limits
    val maxRadiusLimit: Int = 100,

    // Configurable Auto Notifications template
    val isWelcomeNotifyEnabled: Boolean = true,
    val welcomeNotifyMsg: String = "مرحباً بك في تطبيق دليل خدمات اليمن الكرام!",
    val isAppointmentNotifyEnabled: Boolean = true,
    val appointmentNotifyMsg: String = "تذكير: يرجى الحفاظ على جودة وسرعة تلبية طلبات التواصل.",
    val isBillingNotifyEnabled: Boolean = true,
    val billingNotifyMsg: String = "تنبيه: تم طلب تسوية قيمة الاشتراك الشهري لمقدمي الخدمات.",
    val notificationSendHour: Int = 12,

    // Realtime chat switches
    val isChatFeatureEnabled: Boolean = true,
    val isChatFloatingIconVisible: Boolean = true,
    val chatFloatingIconSize: Int = 48,
    val chatFloatingIconSymbol: String = "chat"
)

@Entity(tableName = "moderators")
data class ModeratorEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val username: String,
    val password: String,
    val permissions: String = "ALL", // ALL, CATEGORIES, PROVIDERS, REPORTS
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val parentId: Int? = null, // null for primary, non-null for sub-category
    val nameAr: String,
    val nameEn: String,
    val iconName: String,
    val displayOrder: Int = 0
)

@Entity(tableName = "providers")
data class ProviderEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val phone: String,
    val categoryId: Int,
    val address: String,
    val district: String,
    val gps: String? = null,
    val imageUrl: String, // Base64 or local URI
    val idCardUrl: String? = null,
    val rating: Float = 0f,
    val reviewsCount: Int = 0,
    val isPinned: Boolean = false,
    val isRecommended: Boolean = false,
    val isSubscribed: Boolean = false,
    val isActive: Boolean = true,
    val isVerified: Boolean = false,
    val isChatActive: Boolean = true
)

@Entity(tableName = "pending_providers")
data class PendingProviderEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val phone: String,
    val categoryId: Int,
    val address: String,
    val district: String,
    val gps: String? = null,
    val imageUrl: String,
    val idCardUrl: String? = null,
    val status: String = "pending", // pending, approved, rejected
    val rejectionReason: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val isVerified: Boolean = false,
    val isChatActive: Boolean = true
)

@Entity(tableName = "reviews")
data class ReviewEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val providerId: Int,
    val userName: String,
    val rating: Int, // 1 to 5
    val comment: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "banners")
data class BannerEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val imageUrl: String,
    val durationSec: Int = 5,
    val linkUrl: String? = null,
    val sizeType: String = "MEDIUM", // SMALL, MEDIUM, LARGE
    val isActive: Boolean = true
)

@Entity(tableName = "reports")
data class ReportEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val providerId: Int,
    val providerName: String,
    val reason: String,
    val text: String,
    val status: String = "pending", // pending, reviewed
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "loyalty_points")
data class LoyaltyPointsEntity(
    @PrimaryKey val id: String = "user_points",
    val score: Int = 0
)

@Entity(tableName = "app_cities")
data class CityEntity(
    @PrimaryKey val nameAr: String,
    val nameEn: String
)

@Entity(tableName = "subscription_requests")
data class SubscriptionRequestEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val providerId: Int,
    val providerName: String,
    val durationMonths: Int = 1,
    val paymentAmountAr: String = "10,000 ريال يمني",
    val transactionDetails: String = "",
    val status: String = "PENDING", // PENDING, APPROVED, REJECTED
    val requestedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "device_whitelist")
data class DeviceWhitelistEntity(
    @PrimaryKey val deviceId: String,
    val label: String,
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val senderName: String,
    val receiverId: Int, // provider ID, or 0 for Admin support
    val messageText: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isFromUser: Boolean = true // sender role: true = User/Guest, false = Provider or Admin
)

