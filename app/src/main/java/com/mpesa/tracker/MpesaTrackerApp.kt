package com.mpesa.tracker

import android.app.Application
import com.mpesa.tracker.data.db.AppDatabase
import com.mpesa.tracker.data.repository.TransactionRepository
import com.mpesa.tracker.utils.NotificationHelper

class MpesaTrackerApp : Application() {

    val database by lazy { AppDatabase.getInstance(this) }

    val repository by lazy {
        TransactionRepository(
            database.transactionDao(),
            database.budgetDao(),
            database.categoryMappingDao(),
            database.categoryRuleDao(),
            database.exclusionRuleDao(),
            database.categoryDao()
        )
    }

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createChannel(this)
    }
}
