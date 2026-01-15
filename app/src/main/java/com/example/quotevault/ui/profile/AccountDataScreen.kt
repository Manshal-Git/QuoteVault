package com.example.quotevault.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.quotevault.R
import com.example.quotevault.ui.components.PrimaryButton
import com.example.quotevault.ui.components.Toolbar
import com.example.quotevault.ui.theme.TextTertiaryDark

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountDataScreen(
    onNavigateBack: () -> Unit,
    viewModel: AccountDataViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var showCurrentPassword by remember { mutableStateOf(false) }
    var showNewPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }
    
    Column {
        Toolbar(
            title = stringResource(R.string.heading_account_and_data),
            onNavigateBack = onNavigateBack
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            MaterialTheme.colorScheme.primaryContainer,
                            MaterialTheme.colorScheme.secondaryContainer,
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Manage your account settings and data",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                // Change Password Section
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Change Password",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        Text(
                            text = "Update your password to keep your account secure",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        // Current Password
                        Text(
                            text = "Current Password",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        OutlinedTextField(
                            value = state.currentPassword,
                            onValueChange = { 
                                viewModel.handleIntent(AccountDataIntent.CurrentPasswordChanged(it)) 
                            },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = {
                                Text(
                                    text = "Enter current password",
                                    color = TextTertiaryDark
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = TextTertiaryDark
                                )
                            },
                            trailingIcon = {
                                IconButton(onClick = { showCurrentPassword = !showCurrentPassword }) {
                                    Icon(
                                        imageVector = if (showCurrentPassword) 
                                            Icons.Default.Visibility 
                                        else 
                                            Icons.Default.VisibilityOff,
                                        contentDescription = if (showCurrentPassword) 
                                            "Hide password" 
                                        else 
                                            "Show password",
                                        tint = TextTertiaryDark
                                    )
                                }
                            },
                            visualTransformation = if (showCurrentPassword) 
                                VisualTransformation.None 
                            else 
                                PasswordVisualTransformation(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface
                            ),
                            shape = RoundedCornerShape(12.dp),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Next
                            ),
                            isError = state.currentPasswordError != null,
                            singleLine = true
                        )
                        
                        if (state.currentPasswordError != null) {
                            Text(
                                text = state.currentPasswordError!!,
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }
                        
                        // New Password
                        Text(
                            text = "New Password",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        OutlinedTextField(
                            value = state.newPassword,
                            onValueChange = { 
                                viewModel.handleIntent(AccountDataIntent.NewPasswordChanged(it)) 
                            },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = {
                                Text(
                                    text = "Enter new password",
                                    color = TextTertiaryDark
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = TextTertiaryDark
                                )
                            },
                            trailingIcon = {
                                IconButton(onClick = { showNewPassword = !showNewPassword }) {
                                    Icon(
                                        imageVector = if (showNewPassword) 
                                            Icons.Default.Visibility 
                                        else 
                                            Icons.Default.VisibilityOff,
                                        contentDescription = if (showNewPassword) 
                                            "Hide password" 
                                        else 
                                            "Show password",
                                        tint = TextTertiaryDark
                                    )
                                }
                            },
                            visualTransformation = if (showNewPassword) 
                                VisualTransformation.None 
                            else 
                                PasswordVisualTransformation(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface
                            ),
                            shape = RoundedCornerShape(12.dp),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Next
                            ),
                            isError = state.newPasswordError != null,
                            singleLine = true
                        )
                        
                        if (state.newPasswordError != null) {
                            Text(
                                text = state.newPasswordError!!,
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }
                        
                        // Confirm Password
                        Text(
                            text = "Confirm New Password",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        OutlinedTextField(
                            value = state.confirmPassword,
                            onValueChange = { 
                                viewModel.handleIntent(AccountDataIntent.ConfirmPasswordChanged(it)) 
                            },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = {
                                Text(
                                    text = "Confirm new password",
                                    color = TextTertiaryDark
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = TextTertiaryDark
                                )
                            },
                            trailingIcon = {
                                IconButton(onClick = { showConfirmPassword = !showConfirmPassword }) {
                                    Icon(
                                        imageVector = if (showConfirmPassword) 
                                            Icons.Default.Visibility 
                                        else 
                                            Icons.Default.VisibilityOff,
                                        contentDescription = if (showConfirmPassword) 
                                            "Hide password" 
                                        else 
                                            "Show password",
                                        tint = TextTertiaryDark
                                    )
                                }
                            },
                            visualTransformation = if (showConfirmPassword) 
                                VisualTransformation.None 
                            else 
                                PasswordVisualTransformation(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface
                            ),
                            shape = RoundedCornerShape(12.dp),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            isError = state.confirmPasswordError != null,
                            singleLine = true
                        )
                        
                        if (state.confirmPasswordError != null) {
                            Text(
                                text = state.confirmPasswordError!!,
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Change Password Button
                        PrimaryButton(
                            onClick = { viewModel.handleIntent(AccountDataIntent.ChangePassword) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            text = if (state.isLoading) "" else "Change Password",
                            enabled = state.currentPassword.isNotEmpty() && 
                                     state.newPassword.isNotEmpty() && 
                                     state.confirmPassword.isNotEmpty() && 
                                     !state.isLoading
                        )
                        
                        if (state.isLoading) {
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        
                        if (state.isPasswordChanged) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer
                                )
                            ) {
                                Text(
                                    text = "✓ Password changed successfully!",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }
                    }
                }
                
                // Delete Account Section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Danger Zone",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        
                        Text(
                            text = "Once you delete your account, there is no going back. Please be certain.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        OutlinedButton(
                            onClick = { viewModel.handleIntent(AccountDataIntent.DeleteAccount) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text("Delete Account")
                        }
                    }
                }
            }
            
            // Error Snackbar
            if (state.error != null) {
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    action = {
                        TextButton(onClick = { viewModel.handleIntent(AccountDataIntent.ClearError) }) {
                            Text("Dismiss")
                        }
                    }
                ) {
                    Text(state.error!!)
                }
            }
        }
    }
}
