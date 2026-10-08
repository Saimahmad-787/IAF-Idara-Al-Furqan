package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.example.data.local.IafDatabase
import com.example.data.repository.IafRepository
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.ParentHomeScreen
import com.example.ui.screens.TeacherHomeScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AuthViewModel
import com.example.ui.viewmodel.ParentViewModel
import com.example.ui.viewmodel.TeacherViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val db = IafDatabase.getDatabase(applicationContext)
        val repository = IafRepository(db)
        val authViewModel = AuthViewModel(repository)
        val teacherViewModel = TeacherViewModel(repository)

        setContent {
            val authUiState by authViewModel.uiState.collectAsState()
            val layoutDirection = if (authUiState.selectedLanguage.isRtl) {
                LayoutDirection.Rtl
            } else {
                LayoutDirection.Ltr
            }

            CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
                MyApplicationTheme(darkTheme = authUiState.isDarkMode) {
                    Surface(modifier = Modifier.fillMaxSize()) {
                        val currentUser = authUiState.currentUser

                        if (currentUser == null) {
                            LoginScreen(
                                authViewModel = authViewModel,
                                uiState = authUiState
                            )
                        } else {
                            BackHandler {
                                // Prevent accidental logout or exit
                            }

                            when (currentUser.role) {
                                "Teacher" -> {
                                    TeacherHomeScreen(
                                        teacherUsername = currentUser.username,
                                        teacherViewModel = teacherViewModel,
                                        language = authUiState.selectedLanguage,
                                        isDarkMode = authUiState.isDarkMode,
                                        onToggleDarkMode = { authViewModel.toggleDarkMode() },
                                        onSignOut = { authViewModel.signOut() },
                                        onLanguageChange = { authViewModel.setLanguage(it) }
                                    )
                                }
                                else -> {
                                    val parentViewModel = remember(currentUser.username) {
                                        ParentViewModel(repository, currentUser.username)
                                    }
                                    ParentHomeScreen(
                                        parentViewModel = parentViewModel,
                                        language = authUiState.selectedLanguage,
                                        isDarkMode = authUiState.isDarkMode,
                                        onToggleDarkMode = { authViewModel.toggleDarkMode() },
                                        onSignOut = { authViewModel.signOut() },
                                        onLanguageChange = { authViewModel.setLanguage(it) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
