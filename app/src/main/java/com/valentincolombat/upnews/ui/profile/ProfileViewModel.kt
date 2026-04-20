package com.valentincolombat.upnews.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.valentincolombat.upnews.data.model.SubscriptionTier
import com.valentincolombat.upnews.data.remote.SupabaseClient
import com.valentincolombat.upnews.data.repository.AuthRepository
import com.valentincolombat.upnews.data.repository.UserRepository
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ProfileUiState(
    val isLoading: Boolean = true,
    val displayName: String = "",
    val userEmail: String = "",
    val companionId: String? = null,
    val currentStreak: Int = 0,
    val currentXp: Int = 0,
    val maxXp: Int = 100,
    val articlesReadToday: Int = 0,
    val articlesReadThisMonth: Int = 0,
    val notificationTime: String = "9:00",
    val notificationTimeFromServer: String? = null,
    val isPremium: Boolean = false,
    val showLogoutDialog: Boolean = false,
    val showDeleteDialog: Boolean = false,
    val isDeleting: Boolean = false,
    val deleteError: String? = null,
    val showCategoryPicker: Boolean = false,
    val showDeleteInfo: Boolean = false,
    val showSubscription: Boolean = false,
    val showPremiumInfo: Boolean = false,
)

class ProfileViewModel : ViewModel() {

    private val userRepo = UserRepository.shared
    private val authRepo = AuthRepository.shared
    private val client   = SupabaseClient.client

    // MARK: - Données utilisateur (depuis UserRepository)

