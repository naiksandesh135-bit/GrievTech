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
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grievtech.app.R
import com.grievtech.app.data.model.UserRole
import com.grievtech.app.ui.auth.AuthUiEvent
import com.grievtech.app.ui.auth.AuthViewModel
import com.grievtech.app.ui.components.ErrorBanner
import com.grievtech.app.ui.components.GrievButton
import com.grievtech.app.ui.components.GrievButtonType
import com.grievtech.app.ui.components.GrievTextField
import com.grievtech.app.ui.components.GrievTopBar
import com.grievtech.app.ui.theme.Elevation
import com.grievtech.app.ui.theme.GrievTechTheme
import com.grievtech.app.ui.theme.Shapes
import com.grievtech.app.ui.theme.Spacing

@Composable
fun LoginScreen(
    initialRole: String,
    viewModel: AuthViewModel,
    onLoginSuccess: (UserRole) -> Unit,
    onNavigateToRegister: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.loginState.collectAsState()
    val keyboardController = LocalSoftwareKeyboardController.current

    val initialUserRole = if (initialRole == "ADMIN") UserRole.ADMIN else UserRole.CITIZEN

    LaunchedEffect(key1 = initialRole) {
        viewModel.setTargetRole(initialUserRole)
    }

    LaunchedEffect(key1 = true) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is AuthUiEvent.LoginSuccess -> onLoginSuccess(event.role)
                else -> {}
            }
        }
    }

    LoginScreenContent(
        state = state,
        onRoleChange = viewModel::setTargetRole,
        onEmailChange = viewModel::onLoginEmailChanged,
        onPasswordChange = viewModel::onLoginPasswordChanged,
        onTogglePasswordVisibility = viewModel::toggleLoginPasswordVisibility,
        onLoginClick = {
            keyboardController?.hide()
            viewModel.login()
        },
        onForgotPasswordClick = { viewModel.setForgotPasswordOpen(true) },
        onDismissForgotPassword = { viewModel.setForgotPasswordOpen(false) },
        onForgotPasswordEmailChange = viewModel::onForgotPasswordEmailChanged,
        onSendResetClick = viewModel::sendPasswordReset,
        onNavigateToRegister = onNavigateToRegister,
        onBackClick = onBackClick,
        modifier = modifier
    )
}

