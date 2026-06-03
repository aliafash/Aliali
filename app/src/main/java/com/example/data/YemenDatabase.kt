package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        SystemSettingsEntity::class,
        CategoryEntity::class,
        ProviderEntity::class,
        PendingProviderEntity::class,
        ReviewEntity::class,
        BannerEntity::class,
        ReportEntity::class,
        LoyaltyPointsEntity::class,
        CityEntity::class,
        ModeratorEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class YemenDatabase : RoomDatabase() {
    abstract fun yemenDao(): YemenDao

    companion object {
        @Volatile
        private var INSTANCE: YemenDatabase? = null

        fun getDatabase(context: Context): YemenDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    YemenDatabase::class.java,
                    "yemen_services_db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
