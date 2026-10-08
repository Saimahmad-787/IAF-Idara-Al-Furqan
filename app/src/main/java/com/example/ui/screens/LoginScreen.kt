package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.localization.AppLanguage
import com.example.localization.Strings
import com.example.ui.components.IafLogo
import com.example.ui.theme.*
import com.example.ui.viewmodel.AuthUiState
import com.example.ui.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    authViewModel: AuthViewModel,
    uiState: AuthUiState
) {
    val language = uiState.selectedLanguage
    var showRecoveryDialog by remember { mutableStateOf(false) }

    // Dialog form states
    var bootstrapCode by remember { mutableStateOf("") }
    var bootstrapUser by remember { mutableStateOf("") }
    var bootstrapPass by remember { mutableStateOf("") }
    var bootstrapName by remember { mutableStateOf("") }
    var bootstrapPhone by remember { mutableStateOf("") }

    var teacherRegCode by remember { mutableStateOf("") }
    var teacherRegUser by remember { mutableStateOf("") }
    var teacherRegPass by remember { mutableStateOf("") }
    var teacherRegName by remember { mutableStateOf("") }
    var teacherRegPhone by remember { mutableStateOf("") }
    var teacherRegInstId by remember { mutableStateOf(uiState.instituteIdInput) }

    var parentUser by remember { mutableStateOf("") }
    var parentPass by remember { mutableStateOf("") }
    var parentName by remember { mutableStateOf("") }
    var parentPhone by remember { mutableStateOf("") }

    val isTeacherPortal = uiState.selectedPortal == "Teacher"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = Strings.get("app_title", language), fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { authViewModel.toggleDarkMode() }) {
                        Icon(
                            imageVector = if (uiState.isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = if (uiState.isDarkMode) "Switch to White Mode" else "Switch to Black Mode",
                            tint = Color.White
                        )
                    }
                    LanguageDropdown(
                        selectedLanguage = language,
                        onLanguageSelected = { authViewModel.setLanguage(it) }
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = IafEmerald,
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(appBackgroundColor()),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 520.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                // IAF Official Logo Emblem
                IafLogo(size = 90.dp, isCircular = true)

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "IDARA AL-FURQAN",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = IafEmerald,
                    letterSpacing = 1.sp
                )

                Text(
                    text = "ادارہ الفرقان — اسلامی تعلیمی ادارہ",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF2E6342)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Distinct Portal Switcher (Teacher Portal vs Parent Portal)
                TabRow(
                    selectedTabIndex = if (isTeacherPortal) 0 else 1,
                    containerColor = Color(0xFFE8EFEA),
                    contentColor = IafEmerald,
                    indicator = { tabPositions ->
                        // Standard tab indicator handled automatically
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Tab(
                        selected = isTeacherPortal,
                        onClick = { authViewModel.setPortal("Teacher") },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.School, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Teacher Portal", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        },
                        modifier = Modifier.testTag("portal_teacher_tab")
                    )
                    Tab(
                        selected = !isTeacherPortal,
                        onClick = { authViewModel.setPortal("Parent") },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.FamilyRestroom, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Parent / Student", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        },
                        modifier = Modifier.testTag("portal_parent_tab")
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Error / Success feedback banners
                uiState.errorMessage?.let { errorMsg ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEE2E2)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Error, contentDescription = "Error", tint = Color(0xFFDC2626))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = errorMsg, color = Color(0xFF991B1B), fontSize = 13.sp)
                        }
                    }
                }

                uiState.successMessage?.let { successMsg ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFDCFCE7)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = "Success", tint = Color(0xFF16803D))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = successMsg, color = Color(0xFF166534), fontSize = 13.sp)
                        }
                    }
                }

                // Dedicated Card for Selected Portal
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = appCardColor()),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        // Portal Header Banner inside card
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isTeacherPortal) Color(0xFFE4F4EA) else Color(0xFFF1F5F9),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (isTeacherPortal) Icons.Default.AdminPanelSettings else Icons.Default.Person,
                                    contentDescription = null,
                                    tint = if (isTeacherPortal) IafEmerald else Color(0xFF334155),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isTeacherPortal) "TEACHER & ADMINISTRATION SIGN IN" else "PARENT & STUDENT SIGN IN",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isTeacherPortal) IafEmerald else Color(0xFF1E293B),
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Username Field
                        OutlinedTextField(
                            value = uiState.usernameInput,
                            onValueChange = { authViewModel.updateUsername(it) },
                            label = {
                                Text(if (isTeacherPortal) "Teacher Username" else "Parent Username")
                            },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = IafEmerald) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("username_input"),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Password Field
                        OutlinedTextField(
                            value = uiState.passwordInput,
                            onValueChange = { authViewModel.updatePassword(it) },
                            label = { Text(Strings.get("password", language)) },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = IafEmerald) },
                            trailingIcon = {
                                IconButton(onClick = { authViewModel.togglePasswordVisibility() }) {
                                    Icon(
                                        imageVector = if (uiState.isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = "Toggle password visibility"
                                    )
                                }
                            },
                            visualTransformation = if (uiState.isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("password_input"),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Sign In Button
                        Button(
                            onClick = { authViewModel.login() },
                            enabled = !uiState.isLoading,
                            colors = ButtonDefaults.buttonColors(containerColor = IafEmerald),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("login_button"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            if (uiState.isLoading) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp))
                            } else {
                                Text(
                                    text = if (isTeacherPortal) "Sign In as Teacher" else "Sign In as Parent / Student",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Distinct Registration Action based on Portal
                        if (isTeacherPortal) {
                            OutlinedButton(
                                onClick = { authViewModel.openTeacherRegisterDialog(true) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp)
                                    .testTag("teacher_register_button"),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, IafEmerald)
                            ) {
                                Icon(Icons.Default.Key, contentDescription = null, tint = IafEmerald, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Register Teacher (Authorization Code Required)",
                                    color = IafEmerald,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        } else {
                            OutlinedButton(
                                onClick = { authViewModel.openParentRegisterDialog(true) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp)
                                    .testTag("parent_register_button"),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, IafEmerald)
                            ) {
                                Icon(Icons.Default.PersonAdd, contentDescription = null, tint = IafEmerald, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Register New Parent Account",
                                    color = IafEmerald,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Account Recovery Instructions
                        TextButton(
                            onClick = { showRecoveryDialog = true },
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Text(
                                text = "Account Recovery / Support Instructions",
                                fontSize = 12.sp,
                                color = Color(0xFF4B5563)
                            )
                        }
                    }
                }
            }
        }
    }

    // Teacher Registration Dialog (Requires Authorization Code!)
    if (uiState.showTeacherRegisterDialog) {
        AlertDialog(
            onDismissRequest = { authViewModel.openTeacherRegisterDialog(false) },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = IafEmerald)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Teacher Registration", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = "Authorized teachers only. Enter the institute authorization code provided by the administrator.",
                        fontSize = 12.sp,
                        color = Color.DarkGray
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = teacherRegCode,
                        onValueChange = { teacherRegCode = it },
                        label = { Text("Institute Authorization Code *") },
                        placeholder = { Text("Enter secret authorization code") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("teacher_reg_code"),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = teacherRegUser,
                        onValueChange = { teacherRegUser = it },
                        label = { Text("Teacher Username *") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("teacher_reg_username"),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = teacherRegPass,
                        onValueChange = { teacherRegPass = it },
                        label = { Text("Password (min 6 chars) *") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("teacher_reg_password"),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = teacherRegName,
                        onValueChange = { teacherRegName = it },
                        label = { Text("Full Name / Title (e.g. Ustadh Farooq)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = teacherRegPhone,
                        onValueChange = { teacherRegPhone = it },
                        label = { Text("Phone Number") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        authViewModel.registerTeacher(
                            code = teacherRegCode,
                            u = teacherRegUser,
                            p = teacherRegPass,
                            name = teacherRegName,
                            phone = teacherRegPhone,
                            instituteId = com.example.data.repository.IafRepository.INSTITUTE_ID
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = IafEmerald),
                    modifier = Modifier.testTag("confirm_teacher_reg")
                ) {
                    Text("Register Teacher")
                }
            },
            dismissButton = {
                TextButton(onClick = { authViewModel.openTeacherRegisterDialog(false) }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Parent Registration Dialog
    if (uiState.showParentRegisterDialog) {
        AlertDialog(
            onDismissRequest = { authViewModel.openParentRegisterDialog(false) },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.PersonAdd, contentDescription = null, tint = IafEmerald)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Parent Registration", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = "Create a parent account. After logging in, you will be able to link to your children using their student Link Code.",
                        fontSize = 12.sp,
                        color = Color.DarkGray
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = parentUser,
                        onValueChange = { parentUser = it },
                        label = { Text("Parent Username *") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("parent_reg_username"),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = parentPass,
                        onValueChange = { parentPass = it },
                        label = { Text("Password (min 6 chars) *") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("parent_reg_password"),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = parentName,
                        onValueChange = { parentName = it },
                        label = { Text("Full Name (e.g. Tariq Mahmood) *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = parentPhone,
                        onValueChange = { parentPhone = it },
                        label = { Text("Contact Phone Number *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        authViewModel.registerParent(
                            u = parentUser,
                            p = parentPass,
                            name = parentName,
                            phone = parentPhone
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = IafEmerald),
                    modifier = Modifier.testTag("confirm_parent_reg_button")
                ) {
                    Text("Register Parent")
                }
            },
            dismissButton = {
                TextButton(onClick = { authViewModel.openParentRegisterDialog(false) }) {
                    Text("Cancel")
                }
            }
        )
    }

    // First Teacher Bootstrap Dialog
    if (uiState.showBootstrapDialog) {
        AlertDialog(
            onDismissRequest = { authViewModel.openBootstrapDialog(false) },
            title = { Text(text = Strings.get("teacher_bootstrap", language), fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = "Enter the institute bootstrap authorization code and set your teacher admin credentials.",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = bootstrapCode,
                        onValueChange = { bootstrapCode = it },
                        label = { Text(Strings.get("bootstrap_code", language)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("bootstrap_code_input"),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = bootstrapUser,
                        onValueChange = { bootstrapUser = it },
                        label = { Text("Teacher Username") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("bootstrap_username_input"),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = bootstrapPass,
                        onValueChange = { bootstrapPass = it },
                        label = { Text("Password (min 6 chars)") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("bootstrap_password_input"),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = bootstrapName,
                        onValueChange = { bootstrapName = it },
                        label = { Text("Full Name / Title") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = bootstrapPhone,
                        onValueChange = { bootstrapPhone = it },
                        label = { Text("Contact Phone") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        authViewModel.bootstrapTeacher(
                            code = bootstrapCode,
                            u = bootstrapUser,
                            p = bootstrapPass,
                            name = bootstrapName,
                            phone = bootstrapPhone
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = IafEmerald),
                    modifier = Modifier.testTag("confirm_bootstrap_button")
                ) {
                    Text("Complete Setup")
                }
            },
            dismissButton = {
                TextButton(onClick = { authViewModel.openBootstrapDialog(false) }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Account Recovery Instructions Dialog
    if (showRecoveryDialog) {
        AlertDialog(
            onDismissRequest = { showRecoveryDialog = false },
            title = { Text("Account Recovery Instructions", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        text = "For security and privacy at Idara Al-Furqan, credentials cannot be recovered without verification from an authorized institute teacher.",
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "• Teachers: Contact the head administration office.\n• Parents/Students: Please speak with your child's assigned Ustadh or visit the administration desk with your child's enrollment ID.",
                        fontSize = 12.sp,
                        color = Color.DarkGray
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showRecoveryDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = IafEmerald)
                ) {
                    Text("Got It")
                }
            }
        )
    }
}

@Composable
fun LanguageDropdown(
    selectedLanguage: AppLanguage,
    onLanguageSelected: (AppLanguage) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        TextButton(
            onClick = { expanded = true },
            colors = ButtonDefaults.textButtonColors(contentColor = Color.White)
        ) {
            Icon(Icons.Default.Language, contentDescription = "Language", modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = selectedLanguage.displayName, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            AppLanguage.values().forEach { lang ->
                DropdownMenuItem(
                    text = { Text(text = lang.displayName) },
                    onClick = {
                        onLanguageSelected(lang)
                        expanded = false
                    }
                )
            }
        }
    }
}
