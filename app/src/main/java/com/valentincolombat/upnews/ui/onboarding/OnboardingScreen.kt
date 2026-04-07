package com.valentincolombat.upnews.ui.onboarding

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.valentincolombat.upnews.R
import com.valentincolombat.upnews.ui.theme.OrangeStrong
import com.valentincolombat.upnews.ui.theme.UpNewsBackground
import com.valentincolombat.upnews.data.model.OnboardingPage
import com.valentincolombat.upnews.ui.theme.UpNewsTheme
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(viewModel: OnboardingViewModel = viewModel()) {
    val pagerState = rememberPagerState(pageCount = { OnboardingPage.pages.size })
    val scope = rememberCoroutineScope()
    val isLastPage = pagerState.currentPage == OnboardingPage.pages.size - 1

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(UpNewsBackground),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { index ->
            OnboardingPageContent(page = OnboardingPage.pages[index])
        }

        // Indicateurs de page — équivalent HStack de Capsule iOS
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 16.dp)
        ) {
            OnboardingPage.pages.forEachIndexed { index, _ ->
                val width by animateDpAsState(
                    targetValue = if (index == pagerState.currentPage) 24.dp else 8.dp,
                    animationSpec = tween(durationMillis = 200),
                    label = "indicator_width_$index"
                )
                Box(
                    modifier = Modifier
                        .height(8.dp)
                        .width(width)
                        .clip(RoundedCornerShape(50))
                        .background(
                            if (index == pagerState.currentPage) Color.Black
                            else Color.Gray.copy(alpha = 0.3f)
                        )
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Bouton CTA — "NEXT" → "GO" sur la dernière page
        val buttonWidth by animateDpAsState(
            targetValue = if (isLastPage) 120.dp else 90.dp,
            animationSpec = tween(durationMillis = 200),
            label = "button_width"
        )
        Button(
            onClick = {
                if (isLastPage) {
                    viewModel.completeOnboarding()
                } else {
                    scope.launch {
                        pagerState.animateScrollToPage(pagerState.currentPage + 1)
                    }
                }
            },
            modifier = Modifier
                .width(buttonWidth)
                .height(44.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isLastPage) OrangeStrong else Color.Black
            )
        ) {
            Text(
                text = if (isLastPage) "GO" else "NEXT",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.navigationBarsPadding().height(24.dp))
    }
}

// MARK: - Page View

@Composable
private fun OnboardingPageContent(page: OnboardingPage) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.height(280.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = imageResForName(page.imageName)),
                contentDescription = page.title,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        shadowElevation = 50.dp.toPx()
                        shape = CircleShape
                        clip = false
                    }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = page.title,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = page.description,
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            color = Color.Gray
        )
    }
}

/**
 * Résout le nom d'image iOS vers l'ID de ressource Android drawable.
 * Placer les images dans res/drawable/ avec le même nom (ex: mousse.png).
 */
private fun imageResForName(name: String): Int = when (name) {
    "mousse" -> R.drawable.mousse
    "givreetplume" -> R.drawable.givreetplume
    "mochi" -> R.drawable.mochi
    "cannelle" -> R.drawable.cannelle
    else -> android.R.drawable.ic_menu_gallery
}

@Preview
@Composable
private fun OnboardingScreenPreview() {
    UpNewsTheme(darkTheme = false) {
        OnboardingScreen()
    }
}
