package com.example.data

import androidx.room.*

@Entity(tableName = "system_settings")
data class SystemSettingsEntity(
    @PrimaryKey val id: String = "global",
    val appName: String = "خدمات اليمن",
    val themeName: String = "COSMIC_SLATE", // COSMIC_SLATE, CHARCOAL_GOLD, ROYAL_EMERALD
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
    val smartAssistantColor: String = "DEFAULT"
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
    val isActive: Boolean = true
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
    val createdAt: Long = System.currentTimeMillis()
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
