package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.FeePaymentEntity
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ConfirmedReceiptView(
    fee: FeePaymentEntity,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(2.dp, Color(0xFF16803D)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Receipt Header with Logo
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IafLogo(size = 48.dp, isCircular = true)
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "IDARA AL-FURQAN",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF114B2C)
                    )
                    Text(
                        text = "OFFICIAL FEE PAYMENT RECEIPT",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFD4A017),
                        letterSpacing = 1.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color(0xFFE2E8F0))
            Spacer(modifier = Modifier.height(10.dp))

            // Verified Badge
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFDCFCE7), RoundedCornerShape(6.dp))
                    .border(1.dp, Color(0xFF16803D), RoundedCornerShape(6.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Verified",
                    tint = Color(0xFF16803D),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "CONFIRMED & POSTED TO FINANCIAL LEDGER",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF16803D)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Receipt Details
            ReceiptRow(label = "Receipt Number", value = fee.receiptNumber ?: "N/A", isBold = true)
            ReceiptRow(label = "Student Name", value = fee.studentName, isBold = true)
            ReceiptRow(label = "Institute ID", value = fee.instituteStudentCode)
            ReceiptRow(label = "Fee Period", value = fee.invoiceMonth)

            val dateStr = if (fee.verificationDate != null) {
                SimpleDateFormat("dd MMMM yyyy", Locale.US).format(Date(fee.verificationDate))
            } else {
                SimpleDateFormat("dd MMMM yyyy", Locale.US).format(Date(fee.submissionDate))
            }
            ReceiptRow(label = "Payment Date", value = dateStr)
            ReceiptRow(label = "Payment Method", value = fee.paymentMethod)

            if (!fee.transactionRef.isNullOrBlank()) {
                ReceiptRow(label = "Reference / Slip", value = fee.transactionRef)
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = Color(0xFFE2E8F0))
            Spacer(modifier = Modifier.height(8.dp))

            ReceiptRow(label = "Amount Billed", value = "PKR %.2f".format(fee.amountDue))
            ReceiptRow(label = "Amount Paid", value = "PKR %.2f".format(fee.amountPaid), isBold = true, highlight = true)

            val balance = maxOf(0.0, fee.amountDue - fee.amountPaid)
            ReceiptRow(label = "Remaining Balance", value = "PKR %.2f".format(balance))

            if (!fee.verifiedBy.isNullOrBlank()) {
                ReceiptRow(label = "Verified By", value = fee.verifiedBy)
            }

            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "Thank you for supporting Idara Al-Furqan. May Allah bless your efforts.",
                fontSize = 10.sp,
                color = Color(0xFF718096),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun ReceiptRow(
    label: String,
    value: String,
    isBold: Boolean = false,
    highlight: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color(0xFF4A5568)
        )
        Text(
            text = value,
            fontSize = if (highlight) 14.sp else 12.sp,
            fontWeight = if (isBold || highlight) FontWeight.Bold else FontWeight.Normal,
            color = if (highlight) Color(0xFF16803D) else Color(0xFF1A202C)
        )
    }
}
