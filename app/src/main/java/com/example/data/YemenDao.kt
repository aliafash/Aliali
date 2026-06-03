package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface YemenDao {

    // --- System Settings ---
    @Query("SELECT * FROM system_settings WHERE id = 'global'")
    suspend fun getSettings(): SystemSettingsEntity?

    @Query("SELECT * FROM system_settings WHERE id = 'global'")
    fun getSettingsFlow(): Flow<SystemSettingsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSettings(settings: SystemSettingsEntity)

    // --- Categories ---
    @Query("SELECT * FROM categories ORDER BY displayOrder ASC")
    fun getCategoriesFlow(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE parentId IS NULL ORDER BY displayOrder ASC")
    fun getMainCategoriesFlow(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE parentId = :parentId ORDER BY displayOrder ASC")
    fun getSubCategoriesFlow(parentId: Int): Flow<List<CategoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CategoryEntity): Long

    @Query("DELETE FROM categories WHERE id = :id")
    suspend fun deleteCategoryById(id: Int)

    // --- Providers ---
    @Query("SELECT * FROM providers ORDER BY isPinned DESC, rating DESC")
    fun getProvidersFlow(): Flow<List<ProviderEntity>>

    @Query("SELECT * FROM providers WHERE isRecommended = 1 ORDER BY rating DESC")
    fun getRecommendedProvidersFlow(): Flow<List<ProviderEntity>>

    @Query("SELECT * FROM providers WHERE categoryId = :categoryId ORDER BY isPinned DESC, rating DESC")
    fun getProvidersByCategoryFlow(categoryId: Int): Flow<List<ProviderEntity>>

    @Query("SELECT * FROM providers WHERE id = :id")
    suspend fun getProviderById(id: Int): ProviderEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProvider(provider: ProviderEntity): Long

    @Query("DELETE FROM providers WHERE id = :id")
    suspend fun deleteProviderById(id: Int)

    // --- Pending Providers ---
    @Query("SELECT * FROM pending_providers ORDER BY createdAt DESC")
    fun getPendingProvidersFlow(): Flow<List<PendingProviderEntity>>

    @Query("SELECT * FROM pending_providers WHERE id = :id")
    suspend fun getPendingProviderById(id: Int): PendingProviderEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPendingProvider(pending: PendingProviderEntity): Long

    @Query("DELETE FROM pending_providers WHERE id = :id")
    suspend fun deletePendingProviderById(id: Int)

    // --- Reviews ---
    @Query("SELECT * FROM reviews WHERE providerId = :providerId ORDER BY createdAt DESC")
    fun getReviewsFlow(providerId: Int): Flow<List<ReviewEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReview(review: ReviewEntity): Long

    // --- Banners ---
    @Query("SELECT * FROM banners WHERE isActive = 1")
    fun getActiveBannersFlow(): Flow<List<BannerEntity>>

    @Query("SELECT * FROM banners")
    fun getAllBannersFlow(): Flow<List<BannerEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBanner(banner: BannerEntity): Long

    @Query("DELETE FROM banners WHERE id = :id")
    suspend fun deleteBannerById(id: Int)

    // --- Reports ---
    @Query("SELECT * FROM reports ORDER BY createdAt DESC")
    fun getReportsFlow(): Flow<List<ReportEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: ReportEntity): Long

    @Query("DELETE FROM reports WHERE id = :id")
    suspend fun deleteReportById(id: Int)

    // --- Loyalty Points ---
    @Query("SELECT * FROM loyalty_points WHERE id = 'user_points'")
    suspend fun getLoyaltyPoints(): LoyaltyPointsEntity?

    @Query("SELECT * FROM loyalty_points WHERE id = 'user_points'")
    fun getLoyaltyPointsFlow(): Flow<LoyaltyPointsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLoyaltyPoints(points: LoyaltyPointsEntity)

    // --- Cities (Advanced Search Filters) ---
    @Query("SELECT * FROM app_cities ORDER BY nameAr ASC")
    fun getCitiesFlow(): Flow<List<CityEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCity(city: CityEntity)

    @Query("DELETE FROM app_cities WHERE nameAr = :nameAr")
    suspend fun deleteCityByName(nameAr: String)

    // --- Moderators ---
    @Query("SELECT * FROM moderators ORDER BY username ASC")
    fun getModeratorsFlow(): Flow<List<ModeratorEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertModerator(moderator: ModeratorEntity): Long

    @Query("DELETE FROM moderators WHERE id = :id")
    suspend fun deleteModeratorById(id: Int)
}
