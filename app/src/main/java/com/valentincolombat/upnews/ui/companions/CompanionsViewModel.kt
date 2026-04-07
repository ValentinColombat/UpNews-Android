package com.valentincolombat.upnews.ui.companions

import androidx.annotation.DrawableRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.valentincolombat.upnews.data.model.CompanionData
import com.valentincolombat.upnews.ui.components.companionDrawable
import com.valentincolombat.upnews.data.model.SubscriptionTier
import com.valentincolombat.upnews.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// MARK: - Companion Model

data class CompanionCharacter(
    val id: String,
    val name: String,
    @get:DrawableRes val drawableId: Int,
    val unlockLevel: Int,
    val isUnlocked: Boolean,
    val isEquipped: Boolean
)

// MARK: - ViewModel

class CompanionsViewModel : ViewModel() {

    private val userRepo = UserRepository.shared

    // MARK: - State exposé depuis UserRepository

    val currentLevel             = userRepo.currentLevel
    val currentXp                = userRepo.currentXp
    val maxXp                    = userRepo.maxXp
    val notificationBonusClaimed = userRepo.notificationBonusClaimed
    val subscriptionTier         = userRepo.subscriptionTier
    val isPremium                = userRepo.subscriptionTier
        .map { it == SubscriptionTier.PREMIUM }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), userRepo.isPremium)

    // MARK: - État local

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    val companions: StateFlow<List<CompanionCharacter>> = combine(
        userRepo.currentLevel,
        userRepo.selectedCompanionId,
        userRepo.subscriptionTier
    ) { level, equippedId, _ ->
        buildCompanionsList(level, equippedId)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // Paywall
    private val _showPaywall = MutableStateFlow(false)
    val showPaywall: StateFlow<Boolean> = _showPaywall.asStateFlow()

    // Popup compagnon verrouillé
    private val _lockedCompanionPopup = MutableStateFlow<CompanionCharacter?>(null)
    val lockedCompanionPopup: StateFlow<CompanionCharacter?> = _lockedCompanionPopup.asStateFlow()

    // MARK: - Init

    init { load() }

    // MARK: - Chargement

    fun load() {
        viewModelScope.launch {
            _isLoading.value = true
            runCatching { userRepo.loadUserProfile() }
            _isLoading.value = false
        }
    }

    private fun buildCompanionsList(currentLevel: Int, equippedId: String): List<CompanionCharacter> {
        return CompanionData.all.map { companion ->
            val isUnlocked = if (userRepo.isPremium) currentLevel >= companion.unlockLevel
                             else companion.unlockLevel <= 5 && currentLevel >= companion.unlockLevel
            CompanionCharacter(
                id          = companion.id,
                name        = companion.name,
                drawableId  = companionDrawable(companion.id),
                unlockLevel = companion.unlockLevel,
                isUnlocked  = isUnlocked,
                isEquipped  = companion.id == equippedId
            )
        }
    }

    // MARK: - Équipement

    fun equipCompanion(companion: CompanionCharacter) {
        if (!companion.isUnlocked || companion.isEquipped) return

        // Mise à jour locale immédiate — déclenche la recomposition via selectedCompanionId
        userRepo.setSelectedCompanionId(companion.id)

        viewModelScope.launch {
            runCatching { userRepo.saveEquippedCompanion(companion.id) }
        }
    }

    // MARK: - Bonus notification

    suspend fun claimNotificationBonus(): Boolean {
        if (notificationBonusClaimed.value) return false
        val levelBefore = currentLevel.value
        runCatching { userRepo.claimNotificationBonus() }
        runCatching { userRepo.saveNotificationTime("09:00") }
        val levelAfter = currentLevel.value
        if (levelAfter > levelBefore) {
            checkNewlyUnlockedCompanions(levelBefore, levelAfter)
        }
        return levelAfter > levelBefore
    }

    // MARK: - Passage Premium

    fun handleUpgradeToPremium() {
        val newlyUnlocked = userRepo.unlockPremiumCompanions()
        userRepo.notifyPremiumCompanionUnlocks(newlyUnlocked)
    }

    // MARK: - Popup compagnons débloqués (délégué au UserRepository)

    fun checkNewlyUnlockedCompanions(oldLevel: Int, newLevel: Int) {
        userRepo.notifyCompanionUnlocksIfNeeded(oldLevel, newLevel)
    }

    // MARK: - Paywall

    fun showPaywall()    { _showPaywall.value = true  }
    fun dismissPaywall() { _showPaywall.value = false }

    // MARK: - Popup compagnon verrouillé

    fun onLockedCompanionTapped(companion: CompanionCharacter) {
        _lockedCompanionPopup.value = companion
    }

    fun dismissLockedPopup() {
        _lockedCompanionPopup.value = null
    }
}
