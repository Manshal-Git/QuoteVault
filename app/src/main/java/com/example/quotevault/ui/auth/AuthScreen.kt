package com.example.quotevault.ui.auth

import android.R.attr.enabled
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.quotevault.ui.components.PrimaryButton
import com.example.quotevault.ui.theme.TextSecondaryDark
import com.example.quotevault.ui.theme.TextTertiaryDark

@Composable
fun AuthScreen(
    viewModel: AuthViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {},
    onAuthSuccess: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    
    LaunchedEffect(state.isSignedIn) {
        if (state.isSignedIn) {
            onAuthSuccess()
        }
    }

    val animatedBackgroundColor1 by animateColorAsState(
        targetValue = if (state.isSignInMode) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.secondaryContainer
        },
        animationSpec = tween(durationMillis = 400)
    )

    val animatedBackgroundColor2 by animateColorAsState(
        targetValue = if (state.isSignInMode) {
            MaterialTheme.colorScheme.secondaryContainer
        } else {
            MaterialTheme.colorScheme.primaryContainer
        },
        animationSpec = tween(durationMillis = 400)
    )
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        animatedBackgroundColor1,
                        animatedBackgroundColor2
                    )
                )
            )
            .statusBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            Spacer(Modifier.height(132.dp))
            // Title
            Text(
                text = "Inspiration Awaits",
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            
            Text(
                text = "Sign ${if (state.isSignInMode) "In" else "Up"} to access your daily wisdom\nand curated quotes.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondaryDark,
                modifier = Modifier.padding(top = 8.dp)
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Email Field
            Text(
                text = "Email Address",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onBackground
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = state.email,
                onValueChange = { viewModel.handleIntent(AuthIntent.EmailChanged(it)) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        text = "Enter your email",
                        color = TextTertiaryDark
                    )
                },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = null,
                        tint = TextTertiaryDark
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
                    focusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                isError = state.emailError != null,
                singleLine = true
            )
            
            if (state.emailError != null) {
                Text(
                    text = state.emailError!!,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Password Field
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Password",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = state.password,
                onValueChange = { viewModel.handleIntent(AuthIntent.PasswordChanged(it)) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        text = "Enter password",
                        color = TextTertiaryDark
                    )
                },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = TextTertiaryDark
                    )
                },
                visualTransformation = PasswordVisualTransformation(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
                    focusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                isError = state.passwordError != null,
                singleLine = true
            )
            
            if (state.passwordError != null) {
                Text(
                    text = state.passwordError!!,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                )
            }

            AnimatedVisibility(
                visible = state.isSignInMode,
                modifier = Modifier
                    .align(Alignment.End)
            ) {
                TextButton(
                    onClick = { viewModel.handleIntent(AuthIntent.ForgotPasswordClicked) }
                ) {
                    Text(
                        text = "Forgot password?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Sign In Button
            Box {
                val primaryButtonText = if (!state.isLoading) {
                    if (state.isSignInMode) "Sign In" else "Sign Up"
                } else {
                    ""
                }
                PrimaryButton(
                    onClick = { viewModel.handleIntent(AuthIntent.CallToActionClicked) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    text = primaryButtonText,
                    enabled = state.email.isNotEmpty() && state.password.isNotEmpty(),
                )
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(24.dp)
                            .align(Alignment.Center),
                        color = Color.White
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Divider
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.outline
                )
                Text(
                    text = "OR CONTINUE WITH",
                    style = MaterialTheme.typography.labelLarge,
                    color = TextTertiaryDark,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.outline
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Social Sign In Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    onClick = { viewModel.handleIntent(AuthIntent.GoogleSignInClicked) },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = null,
                    shape = RoundedCornerShape(12.dp),
                    enabled = !state.isLoading
                ) {
                    Text(
                        text = "G  Google",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                OutlinedButton(
                    onClick = { viewModel.handleIntent(AuthIntent.AppleSignInClicked) },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = null,
                    shape = RoundedCornerShape(12.dp),
                    enabled = !state.isLoading
                ) {
                    Text(
                        text = "  Apple",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Sign Up Link
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (state.isSignInMode) {
                    Text(
                        text = "Don't have an account?  ",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondaryDark
                    )
                    TextButton(
                        onClick = { viewModel.handleIntent(AuthIntent.SignUpOptionClicked) },
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            text = "Create one",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                } else {
                    Text(
                        text = "Already have an account?  ",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondaryDark
                    )
                    TextButton(
                        onClick = { viewModel.handleIntent(AuthIntent.SignInOptionClicked) },
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            text = "Sign in",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        // Error SnackBar
        if (state.error != null) {
            Snackbar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                action = {
                    TextButton(onClick = { viewModel.handleIntent(AuthIntent.ClearError) }) {
                        Text("Dismiss")
                    }
                }
            ) {
                Text(state.error!!)
            }
        }
    }
}
