package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertUser(user: UserEntity)

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): UserEntity?

    @Query("SELECT COUNT(*) FROM users WHERE role = 'Teacher'")
    suspend fun getTeacherCount(): Int

    @Query("SELECT * FROM users ORDER BY createdAt DESC")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Query("UPDATE users SET passwordHash = :newPasswordHash WHERE username = :username")
    suspend fun updatePassword(username: String, newPasswordHash: String): Int

    @Query("DELETE FROM users WHERE username = :oldUsername")
    suspend fun deleteUserByUsername(oldUsername: String): Int
}

@Dao
interface StudentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: StudentEntity): Long

    @Update
    suspend fun updateStudent(student: StudentEntity)

    @Query("DELETE FROM students WHERE id = :id")
    suspend fun deleteStudentById(id: Long)

    @Query("SELECT * FROM students ORDER BY fullName ASC")
    fun getAllStudents(): Flow<List<StudentEntity>>

    @Query("SELECT * FROM students WHERE id = :id LIMIT 1")
    suspend fun getStudentById(id: Long): StudentEntity?

    @Query("SELECT * FROM students WHERE instituteStudentCode = :code LIMIT 1")
    suspend fun getStudentByInstituteCode(code: String): StudentEntity?

    @Query("SELECT * FROM students WHERE instituteStudentCode = :code")
    suspend fun getStudentsByInstituteCode(code: String): List<StudentEntity>

    @Query("SELECT COUNT(*) FROM students")
    fun getStudentCountFlow(): Flow<Int>

    @Query("SELECT COUNT(*) FROM students")
    suspend fun getStudentCount(): Int
}

@Dao
interface ParentStudentLinkDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLink(link: ParentStudentLinkEntity): Long

    @Update
    suspend fun updateLink(link: ParentStudentLinkEntity)

    @Query("SELECT * FROM parent_student_links WHERE parentUsername = :parentUsername ORDER BY requestDate DESC")
    fun getLinksForParent(parentUsername: String): Flow<List<ParentStudentLinkEntity>>

    @Query("SELECT * FROM parent_student_links WHERE parentUsername = :parentUsername AND status = 'Approved' ORDER BY studentName ASC")
    fun getApprovedLinksForParent(parentUsername: String): Flow<List<ParentStudentLinkEntity>>

    @Query("SELECT * FROM parent_student_links WHERE status = 'Pending' ORDER BY requestDate DESC")
    fun getAllPendingRequests(): Flow<List<ParentStudentLinkEntity>>

    @Query("SELECT * FROM parent_student_links ORDER BY requestDate DESC")
    fun getAllLinks(): Flow<List<ParentStudentLinkEntity>>

    @Query("SELECT * FROM parent_student_links WHERE parentUsername = :parentUsername AND (studentId = :studentId OR instituteStudentCode = :code) LIMIT 1")
    suspend fun findExistingLink(parentUsername: String, studentId: Long, code: String): ParentStudentLinkEntity?

    @Query("UPDATE parent_student_links SET parentUsername = :newUsername WHERE parentUsername = :oldUsername")
    suspend fun updateParentUsername(oldUsername: String, newUsername: String)
}

@Dao
interface AttendanceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendance(attendance: AttendanceEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(attendances: List<AttendanceEntity>)

    @Update
    suspend fun updateAttendance(attendance: AttendanceEntity)

    @Query("SELECT * FROM attendance WHERE date = :date ORDER BY studentName ASC")
    fun getAttendanceForDate(date: String): Flow<List<AttendanceEntity>>

    @Query("SELECT * FROM attendance WHERE studentId = :studentId ORDER BY date DESC")
    fun getAttendanceForStudent(studentId: Long): Flow<List<AttendanceEntity>>

    @Query("SELECT * FROM attendance ORDER BY date DESC, studentName ASC")
    fun getAllAttendance(): Flow<List<AttendanceEntity>>

    @Query("UPDATE attendance SET isRetentionReview = 1 WHERE id = :id")
    suspend fun markRetentionReview(id: Long)

    @Query("DELETE FROM attendance WHERE id = :id")
    suspend fun deleteAttendanceById(id: Long)
}

