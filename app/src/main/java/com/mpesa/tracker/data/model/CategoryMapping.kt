package com.mpesa.tracker.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "category_mappings")
data class CategoryMapping(
    @PrimaryKey
    val searchText: String, // e.g., "NAIVAS"
    val category: String    // e.g., "Groceries"
)
