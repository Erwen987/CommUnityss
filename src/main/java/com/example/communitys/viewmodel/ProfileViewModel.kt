package com.example.communitys.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.communitys.model.repository.AuthRepository
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {

    private val authRepository = AuthRepository()

    private val _userProfile = MutableLiveData<UserProfile>()
    val userProfile: LiveData<UserProfile> = _userProfile

    private val _logoutState = MutableLiveData<LogoutState>()
    val logoutState: LiveData<LogoutState> = _logoutState

    init {
        loadUserProfile()
    }

    fun loadUserProfile() {
        viewModelScope.launch {
            val result = authRepository.getCurrentUser()

            result.onSuccess { user ->
                _userProfile.value = UserProfile(
                    name      = "${user.firstName} ${user.lastName}".trim(),
                    firstName = user.firstName,
                    lastName  = user.lastName,
                    email     = user.email,
                    barangay  = formatBarangay(user.barangay),
                    points    = user.points
                )
            }
        }
    }

    fun updateProfile(name: String, email: String, barangay: String) {
        _userProfile.value = _userProfile.value?.copy(
            name     = name,
            email    = email,
            barangay = barangay
        )
    }

    fun logout() {
        _logoutState.value = LogoutState.Loading
        viewModelScope.launch {
            val result = authRepository.logout()
            result.onSuccess {
                _logoutState.value = LogoutState.Success
            }.onFailure { exception ->
                _logoutState.value = LogoutState.Error(exception.message ?: "Logout failed")
            }
        }
    }

    private fun formatBarangay(barangay: String): String {
        if (barangay.isBlank()) return "Dagupan City, Pangasinan"
        val trimmed = barangay.trim()
        val prefix = if (trimmed.startsWith("Barangay", ignoreCase = true)) "" else "Barangay "
        return "${prefix}${trimmed}, Dagupan City, Pangasinan"
    }

    data class UserProfile(
        val name      : String = "",
        val firstName : String = "",
        val lastName  : String = "",
        val email     : String = "",
        val barangay  : String = "",
        val points    : Int    = 0
    )

    sealed class LogoutState {
        object Loading : LogoutState()
        object Success : LogoutState()
        data class Error(val message: String) : LogoutState()
    }
}