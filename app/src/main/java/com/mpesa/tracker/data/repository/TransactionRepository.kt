package com.mpesa.tracker.data.repository

import android.util.Log
import com.mpesa.tracker.data.db.BudgetDao
import com.mpesa.tracker.data.db.CategoryDao
import com.mpesa.tracker.data.db.CategoryMappingDao
import com.mpesa.tracker.data.db.CategoryRuleDao
import com.mpesa.tracker.data.db.CategoryTotal
import com.mpesa.tracker.data.db.ExclusionRuleDao
import com.mpesa.tracker.data.db.TransactionDao
import com.mpesa.tracker.data.model.Budget
import com.mpesa.tracker.data.model.BudgetWithSpent
import com.mpesa.tracker.data.model.CategoryMapping
import com.mpesa.tracker.data.model.CategoryRule
import com.mpesa.tracker.data.model.ExclusionRule
import com.mpesa.tracker.data.model.Transaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Calendar

class TransactionRepository(
    private val transactionDao: TransactionDao,
    private val budgetDao: BudgetDao,
    private val categoryMappingDao: CategoryMappingDao,
    private val categoryRuleDao: CategoryRuleDao,
    private val exclusionRuleDao: ExclusionRuleDao,
    private val categoryDao: CategoryDao
) {

    companion object {
        private const val TAG = "TransactionRepo"
    }

    // ── Categories ────────────────────────────────────────────────────────────

    fun getAllCategories(): Flow<List<com.mpesa.tracker.data.model.Category>> =
        categoryDao.getAllCategories()

    suspend fun addCategory(name: String) {
        categoryDao.insert(com.mpesa.tracker.data.model.Category(name = name, isDefault = false))
    }

    suspend fun deleteCategory(category: com.mpesa.tracker.data.model.Category) {
        categoryDao.delete(category)
    }

    // ── Transactions ──────────────────────────────────────────────────────────

    fun getAllTransactions(): Flow<List<Transaction>> =
        transactionDao.getAllTransactionsIncludingExcluded().map { applyCategoryRules(it) }

    fun getTransactionsByDateRange(startMs: Long, endMs: Long): Flow<List<Transaction>> =
        transactionDao.getTransactionsByDateRange(startMs, endMs).map { applyCategoryRules(it) }

    private suspend fun applyCategoryRules(transactions: List<Transaction>): List<Transaction> {
        val rules = categoryRuleDao.getAll().associateBy { it.identifier }
        return transactions.map { tx ->
            val id = tx.recipient?.lowercase()?.trim()
            if (id != null && !tx.isUserEdited) {
                rules[id]?.let { tx.copy(category = it.category) } ?: tx
            } else tx
        }
    }

    /**
     * Insert a transaction.
     *
     * Post-processing steps (parser is NEVER touched):
     *  1. Check exclusion rules — if matched, skip saving and return null.
     *  2. Apply smart category from learned rules / mappings.
     *  3. Save to Room.
     */
    suspend fun insertTransaction(transaction: Transaction): Transaction? {
        val normalizedName = (transaction.recipient ?: transaction.phone ?: "")
            .lowercase().trim()

        // ── Step 1: Exclusion check ───────────────────────────────────────────
        if (normalizedName.isNotBlank()) {
            val enabledRules = exclusionRuleDao.getEnabledRules()
            val matchedRule  = enabledRules.firstOrNull { it.matches(normalizedName) }
            if (matchedRule != null) {
                Log.d(TAG, "Excluded '$normalizedName' by rule '${matchedRule.keyword}'")
                return null   // silently dropped — parser already ran, we just don't save
            }
        }

        // ── Step 2: Smart category ────────────────────────────────────────────
        val resolvedCategory = if (!transaction.isUserEdited && normalizedName.isNotBlank()) {
            getSmartCategory(transaction.recipient ?: "") ?: transaction.category
        } else transaction.category

        val txToSave = if (resolvedCategory != transaction.category)
            transaction.copy(category = resolvedCategory) else transaction

        // ── Step 3: Persist ───────────────────────────────────────────────────
        val id = transactionDao.insert(txToSave)
        return txToSave.copy(id = id)
    }

    suspend fun findByTransactionId(txId: String): Transaction? =
        transactionDao.findByTransactionId(txId)

    suspend fun getSmartCategory(recipient: String): String? {
        val key = recipient.lowercase().trim()
        return categoryRuleDao.getCategory(key)
            ?: categoryMappingDao.getCategoryForText(recipient)
    }

    suspend fun updateTransaction(transaction: Transaction) =
        transactionDao.update(transaction)

    suspend fun updateTransactionWithRule(transaction: Transaction, newCategory: String) {
        val updated = transaction.copy(category = newCategory, isUserEdited = true)
        transactionDao.update(updated)
        val id = transaction.recipient?.lowercase()?.trim()
        if (id != null) {
            categoryRuleDao.insert(CategoryRule(identifier = id, category = newCategory))
        }
    }

    suspend fun deleteTransaction(transaction: Transaction) =
        transactionDao.delete(transaction)

    fun getTotalExpenses(startMs: Long, endMs: Long): Flow<Double> =
        transactionDao.getTotalExpensesFlow(startMs, endMs).map { it ?: 0.0 }

    fun getTotalIncome(startMs: Long, endMs: Long): Flow<Double> =
        transactionDao.getTotalIncomeFlow(startMs, endMs).map { it ?: 0.0 }

    fun getExpensesByCategory(startMs: Long, endMs: Long): Flow<List<CategoryTotal>> =
        transactionDao.getExpensesByCategoryFlow(startMs, endMs)

    fun getRecentTransactions(limit: Int = 10): Flow<List<Transaction>> =
        transactionDao.getRecentTransactionsFlow(limit)

    suspend fun getRecentTransactionsList(limit: Int = 10): List<Transaction> =
        transactionDao.getRecentTransactions(limit)

    suspend fun getTransactionsForExport(startMs: Long, endMs: Long): List<Transaction> =
        transactionDao.getForExport(startMs, endMs)

    // ── Exclusion Rules ───────────────────────────────────────────────────────

    fun getAllExclusionRules(): Flow<List<ExclusionRule>> =
        exclusionRuleDao.getAllRules()

    suspend fun addExclusionRule(keyword: String, matchType: ExclusionRule.MatchType): Long =
        exclusionRuleDao.insert(
            ExclusionRule(keyword = keyword.lowercase().trim(), matchType = matchType)
        )

    suspend fun toggleExclusionRule(rule: ExclusionRule, enabled: Boolean) =
        exclusionRuleDao.setEnabled(rule.id, enabled)

    suspend fun deleteExclusionRule(rule: ExclusionRule) =
        exclusionRuleDao.delete(rule)

    // ── Category Mappings ─────────────────────────────────────────────────────

    suspend fun saveCategoryMapping(searchText: String, category: String) {
        categoryMappingDao.insert(CategoryMapping(searchText, category))
    }

    // ── Budgets ───────────────────────────────────────────────────────────────

    fun getBudgetsForMonth(month: Int, year: Int): Flow<List<Budget>> =
        budgetDao.getBudgetsForMonth(month, year)

    fun getTotalBudgetForMonth(month: Int, year: Int): Flow<Double> =
        budgetDao.getBudgetsForMonth(month, year).map { budgets ->
            budgets.sumOf { it.limitAmount }
        }

    suspend fun insertBudget(budget: Budget): Long = budgetDao.insert(budget)
    suspend fun updateBudget(budget: Budget) = budgetDao.update(budget)
    suspend fun deleteBudget(budget: Budget) = budgetDao.delete(budget)

    suspend fun getSpentInCategory(category: String, startMs: Long, endMs: Long): Double =
        transactionDao.getByCategory(category, startMs, endMs).filter { it.isExpense }.sumOf { it.amount }

    fun monthRange(month: Int, year: Int): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        cal.set(year, month, 1, 0, 0, 0); cal.set(Calendar.MILLISECOND, 0)
        val start = cal.timeInMillis
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
        cal.set(Calendar.HOUR_OF_DAY, 23); cal.set(Calendar.MINUTE, 59); cal.set(Calendar.SECOND, 59)
        return Pair(start, cal.timeInMillis)
    }
}
