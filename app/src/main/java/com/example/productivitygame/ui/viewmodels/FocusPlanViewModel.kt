package com.example.productivitygame.ui.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.productivitygame.data.FocusPlan
import com.example.productivitygame.data.FocusPlanDetails
import com.example.productivitygame.data.dao.FocusPlanDao
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class FocusPlanViewModel(focusPlanDao: FocusPlanDao): ViewModel() {
    val focusPlanList: StateFlow<List<FocusPlan>> =
        focusPlanDao
        .getAllFocusPlans()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
            initialValue = listOf()
        )

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }
}