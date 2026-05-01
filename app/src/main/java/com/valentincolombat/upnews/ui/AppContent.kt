package com.valentincolombat.upnews.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.HourglassTop
import androidx.compose.material.icons.rounded.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.valentincolombat.upnews.service.AppStateService
import com.valentincolombat.upnews.ui.auth.AuthScreen
import com.valentincolombat.upnews.ui.category.CategorySelectionScreen
import com.valentincolombat.upnews.ui.components.LoadingView
import com.valentincolombat.upnews.ui.navigation.MainTabView
import com.valentincolombat.upnews.ui.onboarding.CompanionSelectionScreen
import com.valentincolombat.upnews.ui.onboarding.OnboardingScreen
import com.valentincolombat.upnews.ui.theme.UpNewsBackground
import com.valentincolombat.upnews.ui.theme.UpNewsGreen

@Composable
fun AppContent(viewModel: AppViewModel = viewModel()) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val errorReason by viewModel.errorReason.collectAsStateWithLifecycle()

    when (currentScreen) {
        AppStateService.AppScreen.LOADING           -> LoadingView()
        AppStateService.AppScreen.ONBOARDING        -> OnboardingScreen()
        AppStateService.AppScreen.AUTH              -> AuthScreen()
        AppStateService.AppScreen.COMPANION_SELECTION -> CompanionSelectionScreen()
        AppStateService.AppScreen.CATEGORY_SELECTION  -> CategorySelectionScreen()
        AppStateService.AppScreen.MAIN              -> MainTabView()
        AppStateService.AppScreen.ERROR             -> StartupErrorView(
            reason = errorReason,
            onRetry = { viewModel.retry() }
        )
    }
}

private data class ErrorContent(
    val icon: ImageVector,
    val title: String,
    val subtitle: String,
)

private fun errorContent(reason: AppStateService.ErrorReason?): ErrorContent = when (reason) {
    AppStateService.ErrorReason.NETWORK -> ErrorContent(
        icon = Icons.Rounded.WifiOff,
        title = "Pas de connexion",
        subtitle = "Vérifie ta connexion internet et réessaie."
    )
    AppStateService.ErrorReason.TIMEOUT -> ErrorContent(
        icon = Icons.Rounded.HourglassTop,
        title = "Chargement trop long",
        subtitle = "Le serveur met trop de temps à répondre. Réessaie dans quelques instants."
    )
    AppStateService.ErrorReason.SERVER, null -> ErrorContent(
        icon = Icons.Rounded.CloudOff,
        title = "Une erreur est survenue",
        subtitle = "Un problème nous a empêché de charger tes données. Réessaie dans quelques instants."
    )
}

@Composable
private fun StartupErrorView(reason: AppStateService.ErrorReason?, onRetry: () -> Unit) {
    val content = errorContent(reason)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(UpNewsBackground),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(horizontal = 40.dp)
        ) {
            Icon(
                imageVector = content.icon,
                contentDescription = null,
                tint = Color.Gray.copy(alpha = 0.5f),
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = content.title,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black
            )
            Text(
                text = content.subtitle,
                fontSize = 14.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = onRetry,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = UpNewsGreen)
            ) {
                Text("Réessayer", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
            }
        }
    }
}