    val displayName         = userRepo.displayName
    val selectedCompanionId = userRepo.selectedCompanionId
    val currentStreak       = userRepo.currentStreak
    val currentXp           = userRepo.currentXp
    val maxXp               = userRepo.maxXp
    val articlesReadToday   = userRepo.articlesReadToday
    val articlesReadThisMonth = userRepo.articlesReadThisMonth
    val preferredCategories = userRepo.preferredCategories
    val isPremium           = userRepo.subscriptionTier
        .map { it == SubscriptionTier.PREMIUM }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), userRepo.isPremium)

    // MARK: - État local de la vue

    private val _userEmail      = MutableStateFlow("")
    val userEmail: StateFlow<String> = _userEmail.asStateFlow()

    private val _isLoading      = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _notificationTime = MutableStateFlow("9:00")
    val notificationTime: StateFlow<String> = _notificationTime.asStateFlow()

    // MARK: - État des dialogs

    private val _showLogoutDialog   = MutableStateFlow(false)
    val showLogoutDialog: StateFlow<Boolean> = _showLogoutDialog.asStateFlow()

    private val _showDeleteDialog   = MutableStateFlow(false)
    val showDeleteDialog: StateFlow<Boolean> = _showDeleteDialog.asStateFlow()

    private val _isDeleting         = MutableStateFlow(false)
    val isDeleting: StateFlow<Boolean> = _isDeleting.asStateFlow()

    private val _deleteError        = MutableStateFlow<String?>(null)
    val deleteError: StateFlow<String?> = _deleteError.asStateFlow()

    private val _showCategoryPicker = MutableStateFlow(false)
    val showCategoryPicker: StateFlow<Boolean> = _showCategoryPicker.asStateFlow()

    private val _showDeleteInfo     = MutableStateFlow(false)
    val showDeleteInfo: StateFlow<Boolean> = _showDeleteInfo.asStateFlow()

    private val _showSubscription   = MutableStateFlow(false)
    val showSubscription: StateFlow<Boolean> = _showSubscription.asStateFlow()

    private val _showPremiumInfo    = MutableStateFlow(false)
    val showPremiumInfo: StateFlow<Boolean> = _showPremiumInfo.asStateFlow()

    // MARK: - Notification (déclaré avant uiState pour l'initialisation)

    /** Heure chargée depuis Supabase (null = jamais configurée). Utilisé pour la migration. */
    val notificationTimeFromServer: StateFlow<String?> = userRepo.notificationTime

    // MARK: - État agrégé UI

    @Suppress("UNCHECKED_CAST")
    val uiState: StateFlow<ProfileUiState> = combine(
        _isLoading, displayName, _userEmail, selectedCompanionId, currentStreak,
        currentXp, maxXp, articlesReadToday, articlesReadThisMonth, isPremium,
        _notificationTime, notificationTimeFromServer,
        _showLogoutDialog, _showDeleteDialog, _isDeleting,
        _deleteError, _showCategoryPicker, _showDeleteInfo, _showSubscription, _showPremiumInfo
    ) { v ->
        ProfileUiState(
            isLoading                  = v[0]  as Boolean,
            displayName                = v[1]  as String,
            userEmail                  = v[2]  as String,
            companionId                = v[3]  as String?,
            currentStreak              = v[4]  as Int,
            currentXp                  = v[5]  as Int,
            maxXp                      = v[6]  as Int,
            articlesReadToday          = v[7]  as Int,
            articlesReadThisMonth      = v[8]  as Int,
            isPremium                  = v[9]  as Boolean,
            notificationTime           = v[10] as String,
            notificationTimeFromServer = v[11] as String?,
            showLogoutDialog           = v[12] as Boolean,
            showDeleteDialog           = v[13] as Boolean,
            isDeleting                 = v[14] as Boolean,
            deleteError                = v[15] as String?,
            showCategoryPicker         = v[16] as Boolean,
            showDeleteInfo             = v[17] as Boolean,
            showSubscription           = v[18] as Boolean,
            showPremiumInfo            = v[19] as Boolean,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProfileUiState())

    // MARK: - Init

    init { loadProfileData() }

    // MARK: - Chargement

    fun loadProfileData() {
        viewModelScope.launch {
            _isLoading.value = true
            runCatching {
                val session = client.auth.currentSessionOrNull()
                _userEmail.value = session?.user?.email ?: ""
                userRepo.loadAllData()
                userRepo.notificationTime.value?.let { _notificationTime.value = it }
            }
            _isLoading.value = false
        }
    }

    // MARK: - Préférences catégories

    val categoriesPreviewText: String
        get() {
            val cats = userRepo.preferredCategories.value
            return when {
                cats.isEmpty() -> "Aucune"
                cats.size == 1 -> cats.first().replaceFirstChar { it.uppercase() }
                cats.size >= 4 -> "Toutes"
                else           -> "${cats.size} catégories"
            }
        }

    fun saveCategories(categories: List<String>) {
        viewModelScope.launch {
            runCatching { userRepo.updatePreferredCategories(categories) }
            _showCategoryPicker.value = false
        }
    }

    // MARK: - Notification

    fun saveNotificationTime(time: String) {
        viewModelScope.launch {
            _notificationTime.value = time
            runCatching { userRepo.saveNotificationTime(time) }
        }
    }

    // MARK: - Déconnexion

    fun showLogoutDialog()  { _showLogoutDialog.value = true  }
    fun hideLogoutDialog()  { _showLogoutDialog.value = false }

    fun logout() {
        viewModelScope.launch {
            runCatching { authRepo.signOut() }
            _showLogoutDialog.value = false
        }
    }

    // MARK: - Suppression de compte

    fun showDeleteDialog()  { _showDeleteDialog.value = true  }
    fun hideDeleteDialog()  { _showDeleteDialog.value = false }
    fun showDeleteInfo()    { _showDeleteInfo.value = true    }
    fun hideDeleteInfo()    { _showDeleteInfo.value = false   }
    fun clearDeleteError()  { _deleteError.value = null       }

    fun deleteAccount() {
        viewModelScope.launch {
            _isDeleting.value  = true
            _deleteError.value = null
            runCatching {
                val session = client.auth.currentSessionOrNull()
                    ?: throw Exception("Non connecté")
                val userId  = session.user?.id?.toString()
                    ?: throw Exception("ID manquant")
                client.from("users").delete { filter { eq("id", userId) } }
                userRepo.reset()
                authRepo.signOut()
            }.onFailure {
                _deleteError.value = "Impossible de supprimer le compte. Vérifie ta connexion et réessaie."
                _isDeleting.value  = false
            }
        }
    }

    // MARK: - Premium

    fun onPremiumBadgeTap() {
        if (isPremium.value) _showPremiumInfo.value = true
        else _showSubscription.value = true
    }

    fun hidePremiumInfo()    { _showPremiumInfo.value  = false }
    fun hideSubscription()   { _showSubscription.value = false }

    // MARK: - Pickers

    fun openCategoryPicker()  { _showCategoryPicker.value = true  }
    fun closeCategoryPicker() { _showCategoryPicker.value = false }

}
