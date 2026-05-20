package com.mpesa.tracker.sms

import com.mpesa.tracker.data.model.Transaction
import com.mpesa.tracker.data.model.TransactionType
import java.util.*
import java.util.regex.Pattern

/**
 * Parses M-Pesa SMS messages into structured Transaction objects.
 * Handles: Send Money, Receive Money, Paybill, Buy Goods, Withdraw, Airtime.
 */
object MpesaParser {

    // ── Regex Patterns ────────────────────────────────────────────────────────

    /** e.g. "QGH1234XY Confirmed. Ksh1,000.00 sent to JOHN DOE 0712345678 on 1/5/24 at 10:30 AM." */
    private val SEND = Pattern.compile(
        """(?:([A-Z0-9]{8,12})\s+)?Confirmed\.\s*Ksh([\d,]+\.?\d*)\s+sent to\s+(.+?)\s+(\d{9,12})\s+on\s+([\d/]+)\s+at\s+([\d:]+ [AP]M)""",
        Pattern.CASE_INSENSITIVE
    )

    /** e.g. "QGH1234XY Confirmed. You have received Ksh500.00 from JANE DOE 0798765432 on 1/5/24 at 11:00 AM." */
    private val RECEIVE = Pattern.compile(
        """(?:([A-Z0-9]{8,12})[\s.:]+)?(?:Confirmed[\s.:]*)?(?:You have received\s+)?Ksh[\s.]*([\d,]+\.?\d*)\s+(?:deposited to your M-PESA account by|has been received into your M-PESA account from|received into your M-PESA account from|received from|from|deposited to|transferred from|has been received into|received into|received by|received by agent)\s+(.+?)(?=\s+on\s+\d|\s+New M-PESA|\s+balance|\.|\s*$)""",
        Pattern.CASE_INSENSITIVE
    )

    /** e.g. "Confirmed. Ksh2,000.00 paid to KPLC PREPAID 123456. on 1/5/24 at 12:00 PM." */
    private val PAYBILL = Pattern.compile(
        """(?:([A-Z0-9]{8,12})\s+)?Confirmed\.\s*Ksh([\d,]+\.?\d*)\s+paid to\s+(.+?)\.\s+on\s+([\d/]+)\s+at\s+([\d:]+ [AP]M)""",
        Pattern.CASE_INSENSITIVE
    )

    /** e.g. "Confirmed. Ksh350.00 paid to NAIVAS SUPERMARKET. New M-PESA balance..." */
    private val BUY_GOODS = Pattern.compile(
        """(?:([A-Z0-9]{8,12})\s+)?Confirmed\.\s*Ksh([\d,]+\.?\d*)\s+paid to\s+(.+?)\.\s+New M-PESA""",
        Pattern.CASE_INSENSITIVE
    )

    /** e.g. "QGH1234XY Confirmed. Ksh3,000.00 withdrawn from agent JOHN AGENT 0799999 on 1/5/24..." */
    private val WITHDRAW = Pattern.compile(
        """(?:([A-Z0-9]{8,12})\s+)?Confirmed\.\s*Ksh([\d,]+\.?\d*)\s+withdrawn from agent\s+(.+?)\s+on\s+([\d/]+)\s+at\s+([\d:]+ [AP]M)""",
        Pattern.CASE_INSENSITIVE
    )

    /** e.g. "Ksh100.00 airtime for 0712345678 has been successfully topped up." */
    private val AIRTIME = Pattern.compile(
        """Ksh([\d,]+\.?\d*)\s+airtime for\s+(\d{9,12})\s+has been successfully topped up""",
        Pattern.CASE_INSENSITIVE
    )

    /** Extracts the remaining M-Pesa balance from any message */
    private val BALANCE = Pattern.compile(
        """New M-PESA balance is Ksh([\d,]+\.?\d*)""",
        Pattern.CASE_INSENSITIVE
    )

    // ── Public API ────────────────────────────────────────────────────────────

    /** Returns true if this SMS appears to be from M-Pesa */
    fun isMpesaMessage(sender: String, body: String): Boolean {
        val s = sender.uppercase()
        val b = body.uppercase()
        val senderMatch = s.contains("MPESA") || s.contains("M-PESA") || s.contains("SAFARICOM")
        val bodyMatch = b.contains("KSH") && (
                b.contains("CONFIRMED") ||
                b.contains("M-PESA") ||
                b.contains("RECEIVED") ||
                b.contains("DEPOSITED") ||
                b.contains("TRANSFERRED") ||
                b.contains("SENT TO") ||
                b.contains("PAID TO") ||
                b.contains("WITHDRAW")
        )
        return senderMatch || bodyMatch
    }

    /**
     * Parses an M-Pesa SMS body into a [Transaction].
     * Returns null if the message cannot be parsed.
     */
    fun parse(sender: String, body: String, timestamp: Long = System.currentTimeMillis()): Transaction? {
        if (!isMpesaMessage(sender, body)) return null
        val balance = extractBalance(body)

        return tryParseSend(body, timestamp, balance)
            ?: tryParseReceive(body, timestamp, balance)
            ?: tryParsePaybill(body, timestamp, balance)
            ?: tryParseBuyGoods(body, timestamp, balance)
            ?: tryParseWithdraw(body, timestamp, balance)
            ?: tryParseAirtime(body, timestamp, balance)
    }

    // ── Private parsers ───────────────────────────────────────────────────────

