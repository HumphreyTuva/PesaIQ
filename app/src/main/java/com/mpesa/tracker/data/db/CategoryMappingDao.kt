package com.mpesa.tracker.data.db

import androidx.room.*
import com.mpesa.tracker.data.model.CategoryMapping

@Dao
interface CategoryMappingDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(mapping: CategoryMapping)

    @Query("SELECT * FROM category_mappings")
    suspend fun getAllMappings(): List<CategoryMapping>

    @Query("SELECT category FROM category_mappings WHERE searchText = :searchText LIMIT 1")
    suspend fun getCategoryForText(searchText: String): String?
}
