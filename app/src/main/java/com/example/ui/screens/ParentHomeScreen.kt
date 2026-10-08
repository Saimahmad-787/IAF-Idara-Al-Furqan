package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.local.*
import com.example.localization.AppLanguage
import com.example.localization.Strings
import com.example.ui.components.ConfirmedReceiptView
import com.example.ui.components.IafLogo
import com.example.ui.components.StudentIdCard
import com.example.ui.theme.*
import com.example.ui.viewmodel.ParentViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParentHomeScreen(
    parentViewModel: ParentViewModel,
    language: AppLanguage,
    isDarkMode: Boolean = false,
    onToggleDarkMode: () -> Unit = {},
    onSignOut: () -> Unit,
    onLanguageChange: (AppLanguage) -> Unit
) {
    val approvedLinks by parentViewModel.approvedLinks.collectAsState()
    val allLinks by parentViewModel.allParentLinks.collectAsState()
    val selectedStudent by parentViewModel.selectedStudent.collectAsState()
    val selectedStudentId by parentViewModel.selectedStudentId.collectAsState()

    val attendance by parentViewModel.attendance.collectAsState()
    val quranRecords by parentViewModel.quranRecords.collectAsState()
    val fees by parentViewModel.fees.collectAsState()
    val announcements by parentViewModel.announcements.collectAsState()
    val prayers by parentViewModel.prayerTimings.collectAsState()
    val leaveRequests by parentViewModel.leaveRequests.collectAsState()
    val weeklyDays by parentViewModel.weeklyDays.collectAsState()

    val showLinkDialog by parentViewModel.showLinkStudentDialog.collectAsState()
    val showPaymentDialog by parentViewModel.showPaymentDialog.collectAsState()
    val showReceiptDialog by parentViewModel.showReceiptDialog.collectAsState()
    val receiptFee by parentViewModel.selectedReceiptFee.collectAsState()
    val showLeaveDialog by parentViewModel.showLeaveDialog.collectAsState()
    val showAccountSettingsDialog by parentViewModel.showAccountSettingsDialog.collectAsState()
    val currentParentUser by parentViewModel.currentParentUsername.collectAsState()
    val statusMsg by parentViewModel.statusMessage.collectAsState()

    var activeTab by remember { mutableStateOf(0) } // 0: Overview, 1: Attendance, 2: Quran, 3: Fees, 4: Requests
    var showIdCardPreview by remember { mutableStateOf(false) }

    // Dialog input states
    var linkCodeInput by remember { mutableStateOf("") }
    var payAmount by remember { mutableStateOf("") }
    var payMethod by remember { mutableStateOf("Bank Transfer") }
    var payRef by remember { mutableStateOf("") }
    var payMonth by remember { mutableStateOf(SimpleDateFormat("MMMM yyyy", Locale.US).format(Date())) }
    var payScreenshotUri by remember { mutableStateOf<String?>(null) }

    val dialogPhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            payScreenshotUri = uri.toString()
        }
    }

    var leaveStart by remember { mutableStateOf("") }
    var leaveEnd by remember { mutableStateOf("") }
    var leaveReason by remember { mutableStateOf("") }

    var parentNewUser by remember { mutableStateOf("") }
    var parentPassVerify by remember { mutableStateOf("") }
    var parentCurrentPass by remember { mutableStateOf("") }
    var parentNewPass by remember { mutableStateOf("") }
    var settingsTab by remember { mutableStateOf(0) } // 0: Username, 1: Password

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IafLogo(size = 36.dp, isCircular = true)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Idara Al-Furqan",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Parent Portal • ${parentViewModel.parentUsername}",
                                fontSize = 11.sp,
                                color = Color(0xFFFBE49D)
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = onToggleDarkMode,
                        modifier = Modifier.testTag("theme_mode_toggle_parent")
                    ) {
                        Icon(
                            imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = if (isDarkMode) "Switch to White Mode" else "Switch to Black Mode",
                            tint = Color.White
                        )
                    }
                    IconButton(
                        onClick = { parentViewModel.openAccountSettings(true) },
                        modifier = Modifier.testTag("parent_settings_button")
                    ) {
                        Icon(Icons.Default.ManageAccounts, contentDescription = "Account Settings", tint = Color.White)
                    }
                    LanguageDropdown(
                        selectedLanguage = language,
                        onLanguageSelected = onLanguageChange
                    )
                    IconButton(
                        onClick = onSignOut,
                        modifier = Modifier.testTag("parent_sign_out")
                    ) {
                        Icon(Icons.Default.Logout, contentDescription = Strings.get("sign_out", language), tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = IafEmerald,
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        },
        bottomBar = {
            if (approvedLinks.isNotEmpty()) {
                NavigationBar(containerColor = appCardColor(), tonalElevation = 8.dp) {
                    NavigationBarItem(
                        selected = activeTab == 0,
                        onClick = { activeTab = 0 },
                        icon = { Icon(Icons.Default.Dashboard, contentDescription = "Overview") },
                        label = { Text("Overview", fontSize = 11.sp) }
                    )
                    NavigationBarItem(
                        selected = activeTab == 1,
                        onClick = { activeTab = 1 },
                        icon = { Icon(Icons.Default.EventAvailable, contentDescription = Strings.get("attendance", language)) },
                        label = { Text(Strings.get("attendance", language), fontSize = 11.sp) }
                    )
                    NavigationBarItem(
                        selected = activeTab == 2,
                        onClick = { activeTab = 2 },
                        icon = { Icon(Icons.Default.MenuBook, contentDescription = Strings.get("quran", language)) },
                        label = { Text("Quran", fontSize = 11.sp) }
                    )
                    NavigationBarItem(
                        selected = activeTab == 3,
                        onClick = { activeTab = 3 },
                        icon = { Icon(Icons.Default.Payment, contentDescription = Strings.get("fees", language)) },
                        label = { Text(Strings.get("fees", language), fontSize = 11.sp) }
                    )
                    NavigationBarItem(
                        selected = activeTab == 4,
                        onClick = { activeTab = 4 },
                        icon = { Icon(Icons.Default.PendingActions, contentDescription = "Requests") },
                        label = { Text("Requests", fontSize = 11.sp) }
                    )
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(appBackgroundColor())
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Status message bar
                statusMsg?.let { msg ->
                    Surface(
                        color = Color(0xFF166534),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = msg, color = Color.White, fontSize = 12.sp, modifier = Modifier.weight(1f))
                            IconButton(
                                onClick = { parentViewModel.clearStatusMessage() },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }

                // Multi-Student Selector Header Bar
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(0.dp),
                    colors = CardDefaults.cardColors(containerColor = appCardColor()),
                    border = BorderStroke(1.dp, appBorderColor()),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        if (approvedLinks.isNotEmpty()) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Icon(Icons.Default.Face, contentDescription = null, tint = IafEmerald)
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(text = "Viewing Student:", fontSize = 10.sp, color = Color.Gray)
                                    StudentDropdownSelector(
                                        links = approvedLinks,
                                        selectedStudentId = selectedStudentId,
                                        onSelect = { parentViewModel.selectStudent(it) }
                                    )
                                }
                            }
                        } else {
                            Text(
                                text = "No Approved Students",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Button(
                            onClick = { parentViewModel.openLinkStudentDialog(true) },
                            colors = ButtonDefaults.buttonColors(containerColor = IafEmerald),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("add_another_student_btn")
                        ) {
                            Icon(Icons.Default.AddLink, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (approvedLinks.isEmpty()) Strings.get("add_first_student", language) else Strings.get("add_another_student", language),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // If No Students Linked At All: Clean Empty State
                if (approvedLinks.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.verticalScroll(rememberScrollState())
                        ) {
                            IafLogo(size = 72.dp, isCircular = true)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = Strings.get("no_students", language),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E293B),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "To view your child's attendance, Quran progress, ID card, and fees, link their account using the Institute ID provided by their Ustadh (e.g. IAF-STU-001).",
                                fontSize = 13.sp,
                                color = Color.Gray,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(18.dp))

                            Button(
                                onClick = { parentViewModel.openLinkStudentDialog(true) },
                                colors = ButtonDefaults.buttonColors(containerColor = IafEmerald),
                                modifier = Modifier.testTag("empty_state_link_btn")
                            ) {
                                Icon(Icons.Default.AddLink, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(Strings.get("add_first_student", language), fontWeight = FontWeight.Bold)
                            }

                            // Show pending requests if parent submitted any
                            val pending = allLinks.filter { it.status == "Pending" }
                            if (pending.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(24.dp))
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7))
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Text(
                                            text = "Verification Requests Awaiting Teacher Review:",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = Color(0xFF92400E)
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        pending.forEach { link ->
                                            Text(
                                                text = "• ${link.studentName} (${link.instituteStudentCode})",
                                                fontSize = 12.sp,
                                                color = Color(0xFF78350F)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else if (selectedStudent != null) {
                    val stu = selectedStudent!!

                    when (activeTab) {
                        0 -> ParentOverviewTab(
                            student = stu,
                            attendance = attendance,
                            quranRecords = quranRecords,
                            fees = fees,
                            announcements = announcements,
                            prayers = prayers,
                            weeklyDays = weeklyDays,
                            language = language,
                            onViewIdCard = { showIdCardPreview = true },
                            onPayFee = {
                                payAmount = stu.monthlyFee.toString()
                                parentViewModel.openPaymentDialog(true)
                            },
                            onRequestLeave = { parentViewModel.openLeaveDialog(true) }
                        )
                        1 -> ParentAttendanceTab(
                            attendance = attendance,
                            weeklyDays = weeklyDays,
                            language = language
                        )
                        2 -> ParentQuranTab(
                            quranRecords = quranRecords,
                            student = stu,
                            weeklyDays = weeklyDays,
                            language = language
                        )
                        3 -> ParentFeesTab(
                            fees = fees,
                            student = stu,
                            language = language,
                            parentViewModel = parentViewModel,
                            onViewReceipt = { parentViewModel.viewReceipt(it) }
                        )
                        4 -> ParentRequestsTab(
                            allLinks = allLinks,
                            leaveRequests = leaveRequests,
                            language = language,
                            onAddStudent = { parentViewModel.openLinkStudentDialog(true) },
                            onRequestLeave = { parentViewModel.openLeaveDialog(true) }
                        )
                    }
                }
            }
        }
    }

    // Link Student Dialog (One Parent, Multiple Students)
    if (showLinkDialog) {
        AlertDialog(
            onDismissRequest = { parentViewModel.openLinkStudentDialog(false) },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AddLink, contentDescription = null, tint = IafEmerald)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(Strings.get("add_another_student", language), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column {
                    Text(
                        text = "Enter your child's Institute ID assigned by the teacher (e.g. IAF-STU-001). A verification request will be sent to the institute administration.",
                        fontSize = 12.sp,
                        color = Color.DarkGray
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = linkCodeInput,
                        onValueChange = { linkCodeInput = it },
                        label = { Text("Student Institute ID (e.g. IAF-STU-001)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("link_code_input"),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (linkCodeInput.isNotBlank()) {
                            parentViewModel.submitLinkRequest(linkCodeInput)
                            linkCodeInput = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = IafEmerald),
                    modifier = Modifier.testTag("submit_link_button")
                ) {
                    Text("Submit Link Request")
                }
            },
            dismissButton = {
                TextButton(onClick = { parentViewModel.openLinkStudentDialog(false) }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Submit Payment Dialog
    if (showPaymentDialog && selectedStudent != null) {
        val isOnline = payMethod != "Cash"
        val amt = payAmount.toDoubleOrNull() ?: 0.0
        val canSubmit = amt > 0 && (!isOnline || !payScreenshotUri.isNullOrBlank())

        AlertDialog(
            onDismissRequest = {
                payScreenshotUri = null
                parentViewModel.openPaymentDialog(false)
            },
            title = { Text(Strings.get("submit_payment", language), fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = "Student: ${selectedStudent!!.fullName} (${selectedStudent!!.instituteStudentCode})",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = IafEmerald
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = payAmount,
                        onValueChange = { payAmount = it },
                        label = { Text("Amount Paid (PKR)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("pay_amount_input"),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Payment Method:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    listOf("Bank Transfer", "EasyPaisa", "JazzCash", "Cash").forEach { method ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { payMethod = method }
                                .padding(vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = payMethod == method, onClick = { payMethod = method })
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(method, fontSize = 13.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = payRef,
                        onValueChange = { payRef = it },
                        label = { Text("Transaction Reference / Slip #") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = payMonth,
                        onValueChange = { payMonth = it },
                        label = { Text("Fee Month (e.g. October 2026)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    // Mandatory Screenshot Proof for Online Payments
                    if (isOnline) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (payScreenshotUri.isNullOrBlank()) Color(0xFFFEF2F2) else Color(0xFFF0FDF4),
                            border = BorderStroke(1.dp, if (payScreenshotUri.isNullOrBlank()) Color(0xFFFCA5A5) else Color(0xFF86EFAC)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (payScreenshotUri.isNullOrBlank()) Icons.Default.Warning else Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = if (payScreenshotUri.isNullOrBlank()) Color(0xFFDC2626) else Color(0xFF16A34A),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Payment Screenshot * (Required)",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (payScreenshotUri.isNullOrBlank()) Color(0xFF991B1B) else Color(0xFF166534)
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))

                                if (payScreenshotUri.isNullOrBlank()) {
                                    Text(
                                        text = "Online payment requires an attached screenshot of your payment slip/transaction confirmation.",
                                        fontSize = 11.sp,
                                        color = Color(0xFF7F1D1D)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Button(
                                            onClick = {
                                                dialogPhotoPickerLauncher.launch(
                                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                                )
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = IafEmerald),
                                            shape = RoundedCornerShape(6.dp),
                                            modifier = Modifier.weight(1f).height(34.dp)
                                        ) {
                                            Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Upload", fontSize = 11.sp)
                                        }
                                        OutlinedButton(
                                            onClick = {
                                                payScreenshotUri = "receipt_slip_${System.currentTimeMillis()}.png"
                                            },
                                            shape = RoundedCornerShape(6.dp),
                                            modifier = Modifier.weight(1f).height(34.dp)
                                        ) {
                                            Text("Sample Slip", fontSize = 11.sp)
                                        }
                                    }
                                } else {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Screenshot attached: ${payScreenshotUri!!.takeLast(24)}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFF166534),
                                            modifier = Modifier.weight(1f)
                                        )
                                        TextButton(onClick = { payScreenshotUri = null }) {
                                            Text("Remove", fontSize = 11.sp, color = Color(0xFFDC2626))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (canSubmit) {
                            parentViewModel.submitPayment(amt, payMethod, payRef, payMonth, null, payScreenshotUri)
                            payScreenshotUri = null
                        }
                    },
                    enabled = canSubmit,
                    colors = ButtonDefaults.buttonColors(containerColor = IafEmerald),
                    modifier = Modifier.testTag("confirm_submit_payment")
                ) {
                    Text("Submit for Verification")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    payScreenshotUri = null
                    parentViewModel.openPaymentDialog(false)
                }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Leave Request Dialog
    if (showLeaveDialog && selectedStudent != null) {
        AlertDialog(
            onDismissRequest = { parentViewModel.openLeaveDialog(false) },
            title = { Text("Submit Leave Request", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Student: ${selectedStudent!!.fullName}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = IafEmerald)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = leaveStart,
                        onValueChange = { leaveStart = it },
                        label = { Text("Start Date (e.g. 2026-10-10)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = leaveEnd,
                        onValueChange = { leaveEnd = it },
                        label = { Text("End Date (e.g. 2026-10-12)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = leaveReason,
                        onValueChange = { leaveReason = it },
                        label = { Text("Reason for absence") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (leaveStart.isNotBlank() && leaveReason.isNotBlank()) {
                            parentViewModel.submitLeaveRequest(leaveStart, leaveEnd.ifBlank { leaveStart }, leaveReason)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = IafEmerald)
                ) {
                    Text("Submit Request")
                }
            },
            dismissButton = {
                TextButton(onClick = { parentViewModel.openLeaveDialog(false) }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Digital Student ID Card Preview & Customization Dialog
    if (showIdCardPreview && selectedStudent != null) {
        com.example.ui.components.StudentIdCardViewerAndEditorDialog(
            student = selectedStudent!!,
            language = language,
            onDismiss = { showIdCardPreview = false }
        )
    }

    // Confirmed Receipt Dialog
    if (showReceiptDialog && receiptFee != null) {
        AlertDialog(
            onDismissRequest = { parentViewModel.closeReceipt() },
            title = { Text(Strings.get("confirmed_receipt", language), fontWeight = FontWeight.Bold) },
            text = {
                ConfirmedReceiptView(fee = receiptFee!!)
            },
            confirmButton = {
                Button(
                    onClick = { parentViewModel.closeReceipt() },
                    colors = ButtonDefaults.buttonColors(containerColor = IafEmerald)
                ) {
                    Text("Close")
                }
            }
        )
    }

    // Parent Account Settings Dialog (Change Username & Change Password)
    if (showAccountSettingsDialog) {
        AlertDialog(
            onDismissRequest = { parentViewModel.openAccountSettings(false) },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.ManageAccounts, contentDescription = null, tint = IafEmerald)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Parent Account Settings", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = "Logged in as: $currentParentUser",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = IafEmerald
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    TabRow(
                        selectedTabIndex = settingsTab,
                        containerColor = Color(0xFFF1F5F9),
                        contentColor = IafEmerald,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Tab(
                            selected = settingsTab == 0,
                            onClick = { settingsTab = 0 },
                            text = { Text("Change Username", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                        )
                        Tab(
                            selected = settingsTab == 1,
                            onClick = { settingsTab = 1 },
                            text = { Text("Change Password", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (settingsTab == 0) {
                        Text(
                            text = "Enter a new username and your current password to verify.",
                            fontSize = 12.sp,
                            color = Color.DarkGray
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = parentNewUser,
                            onValueChange = { parentNewUser = it },
                            label = { Text("New Username") },
                            modifier = Modifier.fillMaxWidth().testTag("parent_new_username_input"),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = parentPassVerify,
                            onValueChange = { parentPassVerify = it },
                            label = { Text("Current Password (to verify)") },
                            visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth().testTag("parent_pass_verify_input"),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                if (parentNewUser.isNotBlank() && parentPassVerify.isNotBlank()) {
                                    parentViewModel.changeParentUsername(parentNewUser, parentPassVerify) {
                                        parentNewUser = ""
                                        parentPassVerify = ""
                                        parentViewModel.openAccountSettings(false)
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = IafEmerald),
                            modifier = Modifier.fillMaxWidth().testTag("save_parent_username_btn")
                        ) {
                            Text("Save New Username")
                        }
                    } else {
                        Text(
                            text = "Enter your current password and your new password (minimum 6 characters).",
                            fontSize = 12.sp,
                            color = Color.DarkGray
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = parentCurrentPass,
                            onValueChange = { parentCurrentPass = it },
                            label = { Text("Current Password") },
                            visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth().testTag("parent_current_pass_input"),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = parentNewPass,
                            onValueChange = { parentNewPass = it },
                            label = { Text("New Password (min 6 chars)") },
                            visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth().testTag("parent_new_pass_input"),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                if (parentCurrentPass.isNotBlank() && parentNewPass.length >= 6) {
                                    parentViewModel.changeParentPassword(parentCurrentPass, parentNewPass) {
                                        parentCurrentPass = ""
                                        parentNewPass = ""
                                        parentViewModel.openAccountSettings(false)
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = IafEmerald),
                            modifier = Modifier.fillMaxWidth().testTag("save_parent_password_btn")
                        ) {
                            Text("Save New Password")
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { parentViewModel.openAccountSettings(false) }) {
                    Text("Close")
                }
            }
        )
    }
}

// -------------------------------------------------------------
// Dropdown Selector for Multi-Student Switcher
// -------------------------------------------------------------
@Composable
fun StudentDropdownSelector(
    links: List<ParentStudentLinkEntity>,
    selectedStudentId: Long?,
    onSelect: (Long) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val current = links.find { it.studentId == selectedStudentId } ?: links.firstOrNull()

    Box {
        Surface(
            modifier = Modifier
                .clickable { expanded = true }
                .padding(vertical = 2.dp),
            shape = RoundedCornerShape(6.dp),
            color = Color(0xFFE4F4EA)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = current?.studentName ?: "Select Child",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = IafEmerald
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = IafEmerald, modifier = Modifier.size(18.dp))
            }
        }

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            links.forEach { link ->
                DropdownMenuItem(
                    text = {
                        Column {
                            Text(text = link.studentName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(text = link.instituteStudentCode, fontSize = 11.sp, color = Color.Gray)
                        }
                    },
                    onClick = {
                        onSelect(link.studentId)
                        expanded = false
                    }
                )
            }
        }
    }
}

// -------------------------------------------------------------
// 1. Parent Overview Tab
// -------------------------------------------------------------
@Composable
fun ParentOverviewTab(
    student: StudentEntity,
    attendance: List<AttendanceEntity>,
    quranRecords: List<QuranRecordEntity>,
    fees: List<FeePaymentEntity>,
    announcements: List<AnnouncementEntity>,
    prayers: List<PrayerTimingEntity>,
    weeklyDays: List<InstituteWeeklyDayEntity>,
    language: AppLanguage,
    onViewIdCard: () -> Unit,
    onPayFee: () -> Unit,
    onRequestLeave: () -> Unit
) {
    val totalAtt = attendance.size
    val presentCount = attendance.count { it.status == "Present" }
    val attPct = if (totalAtt > 0) (presentCount * 100) / totalAtt else 100

    val unpaidFees = fees.filter { it.status != "Paid" }.sumOf { it.amountDue - it.amountPaid }

    val todayEntity = remember(weeklyDays) {
        com.example.util.WeeklyDaysUtil.getTodayEntity(weeklyDays)
    }
    val isTodayOff = todayEntity != null && !todayEntity.isOn

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Child Banner Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = IafEmerald)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IafLogo(size = 52.dp, isCircular = true)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = student.fullName, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Text(text = "${student.program} • ${student.className}", fontSize = 12.sp, color = Color(0xFFFBE49D))
                        Text(
                            text = "Institute ID: ${student.instituteStudentCode} • Roll: ${student.rollNumber ?: Strings.get("not_assigned", language)}",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = Color.White.copy(alpha = 0.2f))
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onRequestLeave,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(Color.White)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Icon(Icons.Default.EventBusy, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Request Leave", fontSize = 11.sp)
                    }

                    Button(
                        onClick = onViewIdCard,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD4A017)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Icon(Icons.Default.Badge, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFF0B331E))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(Strings.get("student_id_card", language), fontSize = 11.sp, color = Color(0xFF0B331E), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Today's Operating Schedule Status Banner
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = if (isTodayOff) Color(0xFFFEF2F2) else Color(0xFFF0FDF4),
            border = BorderStroke(1.dp, if (isTodayOff) Color(0xFFFCA5A5) else Color(0xFFBBF7D0)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isTodayOff) Icons.Default.EventBusy else Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = if (isTodayOff) Color(0xFFDC2626) else Color(0xFF16A34A),
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = if (isTodayOff) "TODAY IS AN INSTITUTE OFF DAY (${todayEntity?.customDayName ?: "Closed"})" else "TODAY: CLASSES ON (${todayEntity?.customDayName ?: "Open"})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = if (isTodayOff) Color(0xFF991B1B) else Color(0xFF166534)
                    )
                    Text(
                        text = if (isTodayOff) "No regular classes scheduled today (${todayEntity?.timingsOrNotes ?: "Off Day"}). Daily attendance and Quran logs show as OFF." else "Institute is open for classes. ${todayEntity?.timingsOrNotes ?: "Regular class hours"}.",
                        fontSize = 11.sp,
                        color = if (isTodayOff) Color(0xFF7F1D1D) else Color(0xFF14532D)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Quick Metrics (Attendance & Fee Balance)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = appCardColor()),
                border = BorderStroke(1.dp, appBorderColor())
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(text = "Attendance", fontSize = 11.sp, color = appSubtextColor(), fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "$attPct%", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = if (attPct >= 80) Color(0xFF16803D) else Color(0xFFD97706))
                    Text(text = "$presentCount of $totalAtt sessions", fontSize = 10.sp, color = appSubtextColor())
                }
            }

            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = appCardColor()),
                border = BorderStroke(1.dp, appBorderColor())
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(text = "Fee Balance", fontSize = 11.sp, color = appSubtextColor(), fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "PKR %.0f".format(unpaidFees),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (unpaidFees > 0) Color(0xFFDC2626) else Color(0xFF16803D)
                    )
                    Text(
                        text = if (unpaidFees > 0) "Payment Due" else "All Cleared",
                        fontSize = 10.sp,
                        color = if (unpaidFees > 0) Color(0xFFDC2626) else Color(0xFF16803D),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Recent Quran Learning Progress
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = appCardColor()),
            border = BorderStroke(1.dp, appBorderColor())
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Recent Quran Progress", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = appTextColor())
                    Icon(Icons.Default.MenuBook, contentDescription = null, tint = IafEmerald, modifier = Modifier.size(18.dp))
                }
                Spacer(modifier = Modifier.height(8.dp))

                val latest = quranRecords.firstOrNull()
                if (latest != null) {
                    Text(
                        text = "Date: ${latest.date} • ${latest.fluency} Fluency",
                        fontSize = 12.sp,
                        color = Color(0xFF16803D),
                        fontWeight = FontWeight.SemiBold
                    )
                    if (latest.sabaq != null) Text(text = "Sabaq: ${latest.sabaq}", fontSize = 12.sp, color = appTextColor())
                    if (latest.sabqi != null) Text(text = "Sabqi: ${latest.sabqi}", fontSize = 12.sp, color = appTextColor())
                    if (latest.manzil != null) Text(text = "Manzil: ${latest.manzil}", fontSize = 12.sp, color = appTextColor())
                    Text(text = "Lesson: ${latest.versesOrLesson}", fontSize = 12.sp, color = appTextColor())
                    latest.teacherComments?.let {
                        Text(text = "Ustadh Note: $it", fontSize = 11.sp, color = appSubtextColor(), modifier = Modifier.padding(top = 4.dp))
                    }
                } else {
                    Text(text = "No Quran progress records logged yet.", fontSize = 12.sp, color = appSubtextColor())
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Prayer Timings Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = appCardColor()),
            border = BorderStroke(1.dp, appBorderColor())
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(text = Strings.get("prayers", language), fontWeight = FontWeight.Bold, fontSize = 14.sp, color = appTextColor())
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    prayers.forEach { p ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = p.prayerName, fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.SemiBold)
                            Text(text = p.timeString, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = IafEmerald)
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 2. Parent Attendance Tab (2-Week Daily Retention & Weekly Day Status)
// -------------------------------------------------------------
@Composable
fun ParentAttendanceTab(
    attendance: List<AttendanceEntity>,
    weeklyDays: List<InstituteWeeklyDayEntity>,
    language: AppLanguage
) {
    // Generate past 14 days dates for 2-week history
    val past14Days = remember {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val cal = Calendar.getInstance()
        (0..13).map { i ->
            val c = cal.clone() as Calendar
            c.add(Calendar.DAY_OF_YEAR, -i)
            sdf.format(c.time)
        }
    }

    var selectedDailyDate by remember { mutableStateOf("All") }

    val todayEntity = remember(weeklyDays) {
        com.example.util.WeeklyDaysUtil.getTodayEntity(weeklyDays)
    }
    val isTodayOff = todayEntity != null && !todayEntity.isOn

    val filteredAttendance = remember(attendance, selectedDailyDate) {
        if (selectedDailyDate == "All") attendance else attendance.filter { it.date == selectedDailyDate }
    }

    val isSelectedDateOff = remember(selectedDailyDate, weeklyDays) {
        selectedDailyDate != "All" && com.example.util.WeeklyDaysUtil.isDateOff(selectedDailyDate, weeklyDays)
    }
    val selectedDayEntity = remember(selectedDailyDate, weeklyDays) {
        if (selectedDailyDate != "All") com.example.util.WeeklyDaysUtil.getDayEntityForDate(selectedDailyDate, weeklyDays) else null
    }

    val totalRecords = attendance.size
    val presentCount = attendance.count { it.status == "Present" }
    val absentCount = attendance.count { it.status == "Absent" }
    val leaveCount = attendance.count { it.status == "Leave" || it.status == "Late" || it.status == "Excused" }
    val attendanceRate = if (totalRecords > 0) ((presentCount.toDouble() / totalRecords) * 100).toInt() else 100

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Attendance Overview & Today's Status Banner
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = appCardColor()),
            border = BorderStroke(1.dp, appBorderColor()),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Attendance Summary",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = appTextColor()
                        )
                        Text(
                            text = "2-Week Record: $attendanceRate% Attendance Rate",
                            fontSize = 11.sp,
                            color = if (attendanceRate >= 80) Color(0xFF16803D) else Color(0xFFB45309),
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = when {
                            isTodayOff -> Color(0xFFFEE2E2)
                            attendance.any { it.date == SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()) && it.status == "Present" } -> Color(0xFFDCFCE7)
                            else -> Color(0xFFFEF3C7)
                        }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = if (isTodayOff) Icons.Default.EventBusy else Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = if (isTodayOff) Color(0xFF991B1B) else Color(0xFF166534),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isTodayOff) "Today: OFF" else "Today Active",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isTodayOff) Color(0xFF991B1B) else Color(0xFF166534)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFDCFCE7),
                        border = BorderStroke(1.dp, Color(0xFF86EFAC))
                    ) {
                        Column(modifier = Modifier.padding(vertical = 6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("$presentCount", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF15803D))
                            Text("Present", fontSize = 10.sp, color = Color(0xFF166534), fontWeight = FontWeight.Medium)
                        }
                    }
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFFEE2E2),
                        border = BorderStroke(1.dp, Color(0xFFFCA5A5))
                    ) {
                        Column(modifier = Modifier.padding(vertical = 6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("$absentCount", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB91C1C))
                            Text("Absent", fontSize = 10.sp, color = Color(0xFF991B1B), fontWeight = FontWeight.Medium)
                        }
                    }
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFFEF3C7),
                        border = BorderStroke(1.dp, Color(0xFFFCD34D))
                    ) {
                        Column(modifier = Modifier.padding(vertical = 6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("$leaveCount", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB45309))
                            Text("Leave", fontSize = 10.sp, color = Color(0xFF92400E), fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // If today is OFF, show Today's OFF status
        if (isTodayOff) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFFFEF2F2),
                border = BorderStroke(1.dp, Color(0xFFFCA5A5)),
                modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.EventBusy, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "TODAY: INSTITUTE OFF DAY (${todayEntity?.customDayName ?: "Closed"})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color(0xFF991B1B)
                        )
                        Text(
                            text = "Today is marked as an OFF day in the weekly operating schedule (${todayEntity?.timingsOrNotes ?: "Weekend/Holiday"}). Daily attendance shows as OFF.",
                            fontSize = 11.sp,
                            color = Color(0xFF7F1D1D)
                        )
                    }
                }
            }
        }

        // Date selector chips (2 weeks history)
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = appCardColor(),
            border = BorderStroke(1.dp, appBorderColor()),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.DateRange, contentDescription = null, tint = IafEmerald, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Daily Attendance History (Past 2 Weeks)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = IafEmerald
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (selectedDailyDate == "All") "Select a date to check attendance status or view full 2-week history:" else "Showing attendance for: $selectedDailyDate",
                    fontSize = 11.sp,
                    color = appSubtextColor()
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        FilterChip(
                            selected = selectedDailyDate == "All",
                            onClick = { selectedDailyDate = "All" },
                            label = { Text("All (2 Wks)", fontSize = 11.sp) }
                        )
                    }
                    items(past14Days) { d ->
                        val isDayOff = com.example.util.WeeklyDaysUtil.isDateOff(d, weeklyDays)
                        FilterChip(
                            selected = selectedDailyDate == d,
                            onClick = { selectedDailyDate = d },
                            label = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(d, fontSize = 11.sp)
                                    if (isDayOff) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("(OFF)", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFFDC2626))
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // If selected date is an OFF day, show prominent card
        if (isSelectedDateOff) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                border = BorderStroke(1.dp, Color(0xFFFCA5A5))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.EventBusy, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "INSTITUTE STATUS: OFF DAY (${selectedDayEntity?.customDayName ?: "Closed"})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color(0xFF991B1B)
                        )
                        Text(
                            text = "On $selectedDailyDate, the institute was closed as per the weekly operating schedule (${selectedDayEntity?.timingsOrNotes ?: "Off Day"}). Daily attendance for this day is marked OFF.",
                            fontSize = 11.sp,
                            color = Color(0xFF7F1D1D)
                        )
                    }
                }
            }
        }

        if (filteredAttendance.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                Text(
                    text = if (isSelectedDateOff) {
                        "INSTITUTE WAS OFF: On $selectedDailyDate (${selectedDayEntity?.customDayName ?: "Off Day"}), the institute was closed. Daily attendance is OFF."
                    } else if (selectedDailyDate != "All") {
                        "No attendance recorded for $selectedDailyDate."
                    } else {
                        "No attendance recorded in past 2 weeks."
                    },
                    color = appSubtextColor(),
                    textAlign = TextAlign.Center,
                    fontSize = 13.sp
                )
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(filteredAttendance, key = { it.id }) { item ->
                    val isRecordDayOff = com.example.util.WeeklyDaysUtil.isDateOff(item.date, weeklyDays)
                    val dayName = com.example.util.WeeklyDaysUtil.getDayNameFromDate(item.date)

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = appCardColor()),
                        border = BorderStroke(1.dp, appBorderColor()),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = item.date, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = appTextColor())
                                    if (dayName.isNotBlank()) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(text = "($dayName)", fontSize = 11.sp, color = Color.Gray)
                                    }
                                    if (isRecordDayOff) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = Color(0xFFFEE2E2)
                                        ) {
                                            Text(
                                                text = "OFF DAY",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF991B1B),
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                                Text(text = "Shift: ${item.shift}", fontSize = 11.sp, color = Color.Gray)
                            }
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = when (item.status) {
                                    "Present" -> Color(0xFFDCFCE7)
                                    "Absent" -> Color(0xFFFEE2E2)
                                    "Late" -> Color(0xFFFEF3C7)
                                    "Excused" -> Color(0xFFE0E7FF)
                                    "Off Day" -> Color(0xFFF3E8FF)
                                    else -> Color(0xFFF3E8FF)
                                }
                            ) {
                                Text(
                                    text = if (isRecordDayOff && item.status != "Present") "OFF (${item.status})" else item.status,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = when (item.status) {
                                        "Present" -> Color(0xFF166534)
                                        "Absent" -> Color(0xFF991B1B)
                                        "Late" -> Color(0xFF92400E)
                                        "Excused" -> Color(0xFF3730A3)
                                        "Off Day" -> Color(0xFF6B21A8)
                                        else -> Color(0xFF6B21A8)
                                    },
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 3. Parent Quran Progress Tab (Daily 2 Wks, Weekly 1 Mo, Bi-Weekly 2 Mo, Monthly 3 Mo)
// -------------------------------------------------------------
@Composable
fun ParentQuranTab(
    quranRecords: List<QuranRecordEntity>,
    student: StudentEntity,
    weeklyDays: List<InstituteWeeklyDayEntity>,
    language: AppLanguage
) {
    val isHifz = student.program == "Hifz-ul-Quran"
    var selectedFrequencyFilter by remember { mutableStateOf("Daily") } // Default to Daily

    // History filter states
    var selectedDailyDate by remember { mutableStateOf<String?>("All") }
    var selectedWeeklyWeek by remember { mutableStateOf("All") }
    var selectedBiWeeklyPeriod by remember { mutableStateOf("All") }
    var selectedMonthlyMonth by remember { mutableStateOf("All") }

    // Generate past 14 days dates for Daily history (Saved for 2 weeks)
    val past14Days = remember {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val cal = Calendar.getInstance()
        (0..13).map { i ->
            val c = cal.clone() as Calendar
            c.add(Calendar.DAY_OF_YEAR, -i)
            sdf.format(c.time)
        }
    }

    // Generate past 4 weeks for Weekly history (Saved for 1 month)
    val weeklyOptions = listOf(
        "All" to "All Weekly (Past 1 Month)",
        "Week 1" to "Week 1 (Oct 1 - Oct 7)",
        "Week 2" to "Week 2 (Sep 24 - Sep 30)",
        "Week 3" to "Week 3 (Sep 17 - Sep 23)",
        "Week 4" to "Week 4 (Sep 10 - Sep 16)"
    )

    // Generate past 4 bi-weekly periods (Saved for 2 months)
    val biWeeklyOptions = listOf(
        "All" to "All Bi-Weekly (Past 2 Months)",
        "Period 1" to "Oct 1 - Oct 15",
        "Period 2" to "Sep 16 - Sep 30",
        "Period 3" to "Sep 1 - Sep 15",
        "Period 4" to "Aug 16 - Aug 31"
    )

    // Generate past 3 months for Monthly history (Saved for 3 months)
    val monthlyOptions = listOf(
        "All" to "All Monthly (Past 3 Months)",
        "October 2026" to "October 2026",
        "September 2026" to "September 2026",
        "August 2026" to "August 2026"
    )

    // Filter records based on frequency and selected history period
    val filteredRecords = remember(
        quranRecords,
        selectedFrequencyFilter,
        selectedDailyDate,
        selectedWeeklyWeek,
        selectedBiWeeklyPeriod,
        selectedMonthlyMonth
    ) {
        when (selectedFrequencyFilter) {
            "Daily" -> {
                val dailyList = quranRecords.filter { it.recordType.equals("Daily", ignoreCase = true) }
                if (selectedDailyDate == null || selectedDailyDate == "All") {
                    dailyList
                } else {
                    dailyList.filter { it.date == selectedDailyDate }
                }
            }
            "Weekly" -> {
                val weeklyList = quranRecords.filter { it.recordType.equals("Weekly", ignoreCase = true) }
                if (selectedWeeklyWeek == "All") {
                    weeklyList
                } else {
                    weeklyList.filter {
                        when (selectedWeeklyWeek) {
                            "Week 1" -> it.date >= "2026-10-01" && it.date <= "2026-10-07"
                            "Week 2" -> it.date >= "2026-09-24" && it.date <= "2026-09-30"
                            "Week 3" -> it.date >= "2026-09-17" && it.date <= "2026-09-23"
                            "Week 4" -> it.date >= "2026-09-10" && it.date <= "2026-09-16"
                            else -> true
                        }
                    }
                }
            }
            "Bi-Weekly" -> {
                val biList = quranRecords.filter { it.recordType.equals("Bi-Weekly", ignoreCase = true) }
                if (selectedBiWeeklyPeriod == "All") {
                    biList
                } else {
                    biList.filter {
                        when (selectedBiWeeklyPeriod) {
                            "Period 1" -> it.date >= "2026-10-01" && it.date <= "2026-10-15"
                            "Period 2" -> it.date >= "2026-09-16" && it.date <= "2026-09-30"
                            "Period 3" -> it.date >= "2026-09-01" && it.date <= "2026-09-15"
                            "Period 4" -> it.date >= "2026-08-16" && it.date <= "2026-08-31"
                            else -> true
                        }
                    }
                }
            }
            "Monthly" -> {
                val monthlyList = quranRecords.filter { it.recordType.equals("Monthly", ignoreCase = true) }
                if (selectedMonthlyMonth == "All") {
                    monthlyList
                } else {
                    monthlyList.filter {
                        when (selectedMonthlyMonth) {
                            "October 2026" -> it.date.startsWith("2026-10")
                            "September 2026" -> it.date.startsWith("2026-09")
                            "August 2026" -> it.date.startsWith("2026-08")
                            else -> true
                        }
                    }
                }
            }
            else -> quranRecords
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = if (isHifz) "Hifz Record — Sabaq, Sabqi & Manzil" else "Quran Diary — Sabaq (${student.program})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = IafEmerald
                )
                Text(
                    text = "${student.fullName} • ${student.program}",
                    fontSize = 12.sp,
                    color = Color.DarkGray
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Frequency Selector Tabs
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            listOf("Daily", "Weekly", "Bi-Weekly", "Monthly", "All").forEach { freq ->
                val isSelected = selectedFrequencyFilter == freq
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedFrequencyFilter = freq },
                    label = {
                        Text(
                            text = freq,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Retention Policy Banner & History Selector per Frequency
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFFF8FAFC),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                when (selectedFrequencyFilter) {
                    "Daily" -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.DateRange, contentDescription = null, tint = IafEmerald, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Daily Reports (Saved for 2 weeks) • History Selector",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = IafEmerald
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (selectedDailyDate == "All") "Showing all daily records from the past 2 weeks. Select a date below to view that day's data:" else "Showing daily report for: $selectedDailyDate",
                            fontSize = 11.sp,
                            color = Color.DarkGray
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            item {
                                FilterChip(
                                    selected = selectedDailyDate == "All",
                                    onClick = { selectedDailyDate = "All" },
                                    label = { Text("All (2 Wks)", fontSize = 11.sp) }
                                )
                            }
                            items(past14Days) { d ->
                                val isDayOff = com.example.util.WeeklyDaysUtil.isDateOff(d, weeklyDays)
                                FilterChip(
                                    selected = selectedDailyDate == d,
                                    onClick = { selectedDailyDate = d },
                                    label = {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(d, fontSize = 11.sp)
                                            if (isDayOff) {
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("(OFF)", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFFDC2626))
                                            }
                                        }
                                    }
                                )
                            }
                        }

                        // If selected date is an OFF day, show prominent banner
                        val isSelectedDailyOff = selectedDailyDate != "All" && selectedDailyDate != null && com.example.util.WeeklyDaysUtil.isDateOff(selectedDailyDate!!, weeklyDays)
                        val selectedDayEntity = if (selectedDailyDate != "All" && selectedDailyDate != null) com.example.util.WeeklyDaysUtil.getDayEntityForDate(selectedDailyDate!!, weeklyDays) else null

                        if (isSelectedDailyOff) {
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                                border = BorderStroke(1.dp, Color(0xFFFCA5A5))
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.EventBusy, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(24.dp))
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "INSTITUTE STATUS: OFF DAY (${selectedDayEntity?.customDayName ?: "Closed"})",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = Color(0xFF991B1B)
                                        )
                                        Text(
                                            text = "On $selectedDailyDate, the institute was closed as per the weekly operating schedule (${selectedDayEntity?.timingsOrNotes ?: "Holiday/Weekend"}). Daily Quran lesson is OFF.",
                                            fontSize = 11.sp,
                                            color = Color(0xFF7F1D1D)
                                        )
                                    }
                                }
                            }
                        }
                    }
                    "Weekly" -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CalendarViewWeek, contentDescription = null, tint = Color(0xFF166534), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Weekly Reports (Saved for 1 month) • History Selector",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = Color(0xFF166534)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Select a week from the past month to view that week's data:",
                            fontSize = 11.sp,
                            color = Color.DarkGray
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(weeklyOptions) { (key, label) ->
                                FilterChip(
                                    selected = selectedWeeklyWeek == key,
                                    onClick = { selectedWeeklyWeek = key },
                                    label = { Text(label, fontSize = 11.sp) }
                                )
                            }
                        }
                    }
                    "Bi-Weekly" -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.DateRange, contentDescription = null, tint = Color(0xFF3730A3), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Bi-Weekly Reports (Saved for 2 months) • History Selector",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = Color(0xFF3730A3)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Select a bi-weekly period from the past 2 months:",
                            fontSize = 11.sp,
                            color = Color.DarkGray
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(biWeeklyOptions) { (key, label) ->
                                FilterChip(
                                    selected = selectedBiWeeklyPeriod == key,
                                    onClick = { selectedBiWeeklyPeriod = key },
                                    label = { Text(label, fontSize = 11.sp) }
                                )
                            }
                        }
                    }
                    "Monthly" -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = Color(0xFFB45309), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Monthly Reports (Saved for 3 months) • History Selector",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = Color(0xFFB45309)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Select a month from the past 3 months to view report:",
                            fontSize = 11.sp,
                            color = Color.DarkGray
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(monthlyOptions) { (key, label) ->
                                FilterChip(
                                    selected = selectedMonthlyMonth == key,
                                    onClick = { selectedMonthlyMonth = key },
                                    label = { Text(label, fontSize = 11.sp) }
                                )
                            }
                        }
                    }
                    else -> {
                        Text(
                            text = "All Progress Records: Daily (2 weeks), Weekly (1 month), Bi-Weekly (2 months), Monthly (3 months).",
                            fontSize = 11.sp,
                            color = Color.DarkGray
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (filteredRecords.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when (selectedFrequencyFilter) {
                        "Daily" -> {
                            val isSelectedOff = selectedDailyDate != "All" && selectedDailyDate != null && com.example.util.WeeklyDaysUtil.isDateOff(selectedDailyDate!!, weeklyDays)
                            val dayEnt = if (selectedDailyDate != "All" && selectedDailyDate != null) com.example.util.WeeklyDaysUtil.getDayEntityForDate(selectedDailyDate!!, weeklyDays) else null
                            if (isSelectedOff) {
                                "INSTITUTE WAS OFF: On $selectedDailyDate (${dayEnt?.customDayName ?: "Off Day"}), no Quran classes were held. Daily Quran lesson is OFF."
                            } else if (selectedDailyDate != "All") {
                                "No daily progress record found for $selectedDailyDate."
                            } else {
                                "No daily records logged in past 2 weeks."
                            }
                        }
                        "Weekly" -> "No weekly records found for selected week."
                        "Bi-Weekly" -> "No bi-weekly records found for selected period."
                        "Monthly" -> "No monthly records found for selected month."
                        else -> "No Quran progress records logged yet."
                    },
                    color = Color.Gray,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(filteredRecords, key = { it.id }) { rec ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = appCardColor()),
                        border = BorderStroke(1.dp, appBorderColor()),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            // Top bar of card: Date, Frequency Badge & Fluency
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = when (rec.recordType) {
                                            "Monthly" -> Color(0xFFFEF3C7)
                                            "Bi-Weekly" -> Color(0xFFE0E7FF)
                                            "Weekly" -> Color(0xFFDCFCE7)
                                            else -> Color(0xFFF1F5F9)
                                        }
                                    ) {
                                        Text(
                                            text = "${rec.recordType.uppercase()} RECORD",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = when (rec.recordType) {
                                                "Monthly" -> Color(0xFF92400E)
                                                "Bi-Weekly" -> Color(0xFF3730A3)
                                                "Weekly" -> Color(0xFF166534)
                                                else -> Color(0xFF475569)
                                            },
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(text = rec.date, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = appTextColor())
                                    val isRecDateOff = com.example.util.WeeklyDaysUtil.isDateOff(rec.date, weeklyDays)
                                    if (isRecDateOff) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = Color(0xFFFEE2E2)
                                        ) {
                                            Text(
                                                text = "OFF DAY",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF991B1B),
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color(0xFFE4F4EA)
                                ) {
                                    Text(
                                        text = rec.fluency,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = IafEmerald,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            if (isHifz) {
                                // HIFZ STRUCTURE: SABAQ, SABQI, MANZIL
                                rec.juz?.let {
                                    Text(
                                        text = "Juz $it • Surah ${rec.surah ?: "N/A"}",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = IafEmerald
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                }

                                // 1. Sabaq Box
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFFF0FDF4),
                                    border = BorderStroke(1.dp, Color(0xFFBBF7D0)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Text(text = "Sabaq (سبق):", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF166534))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(text = rec.sabaq ?: rec.versesOrLesson, fontSize = 12.sp, color = Color(0xFF0F172A))
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                // 2. Sabqi Box
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFFF0F9FF),
                                    border = BorderStroke(1.dp, Color(0xFFBAE6FD)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Text(text = "Sabqi (سبقی):", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF0369A1))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(text = rec.sabqi ?: "Reviewed current quarter Juz", fontSize = 12.sp, color = Color(0xFF0F172A))
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                // 3. Manzil Box
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFFFEF3C7),
                                    border = BorderStroke(1.dp, Color(0xFFFDE68A)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Text(text = "Manzil (منزل):", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFFB45309))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(text = rec.manzil ?: "Juz 30", fontSize = 12.sp, color = Color(0xFF0F172A))
                                    }
                                }
                            } else {
                                // QAIDA & NAZRA: SABAQ ONLY
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFFF0FDF4),
                                    border = BorderStroke(1.dp, Color(0xFFBBF7D0)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Text(text = "Sabaq (سبق):", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF166534))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(text = rec.sabaq ?: rec.versesOrLesson, fontSize = 13.sp, color = Color(0xFF0F172A), fontWeight = FontWeight.Medium)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Tajweed: ${rec.tajweedPronunciation}",
                                    fontSize = 11.sp,
                                    color = Color(0xFF475569)
                                )
                                Text(
                                    text = "Mistakes: ${rec.mistakes}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (rec.mistakes == 0) Color(0xFF16803D) else Color(0xFFD97706)
                                )
                            }

                            rec.teacherComments?.let {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Ustadh Note: $it",
                                    fontSize = 11.sp,
                                    color = Color(0xFF0F766E),
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                )
                            }

                            rec.nextAssignment?.let {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Next Target: $it",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFFB45309)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 4. Parent Fees Tab (Option 1: Seeing Fees [1-Yr History], Option 2: Paying Fees [Required Online Screenshot])
// -------------------------------------------------------------
@Composable
fun ParentFeesTab(
    fees: List<FeePaymentEntity>,
    student: StudentEntity,
    language: AppLanguage,
    parentViewModel: ParentViewModel,
    onViewReceipt: (FeePaymentEntity) -> Unit
) {
    var feeSubTab by remember { mutableStateOf(0) } // 0: Seeing Fees (1-Yr History), 1: Paying Fees
    var selectedMonthFilter by remember { mutableStateOf("All") }
    var selectedProofFee by remember { mutableStateOf<FeePaymentEntity?>(null) }

    // Paying form fields
    val netMonthlyFee = (student.monthlyFee - student.approvedConcession).coerceAtLeast(0.0)
    var payAmountInput by remember { mutableStateOf(netMonthlyFee.toString()) }
    var payMethodInput by remember { mutableStateOf("Bank Transfer") }
    var payRefInput by remember { mutableStateOf("") }
    var payMonthInput by remember { mutableStateOf(SimpleDateFormat("MMMM yyyy", Locale.US).format(Date())) }
    var payScreenshotUriInput by remember { mutableStateOf<String?>(null) }
    var payNotesInput by remember { mutableStateOf("") }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            payScreenshotUriInput = uri.toString()
        }
    }

    // Past 12 months for 1-year history filter
    val past12Months = remember {
        val sdf = SimpleDateFormat("MMMM yyyy", Locale.US)
        val cal = Calendar.getInstance()
        (0..11).map { i ->
            val c = cal.clone() as Calendar
            c.add(Calendar.MONTH, -i)
            sdf.format(c.time)
        }
    }

    val filteredFees = remember(fees, selectedMonthFilter) {
        if (selectedMonthFilter == "All") fees
        else fees.filter { it.invoiceMonth.equals(selectedMonthFilter, ignoreCase = true) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Tab Header: Option 1 (Seeing Fees) & Option 2 (Paying Fees)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Student Fees Management",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = IafEmerald
                )
                Text(
                    text = "${student.fullName} • Billed: PKR %.2f/mo".format(netMonthlyFee),
                    fontSize = 11.sp,
                    color = Color.DarkGray
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Two Main Options Required: Option 1: Seeing Fees, Option 2: Paying Fees
        TabRow(
            selectedTabIndex = feeSubTab,
            containerColor = appCardColor(),
            contentColor = IafEmerald,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
        ) {
            Tab(
                selected = feeSubTab == 0,
                onClick = { feeSubTab = 0 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("1. Seeing Fees (1-Yr)", fontSize = 12.sp, fontWeight = if (feeSubTab == 0) FontWeight.Bold else FontWeight.Normal)
                    }
                }
            )
            Tab(
                selected = feeSubTab == 1,
                onClick = { feeSubTab = 1 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("2. Paying Fees", fontSize = 12.sp, fontWeight = if (feeSubTab == 1) FontWeight.Bold else FontWeight.Normal)
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        when (feeSubTab) {
            0 -> {
                // ==========================================
                // OPTION 1: SEEING FEES (SAVED FOR 1 YEAR)
                // ==========================================
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFF0FDF4),
                    border = BorderStroke(1.dp, Color(0xFFBBF7D0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.History, contentDescription = null, tint = Color(0xFF166534), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Fee Records (Saved for 1 Year) • Month Selector",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = Color(0xFF166534)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            item {
                                FilterChip(
                                    selected = selectedMonthFilter == "All",
                                    onClick = { selectedMonthFilter = "All" },
                                    label = { Text("All (1 Year)", fontSize = 11.sp) }
                                )
                            }
                            items(past12Months) { m ->
                                FilterChip(
                                    selected = selectedMonthFilter == m,
                                    onClick = { selectedMonthFilter = m },
                                    label = { Text(m, fontSize = 11.sp) }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (filteredFees.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = if (selectedMonthFilter == "All") "No fee records logged in the past 1 year." else "No fee records found for $selectedMonthFilter.",
                                color = Color.Gray,
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = { feeSubTab = 1 },
                                colors = ButtonDefaults.buttonColors(containerColor = IafEmerald)
                            ) {
                                Text("Pay Fee Now", fontSize = 12.sp)
                            }
                        }
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(filteredFees, key = { it.id }) { fee ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = appCardColor()),
                                border = BorderStroke(1.dp, appBorderColor()),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(text = fee.invoiceMonth, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = appTextColor())
                                            Text(text = "Billed: PKR %.2f".format(fee.amountDue), fontSize = 11.sp, color = Color.Gray)
                                        }
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = when (fee.status) {
                                                "Paid" -> Color(0xFFDCFCE7)
                                                "Pending Verification" -> Color(0xFFFEF3C7)
                                                "Rejected" -> Color(0xFFFEE2E2)
                                                else -> Color(0xFFF1F5F9)
                                            }
                                        ) {
                                            Text(
                                                text = fee.status,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = when (fee.status) {
                                                    "Paid" -> Color(0xFF166534)
                                                    "Pending Verification" -> Color(0xFF92400E)
                                                    "Rejected" -> Color(0xFF991B1B)
                                                    else -> Color(0xFF475569)
                                                },
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                            )
                                        }
                                    }

                                    if (fee.amountPaid > 0) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "Amount Paid: PKR %.2f via ${fee.paymentMethod}".format(fee.amountPaid),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Color.DarkGray
                                        )
                                    }

                                    fee.transactionRef?.let {
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(text = "Ref / Slip: $it", fontSize = 11.sp, color = Color.Gray)
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // If screenshot proof was uploaded with online payment, allow viewing it
                                        if (!fee.evidenceUri.isNullOrBlank()) {
                                            OutlinedButton(
                                                onClick = { selectedProofFee = fee },
                                                shape = RoundedCornerShape(6.dp),
                                                modifier = Modifier.height(32.dp)
                                            ) {
                                                Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(14.dp), tint = IafEmerald)
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("View Screenshot", fontSize = 11.sp, color = IafEmerald)
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                        }

                                        if (fee.status == "Paid") {
                                            Button(
                                                onClick = { onViewReceipt(fee) },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16803D)),
                                                shape = RoundedCornerShape(6.dp),
                                                modifier = Modifier.height(32.dp)
                                            ) {
                                                Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(14.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Download Receipt", fontSize = 11.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            1 -> {
                // ==========================================
                // OPTION 2: PAYING FEES (REQUIRED ONLINE SCREENSHOT)
                // ==========================================
                val isOnline = payMethodInput != "Cash"
                val payAmt = payAmountInput.toDoubleOrNull() ?: 0.0
                val canSubmit = payAmt > 0 && (!isOnline || !payScreenshotUriInput.isNullOrBlank())

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = appCardColor()),
                        border = BorderStroke(1.dp, appBorderColor()),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Submit Fee Payment",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = IafEmerald
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Student: ${student.fullName} (${student.instituteStudentCode})",
                                fontSize = 12.sp,
                                color = Color.DarkGray
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedTextField(
                                value = payMonthInput,
                                onValueChange = { payMonthInput = it },
                                label = { Text("Fee Month") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = payAmountInput,
                                onValueChange = { payAmountInput = it },
                                label = { Text("Amount to Pay (PKR)") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Text("Select Payment Method:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(4.dp))

                            listOf(
                                "Bank Transfer" to "Online Bank Transfer / IBFT",
                                "EasyPaisa" to "EasyPaisa Mobile Account",
                                "JazzCash" to "JazzCash Mobile Account",
                                "Cash" to "Cash at Institute Reception"
                            ).forEach { (method, desc) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { payMethodInput = method }
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = payMethodInput == method,
                                        onClick = { payMethodInput = method }
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column {
                                        Text(method, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                        Text(desc, fontSize = 11.sp, color = Color.Gray)
                                    }
                                }
                            }

                            if (isOnline) {
                                Spacer(modifier = Modifier.height(10.dp))

                                // Institute Account Details Card
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFFF0FDF4),
                                    border = BorderStroke(1.dp, Color(0xFF86EFAC)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text("Institute Payment Accounts:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF166534))
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text("• Bank: Bank Al-Habib | A/C: 1029-0081-00234 | Idara Al-Furqan", fontSize = 11.sp, color = Color.DarkGray)
                                        Text("• EasyPaisa / JazzCash: 0300-8472910 (Idara Al-Furqan)", fontSize = 11.sp, color = Color.DarkGray)
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                OutlinedTextField(
                                    value = payRefInput,
                                    onValueChange = { payRefInput = it },
                                    label = { Text("Transaction Reference # / Slip ID") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                // MANDATORY SCREENSHOT PROOF UPLOAD CARD
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (payScreenshotUriInput.isNullOrBlank()) Color(0xFFFEF2F2) else Color(0xFFF0FDF4),
                                    border = BorderStroke(1.dp, if (payScreenshotUriInput.isNullOrBlank()) Color(0xFFFCA5A5) else Color(0xFF86EFAC)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = if (payScreenshotUriInput.isNullOrBlank()) Icons.Default.Warning else Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                tint = if (payScreenshotUriInput.isNullOrBlank()) Color(0xFFDC2626) else Color(0xFF16A34A),
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "Payment Screenshot * (Mandatory for Online)",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (payScreenshotUriInput.isNullOrBlank()) Color(0xFF991B1B) else Color(0xFF166534)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))

                                        if (payScreenshotUriInput.isNullOrBlank()) {
                                            Text(
                                                text = "Online payments (Bank Transfer, EasyPaisa, JazzCash) require an uploaded screenshot of your payment receipt before submitting.",
                                                fontSize = 11.sp,
                                                color = Color(0xFF7F1D1D)
                                            )
                                            Spacer(modifier = Modifier.height(10.dp))
                                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                Button(
                                                    onClick = {
                                                        photoPickerLauncher.launch(
                                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                                        )
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = IafEmerald),
                                                    shape = RoundedCornerShape(6.dp),
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(16.dp))
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text("Pick Screenshot", fontSize = 11.sp)
                                                }
                                                OutlinedButton(
                                                    onClick = {
                                                        payScreenshotUriInput = "online_slip_${System.currentTimeMillis()}.png"
                                                    },
                                                    shape = RoundedCornerShape(6.dp),
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Text("Attach Test Slip", fontSize = 11.sp)
                                                }
                                            }
                                        } else {
                                            Text(
                                                text = "Attached: ${payScreenshotUriInput!!.takeLast(30)}",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color(0xFF166534)
                                            )
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                OutlinedButton(
                                                    onClick = {
                                                        photoPickerLauncher.launch(
                                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                                        )
                                                    },
                                                    shape = RoundedCornerShape(6.dp),
                                                    modifier = Modifier.weight(1f).height(32.dp)
                                                ) {
                                                    Text("Change", fontSize = 11.sp)
                                                }
                                                TextButton(
                                                    onClick = { payScreenshotUriInput = null },
                                                    modifier = Modifier.height(32.dp)
                                                ) {
                                                    Text("Remove", fontSize = 11.sp, color = Color(0xFFDC2626))
                                                }
                                            }
                                        }
                                    }
                                }
                            } else {
                                Spacer(modifier = Modifier.height(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFFF8FAFC),
                                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "Cash payment should be handed over directly to the reception ustadh. They will verify and issue your confirmed receipt.",
                                        fontSize = 11.sp,
                                        color = Color.DarkGray,
                                        modifier = Modifier.padding(10.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = payNotesInput,
                                onValueChange = { payNotesInput = it },
                                label = { Text("Additional Notes (Optional)") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = {
                                    if (canSubmit) {
                                        parentViewModel.submitPayment(
                                            amount = payAmt,
                                            method = payMethodInput,
                                            ref = payRefInput,
                                            month = payMonthInput,
                                            notes = payNotesInput,
                                            evidenceUri = payScreenshotUriInput
                                        )
                                        payScreenshotUriInput = null
                                        payRefInput = ""
                                        feeSubTab = 0 // Switch to Seeing Fees tab to see the updated record!
                                    }
                                },
                                enabled = canSubmit,
                                colors = ButtonDefaults.buttonColors(containerColor = IafEmerald),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth().height(48.dp)
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isOnline && payScreenshotUriInput.isNullOrBlank()) "Screenshot Required to Submit" else "Submit Fee Payment",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Proof Screenshot Viewer Dialog
    if (selectedProofFee != null) {
        AlertDialog(
            onDismissRequest = { selectedProofFee = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Receipt, contentDescription = null, tint = IafEmerald)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Payment Screenshot Proof", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Student: ${selectedProofFee!!.studentName} • ${selectedProofFee!!.invoiceMonth}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.DarkGray
                    )
                    Text(
                        text = "Paid: PKR %.2f via ${selectedProofFee!!.paymentMethod}".format(selectedProofFee!!.amountPaid),
                        fontSize = 12.sp,
                        color = IafEmerald,
                        fontWeight = FontWeight.Bold
                    )
                    selectedProofFee!!.transactionRef?.let {
                        Text(text = "Transaction Ref: $it", fontSize = 11.sp, color = Color.Gray)
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFF1F5F9),
                        border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                        modifier = Modifier.fillMaxWidth().height(160.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(48.dp), tint = IafEmerald)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = selectedProofFee!!.evidenceUri ?: "Proof Screenshot",
                                    fontSize = 11.sp,
                                    color = Color.DarkGray,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 8.dp)
                                )
                                Text(text = "Uploaded & Verified by Administration", fontSize = 10.sp, color = Color(0xFF166534))
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { selectedProofFee = null },
                    colors = ButtonDefaults.buttonColors(containerColor = IafEmerald)
                ) {
                    Text("Close")
                }
            }
        )
    }
}

// -------------------------------------------------------------
// 5. Parent Requests Tab (Links & Leave)
// -------------------------------------------------------------
@Composable
fun ParentRequestsTab(
    allLinks: List<ParentStudentLinkEntity>,
    leaveRequests: List<LeaveRequestEntity>,
    language: AppLanguage,
    onAddStudent: () -> Unit,
    onRequestLeave: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Student Link Verifications Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Student Link Requests", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = IafEmerald)
            TextButton(onClick = onAddStudent) {
                Text("+ Link Another", fontSize = 12.sp, color = IafEmerald, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        allLinks.forEach { link ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = appCardColor()),
                border = BorderStroke(1.dp, appBorderColor())
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = link.studentName, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = appTextColor())
                        Text(text = "ID: ${link.instituteStudentCode}", fontSize = 11.sp, color = appSubtextColor())
                    }
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = when (link.status) {
                            "Approved" -> Color(0xFFDCFCE7)
                            "Pending" -> Color(0xFFFEF3C7)
                            else -> Color(0xFFFEE2E2)
                        }
                    ) {
                        Text(
                            text = link.status,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = when (link.status) {
                                "Approved" -> Color(0xFF166534)
                                "Pending" -> Color(0xFF92400E)
                                else -> Color(0xFF991B1B)
                            },
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Leave Requests Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Leave & Absence Requests", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = IafEmerald)
            TextButton(onClick = onRequestLeave) {
                Text("+ Request Leave", fontSize = 12.sp, color = IafEmerald, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        if (leaveRequests.isEmpty()) {
            Text("No leave requests submitted yet.", fontSize = 12.sp, color = appSubtextColor())
        } else {
            leaveRequests.forEach { req ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = appCardColor()),
                    border = BorderStroke(1.dp, appBorderColor())
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = req.studentName, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = appTextColor())
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = when (req.status) {
                                    "Approved" -> Color(0xFFDCFCE7)
                                    "Pending" -> Color(0xFFFEF3C7)
                                    else -> Color(0xFFFEE2E2)
                                }
                            ) {
                                Text(
                                    text = req.status,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = when (req.status) {
                                        "Approved" -> Color(0xFF166534)
                                        "Pending" -> Color(0xFF92400E)
                                        else -> Color(0xFF991B1B)
                                    },
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(text = "${req.startDate} to ${req.endDate}", fontSize = 11.sp, color = Color.Gray)
                        Text(text = "Reason: ${req.reason}", fontSize = 11.sp, color = Color.DarkGray)
                    }
                }
            }
        }
    }
}
