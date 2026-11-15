package com.grievtech.app.ui.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grievtech.app.data.model.UserRole
import com.grievtech.app.data.repository.AuthRepository
import com.grievtech.app.data.repository.ProfileRepository
import com.grievtech.app.util.Resource
import com.grievtech.app.util.ValidationUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isForgotPasswordOpen: Boolean = false,
    val forgotPasswordEmail: String = "",
    val isResetSuccess: Boolean = false,
    val targetRole: UserRole = UserRole.CITIZEN
)

data class RegisterUiState(
    val fullName: String = "",
    val email: String = "",
    val phone: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isPasswordVisible: Boolean = false,
    val isConfirmPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val fullNameError: String? = null,
    val emailError: String? = null,
    val phoneError: String? = null,
    val confirmPasswordError: String? = null,
    val passwordStrength: ValidationUtils.PasswordStrength = ValidationUtils.PasswordStrength.WEAK
)

sealed class AuthUiEvent {
    data class LoginSuccess(val role: UserRole) : AuthUiEvent()
    object RegisterSuccess : AuthUiEvent()
    data class ShowToast(val message: String) : AuthUiEvent()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val _loginState = MutableStateFlow(LoginUiState())
    val loginState: StateFlow<LoginUiState> = _loginState.asStateFlow()

    private val _registerState = MutableStateFlow(RegisterUiState())
    val registerState: StateFlow<RegisterUiState> = _registerState.asStateFlow()

    private val _uiEvent = Channel<AuthUiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    fun setTargetRole(role: UserRole) {
        _loginState.update { it.copy(targetRole = role, errorMessage = null) }
    }

    fun onLoginEmailChanged(email: String) {
        _loginState.update { it.copy(email = email, errorMessage = null) }
    }

    fun onLoginPasswordChanged(password: String) {
        _loginState.update { it.copy(password = password, errorMessage = null) }
    }