@Dao
interface QuranRecordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: QuranRecordEntity): Long

    @Update
    suspend fun updateRecord(record: QuranRecordEntity)

    @Query("SELECT * FROM quran_records WHERE studentId = :studentId ORDER BY date DESC")
    fun getRecordsForStudent(studentId: Long): Flow<List<QuranRecordEntity>>

    @Query("SELECT * FROM quran_records WHERE studentId = :studentId AND recordType = :recordType ORDER BY date DESC")
    fun getRecordsForStudentByType(studentId: Long, recordType: String): Flow<List<QuranRecordEntity>>

    @Query("SELECT * FROM quran_records ORDER BY date DESC")
    fun getAllRecords(): Flow<List<QuranRecordEntity>>

    @Query("UPDATE quran_records SET isRetentionReview = 1 WHERE id = :id")
    suspend fun markRetentionReview(id: Long)

    @Query("DELETE FROM quran_records WHERE id = :id")
    suspend fun deleteRecordById(id: Long)
}

@Dao
interface FeePaymentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFee(fee: FeePaymentEntity): Long

    @Update
    suspend fun updateFee(fee: FeePaymentEntity)

    @Query("SELECT * FROM fee_payments WHERE studentId = :studentId ORDER BY submissionDate DESC")
    fun getFeesForStudent(studentId: Long): Flow<List<FeePaymentEntity>>

    @Query("SELECT * FROM fee_payments ORDER BY submissionDate DESC")
    fun getAllFees(): Flow<List<FeePaymentEntity>>

    @Query("SELECT * FROM fee_payments WHERE status = 'Pending Verification' ORDER BY submissionDate DESC")
    fun getPendingVerificationFees(): Flow<List<FeePaymentEntity>>

    @Query("SELECT * FROM fee_payments WHERE id = :id LIMIT 1")
    suspend fun getFeeById(id: Long): FeePaymentEntity?

    @Query("UPDATE fee_payments SET status = :status, verifiedBy = :verifiedBy, verificationDate = :verificationDate, notes = :notes WHERE id = :id")
    suspend fun updateFeeStatus(id: Long, status: String, verifiedBy: String?, verificationDate: Long?, notes: String?)

    @Query("UPDATE fee_payments SET isRetentionReview = 1 WHERE id = :id")
    suspend fun markRetentionReview(id: Long)

    @Query("DELETE FROM fee_payments WHERE id = :id")
    suspend fun deleteFeeById(id: Long)
}

@Dao
interface ShiftDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShift(shift: ShiftEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(shifts: List<ShiftEntity>)

    @Update
    suspend fun updateShift(shift: ShiftEntity)

    @Query("SELECT * FROM shifts ORDER BY id ASC")
    fun getAllShifts(): Flow<List<ShiftEntity>>

    @Query("SELECT COUNT(*) FROM shifts")
    suspend fun getShiftCount(): Int

    @Query("DELETE FROM shifts WHERE id = :id")
    suspend fun deleteShift(id: Long)
}

@Dao
interface PrayerTimingDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTiming(timing: PrayerTimingEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(timings: List<PrayerTimingEntity>)

    @Update
    suspend fun updateTiming(timing: PrayerTimingEntity)

    @Query("SELECT * FROM prayer_timings ORDER BY id ASC")
    fun getAllPrayerTimings(): Flow<List<PrayerTimingEntity>>

    @Query("SELECT COUNT(*) FROM prayer_timings")
    suspend fun getPrayerCount(): Int
}

