package com.mpesa.tracker.sms

import android.content.Context
import android.net.Uri
import android.util.Log
import com.mpesa.tracker.MpesaTrackerApp

/**
 * Scans the device's SMS inbox for historical M-Pesa messages
 * and imports them into the local database.
 * Call this once on first launch (after READ_SMS permission granted).
 */
object SmsScanner {

    private const val TAG = "SmsScanner"
    private const val MAX_MESSAGES = 500

    suspend fun scanInbox(context: Context): Int {
        val app = context.applicationContext as MpesaTrackerApp
        val repo = app.repository
        var imported = 0

        val uri = Uri.parse("content://sms/inbox")
        val projection = arrayOf("_id", "address", "body", "date")
        val selection = "address LIKE '%MPESA%' OR address LIKE '%M-PESA%' OR address LIKE '%SAFARICOM%'"
        val sortOrder = "date DESC LIMIT $MAX_MESSAGES"

        context.contentResolver.query(uri, projection, selection, null, sortOrder)?.use { cursor ->
            val bodyIdx = cursor.getColumnIndexOrThrow("body")
            val addrIdx = cursor.getColumnIndexOrThrow("address")
            val dateIdx = cursor.getColumnIndexOrThrow("date")

            while (cursor.moveToNext()) {
                val sender = cursor.getString(addrIdx) ?: continue
                val body   = cursor.getString(bodyIdx) ?: continue
                val date   = cursor.getLong(dateIdx)

                val tx = MpesaParser.parse(sender, body, date) ?: continue

                // Skip if already in DB
                if (repo.findByTransactionId(tx.transactionId) != null) continue

                // Smart categorization is handled within repository.insertTransaction()
                val savedTx = repo.insertTransaction(tx)
                if (savedTx != null) {
                    imported++
                    Log.d(TAG, "Imported: ${tx.type} Ksh${tx.amount} (${tx.transactionId})")
                }
            }
        }

        Log.d(TAG, "Inbox scan complete. Imported $imported transactions.")
        return imported
    }
}