    fun toggleLoginPasswordVisibility() {
        _loginState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun setForgotPasswordOpen(isOpen: Boolean) {
        _loginState.update {
            it.copy(
                isForgotPasswordOpen = isOpen,
                forgotPasswordEmail = if (isOpen) it.email else "",
                isResetSuccess = false
            )
        }
    }

    fun onForgotPasswordEmailChanged(email: String) {
        _loginState.update { it.copy(forgotPasswordEmail = email) }
    }

    fun sendPasswordReset() {
        val email = _loginState.value.forgotPasswordEmail
        if (!ValidationUtils.isValidEmail(email)) {
            _loginState.update { it.copy(errorMessage = "Please enter a valid email address.") }
            return
        }
        viewModelScope.launch {
            authRepository.sendPasswordReset(email).collectLatest { result ->
                when (result) {
                    is Resource.Loading -> _loginState.update { it.copy(isLoading = true) }
                    is Resource.Success -> {
                        _loginState.update {
                            it.copy(isLoading = false, isResetSuccess = true, errorMessage = null)
                        }
                    }
                    is Resource.Error -> {
                        _loginState.update {
                            it.copy(isLoading = false, errorMessage = result.message)
                        }
                    }
                }
            }
        }
    }

    fun login() {
        val email = _loginState.value.email
        val password = _loginState.value.password
        val targetRole = _loginState.value.targetRole

        if (!ValidationUtils.isValidEmail(email)) {
            _loginState.update { it.copy(errorMessage = "Please enter a valid email address.") }
            return
        }
        if (password.isBlank()) {
            _loginState.update { it.copy(errorMessage = "Password cannot be empty.") }
            return
        }

        viewModelScope.launch {
            authRepository.signIn(email, password).collectLatest { result ->
                when (result) {
                    is Resource.Loading -> _loginState.update { it.copy(isLoading = true, errorMessage = null) }
                    is Resource.Success -> {
                        fetchRoleAndNavigate(targetRole)
                    }
                    is Resource.Error -> {
                        _loginState.update { it.copy(isLoading = false, errorMessage = result.message) }
                    }
                }
            }
        }
    }

    private suspend fun fetchRoleAndNavigate(targetRole: UserRole) {
        try {
            Log.d("GrievTechAuth", "fetchRoleAndNavigate started for targetRole=$targetRole")
            when (val result = profileRepository.getMyProfile()) {
                is Resource.Success -> {
                    val userProfile = result.data
                    val rawRole = userProfile.role.value.trim().lowercase()
                    val isExactAdmin = userProfile.role == UserRole.ADMIN || rawRole == "admin" || rawRole == "administrator"
                    Log.d("GrievTechAuth", "Role verification result: userUUID=${userProfile.id}, dbRole=${userProfile.role}, isExactAdmin=$isExactAdmin, targetRole=$targetRole")

                    if (targetRole == UserRole.ADMIN && !isExactAdmin) {
                        Log.w("GrievTechAuth", "Citizen user attempted Admin Login. Access denied.")
                        authRepository.signOut().collectLatest {}
                        _loginState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = "Access denied. Your account does not have administrator privileges."
                            )
                        }
                    } else if (targetRole == UserRole.CITIZEN && isExactAdmin) {
                        Log.w("GrievTechAuth", "Admin user attempted Citizen Login. Access denied.")
                        authRepository.signOut().collectLatest {}
                        _loginState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = "Access denied. Please sign in through the Administrator Portal."
                            )
                        }
                    } else {
                        _loginState.update { it.copy(isLoading = false) }
                        val verifiedRole = if (isExactAdmin) UserRole.ADMIN else UserRole.CITIZEN
                        _uiEvent.send(AuthUiEvent.LoginSuccess(verifiedRole))
                    }
                }
                is Resource.Error -> {
                    Log.e("GrievTechAuth", "Failed to fetch profile during login: message=${result.message}", result.cause)
                    authRepository.signOut().collectLatest {}
                    _loginState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Access denied. Unable to verify user profile (${result.message})."
                        )
                    }
                }
                is Resource.Loading -> {}
            }
        } catch (e: Exception) {
            Log.e("GrievTechAuth", "Exception during profile verification: ${e.message}", e)
            authRepository.signOut().collectLatest {}
            _loginState.update { it.copy(isLoading = false, errorMessage = "Access denied. Unable to verify user role.") }
        }
    }

    fun onRegisterFullNameChanged(fullName: String) {
        _registerState.update { it.copy(fullName = fullName, fullNameError = null) }
    }

    fun onRegisterEmailChanged(email: String) {
        _registerState.update { it.copy(email = email, emailError = null) }
    }

    fun onRegisterPhoneChanged(phone: String) {
        _registerState.update { it.copy(phone = phone, phoneError = null) }
    }

    fun onRegisterPasswordChanged(password: String) {
        val strength = ValidationUtils.checkPasswordStrength(password)
        _registerState.update { it.copy(password = password, passwordStrength = strength) }
    }

    fun onRegisterConfirmPasswordChanged(confirmPassword: String) {
        _registerState.update { it.copy(confirmPassword = confirmPassword, confirmPasswordError = null) }
    }

    fun toggleRegisterPasswordVisibility() {
        _registerState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun toggleRegisterConfirmPasswordVisibility() {
        _registerState.update { it.copy(isConfirmPasswordVisible = !it.isConfirmPasswordVisible) }
    }

    fun register() {
        val state = _registerState.value
        if (state.isLoading) return

        var isValid = true

        if (!ValidationUtils.validateFullName(state.fullName)) {
            _registerState.update { it.copy(fullNameError = "Please enter your full name (at least 2 characters).") }
            isValid = false
        }
        if (!ValidationUtils.isValidEmail(state.email)) {
            _registerState.update { it.copy(emailError = "Please enter a valid email address.") }
            isValid = false
        }
        if (!ValidationUtils.isValidPhone(state.phone)) {
            _registerState.update { it.copy(phoneError = "Please enter a valid 10-14 digit phone number.") }
            isValid = false
        }
        if (!ValidationUtils.validateConfirmPassword(state.password, state.confirmPassword)) {
            _registerState.update { it.copy(confirmPasswordError = "Passwords do not match.") }
            isValid = false
        }

        if (!isValid) return

        _registerState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            authRepository.signUp(
                email = state.email,
                password = state.password,
                fullName = state.fullName,
                phone = state.phone
            ).collectLatest { result ->
                when (result) {
                    is Resource.Loading -> _registerState.update { it.copy(isLoading = true, errorMessage = null) }
                    is Resource.Success -> {
                        _registerState.update { it.copy(isLoading = false) }
                        _uiEvent.send(AuthUiEvent.RegisterSuccess)
                    }
                    is Resource.Error -> {
                        _registerState.update { it.copy(isLoading = false, errorMessage = result.message) }
                    }
                }
            }
        }
    }
}
