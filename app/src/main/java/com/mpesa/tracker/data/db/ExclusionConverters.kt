package com.mpesa.tracker.data.db

import androidx.room.TypeConverter
import com.mpesa.tracker.data.model.ExclusionRule

class ExclusionConverters {
    @TypeConverter
    fun fromMatchType(type: ExclusionRule.MatchType): String = type.name

    @TypeConverter
    fun toMatchType(value: String): ExclusionRule.MatchType =
        ExclusionRule.MatchType.valueOf(value)
}
