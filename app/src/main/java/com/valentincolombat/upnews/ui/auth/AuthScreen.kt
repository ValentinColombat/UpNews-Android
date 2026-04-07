package com.valentincolombat.upnews.ui.auth

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.valentincolombat.upnews.R
import com.valentincolombat.upnews.service.GoogleSecrets
import com.valentincolombat.upnews.ui.theme.Divider
import com.valentincolombat.upnews.ui.theme.SurfaceCard
import com.valentincolombat.upnews.ui.theme.TextDisabled
import com.valentincolombat.upnews.ui.theme.UpNewsBackground
import com.valentincolombat.upnews.ui.theme.UpNewsGreen
import com.valentincolombat.upnews.ui.theme.UpNewsOrange
import com.valentincolombat.upnews.ui.theme.UpNewsRed
import com.valentincolombat.upnews.ui.theme.UpNewsTheme

// MARK: - AuthScreen

@Composable
fun AuthScreen(viewModel: AuthViewModel = viewModel()) {

    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()

    var isSignUpMode by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }

    // MARK: Google Sign-In (Credential Manager)
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }

    // MARK: - Layout (équivalent ScrollView > VStack iOS)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(UpNewsBackground)
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

            // Compagnon Mousse
            Image(
                painter = painterResource(id = R.drawable.mousse),
                contentDescription = "Mousse",
                modifier = Modifier.size(80.dp),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "UpNews",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Bienvenue !",
                fontSize = 14.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

            // MARK: - Toggle Connexion / Inscription
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceCard, RoundedCornerShape(12.dp))
                    .padding(4.dp)
            ) {
                listOf("Connexion" to false, "Inscription" to true).forEach { (label, isSignUp) ->
                    Button(
                        onClick = {
                            isSignUpMode = isSignUp
                            viewModel.clearError()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSignUpMode == isSignUp) Color.White else Color.Transparent,
                            contentColor = Color.Black
                        ),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = if (isSignUpMode == isSignUp) 2.dp else 0.dp
                        )
                    ) {
                        Text(label, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // MARK: - Champs Email / Mot de passe
            AuthTextField(
                value = email,
                onValueChange = { email = it },
                placeholder = "Email",
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = Color.Gray) },
                keyboardType = KeyboardType.Email
            )

            Spacer(modifier = Modifier.height(8.dp))

            AuthPasswordField(
                value = password,
                onValueChange = { password = it },
                showPassword = showPassword,
                onToggleVisibility = { showPassword = !showPassword }
            )

            // MARK: - Indicateur force mot de passe (inscription uniquement)
            if (isSignUpMode && password.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                PasswordStrengthIndicator(password = password)
            }

            // MARK: - Message d'erreur
            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = errorMessage!!,
                    color = Color.Gray,
                    fontSize = 13.sp,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // MARK: - Bouton principal
            Button(
                onClick = {
                    if (isSignUpMode) viewModel.signUp(email, password)
                    else viewModel.signIn(email, password)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = UpNewsGreen,
                    disabledContainerColor = TextDisabled,
                    disabledContentColor = Color.White
                ),
                enabled = !isLoading && email.isNotBlank() && password.isNotBlank()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = if (isSignUpMode) "Créer mon compte" else "Se connecter",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // MARK: - Séparateur
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.weight(1f).height(1.dp).background(Divider))
                Text(
                    text = "  ou  ",
                    color = Color.Gray,
                    fontSize = 13.sp
                )
                Box(modifier = Modifier.weight(1f).height(1.dp).background(Divider))
            }

            Spacer(modifier = Modifier.height(12.dp))

            // MARK: - Google Sign-In
            GoogleSignInButton(
                onClick = {
                    scope.launch {
                        try {
                            kotlinx.coroutines.delay(250)
                            Log.d("GoogleAuth", "Lancement Credential Manager (GetGoogleIdOption)...")
                            val googleIdOption = GetGoogleIdOption.Builder()
                                .setFilterByAuthorizedAccounts(false)
                                .setServerClientId(GoogleSecrets.SERVER_CLIENT_ID)
                                .build()
                            val result = credentialManager.getCredential(
                                context,
                                GetCredentialRequest.Builder()
                                    .addCredentialOption(googleIdOption)
                                    .build()
                            )
                            val credential = GoogleIdTokenCredential.createFrom(result.credential.data)
                            Log.d("GoogleAuth", "idToken obtenu : ${credential.idToken.take(20)}...")
                            viewModel.signInWithGoogle(credential.idToken)
                        } catch (e: GetCredentialException) {
                            Log.w("GoogleAuth", "GetGoogleIdOption échoué (${e::class.simpleName}), fallback GetSignInWithGoogleOption...")
                            try {
                                val signInOption = GetSignInWithGoogleOption.Builder(GoogleSecrets.SERVER_CLIENT_ID).build()
                                val result = credentialManager.getCredential(
                                    context,
                                    GetCredentialRequest.Builder()
                                        .addCredentialOption(signInOption)
                                        .build()
                                )
                                val credential = GoogleIdTokenCredential.createFrom(result.credential.data)
                                Log.d("GoogleAuth", "idToken obtenu via fallback : ${credential.idToken.take(20)}...")
                                viewModel.signInWithGoogle(credential.idToken)
                            } catch (e2: Exception) {
                                Log.e("GoogleAuth", "Fallback échoué: ${e2::class.simpleName} — ${e2.message}")
                                viewModel.setGoogleError("Connexion Google impossible. Réessaie.")
                            }
                        } catch (e: Exception) {
                            Log.e("GoogleAuth", "Exception: ${e::class.simpleName} — ${e.message}")
                        }
                    }
                },
                enabled = !isLoading
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "\"Les matins stressants, c'est terminé !\nGardez votre énergie.\"",
                fontSize = 11.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
    }
}

