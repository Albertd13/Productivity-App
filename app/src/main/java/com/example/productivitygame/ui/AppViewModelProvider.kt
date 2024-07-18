package com.example.productivitygame.ui

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.productivitygame.ProductivityGameApplication
import com.example.productivitygame.ui.viewmodels.AddTaskViewModel
import com.example.productivitygame.ui.viewmodels.EditTaskViewModel
import com.example.productivitygame.ui.viewmodels.FocusPlanViewModel
import com.example.productivitygame.ui.viewmodels.ScheduleViewModel
import com.example.productivitygame.ui.viewmodels.TimerViewModel


object AppViewModelProvider {
    val Factory = viewModelFactory {
        // Initializer for ItemEditViewModel
        initializer {
            AddTaskViewModel(
                recurringCatAndTaskDao = application().container.recurringCatAndTaskDao,
                notificationExactScheduler = application().container.notificationScheduler,
                this.createSavedStateHandle()
            )
        }
        initializer {
            ScheduleViewModel(
                recurringCatAndTaskDao = application().container.recurringCatAndTaskDao,
                notificationExactScheduler = application().container.notificationScheduler
            )
        }
        initializer {
            EditTaskViewModel(
                recurringCatAndTaskDao = application().container.recurringCatAndTaskDao,
                notificationExactScheduler = application().container.notificationScheduler,
                savedStateHandle = this.createSavedStateHandle()
            )
        }
        initializer {
            TimerViewModel(
                timerServiceManager = application().container.foreGroundServiceManager,
                focusPlanDao = application().container.focusPlanDao,
                savedStateHandle = this.createSavedStateHandle()
            )
        }
        initializer {
            FocusPlanViewModel(application().container.focusPlanDao)
        }
    }
}

fun CreationExtras.application(): ProductivityGameApplication =
    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as ProductivityGameApplication)