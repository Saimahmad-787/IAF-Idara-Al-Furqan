package com.example.data.repository

import com.example.data.local.*
import com.example.data.remote.FirestoreSyncManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.*

class IafRepository(
    private val db: IafDatabase,
    var syncManager: FirestoreSyncManager? = null
) {
    private val userDao = db.userDao()
    private val studentDao = db.studentDao()
    private val linkDao = db.parentStudentLinkDao()
    private val attendanceDao = db.attendanceDao()
    private val quranRecordDao = db.quranRecordDao()
    private val feeDao = db.feePaymentDao()
    private val shiftDao = db.shiftDao()
    private val prayerDao = db.prayerTimingDao()
    private val leaveDao = db.leaveRequestDao()
    private val announcementDao = db.announcementDao()
    private val socialLinksDao = db.socialLinksDao()
    private val auditDao = db.auditLogDao()
    private val retentionDao = db.retentionReviewDao()
    private val settingsDao = db.instituteSettingsDao()
    private val weeklyDayDao = db.instituteWeeklyDayDao()

    companion object {
        const val BOOTSTRAP_AUTH_CODE = "14800"
        const val INSTITUTE_ID = "IAF-2026"
        const val SECONDARY_INSTITUTE_ID = "IAF-2027"
    }

    suspend fun getAuthorizationCode(): String = withContext(Dispatchers.IO) {
        settingsDao.getSettingsDirect()?.authorizationCode ?: BOOTSTRAP_AUTH_CODE
    }

    suspend fun updateAuthorizationCode(newCode: String, teacherUsername: String): Result<String> = withContext(Dispatchers.IO) {
        val clean = newCode.trim()
        if (clean.length < 4) {
            return@withContext Result.failure(IllegalArgumentException("Authorization code must be at least 4 characters."))
        }
        val current = settingsDao.getSettingsDirect() ?: InstituteSettingsEntity()
        settingsDao.insertOrUpdate(current.copy(authorizationCode = clean))
        auditDao.insertLog(
            AuditLogEntity(
                action = "TEACHER_AUTH_CODE_CHANGED",
                details = "Teacher '$teacherUsername' updated institute authorization code to '$clean'.",
                performedBy = teacherUsername
            )
        )
        Result.success(clean)
    }

    fun getSettingsFlow(): Flow<InstituteSettingsEntity?> = settingsDao.getSettingsFlow()

    suspend fun initializeDefaultsIfNeeded() = withContext(Dispatchers.IO) {
        // Initialize settings if empty
        if (settingsDao.getSettingsDirect() == null) {
            settingsDao.insertOrUpdate(
                InstituteSettingsEntity(
                    id = 1,
                    authorizationCode = BOOTSTRAP_AUTH_CODE,
                    instituteName = "Idara Al-Furqan",
                    primaryInstituteId = INSTITUTE_ID,
                    secondaryInstituteId = SECONDARY_INSTITUTE_ID
                )
            )
        }

        // Initialize shifts if empty
        if (shiftDao.getShiftCount() == 0) {
            val defaultShifts = listOf(
                ShiftEntity(name = "Sunrise Shift", startTime = "06:00 AM", endTime = "08:00 AM", days = "Mon-Sat", assignedClass = "Hifz Morning", assignedTeacher = "Ustadh Ahmad", room = "Hall A"),
                ShiftEntity(name = "Morning Shift", startTime = "08:30 AM", endTime = "12:30 PM", days = "Mon-Sat", assignedClass = "Nazra & Qaida", assignedTeacher = "Ustadh Ahmad", room = "Room 1"),
                ShiftEntity(name = "Afternoon Shift", startTime = "02:00 PM", endTime = "05:00 PM", days = "Mon-Sat", assignedClass = "Hifz Revision", assignedTeacher = "Ustadh Ahmad", room = "Hall A"),
                ShiftEntity(name = "Evening Shift", startTime = "05:30 PM", endTime = "08:30 PM", days = "Mon-Sat", assignedClass = "Adults & Nazra", assignedTeacher = "Ustadh Ahmad", room = "Room 2")
            )
            shiftDao.insertAll(defaultShifts)
        }

        // Initialize prayer timings if empty
        if (prayerDao.getPrayerCount() == 0) {
            val defaultPrayers = listOf(
                PrayerTimingEntity(prayerName = "Fajr", timeString = "05:15 AM"),
                PrayerTimingEntity(prayerName = "Dhuhr", timeString = "01:15 PM"),
                PrayerTimingEntity(prayerName = "Asr", timeString = "04:30 PM"),
                PrayerTimingEntity(prayerName = "Maghrib", timeString = "06:15 PM"),
                PrayerTimingEntity(prayerName = "Isha", timeString = "08:00 PM")
            )
            prayerDao.insertAll(defaultPrayers)
        }

        // Initialize social links if empty
        if (socialLinksDao.getSocialLinksDirect() == null) {
            socialLinksDao.insertOrUpdate(
                SocialLinksEntity(
                    id = 1,
                    whatsappGroupUrl = "https://chat.whatsapp.com/iaf-furqan",
                    facebookUrl = "https://facebook.com/idara.alfurqan.official",
                    instagramUrl = "https://instagram.com/idara.alfurqan",
                    youtubeUrl = "https://youtube.com/@idaraalfurqan",
                    tiktokUrl = ""
                )
            )
        }

        // Initialize institute weekly days (Monday to Sunday) if empty
        if (weeklyDayDao.getDaysCount() == 0) {
            val defaultDays = listOf(
                InstituteWeeklyDayEntity(dayIndex = 1, defaultDayName = "Monday", customDayName = "Monday", isOn = true, timingsOrNotes = "08:00 AM - 01:00 PM"),
                InstituteWeeklyDayEntity(dayIndex = 2, defaultDayName = "Tuesday", customDayName = "Tuesday", isOn = true, timingsOrNotes = "08:00 AM - 01:00 PM"),
                InstituteWeeklyDayEntity(dayIndex = 3, defaultDayName = "Wednesday", customDayName = "Wednesday", isOn = true, timingsOrNotes = "08:00 AM - 01:00 PM"),
                InstituteWeeklyDayEntity(dayIndex = 4, defaultDayName = "Thursday", customDayName = "Thursday", isOn = true, timingsOrNotes = "08:00 AM - 01:00 PM"),
                InstituteWeeklyDayEntity(dayIndex = 5, defaultDayName = "Friday", customDayName = "Friday (Jumu'ah)", isOn = false, timingsOrNotes = "Weekly Off / Jumu'ah Holiday"),
                InstituteWeeklyDayEntity(dayIndex = 6, defaultDayName = "Saturday", customDayName = "Saturday", isOn = true, timingsOrNotes = "08:00 AM - 01:00 PM"),
                InstituteWeeklyDayEntity(dayIndex = 7, defaultDayName = "Sunday", customDayName = "Sunday", isOn = false, timingsOrNotes = "Weekly Off / Weekend")
            )
            weeklyDayDao.insertAll(defaultDays)
        }

        // 1. Teacher Demo Account
        if (userDao.getUserByUsername("teacher.demo") == null) {
            val teacherDemo = UserEntity(
                username = "teacher.demo",
                passwordHash = hashPassword("teacher123"),
                role = "Teacher",
                fullName = "Ustadh Ahmad Al-Furqan",
                phone = "0300-1122334",
                instituteId = INSTITUTE_ID
            )
            userDao.insertUser(teacherDemo)
            auditDao.insertLog(
                AuditLogEntity(
                    action = "DEMO_TEACHER_INITIALIZED",
                    details = "Teacher demo account 'teacher.demo' created.",
                    performedBy = "system"
                )
            )
        }

        // 2. Parent Demo Account
        if (userDao.getUserByUsername("parent.demo") == null) {
            val parentDemo = UserEntity(
                username = "parent.demo",
                passwordHash = hashPassword("parent123"),
                role = "Parent",
                fullName = "Sheikh Tariq Mahmood",
                phone = "0300-9988776",
                instituteId = INSTITUTE_ID
            )
            userDao.insertUser(parentDemo)
            auditDao.insertLog(
                AuditLogEntity(
                    action = "DEMO_PARENT_INITIALIZED",
                    details = "Parent demo account 'parent.demo' created.",
                    performedBy = "system"
                )
            )
        }

        // 3. Students:
        // "2 of same institue id same parents and everyhting mainly same with different roll number and 1 student with different institue id."
        if (studentDao.getStudentCount() == 0) {
            val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

            // Student 1 (IAF-2026, Parent Sheikh Tariq Mahmood, Roll RN-101)
            val stu1 = StudentEntity(
                instituteId = INSTITUTE_ID,
                instituteStudentCode = "IAF-STU-001",
                fullName = "Muhammad Ahmad",
                rollNumber = "RN-101",
                dobOrAge = "11 years",
                fatherName = "Sheikh Tariq Mahmood",
                phone = "0300-9988776",
                address = "House 14, Street 5, Sector F-8, Islamabad",
                admissionDate = "2026-08-15",
                program = "Hifz-ul-Quran",
                className = "Morning Hifz Group A",
                assignedTeacher = "Ustadh Ahmad Al-Furqan",
                monthlyFee = 2500.0,
                feeStartDate = "2026-08-15",
                dueDate = "10th of every month",
                status = "Active"
            )
            val id1 = studentDao.insertStudent(stu1)

            // Student 2 (Same institute IAF-2026, Same parent Sheikh Tariq Mahmood, Same phone & address, DIFFERENT Roll Number RN-102)
            val stu2 = StudentEntity(
                instituteId = INSTITUTE_ID,
                instituteStudentCode = "IAF-STU-002",
                fullName = "Zainab Ahmad",
                rollNumber = "RN-102",
                dobOrAge = "9 years",
                fatherName = "Sheikh Tariq Mahmood",
                phone = "0300-9988776",
                address = "House 14, Street 5, Sector F-8, Islamabad",
                admissionDate = "2026-08-15",
                program = "Nazra Quran",
                className = "Afternoon Nazra Group B",
                assignedTeacher = "Ustadh Ahmad Al-Furqan",
                monthlyFee = 2000.0,
                feeStartDate = "2026-08-15",
                dueDate = "10th of every month",
                status = "Active"
            )
            val id2 = studentDao.insertStudent(stu2)

            // Student 3 (DIFFERENT Institute ID: IAF-2027)
            val stu3 = StudentEntity(
                instituteId = SECONDARY_INSTITUTE_ID,
                instituteStudentCode = "IAF2-STU-003",
                fullName = "Abdullah Tariq",
                rollNumber = "RN-201",
                dobOrAge = "7 years",
                fatherName = "Tariq Mehmood",
                phone = "0333-5566778",
                address = "Plot 88, Satellite Town, Rawalpindi",
                admissionDate = "2026-09-01",
                program = "Noorani Qaida",
                className = "Qaida Elementary",
                assignedTeacher = "Ustadh Bilal",
                monthlyFee = 1800.0,
                feeStartDate = "2026-09-01",
                dueDate = "10th of every month",
                status = "Active"
            )
            studentDao.insertStudent(stu3)

            // Link Student 1 and Student 2 to parent.demo (both approved)
            linkDao.insertLink(
                ParentStudentLinkEntity(
                    parentUsername = "parent.demo",
                    studentId = id1,
                    instituteStudentCode = "IAF-STU-001",
                    studentName = "Muhammad Ahmad",
                    status = "Approved",
                    approvedDate = System.currentTimeMillis()
                )
            )
            linkDao.insertLink(
                ParentStudentLinkEntity(
                    parentUsername = "parent.demo",
                    studentId = id2,
                    instituteStudentCode = "IAF-STU-002",
                    studentName = "Zainab Ahmad",
                    status = "Approved",
                    approvedDate = System.currentTimeMillis()
                )
            )

            // Seed sample attendance for Student 1 & 2
            attendanceDao.insertAll(
                listOf(
                    AttendanceEntity(studentId = id1, studentName = "Muhammad Ahmad", date = dateStr, status = "Present", recordedBy = "teacher.demo"),
                    AttendanceEntity(studentId = id2, studentName = "Zainab Ahmad", date = dateStr, status = "Present", recordedBy = "teacher.demo")
                )
            )

            // Seed sample Quran records across Daily, Weekly, Bi-Weekly, Monthly frequencies
            // 1. Student 1: Hifz-ul-Quran (Sabaq, Sabqi, Manzil)
            quranRecordDao.insertRecord(
                QuranRecordEntity(
                    studentId = id1,
                    studentName = "Muhammad Ahmad",
                    program = "Hifz-ul-Quran",
                    date = dateStr,
                    recordType = "Daily",
                    juz = 1,
                    surah = "Al-Baqarah",
                    versesOrLesson = "Verses 60-74",
                    sabaq = "Surah Al-Baqarah Page 10-11",
                    sabqi = "Juz 1 Quarter 1-2 (Pages 1-8)",
                    manzil = "Juz 30 (Surah An-Naba to An-Nas)",
                    fluency = "Good",
                    tajweedPronunciation = "Accurate",
                    mistakes = 1,
                    teacherComments = "Daily Sabaq recited well. Minor hesitation on Ayah 68.",
                    nextAssignment = "Page 12 (Ayah 75-83)",
                    recordedBy = "teacher.demo"
                )
            )
            quranRecordDao.insertRecord(
                QuranRecordEntity(
                    studentId = id1,
                    studentName = "Muhammad Ahmad",
                    program = "Hifz-ul-Quran",
                    date = dateStr,
                    recordType = "Weekly",
                    juz = 1,
                    surah = "Al-Baqarah",
                    versesOrLesson = "Pages 1-15 Summary",
                    sabaq = "Juz 1 Pages 1-15 (New Memorization)",
                    sabqi = "Juz 1 First Half Comprehensive",
                    manzil = "Juz 29 & Juz 30",
                    fluency = "Good",
                    tajweedPronunciation = "Clear Makharij",
                    mistakes = 2,
                    teacherComments = "Weekly Hifz Report: Consistent daily attendance. Sabqi pace is strong.",
                    nextAssignment = "Continue with Page 16",
                    recordedBy = "teacher.demo"
                )
            )
            quranRecordDao.insertRecord(
                QuranRecordEntity(
                    studentId = id1,
                    studentName = "Muhammad Ahmad",
                    program = "Hifz-ul-Quran",
                    date = dateStr,
                    recordType = "Bi-Weekly",
                    juz = 1,
                    surah = "Al-Baqarah",
                    versesOrLesson = "Bi-Weekly Progress Evaluation",
                    sabaq = "Juz 1 Pages 1-20 Evaluated",
                    sabqi = "Juz 1 Quarter 3-4",
                    manzil = "Juz 28, 29 & 30",
                    fluency = "Excellent",
                    tajweedPronunciation = "Excellent",
                    mistakes = 1,
                    teacherComments = "14-Day Bi-Weekly Progress: Optimal pace maintained. Memorization retention is solid.",
                    nextAssignment = "Final quarter of Juz 1",
                    recordedBy = "teacher.demo"
                )
            )
            quranRecordDao.insertRecord(
                QuranRecordEntity(
                    studentId = id1,
                    studentName = "Muhammad Ahmad",
                    program = "Hifz-ul-Quran",
                    date = dateStr,
                    recordType = "Monthly",
                    juz = 1,
                    surah = "Al-Baqarah",
                    versesOrLesson = "Monthly Hifz Certification Test",
                    sabaq = "Juz 1 Full Juz Memorized",
                    sabqi = "Juz 1 Cumulative Review",
                    manzil = "Juz 28 to 30",
                    fluency = "Excellent",
                    tajweedPronunciation = "Mumtaz (A+)",
                    mistakes = 0,
                    teacherComments = "Monthly Assessment: Juz 1 memorized with accurate tajweed. Ready to commence Juz 2.",
                    nextAssignment = "Begin Juz 2 (Sayaqool)",
                    recordedBy = "teacher.demo"
                )
            )

            // 2. Student 2: Nazra Quran (Sabaq)
            quranRecordDao.insertRecord(
                QuranRecordEntity(
                    studentId = id2,
                    studentName = "Zainab Ahmad",
                    program = "Nazra Quran",
                    date = dateStr,
                    recordType = "Daily",
                    versesOrLesson = "Surah Maryam Verses 1-15",
                    sabaq = "Surah Maryam Verses 1-15",
                    fluency = "Excellent",
                    tajweedPronunciation = "Clear Madd and Qalqalah",
                    mistakes = 0,
                    teacherComments = "Recited with clear articulation and correct pausing.",
                    nextAssignment = "Surah Maryam Verses 16-30",
                    recordedBy = "teacher.demo"
                )
            )
            quranRecordDao.insertRecord(
                QuranRecordEntity(
                    studentId = id2,
                    studentName = "Zainab Ahmad",
                    program = "Nazra Quran",
                    date = dateStr,
                    recordType = "Weekly",
                    versesOrLesson = "Surah Maryam Verses 1-45",
                    sabaq = "Surah Maryam Ruku 1 to 3",
                    fluency = "Good",
                    tajweedPronunciation = "Accurate pronunciation of difficult letters",
                    mistakes = 1,
                    teacherComments = "Weekly Nazra Review: Steady improvement in fluency.",
                    nextAssignment = "Surah Maryam Ruku 4-5",
                    recordedBy = "teacher.demo"
                )
            )
            quranRecordDao.insertRecord(
                QuranRecordEntity(
                    studentId = id2,
                    studentName = "Zainab Ahmad",
                    program = "Nazra Quran",
                    date = dateStr,
                    recordType = "Bi-Weekly",
                    versesOrLesson = "Surah Maryam Complete",
                    sabaq = "Surah Maryam Verses 1-98",
                    fluency = "Excellent",
                    tajweedPronunciation = "Proper stopping rules (Waqf)",
                    mistakes = 0,
                    teacherComments = "Bi-Weekly Evaluation: Completed Surah Maryam fluently.",
                    nextAssignment = "Begin Surah Ta-Ha",
                    recordedBy = "teacher.demo"
                )
            )
            quranRecordDao.insertRecord(
                QuranRecordEntity(
                    studentId = id2,
                    studentName = "Zainab Ahmad",
                    program = "Nazra Quran",
                    date = dateStr,
                    recordType = "Monthly",
                    versesOrLesson = "Juz 16 Comprehensive Nazra Test",
                    sabaq = "Juz 16 (Surah Al-Kahf & Maryam)",
                    fluency = "Excellent",
                    tajweedPronunciation = "Exam Grade: 98/100",
                    mistakes = 0,
                    teacherComments = "Monthly Nazra Test: Completed Juz 16 with distinction.",
                    nextAssignment = "Commence Juz 17 (Iqtaraba)",
                    recordedBy = "teacher.demo"
                )
            )

            // Seed sample Fee payments
            feeDao.insertFee(
                FeePaymentEntity(
                    studentId = id1,
                    studentName = "Muhammad Ahmad",
                    instituteStudentCode = "IAF-STU-001",
                    invoiceMonth = "September 2026",
                    amountDue = 2500.0,
                    amountPaid = 2500.0,
                    paymentMethod = "Bank Transfer",
                    transactionRef = "TXN-984210",
                    status = "Paid",
                    receiptNumber = "IAF-REC-1001",
                    verifiedBy = "teacher.demo"
                )
            )
            feeDao.insertFee(
                FeePaymentEntity(
                    studentId = id1,
                    studentName = "Muhammad Ahmad",
                    instituteStudentCode = "IAF-STU-001",
                    invoiceMonth = "October 2026",
                    amountDue = 2500.0,
                    amountPaid = 0.0,
                    paymentMethod = "Cash",
                    status = "Unpaid"
                )
            )
            feeDao.insertFee(
                FeePaymentEntity(
                    studentId = id2,
                    studentName = "Zainab Ahmad",
                    instituteStudentCode = "IAF-STU-002",
                    invoiceMonth = "October 2026",
                    amountDue = 2000.0,
                    amountPaid = 2000.0,
                    paymentMethod = "EasyPaisa",
                    transactionRef = "EP-441290",
                    status = "Paid",
                    receiptNumber = "IAF-REC-1002",
                    verifiedBy = "teacher.demo"
                )
            )
        }
    }

    // --- Authentication & User Management ---

    private fun hashPassword(password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    suspend fun isBootstrapAvailable(): Boolean = withContext(Dispatchers.IO) {
        userDao.getTeacherCount() == 0
    }

    suspend fun registerTeacher(
        authCode: String,
        username: String,
        password: String,
        fullName: String,
        phone: String,
        instituteId: String
    ): Result<UserEntity> = withContext(Dispatchers.IO) {
        val currentCode = getAuthorizationCode()
        if (authCode.trim() != currentCode) {
            return@withContext Result.failure(IllegalArgumentException("Invalid authorization code. Please contact the administrator."))
        }
        val cleanUsername = username.trim().lowercase()
        if (cleanUsername.isEmpty() || password.length < 6) {
            return@withContext Result.failure(IllegalArgumentException("Username and password (min 6 chars) are required."))
        }
        if (userDao.getUserByUsername(cleanUsername) != null) {
            return@withContext Result.failure(IllegalArgumentException("Username already exists."))
        }

        val teacher = UserEntity(
            username = cleanUsername,
            passwordHash = hashPassword(password),
            role = "Teacher",
            fullName = fullName.ifBlank { "Teacher" },
            phone = phone,
            instituteId = instituteId.ifBlank { INSTITUTE_ID }
        )
        userDao.insertUser(teacher)
        auditDao.insertLog(
            AuditLogEntity(
                action = "TEACHER_REGISTERED",
                details = "Teacher account '$cleanUsername' registered using authorization code.",
                performedBy = cleanUsername
            )
        )
        Result.success(teacher)
    }

    suspend fun bootstrapFirstTeacher(
        code: String,
        username: String,
        password: String,
        fullName: String,
        phone: String
    ): Result<UserEntity> = withContext(Dispatchers.IO) {
        val currentCode = getAuthorizationCode()
        if (code.trim() != currentCode) {
            return@withContext Result.failure(IllegalArgumentException("Invalid authorization code."))
        }
        if (userDao.getTeacherCount() > 0) {
            return@withContext Result.failure(IllegalStateException("Teacher bootstrap is no longer available."))
        }
        val cleanUsername = username.trim().lowercase()
        if (cleanUsername.isEmpty() || password.length < 6) {
            return@withContext Result.failure(IllegalArgumentException("Username and password (min 6 chars) are required."))
        }
        if (userDao.getUserByUsername(cleanUsername) != null) {
            return@withContext Result.failure(IllegalArgumentException("Username already exists."))
        }

        val teacher = UserEntity(
            username = cleanUsername,
            passwordHash = hashPassword(password),
            role = "Teacher",
            fullName = fullName.ifBlank { "Institute Administrator" },
            phone = phone,
            instituteId = INSTITUTE_ID
        )
        userDao.insertUser(teacher)
        auditDao.insertLog(
            AuditLogEntity(
                action = "TEACHER_BOOTSTRAP",
                details = "First teacher account '$cleanUsername' bootstrapped successfully.",
                performedBy = cleanUsername
            )
        )
        Result.success(teacher)
    }

    suspend fun registerParent(
        username: String,
        password: String,
        fullName: String,
        phone: String
    ): Result<UserEntity> = withContext(Dispatchers.IO) {
        val cleanUsername = username.trim().lowercase()
        if (cleanUsername.isEmpty() || password.length < 6) {
            return@withContext Result.failure(IllegalArgumentException("Username and password (min 6 chars) are required."))
        }
        if (userDao.getUserByUsername(cleanUsername) != null) {
            return@withContext Result.failure(IllegalArgumentException("Username is already taken."))
        }

        val parent = UserEntity(
            username = cleanUsername,
            passwordHash = hashPassword(password),
            role = "Parent",
            fullName = fullName.ifBlank { "Parent" },
            phone = phone,
            instituteId = INSTITUTE_ID
        )
        userDao.insertUser(parent)
        auditDao.insertLog(
            AuditLogEntity(
                action = "PARENT_REGISTRATION",
                details = "Parent account '$cleanUsername' registered.",
                performedBy = cleanUsername
            )
        )
        Result.success(parent)
    }

    suspend fun authenticateTeacher(
        username: String,
        password: String
    ): Result<UserEntity> = withContext(Dispatchers.IO) {
        val cleanUsername = username.trim().lowercase()
        val user = userDao.getUserByUsername(cleanUsername)
            ?: return@withContext Result.failure(IllegalArgumentException("Incorrect teacher username or password."))

        if (user.role != "Teacher") {
            return@withContext Result.failure(IllegalArgumentException("This account is not a teacher account. Please use the Parent Portal."))
        }

        val inputHash = hashPassword(password)
        if (user.passwordHash != inputHash) {
            return@withContext Result.failure(IllegalArgumentException("Incorrect teacher username or password."))
        }

        auditDao.insertLog(
            AuditLogEntity(
                action = "TEACHER_LOGIN",
                details = "Teacher '$cleanUsername' signed in.",
                performedBy = cleanUsername
            )
        )
        Result.success(user)
    }

    suspend fun authenticateParent(
        username: String,
        password: String
    ): Result<UserEntity> = withContext(Dispatchers.IO) {
        val cleanUsername = username.trim().lowercase()
        val user = userDao.getUserByUsername(cleanUsername)
            ?: return@withContext Result.failure(IllegalArgumentException("Incorrect username or password."))

        val inputHash = hashPassword(password)
        if (user.passwordHash != inputHash) {
            return@withContext Result.failure(IllegalArgumentException("Incorrect username or password."))
        }

        auditDao.insertLog(
            AuditLogEntity(
                action = "PARENT_LOGIN",
                details = "Parent/Student '$cleanUsername' signed in.",
                performedBy = cleanUsername
            )
        )
        Result.success(user)
    }

    suspend fun authenticate(
        username: String,
        password: String,
        instituteId: String
    ): Result<UserEntity> = withContext(Dispatchers.IO) {
        val cleanId = instituteId.trim().uppercase()
        val validIds = listOf(INSTITUTE_ID, SECONDARY_INSTITUTE_ID)
        if (cleanId !in validIds && !cleanId.startsWith("IAF")) {
            return@withContext Result.failure(IllegalArgumentException("Invalid Institute ID. Valid IDs: $INSTITUTE_ID, $SECONDARY_INSTITUTE_ID"))
        }

        val cleanUsername = username.trim().lowercase()
        val user = userDao.getUserByUsername(cleanUsername)
            ?: return@withContext Result.failure(IllegalArgumentException("Incorrect username or password."))

        val inputHash = hashPassword(password)
        if (user.passwordHash != inputHash) {
            return@withContext Result.failure(IllegalArgumentException("Incorrect username or password."))
        }

        auditDao.insertLog(
            AuditLogEntity(
                action = "USER_LOGIN",
                details = "User '$cleanUsername' signed in as ${user.role} with Institute ID $cleanId.",
                performedBy = cleanUsername
            )
        )
        Result.success(user)
    }

    suspend fun changePassword(
        username: String,
        currentPassword: String,
        newPassword: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val cleanUsername = username.trim().lowercase()
        val user = userDao.getUserByUsername(cleanUsername)
            ?: return@withContext Result.failure(IllegalArgumentException("User account not found."))

        val currentHash = hashPassword(currentPassword)
        if (user.passwordHash != currentHash) {
            return@withContext Result.failure(IllegalArgumentException("Current password is incorrect."))
        }

        if (newPassword.length < 6) {
            return@withContext Result.failure(IllegalArgumentException("New password must be at least 6 characters."))
        }

        val newHash = hashPassword(newPassword)
        userDao.updatePassword(cleanUsername, newHash)
        auditDao.insertLog(
            AuditLogEntity(
                action = "PASSWORD_CHANGED",
                details = "Password updated for account '$cleanUsername'.",
                performedBy = cleanUsername
            )
        )
        Result.success(Unit)
    }

    suspend fun changeUsername(
        currentUsername: String,
        newUsername: String,
        passwordVerification: String
    ): Result<String> = withContext(Dispatchers.IO) {
        val oldClean = currentUsername.trim().lowercase()
        val newClean = newUsername.trim().lowercase()

        if (newClean.isEmpty() || newClean.length < 3) {
            return@withContext Result.failure(IllegalArgumentException("New username must be at least 3 characters."))
        }
        if (oldClean == newClean) {
            return@withContext Result.failure(IllegalArgumentException("New username must be different from current username."))
        }
        val user = userDao.getUserByUsername(oldClean)
            ?: return@withContext Result.failure(IllegalArgumentException("User account not found."))

        val passHash = hashPassword(passwordVerification)
        if (user.passwordHash != passHash) {
            return@withContext Result.failure(IllegalArgumentException("Incorrect password for verification."))
        }

        if (userDao.getUserByUsername(newClean) != null) {
            return@withContext Result.failure(IllegalArgumentException("Username '$newClean' is already taken."))
        }

        // Insert new user and delete old
        val newUser = user.copy(username = newClean)
        userDao.insertUser(newUser)
        userDao.deleteUserByUsername(oldClean)

        if (user.role == "Parent") {
            linkDao.updateParentUsername(oldClean, newClean)
            leaveDao.updateParentUsername(oldClean, newClean)
        }

        auditDao.insertLog(
            AuditLogEntity(
                action = "USERNAME_CHANGED",
                details = "User '$oldClean' updated their username to '$newClean'.",
                performedBy = newClean
            )
        )
        Result.success(newClean)
    }

    // --- Student Management ---

    fun getAllStudents(): Flow<List<StudentEntity>> = studentDao.getAllStudents()

    suspend fun getStudentById(id: Long): StudentEntity? = withContext(Dispatchers.IO) {
        studentDao.getStudentById(id)
    }

    suspend fun getStudentByInstituteCode(code: String): StudentEntity? = withContext(Dispatchers.IO) {
        studentDao.getStudentByInstituteCode(code.trim().uppercase())
    }

    suspend fun createStudent(
        instituteStudentCode: String,
        fullName: String,
        photoUri: String? = null,
        rollNumber: String?,
        dobOrAge: String,
        fatherName: String,
        fatherCnic: String,
        fatherCnicPhotoUri: String? = null,
        studentCnicOrBForm: String,
        studentBFormPhotoUri: String? = null,
        cnic: String?,
        emergencyContact: String?,
        phone: String,
        address: String,
        admissionDate: String,
        program: String,
        className: String,
        assignedTeacher: String,
        monthlyFee: Double,
        feeStartDate: String,
        dueDate: String,
        approvedConcession: Double,
        notes: String?,
        teacherUsername: String
    ): Result<StudentEntity> = withContext(Dispatchers.IO) {
        val studentCode = instituteStudentCode.trim().uppercase()
        if (studentCode.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("Institute ID is required and must be assigned by teacher."))
        }
        val cleanFatherCnic = fatherCnic.trim()
        if (cleanFatherCnic.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("Father's CNIC is mandatory."))
        }
        val cleanStudentCnic = studentCnicOrBForm.trim()
        if (cleanStudentCnic.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("Student CNIC / B-Form is mandatory."))
        }

        val student = StudentEntity(
            instituteId = INSTITUTE_ID,
            instituteStudentCode = studentCode,
            fullName = fullName.trim(),
            photoUri = photoUri?.trim()?.ifBlank { null },
            rollNumber = rollNumber?.trim()?.ifBlank { null },
            dobOrAge = dobOrAge.trim(),
            fatherName = fatherName.trim(),
            fatherCnic = cleanFatherCnic,
            fatherCnicPhotoUri = fatherCnicPhotoUri?.trim()?.ifBlank { null },
            studentCnicOrBForm = cleanStudentCnic,
            studentBFormPhotoUri = studentBFormPhotoUri?.trim()?.ifBlank { null },
            cnic = cnic?.trim()?.ifBlank { null },
            emergencyContact = emergencyContact?.trim()?.ifBlank { null },
            phone = phone.trim(),
            address = address.trim(),
            admissionDate = admissionDate.trim().ifBlank { SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()) },
            program = program,
            className = className.trim().ifBlank { "General" },
            assignedTeacher = assignedTeacher.trim().ifBlank { "Head Ustadh" },
            monthlyFee = monthlyFee,
            feeStartDate = feeStartDate.trim().ifBlank { SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()) },
            dueDate = dueDate.trim().ifBlank { "10th of every month" },
            approvedConcession = approvedConcession,
            notes = notes?.trim()?.ifBlank { null },
            status = "Active"
        )

        val id = studentDao.insertStudent(student)
        val createdStudent = student.copy(id = id)

        auditDao.insertLog(
            AuditLogEntity(
                action = "STUDENT_CREATED",
                details = "Student '${student.fullName}' created with code $studentCode in program '$program'.",
                performedBy = teacherUsername
            )
        )

        // Create initial pending fee entry for current month
        val currentMonth = SimpleDateFormat("MMMM yyyy", Locale.US).format(Date())
        val initialFee = FeePaymentEntity(
            studentId = id,
            studentName = student.fullName,
            instituteStudentCode = studentCode,
            invoiceMonth = currentMonth,
            amountDue = maxOf(0.0, monthlyFee - approvedConcession),
            amountPaid = 0.0,
            paymentMethod = "Cash",
            status = "Unpaid"
        )
        feeDao.insertFee(initialFee)

        syncManager?.pushStudent(createdStudent)
        syncManager?.pushFeePayment(initialFee)

        Result.success(createdStudent)
    }

    suspend fun updateStudent(student: StudentEntity, teacherUsername: String) = withContext(Dispatchers.IO) {
        studentDao.updateStudent(student)
        syncManager?.pushStudent(student)
        auditDao.insertLog(
            AuditLogEntity(
                action = "STUDENT_UPDATED",
                details = "Student '${student.fullName}' (${student.instituteStudentCode}) record updated.",
                performedBy = teacherUsername
            )
        )
    }

    suspend fun deleteStudent(studentId: Long, teacherUsername: String) = withContext(Dispatchers.IO) {
        val student = studentDao.getStudentById(studentId)
        studentDao.deleteStudentById(studentId)
        auditDao.insertLog(
            AuditLogEntity(
                action = "STUDENT_DELETED",
                details = "Student '${student?.fullName}' (${student?.instituteStudentCode}) removed by teacher.",
                performedBy = teacherUsername
            )
        )
    }

    // --- Parent-Student Link Verification (One Parent -> Multiple Students) ---

    fun getLinksForParent(parentUsername: String): Flow<List<ParentStudentLinkEntity>> =
        linkDao.getLinksForParent(parentUsername)

    fun getApprovedLinksForParent(parentUsername: String): Flow<List<ParentStudentLinkEntity>> =
        linkDao.getApprovedLinksForParent(parentUsername)

    fun getAllPendingLinkRequests(): Flow<List<ParentStudentLinkEntity>> =
        linkDao.getAllPendingRequests()

    fun getAllLinkRequests(): Flow<List<ParentStudentLinkEntity>> =
        linkDao.getAllLinks()

    suspend fun requestStudentLink(
        parentUsername: String,
        instituteStudentCode: String
    ): Result<ParentStudentLinkEntity> = withContext(Dispatchers.IO) {
        val code = instituteStudentCode.trim().uppercase()
        val matchingStudents = studentDao.getStudentsByInstituteCode(code)
        if (matchingStudents.isEmpty()) {
            return@withContext Result.failure(
                IllegalArgumentException("We could not find this institute ID ($code). Please contact your institute ustadh to verify the code.")
            )
        }

        var firstCreated: ParentStudentLinkEntity? = null
        for (student in matchingStudents) {
            val existing = linkDao.findExistingLink(parentUsername, student.id, code)
            if (existing != null && existing.status == "Approved") {
                if (matchingStudents.size == 1) {
                    return@withContext Result.failure(IllegalArgumentException("You are already linked to ${student.fullName}."))
                }
                continue
            }

            val linkRequest = ParentStudentLinkEntity(
                id = existing?.id ?: 0,
                parentUsername = parentUsername,
                studentId = student.id,
                instituteStudentCode = code,
                studentName = student.fullName,
                status = "Pending",
                requestDate = System.currentTimeMillis()
            )
            val id = linkDao.insertLink(linkRequest)
            if (firstCreated == null) {
                firstCreated = linkRequest.copy(id = id)
            }
        }

        val resultEntity = firstCreated ?: ParentStudentLinkEntity(
            parentUsername = parentUsername,
            studentId = matchingStudents.first().id,
            instituteStudentCode = code,
            studentName = matchingStudents.first().fullName,
            status = "Pending"
        )
        syncManager?.pushParentStudentLink(resultEntity)

        auditDao.insertLog(
            AuditLogEntity(
                action = "PARENT_LINK_REQUESTED",
                details = "Parent '$parentUsername' requested link to code $code (${matchingStudents.size} student(s) identified).",
                performedBy = parentUsername
            )
        )
        Result.success(resultEntity)
    }

    suspend fun reviewLinkRequest(
        linkId: Long,
        approve: Boolean,
        rejectionReason: String?,
        teacherUsername: String
    ) = withContext(Dispatchers.IO) {
        val allLinks = linkDao.getAllLinks().first()
        val link = allLinks.find { it.id == linkId } ?: return@withContext
        val updated = link.copy(
            status = if (approve) "Approved" else "Rejected",
            approvedDate = if (approve) System.currentTimeMillis() else null,
            rejectionReason = if (!approve) rejectionReason ?: "Rejected by institute administration" else null
        )
        linkDao.updateLink(updated)
        syncManager?.pushParentStudentLink(updated)

        auditDao.insertLog(
            AuditLogEntity(
                action = if (approve) "PARENT_LINK_APPROVED" else "PARENT_LINK_REJECTED",
                details = "Teacher '$teacherUsername' ${if (approve) "approved" else "rejected"} parent '${link.parentUsername}' link for student '${link.studentName}' (${link.instituteStudentCode}).",
                performedBy = teacherUsername
            )
        )
    }

    // --- Attendance ---

    fun getAttendanceForDate(date: String): Flow<List<AttendanceEntity>> =
        attendanceDao.getAttendanceForDate(date)

    fun getAttendanceForStudent(studentId: Long): Flow<List<AttendanceEntity>> =
        attendanceDao.getAttendanceForStudent(studentId)

    suspend fun saveBatchAttendance(
        date: String,
        records: List<AttendanceEntity>,
        teacherUsername: String
    ) = withContext(Dispatchers.IO) {
        attendanceDao.insertAll(records)
        records.forEach { syncManager?.pushAttendance(it) }
        auditDao.insertLog(
            AuditLogEntity(
                action = "ATTENDANCE_RECORDED",
                details = "Recorded attendance for ${records.size} students on date $date.",
                performedBy = teacherUsername
            )
        )
    }

    suspend fun updateAttendanceRecord(
        record: AttendanceEntity,
        teacherUsername: String
    ) = withContext(Dispatchers.IO) {
        attendanceDao.updateAttendance(record)
        syncManager?.pushAttendance(record)
        auditDao.insertLog(
            AuditLogEntity(
                action = "ATTENDANCE_UPDATED",
                details = "Updated attendance for student '${record.studentName}' on ${record.date} to ${record.status}.",
                performedBy = teacherUsername
            )
        )
    }

    // --- Quran Learning Tracker ---

    fun getQuranRecordsForStudent(studentId: Long): Flow<List<QuranRecordEntity>> =
        quranRecordDao.getRecordsForStudent(studentId)

    fun getAllQuranRecords(): Flow<List<QuranRecordEntity>> =
        quranRecordDao.getAllRecords()

    suspend fun recordQuranProgress(
        record: QuranRecordEntity,
        teacherUsername: String
    ) = withContext(Dispatchers.IO) {
        quranRecordDao.insertRecord(record)
        syncManager?.pushQuranRecord(record)
        auditDao.insertLog(
            AuditLogEntity(
                action = "QURAN_RECORD_SAVED",
                details = "Quran progress recorded for student '${record.studentName}' (${record.program}) on ${record.date}.",
                performedBy = teacherUsername
            )
        )
    }

    // --- Fee Management & Receipts ---

    fun getFeesForStudent(studentId: Long): Flow<List<FeePaymentEntity>> =
        feeDao.getFeesForStudent(studentId)

    fun getAllFees(): Flow<List<FeePaymentEntity>> =
        feeDao.getAllFees()

    fun getPendingVerificationFees(): Flow<List<FeePaymentEntity>> =
        feeDao.getPendingVerificationFees()

    suspend fun submitFeePayment(
        studentId: Long,
        amount: Double,
        paymentMethod: String,
        transactionRef: String?,
        invoiceMonth: String,
        notes: String?,
        evidenceUri: String? = null,
        submittedByUsername: String
    ): Result<FeePaymentEntity> = withContext(Dispatchers.IO) {
        val student = studentDao.getStudentById(studentId)
            ?: return@withContext Result.failure(IllegalArgumentException("Student not found."))

        val payment = FeePaymentEntity(
            studentId = studentId,
            studentName = student.fullName,
            instituteStudentCode = student.instituteStudentCode,
            invoiceMonth = invoiceMonth.ifBlank { SimpleDateFormat("MMMM yyyy", Locale.US).format(Date()) },
            amountDue = student.monthlyFee - student.approvedConcession,
            amountPaid = amount,
            paymentMethod = paymentMethod,
            transactionRef = transactionRef?.trim()?.ifBlank { null },
            evidenceUri = evidenceUri?.trim()?.ifBlank { null },
            status = "Pending Verification",
            notes = notes?.trim()?.ifBlank { null }
        )
        val id = feeDao.insertFee(payment)
        val created = payment.copy(id = id)
        syncManager?.pushFeePayment(created)

        auditDao.insertLog(
            AuditLogEntity(
                action = "FEE_PAYMENT_SUBMITTED",
                details = "Payment of Rs $amount submitted for '${student.fullName}' (${payment.invoiceMonth}) via $paymentMethod with ${if (!evidenceUri.isNullOrBlank()) "screenshot proof" else "no screenshot"}. Pending teacher verification.",
                performedBy = submittedByUsername
            )
        )
        Result.success(created)
    }

    suspend fun verifyFeePayment(
        feeId: Long,
        approve: Boolean,
        notes: String?,
        teacherUsername: String
    ) = withContext(Dispatchers.IO) {
        val fee = feeDao.getFeeById(feeId) ?: return@withContext
        val receiptNumber = if (approve) "IAF-REC-%04d".format(feeId + 1000) else null
        val updated = fee.copy(
            status = if (approve) "Paid" else "Rejected",
            verificationDate = System.currentTimeMillis(),
            receiptNumber = receiptNumber,
            verifiedBy = teacherUsername,
            notes = notes ?: fee.notes
        )
        feeDao.updateFee(updated)
        syncManager?.pushFeePayment(updated)

        auditDao.insertLog(
            AuditLogEntity(
                action = if (approve) "FEE_PAYMENT_APPROVED" else "FEE_PAYMENT_REJECTED",
                details = "Payment of Rs ${fee.amountPaid} for student '${fee.studentName}' ${if (approve) "approved with receipt $receiptNumber" else "rejected"}.",
                performedBy = teacherUsername
            )
        )
    }

    // --- Shifts & Prayer Timings ---

    fun getAllShifts(): Flow<List<ShiftEntity>> = shiftDao.getAllShifts()

    suspend fun addShift(
        name: String,
        startTime: String,
        endTime: String,
        days: String,
        assignedClass: String,
        assignedTeacher: String,
        room: String,
        teacherUsername: String
    ) = withContext(Dispatchers.IO) {
        val shift = ShiftEntity(
            name = name.trim(),
            startTime = startTime.trim(),
            endTime = endTime.trim(),
            days = days.trim().ifBlank { "Monday - Saturday" },
            assignedClass = assignedClass.trim().ifBlank { "General" },
            assignedTeacher = assignedTeacher.trim().ifBlank { teacherUsername },
            room = room.trim().ifBlank { "Hall A" }
        )
        val id = shiftDao.insertShift(shift)
        auditDao.insertLog(
            AuditLogEntity(
                action = "SHIFT_ADDED",
                details = "New shift '$name' ($startTime - $endTime) created.",
                performedBy = teacherUsername
            )
        )
        id
    }

    suspend fun updateShift(shift: ShiftEntity, teacherUsername: String) = withContext(Dispatchers.IO) {
        shiftDao.updateShift(shift)
        auditDao.insertLog(
            AuditLogEntity(
                action = "SHIFT_UPDATED",
                details = "Institute shift '${shift.name}' updated (${shift.startTime} - ${shift.endTime}).",
                performedBy = teacherUsername
            )
        )
    }

    suspend fun deleteShift(shiftId: Long, teacherUsername: String) = withContext(Dispatchers.IO) {
        shiftDao.deleteShift(shiftId)
        auditDao.insertLog(
            AuditLogEntity(
                action = "SHIFT_DELETED",
                details = "Institute shift ID $shiftId deleted.",
                performedBy = teacherUsername
            )
        )
    }

    fun getAllPrayerTimings(): Flow<List<PrayerTimingEntity>> = prayerDao.getAllPrayerTimings()

    suspend fun updatePrayerTiming(timing: PrayerTimingEntity, teacherUsername: String) = withContext(Dispatchers.IO) {
        prayerDao.updateTiming(timing.copy(isCustomized = true))
        auditDao.insertLog(
            AuditLogEntity(
                action = "PRAYER_TIMING_UPDATED",
                details = "Prayer timing for '${timing.prayerName}' updated to ${timing.timeString}.",
                performedBy = teacherUsername
            )
        )
    }

    // --- Direct Fee Status (Paid / Unpaid) ---

    suspend fun setFeeStatusDirectly(
        feeId: Long,
        newStatus: String, // "Paid" or "Unpaid"
        teacherUsername: String
    ) = withContext(Dispatchers.IO) {
        val fee = feeDao.getFeeById(feeId) ?: return@withContext
        val isPaid = newStatus == "Paid"
        val receipt = if (isPaid && fee.receiptNumber.isNullOrBlank()) "IAF-REC-%04d".format(feeId + 1000) else fee.receiptNumber
        val updated = fee.copy(
            status = newStatus,
            amountPaid = if (isPaid) fee.amountDue else 0.0,
            verificationDate = if (isPaid) System.currentTimeMillis() else null,
            receiptNumber = receipt,
            verifiedBy = teacherUsername
        )
        feeDao.updateFee(updated)
        auditDao.insertLog(
            AuditLogEntity(
                action = "FEE_STATUS_CHANGED",
                details = "Fee for student '${fee.studentName}' (${fee.invoiceMonth}) marked as $newStatus by $teacherUsername.",
                performedBy = teacherUsername
            )
        )
    }

    suspend fun createFeeRecord(
        studentId: Long,
        invoiceMonth: String,
        amountDue: Double,
        status: String,
        teacherUsername: String
    ): Result<FeePaymentEntity> = withContext(Dispatchers.IO) {
        val student = studentDao.getStudentById(studentId)
            ?: return@withContext Result.failure(IllegalArgumentException("Student not found."))

        val isPaid = status == "Paid"
        val payment = FeePaymentEntity(
            studentId = studentId,
            studentName = student.fullName,
            instituteStudentCode = student.instituteStudentCode,
            invoiceMonth = invoiceMonth.trim().ifBlank { SimpleDateFormat("MMMM yyyy", Locale.US).format(Date()) },
            amountDue = amountDue,
            amountPaid = if (isPaid) amountDue else 0.0,
            paymentMethod = "Cash",
            status = status,
            verificationDate = if (isPaid) System.currentTimeMillis() else null,
            receiptNumber = if (isPaid) "IAF-REC-%04d".format(System.currentTimeMillis() % 10000 + 1000) else null,
            verifiedBy = if (isPaid) teacherUsername else null
        )
        val id = feeDao.insertFee(payment)
        val created = payment.copy(id = id)

        auditDao.insertLog(
            AuditLogEntity(
                action = "FEE_RECORD_CREATED",
                details = "Fee invoice for '${student.fullName}' (${payment.invoiceMonth}, Rs $amountDue) created with status $status by $teacherUsername.",
                performedBy = teacherUsername
            )
        )
        Result.success(created)
    }

    // --- Leave Requests ---

    fun getLeaveRequestsForParent(parentUsername: String): Flow<List<LeaveRequestEntity>> =
        leaveDao.getRequestsForParent(parentUsername)

    fun getPendingLeaveRequests(): Flow<List<LeaveRequestEntity>> =
        leaveDao.getPendingRequests()

    suspend fun submitLeaveRequest(
        studentId: Long,
        parentUsername: String,
        startDate: String,
        endDate: String,
        reason: String
    ): Result<LeaveRequestEntity> = withContext(Dispatchers.IO) {
        val student = studentDao.getStudentById(studentId)
            ?: return@withContext Result.failure(IllegalArgumentException("Student not found."))

        val request = LeaveRequestEntity(
            studentId = studentId,
            studentName = student.fullName,
            parentUsername = parentUsername,
            startDate = startDate.trim(),
            endDate = endDate.trim(),
            reason = reason.trim(),
            status = "Pending"
        )
        val id = leaveDao.insertRequest(request)
        val created = request.copy(id = id)

        auditDao.insertLog(
            AuditLogEntity(
                action = "LEAVE_REQUESTED",
                details = "Leave requested for student '${student.fullName}' ($startDate to $endDate).",
                performedBy = parentUsername
            )
        )
        Result.success(created)
    }

    suspend fun reviewLeaveRequest(
        requestId: Long,
        approve: Boolean,
        notes: String?,
        teacherUsername: String
    ) = withContext(Dispatchers.IO) {
        val allRequests = leaveDao.getAllRequests().first()
        val req = allRequests.find { it.id == requestId } ?: return@withContext
        val updated = req.copy(
            status = if (approve) "Approved" else "Rejected",
            responseDate = System.currentTimeMillis(),
            responseNotes = notes
        )
        leaveDao.updateRequest(updated)

        auditDao.insertLog(
            AuditLogEntity(
                action = if (approve) "LEAVE_APPROVED" else "LEAVE_REJECTED",
                details = "Teacher '$teacherUsername' ${if (approve) "approved" else "rejected"} leave request for student '${req.studentName}'.",
                performedBy = teacherUsername
            )
        )
    }

    // --- Announcements & Social Links ---

    fun getAllAnnouncements(): Flow<List<AnnouncementEntity>> = announcementDao.getAllAnnouncements()

    fun getAnnouncementsPastMonth(): Flow<List<AnnouncementEntity>> {
        val thirtyDaysAgo = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000)
        return announcementDao.getAnnouncementsSince(thirtyDaysAgo)
    }

    suspend fun createAnnouncement(
        title: String,
        content: String,
        targetGroup: String,
        author: String
    ) = withContext(Dispatchers.IO) {
        val announcement = AnnouncementEntity(
            title = title.trim(),
            content = content.trim(),
            targetGroup = targetGroup,
            author = author,
            date = SimpleDateFormat("dd MMM yyyy", Locale.US).format(Date())
        )
        announcementDao.insertAnnouncement(announcement)
        auditDao.insertLog(
            AuditLogEntity(
                action = "ANNOUNCEMENT_POSTED",
                details = "Announcement '$title' posted for group '$targetGroup'.",
                performedBy = author
            )
        )
    }

    suspend fun updateAnnouncement(
        announcement: AnnouncementEntity,
        teacherUsername: String
    ) = withContext(Dispatchers.IO) {
        announcementDao.updateAnnouncement(announcement)
        auditDao.insertLog(
            AuditLogEntity(
                action = "ANNOUNCEMENT_UPDATED",
                details = "Announcement '${announcement.title}' updated.",
                performedBy = teacherUsername
            )
        )
    }

    suspend fun deleteAnnouncement(
        id: Long,
        teacherUsername: String
    ) = withContext(Dispatchers.IO) {
        announcementDao.deleteAnnouncement(id)
        auditDao.insertLog(
            AuditLogEntity(
                action = "ANNOUNCEMENT_DELETED",
                details = "Announcement ID $id removed.",
                performedBy = teacherUsername
            )
        )
    }

    fun getSocialLinks(): Flow<SocialLinksEntity?> = socialLinksDao.getSocialLinks()

    suspend fun updateSocialLinks(links: SocialLinksEntity, teacherUsername: String) = withContext(Dispatchers.IO) {
        socialLinksDao.insertOrUpdate(links.copy(id = 1))
        auditDao.insertLog(
            AuditLogEntity(
                action = "SOCIAL_LINKS_UPDATED",
                details = "Official institute social links updated.",
                performedBy = teacherUsername
            )
        )
    }

    // --- Retention Reviews & Explicit Teacher Deletion ---

    fun getPendingRetentionReviews(): Flow<List<RetentionReviewEntity>> = retentionDao.getPendingReviews()

    suspend fun generateRetentionReviewsForExpiredRecords() = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val days14 = 14L * 24 * 60 * 60 * 1000 // 2 weeks (Daily Sabak)
        val days30 = 30L * 24 * 60 * 60 * 1000 // 1 month (Weekly Sabak, Notices)
        val days60 = 60L * 24 * 60 * 60 * 1000 // 2 months (Bi-weekly Sabak, Attendance)
        val days90 = 90L * 24 * 60 * 60 * 1000 // 3 months (Monthly Sabak)
        val days365 = 365L * 24 * 60 * 60 * 1000 // 1 year (Fees)

        // Check Quran records according to frequency:
        // Daily: 2 weeks, Weekly: 1 month, Bi-Weekly: 2 months, Monthly: 3 months
        val allQuran = quranRecordDao.getAllRecords().first()
        for (q in allQuran) {
            if (!q.isRetentionReview) {
                val age = now - q.timestamp
                val expired = when (q.recordType) {
                    "Daily" -> age > days14
                    "Weekly" -> age > days30
                    "Bi-Weekly" -> age > days60
                    "Monthly" -> age > days90
                    else -> age > days14
                }
                if (expired) {
                    quranRecordDao.markRetentionReview(q.id)
                    retentionDao.insertReview(
                        RetentionReviewEntity(
                            recordType = "Quran Progress (${q.recordType})",
                            originalRecordId = q.id,
                            studentName = q.studentName,
                            recordDate = q.date,
                            description = "${q.recordType} Sabak record from ${q.date} exceeded retention limit. Awaiting teacher deletion approval.",
                            status = "Pending"
                        )
                    )
                }
            }
        }

        // Check Fees records older than 1 year (365 days)
        val allFees = feeDao.getAllFees().first()
        for (fee in allFees) {
            if (!fee.isRetentionReview && (now - fee.timestamp) > days365) {
                feeDao.markRetentionReview(fee.id)
                retentionDao.insertReview(
                    RetentionReviewEntity(
                        recordType = "Fee Record",
                        originalRecordId = fee.id,
                        studentName = fee.studentName,
                        recordDate = fee.invoiceMonth,
                        description = "Fee invoice for ${fee.invoiceMonth} (Rs ${fee.amountDue}) exceeded 1-year retention limit. Awaiting teacher approval to delete.",
                        status = "Pending"
                    )
                )
            }
        }

        // Check Attendance records older than 60 days
        val allAtt = attendanceDao.getAllAttendance().first()
        for (att in allAtt) {
            if (!att.isRetentionReview && (now - att.timestamp) > days60) {
                attendanceDao.markRetentionReview(att.id)
                retentionDao.insertReview(
                    RetentionReviewEntity(
                        recordType = "Attendance Record",
                        originalRecordId = att.id,
                        studentName = att.studentName,
                        recordDate = att.date,
                        description = "Attendance for ${att.studentName} on ${att.date} exceeded 2-month retention cycle. Awaiting teacher approval to delete.",
                        status = "Pending"
                    )
                )
            }
        }
    }

    suspend fun executeRetentionAction(
        reviewId: Long,
        decision: String, // "ApprovedDeletion", "Kept", "Deferred"
        teacherUsername: String
    ) = withContext(Dispatchers.IO) {
        val allReviews = retentionDao.getAllReviews().first()
        val review = allReviews.find { it.id == reviewId } ?: return@withContext

        val updated = review.copy(
            status = decision,
            reviewedBy = teacherUsername,
            reviewTimestamp = System.currentTimeMillis()
        )
        retentionDao.updateReview(updated)

        if (decision == "ApprovedDeletion") {
            // Delete only the specifically approved record!
            when {
                review.recordType.startsWith("Quran") -> {
                    quranRecordDao.deleteRecordById(review.originalRecordId)
                }
                review.recordType.startsWith("Attendance") -> {
                    attendanceDao.deleteAttendanceById(review.originalRecordId)
                }
                review.recordType.startsWith("Fee") -> {
                    feeDao.deleteFeeById(review.originalRecordId)
                }
            }
        }

        auditDao.insertLog(
            AuditLogEntity(
                action = "RETENTION_REVIEW_DECISION",
                details = "Teacher '$teacherUsername' set decision to '$decision' for ${review.recordType} ID ${review.originalRecordId} (${review.studentName}).",
                performedBy = teacherUsername
            )
        )
    }

    fun getAllAuditLogs(): Flow<List<AuditLogEntity>> = auditDao.getAllLogs()

    // --- Institute Weekly Operating Days (Mon to Sun) ---

    fun getWeeklyDays(): Flow<List<InstituteWeeklyDayEntity>> = weeklyDayDao.getAllDays()

    suspend fun updateWeeklyDay(
        dayIndex: Int,
        customName: String,
        isOn: Boolean,
        timingsOrNotes: String,
        teacherUsername: String
    ) = withContext(Dispatchers.IO) {
        val days = weeklyDayDao.getAllDaysDirect()
        val day = days.find { it.dayIndex == dayIndex } ?: return@withContext
        val updated = day.copy(
            customDayName = customName.trim().ifBlank { day.defaultDayName },
            isOn = isOn,
            timingsOrNotes = timingsOrNotes.trim()
        )
        weeklyDayDao.updateDay(updated)
        auditDao.insertLog(
            AuditLogEntity(
                action = "WEEKLY_DAY_UPDATED",
                details = "Teacher '$teacherUsername' updated weekly day '${updated.customDayName}' to ${if (isOn) "ON (Working Day)" else "OFF (Holiday)"} with note '${updated.timingsOrNotes}'.",
                performedBy = teacherUsername
            )
        )
    }

    suspend fun toggleWeeklyDay(dayIndex: Int, teacherUsername: String) = withContext(Dispatchers.IO) {
        val days = weeklyDayDao.getAllDaysDirect()
        val day = days.find { it.dayIndex == dayIndex } ?: return@withContext
        updateWeeklyDay(
            dayIndex = dayIndex,
            customName = day.customDayName,
            isOn = !day.isOn,
            timingsOrNotes = day.timingsOrNotes,
            teacherUsername = teacherUsername
        )
    }

    suspend fun getDayInfoForDate(dateStr: String): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        // Returns Pair(isOn, customDayName)
        try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val date = sdf.parse(dateStr) ?: return@withContext Pair(true, "Day")
            val cal = Calendar.getInstance().apply { time = date }
            val calDay = cal.get(Calendar.DAY_OF_WEEK)
            val dayIndex = when (calDay) {
                Calendar.MONDAY -> 1
                Calendar.TUESDAY -> 2
                Calendar.WEDNESDAY -> 3
                Calendar.THURSDAY -> 4
                Calendar.FRIDAY -> 5
                Calendar.SATURDAY -> 6
                Calendar.SUNDAY -> 7
                else -> 1
            }
            val days = weeklyDayDao.getAllDaysDirect()
            val day = days.find { it.dayIndex == dayIndex }
            if (day != null) {
                Pair(day.isOn, day.customDayName)
            } else {
                Pair(true, "Day $dayIndex")
            }
        } catch (e: Exception) {
            Pair(true, "Day")
        }
    }

    suspend fun isDateOffDay(dateStr: String): Boolean = withContext(Dispatchers.IO) {
        val (isOn, _) = getDayInfoForDate(dateStr)
        !isOn
    }
}
