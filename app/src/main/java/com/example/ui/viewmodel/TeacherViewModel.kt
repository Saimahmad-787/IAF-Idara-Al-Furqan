package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.*
import com.example.data.repository.IafRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class StudentWizardState(
    val currentStep: Int = 1,
    // Step 1: Student Details
    val instituteStudentCode: String = "IAF-STU-001", // Teacher assigned! (Can be shared for siblings)
    val fullName: String = "",
    val photoUri: String? = null, // Student's photograph for ID Card!
    val rollNumber: String = "", // Optional!
    val dobOrAge: String = "",
    val fatherName: String = "",
    val fatherCnic: String = "", // Mandatory!
    val fatherCnicPhotoUri: String? = null, // Mandatory! Picture of Father CNIC
    val studentCnicOrBForm: String = "", // Mandatory!
    val studentBFormPhotoUri: String? = null, // Mandatory! Picture of Student B-Form / CNIC
    val cnic: String = "", // Optional!
    val emergencyContact: String = "",
    // Step 2: Program & Class
    val program: String = "Noorani Qaida", // Noorani Qaida, Nazra Quran, Hifz-ul-Quran
    val className: String = "Group A",
    val assignedTeacher: String = "Head Ustadh",
    // Step 3: Contact Details
    val phone: String = "",
    val address: String = "",
    // Step 4: Fee Settings
    val monthlyFee: String = "2000",
    val feeStartDate: String = "",
    val dueDate: String = "10th of every month",
    val approvedConcession: String = "0",
    val notes: String = ""
)

class TeacherViewModel(private val repository: IafRepository) : ViewModel() {
    val students: StateFlow<List<StudentEntity>> = repository.getAllStudents()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingLinkRequests: StateFlow<List<ParentStudentLinkEntity>> = repository.getAllPendingLinkRequests()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allFees: StateFlow<List<FeePaymentEntity>> = repository.getAllFees()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val shifts: StateFlow<List<ShiftEntity>> = repository.getAllShifts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val prayerTimings: StateFlow<List<PrayerTimingEntity>> = repository.getAllPrayerTimings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingLeaveRequests: StateFlow<List<LeaveRequestEntity>> = repository.getPendingLeaveRequests()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val announcements: StateFlow<List<AnnouncementEntity>> = repository.getAllAnnouncements()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val announcementsPastMonth: StateFlow<List<AnnouncementEntity>> = repository.getAnnouncementsPastMonth()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allQuranRecords: StateFlow<List<QuranRecordEntity>> = repository.getAllQuranRecords()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val socialLinks: StateFlow<SocialLinksEntity?> = repository.getSocialLinks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val pendingRetentionReviews: StateFlow<List<RetentionReviewEntity>> = repository.getPendingRetentionReviews()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val auditLogs: StateFlow<List<AuditLogEntity>> = repository.getAllAuditLogs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val instituteSettings: StateFlow<InstituteSettingsEntity?> = repository.getSettingsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val weeklyDays: StateFlow<List<InstituteWeeklyDayEntity>> = repository.getWeeklyDays()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // UI Dialog & Form States
    private val _wizardState = MutableStateFlow(StudentWizardState())
    val wizardState: StateFlow<StudentWizardState> = _wizardState.asStateFlow()

    private val _selectedStudent = MutableStateFlow<StudentEntity?>(null)
    val selectedStudent: StateFlow<StudentEntity?> = _selectedStudent.asStateFlow()

    private val _showWizardDialog = MutableStateFlow(false)
    val showWizardDialog: StateFlow<Boolean> = _showWizardDialog.asStateFlow()

    private val _showIdCardDialog = MutableStateFlow(false)
    val showIdCardDialog: StateFlow<Boolean> = _showIdCardDialog.asStateFlow()

