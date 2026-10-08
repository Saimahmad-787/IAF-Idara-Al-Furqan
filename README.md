# IAF — Idara Al-Furqan (ادارہ الفرقان)
## Islamic Institute Management System

A production-grade Android application built using modern Jetpack Compose, Kotlin Coroutines, Room local database persistence, and Firebase-ready security architecture.

---

### Non-Negotiable Institute Compliance

1. **Strict No-Demo Policy:**
   - Zero hardcoded demo accounts, fake student profiles, simulated financial ledgers, or mock bypass credentials.
   - Clean, actionable empty states ("No students have been added yet.", "No fee records are available yet.").
2. **One-Time Secure Teacher Bootstrap:**
   - First-time setup code `14800` is strictly restricted to initial setup when zero teacher accounts exist. Once bootstrapped, the bootstrap is permanently disabled.
3. **Student Registration & Verification:**
   - Roll number is strictly optional.
   - Student records must be created by authorized teachers with Institute IDs (e.g. `IAF-STU-001`).
   - Parents link to students using the teacher-assigned Institute ID; links require independent teacher approval before accessing records.
4. **Digital Student ID Cards:**
   - Rendered using official IAF branding (Deep Emerald Green `#114B2C` & Muted Islamic Gold `#D4A017`).
   - Official IAF Logo prominently displayed.
   - Student address and contact phone included when available.
   - **No QR code or barcode anywhere on the ID card.**
5. **Quran Learning & Three Built-in Programs:**
   - Exactly three programs supported: Noorani Qaida, Nazra Quran, Hifz-ul-Quran.
   - Detailed tracking for Sabaq, Sabqi, Manzil, Juz, fluency, and Tajweed.
6. **Financial Ledger & Confirmed Receipts:**
   - Multi-status workflow (Unpaid, Pending Verification, Paid).
   - Only teacher-approved payments post to the ledger and generate official confirmed receipts.
7. **Four Institute Shifts & Five Daily Prayers:**
   - Shifts: 1. Sunrise Shift, 2. Morning Shift, 3. Afternoon Shift, 4. Evening Shift.
   - Prayers: Fajr, Dhuhr, Asr, Maghrib, Isha (with customizable timings).
8. **Retention Policy & Explicit Deletion Approvals:**
   - Retention review required for expired records (14 days for Sabaq/Sabqi, 2 months for attendance, 1 year for fees).
   - **No automatic deletion:** permanent deletion requires explicit teacher review and approval, recorded in immutable audit logs.
9. **Languages & RTL Support:**
   - Native support for English, Urdu (اردو), Arabic (العربية), and Pashto (پښتو) with dynamic RTL mirroring.
