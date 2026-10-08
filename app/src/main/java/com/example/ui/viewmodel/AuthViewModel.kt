package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.UserEntity
import com.example.data.repository.IafRepository
import com.example.localization.AppLanguage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val currentUser: UserEntity? = null,
    val selectedLanguage: AppLanguage = AppLanguage.ENGLISH,
    val selectedPortal: String = "Teacher", // "Teacher" or "Parent"
    val isDarkMode: Boolean = false, // White mode (false) vs Black mode (true)
    val instituteIdInput: String = IafRepository.INSTITUTE_ID,
    val usernameInput: String = "",
    val passwordInput: String = "",
    val isPasswordVisible: Boolean = false,
    val isBootstrapAvailable: Boolean = false,
    val showBootstrapDialog: Boolean = false,
    val showTeacherRegisterDialog: Boolean = false,
    val showParentRegisterDialog: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class AuthViewModel(private val repository: IafRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun toggleDarkMode() {
        _uiState.value = _uiState.value.copy(isDarkMode = !_uiState.value.isDarkMode)
    }

    fun setDarkMode(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(isDarkMode = enabled)
    }

    init {
        viewModelScope.launch {
            repository.initializeDefaultsIfNeeded()
            checkBootstrapAvailability()
        }
    }

    fun checkBootstrapAvailability() {
        viewModelScope.launch {
            val available = repository.isBootstrapAvailable()
            _uiState.value = _uiState.value.copy(isBootstrapAvailable = available)
        }
    }

    fun setLanguage(language: AppLanguage) {
        _uiState.value = _uiState.value.copy(selectedLanguage = language)
    }

    fun setPortal(portal: String) {
        _uiState.value = _uiState.value.copy(
            selectedPortal = portal,
            usernameInput = "",
            passwordInput = "",
            errorMessage = null,
            successMessage = null
        )
    }

    fun updateInstituteId(id: String) {
        _uiState.value = _uiState.value.copy(instituteIdInput = id, errorMessage = null)
    }

    fun updateUsername(u: String) {
        _uiState.value = _uiState.value.copy(usernameInput = u, errorMessage = null)
    }

    fun updatePassword(p: String) {
        _uiState.value = _uiState.value.copy(passwordInput = p, errorMessage = null)
    }

    fun togglePasswordVisibility() {
        _uiState.value = _uiState.value.copy(isPasswordVisible = !_uiState.value.isPasswordVisible)
    }

    fun openBootstrapDialog(show: Boolean) {
        _uiState.value = _uiState.value.copy(showBootstrapDialog = show, errorMessage = null)
    }

    fun openTeacherRegisterDialog(show: Boolean) {
        _uiState.value = _uiState.value.copy(showTeacherRegisterDialog = show, errorMessage = null)
    }

    fun openParentRegisterDialog(show: Boolean) {
        _uiState.value = _uiState.value.copy(showParentRegisterDialog = show, errorMessage = null)
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(errorMessage = null, successMessage = null)
    }

    fun fillDemoTeacher() {
        _uiState.value = _uiState.value.copy(
            selectedPortal = "Teacher",
            instituteIdInput = IafRepository.INSTITUTE_ID,
            usernameInput = "teacher.demo",
            passwordInput = "teacher123",
            errorMessage = null
        )
    }

    fun fillDemoParent() {
        _uiState.value = _uiState.value.copy(
            selectedPortal = "Parent",
            instituteIdInput = IafRepository.INSTITUTE_ID,
            usernameInput = "parent.demo",
            passwordInput = "parent123",
            errorMessage = null
        )
    }

    fun login() {
        val s = _uiState.value
        if (s.usernameInput.isBlank() || s.passwordInput.isBlank()) {
            _uiState.value = s.copy(errorMessage = "Please enter both username and password.")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val result = if (s.selectedPortal == "Teacher") {
                repository.authenticateTeacher(
                    username = s.usernameInput,
                    password = s.passwordInput
                )
            } else {
                repository.authenticateParent(
                    username = s.usernameInput,
                    password = s.passwordInput
                )
            }
            result.fold(
                onSuccess = { user ->
                    _uiState.value = _uiState.value.copy(
                        currentUser = user,
                        isLoading = false,
                        passwordInput = "",
                        errorMessage = null
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Authentication failed."
                    )
                }
            )
        }
    }

    fun registerTeacher(code: String, u: String, p: String, name: String, phone: String, instituteId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val res = repository.registerTeacher(code, u, p, name, phone, instituteId)
            res.fold(
                onSuccess = { user ->
                    _uiState.value = _uiState.value.copy(
                        currentUser = user,
                        isLoading = false,
                        showTeacherRegisterDialog = false,
                        passwordInput = "",
                        successMessage = "Teacher Account Registered Successfully. Welcome Ustadh!"
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Registration failed."
                    )
                }
            )
        }
    }

    fun bootstrapTeacher(code: String, u: String, p: String, name: String, phone: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val res = repository.bootstrapFirstTeacher(code, u, p, name, phone)
            res.fold(
                onSuccess = { user ->
                    _uiState.value = _uiState.value.copy(
                        currentUser = user,
                        isLoading = false,
                        showBootstrapDialog = false,
                        isBootstrapAvailable = false,
                        passwordInput = "",
                        successMessage = "First Teacher Setup Complete. Welcome to IAF!"
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Bootstrap failed."
                    )
                }
            )
        }
    }

    fun registerParent(u: String, p: String, name: String, phone: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val res = repository.registerParent(u, p, name, phone)
            res.fold(
                onSuccess = { user ->
                    _uiState.value = _uiState.value.copy(
                        currentUser = user,
                        isLoading = false,
                        showParentRegisterDialog = false,
                        passwordInput = "",
                        successMessage = "Parent Account Created. Please link your student using Institute ID."
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Registration failed."
                    )
                }
            )
        }
    }

    fun signOut() {
        _uiState.value = _uiState.value.copy(
            currentUser = null,
            usernameInput = "",
            passwordInput = "",
            errorMessage = null,
            successMessage = null
        )
        checkBootstrapAvailability()
    }
}
