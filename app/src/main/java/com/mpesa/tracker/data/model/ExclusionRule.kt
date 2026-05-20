package com.mpesa.tracker.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * A keyword-based rule that prevents matching transactions from being saved.
 *
 * matchType controls how the keyword is compared against the
 * normalized transaction name (already parsed — NOT raw SMS):
 *   CONTAINS    → keyword appears anywhere in the name  (most flexible)
 *   EXACT       → name equals keyword exactly
 *   STARTS_WITH → name starts with keyword
 *
 * isEnabled lets users pause a rule without deleting it.
 *
 * ⚠️ This entity is evaluated AFTER a transaction is created by the parser.
 *    It is NOT part of the SMS parsing logic.
 */
@Entity(
    tableName = "exclusion_rules",
    indices = [Index(value = ["keyword"], unique = true)]
)
data class ExclusionRule(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val keyword: String,                // e.g. "m-shwari", "ziidi", "international"
    val matchType: MatchType = MatchType.CONTAINS,
    val isEnabled: Boolean = true,
    val isPreset: Boolean = false,      // true = seeded by app, false = user-added
    val createdAt: Long = System.currentTimeMillis()
) {
    enum class MatchType { EXACT, CONTAINS, STARTS_WITH }

    /**
     * Returns true if this rule matches the given normalized transaction name.
     * Called in the repository post-processing layer.
     */
    fun matches(normalizedName: String): Boolean {
        if (!isEnabled) return false
        val kw = keyword.lowercase().trim()
        return when (matchType) {
            MatchType.EXACT       -> normalizedName == kw
            MatchType.CONTAINS    -> normalizedName.contains(kw)
            MatchType.STARTS_WITH -> normalizedName.startsWith(kw)
        }
    }
}