// MARK: - PasswordStrengthIndicator

@Composable
fun PasswordStrengthIndicator(password: String) {
    val hasLength = password.length >= 8
    val hasDigit = password.any { it.isDigit() }
    val hasLetter = password.any { it.isLetter() }
    val hasSpecial = password.any { !it.isLetterOrDigit() }

    val score = listOf(hasLength, hasDigit || hasLetter, hasSpecial).count { it }

    val segmentColor = when (score) {
        1 -> UpNewsRed
        2 -> UpNewsOrange
        else -> UpNewsGreen
    }

    val label = when (score) {
        1 -> "Faible"
        2 -> "Moyen"
        else -> "Fort"
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            repeat(3) { index ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp)
                        .background(
                            color = if (index < score) segmentColor else Divider,
                            shape = RoundedCornerShape(2.dp)
                        )
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            color = segmentColor
        )
    }
}

// MARK: - AuthTextField

@Composable
fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: @Composable (() -> Unit)? = null,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text(placeholder, color = Color.Gray) },
        leadingIcon = leadingIcon,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = UpNewsGreen,
            unfocusedBorderColor = Divider,
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White
        ),
        singleLine = true
    )
}

// MARK: - AuthPasswordField

@Composable
fun AuthPasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    showPassword: Boolean,
    onToggleVisibility: () -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text("Mot de passe", color = Color.Gray) },
        leadingIcon = {
            Icon(Icons.Default.Lock, contentDescription = null, tint = Color.Gray)
        },
        trailingIcon = {
            IconButton(onClick = onToggleVisibility) {
                Icon(
                    imageVector = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                    contentDescription = if (showPassword) "Masquer" else "Afficher",
                    tint = Color.Gray
                )
            }
        },
        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = UpNewsGreen,
            unfocusedBorderColor = Divider,
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White
        ),
        singleLine = true
    )
}

// MARK: - GoogleSignInButton

@Composable
fun GoogleSignInButton(onClick: () -> Unit, enabled: Boolean = true) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.White,
            contentColor = Color.Black
        ),
        border = BorderStroke(1.dp, Divider),
        enabled = enabled
    ) {
        Text(
            text = "Continuer avec Google",
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

// MARK: - Preview

@Preview(showBackground = true)
@Composable
private fun AuthScreenPreview() {
    UpNewsTheme(darkTheme = false) {
        AuthScreen()
    }
}
