package com.mpesa.tracker.data.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.mpesa.tracker.data.model.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(transaction: Transaction): Long

    @Update
    suspend fun update(transaction: Transaction)

    @Delete
    suspend fun delete(transaction: Transaction)

    @Query("SELECT * FROM transactions WHERE isExcluded = 0 ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactionsIncludingExcluded(): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE isExcluded = 0 AND timestamp BETWEEN :startMs AND :endMs ORDER BY timestamp DESC")
    fun getTransactionsByDateRange(startMs: Long, endMs: Long): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE category = :category AND timestamp BETWEEN :startMs AND :endMs")
    suspend fun getByCategory(category: String, startMs: Long, endMs: Long): List<Transaction>

    @Query("SELECT SUM(amount) FROM transactions WHERE isExcluded = 0 AND type IN ('SEND','PAYBILL','BUY_GOODS','WITHDRAW','AIRTIME') AND timestamp BETWEEN :startMs AND :endMs")
    fun getTotalExpensesFlow(startMs: Long, endMs: Long): Flow<Double?>

    @Query("SELECT SUM(amount) FROM transactions WHERE isExcluded = 0 AND type = 'RECEIVE' AND timestamp BETWEEN :startMs AND :endMs")
    fun getTotalIncomeFlow(startMs: Long, endMs: Long): Flow<Double?>

    @Query("SELECT category, SUM(amount) as total FROM transactions WHERE isExcluded = 0 AND type IN ('SEND','PAYBILL','BUY_GOODS','WITHDRAW','AIRTIME') AND timestamp BETWEEN :startMs AND :endMs GROUP BY category ORDER BY total DESC")
    fun getExpensesByCategoryFlow(startMs: Long, endMs: Long): Flow<List<CategoryTotal>>

    @Query("SELECT * FROM transactions WHERE transactionId = :txId LIMIT 1")
    suspend fun findByTransactionId(txId: String): Transaction?

    @Query("SELECT * FROM transactions ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentTransactionsFlow(limit: Int = 10): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentTransactions(limit: Int = 10): List<Transaction>

    @Query("SELECT * FROM transactions WHERE timestamp BETWEEN :startMs AND :endMs ORDER BY timestamp DESC")
    suspend fun getForExport(startMs: Long, endMs: Long): List<Transaction>

    @Query("DELETE FROM transactions")
    suspend fun deleteAll()
}

data class CategoryTotal(
    val category: String,
    val total: Double
)
