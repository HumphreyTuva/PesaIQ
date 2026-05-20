package com.mpesa.tracker.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.*

enum class TransactionType(val label: String, val isExpense: Boolean) {
    SEND("Sent", true),
    RECEIVE("Received", false),
    PAYBILL("Paybill", true),
    BUY_GOODS("Buy Goods", true),
    WITHDRAW("Withdraw", true),
    AIRTIME("Airtime", true),
    UNKNOWN("Unknown", true)
}

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val transactionId: String,           // M-Pesa reference code e.g. QGH1234XY
    val type: TransactionType,
    val amount: Double,
    val recipient: String? = null,       // Name of recipient/business
    val phone: String? = null,           // Phone number if available
    val balance: Double? = null,         // M-Pesa balance after transaction
    val category: String = "Other",      // Auto-categorized: Groceries, Utilities, etc.
    val note: String? = null,            // User-added note
    val rawSms: String,                  // Original SMS text for reference
    val timestamp: Long,                 // Unix epoch ms when SMS was received
    val isManual: Boolean = false,       // true if user added it manually
    val isExcluded: Boolean = false,     // true if excluded from calculations
    val isUserEdited: Boolean = false    // true if the category was manually set by user
) {
    val isExpense: Boolean get() = type.isExpense

    fun formattedAmount(): String {
        return "Ksh %,.2f".format(amount)
    }

    fun formattedDate(): String {
        val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    fun formattedDateShort(): String {
        val sdf = SimpleDateFormat("dd MMM", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    fun displayName(): String = recipient ?: phone ?: type.label
}
