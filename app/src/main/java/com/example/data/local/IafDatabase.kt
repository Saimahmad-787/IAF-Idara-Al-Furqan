package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        UserEntity::class,
        StudentEntity::class,
        ParentStudentLinkEntity::class,
        AttendanceEntity::class,
        QuranRecordEntity::class,
        FeePaymentEntity::class,
        ShiftEntity::class,
        PrayerTimingEntity::class,
        LeaveRequestEntity::class,
        AnnouncementEntity::class,
        SocialLinksEntity::class,
        AuditLogEntity::class,
        RetentionReviewEntity::class,
        InstituteSettingsEntity::class,
        InstituteWeeklyDayEntity::class
    ],
    version = 6,
    exportSchema = false
)
abstract class IafDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun studentDao(): StudentDao
    abstract fun parentStudentLinkDao(): ParentStudentLinkDao
    abstract fun attendanceDao(): AttendanceDao
    abstract fun quranRecordDao(): QuranRecordDao
    abstract fun feePaymentDao(): FeePaymentDao
    abstract fun shiftDao(): ShiftDao
    abstract fun prayerTimingDao(): PrayerTimingDao
    abstract fun leaveRequestDao(): LeaveRequestDao
    abstract fun announcementDao(): AnnouncementDao
    abstract fun socialLinksDao(): SocialLinksDao
    abstract fun auditLogDao(): AuditLogDao
    abstract fun retentionReviewDao(): RetentionReviewDao
    abstract fun instituteSettingsDao(): InstituteSettingsDao
    abstract fun instituteWeeklyDayDao(): InstituteWeeklyDayDao

    companion object {
        @Volatile
        private var INSTANCE: IafDatabase? = null

        fun getDatabase(context: Context): IafDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    IafDatabase::class.java,
                    "iaf_furqan.db"
                ).fallbackToDestructiveMigration(true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
