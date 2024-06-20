package com.example.productivitygame.ui

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.productivitygame.ProductivityGameApplication
import com.example.productivitygame.ui.viewmodels.AddTaskViewModel
import com.example.productivitygame.ui.viewmodels.EditTaskViewModel
import com.example.productivitygame.ui.viewmodels.ScheduleViewModel


object AppViewModelProvider {
    val Factory = viewModelFactory {
        // Initializer for ItemEditViewModel
        initializer {
            AddTaskViewModel(
                recurringCatAndTaskDao = application().container.recurringCatAndTaskDao
            )
        }
        initializer {
            ScheduleViewModel(
                recurringCatAndTaskDao = application().container.recurringCatAndTaskDao
            )
        }
        initializer {
            EditTaskViewModel(
                recurringCatAndTaskDao = application().container.recurringCatAndTaskDao,
                savedStateHandle = this.createSavedStateHandle()
            )
        }
    }
}

fun CreationExtras.application(): ProductivityGameApplication =
    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as ProductivityGameApplication)