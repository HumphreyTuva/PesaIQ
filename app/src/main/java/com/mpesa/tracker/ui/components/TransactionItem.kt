package com.mpesa.tracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mpesa.tracker.data.model.Transaction
import com.mpesa.tracker.data.model.TransactionType

@Composable
fun TransactionItem(
    transaction: Transaction,
    onClick: () -> Unit,
    isDark: Boolean = true
) {
    val accentColor = when (transaction.type) {
        TransactionType.RECEIVE -> Color(0xFF00E676)
        TransactionType.SEND -> Color(0xFFFF9800)
        TransactionType.PAYBILL -> Color(0xFF00BCD4)
        TransactionType.BUY_GOODS -> Color(0xFF9D4EDD)
        TransactionType.WITHDRAW -> Color(0xFFADB5BD)
        TransactionType.AIRTIME -> Color(0xFF03A9F4)
        else -> if (isDark) Color.White else Color.Black
    }

    val emoji = when (transaction.type) {
        TransactionType.RECEIVE -> "💰"
        TransactionType.SEND -> "📤"
        TransactionType.PAYBILL -> "📄"
        TransactionType.BUY_GOODS -> "🛍️"
        TransactionType.WITHDRAW -> "🏧"
        TransactionType.AIRTIME -> "📱"
        else -> "💸"
    }

    val textColor = if (isDark) Color.White else Color.Black
    val secondaryTextColor = if (isDark) Color(0xFF8890B0) else Color(0xFF6E6E73)
    val surfaceColor = if (isDark) Color(0xFF1A1C1E) else Color(0xFFFFFFFF)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(surfaceColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val contentAlpha = if (transaction.isExcluded) 0.5f else 1f
        
        // Icon Circle
        Surface(
            modifier = Modifier.size(48.dp).alpha(contentAlpha),
            shape = CircleShape,
            color = accentColor.copy(alpha = 0.1f),
            border = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(alpha = 0.3f))
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(text = emoji, fontSize = 20.sp)
            }
        }

        Spacer(Modifier.width(16.dp))

        // Info
        Column(modifier = Modifier.weight(1f).alpha(contentAlpha)) {
            Text(
                text = (if (transaction.isExcluded) "${transaction.displayName()} (EXCLUDED)" else transaction.displayName()).uppercase(),
                color = textColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "${transaction.formattedDateShort()} • ${transaction.category}",
                color = secondaryTextColor,
                fontSize = 12.sp
            )
        }

        // Amount
        Column(horizontalAlignment = Alignment.End, modifier = Modifier.alpha(contentAlpha)) {
            val amountText = if (transaction.isExpense) {
                "-Ksh %,.0f".format(transaction.amount)
            } else {
                "+Ksh %,.0f".format(transaction.amount)
            }
            val amountColor = if (transaction.isExpense) Color(0xFFEF5350) else Color(0xFF66BB6A)

            Text(
                text = amountText,
                color = amountColor,
                fontSize = 15.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}
