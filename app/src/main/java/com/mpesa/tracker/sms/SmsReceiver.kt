package com.mpesa.tracker.sms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Log
import com.mpesa.tracker.MpesaTrackerApp
import com.mpesa.tracker.data.model.Transaction
import com.mpesa.tracker.utils.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Listens for incoming SMS messages, detects M-Pesa transactions,
 * and saves them to the local Room database automatically.
 */
class SmsReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "SmsReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        if (messages.isNullOrEmpty()) return

        // Reconstruct full message body (may be split into multiple parts)
        val sender = messages[0].originatingAddress ?: ""
        val body = messages.joinToString("") { it.messageBody }
        val timestamp = messages[0].timestampMillis

        Log.d(TAG, "SMS received from: $sender")

        if (!MpesaParser.isMpesaMessage(sender, body)) {
            Log.d(TAG, "Not an M-Pesa message, skipping.")
            return
        }

        Log.d(TAG, "M-Pesa SMS detected. Parsing...")

        val transaction = MpesaParser.parse(sender, body, timestamp) ?: run {
            Log.w(TAG, "Failed to parse M-Pesa message: $body")
            return
        }

        Log.d(TAG, "Parsed: ${transaction.type} Ksh${transaction.amount} -> ${transaction.recipient}")

        // Save to database on IO thread
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val app = context.applicationContext as MpesaTrackerApp
                val repo = app.repository

                // Avoid duplicates
                val existing = repo.findByTransactionId(transaction.transactionId)
                if (existing != null) {
                    Log.d(TAG, "Duplicate transaction ${transaction.transactionId}, skipping.")
                    return@launch
                }

                // Smart categorization is handled within repository.insertTransaction()
                val savedTransaction = repo.insertTransaction(transaction)
                if (savedTransaction != null) {
                    Log.d(TAG, "Transaction saved successfully with category: ${savedTransaction.category}")

                    // Post a notification with the smart category
                    NotificationHelper.showTransactionNotification(context, savedTransaction)
                } else {
                    Log.d(TAG, "Transaction excluded or failed to save.")
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error saving transaction: ${e.message}", e)
            }
        }
    }
}
