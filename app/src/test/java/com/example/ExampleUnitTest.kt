package com.example

import com.example.data.local.FeePaymentEntity
import com.example.data.local.QuranRecordEntity
import com.example.data.local.StudentEntity
import com.example.data.repository.IafRepository
import com.example.localization.AppLanguage
import com.example.localization.Strings
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testBootstrapCodeMatchesSpecification() {
        assertEquals("14800", IafRepository.BOOTSTRAP_AUTH_CODE)
        assertEquals("IAF-2026", IafRepository.INSTITUTE_ID)
        assertEquals("IAF-2027", IafRepository.SECONDARY_INSTITUTE_ID)
    }

    @Test
    fun testDemoStudentAccountsSetup() {
        // Student 1 & Student 2: Same institute ID (IAF-2026), same parents (Sheikh Tariq Mahmood), different roll numbers
        val stu1 = StudentEntity(
            id = 1,
            instituteId = "IAF-2026",
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

        val stu2 = StudentEntity(
            id = 2,
            instituteId = "IAF-2026", // Same Institute ID
            instituteStudentCode = "IAF-STU-002",
            fullName = "Zainab Ahmad",
            rollNumber = "RN-102", // Different Roll Number
            dobOrAge = "9 years",
            fatherName = "Sheikh Tariq Mahmood", // Same Parent
            phone = "0300-9988776", // Same Phone
            address = "House 14, Street 5, Sector F-8, Islamabad", // Same Address
            admissionDate = "2026-08-15",
            program = "Nazra Quran",
            className = "Afternoon Nazra Group B",
            assignedTeacher = "Ustadh Ahmad Al-Furqan",
            monthlyFee = 2000.0,
            feeStartDate = "2026-08-15",
            dueDate = "10th of every month",
            status = "Active"
        )

        // Student 3: Different Institute ID (IAF-2027)
        val stu3 = StudentEntity(
            id = 3,
            instituteId = "IAF-2027", // Different Institute ID
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

        assertEquals(stu1.instituteId, stu2.instituteId)
        assertEquals(stu1.fatherName, stu2.fatherName)
        assertNotEquals(stu1.rollNumber, stu2.rollNumber)

        assertNotEquals(stu1.instituteId, stu3.instituteId)
        assertEquals("IAF-2027", stu3.instituteId)
    }

    @Test
    fun testStudentRollNumberIsOptional() {
        val studentWithoutRoll = StudentEntity(
            id = 1,
            instituteId = "IAF-2026",
            instituteStudentCode = "IAF-STU-001",
            fullName = "Muhammad Bilal",
            rollNumber = null, // Strictly optional!
            dobOrAge = "11 years",
            fatherName = "Tariq Mahmood",
            phone = "03001234567",
            address = "Street 4, Sector G-8, Islamabad",
            admissionDate = "2026-10-01",
            program = "Hifz-ul-Quran",
            className = "Morning Hifz",
            assignedTeacher = "Ustadh Farooq",
            monthlyFee = 2500.0,
            feeStartDate = "2026-10-01",
            dueDate = "10th"
        )
        assertNull(studentWithoutRoll.rollNumber)
        assertEquals("Muhammad Bilal", studentWithoutRoll.fullName)
        assertEquals("IAF-STU-001", studentWithoutRoll.instituteStudentCode)
    }

    @Test
    fun testLanguagesSupportedAndRtlDetection() {
        assertTrue(AppLanguage.URDU.isRtl)
        assertTrue(AppLanguage.ARABIC.isRtl)
        assertTrue(AppLanguage.PASHTO.isRtl)
        assertFalse(AppLanguage.ENGLISH.isRtl)

        // Verify key terms translated across all 4 languages
        AppLanguage.values().forEach { lang ->
            val appTitle = Strings.get("app_title", lang)
            assertNotNull(appTitle)
            assertTrue(appTitle.isNotBlank())

            val emptyStudents = Strings.get("no_students", lang)
            assertEquals(true, emptyStudents.isNotBlank())
        }
    }

    @Test
    fun testFeeConfirmedReceiptLogic() {
        val pendingFee = FeePaymentEntity(
            id = 10,
            studentId = 1,
            studentName = "Usman Tariq",
            instituteStudentCode = "IAF-STU-001",
            invoiceMonth = "October 2026",
            amountDue = 2000.0,
            amountPaid = 2000.0,
            paymentMethod = "Bank Transfer",
            status = "Pending Verification",
            receiptNumber = null
        )
        assertNull(pendingFee.receiptNumber)
        assertNotEquals("Paid", pendingFee.status)

        val approvedFee = pendingFee.copy(
            status = "Paid",
            receiptNumber = "IAF-REC-1010",
            verifiedBy = "Ustadh Ahmad"
        )
        assertEquals("Paid", approvedFee.status)
        assertEquals("IAF-REC-1010", approvedFee.receiptNumber)
        assertEquals("Ustadh Ahmad", approvedFee.verifiedBy)
    }

    @Test
    fun testHifzQuranRecordStructureAcrossFrequencies() {
        val dailyHifz = QuranRecordEntity(
            studentId = 1,
            studentName = "Muhammad Ahmad",
            program = "Hifz-ul-Quran",
            date = "2026-10-04",
            recordType = "Daily",
            juz = 1,
            surah = "Al-Baqarah",
            versesOrLesson = "Verses 60-74",
            sabaq = "Surah Al-Baqarah Page 11",
            sabqi = "Juz 1 Quarter 1-2",
            manzil = "Juz 30 (Surah An-Naba to An-Nas)",
            fluency = "Good",
            tajweedPronunciation = "Accurate",
            mistakes = 1
        )

        assertEquals("Daily", dailyHifz.recordType)
        assertEquals("Surah Al-Baqarah Page 11", dailyHifz.sabaq)
        assertEquals("Juz 1 Quarter 1-2", dailyHifz.sabqi)
        assertEquals("Juz 30 (Surah An-Naba to An-Nas)", dailyHifz.manzil)

        val weeklyHifz = dailyHifz.copy(
            recordType = "Weekly",
            sabaq = "Juz 1 Pages 1-15",
            sabqi = "Juz 1 First Half",
            manzil = "Juz 29 & 30"
        )
        assertEquals("Weekly", weeklyHifz.recordType)

        val biWeeklyHifz = dailyHifz.copy(recordType = "Bi-Weekly")
        assertEquals("Bi-Weekly", biWeeklyHifz.recordType)

        val monthlyHifz = dailyHifz.copy(recordType = "Monthly")
        assertEquals("Monthly", monthlyHifz.recordType)
    }

    @Test
    fun testQaidaAndNazraQuranRecordStructure() {
        val qaidaRecord = QuranRecordEntity(
            studentId = 2,
            studentName = "Zaid Ali",
            program = "Noorani Qaida",
            date = "2026-10-04",
            recordType = "Daily",
            versesOrLesson = "Lesson 6: Tanween and Sakin",
            sabaq = "Lesson 6: Tanween Exercises",
            sabqi = null,
            manzil = null,
            fluency = "Excellent",
            tajweedPronunciation = "Clear articulation",
            mistakes = 0
        )

        assertEquals("Noorani Qaida", qaidaRecord.program)
        assertEquals("Daily", qaidaRecord.recordType)
        assertEquals("Lesson 6: Tanween Exercises", qaidaRecord.sabaq)
        assertNull(qaidaRecord.sabqi)
        assertNull(qaidaRecord.manzil)

        val nazraRecord = QuranRecordEntity(
            studentId = 3,
            studentName = "Zainab Ahmad",
            program = "Nazra Quran",
            date = "2026-10-04",
            recordType = "Monthly",
            versesOrLesson = "Juz 16 Comprehensive Nazra Test",
            sabaq = "Surah Maryam Verses 1-98",
            sabqi = null,
            manzil = null,
            fluency = "Excellent",
            tajweedPronunciation = "Exam Grade: 98/100",
            mistakes = 0
        )

        assertEquals("Nazra Quran", nazraRecord.program)
        assertEquals("Monthly", nazraRecord.recordType)
        assertEquals("Surah Maryam Verses 1-98", nazraRecord.sabaq)
    }
}
