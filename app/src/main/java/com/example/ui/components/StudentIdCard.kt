package com.example.ui.components

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.local.StudentEntity
import com.example.localization.AppLanguage
import com.example.localization.Strings
import java.util.UUID

data class StudentCardCustomOption(
    val id: String = UUID.randomUUID().toString(),
    val label: String,
    val value: String
)

data class EditableCardState(
    val studentName: String,
    val fatherName: String,
    val rollNumber: String,
    val program: String,
    val className: String,
    val admissionDate: String,
    val phone: String,
    val address: String,
    val photoUri: String? = null,
    val themeColorHex: Long = 0xFF0B331E,
    val visibleFields: Set<String> = setOf(
        "father_name", "roll_number", "program", "class_name", "admission_date", "phone", "address"
    ),
    val customOptions: List<StudentCardCustomOption> = emptyList()
)

fun createDefaultCardState(student: StudentEntity): EditableCardState {
    val initialCustom = mutableListOf<StudentCardCustomOption>()
    if (!student.dobOrAge.isNullOrBlank()) {
        initialCustom.add(StudentCardCustomOption(label = "Date of Birth / Age", value = student.dobOrAge))
    }
    if (!student.studentCnicOrBForm.isNullOrBlank()) {
        initialCustom.add(StudentCardCustomOption(label = "B-Form / CNIC", value = student.studentCnicOrBForm))
    }
    if (!student.emergencyContact.isNullOrBlank()) {
        initialCustom.add(StudentCardCustomOption(label = "Emergency Contact", value = student.emergencyContact))
    }

    return EditableCardState(
        studentName = student.fullName,
        fatherName = student.fatherName,
        rollNumber = student.rollNumber ?: "",
        program = student.program,
        className = student.className,
        admissionDate = student.admissionDate,
        phone = student.phone,
        address = student.address,
        photoUri = student.photoUri,
        customOptions = initialCustom
    )
}

