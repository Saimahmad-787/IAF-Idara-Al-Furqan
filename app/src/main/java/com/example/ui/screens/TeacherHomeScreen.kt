package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.ui.viewmodel.TeacherViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherHomeScreen(
    teacherUsername: String,
    teacherViewModel: TeacherViewModel,
    language: AppLanguage,
    isDarkMode: Boolean = false,
    onToggleDarkMode: () -> Unit = {},
    onSignOut: () -> Unit,
    onLanguageChange: (AppLanguage) -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    val students by teacherViewModel.students.collectAsState()
    val pendingLinks by teacherViewModel.pendingLinkRequests.collectAsState()
    val allFees by teacherViewModel.allFees.collectAsState()
    val shifts by teacherViewModel.shifts.collectAsState()
    val prayers by teacherViewModel.prayerTimings.collectAsState()
    val leaveRequests by teacherViewModel.pendingLeaveRequests.collectAsState()
    val announcements by teacherViewModel.announcements.collectAsState()
    val socialLinks by teacherViewModel.socialLinks.collectAsState()
    val retentionReviews by teacherViewModel.pendingRetentionReviews.collectAsState()
    val auditLogs by teacherViewModel.auditLogs.collectAsState()

    val showWizard by teacherViewModel.showWizardDialog.collectAsState()
    val wizardState by teacherViewModel.wizardState.collectAsState()
    val selectedStudent by teacherViewModel.selectedStudent.collectAsState()
    val showIdCard by teacherViewModel.showIdCardDialog.collectAsState()
    val statusMsg by teacherViewModel.statusMessage.collectAsState()

    // Active More Submenu State
    var moreSubmenu by remember { mutableStateOf<String?>(null) }

    // Dialog states for Quick Actions
    var quranLogStudent by remember { mutableStateOf<StudentEntity?>(null) }
    var selectedReceiptFee by remember { mutableStateOf<FeePaymentEntity?>(null) }

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
                                text = "Teacher Admin • $teacherUsername",
                                fontSize = 11.sp,
                                color = Color(0xFFFBE49D)
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = onToggleDarkMode,
                        modifier = Modifier.testTag("theme_mode_toggle")
                    ) {
                        Icon(
                            imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = if (isDarkMode) "Switch to White Mode" else "Switch to Black Mode",
                            tint = Color.White
                        )
                    }
                    LanguageDropdown(
                        selectedLanguage = language,
                        onLanguageSelected = onLanguageChange
                    )
                    IconButton(
                        onClick = onSignOut,
                        modifier = Modifier.testTag("sign_out_button")
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
            NavigationBar(containerColor = appCardColor(), tonalElevation = 8.dp) {
                NavigationBarItem(
                    selected = selectedTab == 0 && moreSubmenu == null,
                    onClick = { selectedTab = 0; moreSubmenu = null },
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = Strings.get("home", language)) },
                    label = { Text(Strings.get("home", language), fontSize = 11.sp) },
                    modifier = Modifier.testTag("nav_home")
                )
                NavigationBarItem(
                    selected = selectedTab == 1 && moreSubmenu == null,
                    onClick = { selectedTab = 1; moreSubmenu = null },
                    icon = {
                        BadgedBox(badge = {
                            if (students.isNotEmpty()) {
                                Badge { Text("${students.size}") }
                            }
                        }) {
                            Icon(Icons.Default.People, contentDescription = Strings.get("students", language))
                        }
                    },
                    label = { Text(Strings.get("students", language), fontSize = 11.sp) },
                    modifier = Modifier.testTag("nav_students")
                )
                NavigationBarItem(
                    selected = selectedTab == 2 && moreSubmenu == null,
                    onClick = { selectedTab = 2; moreSubmenu = null },
                    icon = { Icon(Icons.Default.EventAvailable, contentDescription = Strings.get("attendance", language)) },
                    label = { Text(Strings.get("attendance", language), fontSize = 11.sp) },
                    modifier = Modifier.testTag("nav_attendance")
                )
                NavigationBarItem(
                    selected = selectedTab == 3 && moreSubmenu == null,
                    onClick = { selectedTab = 3; moreSubmenu = null },
                    icon = {
                        val pendingCount = allFees.count { it.status == "Pending Verification" }
                        BadgedBox(badge = {
                            if (pendingCount > 0) {
                                Badge(containerColor = Color(0xFFD97706)) { Text("$pendingCount") }
                            }
                        }) {
                            Icon(Icons.Default.Payment, contentDescription = Strings.get("fees", language))
                        }
                    },
                    label = { Text(Strings.get("fees", language), fontSize = 11.sp) },
                    modifier = Modifier.testTag("nav_fees")
                )
                NavigationBarItem(
                    selected = selectedTab == 4 || moreSubmenu != null,
                    onClick = { selectedTab = 4; moreSubmenu = null },
                    icon = {
                        val alertCount = pendingLinks.size + leaveRequests.size + retentionReviews.size
                        BadgedBox(badge = {
                            if (alertCount > 0) {
                                Badge(containerColor = Color(0xFFDC2626)) { Text("$alertCount") }
                            }
                        }) {
                            Icon(Icons.Default.Menu, contentDescription = Strings.get("more", language))
                        }
                    },
                    label = { Text(Strings.get("more", language), fontSize = 11.sp) },
                    modifier = Modifier.testTag("nav_more")
                )
            }
        },
        floatingActionButton = {
            if (selectedTab == 1 && moreSubmenu == null) {
                ExtendedFloatingActionButton(
                    onClick = { teacherViewModel.openStudentWizard(true) },
                    containerColor = IafEmerald,
                    contentColor = Color.White,
                    icon = { Icon(Icons.Default.PersonAdd, contentDescription = null) },
                    text = { Text(Strings.get("add_student", language), fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("add_student_fab")
                )
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
                // Status snack/banner
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
                                onClick = { teacherViewModel.clearStatusMessage() },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }

                // Main Views based on selection
                if (moreSubmenu != null) {
                    TeacherMoreSubscreen(
                        submenu = moreSubmenu!!,
                        students = students,
                        pendingLinks = pendingLinks,
                        leaveRequests = leaveRequests,
                        shifts = shifts,
                        prayers = prayers,
                        announcements = announcements,
                        socialLinks = socialLinks,
                        retentionReviews = retentionReviews,
                        auditLogs = auditLogs,
                        language = language,
                        teacherUsername = teacherUsername,
                        teacherViewModel = teacherViewModel,
                        onBack = { moreSubmenu = null },
                        onQuickQuranLog = { student -> quranLogStudent = student }
                    )
                } else {
                    when (selectedTab) {
                        0 -> TeacherDashboardTab(
                            students = students,
                            pendingLinks = pendingLinks,
                            allFees = allFees,
                            leaveRequests = leaveRequests,
                            shifts = shifts,
                            language = language,
                            onNavigateToStudents = { selectedTab = 1 },
                            onNavigateToPendingFees = { selectedTab = 3 },
                            onNavigateToPendingLinks = { moreSubmenu = "links" },
                            onNavigateToPendingLeave = { moreSubmenu = "leave" },
                            onAddFirstStudent = { teacherViewModel.openStudentWizard(true) }
                        )
                        1 -> TeacherStudentsTab(
                            students = students,
                            language = language,
                            onViewIdCard = { teacherViewModel.selectStudentForIdCard(it) },
                            onQuickQuranLog = { quranLogStudent = it },
                            onAddStudent = { teacherViewModel.openStudentWizard(true) }
                        )
                        2 -> TeacherAttendanceTab(
                            students = students,
                            language = language,
                            teacherUsername = teacherUsername,
                            teacherViewModel = teacherViewModel
                        )
                        3 -> TeacherFeesTab(
                            allFees = allFees,
                            students = students,
                            language = language,
                            teacherUsername = teacherUsername,
                            teacherViewModel = teacherViewModel,
                            onViewReceipt = { selectedReceiptFee = it }
                        )
                        4 -> TeacherMoreMenuTab(
                            pendingLinksCount = pendingLinks.size,
                            pendingLeaveCount = leaveRequests.size,
                            retentionCount = retentionReviews.size,
                            language = language,
                            onSelectSubmenu = { moreSubmenu = it }
                        )
                    }
                }
            }
        }
    }

    // 5-Step Student Creation Wizard Dialog
    if (showWizard) {
        StudentCreationWizardDialog(
            state = wizardState,
            language = language,
            onUpdate = { teacherViewModel.updateWizard(it) },
            onDismiss = { teacherViewModel.openStudentWizard(false) },
            onSave = { teacherViewModel.saveStudent(teacherUsername) }
        )
    }

    // Digital Student ID Card Preview & Customization Dialog
    if (showIdCard && selectedStudent != null) {
        com.example.ui.components.StudentIdCardViewerAndEditorDialog(
            student = selectedStudent!!,
            language = language,
            onDismiss = { teacherViewModel.selectStudentForIdCard(null) },
            onSaveStudent = { updatedStudent ->
                teacherViewModel.updateStudent(updatedStudent, teacherUsername)
            }
        )
    }

    // Quick Quran Progress Logger Dialog
    if (quranLogStudent != null) {
        QuranProgressLogDialog(
            student = quranLogStudent!!,
            language = language,
            teacherUsername = teacherUsername,
            onDismiss = { quranLogStudent = null },
            onSave = { recordType, juz, surah, verses, sabaq, sabqi, manzil, fluency, tajweed, mistakes, comments, nextAssignment ->
                teacherViewModel.logQuranProgress(
                    student = quranLogStudent!!,
                    recordType = recordType,
                    juz = juz,
                    surah = surah,
                    lessonOrVerses = verses,
                    sabaq = sabaq,
                    sabqi = sabqi,
                    manzil = manzil,
                    fluency = fluency,
                    tajweed = tajweed,
                    mistakes = mistakes,
                    comments = comments,
                    nextAssignment = nextAssignment,
                    teacherUsername = teacherUsername
                )
                quranLogStudent = null
            }
        )
    }

    // Confirmed Receipt Viewer Dialog
    if (selectedReceiptFee != null) {
        AlertDialog(
            onDismissRequest = { selectedReceiptFee = null },
            title = { Text("Confirmed Payment Receipt", fontWeight = FontWeight.Bold) },
            text = {
                ConfirmedReceiptView(fee = selectedReceiptFee!!)
            },
            confirmButton = {
                Button(
                    onClick = { selectedReceiptFee = null },
                    colors = ButtonDefaults.buttonColors(containerColor = IafEmerald)
                ) {
                    Text("Done")
                }
            }
        )
    }
}

