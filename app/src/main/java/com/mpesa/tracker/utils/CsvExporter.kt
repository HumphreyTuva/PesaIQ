package com.mpesa.tracker.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.mpesa.tracker.data.model.Transaction
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*

object CsvExporter {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    /**
     * Exports [transactions] to a CSV file in the app's cache directory.
     * Returns a shareable [Uri] via FileProvider.
     */
    fun export(context: Context, transactions: List<Transaction>): Uri {
        val filename = "mpesa_transactions_${System.currentTimeMillis()}.csv"
        val file = File(context.cacheDir, filename)

        FileWriter(file).use { writer ->
            // Header
            writer.append("Date,Transaction ID,Type,Category,Amount (Ksh),Recipient/Sender,Phone,Balance (Ksh),Note\n")

            // Rows
            transactions.forEach { tx ->
                writer.append(buildCsvRow(tx))
            }
        }

        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }

    private fun buildCsvRow(tx: Transaction): String {
        val date      = dateFormat.format(Date(tx.timestamp))
        val txId      = escapeCsv(tx.transactionId)
        val type      = escapeCsv(tx.type.label)
        val category  = escapeCsv(tx.category)
        val amount    = tx.amount.toString()
        val recipient = escapeCsv(tx.recipient ?: "")
        val phone     = escapeCsv(tx.phone ?: "")
        val balance   = tx.balance?.toString() ?: ""
        val note      = escapeCsv(tx.note ?: "")
        return "$date,$txId,$type,$category,$amount,$recipient,$phone,$balance,$note\n"
    }

    private fun escapeCsv(value: String): String {
        return if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            "\"${value.replace("\"", "\"\"")}\""
        } else value
    }

    /**
     * Creates a share [Intent] for the exported CSV file.
     */
    fun shareIntent(uri: Uri): Intent {
        return Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }
}