@Composable
fun LoginScreenContent(
    state: com.grievtech.app.ui.auth.LoginUiState,
    onRoleChange: (UserRole) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onTogglePasswordVisibility: () -> Unit,
    onLoginClick: () -> Unit,
    onForgotPasswordClick: () -> Unit,
    onDismissForgotPassword: () -> Unit,
    onForgotPasswordEmailChange: (String) -> Unit,
    onSendResetClick: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val isTargetAdmin = state.targetRole == UserRole.ADMIN

    Scaffold(
        topBar = {
            GrievTopBar(
                title = stringResource(if (isTargetAdmin) R.string.admin_login_title else R.string.login_title),
                subtitle = stringResource(R.string.login_subtitle),
                onBackClick = onBackClick
            )
        },
        modifier = modifier
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surfaceVariant,
                            MaterialTheme.colorScheme.background
                        )
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
                    text = "Raise · Track · Resolve",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
                )

                Spacer(modifier = Modifier.height(Spacing.md))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { contentDescription = "Login Form" },
                    shape = Shapes.medium,
                    elevation = CardDefaults.cardElevation(defaultElevation = Elevation.medium),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(Spacing.lg),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(R.string.select_portal_role),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(Spacing.xs))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
                        ) {
                            FilterChip(
                                selected = state.targetRole == UserRole.CITIZEN,
                                onClick = { onRoleChange(UserRole.CITIZEN) },
                                label = { Text(stringResource(R.string.role_citizen_portal)) },
                                modifier = Modifier.weight(1f)
                            )
                            FilterChip(
                                selected = state.targetRole == UserRole.ADMIN,
                                onClick = { onRoleChange(UserRole.ADMIN) },
                                label = { Text(stringResource(R.string.role_admin_portal)) },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(Spacing.md))

                        if (state.errorMessage != null) {
                            ErrorBanner(
                                message = state.errorMessage,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(Spacing.md))
                        }

                        GrievTextField(
                            value = state.email,
                            onValueChange = onEmailChange,
                            label = stringResource(R.string.email_label),
                            placeholder = "name@example.com",
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Email,
                                    contentDescription = "Email Icon"
                                )
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .semantics { contentDescription = "Email Input Field" }
                        )

                        Spacer(modifier = Modifier.height(Spacing.md))

                        GrievTextField(
                            value = state.password,
                            onValueChange = onPasswordChange,
                            label = stringResource(R.string.password_label),
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Password Icon"
                                )
                            },
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
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(onDone = { onLoginClick() }),
                            modifier = Modifier
                                .fillMaxWidth()
                                .semantics { contentDescription = "Password Input Field" }
                        )

                        Spacer(modifier = Modifier.height(Spacing.xs))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = onForgotPasswordClick) {
                                Text(
                                    text = stringResource(R.string.forgot_password),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(Spacing.md))

                        val buttonText = if (state.isLoading) {
                            stringResource(R.string.loading)
                        } else if (isTargetAdmin) {
                            stringResource(R.string.sign_in_as_admin)
                        } else {
                            stringResource(R.string.sign_in_as_citizen)
                        }

                        GrievButton(
                            text = buttonText,
                            onClick = onLoginClick,
                            enabled = !state.isLoading,
                            type = GrievButtonType.PRIMARY,
                            modifier = Modifier
                                .fillMaxWidth()
                                .semantics { contentDescription = "Sign In Button" }
                        )

                        Spacer(modifier = Modifier.height(Spacing.lg))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.dont_have_account),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            TextButton(onClick = onNavigateToRegister) {
                                Text(
                                    text = stringResource(R.string.register_now),
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

    if (state.isForgotPasswordOpen) {
        AlertDialog(
            onDismissRequest = onDismissForgotPassword,
            title = { Text(text = stringResource(R.string.forgot_password_title)) },
            text = {
                Column {
                    Text(
                        text = stringResource(R.string.forgot_password_desc),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(Spacing.md))
                    GrievTextField(
                        value = state.forgotPasswordEmail,
                        onValueChange = onForgotPasswordEmailChange,
                        label = stringResource(R.string.email_label),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (state.isResetSuccess) {
                        Spacer(modifier = Modifier.height(Spacing.sm))
                        Text(
                            text = stringResource(R.string.reset_link_sent),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = onSendResetClick) {
                    Text(stringResource(R.string.send_reset_link))
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissForgotPassword) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Preview(name = "Light Mode", showBackground = true)
@Composable
fun LoginScreenLightPreview() {
    GrievTechTheme(darkTheme = false) {
        LoginScreenContent(
            state = com.grievtech.app.ui.auth.LoginUiState(),
            onRoleChange = {},
            onEmailChange = {},
            onPasswordChange = {},
            onTogglePasswordVisibility = {},
            onLoginClick = {},
            onForgotPasswordClick = {},
            onDismissForgotPassword = {},
            onForgotPasswordEmailChange = {},
            onSendResetClick = {},
            onNavigateToRegister = {},
            onBackClick = {}
        )
    }
}

@Preview(name = "Dark Mode", showBackground = true)
@Composable
fun LoginScreenDarkPreview() {
    GrievTechTheme(darkTheme = true) {
        LoginScreenContent(
            state = com.grievtech.app.ui.auth.LoginUiState(),
            onRoleChange = {},
            onEmailChange = {},
            onPasswordChange = {},
            onTogglePasswordVisibility = {},
            onLoginClick = {},
            onForgotPasswordClick = {},
            onDismissForgotPassword = {},
            onForgotPasswordEmailChange = {},
            onSendResetClick = {},
            onNavigateToRegister = {},
            onBackClick = {}
        )
    }
}
