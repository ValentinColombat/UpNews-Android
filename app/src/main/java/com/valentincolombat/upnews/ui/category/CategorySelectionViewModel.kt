package com.valentincolombat.upnews.ui.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.valentincolombat.upnews.data.repository.UserRepository
import com.valentincolombat.upnews.service.AppStateService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CategorySelectionViewModel : ViewModel() {

    private val _selectedCategories = MutableStateFlow<Set<String>>(emptySet())
    val selectedCategories: StateFlow<Set<String>> = _selectedCategories.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun toggleCategory(id: String) {
        _selectedCategories.value = _selectedCategories.value.toMutableSet().apply {
            if (contains(id)) remove(id) else add(id)
        }
    }

    fun confirmSelection() {
        if (_selectedCategories.value.isEmpty()) return

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                UserRepository.shared.savePreferredCategories(_selectedCategories.value.toList())
                AppStateService.shared.handleCategoriesSelected()
            } catch (e: Exception) {
                _errorMessage.value = "Impossible de sauvegarder. Vérifie ta connexion et réessaie."
                _isLoading.value = false
            }
        }
    }
}
