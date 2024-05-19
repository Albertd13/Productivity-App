package com.example.productivitygame.data

import android.content.Context

class AppContainer(private val context: Context) {
    val taskDao: TaskDao by lazy {
        TaskDatabase.getDatabase(context).taskDao()
    }
}