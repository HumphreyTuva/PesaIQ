package com.mpesa.tracker.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.mpesa.tracker.data.model.Budget
import com.mpesa.tracker.data.model.CategoryMapping
import com.mpesa.tracker.data.model.CategoryRule
import com.mpesa.tracker.data.model.ExclusionRule
import com.mpesa.tracker.data.model.Transaction

@Database(
    entities = [
        Transaction::class,
        Budget::class,
        CategoryMapping::class,
        CategoryRule::class,
        ExclusionRule::class
    ],
    version = 5,
    exportSchema = false
)
@TypeConverters(Converters::class, ExclusionConverters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun transactionDao(): TransactionDao
    abstract fun budgetDao(): BudgetDao
    abstract fun categoryMappingDao(): CategoryMappingDao
    abstract fun categoryRuleDao(): CategoryRuleDao
    abstract fun exclusionRuleDao(): ExclusionRuleDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // v2 → v3 : adds isUserEdited + category_rules table
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE transactions ADD COLUMN isUserEdited INTEGER NOT NULL DEFAULT 0")
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS category_rules (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        identifier TEXT NOT NULL,
                        category TEXT NOT NULL
                    )
                """.trimIndent())
            }
        }

        // v3 → v4 : adds exclusion_rules table + seeds preset rules
        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `exclusion_rules` (
                        `id`        INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `keyword`   TEXT    NOT NULL,
                        `matchType` TEXT    NOT NULL DEFAULT 'CONTAINS',
                        `isEnabled` INTEGER NOT NULL DEFAULT 1,
                        `isPreset`  INTEGER NOT NULL DEFAULT 0,
                        `createdAt` INTEGER NOT NULL,
                        UNIQUE(`keyword`)
                    )
                """.trimIndent())

                // Seed preset exclusion rules — all disabled by default so
                // users opt-in rather than being surprised
                val now = System.currentTimeMillis()
                val presets = listOf(
                    "m-shwari", "mshwari", "fuliza", "ziidi", "m-pawa",
                    "international", "western union", "world remit", "moneygram",
                    "loan", "repayment", "interest charged"
                )
                presets.forEach { kw ->
                    db.execSQL("""
                        INSERT OR IGNORE INTO exclusion_rules
                        (keyword, matchType, isEnabled, isPreset, createdAt)
                        VALUES ('$kw', 'CONTAINS', 0, 1, $now)
                    """.trimIndent())
                }
            }
        }

        // v4 → v5 : adds isExcluded column to transactions
        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE transactions ADD COLUMN isExcluded INTEGER NOT NULL DEFAULT 0")
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mpesa_tracker.db"
                )
                    .addMigrations(MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
