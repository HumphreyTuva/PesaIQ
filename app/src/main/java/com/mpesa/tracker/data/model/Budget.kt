package com.mpesa.tracker.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "budgets",
    indices = [Index(value = ["category", "month", "year"], unique = true)]
)
data class Budget(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val category: String,           // e.g. "Groceries", "Transport"
    val limitAmount: Double,        // Monthly budget limit
    val month: Int,                 // Calendar.MONTH (0-based)
    val year: Int,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun formattedLimit(): String = "Ksh %,.2f".format(limitAmount)
}

/** Aggregated view: budget + how much has been spent */
data class BudgetWithSpent(
    val budget: Budget,
    val spent: Double
) {
    val remaining: Double get() = budget.limitAmount - spent
    val progressPercent: Int get() = ((spent / budget.limitAmount) * 100).toInt().coerceIn(0, 100)
    val isOverBudget: Boolean get() = spent > budget.limitAmount
    fun formattedSpent(): String = "Ksh %,.2f".format(spent)
    fun formattedRemaining(): String = "Ksh %,.2f".format(remaining.coerceAtLeast(0.0))
}