    // Attendance State
    private val _attendanceDate = MutableStateFlow(SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()))
    val attendanceDate: StateFlow<String> = _attendanceDate.asStateFlow()

    private val _attendanceRecordsForDate = MutableStateFlow<List<AttendanceEntity>>(emptyList())
    val attendanceRecordsForDate: StateFlow<List<AttendanceEntity>> = _attendanceRecordsForDate.asStateFlow()

    private val _batchAttendanceMap = MutableStateFlow<Map<Long, String>>(emptyMap())
    val batchAttendanceMap: StateFlow<Map<Long, String>> = _batchAttendanceMap.asStateFlow()

    // Status Message State
    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    init {
        viewModelScope.launch {
            loadAttendanceForDate(_attendanceDate.value)
            repository.generateRetentionReviewsForExpiredRecords()
        }
    }

    fun clearStatusMessage() {
        _statusMessage.value = null
    }

    // --- Student Wizard Actions ---

    fun openStudentWizard(show: Boolean) {
        if (show) {
            _wizardState.value = StudentWizardState(
                feeStartDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
            )
        }
        _showWizardDialog.value = show
    }

    fun updateWizard(update: (StudentWizardState) -> StudentWizardState) {
        _wizardState.value = update(_wizardState.value)
    }

    fun saveStudent(teacherUsername: String) {
        val w = _wizardState.value
        if (w.fullName.isBlank()) {
            _statusMessage.value = "Student full name is required."
            return
        }
        if (w.instituteStudentCode.isBlank()) {
            _statusMessage.value = "Institute ID is required."
            return
        }
        if (w.fatherCnic.isBlank()) {
            _statusMessage.value = "Father's CNIC is mandatory."
            return
        }
        if (w.fatherCnicPhotoUri.isNullOrBlank()) {
            _statusMessage.value = "Picture of Father's CNIC is mandatory."
            return
        }
        if (w.studentCnicOrBForm.isBlank()) {
            _statusMessage.value = "Student CNIC / B-Form is mandatory."
            return
        }
        if (w.studentBFormPhotoUri.isNullOrBlank()) {
            _statusMessage.value = "Picture of Student B-Form or CNIC is mandatory."
            return
        }

        viewModelScope.launch {
            val res = repository.createStudent(
                instituteStudentCode = w.instituteStudentCode,
                fullName = w.fullName,
                photoUri = w.photoUri,
                rollNumber = w.rollNumber,
                dobOrAge = w.dobOrAge.ifBlank { "Not specified" },
                fatherName = w.fatherName.ifBlank { "Not specified" },
                fatherCnic = w.fatherCnic,
                fatherCnicPhotoUri = w.fatherCnicPhotoUri,
                studentCnicOrBForm = w.studentCnicOrBForm,
                studentBFormPhotoUri = w.studentBFormPhotoUri,
                cnic = w.cnic,
                emergencyContact = w.emergencyContact,
                phone = w.phone.ifBlank { "Not provided" },
                address = w.address.ifBlank { "Not provided" },
                admissionDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()),
                program = w.program,
                className = w.className,
                assignedTeacher = w.assignedTeacher,
                monthlyFee = w.monthlyFee.toDoubleOrNull() ?: 2000.0,
                feeStartDate = w.feeStartDate,
                dueDate = w.dueDate,
                approvedConcession = w.approvedConcession.toDoubleOrNull() ?: 0.0,
                notes = w.notes,
                teacherUsername = teacherUsername
            )
            res.fold(
                onSuccess = { created ->
                    _showWizardDialog.value = false
                    _statusMessage.value = "Student '${created.fullName}' (${created.instituteStudentCode}) added successfully!"
                },
                onFailure = { err ->
                    _statusMessage.value = err.message ?: "Failed to create student."
                }
            )
        }
    }

    fun selectStudentForIdCard(student: StudentEntity?) {
        _selectedStudent.value = student
        _showIdCardDialog.value = student != null
    }

    fun updateStudent(student: StudentEntity, teacherUsername: String) {
        viewModelScope.launch {
            repository.updateStudent(student, teacherUsername)
            _selectedStudent.value = student
            _statusMessage.value = "Updated student details for '${student.fullName}'."
        }
    }

    // --- Attendance Batch Marking ---

    fun setAttendanceDate(date: String) {
        _attendanceDate.value = date
        loadAttendanceForDate(date)
    }

    fun selectAttendanceDate(date: String) {
        setAttendanceDate(date)
    }

    fun loadAttendanceForDate(date: String) {
        viewModelScope.launch {
            repository.getAttendanceForDate(date).collect { list ->
                _attendanceRecordsForDate.value = list
                val map = list.associate { it.studentId to it.status }
                _batchAttendanceMap.value = map
            }
        }
    }

    fun updateStudentAttendanceStatus(studentId: Long, status: String) {
        val current = _batchAttendanceMap.value.toMutableMap()
        current[studentId] = status
        _batchAttendanceMap.value = current
    }

    fun markSingleStudentAttendance(
        student: StudentEntity,
        status: String,
        teacherUsername: String
    ) {
        viewModelScope.launch {
            val date = _attendanceDate.value
            val record = AttendanceEntity(
                studentId = student.id,
                studentName = student.fullName,
                date = date,
                status = status,
                shift = "Morning Shift",
                recordedBy = teacherUsername
            )
            repository.saveBatchAttendance(date, listOf(record), teacherUsername)
            _statusMessage.value = "Marked ${student.fullName} as $status for today."
        }
    }

    fun markAllUnmarkedStudents(
        unmarked: List<StudentEntity>,
        status: String,
        teacherUsername: String
    ) {
        viewModelScope.launch {
            val date = _attendanceDate.value
            val records = unmarked.map { student ->
                AttendanceEntity(
                    studentId = student.id,
                    studentName = student.fullName,
                    date = date,
                    status = status,
                    shift = "Morning Shift",
                    recordedBy = teacherUsername
                )
            }
            repository.saveBatchAttendance(date, records, teacherUsername)
            _statusMessage.value = "All remaining students marked as $status."
        }
    }

    fun saveBatchAttendance(teacherUsername: String) {
        viewModelScope.launch {
            val date = _attendanceDate.value
            val currentStudents = students.value
            val records = currentStudents.map { student ->
                val status = _batchAttendanceMap.value[student.id] ?: "Present"
                AttendanceEntity(
                    studentId = student.id,
                    studentName = student.fullName,
                    date = date,
                    status = status,
                    shift = "Morning Shift",
                    recordedBy = teacherUsername
                )
            }
            repository.saveBatchAttendance(date, records, teacherUsername)
            _statusMessage.value = "Attendance for $date recorded successfully."
        }
    }

    // Attendance History State
    private val _historyDate = MutableStateFlow(SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()))
    val historyDate: StateFlow<String> = _historyDate.asStateFlow()

    val historyAttendanceRecords: StateFlow<List<AttendanceEntity>> = _historyDate
        .flatMapLatest { date -> repository.getAttendanceForDate(date) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectHistoryDate(date: String) {
        _historyDate.value = date
    }

    fun updateAttendanceRecord(record: AttendanceEntity, teacherUsername: String) {
        viewModelScope.launch {
            repository.updateAttendanceRecord(record, teacherUsername)
            _statusMessage.value = "Attendance for ${record.studentName} updated to ${record.status}."
        }
    }

    // --- Direct Fee Management (Paid / Unpaid) ---

    fun setFeeStatusDirectly(feeId: Long, status: String, teacherUsername: String) {
        viewModelScope.launch {
            repository.setFeeStatusDirectly(feeId, status, teacherUsername)
            _statusMessage.value = "Fee record marked as $status."
        }
    }

    fun createFeeRecord(
        studentId: Long,
        invoiceMonth: String,
        amountDue: Double,
        status: String,
        teacherUsername: String
    ) {
        viewModelScope.launch {
            val res = repository.createFeeRecord(studentId, invoiceMonth, amountDue, status, teacherUsername)
            res.fold(
                onSuccess = {
                    _statusMessage.value = "Fee invoice created for ${it.studentName} ($status)."
                },
                onFailure = {
                    _statusMessage.value = it.message ?: "Failed to create fee record."
                }
            )
        }
    }

    // --- Shifts Management ---

    fun addShift(
        name: String,
        startTime: String,
        endTime: String,
        days: String,
        assignedClass: String,
        assignedTeacher: String,
        room: String,
        teacherUsername: String
    ) {
        viewModelScope.launch {
            repository.addShift(name, startTime, endTime, days, assignedClass, assignedTeacher, room, teacherUsername)
            _statusMessage.value = "Shift '$name' added successfully."
        }
    }

    fun updateShift(shift: ShiftEntity, teacherUsername: String) {
        viewModelScope.launch {
            repository.updateShift(shift, teacherUsername)
            _statusMessage.value = "Shift '${shift.name}' updated."
        }
    }

    fun deleteShift(shiftId: Long, teacherUsername: String) {
        viewModelScope.launch {
            repository.deleteShift(shiftId, teacherUsername)
            _statusMessage.value = "Shift deleted."
        }
    }

    // --- Prayer Timings Management ---

    fun updatePrayerTiming(timing: PrayerTimingEntity, teacherUsername: String) {
        viewModelScope.launch {
            repository.updatePrayerTiming(timing, teacherUsername)
            _statusMessage.value = "Prayer time for ${timing.prayerName} updated to ${timing.timeString}."
        }
    }

    // --- Announcements Management ---

    fun updateAnnouncement(announcement: AnnouncementEntity, teacherUsername: String) {
        viewModelScope.launch {
            repository.updateAnnouncement(announcement, teacherUsername)
            _statusMessage.value = "Notice updated successfully."
        }
    }

    fun deleteAnnouncement(id: Long, teacherUsername: String) {
        viewModelScope.launch {
            repository.deleteAnnouncement(id, teacherUsername)
            _statusMessage.value = "Notice deleted."
        }
    }

    // --- Quran Progress Logger ---

    fun logQuranProgress(
        student: StudentEntity,
        recordType: String = "Daily",
        juz: Int?,
        surah: String?,
        lessonOrVerses: String,
        sabaq: String?,
        sabqi: String?,
        manzil: String?,
        fluency: String,
        tajweed: String,
        mistakes: Int,
        comments: String?,
        nextAssignment: String?,
        teacherUsername: String
    ) {
        viewModelScope.launch {
            val record = QuranRecordEntity(
                studentId = student.id,
                studentName = student.fullName,
                program = student.program,
                date = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()),
                recordType = recordType,
                juz = juz,
                surah = surah,
                versesOrLesson = lessonOrVerses,
                sabaq = sabaq,
                sabqi = sabqi,
                manzil = manzil,
                fluency = fluency,
                tajweedPronunciation = tajweed,
                mistakes = mistakes,
                teacherComments = comments,
                nextAssignment = nextAssignment,
                recordedBy = teacherUsername
            )
            repository.recordQuranProgress(record, teacherUsername)
            _statusMessage.value = "$recordType Quran progress recorded for ${student.fullName}."
        }
    }

    // --- Parent Link Verification Review ---

    fun reviewLinkRequest(linkId: Long, approve: Boolean, reason: String?, teacherUsername: String) {
        viewModelScope.launch {
            repository.reviewLinkRequest(linkId, approve, reason, teacherUsername)
            _statusMessage.value = if (approve) "Link request approved." else "Link request rejected."
        }
    }

    // --- Fee Payment Verification ---

    fun verifyFeePayment(feeId: Long, approve: Boolean, notes: String?, teacherUsername: String) {
        viewModelScope.launch {
            repository.verifyFeePayment(feeId, approve, notes, teacherUsername)
            _statusMessage.value = if (approve) "Payment approved and confirmed receipt issued." else "Payment marked as rejected/correction required."
        }
    }

    // --- Leave Review ---

    fun reviewLeaveRequest(requestId: Long, approve: Boolean, notes: String?, teacherUsername: String) {
        viewModelScope.launch {
            repository.reviewLeaveRequest(requestId, approve, notes, teacherUsername)
            _statusMessage.value = if (approve) "Leave request approved." else "Leave request rejected."
        }
    }

    // --- Announcements & Social Links ---

    fun createAnnouncement(title: String, content: String, targetGroup: String, author: String) {
        viewModelScope.launch {
            repository.createAnnouncement(title, content, targetGroup, author)
            _statusMessage.value = "Announcement published."
        }
    }

    fun updateSocialLinks(links: SocialLinksEntity, teacherUsername: String) {
        viewModelScope.launch {
            repository.updateSocialLinks(links, teacherUsername)
            _statusMessage.value = "Official social links updated."
        }
    }

    // --- Retention Review ---

    fun executeRetentionAction(reviewId: Long, decision: String, teacherUsername: String) {
        viewModelScope.launch {
            repository.executeRetentionAction(reviewId, decision, teacherUsername)
            _statusMessage.value = "Retention action executed: $decision."
        }
    }

    fun updateAuthorizationCode(newCode: String, teacherUsername: String) {
        viewModelScope.launch {
            val res = repository.updateAuthorizationCode(newCode, teacherUsername)
            res.fold(
                onSuccess = { code ->
                    _statusMessage.value = "Institute authorization code updated to '$code'."
                },
                onFailure = { err ->
                    _statusMessage.value = err.message ?: "Failed to update code."
                }
            )
        }
    }

    fun changeTeacherPassword(currentPass: String, newPass: String, teacherUsername: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val res = repository.changePassword(teacherUsername, currentPass, newPass)
            res.fold(
                onSuccess = {
                    _statusMessage.value = "Teacher password updated successfully."
                    onSuccess()
                },
                onFailure = { err ->
                    _statusMessage.value = err.message ?: "Failed to update password."
                }
            )
        }
    }

    fun changeTeacherUsername(newUsername: String, passwordVerification: String, currentUsername: String, onSuccess: (String) -> Unit) {
        viewModelScope.launch {
            val res = repository.changeUsername(currentUsername, newUsername, passwordVerification)
            res.fold(
                onSuccess = { updated ->
                    _statusMessage.value = "Username updated to '$updated'."
                    onSuccess(updated)
                },
                onFailure = { err ->
                    _statusMessage.value = err.message ?: "Failed to update username."
                }
            )
        }
    }

    // Weekly Operating Days Management
    fun updateWeeklyDay(dayIndex: Int, customName: String, isOn: Boolean, timingsOrNotes: String, teacherUsername: String = "Admin") {
        viewModelScope.launch {
            repository.updateWeeklyDay(dayIndex, customName, isOn, timingsOrNotes, teacherUsername)
            _statusMessage.value = "Updated $customName to ${if (isOn) "ON (Working Day)" else "OFF (Holiday)"}."
        }
    }

    fun toggleWeeklyDayStatus(dayIndex: Int, teacherUsername: String = "Admin") {
        viewModelScope.launch {
            repository.toggleWeeklyDay(dayIndex, teacherUsername)
        }
    }
}
