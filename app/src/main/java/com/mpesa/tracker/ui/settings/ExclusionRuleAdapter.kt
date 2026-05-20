package com.mpesa.tracker.ui.settings

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mpesa.tracker.data.model.ExclusionRule
import com.mpesa.tracker.databinding.ItemExclusionRuleBinding

class ExclusionRuleAdapter(
    private val onToggle: (ExclusionRule, Boolean) -> Unit,
    private val onDelete: (ExclusionRule) -> Unit
) : ListAdapter<ExclusionRule, ExclusionRuleAdapter.ViewHolder>(DIFF) {

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<ExclusionRule>() {
            override fun areItemsTheSame(a: ExclusionRule, b: ExclusionRule) = a.id == b.id
            override fun areContentsTheSame(a: ExclusionRule, b: ExclusionRule) = a == b
        }

        private val PRESET_EMOJI = mapOf(
            "m-shwari"      to "🏦",
            "mshwari"       to "🏦",
            "fuliza"        to "💳",
            "ziidi"         to "📈",
            "m-pawa"        to "💳",
            "international" to "🌍",
            "western union" to "🌍",
            "world remit"   to "🌍",
            "moneygram"     to "🌍",
            "loan"          to "📋",
            "repayment"     to "📋",
            "interest"      to "📋"
        )
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemExclusionRuleBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, pos: Int) = holder.bind(getItem(pos))

    inner class ViewHolder(private val b: ItemExclusionRuleBinding) :
        RecyclerView.ViewHolder(b.root) {

        fun bind(rule: ExclusionRule) {
            b.tvKeyword.text = rule.keyword
            b.tvMatchType.text = when (rule.matchType) {
                ExclusionRule.MatchType.CONTAINS    -> "contains"
                ExclusionRule.MatchType.EXACT       -> "exact match"
                ExclusionRule.MatchType.STARTS_WITH -> "starts with"
            }
            b.tvEmoji.text = PRESET_EMOJI.entries
                .firstOrNull { rule.keyword.contains(it.key) }?.value
                ?: if (rule.isPreset) "🔒" else "🚫"

            b.tvPresetBadge.visibility = if (rule.isPreset) View.VISIBLE else View.GONE

            // Suppress listener during bind to avoid recursion
            b.switchEnabled.setOnCheckedChangeListener(null)
            b.switchEnabled.isChecked = rule.isEnabled
            b.switchEnabled.setOnCheckedChangeListener { _, isChecked ->
                onToggle(rule, isChecked)
            }

            // Preset rules can be toggled but not deleted
            b.btnDelete.visibility = if (rule.isPreset) View.GONE else View.VISIBLE
            b.btnDelete.setOnClickListener { onDelete(rule) }
        }
    }
}