// -------------------------------------------------------------
// 1. Dashboard Tab
// -------------------------------------------------------------
@Composable
fun TeacherDashboardTab(
    students: List<StudentEntity>,
    pendingLinks: List<ParentStudentLinkEntity>,
    allFees: List<FeePaymentEntity>,
    leaveRequests: List<LeaveRequestEntity>,
    shifts: List<ShiftEntity>,
    language: AppLanguage,
    onNavigateToStudents: () -> Unit,
    onNavigateToPendingFees: () -> Unit,
    onNavigateToPendingLinks: () -> Unit,
    onNavigateToPendingLeave: () -> Unit,
    onAddFirstStudent: () -> Unit
) {
    val pendingFeeCount = allFees.count { it.status == "Pending Verification" }
    val totalCollected = allFees.filter { it.status == "Paid" }.sumOf { it.amountPaid }
    val totalOutstanding = allFees.filter { it.status != "Paid" }.sumOf { it.amountDue - it.amountPaid }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Welcome Banner with IAF Brand
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = IafEmerald)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IafLogo(size = 54.dp, isCircular = true)
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = "Idara Al-Furqan (ادارہ الفرقان)",
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Institute ID: IAF-2026 • Teacher Dashboard",
                        color = Color(0xFFFBE49D),
                        fontSize = 12.sp
                    )
                    Text(
                        text = SimpleDateFormat("EEEE — dd MMMM yyyy", Locale.US).format(Date()),
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 11.sp,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Quick Stats Row
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatCard(
                title = "Active Students",
                value = "${students.count { it.status == "Active" }}",
                icon = Icons.Default.School,
                color = IafEmerald,
                modifier = Modifier.weight(1f),
                onClick = onNavigateToStudents
            )
            StatCard(
                title = "Fees Collected",
                value = "PKR %.0f".format(totalCollected),
                icon = Icons.Default.AccountBalanceWallet,
                color = Color(0xFF16803D),
                modifier = Modifier.weight(1f),
                onClick = onNavigateToPendingFees
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatCard(
                title = "Pending Links",
                value = "${pendingLinks.size}",
                icon = Icons.Default.Link,
                color = if (pendingLinks.isNotEmpty()) Color(0xFFD97706) else Color(0xFF4B5563),
                modifier = Modifier.weight(1f),
                onClick = onNavigateToPendingLinks
            )
            StatCard(
                title = "Payment Reviews",
                value = "$pendingFeeCount",
                icon = Icons.Default.ReceiptLong,
                color = if (pendingFeeCount > 0) Color(0xFFD97706) else Color(0xFF4B5563),
                modifier = Modifier.weight(1f),
                onClick = onNavigateToPendingFees
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Empty state check for students
        if (students.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.PersonSearch,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = Strings.get("no_students", language),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.DarkGray,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = onAddFirstStudent,
                        colors = ButtonDefaults.buttonColors(containerColor = IafEmerald)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(Strings.get("add_first_student", language))
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Today's Shifts Schedule
        Text(
            text = "Today's Institute Shifts",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1E293B)
        )
        Spacer(modifier = Modifier.height(8.dp))

        shifts.forEach { shift ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE4F4EA)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.AccessTime, contentDescription = null, tint = IafEmerald, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = shift.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(
                            text = "${shift.startTime} - ${shift.endTime} • ${shift.assignedClass}",
                            fontSize = 11.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xFFF1F5F9)
                    ) {
                        Text(
                            text = shift.room,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = title, fontSize = 11.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Medium)
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

// -------------------------------------------------------------
// 2. Students Tab
// -------------------------------------------------------------
@Composable
fun TeacherStudentsTab(
    students: List<StudentEntity>,
    language: AppLanguage,
    onViewIdCard: (StudentEntity) -> Unit,
    onQuickQuranLog: (StudentEntity) -> Unit,
    onAddStudent: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedProgramFilter by remember { mutableStateOf("All") }

    val filtered = students.filter { student ->
        val matchesQuery = student.fullName.contains(searchQuery, ignoreCase = true) ||
                student.instituteStudentCode.contains(searchQuery, ignoreCase = true) ||
                (student.rollNumber?.contains(searchQuery, ignoreCase = true) == true)
        val matchesProgram = selectedProgramFilter == "All" || student.program == selectedProgramFilter
        matchesQuery && matchesProgram
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Search & Filter
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
            placeholder = { Text("Search by name, ID (e.g. IAF-STU-001), or roll number") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("student_search_input"),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Program Filter Chips (Exactly 3 programs)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            FilterChip(
                selected = selectedProgramFilter == "All",
                onClick = { selectedProgramFilter = "All" },
                label = { Text("All (${students.size})", fontSize = 11.sp) }
            )
            FilterChip(
                selected = selectedProgramFilter == "Noorani Qaida",
                onClick = { selectedProgramFilter = "Noorani Qaida" },
                label = { Text(Strings.get("noorani_qaida", language), fontSize = 11.sp) }
            )
            FilterChip(
                selected = selectedProgramFilter == "Nazra Quran",
                onClick = { selectedProgramFilter = "Nazra Quran" },
                label = { Text(Strings.get("nazra_quran", language), fontSize = 11.sp) }
            )
            FilterChip(
                selected = selectedProgramFilter == "Hifz-ul-Quran",
                onClick = { selectedProgramFilter = "Hifz-ul-Quran" },
                label = { Text(Strings.get("hifz_ul_quran", language), fontSize = 11.sp) }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (filtered.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.PersonSearch, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(52.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (students.isEmpty()) Strings.get("no_students", language) else "No matching students found.",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.DarkGray
                    )
                    if (students.isEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = onAddStudent,
                            colors = ButtonDefaults.buttonColors(containerColor = IafEmerald)
                        ) {
                            Text(Strings.get("add_first_student", language))
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filtered, key = { it.id }) { student ->
                    StudentCard(
                        student = student,
                        language = language,
                        onViewIdCard = { onViewIdCard(student) },
                        onQuickQuranLog = { onQuickQuranLog(student) }
                    )
                }
            }
        }
    }
}

@Composable
fun StudentCard(
    student: StudentEntity,
    language: AppLanguage,
    onViewIdCard: () -> Unit,
    onQuickQuranLog: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = appCardColor()),
        border = BorderStroke(1.dp, appBorderColor()),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE4F4EA))
                        .border(1.dp, IafEmerald, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (!student.photoUri.isNullOrBlank()) {
                        AsyncImage(
                            model = student.photoUri,
                            contentDescription = student.fullName,
                            modifier = Modifier.fillMaxSize().clip(CircleShape),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                    } else {
                        Icon(Icons.Default.Person, contentDescription = null, tint = IafEmerald, modifier = Modifier.size(28.dp))
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(text = student.fullName, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = appTextColor())
                    Text(
                        text = "${student.program} • ${student.className}",
                        fontSize = 12.sp,
                        color = IafEmerald,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Institute ID: ${student.instituteStudentCode} • Roll: ${student.rollNumber ?: Strings.get("not_assigned", language)}",
                        fontSize = 11.sp,
                        color = appSubtextColor()
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (student.status == "Active") Color(0xFFDCFCE7) else Color(0xFFF1F5F9)
                ) {
                    Text(
                        text = student.status,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (student.status == "Active") Color(0xFF166534) else Color(0xFF475569),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = appBorderColor())
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onQuickQuranLog,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Icon(Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.size(14.dp), tint = IafEmerald)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Log Quran", fontSize = 11.sp, color = IafEmerald)
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = onViewIdCard,
                    colors = ButtonDefaults.buttonColors(containerColor = IafEmerald),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Icon(Icons.Default.Badge, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.White)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(Strings.get("student_id_card", language), fontSize = 11.sp, color = Color.White)
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 3. Attendance Tab (Daily Marking of Unmarked + Done for today + History)
// -------------------------------------------------------------
@Composable
fun TeacherAttendanceTab(
    students: List<StudentEntity>,
    language: AppLanguage,
    teacherUsername: String,
    teacherViewModel: TeacherViewModel
) {
    val date by teacherViewModel.attendanceDate.collectAsState()
    val attendanceRecordsForDate by teacherViewModel.attendanceRecordsForDate.collectAsState()
    val historyDate by teacherViewModel.historyDate.collectAsState()
    val historyAttendanceRecords by teacherViewModel.historyAttendanceRecords.collectAsState()
    val weeklyDays by teacherViewModel.weeklyDays.collectAsState()

    val todayWeeklyEntity = remember(date, weeklyDays) {
        com.example.util.WeeklyDaysUtil.getDayEntityForDate(date, weeklyDays)
    }
    val isDateOff = todayWeeklyEntity != null && !todayWeeklyEntity.isOn

    val historyWeeklyEntity = remember(historyDate, weeklyDays) {
        com.example.util.WeeklyDaysUtil.getDayEntityForDate(historyDate, weeklyDays)
    }
    val isHistoryOff = historyWeeklyEntity != null && !historyWeeklyEntity.isOn

    var activeTab by remember { mutableStateOf(0) } // 0: Daily Attendance Session, 1: Attendance History

    val attendanceMap = remember(attendanceRecordsForDate) {
        attendanceRecordsForDate.associate { it.studentId to it.status }
    }
    val presentCount = remember(attendanceRecordsForDate) {
        attendanceRecordsForDate.count { it.status == "Present" }
    }
    val absentCount = remember(attendanceRecordsForDate) {
        attendanceRecordsForDate.count { it.status == "Absent" }
    }
    val leaveCount = remember(attendanceRecordsForDate) {
        attendanceRecordsForDate.count { it.status == "Leave" || it.status == "Late" || it.status == "Excused" }
    }
    val markedStudentIds = remember(attendanceRecordsForDate) {
        attendanceRecordsForDate.map { it.studentId }.toSet()
    }
    val unmarkedStudents = remember(students, markedStudentIds) {
        students.filter { it.id !in markedStudentIds }
    }
    var dailyFilter by remember { mutableStateOf("All") } // "All", "Unmarked", "Present", "Absent", "Leave"
    var attendanceSearchQuery by remember { mutableStateOf("") }

    val todayStr = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()) }
    val isToday = date == todayStr

    fun changeDailyDate(offsetDays: Int) {
        try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val cal = Calendar.getInstance()
            cal.time = sdf.parse(date) ?: Date()
            cal.add(Calendar.DAY_OF_YEAR, offsetDays)
            teacherViewModel.selectAttendanceDate(sdf.format(cal.time))
        } catch (_: Exception) {}
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Tab Switcher between Daily Attendance & History
        TabRow(
            selectedTabIndex = activeTab,
            containerColor = appCardColor(),
            contentColor = IafEmerald,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
        ) {
            Tab(
                selected = activeTab == 0,
                onClick = { activeTab = 0 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Today, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Daily Attendance (${markedStudentIds.size}/${students.size})",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                modifier = Modifier.testTag("attendance_daily_tab")
            )
            Tab(
                selected = activeTab == 1,
                onClick = { activeTab = 1 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Attendance History",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                modifier = Modifier.testTag("attendance_history_tab")
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (activeTab == 0) {
            // Institute OFF Day Banner if today is configured OFF
            if (isDateOff) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFFEF2F2),
                    border = BorderStroke(1.dp, Color(0xFFFCA5A5)),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.EventBusy, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "INSTITUTE STATUS: OFF DAY (${todayWeeklyEntity?.customDayName ?: "Closed"})",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color(0xFF991B1B)
                            )
                            Text(
                                text = "Today is marked as an OFF day (${todayWeeklyEntity?.timingsOrNotes ?: "Holiday/Weekend"}).",
                                fontSize = 11.sp,
                                color = Color(0xFF7F1D1D)
                            )
                        }
                        if (unmarkedStudents.isNotEmpty()) {
                            Button(
                                onClick = {
                                    teacherViewModel.markAllUnmarkedStudents(unmarkedStudents, "Off Day", teacherUsername)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text("Mark All OFF", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Clean, Charming Date Navigation & Quick Actions Bar
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = appCardColor()),
                border = BorderStroke(1.dp, appBorderColor()),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(
                            onClick = { changeDailyDate(-1) },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(Icons.Default.ChevronLeft, contentDescription = "Previous Day", tint = IafEmerald)
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Event, contentDescription = null, tint = IafEmerald, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = date,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = appTextColor()
                            )
                            if (isToday) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFFDCFCE7)
                                ) {
                                    Text(
                                        text = "Today",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF166534),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            } else {
                                Spacer(modifier = Modifier.width(8.dp))
                                TextButton(
                                    onClick = { teacherViewModel.selectAttendanceDate(todayStr) },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    modifier = Modifier.height(26.dp)
                                ) {
                                    Text("Go to Today", fontSize = 11.sp, color = IafEmerald)
                                }
                            }
                        }

                        IconButton(
                            onClick = { changeDailyDate(1) },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(Icons.Default.ChevronRight, contentDescription = "Next Day", tint = IafEmerald)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Charming Live Summary Statistics Counters
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
                            Column(
                                modifier = Modifier.padding(vertical = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("$presentCount", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF15803D))
                                Text("Present", fontSize = 10.sp, color = Color(0xFF166534), fontWeight = FontWeight.Medium)
                            }
                        }

                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFFEE2E2),
                            border = BorderStroke(1.dp, Color(0xFFFCA5A5))
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("$absentCount", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB91C1C))
                                Text("Absent", fontSize = 10.sp, color = Color(0xFF991B1B), fontWeight = FontWeight.Medium)
                            }
                        }

                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFFEF3C7),
                            border = BorderStroke(1.dp, Color(0xFFFCD34D))
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("$leaveCount", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB45309))
                                Text("Leave", fontSize = 10.sp, color = Color(0xFF92400E), fontWeight = FontWeight.Medium)
                            }
                        }

                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFF1F5F9),
                            border = BorderStroke(1.dp, Color(0xFFCBD5E1))
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("${unmarkedStudents.size}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF475569))
                                Text("Unmarked", fontSize = 10.sp, color = Color(0xFF334155), fontWeight = FontWeight.Medium)
                            }
                        }
                    }

                    if (unmarkedStudents.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = {
                                teacherViewModel.markAllUnmarkedStudents(unmarkedStudents, "Present", teacherUsername)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().height(38.dp).testTag("mark_all_present_btn")
                        ) {
                            Icon(Icons.Default.DoneAll, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Mark All Remaining Present (${unmarkedStudents.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Search & Category Filter Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(
                    "All" to "All (${students.size})",
                    "Unmarked" to "Unmarked (${unmarkedStudents.size})",
                    "Present" to "Present ($presentCount)",
                    "Absent" to "Absent ($absentCount)",
                    "Leave" to "Leave ($leaveCount)"
                ).forEach { (f, label) ->
                    FilterChip(
                        selected = dailyFilter == f,
                        onClick = { dailyFilter = f },
                        label = { Text(label, fontSize = 11.sp) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            val displayedStudents = remember(students, attendanceMap, dailyFilter) {
                when (dailyFilter) {
                    "Unmarked" -> students.filter { it.id !in markedStudentIds }
                    "Present" -> students.filter { attendanceMap[it.id] == "Present" }
                    "Absent" -> students.filter { attendanceMap[it.id] == "Absent" }
                    "Leave" -> students.filter {
                        val st = attendanceMap[it.id]
                        st == "Leave" || st == "Late" || st == "Excused"
                    }
                    else -> students
                }
            }

            if (displayedStudents.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (dailyFilter == "All") "No students enrolled in institute." else "No students match filter '$dailyFilter'.",
                        color = appSubtextColor(),
                        fontSize = 13.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(displayedStudents, key = { it.id }) { student ->
                        val currentStatus = attendanceMap[student.id]
                        val isPresent = currentStatus == "Present"
                        val isAbsent = currentStatus == "Absent"
                        val isLeave = currentStatus == "Leave" || currentStatus == "Late" || currentStatus == "Excused"

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = appCardColor()),
                            border = BorderStroke(
                                1.dp,
                                when {
                                    isPresent -> Color(0xFF86EFAC)
                                    isAbsent -> Color(0xFFFCA5A5)
                                    isLeave -> Color(0xFFFDE68A)
                                    else -> appBorderColor()
                                }
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(38.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    when {
                                                        isPresent -> Color(0xFFDCFCE7)
                                                        isAbsent -> Color(0xFFFEE2E2)
                                                        isLeave -> Color(0xFFFEF3C7)
                                                        else -> Color(0xFFE2E8F0)
                                                    }
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = student.fullName.take(1).uppercase(),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp,
                                                color = when {
                                                    isPresent -> Color(0xFF15803D)
                                                    isAbsent -> Color(0xFFB91C1C)
                                                    isLeave -> Color(0xFFB45309)
                                                    else -> Color(0xFF475569)
                                                }
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(10.dp))

                                        Column {
                                            Text(
                                                text = student.fullName,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = appTextColor()
                                            )
                                            Text(
                                                text = "${student.program} • ${student.className}",
                                                fontSize = 11.sp,
                                                color = IafEmerald,
                                                fontWeight = FontWeight.Medium
                                            )
                                            Text(
                                                text = "ID: ${student.instituteStudentCode} • Roll: ${student.rollNumber ?: "—"}",
                                                fontSize = 10.sp,
                                                color = appSubtextColor()
                                            )
                                        }
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = when {
                                            isPresent -> Color(0xFFDCFCE7)
                                            isAbsent -> Color(0xFFFEE2E2)
                                            isLeave -> Color(0xFFFEF3C7)
                                            else -> Color(0xFFF1F5F9)
                                        }
                                    ) {
                                        Text(
                                            text = currentStatus ?: "Unmarked",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = when {
                                                isPresent -> Color(0xFF15803D)
                                                isAbsent -> Color(0xFFB91C1C)
                                                isLeave -> Color(0xFFB45309)
                                                else -> Color(0xFF64748B)
                                            },
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Quick 1-Tap Segmented Attendance Buttons: P, A, L
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // PRESENT BUTTON
                                    Button(
                                        onClick = {
                                            teacherViewModel.markSingleStudentAttendance(student, "Present", teacherUsername)
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isPresent) Color(0xFF16A34A) else Color.Transparent,
                                            contentColor = if (isPresent) Color.White else Color(0xFF16A34A)
                                        ),
                                        border = if (isPresent) null else BorderStroke(1.dp, Color(0xFF86EFAC)),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f).height(34.dp).testTag("mark_p_${student.id}"),
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Present", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }

                                    // ABSENT BUTTON
                                    Button(
                                        onClick = {
                                            teacherViewModel.markSingleStudentAttendance(student, "Absent", teacherUsername)
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isAbsent) Color(0xFFDC2626) else Color.Transparent,
                                            contentColor = if (isAbsent) Color.White else Color(0xFFDC2626)
                                        ),
                                        border = if (isAbsent) null else BorderStroke(1.dp, Color(0xFFFCA5A5)),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f).height(34.dp).testTag("mark_a_${student.id}"),
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Absent", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }

                                    // LEAVE BUTTON
                                    Button(
                                        onClick = {
                                            teacherViewModel.markSingleStudentAttendance(student, "Leave", teacherUsername)
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isLeave) Color(0xFFD97706) else Color.Transparent,
                                            contentColor = if (isLeave) Color.White else Color(0xFFD97706)
                                        ),
                                        border = if (isLeave) null else BorderStroke(1.dp, Color(0xFFFCD34D)),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.weight(1f).height(34.dp).testTag("mark_l_${student.id}"),
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AccessTime,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Leave", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // ATTENDANCE HISTORY VIEW
            if (isHistoryOff) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFFEF2F2),
                    border = BorderStroke(1.dp, Color(0xFFFCA5A5)),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.EventBusy, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Weekly Schedule: OFF DAY (${historyWeeklyEntity?.customDayName ?: "Closed"})",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color(0xFF991B1B)
                            )
                            Text(
                                text = "The institute was closed on $historyDate as per the weekly operating schedule (${historyWeeklyEntity?.timingsOrNotes ?: "Holiday/Weekend"}). Attendance is OFF.",
                                fontSize = 11.sp,
                                color = Color(0xFF7F1D1D)
                            )
                        }
                    }
                }
            }

            // Date Selector Bar
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(
                            onClick = {
                                try {
                                    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                                    val cal = Calendar.getInstance()
                                    cal.time = sdf.parse(historyDate) ?: Date()
                                    cal.add(Calendar.DAY_OF_YEAR, -1)
                                    teacherViewModel.selectHistoryDate(sdf.format(cal.time))
                                } catch (_: Exception) {}
                            }
                        ) {
                            Icon(Icons.Default.ChevronLeft, contentDescription = "Previous Day")
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = IafEmerald, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = historyDate,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E293B)
                            )
                        }

                        IconButton(
                            onClick = {
                                try {
                                    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                                    val cal = Calendar.getInstance()
                                    cal.time = sdf.parse(historyDate) ?: Date()
                                    cal.add(Calendar.DAY_OF_YEAR, 1)
                                    teacherViewModel.selectHistoryDate(sdf.format(cal.time))
                                } catch (_: Exception) {}
                            }
                        ) {
                            Icon(Icons.Default.ChevronRight, contentDescription = "Next Day")
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Quick Date Chips
                    val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
                    val calYesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
                    val yesterdayStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(calYesterday.time)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilterChip(
                            selected = historyDate == todayStr,
                            onClick = { teacherViewModel.selectHistoryDate(todayStr) },
                            label = { Text("Today", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = historyDate == yesterdayStr,
                            onClick = { teacherViewModel.selectHistoryDate(yesterdayStr) },
                            label = { Text("Yesterday", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Summary Breakdown Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val presentCount = historyAttendanceRecords.count { it.status == "Present" }
                val absentCount = historyAttendanceRecords.count { it.status == "Absent" }
                val lateCount = historyAttendanceRecords.count { it.status == "Late" }
                val excusedCount = historyAttendanceRecords.count { it.status == "Excused" || it.status == "On Leave" }

                StatusCountBadge("Present: $presentCount", Color(0xFF16A34A), Color(0xFFDCFCE7), Modifier.weight(1f))
                StatusCountBadge("Absent: $absentCount", Color(0xFFDC2626), Color(0xFFFEE2E2), Modifier.weight(1f))
                StatusCountBadge("Late: $lateCount", Color(0xFFD97706), Color(0xFFFEF3C7), Modifier.weight(1f))
                StatusCountBadge("Excused: $excusedCount", Color(0xFF4F46E5), Color(0xFFEEF2FF), Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (historyAttendanceRecords.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.EventBusy, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No attendance recorded for date $historyDate.",
                            color = Color.Gray,
                            fontSize = 13.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(historyAttendanceRecords, key = { it.id }) { record ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(text = record.studentName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text(
                                            text = "Recorded by: ${record.recordedBy} • Shift: ${record.shift}",
                                            fontSize = 11.sp,
                                            color = Color.Gray
                                        )
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = when (record.status) {
                                            "Present" -> Color(0xFFDCFCE7)
                                            "Absent" -> Color(0xFFFEE2E2)
                                            "Late" -> Color(0xFFFEF3C7)
                                            else -> Color(0xFFEEF2FF)
                                        }
                                    ) {
                                        Text(
                                            text = record.status,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = when (record.status) {
                                                "Present" -> Color(0xFF15803D)
                                                "Absent" -> Color(0xFFB91C1C)
                                                "Late" -> Color(0xFFB45309)
                                                else -> Color(0xFF4338CA)
                                            },
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Quick Status Change on History
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    listOf("Present", "Absent", "Late", "Excused").forEach { st ->
                                        val isCurrent = record.status == st
                                        FilterChip(
                                            selected = isCurrent,
                                            onClick = {
                                                if (!isCurrent) {
                                                    teacherViewModel.updateAttendanceRecord(record.copy(status = st), teacherUsername)
                                                }
                                            },
                                            label = { Text(st, fontSize = 10.sp) },
                                            modifier = Modifier.weight(1f)
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
}

@Composable
fun StatusCountBadge(label: String, textColor: Color, bgColor: Color, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = bgColor,
        modifier = modifier
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = textColor,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 4.dp, horizontal = 2.dp)
        )
    }
}

// -------------------------------------------------------------
// 4. Fees Tab (Paid & Unpaid Click-to-View + Fee History)
// -------------------------------------------------------------
@Composable
fun TeacherFeesTab(
    allFees: List<FeePaymentEntity>,
    students: List<StudentEntity>,
    language: AppLanguage,
    teacherUsername: String,
    teacherViewModel: TeacherViewModel,
    onViewReceipt: (FeePaymentEntity) -> Unit
) {
    var feeTab by remember { mutableStateOf(0) } // 0: Unpaid Fees, 1: Paid Fees, 2: Fee History
    var selectedFeeForDetails by remember { mutableStateOf<FeePaymentEntity?>(null) }
    var historyMonthFilter by remember { mutableStateOf("All") }
    var searchQuery by remember { mutableStateOf("") }

    val unpaidFees = remember(allFees) { allFees.filter { it.status != "Paid" } }
    val paidFees = remember(allFees) { allFees.filter { it.status == "Paid" } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Tab Row: Unpaid, Paid, Fee History
        TabRow(
            selectedTabIndex = feeTab,
            containerColor = appCardColor(),
            contentColor = IafEmerald,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
        ) {
            Tab(
                selected = feeTab == 0,
                onClick = { feeTab = 0 },
                text = {
                    Text(
                        text = "Unpaid (${unpaidFees.size})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (feeTab == 0) Color(0xFFDC2626) else Color.Gray
                    )
                },
                modifier = Modifier.testTag("fees_unpaid_tab")
            )
            Tab(
                selected = feeTab == 1,
                onClick = { feeTab = 1 },
                text = {
                    Text(
                        text = "Paid (${paidFees.size})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (feeTab == 1) Color(0xFF16A34A) else Color.Gray
                    )
                },
                modifier = Modifier.testTag("fees_paid_tab")
            )
            Tab(
                selected = feeTab == 2,
                onClick = { feeTab = 2 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Fee History",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                modifier = Modifier.testTag("fees_history_tab")
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        when (feeTab) {
            0 -> {
                // UNPAID FEES
                if (unpaidFees.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(52.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("All student fees are fully paid!", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF15803D))
                            Text("No unpaid invoices outstanding at this time.", fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                } else {
                    Text(
                        text = "Click any student name to view complete fee details & mark Paid/Unpaid:",
                        fontSize = 11.sp,
                        color = Color.DarkGray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(unpaidFees, key = { it.id }) { fee ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedFeeForDetails = fee },
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = appCardColor()),
                                border = BorderStroke(1.dp, appBorderColor()),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = fee.studentName, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = appTextColor())
                                        Text(
                                            text = "${fee.instituteStudentCode} • ${fee.invoiceMonth}",
                                            fontSize = 12.sp,
                                            color = appSubtextColor()
                                        )
                                        Text(
                                            text = "Tap to review fee details & mark paid",
                                            fontSize = 11.sp,
                                            color = IafEmerald,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "PKR %.0f".format(fee.amountDue),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = Color(0xFFDC2626)
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = Color(0xFFFEE2E2)
                                        ) {
                                            Text(
                                                text = "Unpaid",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFFB91C1C),
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            1 -> {
                // PAID FEES
                if (paidFees.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("No paid fee records yet.", color = appSubtextColor(), fontSize = 13.sp)
                    }
                } else {
                    Text(
                        text = "Click student to view receipt, payment details, or adjust status:",
                        fontSize = 11.sp,
                        color = appSubtextColor()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(paidFees, key = { it.id }) { fee ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedFeeForDetails = fee },
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = appCardColor()),
                                border = BorderStroke(1.dp, appBorderColor()),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = fee.studentName, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = appTextColor())
                                        Text(
                                            text = "${fee.invoiceMonth} • Receipt: ${fee.receiptNumber ?: "N/A"}",
                                            fontSize = 12.sp,
                                            color = appSubtextColor()
                                        )
                                        Text(
                                            text = "Method: ${fee.paymentMethod} • Verified",
                                            fontSize = 11.sp,
                                            color = Color(0xFF16A34A),
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "PKR %.0f".format(fee.amountPaid),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = Color(0xFF16A34A)
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = Color(0xFFDCFCE7)
                                        ) {
                                            Text(
                                                text = "Paid",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF15803D),
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            2 -> {
                // FEE HISTORY OPTION
                val uniqueMonths = remember(allFees) {
                    listOf("All") + allFees.map { it.invoiceMonth }.distinct()
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = appCardColor()),
                    border = BorderStroke(1.dp, appBorderColor()),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(text = "Fee History Ledger Filter", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = appTextColor())
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search by student name or ID...", fontSize = 12.sp) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Month filters
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            uniqueMonths.take(4).forEach { month ->
                                FilterChip(
                                    selected = historyMonthFilter == month,
                                    onClick = { historyMonthFilter = month },
                                    label = { Text(month, fontSize = 10.sp) }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                val filteredHistory = remember(allFees, historyMonthFilter, searchQuery) {
                    allFees.filter { fee ->
                        val matchesMonth = historyMonthFilter == "All" || fee.invoiceMonth == historyMonthFilter
                        val matchesSearch = searchQuery.isBlank() ||
                                fee.studentName.contains(searchQuery, ignoreCase = true) ||
                                fee.instituteStudentCode.contains(searchQuery, ignoreCase = true)
                        matchesMonth && matchesSearch
                    }
                }

                val totalCollected = filteredHistory.filter { it.status == "Paid" }.sumOf { it.amountPaid }
                val totalUnpaid = filteredHistory.filter { it.status != "Paid" }.sumOf { it.amountDue }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatusCountBadge("Collected: PKR %.0f".format(totalCollected), Color(0xFF16A34A), Color(0xFFDCFCE7), Modifier.weight(1f))
                    StatusCountBadge("Outstanding: PKR %.0f".format(totalUnpaid), Color(0xFFDC2626), Color(0xFFFEE2E2), Modifier.weight(1f))
                    StatusCountBadge("Total Records: ${filteredHistory.size}", Color(0xFF1E293B), Color(0xFFF1F5F9), Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (filteredHistory.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("No fee history found for selected criteria.", color = Color.Gray, fontSize = 13.sp)
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(filteredHistory, key = { it.id }) { fee ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedFeeForDetails = fee },
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = fee.studentName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text(text = "${fee.instituteStudentCode} • ${fee.invoiceMonth}", fontSize = 11.sp, color = Color.Gray)
                                        if (fee.status == "Paid") {
                                            Text(text = "Receipt: ${fee.receiptNumber ?: "Verified"}", fontSize = 11.sp, color = Color(0xFF16A34A), fontWeight = FontWeight.SemiBold)
                                        }
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "PKR %.0f".format(if (fee.status == "Paid") fee.amountPaid else fee.amountDue),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = if (fee.status == "Paid") Color(0xFF16A34A) else Color(0xFFDC2626)
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = if (fee.status == "Paid") Color(0xFFDCFCE7) else Color(0xFFFEE2E2)
                                        ) {
                                            Text(
                                                text = fee.status,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (fee.status == "Paid") Color(0xFF15803D) else Color(0xFFB91C1C),
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
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
    }

    // Modal Dialog when Student is clicked to show fee details & mark Paid/Unpaid
    selectedFeeForDetails?.let { fee ->
        val associatedStudent = students.find { it.id == fee.studentId }
        var paymentMethodInput by remember { mutableStateOf(fee.paymentMethod) }

        AlertDialog(
            onDismissRequest = { selectedFeeForDetails = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (fee.status == "Paid") Icons.Default.CheckCircle else Icons.Default.Payment,
                        contentDescription = null,
                        tint = if (fee.status == "Paid") Color(0xFF16A34A) else Color(0xFFDC2626)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Student Fee Details",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFF8FAFC),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(text = fee.studentName, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1E293B))
                            associatedStudent?.let { stu ->
                                Text(text = "Father: ${stu.fatherName} • Contact: ${stu.phone}", fontSize = 12.sp, color = Color.Gray)
                                Text(text = "Program: ${stu.program} • Class: ${stu.className}", fontSize = 12.sp, color = Color.Gray)
                                Text(text = "Roll: ${stu.rollNumber ?: "N/A"} • ID: ${stu.instituteStudentCode}", fontSize = 12.sp, color = IafEmerald, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Invoice Month:", fontSize = 13.sp, color = Color.Gray)
                        Text(text = fee.invoiceMonth, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Net Amount Due:", fontSize = 13.sp, color = Color.Gray)
                        Text(text = "PKR %.2f".format(fee.amountDue), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Current Status:", fontSize = 13.sp, color = Color.Gray)
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = if (fee.status == "Paid") Color(0xFFDCFCE7) else Color(0xFFFEE2E2)
                        ) {
                            Text(
                                text = fee.status,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (fee.status == "Paid") Color(0xFF15803D) else Color(0xFFB91C1C),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }

                    if (fee.status == "Paid") {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Receipt Number:", fontSize = 13.sp, color = Color.Gray)
                            Text(text = fee.receiptNumber ?: "N/A", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF16A34A))
                        }
                        fee.verifiedBy?.let { verifier ->
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Verified By:", fontSize = 13.sp, color = Color.Gray)
                                Text(text = verifier, fontSize = 12.sp)
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(text = "Select Payment Method:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("Cash", "EasyPaisa", "JazzCash", "Bank").forEach { method ->
                                FilterChip(
                                    selected = paymentMethodInput == method,
                                    onClick = { paymentMethodInput = method },
                                    label = { Text(method, fontSize = 11.sp) }
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                if (fee.status != "Paid") {
                    Button(
                        onClick = {
                            teacherViewModel.setFeeStatusDirectly(fee.id, "Paid", teacherUsername)
                            selectedFeeForDetails = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                        modifier = Modifier.testTag("confirm_mark_paid_btn")
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Mark as Paid")
                    }
                } else {
                    Row {
                        OutlinedButton(
                            onClick = {
                                onViewReceipt(fee)
                                selectedFeeForDetails = null
                            },
                            modifier = Modifier.testTag("view_receipt_btn")
                        ) {
                            Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("View Receipt", fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                teacherViewModel.setFeeStatusDirectly(fee.id, "Unpaid", teacherUsername)
                                selectedFeeForDetails = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                            modifier = Modifier.testTag("confirm_mark_unpaid_btn")
                        ) {
                            Text("Mark as Unpaid", fontSize = 12.sp)
                        }
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedFeeForDetails = null }) {
                    Text("Close")
                }
            }
        )
    }
}

// -------------------------------------------------------------
// 5. More Menu Hub Tab
// -------------------------------------------------------------
@Composable
fun TeacherMoreMenuTab(
    pendingLinksCount: Int,
    pendingLeaveCount: Int,
    retentionCount: Int,
    language: AppLanguage,
    onSelectSubmenu: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "Institute Operations & Management",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1E293B)
        )
        Spacer(modifier = Modifier.height(12.dp))

        MoreMenuItem(
            title = Strings.get("quran", language),
            subtitle = "Sabaq, Sabqi, Manzil & Tajweed progress records",
            icon = Icons.Default.MenuBook,
            onClick = { onSelectSubmenu("quran") }
        )

        MoreMenuItem(
            title = Strings.get("requests", language),
            subtitle = "Verify and approve parent-student link requests",
            icon = Icons.Default.Link,
            badge = if (pendingLinksCount > 0) "$pendingLinksCount Pending" else null,
            badgeColor = Color(0xFFD97706),
            onClick = { onSelectSubmenu("links") }
        )

        MoreMenuItem(
            title = Strings.get("leave", language),
            subtitle = "Review student absence and leave requests",
            icon = Icons.Default.EventBusy,
            badge = if (pendingLeaveCount > 0) "$pendingLeaveCount Pending" else null,
            badgeColor = Color(0xFFD97706),
            onClick = { onSelectSubmenu("leave") }
        )

        MoreMenuItem(
            title = Strings.get("shifts", language),
            subtitle = "Configure Sunrise, Morning, Afternoon & Evening shifts",
            icon = Icons.Default.Schedule,
            onClick = { onSelectSubmenu("shifts") }
        )

        MoreMenuItem(
            title = "Weekly Operating Days (Mon–Sun)",
            subtitle = "Configure days ON/OFF, edit day names, set class/off schedules",
            icon = Icons.Default.CalendarMonth,
            onClick = { onSelectSubmenu("weekly_days") }
        )

        MoreMenuItem(
            title = Strings.get("prayers", language),
            subtitle = "Five daily prayer times (Fajr, Dhuhr, Asr, Maghrib, Isha)",
            icon = Icons.Default.Mosque,
            onClick = { onSelectSubmenu("prayers") }
        )

        MoreMenuItem(
            title = Strings.get("announcements", language),
            subtitle = "Broadcast notices to programs, classes, or all parents",
            icon = Icons.Default.Campaign,
            onClick = { onSelectSubmenu("announcements") }
        )

        MoreMenuItem(
            title = Strings.get("social_links", language),
            subtitle = "Official WhatsApp Group, Facebook, YouTube & Instagram URLs",
            icon = Icons.Default.Share,
            onClick = { onSelectSubmenu("social") }
        )

        MoreMenuItem(
            title = Strings.get("retention", language),
            subtitle = "Review expired records. Strict teacher deletion approvals only",
            icon = Icons.Default.Policy,
            badge = if (retentionCount > 0) "$retentionCount Reviews" else null,
            badgeColor = Color(0xFFDC2626),
            onClick = { onSelectSubmenu("retention") }
        )

        MoreMenuItem(
            title = Strings.get("reports", language),
            subtitle = "Daily/monthly attendance, Quran learning & fee reports",
            icon = Icons.Default.Assessment,
            onClick = { onSelectSubmenu("reports") }
        )

        MoreMenuItem(
            title = Strings.get("audit_log", language),
            subtitle = "Immutable audit log of all teacher actions & verifications",
            icon = Icons.Default.History,
            onClick = { onSelectSubmenu("audit") }
        )

        MoreMenuItem(
            title = "Teacher Profile & Security Settings",
            subtitle = "Change institute authorization code, view credentials",
            icon = Icons.Default.AdminPanelSettings,
            onClick = { onSelectSubmenu("profile") }
        )
    }
}

@Composable
fun MoreMenuItem(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    badge: String? = null,
    badgeColor: Color = Color(0xFFD97706),
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFE4F4EA)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = IafEmerald, modifier = Modifier.size(22.dp))
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                Text(text = subtitle, fontSize = 11.sp, color = Color(0xFF64748B))
            }

            if (badge != null) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = badgeColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = badge,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = badgeColor,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
            }

            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp))
        }
    }
}

// -------------------------------------------------------------
// Subscreens for More Navigation
// -------------------------------------------------------------
@Composable
fun TeacherMoreSubscreen(
    submenu: String,
    students: List<StudentEntity>,
    pendingLinks: List<ParentStudentLinkEntity>,
    leaveRequests: List<LeaveRequestEntity>,
    shifts: List<ShiftEntity>,
    prayers: List<PrayerTimingEntity>,
    announcements: List<AnnouncementEntity>,
    socialLinks: SocialLinksEntity?,
    retentionReviews: List<RetentionReviewEntity>,
    auditLogs: List<AuditLogEntity>,
    language: AppLanguage,
    teacherUsername: String,
    teacherViewModel: TeacherViewModel,
    onBack: () -> Unit,
    onQuickQuranLog: (StudentEntity) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Subscreen Header with Back Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = when (submenu) {
                    "quran" -> Strings.get("quran", language)
                    "links" -> Strings.get("requests", language)
                    "leave" -> Strings.get("leave", language)
                    "shifts" -> Strings.get("shifts", language)
                    "weekly_days" -> "Weekly Operating Days (Mon–Sun)"
                    "prayers" -> Strings.get("prayers", language)
                    "announcements" -> Strings.get("announcements", language)
                    "social" -> Strings.get("social_links", language)
                    "retention" -> Strings.get("retention", language)
                    "reports" -> Strings.get("reports", language)
                    "audit" -> Strings.get("audit_log", language)
                    "profile" -> "Teacher Profile & Settings"
                    else -> "Details"
                },
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = IafEmerald
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        when (submenu) {
            "links" -> {
                if (pendingLinks.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No pending parent link requests.", color = Color.Gray)
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(pendingLinks, key = { it.id }) { link ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = "Parent: ${link.parentUsername}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "Requested Link to: ${link.studentName} (${link.instituteStudentCode})",
                                        fontSize = 13.sp,
                                        color = IafEmerald,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        OutlinedButton(
                                            onClick = { teacherViewModel.reviewLinkRequest(link.id, false, "Rejected by administration", teacherUsername) },
                                            modifier = Modifier.height(34.dp)
                                        ) {
                                            Text(Strings.get("reject", language), color = Color(0xFFDC2626), fontSize = 11.sp)
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Button(
                                            onClick = { teacherViewModel.reviewLinkRequest(link.id, true, null, teacherUsername) },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16803D)),
                                            modifier = Modifier.height(34.dp)
                                        ) {
                                            Text(Strings.get("approve", language), fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            "leave" -> {
                if (leaveRequests.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No pending leave requests.", color = Color.Gray)
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(leaveRequests, key = { it.id }) { req ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(text = "Student: ${req.studentName}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text(text = "Dates: ${req.startDate} to ${req.endDate}", fontSize = 12.sp, color = Color(0xFFD97706), fontWeight = FontWeight.SemiBold)
                                    Text(text = "Reason: ${req.reason}", fontSize = 12.sp, color = Color.DarkGray)
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                        OutlinedButton(
                                            onClick = { teacherViewModel.reviewLeaveRequest(req.id, false, "Not approved", teacherUsername) },
                                            modifier = Modifier.height(34.dp)
                                        ) {
                                            Text(Strings.get("reject", language), color = Color(0xFFDC2626), fontSize = 11.sp)
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Button(
                                            onClick = { teacherViewModel.reviewLeaveRequest(req.id, true, "Approved", teacherUsername) },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16803D)),
                                            modifier = Modifier.height(34.dp)
                                        ) {
                                            Text(Strings.get("approve", language), fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            "quran" -> {
                var quranTab by remember { mutableStateOf(0) } // 0: Log Sabak, 1: Sabak History
                val allQuranRecords by teacherViewModel.allQuranRecords.collectAsState()
                var selectedSubjectFilter by remember { mutableStateOf("All") } // "All", "Hifz", "Qaida", "Nazra"
                var selectedHistoryStudent by remember { mutableStateOf<StudentEntity?>(null) }
                var selectedFreqFilter by remember { mutableStateOf("All") }
                var sabakSearch by remember { mutableStateOf("") }

                TabRow(
                    selectedTabIndex = quranTab,
                    containerColor = appCardColor(),
                    contentColor = IafEmerald,
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp))
                ) {
                    Tab(
                        selected = quranTab == 0,
                        onClick = {
                            quranTab = 0
                            selectedHistoryStudent = null
                        },
                        text = { Text("Log Sabak", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                    )
                    Tab(
                        selected = quranTab == 1,
                        onClick = { quranTab = 1 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Sabak History (${allQuranRecords.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (quranTab == 0) {
                    // Log Sabak tab
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFE4F4EA),
                        border = BorderStroke(1.dp, IafEmerald),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "Quran Learning Tracker (Daily, Weekly, Bi-Weekly & Monthly)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = IafEmerald
                            )
                            Text(
                                text = "• Hifz: Log Sabaq (New), Sabqi (Recent), and Manzil (Cumulative).\n• Qaida & Nazra: Log Sabaq (Current lesson) and tajweed progress.",
                                fontSize = 11.sp,
                                color = Color.DarkGray
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Subject / Program filter option: All, Hifz, Qaida, Nazra
                    Text("Select Subject / Program:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1E293B))
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            "All" to "All (${students.size})",
                            "Hifz" to "Hifz (${students.count { it.program.contains("Hifz", ignoreCase = true) }})",
                            "Qaida" to "Qaida (${students.count { it.program.contains("Qaida", ignoreCase = true) }})",
                            "Nazra" to "Nazra (${students.count { it.program.contains("Nazra", ignoreCase = true) }})"
                        ).forEach { (key, label) ->
                            FilterChip(
                                selected = selectedSubjectFilter == key,
                                onClick = { selectedSubjectFilter = key },
                                label = { Text(label, fontSize = 11.sp) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Filter students related to the selected subject
                    val filteredStudentsForLog = remember(students, selectedSubjectFilter) {
                        students.filter { student ->
                            when (selectedSubjectFilter) {
                                "Hifz" -> student.program.contains("Hifz", ignoreCase = true)
                                "Qaida" -> student.program.contains("Qaida", ignoreCase = true)
                                "Nazra" -> student.program.contains("Nazra", ignoreCase = true)
                                else -> true
                            }
                        }
                    }

                    if (filteredStudentsForLog.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (selectedSubjectFilter == "All") "No students enrolled yet." else "No students found for subject '$selectedSubjectFilter'.",
                                color = Color.Gray,
                                fontSize = 13.sp
                            )
                        }
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(filteredStudentsForLog, key = { it.id }) { student ->
                                val isHifzStudent = student.program.contains("Hifz", ignoreCase = true)
                                val isQaidaStudent = student.program.contains("Qaida", ignoreCase = true)
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onQuickQuranLog(student) },
                                    shape = RoundedCornerShape(10.dp),
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
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(text = student.fullName, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = appTextColor())
                                            Text(
                                                text = when {
                                                    isHifzStudent -> "Hifz • Sabaq, Sabqi, Manzil"
                                                    isQaidaStudent -> "Noorani Qaida • Sabaq"
                                                    else -> "Nazra Quran • Sabaq"
                                                },
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = when {
                                                    isHifzStudent -> IafEmerald
                                                    isQaidaStudent -> Color(0xFFD97706)
                                                    else -> Color(0xFF2563EB)
                                                }
                                            )
                                            Text(text = "Class: ${student.className} • ID: ${student.instituteStudentCode}", fontSize = 11.sp, color = appSubtextColor())
                                        }
                                        Button(
                                            onClick = { onQuickQuranLog(student) },
                                            colors = ButtonDefaults.buttonColors(containerColor = IafEmerald),
                                            shape = RoundedCornerShape(6.dp),
                                            modifier = Modifier.height(34.dp)
                                        ) {
                                            Icon(Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Log Sabak", fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // Sabak History View:
                    // Only show names in history sabak. When click the names it will show data.
                    if (selectedHistoryStudent == null) {
                        // Student Names List Only View
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Schedule, contentDescription = null, tint = Color(0xFF92400E), modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Sabak History — Click Student Name to View Data", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF78350F))
                                }
                                Text(
                                    text = "Click any student's name below to inspect their complete logged Sabak records, tajweed evaluation, and teacher remarks.",
                                    fontSize = 11.sp,
                                    color = Color(0xFF92400E),
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Search by student name
                        OutlinedTextField(
                            value = sabakSearch,
                            onValueChange = { sabakSearch = it },
                            placeholder = { Text("Search student name...") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Subject Filter Chips for student list
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf("All", "Hifz", "Qaida", "Nazra").forEach { subj ->
                                FilterChip(
                                    selected = selectedSubjectFilter == subj,
                                    onClick = { selectedSubjectFilter = subj },
                                    label = { Text(subj, fontSize = 11.sp) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        val filteredStudentsForHistory = remember(students, selectedSubjectFilter, sabakSearch) {
                            students.filter { s ->
                                val matchesSubject = when (selectedSubjectFilter) {
                                    "Hifz" -> s.program.contains("Hifz", ignoreCase = true)
                                    "Qaida" -> s.program.contains("Qaida", ignoreCase = true)
                                    "Nazra" -> s.program.contains("Nazra", ignoreCase = true)
                                    else -> true
                                }
                                val matchesSearch = sabakSearch.isBlank() || s.fullName.contains(sabakSearch, ignoreCase = true)
                                matchesSubject && matchesSearch
                            }
                        }

                        if (filteredStudentsForHistory.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                                Text("No students found.", color = Color.Gray, fontSize = 13.sp)
                            }
                        } else {
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(filteredStudentsForHistory, key = { it.id }) { student ->
                                    val count = allQuranRecords.count { it.studentId == student.id }
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { selectedHistoryStudent = student },
                                        shape = RoundedCornerShape(10.dp),
                                        colors = CardDefaults.cardColors(containerColor = appCardColor()),
                                        border = BorderStroke(1.dp, appBorderColor()),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(14.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(40.dp)
                                                        .clip(CircleShape)
                                                        .background(Color(0xFFE4F4EA)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(Icons.Default.Person, contentDescription = null, tint = IafEmerald, modifier = Modifier.size(24.dp))
                                                }
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Column {
                                                    Text(
                                                        text = student.fullName,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 15.sp,
                                                        color = appTextColor()
                                                    )
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                    ) {
                                                        Surface(
                                                            shape = RoundedCornerShape(4.dp),
                                                            color = when {
                                                                student.program.contains("Hifz", true) -> Color(0xFFDCFCE7)
                                                                student.program.contains("Qaida", true) -> Color(0xFFFEF3C7)
                                                                else -> Color(0xFFDBEAFE)
                                                            }
                                                        ) {
                                                            Text(
                                                                text = student.program,
                                                                fontSize = 10.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                color = when {
                                                                    student.program.contains("Hifz", true) -> Color(0xFF166534)
                                                                    student.program.contains("Qaida", true) -> Color(0xFF92400E)
                                                                    else -> Color(0xFF1E40AF)
                                                                },
                                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                            )
                                                        }
                                                        Text(
                                                            text = "$count Logged Records",
                                                            fontSize = 11.sp,
                                                            color = if (count > 0) IafEmerald else Color.Gray,
                                                            fontWeight = if (count > 0) FontWeight.SemiBold else FontWeight.Normal
                                                        )
                                                    }
                                                }
                                            }

                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text("View Data", fontSize = 11.sp, color = IafEmerald, fontWeight = FontWeight.SemiBold)
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Icon(Icons.Default.ArrowForward, contentDescription = "View", tint = IafEmerald, modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        // Detailed Data View for Selected Student
                        val student = selectedHistoryStudent!!
                        val studentRecords = remember(allQuranRecords, student, selectedFreqFilter) {
                            allQuranRecords.filter { record ->
                                record.studentId == student.id &&
                                        (selectedFreqFilter == "All" || record.recordType == selectedFreqFilter)
                            }
                        }

                        Column {
                            // Back Button and Student Header
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = appCardColor(),
                                border = BorderStroke(1.dp, appBorderColor()),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        TextButton(
                                            onClick = { selectedHistoryStudent = null },
                                            contentPadding = PaddingValues(0.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = IafEmerald, modifier = Modifier.size(18.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Back to Student List", fontSize = 12.sp, color = IafEmerald, fontWeight = FontWeight.Bold)
                                            }
                                        }

                                        Button(
                                            onClick = { onQuickQuranLog(student) },
                                            colors = ButtonDefaults.buttonColors(containerColor = IafEmerald),
                                            shape = RoundedCornerShape(6.dp),
                                            modifier = Modifier.height(30.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp)
                                        ) {
                                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(2.dp))
                                            Text("Log Sabak", fontSize = 11.sp)
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = student.fullName,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = appTextColor()
                                    )
                                    Text(
                                        text = "${student.program} • Class: ${student.className} • ID: ${student.instituteStudentCode}",
                                        fontSize = 12.sp,
                                        color = appSubtextColor()
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Frequency Filter for this student's records
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                listOf("All", "Daily", "Weekly", "Bi-Weekly", "Monthly").forEach { freq ->
                                    FilterChip(
                                        selected = selectedFreqFilter == freq,
                                        onClick = { selectedFreqFilter = freq },
                                        label = { Text(freq, fontSize = 10.sp) }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            if (studentRecords.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(Icons.Default.MenuBook, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(40.dp))
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "No Sabak records found for ${student.fullName} under '$selectedFreqFilter'.",
                                            color = Color.Gray,
                                            fontSize = 13.sp,
                                            textAlign = TextAlign.Center
                                        )
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Button(
                                            onClick = { onQuickQuranLog(student) },
                                            colors = ButtonDefaults.buttonColors(containerColor = IafEmerald)
                                        ) {
                                            Text("Log First Sabak")
                                        }
                                    }
                                }
                            } else {
                                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(studentRecords, key = { it.id }) { record ->
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(10.dp),
                                            colors = CardDefaults.cardColors(containerColor = appCardColor()),
                                            border = BorderStroke(1.dp, appBorderColor()),
                                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                                        ) {
                                            Column(modifier = Modifier.padding(12.dp)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Column {
                                                        Text(text = "Date: ${record.date}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = appTextColor())
                                                        Text(text = "Recorded by: ${record.recordedBy}", fontSize = 11.sp, color = appSubtextColor())
                                                    }
                                                    Surface(
                                                        shape = RoundedCornerShape(6.dp),
                                                        color = when (record.recordType) {
                                                            "Daily" -> Color(0xFFDCFCE7)
                                                            "Weekly" -> Color(0xFFDBEAFE)
                                                            "Bi-Weekly" -> Color(0xFFFEF3C7)
                                                            else -> Color(0xFFF3E8FF)
                                                        }
                                                    ) {
                                                        Text(
                                                            text = record.recordType,
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = when (record.recordType) {
                                                                "Daily" -> Color(0xFF166534)
                                                                "Weekly" -> Color(0xFF1E40AF)
                                                                "Bi-Weekly" -> Color(0xFF92400E)
                                                                else -> Color(0xFF6B21A8)
                                                            },
                                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                                        )
                                                    }
                                                }

                                                Spacer(modifier = Modifier.height(8.dp))

                                                if (record.program.contains("Hifz", ignoreCase = true)) {
                                                    Row(modifier = Modifier.fillMaxWidth()) {
                                                        Column(modifier = Modifier.weight(1f)) {
                                                            Text("Sabaq (New):", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = IafEmerald)
                                                            Text(record.sabaq ?: record.versesOrLesson, fontSize = 12.sp)
                                                        }
                                                        Column(modifier = Modifier.weight(1f)) {
                                                            Text("Sabqi (Recent):", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD97706))
                                                            Text(record.sabqi ?: "N/A", fontSize = 12.sp)
                                                        }
                                                        Column(modifier = Modifier.weight(1f)) {
                                                            Text("Manzil (Revision):", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2563EB))
                                                            Text(record.manzil ?: "N/A", fontSize = 12.sp)
                                                        }
                                                    }
                                                } else {
                                                    Text("Sabaq Lesson: ${record.sabaq ?: record.versesOrLesson}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                                }

                                                if (record.juz != null || record.surah != null) {
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Text(
                                                        text = "Para/Juz: ${record.juz ?: "-"} • Surah: ${record.surah ?: "-"}",
                                                        fontSize = 11.sp,
                                                        color = Color.DarkGray
                                                    )
                                                }

                                                Spacer(modifier = Modifier.height(6.dp))
                                                Text(
                                                    text = "Fluency: ${record.fluency} • Tajweed: ${record.tajweedPronunciation} • Mistakes: ${record.mistakes}",
                                                    fontSize = 11.sp,
                                                    color = Color.DarkGray
                                                )

                                                record.teacherComments?.let { comments ->
                                                    if (comments.isNotBlank()) {
                                                        Spacer(modifier = Modifier.height(4.dp))
                                                        Text(text = "Comments: $comments", fontSize = 11.sp, color = Color.Gray)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            "shifts" -> {
                var editingShift by remember { mutableStateOf<ShiftEntity?>(null) }
                var showAddShiftDialog by remember { mutableStateOf(false) }

                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Institute Shift Timings", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Shift timings and assigned rooms are fully changeable.", fontSize = 11.sp, color = Color.Gray)
                        }
                        Button(
                            onClick = { showAddShiftDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = IafEmerald),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(34.dp).testTag("add_shift_btn")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Shift", fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(shifts) { shift ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(text = shift.name, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = IafEmerald)
                                        Row {
                                            IconButton(
                                                onClick = { editingShift = shift },
                                                modifier = Modifier.size(32.dp).testTag("edit_shift_${shift.id}")
                                            ) {
                                                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = IafEmerald, modifier = Modifier.size(18.dp))
                                            }
                                            IconButton(
                                                onClick = { teacherViewModel.deleteShift(shift.id, teacherUsername) },
                                                modifier = Modifier.size(32.dp).testTag("delete_shift_${shift.id}")
                                            ) {
                                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFDC2626), modifier = Modifier.size(18.dp))
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(text = "Timings: ${shift.startTime} - ${shift.endTime}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                    Text(text = "Days: ${shift.days} • Room: ${shift.room}", fontSize = 12.sp, color = Color.DarkGray)
                                    Text(text = "Class: ${shift.assignedClass} • Ustadh: ${shift.assignedTeacher}", fontSize = 12.sp, color = Color.Gray)
                                }
                            }
                        }
                    }
                }

                // Edit Shift Dialog
                editingShift?.let { shift ->
                    var editName by remember { mutableStateOf(shift.name) }
                    var editStart by remember { mutableStateOf(shift.startTime) }
                    var editEnd by remember { mutableStateOf(shift.endTime) }
                    var editDays by remember { mutableStateOf(shift.days) }
                    var editClass by remember { mutableStateOf(shift.assignedClass) }
                    var editRoom by remember { mutableStateOf(shift.room) }
                    var editTeacher by remember { mutableStateOf(shift.assignedTeacher) }

                    AlertDialog(
                        onDismissRequest = { editingShift = null },
                        title = { Text("Edit Shift Timings", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
                        text = {
                            Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
                                OutlinedTextField(value = editName, onValueChange = { editName = it }, label = { Text("Shift Name") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(value = editStart, onValueChange = { editStart = it }, label = { Text("Start Time (e.g. 08:00 AM)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(value = editEnd, onValueChange = { editEnd = it }, label = { Text("End Time (e.g. 12:30 PM)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(value = editDays, onValueChange = { editDays = it }, label = { Text("Operating Days") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(value = editRoom, onValueChange = { editRoom = it }, label = { Text("Room / Hall") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(value = editClass, onValueChange = { editClass = it }, label = { Text("Assigned Class") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                            }
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    val updated = shift.copy(
                                        name = editName,
                                        startTime = editStart,
                                        endTime = editEnd,
                                        days = editDays,
                                        room = editRoom,
                                        assignedClass = editClass,
                                        assignedTeacher = editTeacher
                                    )
                                    teacherViewModel.updateShift(updated, teacherUsername)
                                    editingShift = null
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = IafEmerald)
                            ) {
                                Text("Save Changes")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { editingShift = null }) { Text("Cancel") }
                        }
                    )
                }

                // Add Shift Dialog
                if (showAddShiftDialog) {
                    var newName by remember { mutableStateOf("") }
                    var newStart by remember { mutableStateOf("08:00 AM") }
                    var newEnd by remember { mutableStateOf("12:00 PM") }
                    var newDays by remember { mutableStateOf("Mon-Sat") }
                    var newRoom by remember { mutableStateOf("Room 1") }
                    var newClass by remember { mutableStateOf("General") }

                    AlertDialog(
                        onDismissRequest = { showAddShiftDialog = false },
                        title = { Text("Add New Shift", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
                        text = {
                            Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
                                OutlinedTextField(value = newName, onValueChange = { newName = it }, label = { Text("Shift Name (e.g. Evening Shift)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(value = newStart, onValueChange = { newStart = it }, label = { Text("Start Time") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(value = newEnd, onValueChange = { newEnd = it }, label = { Text("End Time") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(value = newDays, onValueChange = { newDays = it }, label = { Text("Days") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(value = newRoom, onValueChange = { newRoom = it }, label = { Text("Room / Hall") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(value = newClass, onValueChange = { newClass = it }, label = { Text("Class") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                            }
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    if (newName.isNotBlank()) {
                                        teacherViewModel.addShift(newName, newStart, newEnd, newDays, newClass, teacherUsername, newRoom, teacherUsername)
                                        showAddShiftDialog = false
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = IafEmerald)
                            ) {
                                Text("Create Shift")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showAddShiftDialog = false }) { Text("Cancel") }
                        }
                    )
                }
            }

            "weekly_days" -> {
                val weeklyDays by teacherViewModel.weeklyDays.collectAsState()
                var editingDay by remember { mutableStateOf<InstituteWeeklyDayEntity?>(null) }
                var editNameInput by remember { mutableStateOf("") }
                var editIsOnInput by remember { mutableStateOf(true) }
                var editNotesInput by remember { mutableStateOf("") }

                val onCount = weeklyDays.count { it.isOn }
                val offCount = weeklyDays.size - onCount

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    // Summary Card
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF0FDF4),
                        border = BorderStroke(1.dp, Color(0xFFBBF7D0)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = IafEmerald)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Weekly Operating Days (Monday to Sunday)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = IafEmerald
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Configure which days the institute is ON (open for classes) and OFF (weekend/holiday). Days can be renamed and toggled at any time. When a day is OFF, attendance and daily Quran logs are marked as OFF for that day.",
                                fontSize = 11.sp,
                                color = Color.DarkGray
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFFDCFCE7)
                                ) {
                                    Text(
                                        text = "$onCount Working Days (ON)",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF166534),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFFFEE2E2)
                                ) {
                                    Text(
                                        text = "$offCount Off Days (OFF)",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF991B1B),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    weeklyDays.forEach { day ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (day.isOn) Color.White else Color(0xFFFFF7ED)
                            ),
                            border = BorderStroke(
                                1.dp,
                                if (day.isOn) Color(0xFFE2E8F0) else Color(0xFFFDBA74)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = day.customDayName,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = if (day.isOn) Color(0xFF1E293B) else Color(0xFF9A3412)
                                        )
                                        if (day.customDayName != day.defaultDayName) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "(${day.defaultDayName})",
                                                fontSize = 11.sp,
                                                color = Color.Gray
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = day.timingsOrNotes,
                                        fontSize = 12.sp,
                                        color = Color.DarkGray
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    // Status Badge & Quick Toggle
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (day.isOn) Color(0xFFDCFCE7) else Color(0xFFFEE2E2),
                                        modifier = Modifier.clickable {
                                            teacherViewModel.toggleWeeklyDayStatus(day.dayIndex)
                                        }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = if (day.isOn) "ON (OPEN)" else "OFF (CLOSED)",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (day.isOn) Color(0xFF166534) else Color(0xFF991B1B)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Icon(
                                                imageVector = if (day.isOn) Icons.Default.ToggleOn else Icons.Default.ToggleOff,
                                                contentDescription = "Toggle status",
                                                tint = if (day.isOn) Color(0xFF166534) else Color(0xFF991B1B),
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    IconButton(
                                        onClick = {
                                            editingDay = day
                                            editNameInput = day.customDayName
                                            editIsOnInput = day.isOn
                                            editNotesInput = day.timingsOrNotes
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit Day", tint = IafEmerald, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }
                }

                // Edit Day Dialog
                editingDay?.let { day ->
                    AlertDialog(
                        onDismissRequest = { editingDay = null },
                        title = {
                            Text("Edit Day: ${day.defaultDayName}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        },
                        text = {
                            Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
                                OutlinedTextField(
                                    value = editNameInput,
                                    onValueChange = { editNameInput = it },
                                    label = { Text("Day Display Name (e.g. Monday / پیر)") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )
                                Spacer(modifier = Modifier.height(10.dp))

                                Text("Operating Status:", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    FilterChip(
                                        selected = editIsOnInput,
                                        onClick = { editIsOnInput = true },
                                        label = { Text("ON (Open / Working)") }
                                    )
                                    FilterChip(
                                        selected = !editIsOnInput,
                                        onClick = { editIsOnInput = false },
                                        label = { Text("OFF (Closed / Holiday)") }
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                OutlinedTextField(
                                    value = editNotesInput,
                                    onValueChange = { editNotesInput = it },
                                    label = { Text("Timings or Notes (e.g. 08:00 AM - 01:00 PM)") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )
                            }
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    teacherViewModel.updateWeeklyDay(
                                        dayIndex = day.dayIndex,
                                        customName = editNameInput,
                                        isOn = editIsOnInput,
                                        timingsOrNotes = editNotesInput
                                    )
                                    editingDay = null
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = IafEmerald)
                            ) {
                                Text("Save Changes")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { editingDay = null }) {
                                Text("Cancel")
                            }
                        }
                    )
                }
            }

            "prayers" -> {
                var editingPrayer by remember { mutableStateOf<PrayerTimingEntity?>(null) }

                Column {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE4F4EA)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("Five Daily Nimaz (Prayer) Times", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = IafEmerald)
                            Text("Prayer names and jamat timings are fully changeable by institute administration.", fontSize = 11.sp, color = Color.DarkGray)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(prayers) { prayer ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(text = prayer.prayerName, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1E293B))
                                        Text(text = "Idara Jamat Time", fontSize = 11.sp, color = Color.Gray)
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = Color(0xFFE4F4EA),
                                            border = BorderStroke(1.dp, IafEmerald)
                                        ) {
                                            Text(
                                                text = prayer.timeString,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = IafEmerald,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(8.dp))

                                        IconButton(
                                            onClick = { editingPrayer = prayer },
                                            modifier = Modifier.size(36.dp).testTag("edit_prayer_${prayer.prayerName}")
                                        ) {
                                            Icon(Icons.Default.Edit, contentDescription = "Edit Timing", tint = IafEmerald)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Edit Prayer Timing Dialog
                editingPrayer?.let { prayer ->
                    var editPrayerName by remember { mutableStateOf(prayer.prayerName) }
                    var editTimeString by remember { mutableStateOf(prayer.timeString) }

                    AlertDialog(
                        onDismissRequest = { editingPrayer = null },
                        title = { Text("Change Nimaz / Prayer Time", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
                        text = {
                            Column {
                                OutlinedTextField(
                                    value = editPrayerName,
                                    onValueChange = { editPrayerName = it },
                                    label = { Text("Prayer Name (e.g. Fajr, Jumu'ah)") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                OutlinedTextField(
                                    value = editTimeString,
                                    onValueChange = { editTimeString = it },
                                    label = { Text("Jamat Timing (e.g. 05:15 AM)") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )
                            }
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    val updated = prayer.copy(prayerName = editPrayerName, timeString = editTimeString)
                                    teacherViewModel.updatePrayerTiming(updated, teacherUsername)
                                    editingPrayer = null
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = IafEmerald)
                            ) {
                                Text("Update Timing")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { editingPrayer = null }) { Text("Cancel") }
                        }
                    )
                }
            }

            "retention" -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Gavel, contentDescription = null, tint = Color(0xFF92400E))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Strict Retention Policy", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF92400E))
                        }
                        Text(
                            text = Strings.get("retention_policy_note", language),
                            fontSize = 11.sp,
                            color = Color(0xFF78350F),
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))

                if (retentionReviews.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No records currently pending retention review.", color = Color.Gray)
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(retentionReviews, key = { it.id }) { review ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(text = review.recordType, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFFDC2626))
                                    Text(text = "Student: ${review.studentName} (${review.recordDate})", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                    Text(text = review.description, fontSize = 11.sp, color = Color.DarkGray)
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                        OutlinedButton(
                                            onClick = { teacherViewModel.executeRetentionAction(review.id, "Kept", teacherUsername) },
                                            modifier = Modifier.height(34.dp)
                                        ) {
                                            Text("Keep Record", fontSize = 11.sp)
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Button(
                                            onClick = { teacherViewModel.executeRetentionAction(review.id, "ApprovedDeletion", teacherUsername) },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                                            modifier = Modifier.height(34.dp)
                                        ) {
                                            Text("Approve Permanent Deletion", fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            "reports" -> {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    ReportSummaryCard(
                        title = "Institute Attendance Report",
                        metric = "${students.size} Enrolled Students",
                        details = "Daily summaries, excused absences, and percentages calculated."
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    ReportSummaryCard(
                        title = "Quran Learning & Hifz Progress",
                        metric = "3 Specialized Programs",
                        details = "Noorani Qaida fluency, Nazra recitation, and Hifz Juz tracking."
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    ReportSummaryCard(
                        title = "Financial & Fee Audit Report",
                        metric = "IAF Ledger Active",
                        details = "Teacher-verified payments, confirmed receipts, and outstanding dues."
                    )
                }
            }

            "audit" -> {
                if (auditLogs.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No audit log entries recorded yet.", color = Color.Gray)
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(auditLogs, key = { it.id }) { log ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(text = log.action, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = IafEmerald)
                                        Text(
                                            text = SimpleDateFormat("dd MMM, HH:mm", Locale.US).format(Date(log.timestamp)),
                                            fontSize = 10.sp,
                                            color = Color.Gray
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(text = log.details, fontSize = 11.sp, color = Color.DarkGray)
                                    Text(text = "Performed by: ${log.performedBy}", fontSize = 10.sp, color = Color.Gray)
                                }
                            }
                        }
                    }
                }
            }

            "announcements" -> {
                var announcementTab by remember { mutableStateOf(0) } // 0: Manage Notices, 1: History (Past 1 Month)
                val pastMonthAnnouncements by teacherViewModel.announcementsPastMonth.collectAsState()
                var editingNotice by remember { mutableStateOf<AnnouncementEntity?>(null) }
                var newTitle by remember { mutableStateOf("") }
                var newContent by remember { mutableStateOf("") }
                var target by remember { mutableStateOf("All") }

                TabRow(
                    selectedTabIndex = announcementTab,
                    containerColor = Color.White,
                    contentColor = IafEmerald,
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp))
                ) {
                    Tab(
                        selected = announcementTab == 0,
                        onClick = { announcementTab = 0 },
                        text = { Text("Notices (${announcements.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
                    )
                    Tab(
                        selected = announcementTab == 1,
                        onClick = { announcementTab = 1 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Notice History (Past 1 Month)", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (announcementTab == 0) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("Post New Announcement / Notice", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = newTitle,
                                onValueChange = { newTitle = it },
                                label = { Text("Notice Title *") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = newContent,
                                onValueChange = { newContent = it },
                                label = { Text("Notice Content *") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = {
                                    if (newTitle.isNotBlank()) {
                                        teacherViewModel.createAnnouncement(newTitle, newContent, target, teacherUsername)
                                        newTitle = ""
                                        newContent = ""
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = IafEmerald),
                                modifier = Modifier.align(Alignment.End).testTag("publish_notice_btn")
                            ) {
                                Text("Publish Notice")
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))

                    Text("Active Notices (${announcements.size})", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("Notices can be edited or deleted by teacher.", fontSize = 11.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(8.dp))

                    if (announcements.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                            Text("No announcements published yet.", color = Color.Gray, fontSize = 13.sp)
                        }
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(announcements, key = { it.id }) { item ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(text = item.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.weight(1f))
                                            Row {
                                                IconButton(
                                                    onClick = { editingNotice = item },
                                                    modifier = Modifier.size(32.dp).testTag("edit_notice_${item.id}")
                                                ) {
                                                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = IafEmerald, modifier = Modifier.size(18.dp))
                                                }
                                                IconButton(
                                                    onClick = { teacherViewModel.deleteAnnouncement(item.id, teacherUsername) },
                                                    modifier = Modifier.size(32.dp).testTag("delete_notice_${item.id}")
                                                ) {
                                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFDC2626), modifier = Modifier.size(18.dp))
                                                }
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(text = item.content, fontSize = 12.sp, color = Color.DarkGray)
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(text = "Date: ${item.date} • By: ${item.author} • Target: ${item.targetGroup}", fontSize = 10.sp, color = Color.Gray)
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // NOTICE HISTORY OF PAST 1 MONTH
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE4F4EA)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.History, contentDescription = null, tint = IafEmerald, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Notice History (Past 1 Month)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = IafEmerald)
                            }
                            Text(
                                text = "Showing all notices and circulars issued by Idara Al-Furqan during the past 30 days.",
                                fontSize = 11.sp,
                                color = Color.DarkGray,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    val historyList = if (pastMonthAnnouncements.isNotEmpty()) pastMonthAnnouncements else announcements
                    if (historyList.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                            Text("No announcements recorded in the past 1 month.", color = Color.Gray, fontSize = 13.sp)
                        }
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(historyList, key = { it.id }) { item ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(text = item.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(text = item.content, fontSize = 12.sp, color = Color.DarkGray)
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(text = "Published: ${item.date} • By: ${item.author}", fontSize = 10.sp, color = Color.Gray)
                                    }
                                }
                            }
                        }
                    }
                }

                // Edit Notice Dialog
                editingNotice?.let { notice ->
                    var editTitle by remember { mutableStateOf(notice.title) }
                    var editContent by remember { mutableStateOf(notice.content) }
                    var editTarget by remember { mutableStateOf(notice.targetGroup) }

                    AlertDialog(
                        onDismissRequest = { editingNotice = null },
                        title = { Text("Edit Notice / Circular", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
                        text = {
                            Column {
                                OutlinedTextField(
                                    value = editTitle,
                                    onValueChange = { editTitle = it },
                                    label = { Text("Title") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = editContent,
                                    onValueChange = { editContent = it },
                                    label = { Text("Content") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    if (editTitle.isNotBlank()) {
                                        val updated = notice.copy(title = editTitle, content = editContent, targetGroup = editTarget)
                                        teacherViewModel.updateAnnouncement(updated, teacherUsername)
                                        editingNotice = null
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = IafEmerald)
                            ) {
                                Text("Save Changes")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { editingNotice = null }) { Text("Cancel") }
                        }
                    )
                }
            }

            "social" -> {
                var waUrl by remember { mutableStateOf(socialLinks?.whatsappGroupUrl ?: "") }
                var fbUrl by remember { mutableStateOf(socialLinks?.facebookUrl ?: "") }
                var igUrl by remember { mutableStateOf(socialLinks?.instagramUrl ?: "") }
                var ytUrl by remember { mutableStateOf(socialLinks?.youtubeUrl ?: "") }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Text("Official Institute Social Channels", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Enter the verified URLs for Idara Al-Furqan. Leave empty if unconfigured.", fontSize = 11.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = waUrl,
                            onValueChange = { waUrl = it },
                            label = { Text("WhatsApp Group Link") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = fbUrl,
                            onValueChange = { fbUrl = it },
                            label = { Text("Facebook Official Page") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = igUrl,
                            onValueChange = { igUrl = it },
                            label = { Text("Instagram Profile URL") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = ytUrl,
                            onValueChange = { ytUrl = it },
                            label = { Text("YouTube Channel URL") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                teacherViewModel.updateSocialLinks(
                                    SocialLinksEntity(
                                        id = 1,
                                        whatsappGroupUrl = waUrl,
                                        facebookUrl = fbUrl,
                                        instagramUrl = igUrl,
                                        youtubeUrl = ytUrl
                                    ),
                                    teacherUsername
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = IafEmerald),
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("Save Social Links")
                        }
                    }
                }
            }

            "profile" -> {
                val settings by teacherViewModel.instituteSettings.collectAsState()
                var showChangeCodeDialog by remember { mutableStateOf(false) }
                var isAuthCodeVisible by remember { mutableStateOf(false) }
                var newAuthCodeInput by remember { mutableStateOf("") }

                var showChangeUsernameDialog by remember { mutableStateOf(false) }
                var newUsernameInput by remember { mutableStateOf("") }
                var passVerifyInput by remember { mutableStateOf("") }

                var showChangePasswordDialog by remember { mutableStateOf(false) }
                var currentPassInput by remember { mutableStateOf("") }
                var newPassInput by remember { mutableStateOf("") }

                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    // Profile Overview Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFE4F4EA)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = IafEmerald, modifier = Modifier.size(28.dp))
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(text = "Ustadh Administration", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = IafEmerald)
                                    Text(text = "Logged in as: $teacherUsername", fontSize = 12.sp, color = Color.Gray)
                                    Text(text = "Role: Authorized Teacher / Admin", fontSize = 11.sp, color = Color(0xFF16803D), fontWeight = FontWeight.SemiBold)
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = Color(0xFFF1F5F9))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = "• Primary Campus ID: ${settings?.primaryInstituteId ?: "IAF-2026"}", fontSize = 12.sp, color = Color.DarkGray)
                            Text(text = "• Secondary Branch ID: ${settings?.secondaryInstituteId ?: "IAF-2027"}", fontSize = 12.sp, color = Color.DarkGray)

                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        newUsernameInput = teacherUsername
                                        passVerifyInput = ""
                                        showChangeUsernameDialog = true
                                    },
                                    modifier = Modifier.weight(1f).height(38.dp),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(14.dp), tint = IafEmerald)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Change Username", fontSize = 11.sp, color = IafEmerald)
                                }
                                Button(
                                    onClick = {
                                        currentPassInput = ""
                                        newPassInput = ""
                                        showChangePasswordDialog = true
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = IafEmerald),
                                    modifier = Modifier.weight(1f).height(38.dp),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Change Password", fontSize = 11.sp)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Authorization Code Management Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.5.dp, Color(0xFFD4A017))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Key, contentDescription = null, tint = Color(0xFFD4A017))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(text = "Teacher Authorization Code", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF1E293B))
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "This secret authorization code is required whenever a new teacher or administrator registers for this institute. You can change this code at any time to preserve security.",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFFEF3C7),
                                border = BorderStroke(1.dp, Color(0xFFD4A017)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(text = "CURRENT AUTHORIZATION CODE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF92400E))
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = if (isAuthCodeVisible) (settings?.authorizationCode ?: "14800") else "•••••",
                                                fontSize = 20.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFFB45309),
                                                letterSpacing = 2.sp
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            IconButton(
                                                onClick = { isAuthCodeVisible = !isAuthCodeVisible },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(
                                                    imageVector = if (isAuthCodeVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                                    contentDescription = "Toggle code visibility",
                                                    tint = Color(0xFFB45309),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                    Button(
                                        onClick = {
                                            newAuthCodeInput = settings?.authorizationCode ?: "14800"
                                            showChangeCodeDialog = true
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = IafEmerald),
                                        shape = RoundedCornerShape(6.dp),
                                        modifier = Modifier.testTag("change_auth_code_btn")
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Change Code", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }

                    if (showChangeCodeDialog) {
                        AlertDialog(
                            onDismissRequest = { showChangeCodeDialog = false },
                            title = { Text("Update Authorization Code", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
                            text = {
                                Column {
                                    Text(
                                        text = "Enter a new authorization code for Idara Al-Furqan teachers. Minimum 4 characters.",
                                        fontSize = 12.sp,
                                        color = Color.DarkGray
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    OutlinedTextField(
                                        value = newAuthCodeInput,
                                        onValueChange = { newAuthCodeInput = it },
                                        label = { Text("New Authorization Code") },
                                        modifier = Modifier.fillMaxWidth().testTag("new_auth_code_input"),
                                        singleLine = true
                                    )
                                }
                            },
                            confirmButton = {
                                Button(
                                    onClick = {
                                        if (newAuthCodeInput.trim().length >= 4) {
                                            teacherViewModel.updateAuthorizationCode(newAuthCodeInput.trim(), teacherUsername)
                                            showChangeCodeDialog = false
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = IafEmerald),
                                    modifier = Modifier.testTag("save_auth_code_btn")
                                ) {
                                    Text("Save Code")
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showChangeCodeDialog = false }) {
                                    Text("Cancel")
                                }
                            }
                        )
                    }

                    // Change Username Dialog
                    if (showChangeUsernameDialog) {
                        AlertDialog(
                            onDismissRequest = { showChangeUsernameDialog = false },
                            title = { Text("Change Teacher Username", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
                            text = {
                                Column {
                                    Text(
                                        text = "Current username: $teacherUsername\nEnter your new username and current password to verify.",
                                        fontSize = 12.sp,
                                        color = Color.DarkGray
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    OutlinedTextField(
                                        value = newUsernameInput,
                                        onValueChange = { newUsernameInput = it },
                                        label = { Text("New Username") },
                                        modifier = Modifier.fillMaxWidth().testTag("new_teacher_username_input"),
                                        singleLine = true
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedTextField(
                                        value = passVerifyInput,
                                        onValueChange = { passVerifyInput = it },
                                        label = { Text("Current Password (to verify)") },
                                        visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                                        modifier = Modifier.fillMaxWidth().testTag("teacher_verify_pass_input"),
                                        singleLine = true
                                    )
                                }
                            },
                            confirmButton = {
                                Button(
                                    onClick = {
                                        if (newUsernameInput.isNotBlank() && passVerifyInput.isNotBlank()) {
                                            teacherViewModel.changeTeacherUsername(newUsernameInput, passVerifyInput, teacherUsername) {
                                                showChangeUsernameDialog = false
                                            }
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = IafEmerald),
                                    modifier = Modifier.testTag("confirm_change_username_btn")
                                ) {
                                    Text("Update Username")
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showChangeUsernameDialog = false }) {
                                    Text("Cancel")
                                }
                            }
                        )
                    }

                    // Change Password Dialog
                    if (showChangePasswordDialog) {
                        AlertDialog(
                            onDismissRequest = { showChangePasswordDialog = false },
                            title = { Text("Change Teacher Password", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
                            text = {
                                Column {
                                    Text(
                                        text = "Enter your current password and your new password (minimum 6 characters).",
                                        fontSize = 12.sp,
                                        color = Color.DarkGray
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    OutlinedTextField(
                                        value = currentPassInput,
                                        onValueChange = { currentPassInput = it },
                                        label = { Text("Current Password") },
                                        visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                                        modifier = Modifier.fillMaxWidth().testTag("teacher_current_pass_input"),
                                        singleLine = true
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedTextField(
                                        value = newPassInput,
                                        onValueChange = { newPassInput = it },
                                        label = { Text("New Password (min 6 chars)") },
                                        visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                                        modifier = Modifier.fillMaxWidth().testTag("teacher_new_pass_input"),
                                        singleLine = true
                                    )
                                }
                            },
                            confirmButton = {
                                Button(
                                    onClick = {
                                        if (currentPassInput.isNotBlank() && newPassInput.length >= 6) {
                                            teacherViewModel.changeTeacherPassword(currentPassInput, newPassInput, teacherUsername) {
                                                showChangePasswordDialog = false
                                            }
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = IafEmerald),
                                    modifier = Modifier.testTag("confirm_change_password_btn")
                                ) {
                                    Text("Update Password")
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showChangePasswordDialog = false }) {
                                    Text("Cancel")
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ReportSummaryCard(title: String, metric: String, details: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(text = title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = IafEmerald)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = metric, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1E293B))
            Text(text = details, fontSize = 11.sp, color = Color.Gray)
        }
    }
}

// -------------------------------------------------------------
// 5-Step Student Creation Wizard Dialog
// -------------------------------------------------------------
@Composable
fun StudentCreationWizardDialog(
    state: com.example.ui.viewmodel.StudentWizardState,
    language: AppLanguage,
    onUpdate: ((com.example.ui.viewmodel.StudentWizardState) -> com.example.ui.viewmodel.StudentWizardState) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    val fatherCnicPhotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            onUpdate { s -> s.copy(fatherCnicPhotoUri = uri.toString()) }
        }
    }

    val studentBFormPhotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            onUpdate { s -> s.copy(studentBFormPhotoUri = uri.toString()) }
        }
    }

    val studentPhotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            onUpdate { s -> s.copy(photoUri = uri.toString()) }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "Add New Student — Step ${state.currentStep} of 5",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = IafEmerald
                )
                Text(
                    text = when (state.currentStep) {
                        1 -> "1. Student Details"
                        2 -> "2. Program and Class"
                        3 -> "3. Contact Details"
                        4 -> "4. Fee Settings"
                        else -> "5. Review and Save"
                    },
                    fontSize = 12.sp,
                    color = Color.DarkGray
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                when (state.currentStep) {
                    1 -> {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFE4F4EA),
                            border = BorderStroke(1.dp, IafEmerald),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "Institute ID Assignment",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = IafEmerald
                                )
                                Text(
                                    text = "Assign the Institute Student ID manually. You can assign the same ID to siblings sharing the same father so they can be accessed via 1 account.",
                                    fontSize = 11.sp,
                                    color = Color.DarkGray
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = state.instituteStudentCode,
                            onValueChange = { onUpdate { s -> s.copy(instituteStudentCode = it) } },
                            label = { Text("Institute Student ID * (e.g. IAF-STU-001)") },
                            placeholder = { Text("IAF-STU-001") },
                            modifier = Modifier.fillMaxWidth().testTag("wizard_institute_code"),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = state.fullName,
                            onValueChange = { onUpdate { s -> s.copy(fullName = it) } },
                            label = { Text("Student Full Name *") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("wizard_fullname"),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        // Student Photograph Attachment for Student ID Card
                        Text(
                            text = "Student Photograph (Used in Official Student ID Card)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF1E293B)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        if (!state.photoUri.isNullOrBlank()) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFE4F4EA)),
                                border = BorderStroke(1.dp, IafEmerald)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(64.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFFE8EFEA))
                                            .border(1.5.dp, Color(0xFFD4A017), RoundedCornerShape(8.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        AsyncImage(
                                            model = state.photoUri,
                                            contentDescription = "Student Photo Preview",
                                            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(7.dp)),
                                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Student Photo Attached ✓",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = IafEmerald
                                        )
                                        Text(
                                            text = "This photograph will appear on the student's digital ID card.",
                                            fontSize = 11.sp,
                                            color = Color.DarkGray
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Button(
                                                onClick = {
                                                    studentPhotoLauncher.launch(
                                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                                    )
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = IafEmerald),
                                                shape = RoundedCornerShape(6.dp),
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                modifier = Modifier.height(28.dp)
                                            ) {
                                                Text("Change", fontSize = 11.sp)
                                            }
                                            TextButton(
                                                onClick = { onUpdate { s -> s.copy(photoUri = null) } },
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                modifier = Modifier.height(28.dp)
                                            ) {
                                                Text("Remove", fontSize = 11.sp, color = Color(0xFFDC2626))
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFF8FAFC),
                                border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(54.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFFE8EFEA))
                                            .border(1.5.dp, Color(0xFFD4A017), RoundedCornerShape(8.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Person, contentDescription = null, tint = IafEmerald, modifier = Modifier.size(32.dp))
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Upload Student Photo (for ID Card)",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFF334155)
                                        )
                                        Text(
                                            text = "Will be displayed on the student ID card",
                                            fontSize = 11.sp,
                                            color = Color.Gray
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Button(
                                                onClick = {
                                                    studentPhotoLauncher.launch(
                                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                                    )
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = IafEmerald),
                                                shape = RoundedCornerShape(6.dp),
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                                modifier = Modifier.height(28.dp)
                                            ) {
                                                Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(13.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Select Photo", fontSize = 11.sp)
                                            }
                                            OutlinedButton(
                                                onClick = {
                                                    onUpdate { s -> s.copy(photoUri = "https://images.unsplash.com/photo-1544717305-2782549b5136?w=600&auto=format&fit=crop&q=60") }
                                                },
                                                shape = RoundedCornerShape(6.dp),
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                                modifier = Modifier.height(28.dp)
                                            ) {
                                                Text("Use Sample", fontSize = 11.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = state.fatherName,
                            onValueChange = { onUpdate { s -> s.copy(fatherName = it) } },
                            label = { Text("Father's Name *") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = state.fatherCnic,
                            onValueChange = { onUpdate { s -> s.copy(fatherCnic = it) } },
                            label = { Text("Father's CNIC * (e.g. 35201-1234567-1)") },
                            modifier = Modifier.fillMaxWidth().testTag("wizard_father_cnic"),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        // Father CNIC Picture Attachment
                        Text(
                            text = "Father's CNIC Picture *",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF1E293B)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        if (!state.fatherCnicPhotoUri.isNullOrBlank()) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFE4F4EA)),
                                border = BorderStroke(1.dp, IafEmerald)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = IafEmerald, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Father's CNIC Picture Attached", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = IafEmerald)
                                        }
                                        TextButton(onClick = { onUpdate { s -> s.copy(fatherCnicPhotoUri = null) } }) {
                                            Text("Remove", fontSize = 11.sp, color = Color(0xFFDC2626))
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    AsyncImage(
                                        model = state.fatherCnicPhotoUri,
                                        contentDescription = "Father CNIC",
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(130.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color(0xFFE2E8F0)),
                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    OutlinedButton(
                                        onClick = {
                                            fatherCnicPhotoLauncher.launch(
                                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                            )
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Change Father CNIC Picture", fontSize = 11.sp)
                                    }
                                }
                            }
                        } else {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFF8FAFC),
                                border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(Icons.Default.Badge, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(32.dp))
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Upload Father's CNIC Picture (Mandatory) *",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFF334155)
                                    )
                                    Text(
                                        text = "Take a photo or choose from device gallery",
                                        fontSize = 11.sp,
                                        color = Color.Gray
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = {
                                                fatherCnicPhotoLauncher.launch(
                                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                                )
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = IafEmerald),
                                            shape = RoundedCornerShape(6.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Select Photo", fontSize = 11.sp)
                                        }
                                        OutlinedButton(
                                            onClick = {
                                                onUpdate { s -> s.copy(fatherCnicPhotoUri = "https://images.unsplash.com/photo-1544717305-2782549b5136?w=600&auto=format&fit=crop&q=60") }
                                            },
                                            shape = RoundedCornerShape(6.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("Use Sample", fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = state.studentCnicOrBForm,
                            onValueChange = { onUpdate { s -> s.copy(studentCnicOrBForm = it) } },
                            label = { Text("Student CNIC / B-Form Number *") },
                            modifier = Modifier.fillMaxWidth().testTag("wizard_student_cnic"),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        // Student B-Form / CNIC Picture Attachment
                        Text(
                            text = "Student B-Form / CNIC Picture *",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF1E293B)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        if (!state.studentBFormPhotoUri.isNullOrBlank()) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFE4F4EA)),
                                border = BorderStroke(1.dp, IafEmerald)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = IafEmerald, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Student B-Form / CNIC Attached", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = IafEmerald)
                                        }
                                        TextButton(onClick = { onUpdate { s -> s.copy(studentBFormPhotoUri = null) } }) {
                                            Text("Remove", fontSize = 11.sp, color = Color(0xFFDC2626))
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    AsyncImage(
                                        model = state.studentBFormPhotoUri,
                                        contentDescription = "Student B-Form",
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(130.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color(0xFFE2E8F0)),
                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    OutlinedButton(
                                        onClick = {
                                            studentBFormPhotoLauncher.launch(
                                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                            )
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Change Student B-Form Picture", fontSize = 11.sp)
                                    }
                                }
                            }
                        } else {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFF8FAFC),
                                border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(Icons.Default.Description, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(32.dp))
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Upload Student B-Form / CNIC Picture (Mandatory) *",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFF334155)
                                    )
                                    Text(
                                        text = "Take a photo or choose from device gallery",
                                        fontSize = 11.sp,
                                        color = Color.Gray
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = {
                                                studentBFormPhotoLauncher.launch(
                                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                                )
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = IafEmerald),
                                            shape = RoundedCornerShape(6.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Select Photo", fontSize = 11.sp)
                                        }
                                        OutlinedButton(
                                            onClick = {
                                                onUpdate { s -> s.copy(studentBFormPhotoUri = "https://images.unsplash.com/photo-1584697964190-7bb9ff27756e?w=600&auto=format&fit=crop&q=60") }
                                            },
                                            shape = RoundedCornerShape(6.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("Use Sample", fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = state.rollNumber,
                            onValueChange = { onUpdate { s -> s.copy(rollNumber = it) } },
                            label = { Text("Roll Number (Optional - leave blank if not assigned)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = state.dobOrAge,
                            onValueChange = { onUpdate { s -> s.copy(dobOrAge = it) } },
                            label = { Text("Date of Birth / Age (e.g. 10 years)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        if (state.fatherCnicPhotoUri.isNullOrBlank() || state.studentBFormPhotoUri.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFFEF2F2),
                                border = BorderStroke(1.dp, Color(0xFFFCA5A5)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "⚠️ Both Father's CNIC picture and Student B-Form/CNIC picture must be attached before proceeding.",
                                    fontSize = 11.sp,
                                    color = Color(0xFFB91C1C),
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                        }
                    }

                    2 -> {
                        Text("Select Program (Exactly 3 Built-in Programs):", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(6.dp))
                        listOf("Noorani Qaida", "Nazra Quran", "Hifz-ul-Quran").forEach { prog ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onUpdate { s -> s.copy(program = prog) } }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(selected = state.program == prog, onClick = { onUpdate { s -> s.copy(program = prog) } })
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = prog, fontSize = 13.sp)
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = state.className,
                            onValueChange = { onUpdate { s -> s.copy(className = it) } },
                            label = { Text("Class / Group (e.g. Group A)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = state.assignedTeacher,
                            onValueChange = { onUpdate { s -> s.copy(assignedTeacher = it) } },
                            label = { Text("Assigned Teacher / Ustadh") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }

                    3 -> {
                        OutlinedTextField(
                            value = state.phone,
                            onValueChange = { onUpdate { s -> s.copy(phone = it) } },
                            label = { Text("Contact Phone Number *") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = state.address,
                            onValueChange = { onUpdate { s -> s.copy(address = it) } },
                            label = { Text("Home Address * (will show on ID card)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = state.emergencyContact,
                            onValueChange = { onUpdate { s -> s.copy(emergencyContact = it) } },
                            label = { Text("Emergency Contact (Optional)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }

                    4 -> {
                        OutlinedTextField(
                            value = state.monthlyFee,
                            onValueChange = { onUpdate { s -> s.copy(monthlyFee = it) } },
                            label = { Text("Monthly Fee (PKR)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = state.approvedConcession,
                            onValueChange = { onUpdate { s -> s.copy(approvedConcession = it) } },
                            label = { Text("Approved Concession / Discount (PKR)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = state.dueDate,
                            onValueChange = { onUpdate { s -> s.copy(dueDate = it) } },
                            label = { Text("Due Date (e.g. 10th of month)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = state.notes,
                            onValueChange = { onUpdate { s -> s.copy(notes = it) } },
                            label = { Text("Private Teacher Notes (Optional)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    5 -> {
                        Text("Review New Student Record", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = IafEmerald)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("• Assigned Institute ID: ${state.instituteStudentCode.ifBlank { "Not set" }}", fontWeight = FontWeight.Bold, color = IafEmerald)
                        Text("• Full Name: ${state.fullName}")
                        Text("• Student ID Card Photo: ${if (!state.photoUri.isNullOrBlank()) "✓ [Photograph Attached]" else "[No photo attached (Avatar used)]"}")
                        if (!state.photoUri.isNullOrBlank()) {
                            AsyncImage(
                                model = state.photoUri,
                                contentDescription = "Student Photo",
                                modifier = Modifier
                                    .padding(vertical = 4.dp)
                                    .size(70.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(1.5.dp, Color(0xFFD4A017), RoundedCornerShape(8.dp)),
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                            )
                        }
                        Text("• Father: ${state.fatherName.ifBlank { "Not specified" }}")
                        Text("• Father CNIC: ${state.fatherCnic.ifBlank { "Not provided" }} ${if (!state.fatherCnicPhotoUri.isNullOrBlank()) "✓ [Picture Attached]" else "⚠️ [Missing Picture]"}")
                        if (!state.fatherCnicPhotoUri.isNullOrBlank()) {
                            AsyncImage(
                                model = state.fatherCnicPhotoUri,
                                contentDescription = "Father CNIC",
                                modifier = Modifier
                                    .padding(vertical = 4.dp)
                                    .height(75.dp)
                                    .fillMaxWidth(0.5f)
                                    .clip(RoundedCornerShape(6.dp)),
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                            )
                        }
                        Text("• Student CNIC / B-Form: ${state.studentCnicOrBForm.ifBlank { "Not provided" }} ${if (!state.studentBFormPhotoUri.isNullOrBlank()) "✓ [Picture Attached]" else "⚠️ [Missing Picture]"}")
                        if (!state.studentBFormPhotoUri.isNullOrBlank()) {
                            AsyncImage(
                                model = state.studentBFormPhotoUri,
                                contentDescription = "Student B-Form",
                                modifier = Modifier
                                    .padding(vertical = 4.dp)
                                    .height(75.dp)
                                    .fillMaxWidth(0.5f)
                                    .clip(RoundedCornerShape(6.dp)),
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                            )
                        }
                        Text("• Program: ${state.program}")
                        Text("• Class: ${state.className}")
                        Text("• Roll Number: ${state.rollNumber.ifBlank { "Not assigned (Optional)" }}")
                        Text("• Phone: ${state.phone}")
                        Text("• Address: ${state.address}")
                        Text("• Net Monthly Fee: PKR ${(state.monthlyFee.toDoubleOrNull() ?: 2000.0) - (state.approvedConcession.toDoubleOrNull() ?: 0.0)}")
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Teacher-assigned Institute ID '${state.instituteStudentCode}' will be assigned to this student.",
                            fontSize = 11.sp,
                            color = Color.DarkGray
                        )
                    }
                }
            }
        },
        confirmButton = {
            val canProceed = when (state.currentStep) {
                1 -> state.instituteStudentCode.isNotBlank() &&
                        state.fullName.isNotBlank() &&
                        state.fatherName.isNotBlank() &&
                        state.fatherCnic.isNotBlank() &&
                        !state.fatherCnicPhotoUri.isNullOrBlank() &&
                        state.studentCnicOrBForm.isNotBlank() &&
                        !state.studentBFormPhotoUri.isNullOrBlank()
                3 -> state.phone.isNotBlank() && state.address.isNotBlank()
                else -> true
            }

            if (state.currentStep < 5) {
                Button(
                    onClick = { onUpdate { s -> s.copy(currentStep = s.currentStep + 1) } },
                    enabled = canProceed,
                    colors = ButtonDefaults.buttonColors(containerColor = IafEmerald)
                ) {
                    Text("Next")
                }
            } else {
                Button(
                    onClick = onSave,
                    enabled = canProceed,
                    colors = ButtonDefaults.buttonColors(containerColor = IafEmerald),
                    modifier = Modifier.testTag("wizard_save_button")
                ) {
                    Text("Save Student")
                }
            }
        },
        dismissButton = {
            if (state.currentStep > 1) {
                TextButton(onClick = { onUpdate { s -> s.copy(currentStep = s.currentStep - 1) } }) {
                    Text("Previous")
                }
            } else {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        }
    )
}

// -------------------------------------------------------------
// Quick Quran Progress Logger Dialog
// -------------------------------------------------------------
@Composable
fun QuranProgressLogDialog(
    student: StudentEntity,
    language: AppLanguage,
    teacherUsername: String,
    onDismiss: () -> Unit,
    onSave: (String, Int?, String?, String, String?, String?, String?, String, String, Int, String?, String?) -> Unit
) {
    val isHifz = student.program == "Hifz-ul-Quran"
    var recordFrequency by remember { mutableStateOf("Daily") } // "Daily", "Weekly", "Bi-Weekly", "Monthly"
    var juz by remember { mutableStateOf("1") }
    var surah by remember { mutableStateOf("Al-Baqarah") }
    var lessonOrVerses by remember { mutableStateOf("Verses 1-25") }
    var sabaq by remember { mutableStateOf(if (isHifz) "Surah Al-Baqarah Page 11" else "Lesson 14: Madd Rules") }
    var sabqi by remember { mutableStateOf("Juz 1 Pages 1-8") }
    var manzil by remember { mutableStateOf("Juz 30 (Full)") }
    var fluency by remember { mutableStateOf("Good") }
    var tajweed by remember { mutableStateOf("Accurate") }
    var mistakes by remember { mutableStateOf("1") }
    var comments by remember { mutableStateOf("Good recitation today.") }
    var nextAssignment by remember { mutableStateOf("Continue next portion tomorrow") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text("Log Quran Progress", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = IafEmerald)
                Text("${student.fullName} • ${student.program}", fontSize = 12.sp, color = Color.DarkGray)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Record Frequency Selector (Daily, Weekly, Bi-Weekly, Monthly)
                Text(text = "RECORD FREQUENCY / TYPE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf("Daily", "Weekly", "Bi-Weekly", "Monthly").forEach { freq ->
                        val isSelected = recordFrequency == freq
                        FilterChip(
                            selected = isSelected,
                            onClick = { recordFrequency = freq },
                            label = { Text(freq, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (isHifz) {
                    // HIFZ SPECIALIZED SECTIONS: SABAQ, SABQI, MANZIL
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFE4F4EA),
                        border = BorderStroke(1.dp, IafEmerald),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "HIFZ CURRICULUM — $recordFrequency RECORD",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = IafEmerald
                            )
                            Text(
                                text = "Record Sabaq (New), Sabqi (Recent Revision), and Manzil (Old Revision).",
                                fontSize = 10.sp,
                                color = Color.DarkGray
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = juz,
                            onValueChange = { juz = it },
                            label = { Text("Juz #") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = surah,
                            onValueChange = { surah = it },
                            label = { Text("Surah") },
                            modifier = Modifier.weight(2f),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // 1. Sabaq (New Lesson)
                    OutlinedTextField(
                        value = sabaq,
                        onValueChange = { sabaq = it },
                        label = { Text("Sabaq (سبق - New Lesson) *") },
                        placeholder = { Text("e.g. Page 12, Ayah 1-20") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // 2. Sabqi (Recent Revision)
                    OutlinedTextField(
                        value = sabqi,
                        onValueChange = { sabqi = it },
                        label = { Text("Sabqi (سبقی - Recent Revision) *") },
                        placeholder = { Text("e.g. Previous 5 Pages or Quarter Juz") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // 3. Manzil (Old Revision)
                    OutlinedTextField(
                        value = manzil,
                        onValueChange = { manzil = it },
                        label = { Text("Manzil (منزل - Long-Term Revision) *") },
                        placeholder = { Text("e.g. Juz 30 or Juz 1-2") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                } else {
                    // QAIDA & NAZRA: SABAQ ONLY
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFF0FDF4),
                        border = BorderStroke(1.dp, Color(0xFF16A34A)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "${student.program.uppercase()} — $recordFrequency SABAQ",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF166534)
                            )
                            Text(
                                text = "Record student's Sabaq (lesson/ayahs) and tajweed progress.",
                                fontSize = 10.sp,
                                color = Color.DarkGray
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = sabaq,
                        onValueChange = {
                            sabaq = it
                            lessonOrVerses = it
                        },
                        label = { Text("Sabaq (سبق - Current Lesson) *") },
                        placeholder = { Text(if (student.program.contains("Qaida")) "e.g. Lesson 5: Tanween" else "e.g. Surah Maryam Ayah 1-25") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Evaluation & Feedback
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = fluency,
                        onValueChange = { fluency = it },
                        label = { Text("Fluency Rating") },
                        placeholder = { Text("Excellent / Good / Fair") },
                        modifier = Modifier.weight(2f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = mistakes,
                        onValueChange = { mistakes = it },
                        label = { Text("Mistakes") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = tajweed,
                    onValueChange = { tajweed = it },
                    label = { Text("Tajweed & Pronunciation") },
                    placeholder = { Text("e.g. Accurate Qalqalah, clear Madd") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = comments,
                    onValueChange = { comments = it },
                    label = { Text("Teacher Comments & Remarks") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = nextAssignment,
                    onValueChange = { nextAssignment = it },
                    label = { Text("Next Assignment Target") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        recordFrequency,
                        juz.toIntOrNull(),
                        surah,
                        if (isHifz) sabaq else lessonOrVerses.ifBlank { sabaq },
                        if (isHifz) sabaq else sabaq,
                        if (isHifz) sabqi else null,
                        if (isHifz) manzil else null,
                        fluency,
                        tajweed,
                        mistakes.toIntOrNull() ?: 0,
                        comments,
                        nextAssignment
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = IafEmerald)
            ) {
                Text("Save $recordFrequency Progress")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
