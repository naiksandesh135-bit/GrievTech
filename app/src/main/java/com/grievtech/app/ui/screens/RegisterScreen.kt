package com.grievtech.app.ui.screens

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import com.grievtech.app.R
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grievtech.app.ui.auth.AuthUiEvent
import com.grievtech.app.ui.auth.AuthViewModel
import com.grievtech.app.ui.components.ErrorBanner
import com.grievtech.app.ui.components.GrievButton
import com.grievtech.app.ui.components.GrievButtonType
import com.grievtech.app.ui.components.GrievTextField
import com.grievtech.app.ui.theme.Elevation
import com.grievtech.app.ui.theme.GrievTechTheme
import com.grievtech.app.ui.theme.PrimaryPurple
import com.grievtech.app.ui.theme.SecondaryBlue
import com.grievtech.app.ui.theme.Shapes
import com.grievtech.app.ui.theme.Spacing
import com.grievtech.app.util.ValidationUtils

@Composable
fun RegisterScreen(
    viewModel: AuthViewModel,
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.registerState.collectAsState()
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(key1 = true) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is AuthUiEvent.RegisterSuccess -> onRegisterSuccess()
                else -> {}
            }
        }
    }

    RegisterScreenContent(
        state = state,
        onFullNameChange = viewModel::onRegisterFullNameChanged,
        onEmailChange = viewModel::onRegisterEmailChanged,
        onPhoneChange = viewModel::onRegisterPhoneChanged,
        onPasswordChange = viewModel::onRegisterPasswordChanged,
        onConfirmPasswordChange = viewModel::onRegisterConfirmPasswordChanged,
        onTogglePasswordVisibility = viewModel::toggleRegisterPasswordVisibility,
        onToggleConfirmPasswordVisibility = viewModel::toggleRegisterConfirmPasswordVisibility,
        onRegisterClick = {
            keyboardController?.hide()
            viewModel.register()
        },
        onNavigateToLogin = onNavigateToLogin,
        modifier = modifier
    )
}

