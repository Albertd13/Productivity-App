package com.example.productivitygame.ui

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.productivitygame.ProductivityGameApplication


object AppViewModelProvider {
    val Factory = viewModelFactory {
        // Initializer for ItemEditViewModel
        initializer {
            val app = application()
            AddTaskViewModel(
                recurringCatAndTaskDao = app.container.recurringCatAndTaskDao
            )
        }
    }
}

fun CreationExtras.application(): ProductivityGameApplication =
    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as ProductivityGameApplication)