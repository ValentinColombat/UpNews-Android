package com.valentincolombat.upnews.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.valentincolombat.upnews.data.local.ONBOARDING_COMPLETED
import com.valentincolombat.upnews.data.local.dataStore
import com.valentincolombat.upnews.service.AppStateService
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class AppViewModel(application: Application) : AndroidViewModel(application) {

    val currentScreen = AppStateService.shared.currentScreen

    private var hasCompletedOnboarding = false

    init {
        viewModelScope.launch {
            hasCompletedOnboarding = getApplication<Application>().dataStore.data
                .map { it[ONBOARDING_COMPLETED] ?: false }
                .first()
            AppStateService.shared.initialize(hasCompletedOnboarding)
        }
    }

    /** Relance initialize() depuis l'écran d'erreur */
    fun retry() {
        viewModelScope.launch {
            AppStateService.shared.initialize(hasCompletedOnboarding)
        }
    }
}
