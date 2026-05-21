package com.mpesa.tracker.ui.settings

import androidx.lifecycle.*
import com.mpesa.tracker.data.model.ExclusionRule
import com.mpesa.tracker.data.repository.TransactionRepository
import kotlinx.coroutines.launch

class SettingsViewModel(private val repo: TransactionRepository) : ViewModel() {

    val exclusionRules: LiveData<List<ExclusionRule>> =
        repo.getAllExclusionRules().asLiveData()

    val categories: LiveData<List<com.mpesa.tracker.data.model.Category>> =
        repo.getAllCategories().asLiveData()

    fun toggleRule(rule: ExclusionRule, enabled: Boolean) {
        viewModelScope.launch { repo.toggleExclusionRule(rule, enabled) }
    }

    fun addRule(keyword: String, matchType: ExclusionRule.MatchType) {
        if (keyword.isBlank()) return
        viewModelScope.launch { repo.addExclusionRule(keyword, matchType) }
    }

    fun deleteRule(rule: ExclusionRule) {
        viewModelScope.launch { repo.deleteExclusionRule(rule) }
    }

    fun addCategory(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch { repo.addCategory(name) }
    }

    fun deleteCategory(category: com.mpesa.tracker.data.model.Category) {
        viewModelScope.launch { repo.deleteCategory(category) }
    }
}

class SettingsViewModelFactory(private val repo: TransactionRepository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return SettingsViewModel(repo) as T
    }
}
