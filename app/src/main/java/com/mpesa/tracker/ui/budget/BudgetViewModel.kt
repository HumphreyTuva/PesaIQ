package com.mpesa.tracker.ui.budget

import androidx.lifecycle.*
import com.mpesa.tracker.data.model.Budget
import com.mpesa.tracker.data.model.BudgetWithSpent
import com.mpesa.tracker.data.repository.TransactionRepository
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.util.Calendar

class BudgetViewModel(private val repo: TransactionRepository) : ViewModel() {

    val currentMonth = Calendar.getInstance().get(Calendar.MONTH)
    val currentYear  = Calendar.getInstance().get(Calendar.YEAR)

    private val range = repo.monthRange(currentMonth, currentYear)
    private val startMs = range.first
    private val endMs = range.second

    val budgets: LiveData<List<Budget>> =
        repo.getBudgetsForMonth(currentMonth, currentYear).asLiveData()

    val budgetsWithSpent: LiveData<List<BudgetWithSpent>> =
        repo.getBudgetsForMonth(currentMonth, currentYear).combine(
            repo.getTransactionsByDateRange(startMs, endMs)
        ) { budgetList, transactionList ->
            budgetList.map { budget ->
                val spent = transactionList
                    .filter { it.category == budget.category && it.isExpense }
                    .sumOf { it.amount }
                BudgetWithSpent(budget, spent)
            }
        }.asLiveData()

    fun addBudget(category: String, limit: Double) {
        viewModelScope.launch {
            val budget = Budget(
                category    = category,
                limitAmount = limit,
                month       = currentMonth,
                year        = currentYear
            )
            repo.insertBudget(budget)
        }
    }

    fun updateBudget(budget: Budget, newLimit: Double) {
        viewModelScope.launch {
            repo.updateBudget(budget.copy(limitAmount = newLimit))
        }
    }

    fun deleteBudget(budget: Budget) {
        viewModelScope.launch { repo.deleteBudget(budget) }
    }

    val availableCategories = listOf(
        "Groceries", "Utilities", "Transport", "Food & Dining",
        "Airtime", "Entertainment", "Health", "Education",
        "Transfer", "Withdrawal", "Other"
    )
}

class BudgetViewModelFactory(private val repo: TransactionRepository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return BudgetViewModel(repo) as T
    }
}
