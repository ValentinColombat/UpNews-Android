package com.valentincolombat.upnews.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.automirrored.rounded.LibraryBooks
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Pets
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.valentincolombat.upnews.R
import com.valentincolombat.upnews.data.repository.UserRepository
import com.valentincolombat.upnews.ui.companions.CompanionsScreen
import com.valentincolombat.upnews.ui.companions.UnlockedCompanionInfo
import com.valentincolombat.upnews.ui.companions.UnlockCompanionOverlay
import com.valentincolombat.upnews.ui.components.ComposeConfettiOverlay
import com.valentincolombat.upnews.ui.components.companionDrawable
import com.valentincolombat.upnews.ui.home.HomeScreen
import com.valentincolombat.upnews.ui.library.LibraryScreen
import com.valentincolombat.upnews.ui.profile.ProfileScreen
import com.valentincolombat.upnews.ui.theme.UpNewsBlueMid
import com.valentincolombat.upnews.ui.theme.UpNewsOrange


// Équivalent MainTabView.swift
@Composable
fun MainTabView() {
    var selectedTab by remember { mutableIntStateOf(0) }
    var homeArticleOpen by remember { mutableStateOf(false) }
    var homeResetKey by remember { mutableIntStateOf(0) }

    // Compagnons débloqués — collecté globalement depuis UserRepository
    var unlockCompanions by remember { mutableStateOf<List<UnlockedCompanionInfo>>(emptyList()) }
    LaunchedEffect(Unit) {
        UserRepository.shared.companionUnlockEvent.collect { triples ->
            val infos = triples.map { (name, id, level) ->
                UnlockedCompanionInfo(name, companionDrawable(id), level)
            }
            if (infos.isNotEmpty()) unlockCompanions = infos
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            bottomBar = {
                NavigationBar(
                    containerColor = Color.White,
                    modifier = Modifier.shadow(
                        elevation = 12.dp,
                        spotColor = Color.Black.copy(alpha = 0.12f),
                        ambientColor = Color.Black.copy(alpha = 0.05f)
                    )
                ) {
                    tabs.forEachIndexed { index, tab ->
                        NavigationBarItem(
                            selected = selectedTab == index && !(index == 0 && homeArticleOpen),
                            onClick = {
                                if (index == 0 && selectedTab == 0 && homeArticleOpen) {
                                    homeResetKey++
                                } else {
                                    selectedTab = index
                                }
                            },
                            icon = {
                                Icon(imageVector = tab.icon, contentDescription = tab.label, modifier = Modifier.size(28.dp))
                            },
                            label = null,
                            alwaysShowLabel = false,
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = UpNewsBlueMid,
                                selectedTextColor = UpNewsBlueMid,
                                unselectedIconColor = Color.Gray,
                                unselectedTextColor = Color.Gray,
                                indicatorColor = Color.White
                            )
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = innerPadding.calculateBottomPadding())
            ) {
                when (selectedTab) {
                    0 -> HomeScreen(
                        onNavigateToProfile = { selectedTab = 3 },
                        onNavigateToCompanions = { selectedTab = 1 },
                        resetKey = homeResetKey,
                        onArticleOpenChanged = { homeArticleOpen = it }
                    )
                    1 -> CompanionsScreen()
                    2 -> LibraryScreen(onGoHome = { selectedTab = 0 })
                    3 -> ProfileScreen()
                }
            }
        }

        // Overlay global débloquage compagnon
        if (unlockCompanions.isNotEmpty()) {
            UnlockCompanionOverlay(
                companions             = unlockCompanions,
                onDismiss              = { unlockCompanions = emptyList() },
                onNavigateToCompanions = { unlockCompanions = emptyList(); selectedTab = 1 }
            )
            // Canvas pur Compose — aucun AndroidView, z-ordering garanti au premier plan
            ComposeConfettiOverlay()
        }
    }
}

// Définition des onglets — équivalent des .tabItem { Label(...) }
private data class Tab(val label: String, val icon: ImageVector)

private val tabs = listOf(
    Tab("Home", Icons.Rounded.Home),
    Tab("Compagnons", Icons.Rounded.Pets),
    Tab("Bibliothèque", Icons.AutoMirrored.Rounded.LibraryBooks),
    Tab("Profil", Icons.Rounded.Person)
)
