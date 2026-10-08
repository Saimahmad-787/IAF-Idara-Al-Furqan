package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.*
import com.example.data.repository.IafRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ParentViewModel(
    private val repository: IafRepository,
    val parentUsername: String
) : ViewModel() {

    val approvedLinks: StateFlow<List<ParentStudentLinkEntity>> = repository.getApprovedLinksForParent(parentUsername)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allParentLinks: StateFlow<List<ParentStudentLinkEntity>> = repository.getLinksForParent(parentUsername)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val announcements: StateFlow<List<AnnouncementEntity>> = repository.getAllAnnouncements()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val prayerTimings: StateFlow<List<PrayerTimingEntity>> = repository.getAllPrayerTimings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val socialLinks: StateFlow<SocialLinksEntity?> = repository.getSocialLinks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val leaveRequests: StateFlow<List<LeaveRequestEntity>> = repository.getLeaveRequestsForParent(parentUsername)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val weeklyDays: StateFlow<List<InstituteWeeklyDayEntity>> = repository.getWeeklyDays()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Selected Student State
    private val _selectedStudentId = MutableStateFlow<Long?>(null)
    val selectedStudentId: StateFlow<Long?> = _selectedStudentId.asStateFlow()

    private val _selectedStudent = MutableStateFlow<StudentEntity?>(null)
    val selectedStudent: StateFlow<StudentEntity?> = _selectedStudent.asStateFlow()

    // Student Specific Flows
    private val _attendance = MutableStateFlow<List<AttendanceEntity>>(emptyList())
    val attendance: StateFlow<List<AttendanceEntity>> = _attendance.asStateFlow()

    private val _quranRecords = MutableStateFlow<List<QuranRecordEntity>>(emptyList())
    val quranRecords: StateFlow<List<QuranRecordEntity>> = _quranRecords.asStateFlow()

    private val _fees = MutableStateFlow<List<FeePaymentEntity>>(emptyList())
    val fees: StateFlow<List<FeePaymentEntity>> = _fees.asStateFlow()

    // UI Dialog States
    private val _showLinkStudentDialog = MutableStateFlow(false)
    val showLinkStudentDialog: StateFlow<Boolean> = _showLinkStudentDialog.asStateFlow()

    private val _showPaymentDialog = MutableStateFlow(false)
    val showPaymentDialog: StateFlow<Boolean> = _showPaymentDialog.asStateFlow()

    private val _showReceiptDialog = MutableStateFlow(false)
    val showReceiptDialog: StateFlow<Boolean> = _showReceiptDialog.asStateFlow()

    private val _selectedReceiptFee = MutableStateFlow<FeePaymentEntity?>(null)
    val selectedReceiptFee: StateFlow<FeePaymentEntity?> = _selectedReceiptFee.asStateFlow()

    private val _showLeaveDialog = MutableStateFlow(false)
    val showLeaveDialog: StateFlow<Boolean> = _showLeaveDialog.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    init {
        viewModelScope.launch {
            approvedLinks.collect { list ->
                if (_selectedStudentId.value == null && list.isNotEmpty()) {
                    selectStudent(list.first().studentId)
                } else if (_selectedStudentId.value != null && list.none { it.studentId == _selectedStudentId.value }) {
                    selectStudent(list.firstOrNull()?.studentId)
                }
            }
        }
    }

    fun clearStatusMessage() {
        _statusMessage.value = null
    }

    fun selectStudent(studentId: Long?) {
        _selectedStudentId.value = studentId
        if (studentId == null) {
            _selectedStudent.value = null
            _attendance.value = emptyList()
            _quranRecords.value = emptyList()
            _fees.value = emptyList()
            return
        }

        // Security check: Only load student data if the student is among approved links for this parent
        val isApproved = approvedLinks.value.any { it.studentId == studentId }
        if (!isApproved) {
            _selectedStudent.value = null
            _attendance.value = emptyList()
            _quranRecords.value = emptyList()
            _fees.value = emptyList()
            _statusMessage.value = "Access denied: You can only view approved linked students."
            return
        }

        viewModelScope.launch {
            val student = repository.getStudentById(studentId)
            _selectedStudent.value = student
            repository.getAttendanceForStudent(studentId).collect {
                _attendance.value = it
            }
        }

        viewModelScope.launch {
            repository.getQuranRecordsForStudent(studentId).collect {
                _quranRecords.value = it
            }
        }

        viewModelScope.launch {
            repository.getFeesForStudent(studentId).collect {
                _fees.value = it
            }
        }
    }

    fun openLinkStudentDialog(show: Boolean) {
        _showLinkStudentDialog.value = show
    }

    fun submitLinkRequest(instituteStudentCode: String) {
        viewModelScope.launch {
            val res = repository.requestStudentLink(parentUsername, instituteStudentCode)
            res.fold(
                onSuccess = { link ->
                    _showLinkStudentDialog.value = false
                    _statusMessage.value = "Link request for '${link.studentName}' (${link.instituteStudentCode}) submitted. Awaiting teacher verification."
                },
                onFailure = { err ->
                    _statusMessage.value = err.message ?: "Failed to submit link request."
                }
            )
        }
    }

    fun openPaymentDialog(show: Boolean) {
        _showPaymentDialog.value = show
    }

    fun submitPayment(amount: Double, method: String, ref: String?, month: String, notes: String?, evidenceUri: String? = null) {
        val stuId = _selectedStudentId.value ?: return
        viewModelScope.launch {
            val res = repository.submitFeePayment(stuId, amount, method, ref, month, notes, evidenceUri, parentUsername)
            res.fold(
                onSuccess = {
                    _showPaymentDialog.value = false
                    _statusMessage.value = "Payment submission recorded with proof screenshot. Teacher will review and issue confirmed receipt."
                },
                onFailure = { err ->
                    _statusMessage.value = err.message ?: "Failed to submit payment."
                }
            )
        }
    }

    fun viewReceipt(fee: FeePaymentEntity) {
        _selectedReceiptFee.value = fee
        _showReceiptDialog.value = true
    }

    fun closeReceipt() {
        _showReceiptDialog.value = false
        _selectedReceiptFee.value = null
    }

    fun openLeaveDialog(show: Boolean) {
        _showLeaveDialog.value = show
    }

    fun submitLeaveRequest(startDate: String, endDate: String, reason: String) {
        val stuId = _selectedStudentId.value ?: return
        viewModelScope.launch {
            val res = repository.submitLeaveRequest(stuId, parentUsername, startDate, endDate, reason)
            res.fold(
                onSuccess = {
                    _showLeaveDialog.value = false
                    _statusMessage.value = "Leave request submitted to institute administration."
                },
                onFailure = { err ->
                    _statusMessage.value = err.message ?: "Failed to submit leave request."
                }
            )
        }
    }

    // Account Settings
    private val _showAccountSettingsDialog = MutableStateFlow(false)
    val showAccountSettingsDialog: StateFlow<Boolean> = _showAccountSettingsDialog.asStateFlow()

    private val _currentParentUsername = MutableStateFlow(parentUsername)
    val currentParentUsername: StateFlow<String> = _currentParentUsername.asStateFlow()

    fun openAccountSettings(show: Boolean) {
        _showAccountSettingsDialog.value = show
    }

    fun changeParentPassword(currentPass: String, newPass: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val res = repository.changePassword(_currentParentUsername.value, currentPass, newPass)
            res.fold(
                onSuccess = {
                    _statusMessage.value = "Password updated successfully."
                    onSuccess()
                },
                onFailure = { err ->
                    _statusMessage.value = err.message ?: "Failed to update password."
                }
            )
        }
    }

    fun changeParentUsername(newUsername: String, passwordVerification: String, onSuccess: (String) -> Unit) {
        viewModelScope.launch {
            val res = repository.changeUsername(_currentParentUsername.value, newUsername, passwordVerification)
            res.fold(
                onSuccess = { updated ->
                    _currentParentUsername.value = updated
                    _statusMessage.value = "Username changed to '$updated'."
                    onSuccess(updated)
                },
                onFailure = { err ->
                    _statusMessage.value = err.message ?: "Failed to update username."
                }
            )
        }
    }
}
