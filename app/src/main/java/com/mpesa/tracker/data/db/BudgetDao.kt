package com.mpesa.tracker.data.db

import androidx.room.*
import com.mpesa.tracker.data.model.Budget
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(budget: Budget): Long

    @Update
    suspend fun update(budget: Budget)

    @Delete
    suspend fun delete(budget: Budget)

    @Query("SELECT * FROM budgets WHERE month = :month AND year = :year ORDER BY category ASC")
    fun getBudgetsForMonth(month: Int, year: Int): Flow<List<Budget>>

    @Query("SELECT * FROM budgets WHERE month = :month AND year = :year AND category = :category LIMIT 1")
    suspend fun getBudgetForCategory(category: String, month: Int, year: Int): Budget?

    @Query("SELECT DISTINCT category FROM budgets ORDER BY category ASC")
    suspend fun getAllCategories(): List<String>
}
