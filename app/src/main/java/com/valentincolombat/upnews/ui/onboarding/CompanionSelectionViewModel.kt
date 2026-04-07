package com.valentincolombat.upnews.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.valentincolombat.upnews.data.repository.UserRepository
import com.valentincolombat.upnews.service.AppStateService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.valentincolombat.upnews.utils.isNetworkError
import kotlinx.coroutines.launch

class CompanionSelectionViewModel : ViewModel() {

    private val userRepo = UserRepository.shared

    private val _selectedCompanionId = MutableStateFlow<String?>(null)
    val selectedCompanionId: StateFlow<String?> = _selectedCompanionId.asStateFlow()

    private val _displayName = MutableStateFlow("")
    val displayName: StateFlow<String> = _displayName.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun selectCompanion(id: String) {
        _selectedCompanionId.value = id
    }

    fun updateDisplayName(name: String) {
        if (name.length <= 10) _displayName.value = name
    }

    fun confirmSelection() {
        val companionId = _selectedCompanionId.value ?: return
        val trimmedName = _displayName.value.trim()

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                userRepo.saveCompanionSelection(trimmedName, companionId)
                AppStateService.shared.handleCompanionSelected()
            } catch (e: Exception) {
                _errorMessage.value = if (e.isNetworkError())
                    "Pas de connexion internet. Vérifie ta connexion et réessaie."
                else
                    "Une erreur est survenue. Réessaie dans quelques instants."
            } finally {
                _isLoading.value = false
            }
        }
    }

}