@Dao
interface LeaveRequestDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRequest(request: LeaveRequestEntity): Long

    @Update
    suspend fun updateRequest(request: LeaveRequestEntity)

    @Query("SELECT * FROM leave_requests WHERE parentUsername = :parentUsername ORDER BY requestDate DESC")
    fun getRequestsForParent(parentUsername: String): Flow<List<LeaveRequestEntity>>

    @Query("SELECT * FROM leave_requests WHERE studentId = :studentId ORDER BY requestDate DESC")
    fun getRequestsForStudent(studentId: Long): Flow<List<LeaveRequestEntity>>

    @Query("SELECT * FROM leave_requests WHERE status = 'Pending' ORDER BY requestDate DESC")
    fun getPendingRequests(): Flow<List<LeaveRequestEntity>>

    @Query("SELECT * FROM leave_requests ORDER BY requestDate DESC")
    fun getAllRequests(): Flow<List<LeaveRequestEntity>>

    @Query("UPDATE leave_requests SET parentUsername = :newUsername WHERE parentUsername = :oldUsername")
    suspend fun updateParentUsername(oldUsername: String, newUsername: String)
}

@Dao
interface AnnouncementDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnnouncement(announcement: AnnouncementEntity): Long

    @Query("DELETE FROM announcements WHERE id = :id")
    suspend fun deleteAnnouncement(id: Long)

    @Update
    suspend fun updateAnnouncement(announcement: AnnouncementEntity)

    @Query("SELECT * FROM announcements WHERE timestamp >= :cutoff ORDER BY timestamp DESC")
    fun getAnnouncementsSince(cutoff: Long): Flow<List<AnnouncementEntity>>

    @Query("SELECT * FROM announcements ORDER BY timestamp DESC")
    fun getAllAnnouncements(): Flow<List<AnnouncementEntity>>
}

@Dao
interface SocialLinksDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(links: SocialLinksEntity)

    @Query("SELECT * FROM social_links WHERE id = 1 LIMIT 1")
    fun getSocialLinks(): Flow<SocialLinksEntity?>

    @Query("SELECT * FROM social_links WHERE id = 1 LIMIT 1")
    suspend fun getSocialLinksDirect(): SocialLinksEntity?
}

@Dao
interface AuditLogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: AuditLogEntity): Long

    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC LIMIT 200")
    fun getAllLogs(): Flow<List<AuditLogEntity>>
}

@Dao
interface RetentionReviewDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReview(review: RetentionReviewEntity): Long

    @Update
    suspend fun updateReview(review: RetentionReviewEntity)

    @Query("SELECT * FROM retention_reviews WHERE status = 'Pending' ORDER BY id DESC")
    fun getPendingReviews(): Flow<List<RetentionReviewEntity>>

    @Query("SELECT * FROM retention_reviews ORDER BY id DESC")
    fun getAllReviews(): Flow<List<RetentionReviewEntity>>
}

@Dao
interface InstituteSettingsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(settings: InstituteSettingsEntity)

    @Query("SELECT * FROM institute_settings WHERE id = 1 LIMIT 1")
    fun getSettingsFlow(): Flow<InstituteSettingsEntity?>

    @Query("SELECT * FROM institute_settings WHERE id = 1 LIMIT 1")
    suspend fun getSettingsDirect(): InstituteSettingsEntity?
}

@Dao
interface InstituteWeeklyDayDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(day: InstituteWeeklyDayEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(days: List<InstituteWeeklyDayEntity>)

    @Update
    suspend fun updateDay(day: InstituteWeeklyDayEntity)

    @Query("SELECT * FROM institute_weekly_days ORDER BY dayIndex ASC")
    fun getAllDays(): Flow<List<InstituteWeeklyDayEntity>>

    @Query("SELECT * FROM institute_weekly_days ORDER BY dayIndex ASC")
    suspend fun getAllDaysDirect(): List<InstituteWeeklyDayEntity>

    @Query("SELECT COUNT(*) FROM institute_weekly_days")
    suspend fun getDaysCount(): Int
}

