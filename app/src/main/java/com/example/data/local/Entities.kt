package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val username: String,
    val passwordHash: String,
    val role: String, // "Teacher" or "Parent"
    val fullName: String,
    val phone: String,
    val instituteId: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "students")
data class StudentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val instituteId: String,
    val instituteStudentCode: String, // e.g. IAF-STU-001 (assigned by teacher, can be shared between siblings)
    val fullName: String,
    val photoUri: String? = null,
    val rollNumber: String? = null, // Optional, can be null
    val dobOrAge: String,
    val fatherName: String,
    val fatherCnic: String = "", // Mandatory
    val fatherCnicPhotoUri: String? = null, // Mandatory document picture
    val studentCnicOrBForm: String = "", // Mandatory
    val studentBFormPhotoUri: String? = null, // Mandatory document picture
    val cnic: String? = null, // Masked by default in UI
    val emergencyContact: String? = null,
    val phone: String,
    val address: String,
    val admissionDate: String,
    val program: String, // Noorani Qaida, Nazra Quran, Hifz-ul-Quran
    val className: String,
    val assignedTeacher: String,
    val monthlyFee: Double,
    val feeStartDate: String,
    val dueDate: String,
    val approvedConcession: Double = 0.0,
    val notes: String? = null,
    val status: String = "Active", // Active, Inactive, On Leave
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "parent_student_links")
data class ParentStudentLinkEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val parentUsername: String,
    val studentId: Long,
    val instituteStudentCode: String,
    val studentName: String,
    val status: String = "Pending", // Pending, Approved, Rejected
    val requestDate: Long = System.currentTimeMillis(),
    val approvedDate: Long? = null,
    val rejectionReason: String? = null
)

@Entity(tableName = "attendance")
data class AttendanceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studentId: Long,
    val studentName: String,
    val date: String, // YYYY-MM-DD
    val status: String, // Present, Absent, Late, Excused, On Approved Leave
    val shift: String = "Morning Shift",
    val notes: String? = null,
    val recordedBy: String = "Teacher",
    val timestamp: Long = System.currentTimeMillis(),
    val isRetentionReview: Boolean = false
)

@Entity(tableName = "quran_records")
data class QuranRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studentId: Long,
    val studentName: String,
    val program: String,
    val date: String,
    val recordType: String = "Daily", // Daily, Weekly, Bi-Weekly, Monthly
    val juz: Int? = null,
    val surah: String? = null,
    val versesOrLesson: String,
    val sabaq: String? = null,
    val sabqi: String? = null,
    val manzil: String? = null,
    val fluency: String, // Excellent, Good, Fair, Needs Improvement
    val tajweedPronunciation: String,
    val mistakes: Int = 0,
    val teacherComments: String? = null,
    val nextAssignment: String? = null,
    val recordedBy: String = "Teacher",
    val timestamp: Long = System.currentTimeMillis(),
    val isRetentionReview: Boolean = false
)

@Entity(tableName = "fee_payments")
data class FeePaymentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studentId: Long,
    val studentName: String,
    val instituteStudentCode: String,
    val invoiceMonth: String,
    val amountDue: Double,
    val amountPaid: Double,
    val paymentMethod: String, // Cash, Bank Transfer, EasyPaisa, JazzCash
    val transactionRef: String? = null,
    val evidenceUri: String? = null,
    val status: String, // Unpaid, Partially Paid, Pending Verification, Paid, Overdue, Rejected
    val submissionDate: Long = System.currentTimeMillis(),
    val verificationDate: Long? = null,
    val receiptNumber: String? = null,
    val verifiedBy: String? = null,
    val notes: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val isRetentionReview: Boolean = false
)

@Entity(tableName = "shifts")
data class ShiftEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String, // Sunrise Shift, Morning Shift, Afternoon Shift, Evening Shift
    val startTime: String,
    val endTime: String,
    val days: String,
    val assignedClass: String,
    val assignedTeacher: String,
    val room: String,
    val isActive: Boolean = true
)

@Entity(tableName = "prayer_timings")
data class PrayerTimingEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val prayerName: String, // Fajr, Dhuhr, Asr, Maghrib, Isha
    val timeString: String,
    val isCustomized: Boolean = false
)

@Entity(tableName = "leave_requests")
data class LeaveRequestEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studentId: Long,
    val studentName: String,
    val parentUsername: String,
    val startDate: String,
    val endDate: String,
    val reason: String,
    val status: String = "Pending", // Pending, Approved, Rejected
    val requestDate: Long = System.currentTimeMillis(),
    val responseDate: Long? = null,
    val responseNotes: String? = null
)

@Entity(tableName = "announcements")
data class AnnouncementEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val content: String,
    val targetGroup: String = "All", // All, Noorani Qaida, Nazra Quran, Hifz-ul-Quran
    val author: String = "Administration",
    val date: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "social_links")
data class SocialLinksEntity(
    @PrimaryKey val id: Long = 1,
    val whatsappGroupUrl: String = "",
    val facebookUrl: String = "",
    val instagramUrl: String = "",
    val youtubeUrl: String = "",
    val tiktokUrl: String = ""
)

@Entity(tableName = "audit_logs")
data class AuditLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val action: String,
    val details: String,
    val performedBy: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "retention_reviews")
data class RetentionReviewEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val recordType: String, // Sabaq, Sabqi, Manzil, Weekly Report, Biweekly Report, Monthly Test, Attendance, Fees
    val originalRecordId: Long,
    val studentName: String,
    val recordDate: String,
    val description: String,
    val status: String = "Pending", // Pending, ApprovedDeletion, Kept, Deferred
    val reviewedBy: String? = null,
    val reviewTimestamp: Long? = null
)

@Entity(tableName = "institute_settings")
data class InstituteSettingsEntity(
    @PrimaryKey val id: Long = 1,
    val authorizationCode: String = "14800",
    val instituteName: String = "Idara Al-Furqan",
    val primaryInstituteId: String = "IAF-2026",
    val secondaryInstituteId: String = "IAF-2027"
)

@Entity(tableName = "institute_weekly_days")
data class InstituteWeeklyDayEntity(
    @PrimaryKey val dayIndex: Int, // 1: Monday, 2: Tuesday, 3: Wednesday, 4: Thursday, 5: Friday, 6: Saturday, 7: Sunday
    val defaultDayName: String,     // "Monday", "Tuesday", etc.
    val customDayName: String,      // Editable name (e.g. "Monday", "Friday / جمعۃ المبارک", etc.)
    val isOn: Boolean = true,       // true: ON (working day), false: OFF (off day / holiday)
    val timingsOrNotes: String = "Regular Classes" // Editable notes / timings
)

