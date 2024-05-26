package com.example.productivitygame.data

import android.content.Context

class AppContainer(private val context: Context) {
    val recurringCatAndTaskDao: RecurringCatAndTaskDao by lazy {
        TaskDatabase.getDatabase(context).recurringTaskDao()
    }
}