package com.example.productivitygame.ui

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.productivitygame.ProductivityGameApplication
import com.example.productivitygame.data.AppContainer


object AppViewModelProvider {
    val Factory = viewModelFactory {
        // Initializer for ItemEditViewModel
        initializer {
            AddTaskViewModel(
                inventoryApplication().container.taskDao
            )
        }
    }
}

fun CreationExtras.inventoryApplication(): ProductivityGameApplication =
    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as ProductivityGameApplication)