@Composable
fun StudentIdCard(
    student: StudentEntity,
    language: AppLanguage,
    modifier: Modifier = Modifier,
    customState: EditableCardState? = null
) {
    val state = customState ?: remember(student) { createDefaultCardState(student) }
    val themeColor = Color(state.themeColorHex)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(2.dp, Color(0xFFD4A017)), // Muted Islamic Gold border
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(themeColor, themeColor.copy(alpha = 0.85f), themeColor.copy(alpha = 0.7f))
                        )
                    )
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IafLogo(size = 46.dp, isCircular = true)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "IDARA AL-FURQAN",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFBE49D),
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "ادارہ الفرقان — اسلامی تعلیمی ادارہ",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                        Text(
                            text = Strings.get("student_id_card", language).uppercase(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFE4F4EA),
                            letterSpacing = 1.sp
                        )
                    }
                }
            }

            // Divider gold line
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .background(Color(0xFFD4A017))
            )

            // Body Content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    // Student Photo with Gold Border
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFE8EFEA))
                            .border(1.5.dp, Color(0xFFD4A017), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        val photo = state.photoUri ?: student.photoUri
                        if (!photo.isNullOrBlank()) {
                            AsyncImage(
                                model = photo,
                                contentDescription = "Student Photograph",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(9.dp)),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Student Photograph",
                                tint = themeColor,
                                modifier = Modifier.size(52.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    // Key Identity Info
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = state.studentName,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = themeColor
                        )

                        if ("father_name" in state.visibleFields) {
                            Text(
                                text = "${Strings.get("father_name", language)}: ${state.fatherName}",
                                fontSize = 12.sp,
                                color = Color(0xFF333333)
                            )
                        }

                        if ("roll_number" in state.visibleFields) {
                            Spacer(modifier = Modifier.height(6.dp))
                            val rollDisplay = if (state.rollNumber.isNotBlank()) state.rollNumber else Strings.get("not_assigned", language)
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFE4F4EA),
                                border = BorderStroke(1.dp, themeColor)
                            ) {
                                Text(
                                    text = "${Strings.get("roll_number", language)}: $rollDisplay",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = themeColor,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = Color(0xFFE0E0E0), thickness = 0.8.dp)
                Spacer(modifier = Modifier.height(10.dp))

                // Standard Academic & Contact Details (Optionally rendered based on visibleFields)
                if ("program" in state.visibleFields) {
                    IdCardRow(label = Strings.get("program", language), value = state.program)
                }
                if ("class_name" in state.visibleFields) {
                    IdCardRow(label = Strings.get("class_name", language), value = state.className)
                }
                if ("admission_date" in state.visibleFields) {
                    IdCardRow(label = "Admission Date", value = state.admissionDate)
                }
                if ("phone" in state.visibleFields) {
                    val phoneDisplay = if (state.phone.isNotBlank()) state.phone else Strings.get("not_provided", language)
                    IdCardRow(label = Strings.get("contact_number", language), value = phoneDisplay)
                }
                if ("address" in state.visibleFields) {
                    val addressDisplay = if (state.address.isNotBlank()) state.address else Strings.get("not_provided", language)
                    IdCardRow(label = Strings.get("address", language), value = addressDisplay, wrapText = true)
                }

                // Custom Added Options / Fields
                state.customOptions.forEach { opt ->
                    IdCardRow(label = opt.label, value = opt.value, wrapText = true)
                }

                // STRICT COMPLIANCE: NO QR CODE OR BARCODE IS PRESENT ON THIS ID CARD.
            }

            // Footer Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(themeColor)
                    .padding(vertical = 6.dp, horizontal = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Official Student Identity — Verified by Idara Al-Furqan Administration",
                    fontSize = 9.sp,
                    color = Color(0xFFFBE49D),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun StudentIdCardViewerAndEditorDialog(
    student: StudentEntity,
    language: AppLanguage,
    onDismiss: () -> Unit,
    onSaveStudent: ((StudentEntity) -> Unit)? = null
) {
    var cardState by remember(student) { mutableStateOf(createDefaultCardState(student)) }
    var activeTab by remember { mutableStateOf(0) } // 0: View Card, 1: Edit & Customize Options

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            cardState = cardState.copy(photoUri = uri.toString())
        }
    }

    // New Custom Option Form Inputs
    var newOptionLabel by remember { mutableStateOf("") }
    var newOptionValue by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Badge, contentDescription = null, tint = Color(cardState.themeColorHex))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (activeTab == 0) Strings.get("student_id_card", language) else "Customize Student Card",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Tab Switcher between View Card & Edit Card
                TabRow(
                    selectedTabIndex = activeTab,
                    containerColor = Color(0xFFF1F5F9),
                    contentColor = Color(cardState.themeColorHex),
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp))
                ) {
                    Tab(
                        selected = activeTab == 0,
                        onClick = { activeTab = 0 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Preview Card", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    )
                    Tab(
                        selected = activeTab == 1,
                        onClick = { activeTab = 1 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Edit & Options", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    )
                    Tab(
                        selected = activeTab == 2,
                        onClick = { activeTab = 2 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Badge, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("CNIC & Docs", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (activeTab == 0) {
                    // LIVE PREVIEW OF CARD
                    StudentIdCard(
                        student = student,
                        language = language,
                        customState = cardState
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "• Official Student Identity Card for Idara Al-Furqan.\n• Roll number displayed on card. Institute ID and QR/Barcode omitted per institute privacy standards.\n• Tap 'Edit & Options' above to change data, add new fields, or remove existing options.",
                        fontSize = 11.sp,
                        color = Color.DarkGray
                    )
                } else if (activeTab == 1) {
                    // EDIT & CUSTOMIZE OPTIONS INTERFACE
                    // Student ID Card Photograph
                    Text("Student Photograph (on ID Card):", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                        border = BorderStroke(1.dp, Color(0xFFCBD5E1))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(62.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFE8EFEA))
                                    .border(1.5.dp, Color(0xFFD4A017), RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (!cardState.photoUri.isNullOrBlank()) {
                                    AsyncImage(
                                        model = cardState.photoUri,
                                        contentDescription = "ID Card Photo",
                                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(7.dp)),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = Color(cardState.themeColorHex),
                                        modifier = Modifier.size(38.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (!cardState.photoUri.isNullOrBlank()) "Student Photo Attached" else "No Photograph Added",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = if (!cardState.photoUri.isNullOrBlank()) Color(0xFF166534) else Color(0xFF64748B)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Button(
                                        onClick = {
                                            photoPickerLauncher.launch(
                                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                            )
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(cardState.themeColorHex)),
                                        shape = RoundedCornerShape(6.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        modifier = Modifier.height(30.dp)
                                    ) {
                                        Text(if (!cardState.photoUri.isNullOrBlank()) "Change Photo" else "Upload Photo", fontSize = 11.sp)
                                    }
                                    if (!cardState.photoUri.isNullOrBlank()) {
                                        OutlinedButton(
                                            onClick = { cardState = cardState.copy(photoUri = null) },
                                            shape = RoundedCornerShape(6.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                            modifier = Modifier.height(30.dp)
                                        ) {
                                            Text("Remove", fontSize = 11.sp, color = Color(0xFFDC2626))
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Text("Change Card Theme Color:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val themes = listOf(
                            "Emerald" to 0xFF0B331E,
                            "Navy" to 0xFF0F2B5C,
                            "Maroon" to 0xFF5C0F1E,
                            "Amber" to 0xFF78350F
                        )
                        themes.forEach { (name, hex) ->
                            val isSelected = cardState.themeColorHex == hex
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(hex),
                                border = if (isSelected) BorderStroke(2.dp, Color(0xFFD4A017)) else null,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(34.dp)
                                    .clickable { cardState = cardState.copy(themeColorHex = hex) }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(name, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Text("Edit Core Student Information:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = cardState.studentName,
                        onValueChange = { cardState = cardState.copy(studentName = it) },
                        label = { Text("Student Full Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = cardState.fatherName,
                        onValueChange = { cardState = cardState.copy(fatherName = it) },
                        label = { Text("Father's Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = cardState.rollNumber,
                        onValueChange = { cardState = cardState.copy(rollNumber = it) },
                        label = { Text("Roll Number") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = cardState.className,
                        onValueChange = { cardState = cardState.copy(className = it) },
                        label = { Text("Class / Group") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = cardState.phone,
                        onValueChange = { cardState = cardState.copy(phone = it) },
                        label = { Text("Contact Phone") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = cardState.address,
                        onValueChange = { cardState = cardState.copy(address = it) },
                        label = { Text("Address") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Field Visibility / Removal Options
                    Text("Enable / Disable Standard Fields on Card:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text("Uncheck to remove an option from the card.", fontSize = 11.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(6.dp))

                    val standardFields = listOf(
                        "father_name" to "Father's Name",
                        "roll_number" to "Roll Number",
                        "program" to "Academic Program",
                        "class_name" to "Class / Group",
                        "admission_date" to "Admission Date",
                        "phone" to "Contact Phone",
                        "address" to "Home Address"
                    )

                    standardFields.forEach { (key, title) ->
                        val isChecked = key in cardState.visibleFields
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val newSet = cardState.visibleFields.toMutableSet()
                                    if (isChecked) newSet.remove(key) else newSet.add(key)
                                    cardState = cardState.copy(visibleFields = newSet)
                                }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isChecked,
                                onCheckedChange = { checked ->
                                    val newSet = cardState.visibleFields.toMutableSet()
                                    if (checked) newSet.add(key) else newSet.remove(key)
                                    cardState = cardState.copy(visibleFields = newSet)
                                }
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = title, fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Custom Added Options
                    Text("Custom Options / Fields (${cardState.customOptions.size}):", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text("Add or remove any custom information on this student card.", fontSize = 11.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(6.dp))

                    if (cardState.customOptions.isNotEmpty()) {
                        cardState.customOptions.forEach { opt ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = opt.label, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(cardState.themeColorHex))
                                        Text(text = opt.value, fontSize = 12.sp, color = Color.DarkGray)
                                    }
                                    IconButton(
                                        onClick = {
                                            cardState = cardState.copy(
                                                customOptions = cardState.customOptions.filter { it.id != opt.id }
                                            )
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Remove Option", tint = Color(0xFFDC2626), modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Form to Add New Custom Option
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE4F4EA)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("+ Add New Option / Field to Card", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF166534))
                            Spacer(modifier = Modifier.height(6.dp))

                            // Quick preset chips
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                listOf("Blood Group", "B-Form #", "Bus Route", "Shift").forEach { preset ->
                                    FilterChip(
                                        selected = newOptionLabel == preset,
                                        onClick = { newOptionLabel = preset },
                                        label = { Text(preset, fontSize = 10.sp) }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            OutlinedTextField(
                                value = newOptionLabel,
                                onValueChange = { newOptionLabel = it },
                                label = { Text("Field Label (e.g. Blood Group)") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            OutlinedTextField(
                                value = newOptionValue,
                                onValueChange = { newOptionValue = it },
                                label = { Text("Field Value (e.g. B+ Positive)") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            Button(
                                onClick = {
                                    if (newOptionLabel.isNotBlank() && newOptionValue.isNotBlank()) {
                                        cardState = cardState.copy(
                                            customOptions = cardState.customOptions + StudentCardCustomOption(
                                                label = newOptionLabel.trim(),
                                                value = newOptionValue.trim()
                                            )
                                        )
                                        newOptionLabel = ""
                                        newOptionValue = ""
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF166534)),
                                modifier = Modifier.align(Alignment.End).height(32.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Add Option", fontSize = 11.sp)
                            }
                        }
                    }
                } else {
                    // Documents tab (activeTab == 2)
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "Mandatory Student Identification Documents",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color(cardState.themeColorHex)
                        )

                        // Father's CNIC Document
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                            border = BorderStroke(1.dp, Color(0xFFCBD5E1))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Father's CNIC Document", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF1E293B))
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = if (!student.fatherCnicPhotoUri.isNullOrBlank()) Color(0xFFDCFCE7) else Color(0xFFFEF2F2)
                                    ) {
                                        Text(
                                            text = if (!student.fatherCnicPhotoUri.isNullOrBlank()) "Attached ✓" else "Missing",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (!student.fatherCnicPhotoUri.isNullOrBlank()) Color(0xFF166534) else Color(0xFFB91C1C),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Father Name: ${student.fatherName}", fontSize = 11.sp, color = Color.DarkGray)
                                Text("Father CNIC No: ${student.fatherCnic}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
                                Spacer(modifier = Modifier.height(8.dp))
                                if (!student.fatherCnicPhotoUri.isNullOrBlank()) {
                                    AsyncImage(
                                        model = student.fatherCnicPhotoUri,
                                        contentDescription = "Father CNIC Document",
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(150.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color(0xFFE2E8F0)),
                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(70.dp)
                                            .background(Color(0xFFF1F5F9), RoundedCornerShape(6.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("No CNIC document photo uploaded", fontSize = 11.sp, color = Color.Gray)
                                    }
                                }
                            }
                        }

                        // Student's B-Form / CNIC Document
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                            border = BorderStroke(1.dp, Color(0xFFCBD5E1))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Student B-Form / CNIC Document", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF1E293B))
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = if (!student.studentBFormPhotoUri.isNullOrBlank()) Color(0xFFDCFCE7) else Color(0xFFFEF2F2)
                                    ) {
                                        Text(
                                            text = if (!student.studentBFormPhotoUri.isNullOrBlank()) "Attached ✓" else "Missing",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (!student.studentBFormPhotoUri.isNullOrBlank()) Color(0xFF166534) else Color(0xFFB91C1C),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Student Name: ${student.fullName}", fontSize = 11.sp, color = Color.DarkGray)
                                Text("B-Form / CNIC No: ${student.studentCnicOrBForm}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
                                Spacer(modifier = Modifier.height(8.dp))
                                if (!student.studentBFormPhotoUri.isNullOrBlank()) {
                                    AsyncImage(
                                        model = student.studentBFormPhotoUri,
                                        contentDescription = "Student B-Form Document",
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(150.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color(0xFFE2E8F0)),
                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(70.dp)
                                            .background(Color(0xFFF1F5F9), RoundedCornerShape(6.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("No B-Form document photo uploaded", fontSize = 11.sp, color = Color.Gray)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (onSaveStudent != null && (cardState.photoUri != student.photoUri || cardState.studentName != student.fullName || cardState.fatherName != student.fatherName || cardState.rollNumber != (student.rollNumber ?: "") || cardState.className != student.className || cardState.phone != student.phone || cardState.address != student.address)) {
                        onSaveStudent(
                            student.copy(
                                photoUri = cardState.photoUri,
                                fullName = cardState.studentName,
                                fatherName = cardState.fatherName,
                                rollNumber = cardState.rollNumber.ifBlank { null },
                                className = cardState.className,
                                phone = cardState.phone,
                                address = cardState.address
                            )
                        )
                    }
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(cardState.themeColorHex))
            ) {
                Text(if (activeTab == 0) "Close" else "Save & Close")
            }
        }
    )
}

@Composable
private fun IdCardRow(
    label: String,
    value: String,
    wrapText: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = if (wrapText) Alignment.Top else Alignment.CenterVertically
    ) {
        Text(
            text = "$label:",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF555555),
            modifier = Modifier.width(110.dp)
        )
        Text(
            text = value,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF111111),
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Start
        )
    }
}
