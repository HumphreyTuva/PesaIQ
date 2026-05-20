package com.mpesa.tracker.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "category_rules")
data class CategoryRule(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val identifier: String, // Normalized transaction recipient name (lowercase, trimmed)
    val category: String
)
