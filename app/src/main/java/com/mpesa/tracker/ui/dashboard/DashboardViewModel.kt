package com.mpesa.tracker.ui.dashboard

import androidx.lifecycle.*
import com.mpesa.tracker.data.db.CategoryTotal
import com.mpesa.tracker.data.model.Transaction
import com.mpesa.tracker.data.repository.TransactionRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

data class DashboardState(
    val totalIncome: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val balance: Double = 0.0,
    val monthlyLimit: Double = 0.0,
    val recentTransactions: List<Transaction> = emptyList(),
    val categoryBreakdown: List<CategoryTotal> = emptyList(),
    val isLoading: Boolean = false
)

class DashboardViewModel(private val repo: TransactionRepository) : ViewModel() {

    private val _state = MutableLiveData(DashboardState(isLoading = true))
    val state: LiveData<DashboardState> = _state

    // Month selector (default: current month)
    private val _selectedMonth = MutableStateFlow(Calendar.getInstance().get(Calendar.MONTH))
    private val _selectedYear  = MutableStateFlow(Calendar.getInstance().get(Calendar.YEAR))
    
    val selectedMonth: LiveData<Int> = _selectedMonth.asLiveData()
    val selectedYear:  LiveData<Int> = _selectedYear.asLiveData()

    init {
        observeDashboardData()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeDashboardData() {
        combine(_selectedMonth, _selectedYear) { month, year ->
            month to year
        }.flatMapLatest { (month, year) ->
            val (start, end) = repo.monthRange(month, year)
            
            combine(
                repo.getTotalIncome(start, end),
                repo.getTotalExpenses(start, end),
                repo.getRecentTransactions(10),
                repo.getExpensesByCategory(start, end),
                repo.getTotalBudgetForMonth(month, year)
            ) { income, expenses, recent, categories, budgetLimit ->
                DashboardState(
                    totalIncome = income,
                    totalExpenses = expenses,
                    balance = income - expenses,
                    monthlyLimit = budgetLimit,
                    recentTransactions = recent,
                    categoryBreakdown = categories,
                    isLoading = false
                )
            }
        }.onEach { newState ->
            _state.value = newState
        }.launchIn(viewModelScope)
    }

    fun previousMonth() {
        val cal = Calendar.getInstance()
        cal.set(_selectedYear.value, _selectedMonth.value, 1)
        cal.add(Calendar.MONTH, -1)
        _selectedMonth.value = cal.get(Calendar.MONTH)
        _selectedYear.value  = cal.get(Calendar.YEAR)
    }

    fun nextMonth() {
        val cal = Calendar.getInstance()
        cal.set(_selectedYear.value, _selectedMonth.value, 1)
        cal.add(Calendar.MONTH, 1)
        _selectedMonth.value = cal.get(Calendar.MONTH)
        _selectedYear.value  = cal.get(Calendar.YEAR)
    }
}

class DashboardViewModelFactory(private val repo: TransactionRepository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return DashboardViewModel(repo) as T
    }
}