    private fun tryParseSend(body: String, ts: Long, balance: Double?): Transaction? {
        val m = SEND.matcher(body)
        if (!m.find()) return null
        return Transaction(
            transactionId = m.group(1) ?: generateId(),
            type = TransactionType.SEND,
            amount = parseAmount(m.group(2) ?: return null),
            recipient = m.group(3)?.trim(),
            phone = m.group(4),
            timestamp = ts,
            balance = balance,
            rawSms = body,
            category = "Transfer"
        )
    }

    private fun tryParseReceive(body: String, ts: Long, balance: Double?): Transaction? {
        val m = RECEIVE.matcher(body)
        if (!m.find()) return null
        val amount = parseAmount(m.group(2) ?: return null)
        val sender = m.group(3)?.trim()?.removeSuffix(".") ?: "Unknown"

        return Transaction(
            transactionId = m.group(1) ?: generateId(),
            type = TransactionType.RECEIVE,
            amount = amount,
            recipient = sender,
            timestamp = ts,
            balance = balance,
            rawSms = body,
            category = "Income"
        )
    }

    private fun tryParsePaybill(body: String, ts: Long, balance: Double?): Transaction? {
        val m = PAYBILL.matcher(body)
        if (!m.find()) return null
        val biz = m.group(3)?.trim() ?: ""
        return Transaction(
            transactionId = m.group(1) ?: generateId(),
            type = TransactionType.PAYBILL,
            amount = parseAmount(m.group(2) ?: return null),
            recipient = biz,
            timestamp = ts,
            balance = balance,
            rawSms = body,
            category = categorizeBusiness(biz)
        )
    }

    private fun tryParseBuyGoods(body: String, ts: Long, balance: Double?): Transaction? {
        val m = BUY_GOODS.matcher(body)
        if (!m.find()) return null
        val biz = m.group(3)?.trim() ?: ""
        return Transaction(
            transactionId = m.group(1) ?: generateId(),
            type = TransactionType.BUY_GOODS,
            amount = parseAmount(m.group(2) ?: return null),
            recipient = biz,
            timestamp = ts,
            balance = balance,
            rawSms = body,
            category = categorizeBusiness(biz)
        )
    }

    private fun tryParseWithdraw(body: String, ts: Long, balance: Double?): Transaction? {
        val m = WITHDRAW.matcher(body)
        if (!m.find()) return null
        return Transaction(
            transactionId = m.group(1) ?: generateId(),
            type = TransactionType.WITHDRAW,
            amount = parseAmount(m.group(2) ?: return null),
            recipient = m.group(3)?.trim(),
            timestamp = ts,
            balance = balance,
            rawSms = body,
            category = "Withdrawal"
        )
    }

    private fun tryParseAirtime(body: String, ts: Long, balance: Double?): Transaction? {
        val m = AIRTIME.matcher(body)
        if (!m.find()) return null
        return Transaction(
            transactionId = generateId(),
            type = TransactionType.AIRTIME,
            amount = parseAmount(m.group(1) ?: return null),
            phone = m.group(2),
            timestamp = ts,
            balance = balance,
            rawSms = body,
            category = "Airtime"
        )
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun extractBalance(body: String): Double? {
        val m = BALANCE.matcher(body)
        return if (m.find()) parseAmount(m.group(1) ?: return null) else null
    }

    private fun parseAmount(raw: String): Double =
        raw.replace(",", "").toDoubleOrNull() ?: 0.0

    private fun generateId(): String =
        UUID.randomUUID().toString().take(10).uppercase()

    /**
     * Rule-based auto-categorization of business names.
     * Extend this map as needed.
     */
    fun categorizeBusiness(name: String): String {
        val n = name.uppercase()
        return when {
            n.containsAny("KPLC", "KENYA POWER", "UMEME", "STIMA")       -> "Utilities"
            n.containsAny("NAIVAS", "QUICKMART", "CARREFOUR", "CLEANSHELF",
                "SUPERMARKET", "MARKET", "GROCERY")                        -> "Groceries"
            n.containsAny("SAFARICOM", "AIRTEL", "TELKOM", "FAIBA")       -> "Airtime"
            n.containsAny("NETFLIX", "SHOWMAX", "DSTV", "MULTICHOICE",
                "YOUTUBE", "SPOTIFY")                                      -> "Entertainment"
            n.containsAny("UBER", "BOLT", "LITTLE", "FARAS", "MATATU",
                "BUS", "TAXI", "TRANSPORT")                                -> "Transport"
            n.containsAny("NHIF", "HOSPITAL", "PHARMACY", "CLINIC",
                "HEALTH", "DOCTOR", "MEDICAL")                             -> "Health"
            n.containsAny("SCHOOL", "UNIVERSITY", "COLLEGE", "FEES",
                "TUITION", "EDUCATION")                                    -> "Education"
            n.containsAny("ZUKU", "SAFARICOM HOME", "FAIBA HOME",
                "INTERNET", "WIFI")                                        -> "Internet"
            n.containsAny("RESTAURANT", "HOTEL", "CAFE", "FOOD",
                "PIZZA", "CHICKEN", "KFC", "JAVA", "ART CAFFE")           -> "Food & Dining"
            n.containsAny("RENT", "LANDLORD", "APARTMENT", "HOUSE")       -> "Rent"
            else                                                           -> "Other"
        }
    }

    private fun String.containsAny(vararg keys: String) = keys.any { this.contains(it) }
}
