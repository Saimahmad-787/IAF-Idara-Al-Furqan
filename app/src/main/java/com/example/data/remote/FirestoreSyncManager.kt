package com.example.data.remote

import android.content.Context
import android.util.Log
import com.example.R
import com.example.data.local.*
import com.example.data.repository.IafRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class FirestoreSyncManager(
    private val context: Context,
    private val db: IafDatabase
) {
    private val TAG = "FirestoreSyncManager"
    private val scope = CoroutineScope(Dispatchers.IO)
    private var firestoreInstance: FirebaseFirestore? = null
    private val activeListeners = mutableListOf<ListenerRegistration>()

    private fun getFirestore(): FirebaseFirestore {
        if (firestoreInstance == null) {
            val dbId = context.getString(R.string.firestore_database_id)
            firestoreInstance = FirebaseFirestore.getInstance(dbId)
        }
        return firestoreInstance!!
    }

    private fun instituteId(): String = IafRepository.INSTITUTE_ID

    fun startRealtimeSync(role: String, username: String) {
        stopRealtimeSync()
        try {
            val firestore = getFirestore()
            val instId = instituteId()
            val instDoc = firestore.collection("institutes").document(instId)

            // 1. Synchronize Students in real-time
            val studentsListener = instDoc.collection("students")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e(TAG, "Students listener error: ${error.message}")
                        return@addSnapshotListener
                    }
                    snapshot?.documents?.forEach { doc ->
                        scope.launch {
                            val studentCode = doc.getString("instituteStudentCode") ?: return@launch
                            val fullName = doc.getString("fullName") ?: ""
                            val existing = db.studentDao().getStudentByInstituteCode(studentCode)
                            val entity = StudentEntity(
                                id = existing?.id ?: 0,
                                instituteId = instId,
                                instituteStudentCode = studentCode,
                                fullName = fullName,
                                rollNumber = doc.getString("rollNumber"),
                                dobOrAge = doc.getString("dobOrAge") ?: "Not specified",
                                fatherName = doc.getString("fatherName") ?: "Not specified",
                                fatherCnic = doc.getString("fatherCnic") ?: "",
                                studentCnicOrBForm = doc.getString("studentCnicOrBForm") ?: "",
                                photoUri = doc.getString("photoUri"),
                                fatherCnicPhotoUri = doc.getString("fatherCnicPhotoUri"),
                                studentBFormPhotoUri = doc.getString("studentBFormPhotoUri"),
                                phone = doc.getString("phone") ?: "",
                                address = doc.getString("address") ?: "",
                                admissionDate = doc.getString("admissionDate") ?: "",
                                program = doc.getString("program") ?: "Hifz",
                                className = doc.getString("className") ?: "General",
                                assignedTeacher = doc.getString("assignedTeacher") ?: "Head Ustadh",
                                monthlyFee = doc.getDouble("monthlyFee") ?: 2000.0,
                                feeStartDate = doc.getString("feeStartDate") ?: "",
                                dueDate = doc.getString("dueDate") ?: "10th of every month",
                                approvedConcession = doc.getDouble("approvedConcession") ?: 0.0,
                                status = doc.getString("status") ?: "Active"
                            )
                            if (existing != null) {
                                db.studentDao().updateStudent(entity)
                            } else {
                                db.studentDao().insertStudent(entity)
                            }
                        }
                    }
                }
            activeListeners.add(studentsListener)

            // 2. Synchronize Attendance in real-time
            val attendanceListener = instDoc.collection("attendance")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e(TAG, "Attendance listener error: ${error.message}")
                        return@addSnapshotListener
                    }
                    snapshot?.documents?.forEach { doc ->
                        scope.launch {
                            val studentId = doc.getLong("studentId") ?: return@launch
                            val date = doc.getString("date") ?: return@launch
                            val status = doc.getString("status") ?: "Present"
                            val studentName = doc.getString("studentName") ?: ""
                            val markedBy = doc.getString("markedBy") ?: "Teacher"
                            
                            val entity = AttendanceEntity(
                                studentId = studentId,
                                studentName = studentName,
                                date = date,
                                status = status,
                                recordedBy = markedBy
                            )
                            db.attendanceDao().insertAttendance(entity)
                        }
                    }
                }
            activeListeners.add(attendanceListener)

            // 3. Synchronize Quran Learning in real-time
            val quranListener = instDoc.collection("quran_records")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e(TAG, "Quran records listener error: ${error.message}")
                        return@addSnapshotListener
                    }
                    snapshot?.documents?.forEach { doc ->
                        scope.launch {
                            val studentId = doc.getLong("studentId") ?: return@launch
                            val date = doc.getString("date") ?: return@launch
                            val recordType = doc.getString("recordType") ?: "Daily"
                            val sabaq = doc.getString("sabaq") ?: ""
                            val sabqi = doc.getString("sabqi") ?: ""
                            val manzil = doc.getString("manzil") ?: ""
                            val fluency = doc.getString("fluency") ?: (doc.getString("evaluation") ?: "Good")
                            val tajweedPronunciation = doc.getString("tajweedPronunciation") ?: "Accurate"
                            val versesOrLesson = doc.getString("versesOrLesson") ?: (if (sabaq.isNotEmpty()) sabaq else "General")
                            val recordedBy = doc.getString("recordedBy") ?: "Teacher"
                            val studentName = doc.getString("studentName") ?: ""
                            val program = doc.getString("program") ?: "Hifz"

                            val entity = QuranRecordEntity(
                                studentId = studentId,
                                studentName = studentName,
                                program = program,
                                date = date,
                                recordType = recordType,
                                versesOrLesson = versesOrLesson,
                                sabaq = sabaq,
                                sabqi = sabqi,
                                manzil = manzil,
                                fluency = fluency,
                                tajweedPronunciation = tajweedPronunciation,
                                recordedBy = recordedBy
                            )
                            db.quranRecordDao().insertRecord(entity)
                        }
                    }
                }
            activeListeners.add(quranListener)

            // 4. Synchronize Fee Payments & Invoices in real-time
            val feeListener = instDoc.collection("fee_payments")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e(TAG, "Fee payments listener error: ${error.message}")
                        return@addSnapshotListener
                    }
                    snapshot?.documents?.forEach { doc ->
                        scope.launch {
                            val studentId = doc.getLong("studentId") ?: return@launch
                            val month = doc.getString("invoiceMonth") ?: return@launch
                            val amountDue = doc.getDouble("amountDue") ?: 0.0
                            val amountPaid = doc.getDouble("amountPaid") ?: 0.0
                            val status = doc.getString("status") ?: "Unpaid"
                            val method = doc.getString("paymentMethod") ?: "Cash"
                            val receipt = doc.getString("receiptNumber")
                            val studentName = doc.getString("studentName") ?: ""
                            val code = doc.getString("instituteStudentCode") ?: ""

                            val entity = FeePaymentEntity(
                                studentId = studentId,
                                studentName = studentName,
                                instituteStudentCode = code,
                                invoiceMonth = month,
                                amountDue = amountDue,
                                amountPaid = amountPaid,
                                paymentMethod = method,
                                status = status,
                                receiptNumber = receipt
                            )
                            db.feePaymentDao().insertFee(entity)
                        }
                    }
                }
            activeListeners.add(feeListener)

            // 5. Synchronize Parent-Student Links & Verification Requests in real-time
            val linksListener = instDoc.collection("parent_student_links")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e(TAG, "Links listener error: ${error.message}")
                        return@addSnapshotListener
                    }
                    snapshot?.documents?.forEach { doc ->
                        scope.launch {
                            val parentUser = doc.getString("parentUsername") ?: return@launch
                            val studentCode = doc.getString("studentInstituteCode") ?: return@launch
                            val studentId = doc.getLong("studentId") ?: return@launch
                            val status = doc.getString("status") ?: "Pending"
                            val studentName = doc.getString("studentName") ?: ""

                            val existing = db.parentStudentLinkDao().findExistingLink(parentUser, studentId, studentCode)
                            val entity = ParentStudentLinkEntity(
                                id = existing?.id ?: 0,
                                parentUsername = parentUser,
                                studentId = studentId,
                                instituteStudentCode = studentCode,
                                studentName = studentName,
                                status = status,
                                requestDate = doc.getLong("requestDate") ?: System.currentTimeMillis()
                            )
                            if (existing != null) {
                                db.parentStudentLinkDao().updateLink(entity)
                            } else {
                                db.parentStudentLinkDao().insertLink(entity)
                            }
                        }
                    }
                }
            activeListeners.add(linksListener)

            Log.i(TAG, "All Firestore Realtime Listeners successfully initialized.")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start Firestore realtime listeners: ${e.message}")
        }
    }

    fun stopRealtimeSync() {
        activeListeners.forEach { it.remove() }
        activeListeners.clear()
    }

    // Push local updates to Firestore
    suspend fun pushStudent(student: StudentEntity) {
        try {
            val firestore = getFirestore()
            val docRef = firestore.collection("institutes").document(instituteId())
                .collection("students").document(student.instituteStudentCode)

            val data = hashMapOf(
                "instituteStudentCode" to student.instituteStudentCode,
                "fullName" to student.fullName,
                "rollNumber" to student.rollNumber,
                "dobOrAge" to student.dobOrAge,
                "fatherName" to student.fatherName,
                "fatherCnic" to student.fatherCnic,
                "studentCnicOrBForm" to student.studentCnicOrBForm,
                "photoUri" to student.photoUri,
                "phone" to student.phone,
                "address" to student.address,
                "admissionDate" to student.admissionDate,
                "program" to student.program,
                "className" to student.className,
                "assignedTeacher" to student.assignedTeacher,
                "monthlyFee" to student.monthlyFee,
                "feeStartDate" to student.feeStartDate,
                "dueDate" to student.dueDate,
                "approvedConcession" to student.approvedConcession,
                "status" to student.status
            )
            docRef.set(data, SetOptions.merge()).await()
        } catch (e: Exception) {
            Log.w(TAG, "Push student to Firestore failed: ${e.message}")
        }
    }

    suspend fun pushAttendance(attendance: AttendanceEntity) {
        try {
            val firestore = getFirestore()
            val docId = "${attendance.studentId}_${attendance.date}"
            val docRef = firestore.collection("institutes").document(instituteId())
                .collection("attendance").document(docId)

            val data = hashMapOf(
                "studentId" to attendance.studentId,
                "studentName" to attendance.studentName,
                "date" to attendance.date,
                "status" to attendance.status,
                "markedBy" to attendance.recordedBy
            )
            docRef.set(data, SetOptions.merge()).await()
        } catch (e: Exception) {
            Log.w(TAG, "Push attendance to Firestore failed: ${e.message}")
        }
    }

    suspend fun pushQuranRecord(record: QuranRecordEntity) {
        try {
            val firestore = getFirestore()
            val docId = "${record.studentId}_${record.date}_${record.recordType}"
            val docRef = firestore.collection("institutes").document(instituteId())
                .collection("quran_records").document(docId)

            val data = hashMapOf(
                "studentId" to record.studentId,
                "studentName" to record.studentName,
                "program" to record.program,
                "date" to record.date,
                "recordType" to record.recordType,
                "versesOrLesson" to record.versesOrLesson,
                "sabaq" to record.sabaq,
                "sabqi" to record.sabqi,
                "manzil" to record.manzil,
                "fluency" to record.fluency,
                "tajweedPronunciation" to record.tajweedPronunciation,
                "recordedBy" to record.recordedBy
            )
            docRef.set(data, SetOptions.merge()).await()
        } catch (e: Exception) {
            Log.w(TAG, "Push Quran record to Firestore failed: ${e.message}")
        }
    }

    suspend fun pushFeePayment(fee: FeePaymentEntity) {
        try {
            val firestore = getFirestore()
            val docId = "${fee.studentId}_${fee.invoiceMonth.replace(" ", "_")}"
            val docRef = firestore.collection("institutes").document(instituteId())
                .collection("fee_payments").document(docId)

            val data = hashMapOf(
                "studentId" to fee.studentId,
                "studentName" to fee.studentName,
                "instituteStudentCode" to fee.instituteStudentCode,
                "invoiceMonth" to fee.invoiceMonth,
                "amountDue" to fee.amountDue,
                "amountPaid" to fee.amountPaid,
                "paymentMethod" to fee.paymentMethod,
                "status" to fee.status,
                "receiptNumber" to fee.receiptNumber
            )
            docRef.set(data, SetOptions.merge()).await()
        } catch (e: Exception) {
            Log.w(TAG, "Push fee payment to Firestore failed: ${e.message}")
        }
    }

    suspend fun pushParentStudentLink(link: ParentStudentLinkEntity) {
        try {
            val firestore = getFirestore()
            val docId = "${link.parentUsername}_${link.studentId}"
            val docRef = firestore.collection("institutes").document(instituteId())
                .collection("parent_student_links").document(docId)

            val data = hashMapOf(
                "parentUsername" to link.parentUsername,
                "studentId" to link.studentId,
                "studentInstituteCode" to link.instituteStudentCode,
                "studentName" to link.studentName,
                "status" to link.status,
                "requestDate" to link.requestDate
            )
            docRef.set(data, SetOptions.merge()).await()
        } catch (e: Exception) {
            Log.w(TAG, "Push parent student link to Firestore failed: ${e.message}")
        }
    }
}
