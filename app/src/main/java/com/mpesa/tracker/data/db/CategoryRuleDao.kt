package com.mpesa.tracker.data.db

import androidx.room.*
import com.mpesa.tracker.data.model.CategoryRule

@Dao
interface CategoryRuleDao {
    @Query("SELECT * FROM category_rules")
    suspend fun getAll(): List<CategoryRule>

    @Query("SELECT category FROM category_rules WHERE identifier = :identifier LIMIT 1")
    suspend fun getCategory(identifier: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(rule: CategoryRule)

    @Query("DELETE FROM category_rules")
    suspend fun deleteAll()
}
