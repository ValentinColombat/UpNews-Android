package com.valentincolombat.upnews.ui.onboarding

import android.app.Application
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.valentincolombat.upnews.data.local.ONBOARDING_COMPLETED
import com.valentincolombat.upnews.data.local.dataStore
import com.valentincolombat.upnews.service.AppStateService
import kotlinx.coroutines.launch

class OnboardingViewModel(application: Application) : AndroidViewModel(application) {

    /** Équivalent AppStateService.shared.completeOnboarding() iOS */
    fun completeOnboarding() {
        viewModelScope.launch {
            getApplication<Application>().dataStore.edit { prefs ->
                prefs[ONBOARDING_COMPLETED] = true
            }
            AppStateService.shared.completeOnboarding()
        }
    }
}
