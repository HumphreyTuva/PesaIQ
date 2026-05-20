package com.mpesa.tracker.data.db

import androidx.room.*
import com.mpesa.tracker.data.model.ExclusionRule
import kotlinx.coroutines.flow.Flow

@Dao
interface ExclusionRuleDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(rule: ExclusionRule): Long

    @Update
    suspend fun update(rule: ExclusionRule)

    @Delete
    suspend fun delete(rule: ExclusionRule)

    /** All rules as a live Flow — observed by SettingsFragment. */
    @Query("SELECT * FROM exclusion_rules ORDER BY isPreset DESC, keyword ASC")
    fun getAllRules(): Flow<List<ExclusionRule>>

    /** Only the enabled rules — loaded once per transaction insert for fast lookup. */
    @Query("SELECT * FROM exclusion_rules WHERE isEnabled = 1")
    suspend fun getEnabledRules(): List<ExclusionRule>

    /** Toggle a rule on/off without deleting it. */
    @Query("UPDATE exclusion_rules SET isEnabled = :enabled WHERE id = :id")
    suspend fun setEnabled(id: Long, enabled: Boolean)

    @Query("DELETE FROM exclusion_rules WHERE isPreset = 0")
    suspend fun deleteAllCustomRules()

    @Query("SELECT COUNT(*) FROM exclusion_rules")
    suspend fun count(): Int
}