@Composable
fun RegisterScreenContent(
    state: com.grievtech.app.ui.auth.RegisterUiState,
    onFullNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onTogglePasswordVisibility: () -> Unit,
    onToggleConfirmPasswordVisibility: () -> Unit,
    onRegisterClick: () -> Unit,
    onNavigateToLogin: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Scaffold(modifier = modifier) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(PrimaryPurple, SecondaryBlue)
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .imePadding()
                    .padding(Spacing.lg),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Surface(
                    modifier = Modifier
                        .size(160.dp)
                        .clip(CircleShape),
                    color = Color.Black,
                    shadowElevation = 8.dp
                ) {
                    Image(
                        painter = painterResource(R.drawable.ic_logo_full),
                        contentDescription = "GrievTech logo",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.height(Spacing.xs))

                Text(
                    text = "GrievTech",
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 32.sp
                    ),
                    color = MaterialTheme.colorScheme.onPrimary
                )

                Text(
                    text = "Join Citizen Network",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
                )

                Spacer(modifier = Modifier.height(Spacing.lg))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { contentDescription = "Registration Form" },
                    shape = Shapes.medium,
                    elevation = CardDefaults.cardElevation(defaultElevation = Elevation.medium),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(Spacing.lg),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Create Citizen Account",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(Spacing.md))

                        if (state.errorMessage != null) {
                            ErrorBanner(
                                message = state.errorMessage,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(Spacing.md))
                        }

                        GrievTextField(
                            value = state.fullName,
                            onValueChange = onFullNameChange,
                            label = "Full Name",
                            placeholder = "John Doe",
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = "Person Icon") },
                            errorText = state.fullNameError,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text,
                                imeAction = ImeAction.Next
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .semantics { contentDescription = "Full Name Input" }
                        )

                        Spacer(modifier = Modifier.height(Spacing.md))

                        GrievTextField(
                            value = state.email,
                            onValueChange = onEmailChange,
                            label = "Email Address",
                            placeholder = "john@example.com",
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Email Icon") },
                            errorText = state.emailError,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .semantics { contentDescription = "Email Address Input" }
                        )

                        Spacer(modifier = Modifier.height(Spacing.md))

                        GrievTextField(
                            value = state.phone,
                            onValueChange = onPhoneChange,
                            label = "Phone Number",
                            placeholder = "+91 9876543210",
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = "Phone Icon") },
                            errorText = state.phoneError,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Phone,
                                imeAction = ImeAction.Next
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .semantics { contentDescription = "Phone Number Input" }
                        )

                        Spacer(modifier = Modifier.height(Spacing.md))

                        GrievTextField(
                            value = state.password,
                            onValueChange = onPasswordChange,
                            label = "Password",
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Lock Icon") },
                            trailingIcon = {
                                IconButton(onClick = onTogglePasswordVisibility) {
                                    Icon(
                                        imageVector = if (state.isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = if (state.isPasswordVisible) "Hide password" else "Show password"
                                    )
                                }
                            },
                            visualTransformation = if (state.isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Next
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .semantics { contentDescription = "Password Input" }
                        )

                        if (state.password.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(Spacing.xs))
                            Text(
                                text = "Password Strength: ${state.passwordStrength.name}",
                                style = MaterialTheme.typography.labelMedium,
                                color = when (state.passwordStrength) {
                                    ValidationUtils.PasswordStrength.STRONG -> MaterialTheme.colorScheme.primary
                                    ValidationUtils.PasswordStrength.MEDIUM -> MaterialTheme.colorScheme.secondary
                                    ValidationUtils.PasswordStrength.WEAK -> MaterialTheme.colorScheme.error
                                },
                                modifier = Modifier.align(Alignment.Start)
                            )
                        }

                        Spacer(modifier = Modifier.height(Spacing.md))

                        GrievTextField(
                            value = state.confirmPassword,
                            onValueChange = onConfirmPasswordChange,
                            label = "Confirm Password",
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Lock Icon") },
                            trailingIcon = {
                                IconButton(onClick = onToggleConfirmPasswordVisibility) {
                                    Icon(
                                        imageVector = if (state.isConfirmPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = if (state.isConfirmPasswordVisible) "Hide password" else "Show password"
                                    )
                                }
                            },
                            errorText = state.confirmPasswordError,
                            visualTransformation = if (state.isConfirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(onDone = { onRegisterClick() }),
                            modifier = Modifier
                                .fillMaxWidth()
                                .semantics { contentDescription = "Confirm Password Input" }
                        )

                        Spacer(modifier = Modifier.height(Spacing.md))

                        Text(
                            text = "By registering, you agree to GrievTech Civic Terms of Service and Privacy Policy.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(Spacing.md))

                        GrievButton(
                            text = if (state.isLoading) "Creating Account..." else "Register Account",
                            onClick = onRegisterClick,
                            enabled = !state.isLoading,
                            type = GrievButtonType.PRIMARY,
                            modifier = Modifier
                                .fillMaxWidth()
                                .semantics { contentDescription = "Register Account Button" }
                        )

                        Spacer(modifier = Modifier.height(Spacing.lg))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Already have an account?",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            TextButton(onClick = onNavigateToLogin) {
                                Text(
                                    text = "Sign In",
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(name = "Light Mode", showBackground = true)
@Composable
fun RegisterScreenLightPreview() {
    GrievTechTheme(darkTheme = false) {
        RegisterScreenContent(
            state = com.grievtech.app.ui.auth.RegisterUiState(),
            onFullNameChange = {},
            onEmailChange = {},
            onPhoneChange = {},
            onPasswordChange = {},
            onConfirmPasswordChange = {},
            onTogglePasswordVisibility = {},
            onToggleConfirmPasswordVisibility = {},
            onRegisterClick = {},
            onNavigateToLogin = {}
        )
    }
}

@Preview(name = "Dark Mode", showBackground = true)
@Composable
fun RegisterScreenDarkPreview() {
    GrievTechTheme(darkTheme = true) {
        RegisterScreenContent(
            state = com.grievtech.app.ui.auth.RegisterUiState(),
            onFullNameChange = {},
            onEmailChange = {},
            onPhoneChange = {},
            onPasswordChange = {},
            onConfirmPasswordChange = {},
            onTogglePasswordVisibility = {},
            onToggleConfirmPasswordVisibility = {},
            onRegisterClick = {},
            onNavigateToLogin = {}
        )
    }
}
