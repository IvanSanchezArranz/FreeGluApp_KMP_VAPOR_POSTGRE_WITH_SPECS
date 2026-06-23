package com.ivan.freeglukmp.presentation.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.ivan.freeglukmp.data.local.LocalFavoritesDataSource
import com.ivan.freeglukmp.domain.repository.AuthRepository
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onNavigateToRegister: () -> Unit,
    onLoginSuccess: () -> Unit,
    onSkipLogin: () -> Unit
) {
    val authRepository: AuthRepository = koinInject()
    val localFavoritesDataSource = remember { LocalFavoritesDataSource() }
    val scope = rememberCoroutineScope()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    // Merge favorites modal state
    var showMergeDialog by remember { mutableStateOf(false) }
    val localFavorites = localFavoritesDataSource.getAllFavorites()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "FreeGluApp",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Text(
                text = "Inicia sesión para sincronizar tus alimentos sin gluten favoritos",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Correo Electrónico") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary
                )
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Contraseña") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary
                )
            )

            if (errorMessage != null) {
                Text(
                    text = errorMessage ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            Button(
                onClick = {
                    if (email.isEmpty() || password.isEmpty()) {
                        errorMessage = "Por favor, rellena todos los campos."
                        return@Button
                    }
                    isLoading = true
                    errorMessage = null
                    scope.launch {
                        authRepository.login(email, password)
                            .onSuccess {
                                isLoading = false
                                if (localFavorites.isNotEmpty()) {
                                    showMergeDialog = true
                                } else {
                                    onLoginSuccess()
                                }
                            }
                            .onFailure {
                                isLoading = false
                                errorMessage = it.message ?: "Error al iniciar sesión. Comprueba tus datos."
                            }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Iniciar Sesión")
                }
            }

            TextButton(
                onClick = onNavigateToRegister,
                enabled = !isLoading
            ) {
                Text("¿No tienes cuenta? Regístrate aquí")
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            OutlinedButton(
                onClick = onSkipLogin,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                Text("Entrar como Invitado")
            }
        }
    }

    // Explicit Favorites Merge Dialog (User Story 4)
    if (showMergeDialog) {
        AlertDialog(
            onDismissRequest = {
                showMergeDialog = false
                onLoginSuccess()
            },
            title = { Text("Fusionar Favoritos") },
            text = {
                Text("Hemos detectado que tienes ${localFavorites.size} favoritos guardados de forma local. ¿Deseas fusionarlos con tu nueva cuenta en la nube para no perderlos?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            authRepository.syncFavorites(localFavorites)
                                .onSuccess {
                                    // Successfully synced, clear local favorites atomically to prevent duplicate logic and race conditions
                                    localFavoritesDataSource.clearAll()
                                    showMergeDialog = false
                                    onLoginSuccess()
                                }
                                .onFailure {
                                    errorMessage = "Error al fusionar: ${it.message}"
                                    showMergeDialog = false
                                    onLoginSuccess()
                                }
                        }
                    }
                ) {
                    Text("Sí, Fusionar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showMergeDialog = false
                        onLoginSuccess()
                    }
                ) {
                    Text("No, mantener separados")
                }
            }
        )
    }
}
