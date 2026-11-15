package com.grievtech.app.ui.session

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grievtech.app.data.model.Profile
import com.grievtech.app.data.model.UserRole
import com.grievtech.app.data.repository.AuthRepository
import com.grievtech.app.data.repository.ProfileRepository
import com.grievtech.app.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AuthState {
    object Loading : AuthState()
    object Unauthenticated : AuthState()
    data class Citizen(val profile: Profile) : AuthState()
    data class Admin(val profile: Profile) : AuthState()
}

@HiltViewModel
class SessionViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        observeSession()
    }

    fun observeSession() {
        viewModelScope.launch {
            authRepository.sessionStatus.collectLatest { isAuthenticated ->
                Log.d("GrievTechAuth", "SessionViewModel observeSession: isAuthenticated=$isAuthenticated")
                if (isAuthenticated) {
                    fetchProfile()
                } else {
                    _authState.value = AuthState.Unauthenticated
                }
            }
        }
    }

    fun fetchProfile() {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            when (val result = profileRepository.getMyProfile()) {
                is Resource.Success -> {
                    val profile = result.data
                    val rawRole = profile.role.value.trim().lowercase()
                    val isExactAdmin = profile.role == UserRole.ADMIN || rawRole == "admin" || rawRole == "administrator"
                    Log.d("GrievTechAuth", "SessionViewModel profile resolved: id=${profile.id}, role=${profile.role}, isExactAdmin=$isExactAdmin")

                    _authState.value = if (isExactAdmin) {
                        AuthState.Admin(profile)
                    } else {
                        AuthState.Citizen(profile)
                    }
                }
                is Resource.Error -> {
                    Log.e("GrievTechAuth", "SessionViewModel profile fetch error: ${result.message}")
                    _authState.value = AuthState.Unauthenticated
                }
                is Resource.Loading -> {
                    _authState.value = AuthState.Loading
                }
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            _authState.value = AuthState.Unauthenticated
            authRepository.signOut().collectLatest { result ->
                Log.d("GrievTechAuth", "SessionViewModel signOut result: $result")
                _authState.value = AuthState.Unauthenticated
            }
        }
    }
}
