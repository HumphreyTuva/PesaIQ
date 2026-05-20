package com.mpesa.tracker.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.mpesa.tracker.R
import com.mpesa.tracker.data.model.Transaction
import com.mpesa.tracker.data.model.TransactionType
import com.mpesa.tracker.ui.MainActivity

object NotificationHelper {

    private const val CHANNEL_ID   = "mpesa_transactions"
    private const val CHANNEL_NAME = "M-Pesa Transactions"

    fun createChannel(context: Context) {
        val mgr = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // FIX 1: Elevate to IMPORTANCE_HIGH so it can drop down as a dynamic banner alert
        val channel = NotificationChannel(
            CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Notifications for new M-Pesa transactions"
            enableVibration(true) // Give tactile confirmation to the user
        }
        mgr.createNotificationChannel(channel)
    }

    fun showTransactionNotification(context: Context, transaction: Transaction) {
        val mgr = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pi = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val emoji = if (transaction.type == TransactionType.RECEIVE) "💰" else "💸"
        val title = "$emoji ${transaction.type.label}: ${transaction.formattedAmount()}"
        val text  = when {
            transaction.recipient != null -> "${transaction.type.label} ${if (transaction.type.isExpense) "to" else "from"} ${transaction.recipient}"
            transaction.phone != null     -> "${transaction.type.label} ${transaction.phone}"
            else                          -> transaction.category
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_micon)

            // OPTIONAL ADDITION: Tint your white icon silhouette with your premium theme color space
            // Assuming your neon accent color is named 'accent_neon' or 'colorPrimary' inside colors.xml
            .setColor(context.getColor(R.color.colorPrimary))

            .setContentTitle(title)
            .setContentText(text)

            // FIX 2: Set priority to HIGH so Android matches the channel setup precisely
            .setPriority(NotificationCompat.PRIORITY_HIGH)

            .setContentIntent(pi)
            .setAutoCancel(true)
            .build()

        mgr.notify(transaction.transactionId.hashCode(), notification)
    }
}