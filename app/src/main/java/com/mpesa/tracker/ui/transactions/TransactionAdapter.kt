package com.mpesa.tracker.ui.transactions

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.graphics.ColorUtils
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.color.MaterialColors
import com.mpesa.tracker.R
import com.mpesa.tracker.data.model.Transaction
import com.mpesa.tracker.data.model.TransactionType
import com.mpesa.tracker.databinding.ItemTransactionBinding
import java.text.SimpleDateFormat
import java.util.*

class TransactionAdapter(
    private val onClick: (Transaction) -> Unit
) : ListAdapter<Transaction, TransactionAdapter.ViewHolder>(DIFF) {

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<Transaction>() {
            override fun areItemsTheSame(a: Transaction, b: Transaction) = a.id == b.id
            override fun areContentsTheSame(a: Transaction, b: Transaction) = a == b
        }

        private val TYPE_EMOJI = mapOf(
            TransactionType.RECEIVE   to "💰",
            TransactionType.SEND      to "📤",
            TransactionType.PAYBILL   to "📄",
            TransactionType.BUY_GOODS to "🛍️",
            TransactionType.WITHDRAW  to "🏧",
            TransactionType.AIRTIME   to "📱",
            TransactionType.UNKNOWN   to "❓"
        )
    }

    private val dateFmt = SimpleDateFormat("dd MMM", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemTransactionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(getItem(position))

    inner class ViewHolder(private val b: ItemTransactionBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(tx: Transaction) {
            val ctx = b.root.context

            b.tvName.text     = tx.displayName().uppercase()
            b.tvDate.text     = "${dateFmt.format(Date(tx.timestamp))} • ${tx.category}"
            b.tvTypeEmoji.text = TYPE_EMOJI[tx.type] ?: "💸"

            // Amount with sign and colour
            val amountStr = if (tx.isExpense) "-Ksh %,.0f".format(tx.amount)
                            else "+Ksh %,.0f".format(tx.amount)
            b.tvAmount.text = amountStr
            
            val greenColor = MaterialColors.getColor(ctx, com.google.android.material.R.attr.colorPrimary, Color.GREEN)
            val redColor = MaterialColors.getColor(ctx, com.google.android.material.R.attr.colorError, Color.RED)
            
            b.tvAmount.setTextColor(
                if (tx.isExpense) redColor
                else greenColor
            )

            // Icon background colour by type - updated for dynamic theme
            val typeColor = when(tx.type) {
                TransactionType.RECEIVE   -> "#00E676"
                TransactionType.SEND      -> "#FF9800"
                TransactionType.PAYBILL   -> "#00BCD4"
                TransactionType.BUY_GOODS -> "#9D4EDD"
                TransactionType.WITHDRAW  -> "#ADB5BD"
                TransactionType.AIRTIME   -> "#03A9F4"
                else -> "#888888"
            }
            val parsedColor = Color.parseColor(typeColor)
            b.cardTypeIcon.strokeColor = parsedColor
            b.cardTypeIcon.setCardBackgroundColor(ColorUtils.setAlphaComponent(parsedColor, 26)) // ~10% alpha

            // Visual feedback for excluded transactions
            if (tx.isExcluded) {
                b.root.alpha = 0.5f
                b.tvName.text = "${tx.displayName().uppercase()} (EXCLUDED)"
            } else {
                b.root.alpha = 1.0f
                b.tvName.text = tx.displayName().uppercase()
            }

            b.root.setOnClickListener { onClick(tx) }
        }
    }
}
