package com.mpesa.tracker.ui.budget

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mpesa.tracker.R
import com.mpesa.tracker.data.model.BudgetWithSpent
import com.mpesa.tracker.databinding.ItemBudgetBinding

class BudgetAdapter(
    private val onDelete: (BudgetWithSpent) -> Unit
) : ListAdapter<BudgetWithSpent, BudgetAdapter.ViewHolder>(DIFF) {

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<BudgetWithSpent>() {
            override fun areItemsTheSame(a: BudgetWithSpent, b: BudgetWithSpent) = a.budget.id == b.budget.id
            override fun areContentsTheSame(a: BudgetWithSpent, b: BudgetWithSpent) = a == b
        }

        private val CATEGORY_EMOJI = mapOf(
            "Groceries"    to "🛒",
            "Utilities"    to "💡",
            "Transport"    to "🚌",
            "Food & Dining" to "🍽️",
            "Airtime"      to "📱",
            "Entertainment" to "🎬",
            "Health"       to "🏥",
            "Education"    to "📚",
            "Rent"         to "🏠",
            "Internet"     to "🌐",
            "Transfer"     to "💸",
            "Withdrawal"   to "🏧",
            "Other"        to "📦"
        )
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemBudgetBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, pos: Int) = holder.bind(getItem(pos))

    inner class ViewHolder(private val b: ItemBudgetBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(item: BudgetWithSpent) {
            val ctx = b.root.context
            b.tvCategory.text      = item.budget.category
            b.tvCategoryEmoji.text = CATEGORY_EMOJI[item.budget.category] ?: "💰"
            b.tvSpent.text         = "Ksh %,.0f spent of %,.0f".format(item.spent, item.budget.limitAmount)
            b.progressBar.progress = item.progressPercent

            val (indicatorColor, remainingColor, remainingText) = when {
                item.isOverBudget            -> Triple(
                    ContextCompat.getColor(ctx, R.color.expense_red),
                    ContextCompat.getColor(ctx, R.color.expense_red),
                    "Over by Ksh %,.0f".format(item.spent - item.budget.limitAmount)
                )
                item.progressPercent >= 80  -> Triple(
                    ContextCompat.getColor(ctx, R.color.warning_amber),
                    ContextCompat.getColor(ctx, R.color.warning_amber),
                    "Ksh %,.0f left".format(item.remaining)
                )
                else                        -> Triple(
                    ContextCompat.getColor(ctx, R.color.purple_primary),
                    ContextCompat.getColor(ctx, R.color.income_green),
                    "Ksh %,.0f left".format(item.remaining)
                )
            }

            b.progressBar.setIndicatorColor(indicatorColor)
            b.tvRemaining.setTextColor(remainingColor)
            b.tvRemaining.text = remainingText

            if (item.isOverBudget) {
                b.tvOverBudget.visibility = View.VISIBLE
                b.tvOverBudget.text = "⚠️  Over budget by Ksh %,.0f!".format(item.spent - item.budget.limitAmount)
            } else {
                b.tvOverBudget.visibility = View.GONE
            }

            b.btnDelete.setOnClickListener { onDelete(item) }
        }
    }
}
