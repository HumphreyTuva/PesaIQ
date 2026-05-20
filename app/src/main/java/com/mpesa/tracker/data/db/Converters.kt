package com.mpesa.tracker.data.db

import androidx.room.TypeConverter
import com.mpesa.tracker.data.model.TransactionType

class Converters {
    @TypeConverter
    fun fromTransactionType(type: TransactionType): String = type.name

    @TypeConverter
    fun toTransactionType(value: String): TransactionType =
        TransactionType.valueOf(value)
}
