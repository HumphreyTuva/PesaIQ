package com.mpesa.tracker.ui.transactions

import androidx.lifecycle.*
import com.mpesa.tracker.data.model.Transaction
import com.mpesa.tracker.data.repository.TransactionRepository
import kotlinx.coroutines.launch

class TransactionsViewModel(private val repo: TransactionRepository) : ViewModel() {

    private val _searchQuery    = MutableLiveData("")
    private val _filterCategory = MutableLiveData<String?>(null)

    val allTransactions: LiveData<List<Transaction>> = repo.getAllTransactions().asLiveData()
    val categories: LiveData<List<com.mpesa.tracker.data.model.Category>> = repo.getAllCategories().asLiveData()

    val filteredTransactions: LiveData<List<Transaction>> =
        MediatorLiveData<List<Transaction>>().apply {
            fun update() {
                val all   = allTransactions.value ?: return
                val query = _searchQuery.value?.lowercase() ?: ""
                val cat   = _filterCategory.value
                value = all.filter { tx ->
                    val matchesQuery = query.isEmpty() ||
                        tx.recipient?.lowercase()?.contains(query) == true ||
                        tx.phone?.contains(query) == true ||
                        tx.category.lowercase().contains(query) ||
                        tx.transactionId.lowercase().contains(query) ||
                        tx.type.label.lowercase().contains(query)
                    
                    val matchesCategory = when (cat) {
                        null -> !tx.isExcluded // "All" shows only active
                        "Excluded" -> tx.isExcluded // "Excluded" shows only excluded
                        else -> tx.category == cat && !tx.isExcluded // Specific category shows active ones
                    }
                    
                    matchesQuery && matchesCategory
                }
            }
            addSource(allTransactions)    { update() }
            addSource(_searchQuery)       { update() }
            addSource(_filterCategory)    { update() }
        }

    fun setSearchQuery(q: String)        { _searchQuery.value = q }
    fun setFilterCategory(cat: String?)  { _filterCategory.value = cat }

    fun updateTransaction(tx: Transaction) {
        viewModelScope.launch { repo.updateTransaction(tx) }
    }

    fun updateTransactionWithRule(tx: Transaction, newCategory: String) {
        viewModelScope.launch { repo.updateTransactionWithRule(tx, newCategory) }
    }

    fun saveCategoryMapping(recipient: String, category: String) {
        viewModelScope.launch { repo.saveCategoryMapping(recipient, category) }
    }

    fun excludeTransaction(tx: Transaction) {
        viewModelScope.launch { 
            repo.updateTransaction(tx.copy(isExcluded = true)) 
        }
    }

    fun includeTransaction(tx: Transaction) {
        viewModelScope.launch { 
            repo.updateTransaction(tx.copy(isExcluded = false))
        }
    }

    fun deleteTransaction(tx: Transaction) {
        viewModelScope.launch { repo.deleteTransaction(tx) }
    }

    fun restoreTransaction(tx: Transaction) {
        viewModelScope.launch { repo.insertTransaction(tx) }
    }

    fun addManualTransaction(transaction: Transaction) {
        viewModelScope.launch { repo.insertTransaction(transaction) }
    }

    fun addCategory(name: String) {
        viewModelScope.launch { repo.addCategory(name) }
    }

    fun deleteCategory(category: com.mpesa.tracker.data.model.Category) {
        viewModelScope.launch { repo.deleteCategory(category) }
    }
}

class TransactionsViewModelFactory(private val repo: TransactionRepository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return TransactionsViewModel(repo) as T
    }
}
